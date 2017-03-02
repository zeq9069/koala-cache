package com.kyrincloud.koala_cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class IndexCache {
	
	private Map<String,Position> index = new TreeMap<String, Position>(new Comparator<String>() {

		public int compare(String o1, String o2) {
			return o1.compareTo(o2);
		}
	});
	
	FileInputStream indexChannel;
	
	FileInputStream dataChannel;
	
	public String path;
	
	public String dataPath;
	
	public IndexCache(String indexPath , String dataPath){
		this.path = indexPath;
		this.dataPath = dataPath;
		try {
			indexChannel = new FileInputStream(new File(indexPath));
			dataChannel = new FileInputStream(new File(dataPath));
			
			load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void load() throws IOException{
		FileInputStream fis = indexChannel;
		
		while(indexChannel.available()>0){
			ByteBuffer k = ByteBuffer.allocate(4);
			fis.read(k.array());
			byte[] kk = new byte[k.getInt()];
			fis.read(kk);
			String key = new String(kk);
			
			ByteBuffer start = ByteBuffer.allocate(8);
			fis.read(start.array());
			start.position(0);
			
			ByteBuffer end = ByteBuffer.allocate(8);
			fis.read(end.array());
			end.position(0);
			
			index.put(key,Position.build(start.getLong(), end.getLong(), key));
		}
	}
	
	public Position get(String key){
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
	
	public String search(String key , long offset , long allSize) throws Exception{
		
		FileChannel channel = dataChannel.getChannel();
		channel.position(offset);
		ByteBuffer size = ByteBuffer.allocate(4);
		channel.read(size);
		size.position(0);
		ByteBuffer value = ByteBuffer.allocate(size.getInt());
		channel.read(value);
		
		String res = new String(value.array());
		
		if(res.equals(key)){
			return res;
		}
		long p = channel.position();
		if(p+(key.length()+4) <= allSize+1){
			return search(key, p, allSize);
		}
		return null;
	}
	
public String searchCache(String key , long offset , long allSize) throws Exception{
		
		FileInputStream fis = dataChannel;
		FileChannel channel = fis.getChannel();
		channel.position(offset);
		ByteBuffer block = ByteBuffer.allocate((int)(allSize-offset+1));
		channel.read(block);
		block.position(0);
		
		Block b = new Block(block);
		String result = b.get(key);
		System.out.println("查找次数："+b.count);
		return result;
	}
}
