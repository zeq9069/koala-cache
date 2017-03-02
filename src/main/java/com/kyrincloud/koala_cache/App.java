package com.kyrincloud.koala_cache;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

/**
 * 单机存储引擎验证
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {
    	
    	//load();
    	
    	IndexCache index = new IndexCache("/tmp/index", "/tmp/data");
    	String key = "";
    	long time = 0;
    	for(int i = 0 ; i < 100000 ;i++){
	    	Stopwatch stop = Stopwatch.createStarted();
	    	Position pos = index.get("00000000000000000000000000008888");
			if(pos == null){
				throw new Exception("pos is null");
			}
	    	key =  index.searchCache("00000000000000000000000000008888",pos.getStart(),pos.getEnd());
	    	//System.out.println(stop.elapsed(TimeUnit.MILLISECONDS));
    	}
    	System.out.println(key+" 总耗时："+time);
    }
    
    public static void load() throws Exception{
    	Cache cache = new Cache("/tmp/index", "/tmp/data");
    	for(int i = 0 ; i< 10000000;i++){
    		String key = i+"";
    		for(int j = key.length();j<32;j++){
    			key="0"+key;
    		}
    		cache.put(key);
    	}
    	cache.flush();
    }
}
