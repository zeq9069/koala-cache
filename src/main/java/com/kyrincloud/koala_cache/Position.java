package com.kyrincloud.koala_cache;

/**
 * key的位置信息
 * 
 * @author kyrin
 *
 */
public class Position {

	private String key;

	private long start;

	private long end;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public static Position build(long start, long end, String key) {
		Position pos = new Position();
		pos.setStart(start);
		pos.setEnd(end);
		pos.setKey(key);
		return pos;
	}

	@Override
	public String toString() {
		return "Position [key=" + key + ", start=" + start + ", end=" + end + "]";
	}

}
