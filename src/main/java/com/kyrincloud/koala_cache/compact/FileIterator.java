package com.kyrincloud.koala_cache.compact;

import java.nio.ByteBuffer;

public class FileIterator implements Comparable<FileIterator>{
	
	private ByteBuffer map;
	
	private String nextElement;
	
	public FileIterator(ByteBuffer map) {
		this.map = map;
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
			return 1;
		}
		if(nextElement == null && nextKey != null ){
			return -1;
		}
		if(nextKey == null && nextElement == null){
			return 0;
		}
		return nextElement.compareTo(nextKey);
	}

}
