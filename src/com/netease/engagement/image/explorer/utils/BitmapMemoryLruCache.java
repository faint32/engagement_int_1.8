package com.netease.engagement.image.explorer.utils;

import android.graphics.Bitmap;

import com.netease.common.image.ImageManager;


public class BitmapMemoryLruCache {
	private static BitmapMemoryLruCache instance;
	
	private BitmapMemoryLruCache() {

	}
	
	public static BitmapMemoryLruCache getInstance() {
		try {
			if(instance == null) {
				instance = new BitmapMemoryLruCache();
			}
		} catch(Exception e) {
		}
		return instance;
	}
	
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		ImageManager.getInstance().addImageCache(key, bitmap);
	}  
	  
	public Bitmap getBitmapFromMemCache(String key) {  
	    return ImageManager.getInstance().getImageCache(key);  
	}  
	
	public void deleteBitmapFromMemCache() {
		
	}

	public static void closeBitmapMemCache() {
	}
}
