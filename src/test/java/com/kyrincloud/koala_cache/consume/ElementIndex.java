package com.kyrincloud.koala_cache.consume;
/**
 * element index in file
 * element在文件中的偏移量位置
 * @author kyrin
 *
 */
public class ElementIndex {
	
	private long offset;//position 偏移量
	
	private int playloadSize;//data中element数据的真是大小
	
	private int blockSize;//第一次创建元素时element的大小，如果被重复使用，blockSize可能大于playloadSize
	
	private long expireTime;//过期时间
	
}
