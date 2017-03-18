package com.kyrincloud.koala_cache.compact;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import com.kyrincloud.koala_cache.core.Entity;
import com.kyrincloud.koala_cache.core.Slice;

public class FileIterator implements Comparable<FileIterator>{
	
	private MappedByteBuffer map;
	
	private Entity nextElement;
	
	public FileIterator(FileChannel channel) {
		try {
			map = channel.map(MapMode.READ_ONLY, 0,channel.size());
			hasNext();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Entity getNextElement() {
		return nextElement;
	}
	
	public boolean hasNext(){
		if(map.remaining() > 4){
			int len = map.getInt();
			Slice key = new Slice(len);
			map.get(key.array());
			
			int vlen = map.getInt();
			Slice value = new Slice(vlen);
			map.get(value.array());
			
			nextElement = new Entity(key, value);
			return true;
		}
		nextElement = null;
		return false;
	}
	
	public Entity next(){
		Entity entity = nextElement;
		return entity;
	}

	//当其中一个为null的时候（说明一个FileIterator已经遍历完了），是不最大的，永远不会再去遍历这个Fileiterator了
	public int compareTo(FileIterator iterator) {
		Entity nextEntity = iterator.getNextElement();
		if((nextEntity == null || nextEntity.getKey() == null) && (nextElement != null && nextElement.getKey() != null) ){
			return -1;
		}
		if((nextElement == null || nextElement.getKey() == null ) && (nextEntity != null && nextEntity.getKey() != null )){
			return 1;
		}
		if(( nextEntity == null || nextEntity.getKey() == null) && (nextElement == null || nextElement.getKey() == null)){
			return 0;
		}
		
		return nextElement.getKey().compareTo(nextEntity.getKey());
	}

}
