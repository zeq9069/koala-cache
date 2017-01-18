package com.kyrincloud.koala_cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class Entity implements Serializable , Cloneable{

	private static final long serialVersionUID = 3152311047987809480L;
	
	private static final Log LOG = LogFactory.getLog(Entity.class);
	
	private final Object key;
	
	private Object value;
	
	private long version;//缓存版本号，时间戳
	
	private long createTime;//创建时间
	
	private long lastAccessTime;//最后访问时间
	
	private long nextToLastAccessTime;
	
	private long hitCount;//命中数量
	
	private long timeToLive;//存活时间，单位：秒，0表示无限
	
	private long timeToIdle;//空闲时间，单位：秒，0表示无限
	
	private long lastUpdateTime;//最后更新时间
	
	private boolean eternal;//是否永久
	
	private boolean lifespanSet;//TTL和TTI是否设置寿命
	
	public Entity(Object key , Object value , long version) {
		this.key = key;
		this.value = value;
		this.version = version;
		this.createTime = System.currentTimeMillis();
		this.hitCount = 0;
	}
	
	public Entity(Serializable key , Serializable value , long version){
		this((Object)key, (Object)value, version);
	}
	
	public Entity(Serializable key , Serializable value){
		this((Object)key, (Object)value, 1L);
	}
	
	public Entity(Object key , Object value){
		this(key,value,1L);
	}
	
	public final Serializable getKey(){
		Serializable seriKey = null;
		try{
			seriKey = (Serializable)key;
		}catch(Exception e){
			throw new RuntimeException("Key "+key+" is not Serializable.");
		}
		return seriKey;
	}
	
	public final Object getObjectKey(){
		return key;
	}
	
	public final Serializable getValue(){
		Serializable seriValue = null;
		try{
			seriValue = (Serializable)value;
		}catch(Exception e){
			throw new RuntimeException("Value "+value+" is not Serializable.");
		}
		return seriValue;
	}
	
	public final Object getObjectValue(){
		return value;
	}
	
	public long getVersion() {
		return version;
	}
	
	public void setVersion(long version) {
		this.version = version;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime() {
		this.createTime = System.currentTimeMillis();
	}

	public long getLastAccessTime() {
		return lastAccessTime;
	}

	public void setLastAccessTime(long lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}

	public long getNextToLastAccessTime() {
		return nextToLastAccessTime;
	}

	public void setNextToLastAccessTime(long nextToLastAccessTime) {
		this.nextToLastAccessTime = nextToLastAccessTime;
	}

	public long getHitCount() {
		return hitCount;
	}

	public void setHitCount(long hitCount) {
		this.hitCount = hitCount;
	}

	public long getTimeToLive() {
		return timeToLive;
	}
	
	//second
	public void setTimeToLive(long timeToLive) {
		this.timeToLive = timeToLive;
		lifespanSet = true;
	}

	public long getTimeToIdle() {
		return timeToIdle;
	}

	public void setTimeToIdle(long timeToIdle) {
		this.timeToIdle = timeToIdle;
		lifespanSet = true;
	}

	public long getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public boolean isEternal() {
		return eternal;
	}

	public void setEternal(boolean eternal) {
		this.eternal = eternal;
	}

	public boolean isLifespanSet() {
		return lifespanSet;
	}

	public void setLifespanSet(boolean lifespanSet) {
		this.lifespanSet = lifespanSet;
	}

	public void setValue(Object value) {
		this.value = value;
	}
	
	public final void resetAccessStatistics(){
		this.lastAccessTime = 0;
		this.nextToLastAccessTime = 0;
		this.hitCount = 0;
	}
	
	public final void updateStatistics(){
		this.nextToLastAccessTime = lastAccessTime;
		lastAccessTime = System.currentTimeMillis();
		hitCount++;
	}
	
	@Override
	public final boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		
		Entity entity = (Entity) obj;
		
		if(key == null || entity.getObjectKey() == null){
			return false;
		}
		return key.equals(entity.getObjectKey());
	}
	
	@Override
	public int hashCode() {
		return key.hashCode();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(" { key = ").append(key)
			.append(" , value = ").append(value)
			.append(" , version = ").append(version)
			.append(" , hitCount = ").append(hitCount)
			.append(" , createTime = ").append(createTime)
			.append(" , lastAccessTime = ").append(lastAccessTime);
		return sb.toString();
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Entity entity = new Entity(deepCopy(key), deepCopy(value));
		entity.createTime = createTime;
		entity.lastAccessTime = lastAccessTime;
		entity.nextToLastAccessTime = nextToLastAccessTime;
		entity.hitCount = hitCount;
		return entity;
	}
	
	private Object deepCopy(Object oldValue){
		Serializable result = null;
		ByteArrayOutputStream byteArrOutput = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
			try {
				oos = new ObjectOutputStream(byteArrOutput);
				oos.writeObject(value);
				ByteArrayInputStream bais = new ByteArrayInputStream(byteArrOutput.toByteArray());
				ois = new ObjectInputStream(bais);
				return (Serializable) ois.readObject();
			} catch (IOException e) {
				LOG.error("deepCopy fail");
			} catch (ClassNotFoundException e) {
				LOG.error("deepCopy fail");
			}finally{
				try{
					if(oos != null){
						oos.close();
					}
					if(ois != null){
						ois.close();
					}
				}catch(Exception e){
					LOG.error("deepCopy IO close fail.");
				}
			}
			return result;
	}
	
	public final long getSerializableSize(){
		if(!isSerialize()){
			return 0;
		}
		long size = 0;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(this);
			size = baos.size();
			return size;
		} catch (IOException e) {
			LOG.error("getSerializableSize IO fail.");
		}finally{
			try{
				if(oos != null){
					oos.close();
				}
			}catch(Exception e){
				LOG.error("getSerializableSize IO close fail.");
			}
		}
		return size;
	}
	
	public final boolean isSerialize(){
		return key instanceof Serializable && value instanceof Serializable;
	}
	
	public final boolean isSerializeKey(){
		return key instanceof Serializable;
	}
	
	public final boolean isSerializeValue(){
		return value instanceof Serializable;
	}
	
	public final boolean isExpire(){
		//TODO 
		if(!lifespanSet){
			return false;
		}
		long now = System.currentTimeMillis();
		long expireTime = getExpireTime();
		
		return false;
	}
	
	public long getExpireTime(){
		return 0;
		
	} 
}
