package com.kyrincloud.koala_cache;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 内存数据缓存
 * 
 * @author kyrin
 *
 */
public class Table {

	private TreeMap<Slice, Slice> memcache;

	private int size = 0;

	private ReentrantLock lock = new ReentrantLock();

	public Table() {
		memcache = new TreeMap<Slice, Slice>(new Comparator<Slice>() {
			public int compare(Slice o1, Slice o2) {
				return o1.compareTo(o2);
			}
		});
	}

	public void put(byte[] key , byte[] value) {
		try {
			lock.lock();
			Slice k = new Slice(key);
			Slice v = new Slice(value);
			size += k.size() + v.size() + 4 + 4;
			memcache.put(k, v);
		} finally {
			lock.unlock();
		}
	}

	public Slice get(byte[] key) {
		Slice k = new Slice(key);
		Slice value = memcache.get(k);
		return value == null ? null : k;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setMemcache(TreeMap<Slice,Slice> memcache) {
		this.memcache = memcache;
	}

	public TreeMap<Slice, Slice> getMemcache() {
		return memcache;
	}

	public int getTableSize() {
		return memcache.size();
	}

	public Set<Slice> getKeySet() {
		return memcache.keySet();
	}

	public boolean isEmpty() {
		return memcache.isEmpty();
	}

}
