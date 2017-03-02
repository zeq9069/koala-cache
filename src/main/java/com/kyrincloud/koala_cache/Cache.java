package com.kyrincloud.koala_cache;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Cache {
	
	private List<String> cache = new ArrayList<String>();
	
	AtomicInteger counter = new AtomicInteger(0);
	
	private String indexPath;
	
	private String dataPath;
	
	public Cache(String indexPath , String dataPath) {
		this.indexPath = indexPath;
		this.dataPath = dataPath;
	}
	
	public void put(String key){
		int count = key.getBytes().length;
		counter.addAndGet(count);
		cache.add(key);
	}
	
	public void flush() throws Exception{
		Map<String,Long> index = new HashMap<String, Long>();
		
		FileOutputStream fos = new FileOutputStream(new File(dataPath));
		
		Iterator<String> keys = cache.iterator();
		long count = 0;
		int blockSize = 0;
		while(keys.hasNext()){
			String key = keys.next();
			Integer len = key.getBytes().length;
			count+=len+4;
			blockSize+=len+4;
			ByteBuffer values = ByteBuffer.allocate(4+len);
			values.putInt(len);
			values.put(key.getBytes());
			fos.write(values.array());
			if(blockSize>=32768){
				index.put(key, count-(len+4));
				blockSize=0;
			}else if(!keys.hasNext()){
				index.put(key, count-(len+4));
				blockSize=0;
			}
		}
		
		fos.close();
		writeIndex(index);
	} 
	
	public void writeIndex(Map<String,Long> index) throws Exception{
		FileOutputStream fos = new FileOutputStream(new File(indexPath));
		
		Iterator<String> keys = index.keySet().iterator();
		while(keys.hasNext()){
			String key = keys.next();
			ByteBuffer values = ByteBuffer.allocate(4+8+key.length());
			values.putInt(key.length());
			values.put(key.getBytes());
			values.putLong(index.get(key));
			fos.write(values.array());
		}
		fos.close();
	}
	
}
