package com.netease.common.cache.file;

import android.util.Log;

public class StoreDeleteCallback {

	private static final boolean DEBUG = false;
	private static final String TAG = "StoreDeleteCallback";
	
	int mSize;
	long mLength;
	
	int mTotalSize = -1;
	long mTotalLength = -1;
	
	long mDeleteStart;
	
	boolean mIsCancel;
	boolean mComputeTime;
	
	public StoreDeleteCallback() {
	}
	
	public StoreDeleteCallback(boolean computeTime) {
		mComputeTime = computeTime;
	}
	
	public void reset() {
		mIsCancel = false;
		mComputeTime = false;
		mDeleteStart = 0;
		mSize = 0;
		mLength = 0;
		mTotalSize = -1;
		mTotalLength = -1;
	}
	
	public final boolean isCancel() {
		return mIsCancel;
	}

	public final void doCancel() {
		mIsCancel = true;
	}
	
	public void startDelete() {
		mDeleteStart = System.currentTimeMillis();
	}
	
	public final void setTotalSize(int size, long length) {
		if (DEBUG) Log.i(TAG, "setTotalSize: Size: " + size + " Length: " + length);
		
		if (mTotalSize < size) {
			mTotalSize = size;
			mTotalLength = length;
		}
	}
	
	public final void addDelete(int size, long length) {
		if (DEBUG) Log.i(TAG, "addDelete: Size: " + size + " Length: " + length);
		
		mSize += size;
		mLength += length;
		
		computeTime();
	}
	
	private void computeTime() {
		if (mComputeTime && ! mIsCancel) {
			long time = -1;
			if (mTotalSize > mSize) {
				time = (System.currentTimeMillis() - mDeleteStart) 
					* (mTotalSize - mSize) / mSize; 
			}
			timeNeeded(time);
		}
	}
	
	/**
	 * @param time 小于0表示所需时间 
	 */
	protected void timeNeeded(long time) {
		
	}
	
	public long getLength() {
		return mLength;
	}
	
	public int getSize() {
		return mSize;
	}
	
	public int getTotalSize() {
		return mTotalSize;
	}
	
	public void endDetele() {
		timeNeeded(0);
	}
}
