package com.kyrincloud.koala_cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

public class AppTest{
	
	public static void main(String[] args) {
		CacheManager manager = CacheManager.getInstance();
		Cache cache = new Cache("cache-1", 1024 * 10, MemoryStoreEvictionPolicy.FIFO, 
				true, "/Users/zhangerqiang/Desktop/cache", true, 60 * 60 * 1000, 60, true, 60, null);
		manager.addCache(cache);
		Cache c = manager.getCache("cache-1");
		Element element = new Element("name", "kyrin");
		c.put(element);
		c.flush();

	}
	
}
