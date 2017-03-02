package com.kyrincloud.koala_cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

/**
 *索引和数据查询
 * @author kyrin
 *
 */
public class IndexCache {
	
	private Map<String,Position> index = new TreeMap<String, Position>(new Comparator<String>() {
		public int compare(String o1, String o2) {
			return o1.compareTo(o2);
		}
	});
	
	private String [] array = null;

	
	FileInputStream indexChannel;
	
	RandomAccessFile dataChannel;
	
	public String indexPath;
	
	public String dataPath;
	
	public IndexCache(String indexPath , String dataPath){
		this.indexPath = indexPath;
		this.dataPath = dataPath;
		try {
			indexChannel = new FileInputStream(new File(indexPath));
			dataChannel = new RandomAccessFile(new File(dataPath),"rw");
			
			load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void load() throws IOException{
		
		while(indexChannel.available()>0){
			ByteBuffer k = ByteBuffer.allocate(4);
			indexChannel.read(k.array());
			byte[] kk = new byte[k.getInt()];
			indexChannel.read(kk);
			String key = new String(kk);
			
			ByteBuffer start = ByteBuffer.allocate(8);
			indexChannel.read(start.array());
			start.position(0);
			
			ByteBuffer end = ByteBuffer.allocate(8);
			indexChannel.read(end.array());
			end.position(0);
			
			index.put(key,Position.build(start.getLong(), end.getLong(), key));
		}
		
		array = new String[index.size()];
		index.keySet().toArray(array);
	}
	
	public Position get(String key){
		int lo=0;
        int hi=array.length-1;
        int mid;
        String tmpKey = null;
        while(lo<=hi){
            mid=(lo+hi)/2;
            if(key.equals(array[mid])){
            	return this.index.get(key);
            }else if(key.compareTo(array[mid])>0){
                lo=mid+1;
            }else{
            	tmpKey = array[mid];
                hi=mid-1;
            }
        }
        return this.index.get(tmpKey);
	}
	
//	public String search(String key , long offset , long allSize) throws Exception{
//		
//		FileChannel channel = dataChannel.getChannel();
//		channel.position(offset);
//		ByteBuffer size = ByteBuffer.allocate(4);
//		channel.read(size);
//		size.position(0);
//		ByteBuffer value = ByteBuffer.allocate(size.getInt());
//		channel.read(value);
//		
//		String res = new String(value.array());
//		
//		if(res.equals(key)){
//			return res;
//		}
//		long p = channel.position();
//		if(p+(key.length()+4) <= allSize+1){
//			return search(key, p, allSize);
//		}
//		return null;
//	}
//	
public String searchCache(String key , long offset , long allSize) throws Exception{
		
		FileChannel channel = dataChannel.getChannel();
		channel.position(offset);
		ByteBuffer block = ByteBuffer.allocate((int)(allSize-offset+1));
		channel.read(block);
		block.position(0);
		
		Block b = new Block(block);
		Stopwatch stop = Stopwatch.createStarted();
		String result = b.get(key);
		System.out.println("block查找耗时："+stop.elapsed(TimeUnit.MICROSECONDS));
		return result;
	}
}
