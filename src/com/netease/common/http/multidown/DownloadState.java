package com.netease.common.http.multidown;

import java.util.concurrent.atomic.AtomicLong;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DownloadState {

	private static final String TARGET_PATH = "path";
	private static final String ETAG = "etag";
	private static final String CONTENT_TYPE = "c_type";
	private static final String TOTAL_SIZE = "t_size";
	private static final String DOWNLOAD_SIZES = "d_sizes";
	
	public String mUrl;
	public String mTitle;
	public String mTargetPath;
	
	String mEtag;
	String mContentType;
	
	long mTotalSize;
	
	long[] mDownloadSizes;
	long mDownloadPageSize; // 第一页文件大小，0表示只有一页
	
	AtomicLong mTotalDowndSize;
	
	public boolean mInit;
	public boolean mFinished;
	public boolean mIsCanceled;
	
	long mLastNotifySize;
	
	public DownloadState() {
		mTotalDowndSize = new AtomicLong();
	}
	
	public DownloadState(String url, JSONObject json) {
		mUrl = url;
		mTargetPath = json.optString(TARGET_PATH);
		mEtag = json.optString(ETAG);
		mContentType = json.optString(CONTENT_TYPE);
		mTotalSize = json.optLong(TOTAL_SIZE);
		
		mTotalDowndSize = new AtomicLong();
		
		JSONArray array = json.optJSONArray(DOWNLOAD_SIZES);
		mDownloadSizes = new long[array.length()];
		for (int i = 0; i < mDownloadSizes.length; i++) {
			mDownloadSizes[i] = array.optLong(i);
			mTotalDowndSize.addAndGet(mDownloadSizes[i]);
		}
		
		mDownloadPageSize = getDownloadPageSize(mTotalSize, 
				mDownloadSizes.length);
		mInit = true;
	}
	
	public boolean isCancel() {
		return mIsCanceled;
	}
	
	public void doCancel() {
		mIsCanceled = true;
	}
	
	public String toJSONString() {
		JSONObject json = new JSONObject();
		try {
			json.put(TARGET_PATH, mTargetPath);
			json.put(ETAG, mEtag);
			json.put(CONTENT_TYPE, mContentType);
			json.put(TOTAL_SIZE, mTotalSize);
			
			JSONArray array = new JSONArray();
			for (long value : mDownloadSizes) {
				array.put(value);
			}
			json.put(DOWNLOAD_SIZES, array);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json.toString();
	}

	public void init(long contentLength, String contentType) {
		mTotalSize = contentLength;
		mContentType = contentType;
		mDownloadPageSize = getDownloadPageSize(contentLength, 
				DownloadService.DownloadThreadCount);
		mDownloadSizes = new long[DownloadService.DownloadThreadCount];
		mInit = true;
	}
	
	private static long getDownloadPageSize(long totalSize, int threadSize) {
		return (totalSize / threadSize) & 0xFFFFFFFFFFFFFF00L;
	}
	
	public int getPreferThreadSize() {
		return mDownloadSizes.length;
	}

	public long getRangeStart(int index) {
		return mDownloadPageSize * index + mDownloadSizes[index];
	}

	public long getRangeEnd(int index) {
		long end = 0;
		if (index == mDownloadSizes.length - 1) {
			end = mTotalSize - 1;
		} else {
			end = (index + 1) * mDownloadPageSize - 1;
		}
		return end;
	}

	public boolean increaseDownloadSize(int index, int size) {
		mDownloadSizes[index] += size;
		boolean ret = false;
		long newSize = mTotalDowndSize.addAndGet(size);
		if (newSize == mTotalSize) {
			mFinished = true;
			ret = true;
		} else if ((newSize - mLastNotifySize) << 10 > mTotalSize){
			mLastNotifySize = newSize;
			ret = true;
		}
		return ret;
	}
	
	public long getPageSize(int index) {
		long pageSize = 0;
		if (index == mDownloadSizes.length - 1) {
			pageSize = mTotalSize - mDownloadPageSize * index;
		} else {
			pageSize = mDownloadPageSize;
		}
		return pageSize;
	}
	
	public long getDownloadSize(int index) {
		return mDownloadSizes[index];
	}
	
	public long getTotalDownloadSize() {
		return mTotalDowndSize.get();
	}
	
	public long getTotalSize() {
		return mTotalSize;
	}
	
	public static String formatDownloadSize(long value) {
		String bytes = null;
		if (value < (15 << 6)) {
			bytes = String.format("%dB", value);
		} else if (value < (15 << 16)) {
			bytes = String.format("%.2fKB", ((double) value) / (1 << 10));
		} else if (value < (15 << 26)) {
			bytes = String.format("%.2fMB", ((double) value) / (1 << 20));
		} else {
			bytes = String.format("%.2fGB", ((double) value) / (1 << 30));
		}
		
		return bytes;
	}
	
	public static String formatDownloadSize(long value, long total) {
		return String.format("%s/%s", formatDownloadSize(value), 
				formatDownloadSize(total));
	}
}
