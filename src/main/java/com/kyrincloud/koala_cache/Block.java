package com.kyrincloud.koala_cache;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Block {
	
	private ByteBuffer block;
	
	private List<String> array = new ArrayList<String>();
	
	public int count = 0;
	
	public Block(ByteBuffer block){
		this.block = block;
	}
	
	public String get(String key){
		//TODO 待优化
		while(block.remaining()>4){
			int keySzie = block.getInt();
			byte[] val = new byte[keySzie];
			block.get(val);
			String k = new String(val);
			array.add(k);
		}
		
		int lo=0;
        int hi=array.size()-1;
        int mid;
        while(lo<=hi){
            mid=(lo+hi)/2;
            if(key.equals(array.get(mid))){
            	return key;
            }else if(key.compareTo(array.get(mid))>0){
                lo=mid+1;
            }else{
                hi=mid-1;
            }
        }
		return null;
	}
}
