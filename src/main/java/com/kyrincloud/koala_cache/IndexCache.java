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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Stopwatch;

/**
 *索引和数据查询
 * @author kyrin
 *
 */
public class IndexCache {
	
	private static final Log LOG = LogFactory.getLog("");

	private Map<String,Position> index = new TreeMap<String, Position>(new Comparator<String>() {
		public int compare(String o1, String o2) {
			return o1.compareTo(o2);
		}
	});
	
	private String [] array = null;

	
	FileInputStream indexChannel;
	
	FileChannel dataChannel;
	
	public String indexPath;
	
	public String dataPath;
	
	public IndexCache(String indexPath , String dataPath){
		this.indexPath = indexPath;
		this.dataPath = dataPath;
		try {
			indexChannel = new FileInputStream(new File(indexPath));
			dataChannel = new RandomAccessFile(new File(dataPath),"rw").getChannel();
			
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
	

	public String searchCache(String key , long offset , long allSize) throws Exception{
		long time = 0;

		dataChannel.position(offset);
		
		Stopwatch stop = Stopwatch.createStarted();
		//待优化
		ByteBuffer block = ByteBuffer.allocate((int)(allSize-offset+1));
		time = stop.elapsed(TimeUnit.MILLISECONDS);
		if(time>0)
		LOG.info("block加载耗时："+time);
		
		////待优化
		dataChannel.read(block);
		
		block.position(0);
		
		
		Block b = new Block(block);

		String result = b.get(key);
		
		
		return result;
	}
}
