package com.kyrincloud.koala_cache;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
/**
 * 遍历查询是做过优化之后的
 * 先统计数量，直接在byte[]中做二分查找
 * @author kyrin
 *
 */
public class Block {
	
	private Slice block;
	
	public int count = 0;
	
	public Block(Slice block){
		this.block = block;
	}
	
	public String get(String key){//优化以后
		List<Integer> indexs = new ArrayList<Integer>();
		for(;block.remaining()>4;){
			indexs.add(block.position());
			int len = block.getInt();
			int idx = block.position()+len;
			block.position(idx);
		}
		
		//二分查询很快
		int lo=0;
        int hi=indexs.size()-1;
        int mid;
        while(lo<=hi){
            mid=(lo+hi)/2;
            String k = indexOf(indexs.get(mid));
            if(key.equals(k)){
            	return key;
            }else if(key.compareTo(k)>0){
                lo=mid+1;
            }else{
                hi=mid-1;
            }
        }
		return null;
	}
	
	public String indexOf(int offset){
		block.position(offset);
		int len = block.getInt();
		byte[] b = new byte[len];
		block.get(b);
		return new String(b);
	}
}
