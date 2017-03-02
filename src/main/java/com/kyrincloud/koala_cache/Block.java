package com.kyrincloud.koala_cache;

import java.nio.ByteBuffer;

public class Block {
	
	private ByteBuffer block;
	
	public int count = 0;
	
	public Block(ByteBuffer block){
		this.block = block;
	}
	
	public String get(String key){
		count++;
		if(block.remaining()<4){
			return null;
		}
		int keySzie = block.getInt();
		byte[] val = new byte[keySzie];
		block.get(val);
		String k = new String(val);
		if(k.equals(key)){
			return key;
		}
		return get(key);
	}
}
