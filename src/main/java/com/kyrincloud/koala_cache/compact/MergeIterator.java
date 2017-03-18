package com.kyrincloud.koala_cache.compact;

import java.util.List;
import java.util.PriorityQueue;

import com.kyrincloud.koala_cache.core.Entity;

public class MergeIterator {
	
	private PriorityQueue<FileIterator> queue;
	
	private Entity nextElement;
	
	public MergeIterator(List<FileIterator> iterators) {
		queue = new PriorityQueue<FileIterator>(iterators);
	}
	
	public boolean hasNext(){
		FileIterator iterator = queue.poll();
		nextElement = iterator.next();
		boolean hasNext = false;
		if(nextElement != null){
			hasNext = true;
			iterator.hasNext();
		}
		queue.add(iterator);
		return hasNext;
	}
	
	public Entity next(){
		Entity entity =  nextElement;
		nextElement = null;
		return entity;
	}

	public Entity getNextElement() {
		return nextElement;
	}
	
}
