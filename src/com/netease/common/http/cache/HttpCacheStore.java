package com.netease.common.http.cache;

public interface HttpCacheStore {

	public void deleteHttpCache();
	
	public void deleteHttpCache(String url);
	
	public void addHttpCache(HttpCache httpCache);
	
	public HttpCache getHttpCache(String url);
	
}
