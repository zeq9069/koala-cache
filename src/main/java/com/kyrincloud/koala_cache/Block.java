package com.kyrincloud.koala_cache;

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
	
	public Slice get(Slice key){//优化以后
		List<Integer> indexs = new ArrayList<Integer>();
		for(;block.remaining()>8;){
			indexs.add(block.position());
			int len = block.getInt();
			block.position(block.position()+len);
			int vlen = block.getInt();
			block.position(block.position() + vlen);
		}
		
		//二分查询很快
		int lo=0;
        int hi=indexs.size()-1;
        int mid;
        while(lo<=hi){
            mid=(lo+hi)/2;
            Entity entity = indexOf(indexs.get(mid));
            if(key.compareTo(entity.getKey()) == 0){
            	return entity.getValue();
            }else if(key.compareTo(entity.getKey())>0){
                lo=mid+1;
            }else{
                hi=mid-1;
            }
        }
		return null;
	}
	
	public Entity indexOf(int offset){
		block.position(offset);
		int klen = block.getInt();
		Slice k = new Slice(klen);
		block.get(k.array());
		
		int vlen = block.getInt();
		Slice v = new Slice(vlen);
		block.get(v.array());
		
		return new Entity(k, v);
	}
}
