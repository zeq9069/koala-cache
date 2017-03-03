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
 * 优化完以后的效果，每100万次读，耗时28秒左右(每次微秒级累积)，只有十几次超过1毫秒
 * 
 * ByteBuffer 分配太耗时，我们用自定义的Slice代替，这就是为什么好多框架都自定义Slice的原因
 * 
 * 前提条件：index，data都是顺序存储
 *
 */
public class App  
{
	private static final Log LOG = LogFactory.getLog(App.class);
	
	static ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2);

    public static void main( String[] args ) throws Exception
    {
    App app = new App();
    //app.load();
    //app.multiRead("0008888888");//多线程情况下性能下降太大，如何优化？
    
    app.singleRead("0008888888");
    
    	
    	
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
    
    
    public void multiRead(final String key){
	final IndexCache index = new IndexCache("/tmp/index", "/tmp/data");
	    	
	    	for(int i = 0 ;i<8;i++){
	    		exec.submit(new Runnable() {
					
					public void run() {
						try {
					    	String res = null;

							long time = 0;
	
					    	for(int i = 0 ; i < 10000 ;i++){
	
					    		Stopwatch stop = Stopwatch.createStarted();
	
						    	Position pos = index.get(key);
						    	
								if(pos == null){
									throw new Exception("pos is null");
								}
	
						    	res =  index.searchCache(key,pos.getStart(),pos.getEnd());
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
	    	exec.shutdown();
    }
    
    public void singleRead(String key) throws Exception{
    	IndexCache index = new IndexCache("/tmp/index", "/tmp/data");
    	long time = 0;
    	
    	for(int i = 0 ; i < 1000000 ;i++){

    		Stopwatch stop = Stopwatch.createStarted();

	    	Position pos = index.get(key);
	    	
			if(pos == null){
				throw new Exception("pos is null");
			}

	    	key =  index.searchCache(key,pos.getStart(),pos.getEnd());
	    	time+= stop.elapsed(TimeUnit.MICROSECONDS);
    	}
    	if(time>1000)
			LOG.info("block加载耗时："+time);
    	System.out.println(key);
    }
    
    
}
