package com.kyrincloud.koala_cache;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

public class Table implements Cloneable{
	
	private TreeMap<String, Byte> memcache;
	
	private int size = 0;
	
	private ReentrantLock lock = new ReentrantLock();
	
	public Table() {
		memcache = new TreeMap<String, Byte>(new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
	}
	
	public void put(String key){
		try{
			lock.lock();
			size += key.length()+4;
			memcache.put(key, (byte)0);
		}finally{
			lock.unlock();
		}
	}
	
	public String get(String key){
		Byte value = memcache.get(key);
		return value == null ? null : key;
	}

	public int getSize() {
		return size;
	}
	
	public void setSize(int size){
		this.size = size;
	}
	
	public void setMemcache(TreeMap<String, Byte> memcache) {
		this.memcache = memcache;
	}

	public TreeMap<String, Byte> getMemcache() {
		return memcache;
	}
	
	public int getTableSize(){
		return memcache.size();
	}
	
	public Set<String> getKeySet() {
		return memcache.keySet();
	}
	
	public boolean isEmpty(){
		return memcache.isEmpty();
	}

}
