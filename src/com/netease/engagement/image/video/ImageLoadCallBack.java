package com.netease.engagement.image.video;

import android.graphics.Bitmap;

/**
 * 图片加载，回调接口
 */
public interface ImageLoadCallBack {
	//图片加载完成
	public void onImageLoaded(Bitmap bitmap);
}
