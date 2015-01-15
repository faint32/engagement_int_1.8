package com.netease.common.http.filedownload;


public interface FileDownloadListener {

	public void onSuccess(String path);
	
	public void onFailed(String err, int errCode);
	
	public void onProgress(long current, long total, int percent, int speed);
	
}
