package com.kyrincloud.koala_cache.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.kyrincloud.koala_cache.comparator.AbstractComparator;

public class FileData {
	
private static final Log LOG = LogFactory.getLog(FileData.class);
	
	private ConcurrentSkipListMap<Slice, Position> index = new ConcurrentSkipListMap<Slice, Position>(new Comparator<Slice>() {
		public int compare(Slice o1, Slice o2) {
			return o1.compareTo(o2);
		}
	});
	
	FileInputStream indexChannel;
	
	FileChannel dataChannel;
	
	ReentrantLock lock = new ReentrantLock();
	
	MappedByteBuffer map ;
	
	public String indexPath;
	
	public String dataPath;
	
	private String number;
	
	private FileDataStatus status = FileDataStatus.LIVING;
	
	private long size;
	
	private AbstractComparator comparator;
	
	@SuppressWarnings("resource")
	public FileData(String indexPath , String dataPath , AbstractComparator comparator){
		this.indexPath = indexPath;
		this.dataPath = dataPath;
		this.comparator = comparator;
		try {
			File index = new File(indexPath);
			number = index.getName().substring(0, index.getName().indexOf("."));
			indexChannel = new FileInputStream(index);
			dataChannel = new RandomAccessFile(new File(dataPath),"rw").getChannel();
			map = dataChannel.map(MapMode.READ_ONLY, 0, dataChannel.size());
			size = dataChannel.size();
			load();
		} catch (IOException e) {
			LOG.error("FileData init fail.",e);
		}
	}
	
	private void load() throws IOException{
		
		while(indexChannel.available()>4){
			Slice kSlice = new Slice(4);
			indexChannel.read(kSlice.array());
			Slice key = new Slice(kSlice.getInt());
			indexChannel.read(key.array());
			
			Slice start = new Slice(8);

			indexChannel.read(start.array());
			start.position(0);
			
			Slice end = new Slice(8);

			indexChannel.read(end.array());
			end.position(0);
			
			index.put(key,Position.build(start.getLong(), end.getLong()));
		}
	}
	
	private Position get(Slice key){
		if(key == null){
			return null;
		}
		
		Entry<Slice, Position> values = index.ceilingEntry(key);
		if(values == null){
			return null;
		}
        return values.getValue();
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
	
	public void setStatus(FileDataStatus status){
		this.status = status;
	}
	
	public long size(){
		return size;
	}
	
	public FileDataStatus getStatus(){
		return this.status;
	}
	
	public synchronized void clear() {

		try {
			if (indexChannel != null) {
				indexChannel.close();
			}
			if (dataChannel != null && dataChannel.isOpen()) {
				dataChannel.close();
			}
			File index = new File(indexPath);
			File data = new File(dataPath);
			if (index.exists()) {
				index.delete();
			}
			if (data.exists()) {
				data.delete();
			}
		} catch (IOException e) {
			LOG.error("FileData clear fial.",e);
		}
	}

	public Entity searchCache(Slice key) throws Exception {
		// 这种分配貌似比直接bytebuffer.allact效率要高一些

		Position pos = get(key);

		if (pos == null) {
			return null;
		}

		Slice slice = new Slice((int) (pos.getEnd() - pos.getStart()));

		ByteBuffer data = map.duplicate();
		data.position((int) pos.getStart());
		data.get(slice.array());

		Block b = new Block(slice,comparator);
		return b.get(key);
	}

}
