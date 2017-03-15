package com.kyrincloud.koala_cache.compact;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import com.kyrincloud.koala_cache.Slice;

public class FileIterator implements Comparable<FileIterator>{
	
	private MappedByteBuffer map;
	
	private Slice nextElement;
	
	public FileIterator(FileChannel channel) {
		try {
			map = channel.map(MapMode.READ_ONLY, 0,channel.size());
			hasNext();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Slice getNextElement() {
		return nextElement;
	}
	
	public boolean hasNext(){
		if(map.remaining() > 4){
			int len = map.getInt();
			Slice key = new Slice(len);
			map.get(key.array());
			nextElement = key;
			return true;
		}
		nextElement = null;
		return false;
	}
	
	public Slice next(){
		Slice key = nextElement;
		return key;
	}

	public int compareTo(FileIterator iterator) {
		Slice nextKey = iterator.getNextElement();
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
