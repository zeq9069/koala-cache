package com.kyrincloud.koala_cache;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

/**
 * 单机存储引擎验证
 * 
 * ByteBuffer 分配太耗时，我们用自定义的Slice代替，这就是为什么好多框架都自定义Slice的原因
 * 
 * 前提条件：index，data都是顺序存储
 *
 */
public class App  
{
	
	public static void main(String[] args) {
		
		MemCache cache = new MemCache("/tmp");
		
		Stopwatch w = Stopwatch.createStarted();
		for(int i = 0 ; i< 10000000;i++){
    		String key = i+"";
    		for(int j = key.length();j<10;j++){
    			key="0"+key;
    		}
    		cache.put(key);
    	}
		System.out.println("写耗时："+w.elapsed(TimeUnit.MILLISECONDS));
		
		Stopwatch s = Stopwatch.createStarted();
		for(int i = 0 ; i < 10000000;i++){
			Stopwatch y = Stopwatch.createStarted();
			if(cache.get("0008888888") == null){
				System.out.println("没查询到~~~");
			}
			long yy = y.elapsed(TimeUnit.MILLISECONDS);
			if(yy >= 1)
			System.out.println(yy);
		}
		System.out.println("读耗时："+s.elapsed(TimeUnit.MILLISECONDS));
	}
    
}
