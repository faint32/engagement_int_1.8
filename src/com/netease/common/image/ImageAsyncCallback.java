package com.netease.common.image;

import android.graphics.Bitmap;

import com.netease.common.image.task.ImageTransaction;

public interface ImageAsyncCallback {

	public void startImageTransacion(ImageTransaction trans);
	
	public boolean onPreUiGetImage(int tid, Bitmap bitmap);
	
	public String getCacheUrl(ImageTransaction trans);
	
	/**
	 * webp 或者 jpg转化中，是否需要保持image的透明属性
	 * @param trans
	 * @return
	 */
	public boolean isTransport(ImageTransaction trans);
	
	public void onUiGetImage(int tid, Bitmap bitmap);
	
	public boolean isValid(int tid);
	
	/**
	 * 图片链接重定向处理 
	 */
	public String onRedirectUrl(String originalUrl, String redirectUrl);
	
}
