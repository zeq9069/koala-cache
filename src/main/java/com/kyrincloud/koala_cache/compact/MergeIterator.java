package com.kyrincloud.koala_cache.compact;

import java.util.List;
import java.util.PriorityQueue;

public class MergeIterator {
	
	private PriorityQueue<FileIterator> queue;
	
	private String nextElement;
	
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
	
	public String next(){
		String key =  nextElement;
		nextElement = null;
		return key;
	}

	public String getNextElement() {
		return nextElement;
	}
	
}
