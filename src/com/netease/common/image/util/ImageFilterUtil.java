package com.netease.common.image.util;


public class ImageFilterUtil {

	static {
		System.loadLibrary("ImageFilter");
	}
	
	
	public static native void brightness(Object bitmap, float bright);
}
