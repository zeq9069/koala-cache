package com.kyrincloud.koala_cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileData {
	
private static final Log LOG = LogFactory.getLog(IndexCache.class);
	
	private Map<String,Position> index = new TreeMap<String, Position>(new Comparator<String>() {
		public int compare(String o1, String o2) {
			return o1.compareTo(o2);
		}
	});
	
	private String [] array = null;

	
	FileInputStream indexChannel;
	
	FileChannel dataChannel;
	
	ReentrantLock lock = new ReentrantLock();
	
	MappedByteBuffer map ;
	
//	//预分配一个block（32k）字节，稍大一些，因为我们不一定是严格意义上的32K
//	//可以提高分配带来的时间消耗
//	private ByteBuffer block = ByteBuffer.allocate(32*1024+4+1024);
	
	public String indexPath;
	
	public String dataPath;
	
	private String number;
	
	@SuppressWarnings("resource")
	public FileData(String indexPath , String dataPath){
		this.indexPath = indexPath;
		this.dataPath = dataPath;
		try {
			File index = new File(indexPath);
			number = index.getName().substring(0, index.getName().indexOf("."));
			indexChannel = new FileInputStream(index);
			dataChannel = new RandomAccessFile(new File(dataPath),"rw").getChannel();
			map = dataChannel.map(MapMode.READ_ONLY, 0, dataChannel.size());
			load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void load() throws IOException{
		
		while(indexChannel.available()>0){
			Slice kSlice = new Slice(4);
			indexChannel.read(kSlice.array());
			byte[] kk = new byte[kSlice.getInt()];
			indexChannel.read(kk);
			String key = new String(kk);
			
			Slice start = new Slice(8);

			indexChannel.read(start.array());
			start.position(0);
			
			Slice end = new Slice(8);

			indexChannel.read(end.array());
			end.position(0);
			
			index.put(key,Position.build(start.getLong(), end.getLong(), key));
		}
		
		array = new String[index.size()];
		index.keySet().toArray(array);
	}
	
	private Position get(String key){
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
	
	public String getNumber(){
		return this.number;
	}
	
	public String getIndexPath() {
		return indexPath;
	}

	public String getDataPath() {
		return dataPath;
	}

	public String searchCache(String key) throws Exception{
		//这种分配貌似比直接bytebuffer.allact效率要高一些
		
		Position pos = get(key);
		
		Slice slice = new Slice((int)(pos.getEnd()-pos.getStart()+1));
		
		ByteBuffer data = map.duplicate();
		data.position((int)pos.getStart());
		data.get(slice.array());
		
		Block b = new Block(slice);
		String result = b.get(key);
		return result;
	}
	

}
