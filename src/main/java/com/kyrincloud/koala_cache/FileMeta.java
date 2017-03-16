package com.kyrincloud.koala_cache;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * 文件元数据管理，存放着目前已经生成的FileData相关的文件，包括，正在合并的，正常的，已经删除还未来得及删除的
 * 除了删除类型的文件，其他文件都可以用来做检索使用
 * @author kyrin
 *
 */
public class FileMeta {
	
	private static final Logger LOG = LoggerFactory.getLogger(FileData.class);
	
	private ConcurrentHashMap<String, FileData> filenames = new ConcurrentHashMap<String, FileData>();
	
	public FileMeta() {
	}
	
	public void put(FileData data){
		if(data != null){
			filenames.put(data.getNumber(), data);
		}
	}
	
	public FileData get(String number){
		return filenames.get(number);
	}
	
	public Slice search(Slice key){
		try{
			for(Iterator<FileData> it = filenames.values().iterator();it.hasNext();){
				FileData data = it.next();
				if(data.getStatus().code() != FileDataStatus.DELETED.code()){
					Slice value = data.searchCache(key);
					if(value != null){
						return value;
					}
				}
			}
		}catch (Exception e) {
			LOG.error("FileData search key fail.",e);
		}
		System.out.println("文件数量："+filenames.size()+" \n");
		return null;
	}
	
	public synchronized int live(){
		int living = 0;
		for(Iterator<FileData> it = filenames.values().iterator();it.hasNext();){
			FileData data = it.next();
			if(data.getStatus().code() == FileDataStatus.LIVING.code()){
				living++;
			}
			else if(data.getStatus().code() == FileDataStatus.DELETED.code()){
				it.remove();
				data.clear();
			}
		}
		return living;
	}
	
	public synchronized FileData toMerging(){
		FileData res = null;
		for(Iterator<FileData> it = filenames.values().iterator();it.hasNext();){
			FileData data = it.next();
			if(data.getStatus().code() == FileDataStatus.LIVING.code()){
				if(res == null){
					res = data;
				}else if(res.size() > data.size()){
					res = data;
				}
			}
		}
		if(res != null){
			res.setStatus(FileDataStatus.MERGING);
		}
		return res;
	}
	
	public synchronized List<FileData> toMergingList(){
		List<FileData> res = Lists.newArrayList();
		for(Iterator<FileData> it = filenames.values().iterator();it.hasNext();){
			FileData data = it.next();
			if(data.getStatus().code() == FileDataStatus.LIVING.code()){
				data.setStatus(FileDataStatus.MERGING);
				res.add(data);
			}
		}
		return res;
	}
	
}
