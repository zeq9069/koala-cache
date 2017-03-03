package com.kyrincloud.koala_cache;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Stopwatch;

/**
 * 单机存储引擎验证
 * 
 * 优化完以后的效果，每100万次读写，耗时33秒，每次都是微妙级别
 *
 */
public class App  
{
	private static final Log LOG = LogFactory.getLog(App.class);
	
	static ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2);

    public static void main( String[] args ) throws Exception
    {
    	//load();
    App app = new App();
    
    //app.multiRead();
    
    app.singleRead();
    	
    	
    	
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
    
    
    public void multiRead(){
	final IndexCache index = new IndexCache("/tmp/index", "/tmp/data");
	    	
	    	for(int i = 0 ;i<8;i++){
	    		exec.submit(new Runnable() {
					
					public void run() {
						try {
							long time = 0;
	
					    	String key = "0008888888";
					    	for(int i = 0 ; i < 10000 ;i++){
	
					    		Stopwatch stop = Stopwatch.createStarted();
	
						    	Position pos = index.get(key);
						    	
								if(pos == null){
									throw new Exception("pos is null");
								}
	
						    	key =  index.searchCache(key,pos.getStart(),pos.getEnd());
						    	time+= stop.elapsed(TimeUnit.MILLISECONDS);
					    	}
					    	if(time>0)
								LOG.info("block加载耗时："+time);
					    	System.out.println(key);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
	    	}
    }
    
    public void singleRead() throws Exception{
    	IndexCache index = new IndexCache("/tmp/index", "/tmp/data");
    	long time = 0;
    	
    	String key = "0008888888";
    	for(int i = 0 ; i < 10000 ;i++){

    		Stopwatch stop = Stopwatch.createStarted();

	    	Position pos = index.get(key);
	    	
			if(pos == null){
				throw new Exception("pos is null");
			}

	    	key =  index.searchCache(key,pos.getStart(),pos.getEnd());
	    	time+= stop.elapsed(TimeUnit.MILLISECONDS);
    	}
    	if(time>0)
			LOG.info("block加载耗时："+time);
    	System.out.println(key);
    }
    
    
}
