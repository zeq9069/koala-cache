package com.kyrincloud.koala_cache;

/**
 * 文件的状态：存活中，正在合并，已经删除
 * 
 * @author kyrin
 *
 */
public enum FileDataStatus {

	LIVING(0), MERGING(1), DELETED(2);

	int code;

	private FileDataStatus(int code) {
		this.code = code;
	}

	public int code() {
		return code;
	}

}
