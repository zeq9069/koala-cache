package com.kyrincloud.koala_cache.consume;

import java.io.Serializable;

public class Element implements Serializable{
	
	private static final long serialVersionUID = -6489492254007679165L;

	private Object key;
	
	private Object value;
	
	private long version;//timestamp
	
	private long timeToLive; //存活时间，从创建时间算起，单位秒
	
	private long createTime;//创建时间
	
	private long lastUpdateTime; //最后更新的时间
	
	private long lastAccessTime;//最后访问的时间
	
	private long hitCount;//命中/访问数量

	public Element(Object key , Object value) {
		this(key, value, System.currentTimeMillis());
	}
	
	public Element(Object key , Object value , long version) {
		this.key = key;
		this.value = value;
		this.version = version;
		this.createTime = System.currentTimeMillis();
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
		this.lastUpdateTime = version;
	}

	public long getVersion() {
		return version;
	}
	
	public boolean isSerializableKey(){
		return key instanceof Serializable;
	}
	
	public boolean isSerializableValue(){
		return value instanceof Serializable;
	}
	
	public void updateAccessStatistics(){
		this.lastAccessTime = System.currentTimeMillis();
		hitCount++;
	}
	
	public boolean isExpire(){
		return System.currentTimeMillis() > getExpireTime();
	}

	private long getExpireTime() {
		return createTime + timeToLive * 1000;
	}

	public void setTimeToLive(long timeToLive) {
		this.timeToLive = timeToLive;
	}

	public long getLastAccessTime() {
		return lastAccessTime;
	}

	public void setLastAccessTime(long lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}

	public long getHitCount() {
		return hitCount;
	}

	public void setHitCount(long hitCount) {
		this.hitCount = hitCount;
	}

	public long getCreateTime() {
		return createTime;
	}

	public long getLastUpdateTime() {
		return lastUpdateTime;
	}

	@Override
	public String toString() {
		return "Element [key=" + key + ", value=" + value + ", version=" + version + ", timeToLive=" + timeToLive
				+ ", createTime=" + createTime + ", lastUpdateTime=" + lastUpdateTime + ", lastAccessTime="
				+ lastAccessTime + ", hitCount=" + hitCount + "]";
	}
	
}
