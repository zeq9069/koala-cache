package com.kyrincloud.koala_cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.kyrincloud.koala_cache.compact.FileIterator;
import com.kyrincloud.koala_cache.compact.MergeIterator;

/**
 * 缓存实现，提供持久化机制，同时，内存达到一定的大小之后会被持久化到硬盘，然后生成的多个文件进行归并，最终生成一个有序的数据文件
 * @author zhangerqiang
 * bug : logNumber 存在并发问题
 */
public class MemCache {
	
	private Table table ;
	
	private long MAX_SIZE = 2<<21;
	
	private ReentrantLock mergeLock = new ReentrantLock();
	
	private AtomicInteger logNumber = new AtomicInteger(0);
	
	private ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
	
	private volatile boolean isSchedule = false;
	
	private volatile boolean isMerge = false;
	
	private PriorityQueue<FileData> filenames = new PriorityQueue<FileData>(new Comparator<FileData>() {

		public int compare(FileData o1, FileData o2) {
			return o1.getNumber().compareTo(o2.getNumber());
		}
	});
	
	private String basePath;
	
	public MemCache(String basePath){
		this.basePath = basePath;
		table = new Table();
		initSchedule();
	}
	
	public void put(String key){
		table.put(key);
		if(table.getSize() >= MAX_SIZE){
			exec.submit(new Runnable() {
				public void run() {
					mayScheduce();
				}
			});
		}
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
				
				if(!isMerge && filenames.size() >= 2){
					System.out.println("开始合并...");
					mergeFile();
				}
			}
		};
		timer.schedule(task, 10 * 1000,1000);
	}
	
	private void mayScheduce() {
		if (isSchedule) {
			return;
		}
		FileOutputStream fos = null;
		FileOutputStream indexFos = null;
		try {
			String dataPath = basePath + "/" + logNumber.incrementAndGet() + ".data";
			String indexPath = basePath + "/" + logNumber + ".index";
			
			isSchedule = true;
			// 持久化
			Table immuMemcache = table.clone();
			if (immuMemcache == null) {
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
			for (String key : immuMemcache.getKeySet()) {
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
					Position pos = Position.build(start, count, key);
					index.put(key, pos);
					blockSize=0;
					start = -1;
				}else if(total == immuMemcache.getTableSize()){
					Position pos = Position.build(start, count, key);
					index.put(key, pos);
					blockSize=0;
					start = -1;
				}
			}
			
			fos.flush();

			writeIndex(index,indexFos);
			
			logNumber.incrementAndGet();
			filenames.add(new FileData(indexPath, dataPath));

			// 文件合并
			if (filenames.size() >= 2) {
				System.out.println("文件开始合并...");
				mergeFile();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			isSchedule = false;
			try {
				fos.close();
			} catch (IOException e) {
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
		if(isMerge){
			return false;
		}
		FileOutputStream fos = null;
		FileOutputStream indexFos = null;
		FileInputStream fis1 = null;
		FileInputStream fis2 = null;
		try {
			isMerge = true;
			mergeLock.lock();
			FileData fileData1 = filenames.poll();
			FileData fileData2 = filenames.poll();

			File f1 = new File(fileData1.getDataPath());
			File f2 = new File(fileData2.getDataPath());
			
			fis1 = new FileInputStream(f1);
			fis2 = new FileInputStream(f2);
			FileIterator it1 = new FileIterator(fis1.getChannel());
			FileIterator it2 = new FileIterator(fis2.getChannel());

			String dataPath = basePath + "/" + logNumber.incrementAndGet() + ".data";
			String indexPath = basePath + "/" + logNumber + ".index";

		    fos = new FileOutputStream(new File(dataPath));
		    indexFos = new FileOutputStream(new File(indexPath));
		    
			MergeIterator merge = new MergeIterator(Lists.newArrayList(it1, it2));

			Map<String,Position> index = new TreeMap<String, Position>(new Comparator<String>() {
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
					Position pos = Position.build(start, count, key);
					index.put(key, pos);
					blockSize=0;
					start = -1;
				}
			}
			
			fos.flush();
			
			Position pos = Position.build(start, count, key);
			index.put(key, pos);
			
			writeIndex(index, indexFos);
			
			fis1.close();
			fis2.close();
			
			f1.delete();
			f2.delete();
			
			new File(fileData1.getIndexPath()).delete();
			new File(fileData2.getIndexPath()).delete();

			logNumber.incrementAndGet();
			filenames.add(new FileData(indexPath, dataPath));
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			isMerge = false;
			mergeLock.unlock();
		}
		return false;
	}
	
	public static void main(String[] args) {
		MemCache cache = new MemCache("/tmp");
		Stopwatch s = Stopwatch.createStarted();
		for(int i = 0 ; i< 10000000;i++){
    		String key = i+"";
    		for(int j = key.length();j<10;j++){
    			key="0"+key;
    		}
    		cache.put(key);
    	}
		System.out.println(s.elapsed(TimeUnit.MILLISECONDS));
		
	}
	
}
