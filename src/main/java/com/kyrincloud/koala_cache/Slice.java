package com.kyrincloud.koala_cache;

/**
 * 分片
 * 
 * @author kyrin
 *
 */
public class Slice implements Comparable<Slice>{

	byte[] block;

	int position;

	int limit;

	public Slice(int cap) {
		this.block = new byte[cap];
		this.limit = cap;
	}

	public Slice(byte[] block) {
		this.block = block;
		this.limit = block.length;
	}

	public byte[] array() {
		return block;
	}

	public int position() {
		return position;
	}

	public void position(int position) {
		this.position = position;
	}

	public int limit() {
		return limit;
	}

	public void limit(int limit) {
		this.limit = limit;
	}

	public int remaining() {
		return limit - position;
	}

	public int size() {
		return block.length;
	}

	public int getInt() {
		int value;
		value = (int) ((block[position + 3] & 0xFF) | ((block[position + 2] & 0xFF) << 8)
				| ((block[position + 1] & 0xFF) << 16) | ((block[position] & 0xFF) << 24));
		position += 4;
		return value;
	}

	public long getLong() {
		long value;
		value = (long) ((block[position + 7] & 0xFF) | ((block[position + 6] & 0xFF) << 8)
				| ((block[position + 5] & 0xFF) << 16) | ((block[position + 4] & 0xFF) << 24)
				| ((block[position + 3] & 0xFF) << 32) | ((block[position + 2] & 0xFF) << 40)
				| ((block[position + 1] & 0xFF) << 48) | ((block[position] & 0xFF) << 56));
		position += 8;
		return value;
	}

	public void putInt(int value) {
		block[position] = (byte) ((value >> 24) & 0xFF);
		block[position + 1] = (byte) ((value >> 16) & 0xFF);
		block[position + 2] = (byte) ((value >> 8) & 0xFF);
		block[position + 3] = (byte) (value & 0xFF);
		position += 4;
	}

	public void putLong(long value) {
		block[position] = (byte) ((value >> 56) & 0xFF);
		block[position + 1] = (byte) ((value >> 48) & 0xFF);
		block[position + 2] = (byte) ((value >> 40) & 0xFF);
		block[position + 3] = (byte) ((value >> 32) & 0xFF);
		block[position + 4] = (byte) ((value >> 24) & 0xFF);
		block[position + 5] = (byte) ((value >> 16) & 0xFF);
		block[position + 6] = (byte) ((value >> 8) & 0xFF);
		block[position + 7] = (byte) (value & 0xFF);
		position += 8;
	}

	public void put(byte[] values) {
		System.arraycopy(values, 0, block, position, values.length);
		position += values.length;
	}

	public void get(byte[] value) {
		System.arraycopy(block, position, value, 0, value.length);
		position+=value.length;
	}

	public byte[] getBlock() {
		return block;
	}

	public void setBlock(byte[] block) {
		this.block = block;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public static void main(String[] args) {
		byte[] f = new byte[] { 1, 2, 2 };
		Slice slice = new Slice(11);
		slice.putLong(10);
		slice.put(f);
		slice.position(0);
		System.out.println(slice.getInt());
	}

	public int compareTo(Slice that) {
		if (this == that) {
			return 0;
		}
		if (this.block == that.block && position == that.position && this.limit == that.limit) {
			return 0;
		}

		if (this.size() != that.size()) {
			return this.size() - that.size();
		}

		for (int i = 0; i < this.size(); i++) {
			int thisByte = 0xFF & this.block[this.position + i];
			int thatByte = 0xFF & that.block[that.position + i];
			if (thisByte != thatByte) {
				return (thisByte) - (thatByte);
			}
		}
		return 0;
	}
}
