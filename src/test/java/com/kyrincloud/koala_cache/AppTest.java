package com.kyrincloud.koala_cache;

import java.rmi.server.UID;

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
		System.out.println(System.currentTimeMillis());
		for(int i = 0 ; i < 20000000 ; i++){
			Element element = new Element("name"+i, "kyrin");
			c.put(element);
		}
//		for(int i = 0 ; i < 1000 ; i++){
//			Element element = new Element("name"+i, "kyrin");
//			c.remove("name"+i);
//		}
		c.flush();
		System.out.println(System.currentTimeMillis());
		System.out.println(c.get("name19999"));
		System.out.println(System.currentTimeMillis());


	}
	
}
