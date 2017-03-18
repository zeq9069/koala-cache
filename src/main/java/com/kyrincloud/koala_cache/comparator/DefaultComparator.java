package com.kyrincloud.koala_cache.comparator;

import com.kyrincloud.koala_cache.core.Entity;
import com.kyrincloud.koala_cache.core.Slice;

/**
 * default comparator  
 * 
 * default k-k类型的精确查找比较
 * 
 * @author kyrin
 *
 */
public class DefaultComparator extends AbstractComparator{

	@Override
	public int compare(Slice o1, Entity o2) {
		return o1.compareTo(o2.getKey());
	}

}
