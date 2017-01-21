package com.kyrincloud.koala_cache.consume;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class LruCache implements Store{
	
	private static final Logger log = Logger.getLogger(LruCache.class.getName());
	
	private final static int DEFAULT_CAP = 1024;
	
	private final int cap;
	
	
	private Map<Object , Element> map;
	
	public LruCache() {
		this(DEFAULT_CAP);
	}
	
	public LruCache(final int cap) {
		this.cap = cap;
		this.map = new LinkedHashMap<Object, Element>(cap){
			private static final long serialVersionUID = -7056599009333130008L;
			@Override
			protected boolean removeEldestEntry(Entry<Object, Element> eldest) {
				log.info("Element [ key = "+eldest.getKey()+" , value = "+eldest.getValue().getValue()+" version = "+eldest.getValue().getVersion()+" ]");
				return map.size() > cap ;
			}
			
		};
	}

	public void put(Element element) {
		map.put(element.getKey(), element);
	}

	public Element get(Object key) {
		return map.get(key);
	}

	public int getSzie() {
		return map.size();
	}

}
