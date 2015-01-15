package com.netease.common.cache;

import java.io.File;

import org.apache.http.HttpEntity;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.netease.common.cache.file.FileManager;
import com.netease.common.cache.file.StoreDeleteCallback;
import com.netease.common.cache.file.StoreFile;
import com.netease.common.cache.task.ClearCacheTask;
import com.netease.common.config.IConfig;
import com.netease.common.service.BaseService;
import com.netease.common.task.TransactionEngine;
import com.netease.util.PlatformUtil;
import com.netease.util.Util;


public class CacheManager implements IConfig {

	private static FileManager mSDCardFileManager;
	private static FileManager mIntervalFileManager;
	
	
	/************************以下 IConfig 配置项*******************************/
	
	// 建议使用.开头作为缓存文件夹名，以隐藏多媒体文件
	public static String FileCacheRoot = "/.test_cache";
	
	// 建议使用package名字作为sdcard文件夹的前置文件夹名
	public static String FileCacheSDCardPrefix = "/netease";
	
	/************************以上 IConfig 配置项*******************************/
	
	
	private static FileManager getFileManager() {
		FileManager fileManager = null;
		
		if(PlatformUtil.hasStorage()) {
			fileManager = getSDCardFileManager();
		} else {
			fileManager = getIntervalFileManager();
		}
		
		return fileManager;
	}
	
	private static synchronized FileManager getSDCardFileManager() {
		if (mSDCardFileManager == null) {
			mSDCardFileManager = new FileManager(Environment.getExternalStorageDirectory().getPath() 
					+ FileCacheSDCardPrefix + FileCacheRoot);
		}
		
		return mSDCardFileManager;
	}
	
	private static synchronized FileManager getIntervalFileManager() {
		if (mIntervalFileManager == null) {
			String path = BaseService.getServiceContext().getApplicationContext()
					.getFilesDir().getParent();
			if (path.endsWith("/")) {
				path = path.substring(0, path.length() - 1);
			}
			
			mIntervalFileManager = new FileManager(path + FileCacheRoot);
		}
		
		return mIntervalFileManager;
	}
	
	/**
	 * 获取缓存根路径
	 * 
	 * @return
	 */
	public static String getCacheRoot() {
		FileManager manager = getFileManager();
		return manager.getPath();
	}
	
	/**
	 * 获取根路径
	 * 
	 * @return
	 */
	public static String getRoot() {
		FileManager manager = getFileManager();
		return new File(manager.getPath()).getParent();
	}
	
	public static StoreFile getStoreFile(String url) {
		return getStoreFile(url, null);
	}
	
	public static StoreFile getStoreFile(String url, HttpEntity entity) {
		StoreFile file = null;
		
		if (entity == null) {
			file = getFileManager().getStoreFile(url);
		} else {
			file = getFileManager().getStoreFile(url);
		}
		return file;
	}
	
	public static StoreFile parseStoreFile(String localPath) {
		StoreFile storeFile = null;
		if (! TextUtils.isEmpty(localPath)) {
			storeFile = getFileManager().parseStoreFile(localPath);
		} 
		
		return storeFile;
	}
	
	public static boolean checkCacheSizeAvaiable(long size) {
		return true;
	}
	
	public static void deleteCache(StoreDeleteCallback callback, 
			long timeline, boolean force) {
		getFileManager().delete(callback, timeline, true);
	}
	
	public static void deleteCacheSync() {
		String path = getFileManager().getPath();
		if (! TextUtils.isEmpty(path)) {
			Util.delAllFiles(path);
			
			getFileManager().lengthReset();
		}
	}
	
	public static void deleteCache() {
		TransactionEngine.Instance().beginTransaction(new ClearCacheTask());
	}
	
	/**
	 * 获取文件缓存大小
	 * 
	 * @return
	 */
	public static long getCacheSize() {
		return getFileManager().length();
	}
	
}
