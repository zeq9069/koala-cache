package com.kyrincloud.koala_cache.consume;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DiskStore implements Store{
	
	private String cacheName;

	private Map<Object, ElementIndex> diskElements = Collections.synchronizedMap(new HashMap<Object, ElementIndex>());//已经存在硬盘的element
	
	private Map<Object, Element> addElements = Collections.synchronizedMap(new HashMap<Object, Element>());//新添加到disk上的元素
	
	private List<ElementIndex> freeBlock = Collections.synchronizedList(new ArrayList<ElementIndex>());//删除的element对应的索引
	
	private String indexPathDir;
	
	private boolean persistence;
	
	private RandomAccessFile randomAccessFile;
	
	private File indexFile;
	
	private File dataFile;
	
	private Thread flushElementThread;
	
	private Thread expireElementThread;
	
	private long threadInterval = 1000l;
	
	private long totalSize;//磁盘存储容量
	
	public DiskStore(String cacheName,String indexPathDir) throws Exception {
		this.cacheName = cacheName;
		this.indexPathDir = indexPathDir;
		initialize();
	}
	
	private void initialize() {
		//TODO 完善
		try{
		//1.初始化文件
		loadFile();
		
		//2.start thread
		if(persistence){
			flushElementThread = new Thread(new Runnable() {
				
				public void run() {
					while(true){
						if(!persistence){
							return;
						}
						
						try {
							TimeUnit.MILLISECONDS.sleep(threadInterval);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if(addElements.size() > 0){
							flushAddElements();
						}
					}
				}
			});
		}
		
		
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	private void loadFile() throws Exception{
		File dir = new File(cacheName);
		if(dir.exists() && !dir.isDirectory()){
			throw new Exception("cacheName not is directory.");
		}
		if(!dir.exists() && !dir.createNewFile()){
			throw new Exception("create cacheName directory fail.");
		}
		
		indexFile = new File(indexPathDir,getIndexFileName());
		dataFile = new File(indexPathDir, getDataFileName());
		
		deleteIndexIfDataNotExist();
		
		if(persistence){
			if(!loadIndex()){
				dataFile.delete();
			}
		}else{
			dataFile.delete();
		}
	}
	
	private void deleteIndexIfDataNotExist(){
		if(!dataFile.exists() && indexFile.exists()){
			indexFile.delete();
		}
	}
	
	//加载完成或者失败之后，把原来的索引文件删除，重新创建索引文件，这个策略ehcache中有描述
	private boolean loadIndex() {
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		boolean success = false;
		if(indexFile.exists()){
			try {
				fis = new FileInputStream(indexFile);
				ois = new ObjectInputStream(fis);
				diskElements = (Map<Object, ElementIndex>) ois.readObject();
				freeBlock = (List<ElementIndex>) ois.readObject();
				success = true;
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}catch(IOException e){
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}finally{
				try {
					if(fis != null){
						fis.close();
					}
					if(ois != null){
						ois.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				createNewIndexFile();
			}
		}else{
			createNewIndexFile();
		}
		return success;
	}
	
	private void createNewIndexFile(){
		if(indexFile.exists()){
			indexFile.delete();
			try {
				indexFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void put(Element element) {
		
	}

	public Element getByKey(Object key) {
		return null;
	}

	public int getSzie() {
		return 0;
	}
	
	public String getIndexFileName(){
		return cacheName+".index";
	}
	
	public String getDataFileName(){
		return cacheName+".data";
	}
	
	public void flushAddElements(){
	}

}
