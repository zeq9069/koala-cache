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
		boolean hasNext = iterator.hasNext();
		nextElement = iterator.next();
		queue.add(iterator);
		return hasNext;
	}
	
	public String next(){
		return nextElement;
	}

	public String getNextElement() {
		return nextElement;
	}
	
}
