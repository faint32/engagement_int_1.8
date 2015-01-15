package com.netease.common.config;

import com.netease.common.cache.CacheManager;
import com.netease.common.cache.file.FileManager;
import com.netease.common.http.cache.HttpCacheManager;
import com.netease.common.http.cache.db.HttpCacheDatabase;
import com.netease.common.http.cache.pref.HttpCachePreference;
import com.netease.common.image.ImageManager;
import com.netease.common.log.NTLog;
import com.netease.common.task.TransactionEngine;

public class ConfigInit {

	public static void init() {
		
		/***************************************
		 * 缓存目录设置
		 ***************************************/
		// 缓存管理的根目录
		CacheManager.FileCacheRoot = ".test_dir";
		// 缓存在sdcard时的根目录
		CacheManager.FileCacheSDCardPrefix = "netease";
		
		/***************************************
		 * 文件缓存方式下的参数设置
		 ***************************************/
		// 缺省文件管理是否使用自动清理文件夹方式
		FileManager.FSAutoClear = true;
		// 达到多少大小时对文件目录进行自动清理
		FileManager.FSAutoClearSize = 500 * 1024 * 1024;
		// 自动清理多少天以前的数据
		FileManager.FSAutoClearTime = 7 * 24 * 3600 * 1000;
		// 缓存文件目录的子目录个数
		FileManager.FSCacheDirs = 32;
		
		/***************************************
		 * Http缓存管理的参数设置
		 ***************************************/
		// Http缓存管理控制
		HttpCacheManager.CacheControl = HttpCacheManager.CTRL_CACHE_FILE 
					| HttpCacheManager.CTRL_CACHE_DATABASE // 需要数据库provider申明
					| HttpCacheManager.CTRL_CACHE_PREFERENCE;
		
		/***************************************
		 * Http缓存管理中使用CTRL_CACHE_DATABASE的情况下，对sqlite数据表相关的参数设置
		 ***************************************/
		// Http权限
		HttpCacheDatabase.Authority = "http_cache";
		// http 数据库名
		HttpCacheDatabase.DataBaseName = "http_cache.db";
		
		/***************************************
		 * Http缓存管理中使用CTRL_CACHE_PREFERENCE的情况下，对preference的文件参数设置
		 ***************************************/
		// preference文件名
		HttpCachePreference.HttpCachePref = "http_cache";
		
		
		/***************************************
		 * 图片下载http线程等相关参数
		 ***************************************/
		// 图片下载线程优先级
		ImageManager.ImageHttpPriority = Thread.NORM_PRIORITY - 1;
		// 图片下载线程个数
		ImageManager.ImageHttpThread = 4;
		
		/***************************************
		 * 图片下载http线程等相关参数
		 ***************************************/
		// 事务线程个数
		TransactionEngine.CoreThreadCount = 6;
		// 事务线程优先级
		TransactionEngine.Priority = Thread.NORM_PRIORITY - 1;
		
		/***************************************
		 * Log
		 ***************************************/
		NTLog.DEBUG_ALL = NTLog.TO_CONSOLE;
//					| NTLog.FROM_LOGCAT | NTLog.TO_FILE;
		
	}
	
}
