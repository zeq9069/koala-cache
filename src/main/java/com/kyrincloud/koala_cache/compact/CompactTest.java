package com.kyrincloud.koala_cache.compact;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import com.google.common.collect.Lists;
import com.kyrincloud.koala_cache.Slice;

public class CompactTest {
	
	public static void main(String[] args) throws Exception {
		CompactTest test = new CompactTest();
		//test.write();
		test.compact();
		
	}
	
	public void compact() throws Exception {
		FileChannel ch1 = new FileInputStream(new File("/tmp/data1")).getChannel();
		FileChannel ch2 = new FileInputStream(new File("/tmp/data2")).getChannel();

		FileIterator it1 = new FileIterator(ch1.map(MapMode.READ_ONLY, 0,ch1.size()).duplicate());
		FileIterator it2 = new FileIterator(ch2.map(MapMode.READ_ONLY, 0,ch2.size()).duplicate());
		
		MergeIterator merge = new MergeIterator(Lists.newArrayList(it1,it2));
		
		while(merge.hasNext()){
			System.out.println(">>>"+merge.getNextElement());
		}
	}
	
	public void write() throws Exception{
		FileOutputStream fos1 = new FileOutputStream("/tmp/data1");
		FileOutputStream fos2 = new FileOutputStream("/tmp/data2");

		
		for(int i = 1 ;i <= 10 ;i++){
			String key = i+"";
			for(int j = key.length() ; j < 10 ; j++){
				key ="0"+key;
			}
			Slice slice = new Slice(4+key.length());
			slice.putInt(key.length());
			slice.put(key.getBytes());
			if(i % 2 == 0){
				fos2.write(slice.array());
			}else{
				fos1.write(slice.array());
			}
		}
		
		fos1.flush();
		fos2.flush();
		fos1.close();
		fos2.close();
	}

}
