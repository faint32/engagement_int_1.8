package com.netease.common.http.cache;

import com.netease.common.cache.CacheManager;
import com.netease.common.cache.file.StoreFile;


public class HttpCacheFileStore implements HttpCacheStore {

//	private static HttpCacheFileStore mInstance;
	
	public static HttpCacheStore getInstance() {
//		if (mInstance != null) {
//			mInstance = new HttpCacheFileStore();
//		}
		return new HttpCacheFileStore();
	}
	
	@Override
	public void addHttpCache(HttpCache httpCache) {
		addHttpFileCache(httpCache);
	}

	@Override
	public HttpCache getHttpCache(String url) {
		HttpCache httpCache = new HttpCache();
		httpCache.LocalFile = CacheManager.getStoreFile(url);
		return httpCache;
	}
	
	@Override
	public void deleteHttpCache(String url) {
		StoreFile storeFile = CacheManager.getStoreFile(url);
		storeFile.delete();
	}
	
	public static HttpCache getHttpFileCache(String url) {
		HttpCache httpCache = new HttpCache(url, CacheManager.getStoreFile(url));
		return httpCache; 
	}

	public static void addHttpFileCache(HttpCache cache) {
		
	}
	
	public static void deleteHttpFileCache(String url) {
		
	}

	@Override
	public void deleteHttpCache() {
		
	}

}
