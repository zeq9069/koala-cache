package com.kyrincloud.koala_cache.core;

/**
 * key的位置信息
 * 
 * @author kyrin
 *
 */
public class Position {

	private long start;

	private long end;

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

	public static Position build(long start, long end) {
		Position pos = new Position();
		pos.setStart(start);
		pos.setEnd(end);
		return pos;
	}

	@Override
	public String toString() {
		return "Position [ start=" + start + ", end=" + end + "]";
	}

}
