package com.kyrincloud.koala_cache.ohc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import org.caffinitas.ohc.CacheSerializer;

public class KVCacheSerializer implements CacheSerializer{
	
	public int serializedSize(Object value) {
		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream obj = new ObjectOutputStream(baos);
			obj.writeObject(value);
			return baos.size();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public void serialize(Object value, ByteBuffer buf) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream obj = new ObjectOutputStream(baos);
			obj.writeObject(value);
			buf.put(baos.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Object deserialize(ByteBuffer buf) {
		try {
		ByteArrayInputStream bais = new ByteArrayInputStream(buf.array());
		ObjectInputStream ois = new ObjectInputStream(bais);
		return ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
