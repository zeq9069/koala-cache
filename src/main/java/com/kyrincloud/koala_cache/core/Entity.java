package com.kyrincloud.koala_cache.core;

public class Entity {
	
	private Slice key;
	
	private Slice value;
	
	public Entity(Slice key , Slice value) {
		this.key = key;
		this.value = value;
	}

	public Slice getKey() {
		return key;
	}

	public void setKey(Slice key) {
		this.key = key;
	}

	public Slice getValue() {
		return value;
	}

	public void setValue(Slice value) {
		this.value = value;
	}
	
	public Slice encode(){
		Slice slice = new Slice(key.size()+value.size()+8);
		slice.putInt(key.size());
		slice.put(key.array());
		slice.putInt(value.size());
		slice.put(value.array());
		return slice;
	}

}
