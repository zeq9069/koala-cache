package com.kyrincloud.koala_cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.collect.Lists;
import com.kyrincloud.koala_cache.compact.FileIterator;
import com.kyrincloud.koala_cache.compact.MergeIterator;

/**
 * 缓存实现，提供持久化机制，同时，内存达到一定的大小之后会被持久化到硬盘，然后生成的多个文件进行归并，最终生成一个有序的数据文件
 * @author zhangerqiang
 *
 */
public class MemCache {
	
	private TreeMap<String,Byte> memcache ;
	
	private long size;
	
	private long MAX_SIZE = 2<<21;
	
	private ReentrantLock lock = new ReentrantLock();
	
	private AtomicInteger logNumber = new AtomicInteger(0);
	
	private ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
	
	private PriorityQueue<String> filenames = new PriorityQueue<String>(new Comparator<String>() {

		public int compare(String o1, String o2) {
			String name1 = o1.substring(0, o1.length()-4);
			String name2 = o1.substring(0, o1.length()-4);
			return name1.compareTo(name2);
		}
	});
	
	private String basePath;
	
	public MemCache(String basePath){
		this.basePath = basePath;
		memcache = new TreeMap<String, Byte>(new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
	}
	
	
	public void put(String key){
		try{
			lock.lock();
			size+=key.length()+4;
			memcache.put(key, (byte)0);
			if(size >= MAX_SIZE){
				mayScheduce();
			}
			if(filenames.size()>=2){
				mergeFile();
			}
		}finally{
			lock.unlock();
		}
		
	}
	
	private boolean mayScheduce(){
		TreeMap<String, Byte> immuMemcache = (TreeMap<String, Byte>) memcache.clone();
		memcache = new TreeMap<String, Byte>(new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		
		Slice data = new Slice((int) (4*immuMemcache.size() + size));
		
		for(String key : immuMemcache.keySet()){
			data.putInt(key.length());
			data.put(key.getBytes());
		}
		
		FileOutputStream fos;
		try {
			String filePath = basePath+"/"+logNumber+".data";
			fos = new FileOutputStream(new File(filePath));
			fos.write(data.array());
			fos.flush();
			fos.close();
			logNumber.incrementAndGet();
			size = 0;
			filenames.add(filePath);
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private boolean mergeFile(){
		try {
			File f1 = new File(filenames.poll());
			File f2 = new File(filenames.poll());
			FileInputStream fis1 = new FileInputStream(f1);
			FileInputStream fis2 = new FileInputStream(f2);
			FileIterator it1 = new FileIterator(fis1.getChannel());			
			FileIterator it2 = new FileIterator(fis2.getChannel());

			String filePath = basePath+"/"+logNumber+".data";
			
			FileOutputStream fos = new FileOutputStream(new File(filePath));
			
			MergeIterator merge = new MergeIterator(Lists.newArrayList(it1,it2));
			
			int num = 0;
			while(merge.hasNext()){
				String key = merge.getNextElement();
				num+=4+key.length();
				Slice slice = new Slice(key.length()+4);
				slice.putInt(key.length());
				slice.put(key.getBytes());
				fos.write(slice.array());
				if(num>=0){
					fos.flush();
					num = 0;
				}
			}
			
			fos.flush();
			
			fos.close();
			fis1.close();
			fis2.close();
			
			f1.delete();
			f2.delete();
			
			
			logNumber.incrementAndGet();
			filenames.add(filePath);
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void main(String[] args) {
		MemCache cache = new MemCache("/tmp");
		for(int i = 0 ; i< 1000000;i++){
    		String key = i+"";
    		for(int j = key.length();j<10;j++){
    			key="0"+key;
    		}
    		cache.put(key);
    	}
	}
	
}
