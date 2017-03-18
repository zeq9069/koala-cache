package com.kyrincloud.koala_cache;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.kyrincloud.koala_cache.comparator.IPComparator;

/**
 * 单机存储引擎验证
 * 
 */
public class App  
{
	
	public static void main(String[] args) {
		
		MemCache cache = new MemCache("/tmp",new IPComparator(),true);
//		
//		Stopwatch w = Stopwatch.createStarted();
//		for(int i = 0 ; i< 10000000;i+=4){
//    		String key = i+"";
//    		for(int j = key.length();j<10;j++){
//    			key="0"+key;
//    		} 
//    		String value = (i + 3)+"";
//    		for(int j = value.length();j<10;j++){
//    			value="0"+value;
//    		}
//    		cache.put(key.getBytes(),value.getBytes());
//    	}
		
		Stopwatch s = Stopwatch.createStarted();
		for(int i = 0 ; i < 10000;i+=4){
			Stopwatch y = Stopwatch.createStarted();
			System.out.println(new String(cache.get("0000888888".getBytes()).array()));
			long yy = y.elapsed(TimeUnit.MILLISECONDS);
			if(yy >= 1)
			System.out.println(yy);
		}
	}
    
}
