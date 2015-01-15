package com.netease.common.image;

/**
 * 图片网络请求判断控制
 * 
 * @author dingding
 *
 */
public enum ImageNetControl {
	Default, // 缺省控制，ImageManager.DownloadType 控制
	CacheOnly, // 只使用本地cache
	WifiOnly, // 只使用本地cache或者wifi下下图
	All, // 所有网络下都下图
}
