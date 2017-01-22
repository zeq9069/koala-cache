package com.kyrincloud.koala_cache.consume;


public class Main {
	
	public static void main(String[] args) throws InterruptedException {
		LruMemeryStore cache  = new LruMemeryStore(2000,1.0f,1);
		
		System.out.println(System.currentTimeMillis());
		for(int i = 0 ; i< 10;i++){
			Element element = new Element("cache-name-"+i, "value-"+i);
			cache.put(element);
		}
		System.out.println(System.currentTimeMillis());

		
		System.out.println(cache.getSzie());
		
	}
}
