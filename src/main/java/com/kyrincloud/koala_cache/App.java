package com.kyrincloud.koala_cache;

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 单机存储引擎验证
 *
 */
public class App  
{
	private static final Log LOG = LogFactory.getLog("");

    public static void main( String[] args ) throws Exception
    {
    	//load();

    	IndexCache index = new IndexCache("/tmp/index", "/tmp/data");
    	String key = "0006533729";
    	for(int i = 0 ; i < 10000 ;i++){
    		
	    	Position pos = index.get(key);
	    	
			if(pos == null){
				throw new Exception("pos is null");
			}

	    	key =  index.searchCache(key,pos.getStart(),pos.getEnd());
    	}
    	
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
