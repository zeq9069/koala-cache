package com.kyrincloud.koala_cache.consume;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class LruCache implements Store{
	
	private static final Logger log = Logger.getLogger(LruCache.class.getName());
	
	private final static int DEFAULT_CAP = 1024;
	
	private int cap;
	
	private float loadFactor;
	
	private static final float DEFAULT_LOAD_FACTOR = 0.75f;

	private static final long DEFAULT_EXPIRE_TIME = Long.MAX_VALUE;
	
	private long timeToLive ;
	
	private Map map;
	
	private Thread evictExpireElement;
	
	private long interval = 1000;
	
	public LruCache() {
		this(DEFAULT_CAP , DEFAULT_LOAD_FACTOR , DEFAULT_EXPIRE_TIME);
	}
	
	public LruCache(final int cap , float loadFactor , long timeToLive) {
		this.cap = cap;
		this.loadFactor = loadFactor;
		this.timeToLive = timeToLive;
		LinkedHashMap<Object, Element> container = new LinkedHashMap<Object, Element>(cap,loadFactor,true){
			private static final long serialVersionUID = -7056599009333130008L;
			@Override
			protected boolean removeEldestEntry(Entry<Object, Element> eldest) {
				return map.size() > cap ;
			}
		};
		this.map = Collections.synchronizedMap(container);
		startTask();
	}
	
	public void startTask(){
		evictExpireElement = new Thread(new Runnable() {
			public void run() {
				while(true){
					try {
						TimeUnit.MILLISECONDS.sleep(interval);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					synchronized (map) {
						for(Iterator<Element> it = map.values().iterator();it.hasNext();){
							Element element = it.next();
							if(element.isExpire()){
								log.info("evict element key = "+element.getKey());
								it.remove();
							}
						}
					}
				}
			}
		});
		evictExpireElement.setDaemon(true);
		evictExpireElement.start();
	}

	public void put(Element element) {
		element.setTimeToLive(timeToLive);
		map.put(element.getKey(), element);
	}

	public Element getByKey(Object key) {
		Element element = (Element) map.get(key);
		element.updateAccessStatistics();
		return element;
	}

	public int getSzie() {
		return map.size();
	}

	public int getCap() {
		return cap;
	}

	public float getLoadFactor() {
		return loadFactor;
	}

	public long getTimeToLive() {
		return timeToLive;
	}

}
