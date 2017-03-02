package com.kyrincloud.koala_cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class IndexCache {
	
	private Map<String,Long> index = new HashMap<String, Long>();
	
	public String path;
	
	public String dataPath;
	
	
	
	public IndexCache(String indexPath , String dataPath){
		this.path = indexPath;
		this.dataPath = dataPath;
		try {
			load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void load() throws IOException{
		FileInputStream fis = new FileInputStream(new File(path));
		
		while(fis.available()>0){
			ByteBuffer k = ByteBuffer.allocate(4);
			fis.read(k.array());
			byte[] kk = new byte[k.getInt()];
			fis.read(kk);
			String key = new String(kk);
			
			ByteBuffer v = ByteBuffer.allocate(8);
			fis.read(v.array());
			
			index.put(key, v.getLong());
		}
		fis.close();
	}
	
	private Long get(String key){
		Iterator<String> keys = index.keySet().iterator();
		while(keys.hasNext()){
			String k = keys.next();
			int v = k.compareTo(key);
			if(v>=0){
				return index.get(k);
			}
		}
		return null;
	}
	
	public String search(String key) throws Exception{
		Long offset = get(key);
		if(offset == null){
			throw new Exception("offset is null");
		}
		FileInputStream fis = new FileInputStream(new File(dataPath));
		
		FileChannel channel = fis.getChannel();
		channel.position(offset);
		ByteBuffer size = ByteBuffer.allocate(4);
		channel.read(size);
		size.position(0);
		ByteBuffer value = ByteBuffer.allocate(size.getInt());
		channel.read(value);
		channel.close();
		return new String(value.array());
	}
	
	
	

}
