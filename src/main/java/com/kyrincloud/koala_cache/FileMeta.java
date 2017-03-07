package com.kyrincloud.koala_cache;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class FileMeta {
	
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
	
	public String search(String key){
		try{
			for(Iterator<FileData> it = filenames.values().iterator();it.hasNext();){
				FileData data = it.next();
				if(data.getStatus().code() != FileDataStatus.DELETING.code()){
					String value = data.searchCache(key);
					if(value != null){
						return value;
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public synchronized int live(){
		int living = 0;
		for(Iterator<FileData> it = filenames.values().iterator();it.hasNext();){
			FileData data = it.next();
			if(data.getStatus().code() == FileDataStatus.LIVING.code()){
				living++;
			}
			if(data.getStatus().code() == FileDataStatus.DELETING.code()){
				data.clear();
				it.remove();
			}
		}
		return living;
	}
	
	public synchronized FileData toMerging(){
		for(Iterator<FileData> it = filenames.values().iterator();it.hasNext();){
			FileData data = it.next();
			if(data.getStatus().code() == FileDataStatus.LIVING.code()){
				data.setStatus(FileDataStatus.MERGING);
				return data;
			}
		}
		return null;
	}
	
}
