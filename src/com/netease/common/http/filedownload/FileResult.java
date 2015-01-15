package com.netease.common.http.filedownload;

import java.util.ArrayList;

import com.netease.common.cache.file.StoreFile;

public class FileResult {

	public String mUrl;
	public ArrayList<FileDownloadListener> mCallback = new ArrayList<FileDownloadListener>();
	public StoreFile mStoreFile;
	public String mError;
	public int errCode;
	
	public FileResult() {
		
	}
	
	public FileResult(FileResult result) {
		mUrl = result.mUrl;
		mCallback = result.mCallback;
		mStoreFile = result.mStoreFile;
		mError = result.mError;
		errCode = result.errCode;
	}
	
}
