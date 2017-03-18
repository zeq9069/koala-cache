package com.kyrincloud.koala_cache.utils;

/**
 * IP utils 
 * IP to bytes and bytes to IP
 * @author kyrin
 *
 */
public class IPUtils {

	public static Long transformIP(String ipaddr) {
		String ip[] = ipaddr.split("\\.");
		Long ipLong = 256 * 256 * 256 * Long.parseLong(ip[0]) + 256 * 256 * Long.parseLong(ip[1])
				+ 256 * Long.parseLong(ip[2]) + Long.parseLong(ip[3]);
		return ipLong;
	}
	
	public static String getIP(Long ipaddr) { 
	    long y = ipaddr % 256; 
	    long m = (ipaddr - y) / (256 * 256 * 256); 
	    long n = (ipaddr - 256 * 256 *256 * m - y) / (256 * 256); 
	    long x = (ipaddr - 256 * 256 *256 * m - 256 * 256 *n - y) / 256; 
	    return m + "." + n + "." + x + "." + y; 
	} 
	
	public static void main(String[] args) {
		System.out.println(IPUtils.transformIP("254.44.141.249"));
	}
}
