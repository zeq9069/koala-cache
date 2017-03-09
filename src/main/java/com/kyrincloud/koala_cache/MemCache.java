package com.kyrincloud.koala_cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.kyrincloud.koala_cache.compact.FileIterator;
import com.kyrincloud.koala_cache.compact.MergeIterator;

/**
 * 核心操作类
 * 缓存实现，提供持久化机制，同时，内存达到一定的大小之后会被持久化到硬盘，然后生成的多个文件进行归并，最终生成一个有序的数据文件
 * @author zhangerqiang
 * 
 * Bug : 当put的数据量太大的时候，会导致old spaces 被打满(后期或许会使用堆外内存)（目前通过full gc是可以回收的）
 * Bug : 读和文件合并存在冲突的问题(已解决)
 * Bug : 读与文件合并的优化
 */
public class MemCache {
	
	private static final Logger LOG = LoggerFactory.getLogger(MemCache.class);
	
	private Table table ;
	
	private Table immuMemTable = null;
	
	private long MAX_SIZE = 2<<21;
	
	private ReentrantLock mergeLock = new ReentrantLock();
	
	private AtomicInteger logNumber = new AtomicInteger(0);
	
	private ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	private volatile boolean isSchedule = false;
	
	private volatile boolean isMerge = false;
	
	private FileMeta meta = new FileMeta();
	
	private String basePath;
	
	public MemCache(String basePath){
		this.basePath = basePath;
		table = new Table();
		initSchedule();
	}
	
	public void put(String key) {
		if (table.getSize() >= MAX_SIZE && isSchedule)
			try {
				TimeUnit.MILLISECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		table.put(key);
		
		if (table.getSize() >= MAX_SIZE && !isSchedule) {
			exec.submit(new Runnable() {
				public void run() {
					System.out.println("开始合并...");
					mayScheduce();
				}
			});
		}
	}
	
	public String get(String key){
		String value = table.get(key);
		if(value != null){
			return value;
		}
		if(immuMemTable != null){
			value = immuMemTable.get(key);
		}
		if(value != null){
			return value;
		}
		return meta.search(key);
	}
	
	private void initSchedule(){
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			
			@Override
			public void run() {
				if(!isSchedule && !table.isEmpty()){
					System.out.println("开始调度...");
					mayScheduce();
				}
				
				if(!isMerge && meta.live() >= 2){
					System.out.println("开始合并...");
					mergeFile();
				}
			}
		};
		timer.schedule(task, 60 * 1000,60 * 1000);
	}
	
	private void mayScheduce() {
		if (isSchedule) {
			return;
		}
		isSchedule = true;
		FileOutputStream fos = null;
		FileOutputStream indexFos = null;
		try {
			int currentNum = logNumber.incrementAndGet();
			String dataPath = basePath + "/" + currentNum + ".data";
			String indexPath = basePath + "/" + currentNum + ".index";
			
			// 持久化
			immuMemTable = table;
			if (immuMemTable == null) {
				return;
			}
			table = new Table();

			fos = new FileOutputStream(new File(dataPath));
			indexFos = new FileOutputStream(new File(indexPath));

			Map<String,Position> index = new TreeMap<String, Position>(new Comparator<String>() {

				public int compare(String o1, String o2) {
					return o1.compareTo(o2);
				}
			});
			
			long count = 0;
			int blockSize = 0;
			long start = -1;
			int total = 0;
			for (String key : immuMemTable.getKeySet()) {
				total++;
				if(start == -1){
					start=count;
				}
				Integer len = key.getBytes().length;
				if(start == -1){
					start=count;
				}
				count+=len+4;
				blockSize+=len+4;
				Slice values = new Slice(4+len);
				values.putInt(len);
				values.put(key.getBytes());
				fos.write(values.array());
				if(blockSize>=32768){
					Position pos = Position.build(start, count);
					index.put(key, pos);
					blockSize=0;
					start = -1;
				}else if(total == immuMemTable.getTableSize()){
					Position pos = Position.build(start, count);
					index.put(key, pos);
					blockSize=0;
					start = -1;
				}
			}
			
			fos.flush();

			writeIndex(index,indexFos);
			
			logNumber.incrementAndGet();
			meta.put(new FileData(indexPath, dataPath));
			// 文件合并
			if (meta.live() >= 2) {
				System.out.println("文件开始合并...");
				mergeFile();
			}

		} catch (FileNotFoundException e) {
			LOG.error("MemCache mayScheduce don't found file.",e);
		} catch (IOException e) {
			LOG.error("MemCache mayScheduce I/O exception.",e);
		} catch (Exception e) {
			LOG.error("MemCache mayScheduce exception.",e);
		} finally {
			isSchedule = false;
			immuMemTable = null;
			try {
				fos.close();
			} catch (IOException e) {
				LOG.error("I/O close fail.",e);
			}
		}
	}
	
