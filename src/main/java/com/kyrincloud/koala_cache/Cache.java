package com.kyrincloud.koala_cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Cache {
	
	private List<String> cache = new ArrayList<String>();
	
	Map<String,Position> index = new TreeMap<String, Position>(new Comparator<String>() {

		public int compare(String o1, String o2) {
			return o1.compareTo(o2);
		}
	});

	
	AtomicInteger counter = new AtomicInteger(0);
	
	RandomAccessFile indexChannel;
	
	RandomAccessFile dataChannel;
	
	String indexPath ;
	
	String dataPath;
	
	public Cache(String indexPath , String dataPath) {
		try {
			this.indexPath = indexPath;
			this.dataPath = dataPath;
			
			indexChannel = new RandomAccessFile(new File(indexPath),"rw");
			dataChannel = new RandomAccessFile(new File(dataPath),"rw");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void put(String key){
		int count = key.getBytes().length;
		counter.addAndGet(count);
		cache.add(key);
	}
	
	public void flush() throws Exception{
		
		Iterator<String> keys = cache.iterator();
		long count = 0;
		int blockSize = 0;
		long start = -1;
		while(keys.hasNext()){
			String key = keys.next();
			Integer len = key.getBytes().length;
			if(start == -1){
				start=count;
			}
			count+=len+4;
			blockSize+=len+4;
			Slice values = new Slice(4+len);
			values.putInt(len);
			values.put(key.getBytes());
			dataChannel.write(values.array());
			if(blockSize>=32768){
				Position pos = Position.build(start, count, key);
				index.put(key, pos);
				blockSize=0;
				start = -1;
			}else if(!keys.hasNext()){
				Position pos = Position.build(start, count, key);
				index.put(key, pos);
				blockSize=0;
				start = -1;
			}
		}
		writeIndex(index);
	} 
	
	public void writeIndex(Map<String,Position> index) throws Exception{
		Iterator<String> keys = index.keySet().iterator();
		while(keys.hasNext()){
			String key = keys.next();
			Slice values = new Slice(4+8+8+key.length());
			values.putInt(key.length());
			values.put(key.getBytes());
			values.putLong(index.get(key).getStart());
			values.putLong(index.get(key).getEnd());
			indexChannel.write(values.array());
		}
	}
	
}
