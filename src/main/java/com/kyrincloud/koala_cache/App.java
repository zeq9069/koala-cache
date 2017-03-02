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
    	for(int i = 0 ; i < 10000 ;i++){
	    	Position pos = index.get("0000888888");
	    	
			if(pos == null){
				throw new Exception("pos is null");
			}
	    	Stopwatch stop = Stopwatch.createStarted();
	    	key =  index.searchCache("0000888888",pos.getStart(),pos.getEnd());
	    	long timeout = stop.elapsed(TimeUnit.MILLISECONDS);
			if(timeout > 0)
		    	System.out.println("数据读总耗时："+timeout);
	    	//time+=timeout;
    	}
    	System.out.println(key+" 总耗时："+time);
    }
    
    public static void load() throws Exception{
    	Cache cache = new Cache("/tmp/index", "/tmp/data");
    	for(int i = 0 ; i< 10000000;i++){
    		String key = i+"";
    		for(int j = key.length();j<10;j++){
    			key="0"+key;
    		}
    		cache.put(key);
    	}
    	cache.flush();
    }
}