	public void writeIndex(Map<String,Position> index , FileOutputStream fos) throws Exception{
		Iterator<String> keys = index.keySet().iterator();
		while(keys.hasNext()){
			String key = keys.next();
			Slice values = new Slice(4+8+8+key.length());
			values.putInt(key.length());
			values.put(key.getBytes());
			values.putLong(index.get(key).getStart());
			values.putLong(index.get(key).getEnd());
			fos.write(values.array());
		}
		fos.flush();
		fos.close();
	}
	
	@SuppressWarnings("resource")
	private boolean mergeFile() {
		if (isMerge) {
			return false;
		}
		FileOutputStream fos = null;
		FileOutputStream indexFos = null;
		FileInputStream fis1 = null;
		FileInputStream fis2 = null;
		try {
			isMerge = true;
			mergeLock.lock();
			FileData fileData1 = meta.toMerging();
			FileData fileData2 = meta.toMerging();

			File f1 = new File(fileData1.getDataPath());
			File f2 = new File(fileData2.getDataPath());

			fis1 = new FileInputStream(f1);
			fis2 = new FileInputStream(f2);
			FileIterator it1 = new FileIterator(fis1.getChannel());
			FileIterator it2 = new FileIterator(fis2.getChannel());

			int currentNum = logNumber.incrementAndGet();
			String dataPath = basePath + "/" + currentNum + ".data";
			String indexPath = basePath + "/" + currentNum + ".index";

			fos = new FileOutputStream(new File(dataPath));
			indexFos = new FileOutputStream(new File(indexPath));

			MergeIterator merge = new MergeIterator(Lists.newArrayList(it1, it2));

			Map<String, Position> index = new TreeMap<String, Position>(new Comparator<String>() {
				public int compare(String o1, String o2) {
					return o1.compareTo(o2);
				}
			});

			long count = 0;
			int blockSize = 0;
			long start = -1;
			String key = null;
			while (merge.hasNext()) {
				key = merge.next();
				if (start == -1) {
					start = count;
				}
				Integer len = key.getBytes().length;
				if (start == -1) {
					start = count;
				}
				count += len + 4;
				blockSize += len + 4;
				Slice values = new Slice(4 + len);
				values.putInt(len);
				values.put(key.getBytes());
				fos.write(values.array());
				if (blockSize >= 32768) {
					Position pos = Position.build(start, count);
					index.put(key, pos);
					blockSize = 0;
					start = -1;
				}
			}

			fos.flush();

			Position pos = Position.build(start, count);
			index.put(key, pos);

			writeIndex(index, indexFos);

			fis1.close();
			fis2.close();

			meta.put(new FileData(indexPath, dataPath));

			fileData1.setStatus(FileDataStatus.DELETED);
			fileData2.setStatus(FileDataStatus.DELETED);

			logNumber.incrementAndGet();
			return true;
		} catch (FileNotFoundException e) {
			LOG.error("MemCache mergeFile don't found file.",e);
		} catch (IOException e) {
			LOG.error("MemCache mergeFile I/O exception.",e);
		} catch (Exception e) {
			LOG.error("MemCache mergeFile exception.",e);
		} finally {
			isMerge = false;
			mergeLock.unlock();
		}
		return false;
	}
	
}
