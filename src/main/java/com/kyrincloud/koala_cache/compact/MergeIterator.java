package com.kyrincloud.koala_cache.compact;

import java.util.List;
import java.util.PriorityQueue;

import com.kyrincloud.koala_cache.Slice;

public class MergeIterator {
	
	private PriorityQueue<FileIterator> queue;
	
	private Slice nextElement;
	
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
	
	public Slice next(){
		Slice key =  nextElement;
		nextElement = null;
		return key;
	}

	public Slice getNextElement() {
		return nextElement;
	}
	
}
