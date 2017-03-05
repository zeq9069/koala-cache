package com.kyrincloud.koala_cache.compact;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class FileIterator implements Comparable<FileIterator>{
	
	private MappedByteBuffer map;
	
	private String nextElement;
	
	public FileIterator(FileChannel channel) {
		try {
			map = channel.map(MapMode.READ_ONLY, 0,channel.size());
			hasNext();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getNextElement() {
		return nextElement;
	}
	
	public boolean hasNext(){
		if(map.remaining() > 4){
			int len = map.getInt();
			byte[] key = new byte[len];
			map.get(key);
			nextElement = new String(key);
			return true;
		}
		nextElement = null;
		return false;
	}
	
	public String next(){
		String key = nextElement;
		return key;
	}

	public int compareTo(FileIterator iterator) {
		String nextKey = iterator.getNextElement();
		if(nextKey == null && nextElement != null ){
			return -1;
		}
		if(nextElement == null && nextKey != null ){
			return 1;
		}
		if(nextKey == null && nextElement == null){
			return 0;
		}
		return nextElement.compareTo(nextKey);
	}

}
