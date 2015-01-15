package com.netease.common.cache.file;

public class StoreSizeResult {
	public String mPath;
	public int mSize;
	public long mLength;
	
	@Override
	public String toString() {
		return "Path: " + mPath + " Size: " + mSize + " Length: " + mLength;
	}
}
