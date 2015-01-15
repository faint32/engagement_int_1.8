package com.netease.common.http.cache;

import com.netease.common.cache.CacheManager;
import com.netease.common.cache.file.StoreFile;
import com.netease.common.config.IConfig;
import com.netease.common.debug.CheckAssert;
import com.netease.common.http.THttpRequest;
import com.netease.common.http.cache.db.HttpCacheDatabase;
import com.netease.common.http.cache.pref.HttpCachePreference;


/**
 * Http Cache管理，支持ETag和Express两种方式
 * 
 * Http Cache机制可参考http://wuhua.iteye.com/blog/400368
 * 
 * @author dingding
 *
 */
public class HttpCacheManager implements IConfig {

	private static final boolean DEBUG = false;
	
	/****************************IConfig********************************/
	
	/**
	 * 控制一般的http cache是否有效。有效时，当THttpRequest设置使用文件
	 * cache方式时，HttpCacheManager通过查询文件是否存在和是否过期，并可能通过网络
	 * 验证判断cache是否有效。
	 * 
	 * 一般提供给会产生更新的数据缓存使用
	 */
	public static final short CTRL_CACHE_DATABASE = 0x01;
	
	/**
	 * 数据库方式未设置情况下（CTRL_CACHE_DATABASE），使用Preference进行
	 * 对Http一般缓存的信息记录
	 */
	public static final short CTRL_CACHE_PREFERENCE = 0x02;
	
	/**
	 * 控制整个文件方式的http cache是否有效。有效时，当THttpRequest设置使用文件
	 * cache方式时，HttpCacheManager通过查询文件是否存在判断cache是否有效。
	 * 
	 * 一般提供给图片类缓存使用
	 * 
	 * 注意：当http cache有效且file cache不有效时，file cache采用普通http cache处理
	 */
	public static final short CTRL_CACHE_FILE = 0x04;
	
	
	/**
	 * 备注：数据库方式无效情况下
	 */
	public static short CacheControl = CTRL_CACHE_FILE | CTRL_CACHE_DATABASE | CTRL_CACHE_PREFERENCE;
	
	/****************************IConfig********************************/
	
	/**
	 * 控制一般的http cache是否有效
	 * @return
	 */
	private static boolean isHttpCacheDatabaseEnable() {
		return (CacheControl & CTRL_CACHE_DATABASE) != 0;
	}
	
	/**
	 * 控制是否使用Preferrence对一般缓存进行管理
	 * @return
	 */
	private static boolean isHttpCachePreferenceEnable() {
		return (CacheControl & CTRL_CACHE_PREFERENCE) != 0;
	}
	
	/**
	 * 控制整个文件方式的http cache是否有效
	 * 
	 * 注意：当http cache有效且file cache不有效时，file cache采用普通http cache处理
	 * 
	 * @return
	 */
	private static boolean isHttpCacheFileEnable() {
//		return (CacheControl & CTRL_CACHE_FILE) != 0;
		return true;
	}
	
	public static void deleteHttpCache(HttpCache data) {
		if (data == null) {
			return ;
		}
		
		StoreFile storeFile = data.LocalFile;
		if (storeFile != null) {
			storeFile.delete();
		}
		
		HttpCacheStore cacheStore = getHttpCacheStore();
		if (cacheStore != null) {
			cacheStore.deleteHttpCache(data.Url);
		}
	}
	
	public static void deleteHttpCache(THttpRequest request) {
		if (request == null) {
			return ;
		}
		
		if (request.isCacheDatabase()) {
			StoreFile storeFile = CacheManager.getStoreFile(request.getCacheUrl());
			storeFile.delete();
			
			HttpCacheStore cacheStore = getHttpCacheStore();
			if (cacheStore != null) {
				cacheStore.deleteHttpCache(request.getCacheUrl());
			}
		}
		else if (request.isCacheFile()) {
			HttpCacheFileStore.deleteHttpFileCache(request.getCacheUrl());
		}
	}
	
	public static HttpCache getHttpCache(THttpRequest request) {
		CheckAssert.checkNull(request);
		
		HttpCache httpCache = null;
		
		if (request.isCacheDatabase()) {
			HttpCacheStore cacheStore = getHttpCacheStore();
			
			if (cacheStore != null) {
				httpCache = cacheStore.getHttpCache(request.getCacheUrl());
			}
		}
		else if (request.isCacheFile() && isHttpCacheFileEnable()) {
			httpCache = HttpCacheFileStore.getHttpFileCache(request.getCacheUrl());
		}
		
		if (httpCache != null && httpCache.LocalFile != null) {
			if (request.isCacheGzip()) {
				httpCache.LocalFile.setGzip();
			}
		}
		
		return httpCache;
	}
	
	public static void addHttpCache(THttpRequest request, HttpCache httpCache) {
		if (request.isCacheDatabase()) {
			HttpCacheStore cacheStore = getHttpCacheStore();
			
			if (cacheStore != null) {
				cacheStore.addHttpCache(httpCache);
			}
		} else if (request.isCacheFile() && isHttpCacheFileEnable()) {
			HttpCacheFileStore.addHttpFileCache(httpCache);
		}
	}
	
	private static HttpCacheStore getHttpCacheStore() {
		HttpCacheStore cacheStore = null;
		
		if (isHttpCacheDatabaseEnable()) {
			cacheStore = HttpCacheDatabase.getInstance();
		} else if (isHttpCachePreferenceEnable()) {
			cacheStore = HttpCachePreference.getInstance();
		} else if (isHttpCacheFileEnable()) {
			cacheStore = HttpCacheFileStore.getInstance();
		}
		
		return cacheStore;
	}

}
