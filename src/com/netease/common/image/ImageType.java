package com.netease.common.image;

public enum ImageType {

	// 不使用内存缓存
	NoCache,
	// 使用内存缓存
	MemCache,
	// 圆角不进行缓存
	RoundNoCache,
	// 圆角内存缓存
	RoundMemCache,
	// 圆形不进行缓存
	CircleNoCache,
	// 圆形内存缓存
	CircleMemCache,
	// 无需Decode
	NoDecoded,
}
