package com.netease.common.http.cache.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.netease.common.config.IConfig;
import com.netease.common.db.BaseDBManager;
import com.netease.common.http.cache.HttpCache;
import com.netease.common.http.cache.HttpCacheStore;
import com.netease.common.http.cache.db.HttpCacheDBTables.CacheTable;
import com.netease.common.service.BaseService;

public class HttpCacheDatabase extends BaseDBManager implements HttpCacheStore, IConfig {

	/*************************IConfig******************************/
	
	/**
	 * http cache database authority
	 */
	public static String Authority = "http_cache";
	
	/**
	 * 
	 */
	public static String DataBaseName = "http_cache.db";

	
	/*******************************************************/
	
//	private static HttpCacheDatabase mInstance;
	
	/*******************************************************/
	
	
	public static HttpCacheDatabase getInstance() {
//		if (mInstance == null) {
//			mInstance = new HttpCacheDatabase();
//		}
//		return mInstance;
		return new HttpCacheDatabase();
	}
	
	@Override
	public void addHttpCache(HttpCache httpCache) {
		Context context = BaseService.getServiceContext();
		if (context != null && httpCache != null && httpCache.LocalFile != null) {
			ContentValues values = new ContentValues();
			values.put(CacheTable.C_URL, httpCache.Url);
			values.put(CacheTable.C_LASTMODIFY, httpCache.LastModefy);
			values.put(CacheTable.C_CONTENT_LENGTH, httpCache.ContentLength);
			
			values.put(CacheTable.C_ETAG, httpCache.ETag);
			values.put(CacheTable.C_EXPIRES_STR, httpCache.ExpiresString);
			values.put(CacheTable.C_EXPIRES, httpCache.Expires);
			values.put(CacheTable.C_LOCAL_PATH, httpCache.LocalFile.getPath());
			values.put(CacheTable.C_CONTENT_ENCODING, httpCache.ContentEncoding);
			values.put(CacheTable.C_CHARSET, httpCache.Charset);
			values.put(CacheTable.C_MIME, httpCache.MimeType);
			
			getHttpCacheProvider(context).insert(CacheTable.CONTENT_URI, values);
		}
	}

	@Override
	public HttpCache getHttpCache(String url) {
		HttpCache httpCache = null;
		
		Context context = BaseService.getServiceContext();
		if (context != null && url != null) {
			Cursor cursor = getHttpCacheProvider(context).query(
					CacheTable.CONTENT_URI, HttpCache.Projection,
					CacheTable.C_URL + "=?", new String[]{url}, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					httpCache = new HttpCache(url, cursor);
					if (httpCache.LocalFile == null || ! httpCache.LocalFile.exists()) {
                        httpCache = null;
                    }
				}
				
				cursor.close();
			}
		}
		
		return httpCache;
	}
	
	
	@Override
	public void deleteHttpCache(String url) {
		Context context = BaseService.getServiceContext();
		if (context != null && url != null) {
			getHttpCacheProvider(context).delete(CacheTable.CONTENT_URI, 
					CacheTable.C_URL + "=?", new String[]{url});
		}
	}

	@Override
	public void deleteHttpCache() {
		Context context = BaseService.getServiceContext();
		if (context != null) {
			getHttpCacheProvider(context).delete(CacheTable.CONTENT_URI, 
					null, null);
		}
	}
	
	/*******************************************************/
	
	
	private static HttpCacheDBProvider mDBProvider;
	
	private static HttpCacheDBProvider getHttpCacheProvider(Context context) {
		HttpCacheDBProvider provider = mDBProvider;
		if (provider == null) {
			provider = new HttpCacheDBProvider(context);
			mDBProvider = provider;
		}
		
		return provider;
	}
}
