package com.netease.common.http.cache.pref;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

import com.netease.common.config.IConfig;
import com.netease.common.debug.CheckAssert;
import com.netease.common.http.cache.HttpCache;
import com.netease.common.http.cache.HttpCacheStore;
import com.netease.common.service.BaseService;

public class HttpCachePreference implements HttpCacheStore, IConfig {

	/*******************************************************/
	
	public static String HttpCachePref = "httpcache"; 
	
//	private static HttpCachePreference mInstance;
	
	/*******************************************************/
	
	public static HttpCachePreference getInstance() {
//		if (mInstance == null) {
//			mInstance = new HttpCachePreference();
//		}
//		return mInstance;
		return new HttpCachePreference();
	}
	
	@Override
	public void addHttpCache(HttpCache httpCache) {
		CheckAssert.checkNull(httpCache);
		
		SharedPreferences preferences = getHttpCachePreference();
		
		if (preferences != null && httpCache.LocalFile != null) {
			preferences.edit().putString(httpCache.Url, 
					httpCache.toJSONString()).commit();
		}
	}
	
	@Override
	public HttpCache getHttpCache(String url) {
		HttpCache httpCache = null;
		
		SharedPreferences preferences = getHttpCachePreference();
		if (preferences != null) {
			String value = preferences.getString(url, null);
			if (value != null) {
				try {
					JSONObject json = new JSONObject(value);
					httpCache = new HttpCache(url, json);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		
		return httpCache;		
	}
	
	@Override
	public void deleteHttpCache(String url) {
		SharedPreferences preferences = getHttpCachePreference();
		if (preferences != null) {
			preferences.edit().remove(url).commit();
		}
	}
	
	@Override
	public void deleteHttpCache() {
		SharedPreferences preferences = getHttpCachePreference();
		if (preferences != null) {
			preferences.edit().clear().commit();
		}
	}
	
	private SharedPreferences getHttpCachePreference() {
		SharedPreferences preferences = null;
		Context context = BaseService.getServiceContext();
		if (context != null) {
			preferences = context.getSharedPreferences(
					HttpCachePref, Context.MODE_PRIVATE);
		}
		
		return preferences;
	}

}
