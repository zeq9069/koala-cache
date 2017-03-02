package com.kyrincloud.koala_cache.ohc;

import org.caffinitas.ohc.OHCache;
import org.caffinitas.ohc.OHCacheBuilder;

public class OhcOffHeapTest {
	
	
	public static void main(String[] args) {
		
		OHCache<Object, Object> ohCache = OHCacheBuilder.newBuilder()
				.keySerializer(new KVCacheSerializer())
				.valueSerializer(new KVCacheSerializer())
				.build();
		
		for(int i = 0 ; i < Integer.MAX_VALUE;i++ ){
			ohCache.put("key00000000000"+i, "value000000000000"+i);
		}
		
	}

}
