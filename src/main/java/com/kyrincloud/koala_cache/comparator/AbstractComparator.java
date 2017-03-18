package com.kyrincloud.koala_cache.comparator;

import com.kyrincloud.koala_cache.core.Entity;
import com.kyrincloud.koala_cache.core.Slice;

/**
 * 一个比较器是用来拿搜索的key跟每个Entity进行比较的工具，独立出来可以让用户自己定义比较的方式，也许是简单的
 * key-key类型精确比较，也许是k -（k,v）的范围比较，无论什么你都可以自定义
 * @author kyrin
 *
 * @param <K>
 * @param <T>
 */
public abstract class AbstractComparator{

	public abstract int compare(Slice o1, Entity o2);

}
