package com.kyrincloud.koala_cache.consume;

public interface Store {
	
	
	public void put(Element element);
	
	public Element getByKey(Object key);
	
	public int getSzie();
	
}
