package com.kyrincloud.koala_cache.consume;

import java.io.Serializable;

public class Element implements Serializable{
	
	private static final long serialVersionUID = -6489492254007679165L;

	private Object key;
	
	private Object value;
	
	private long version;//timestamp

	public Element(Object key , Object value) {
		this(key, value, System.currentTimeMillis());
	}
	
	private Element(Object key , Object value , long version) {
		this.key = key;
		this.value = value;
		this.version = version;
	}
	
	public Object getKey() {
		return key;
	}

	public Object getValue() {
		return value;
	}

	public void updateValue(Object value) {
		this.value = value;
		this.version = System.currentTimeMillis();
	}

	public long getVersion() {
		return version;
	}

}
