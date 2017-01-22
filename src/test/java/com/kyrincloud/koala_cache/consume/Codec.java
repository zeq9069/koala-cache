package com.kyrincloud.koala_cache.consume;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Codec {
	
	
	public byte[] encode(Object target) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(target);
		return baos.toByteArray();
	}
	
	public Object decode(byte[] bytes) throws IOException, ClassNotFoundException{
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bais);
		return ois.readObject();
	}

}
