package com.kyrincloud.koala_cache;

/**
 * 单机存储引擎验证
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {
//    	Cache cache = new Cache("/tmp/index", "/tmp/data");
//    	for(int i = 0 ; i< 2;i++){
//    		String key = i+"";
//    		for(int j = key.length();j<32;j++){
//    			key+="0";
//    		}
//    		cache.put(key);
//    	}
//    	cache.flush();
    	
    	IndexCache index = new IndexCache("/tmp/index", "/tmp/data");
    	for(;;){
    	System.out.println(System.currentTimeMillis());
    	System.out.println(index.search("00000000000000000000000000000000"));
    	System.out.println(System.currentTimeMillis());
    	}

    	
    }
}
