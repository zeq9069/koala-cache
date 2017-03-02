package com.kyrincloud.koala_cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Optional;

public class Cache {
	
	private List<String> cache = new ArrayList<String>();
	
	AtomicInteger counter = new AtomicInteger(0);
	
	FileOutputStream indexChannel;
	
	FileOutputStream dataChannel;
	
	String indexPath ;
	
	String dataPath;
	
	public Cache(String indexPath , String dataPath) {
		try {
			this.indexPath = indexPath;
			this.dataPath = dataPath;
			
			indexChannel = new FileOutputStream(new File(indexPath));
			dataChannel = new FileOutputStream(new File(dataPath));

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
		
		Map<String,Position> index = new HashMap<String, Position>();
		
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
			ByteBuffer values = ByteBuffer.allocate(4+len);
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
		dataChannel.flush();
		writeIndex(index);

	} 
	
	public void writeIndex(Map<String,Position> index) throws Exception{
		
		Iterator<String> keys = index.keySet().iterator();
		while(keys.hasNext()){
			String key = keys.next();
			ByteBuffer values = ByteBuffer.allocate(4+8+8+key.length());
			values.putInt(key.length());
			values.put(key.getBytes());
			values.putLong(index.get(key).getStart());
			values.putLong(index.get(key).getEnd());
			indexChannel.write(values.array());
		}
		indexChannel.flush();
	}
	
}
