package com.kyrincloud.koala_cache.consume;

public class Main {
	
	public static void main(String[] args) {
		LruCache cache  = new LruCache();
		
		for(int i = 0 ; i< 2000;i++){
			Element element = new Element("cache-name-"+i, "value-"+i);
			cache.put(element);
		}
		System.out.println(cache.getSzie());
	}
}
