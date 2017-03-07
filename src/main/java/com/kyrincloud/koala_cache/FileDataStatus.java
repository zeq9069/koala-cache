package com.kyrincloud.koala_cache;

public enum FileDataStatus {
	
	LIVING(0),
	MERGING(1),
	DELETING(2);
	
	int code ;
	
	private FileDataStatus(int code) {
		this.code = code;
	}
	
	public int code(){
		return code;
	}

}
