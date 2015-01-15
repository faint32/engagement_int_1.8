package com.netease.common.http.multidown;

public interface DownloadListener {

	public void notifyDownloadState(DownloadState state);
	
}
