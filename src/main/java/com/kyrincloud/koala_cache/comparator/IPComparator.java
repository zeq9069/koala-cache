package com.kyrincloud.koala_cache.comparator;

import com.kyrincloud.koala_cache.Entity;
import com.kyrincloud.koala_cache.Slice;

/**
 * IP comparator 
 * 
 * IP类型的比较属于范围比较
 * 
 * @author kyrin
 *
 */
public class IPComparator extends AbstractComparator{

	@Override
	public int compare(Slice o1, Entity o2) {
		if(o1.compareTo(o2.getKey()) >= 0 && o1.compareTo(o2.getValue()) <= 0) {
			return 0;
		}
		
		return o1.compareTo(o2.getKey());
	}

}
