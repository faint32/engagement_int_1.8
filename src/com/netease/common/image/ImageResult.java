package com.netease.common.image;

import android.graphics.Bitmap;

import com.netease.common.cache.file.StoreFile;

public class ImageResult {

	public String mUrl;
	public int mWidth;
	public int mHeight;
	public int mRoundCornerSize;
	public ImageType mImageType;
	public Bitmap mBitmap;
	public ImageAsyncCallback mImageCallback;
	public StoreFile mStoreFile;
	
	public ImageResult() {
		
	}
	
	public ImageResult(ImageResult result) {
		mUrl = result.mUrl;
		mWidth = result.mWidth;
		mHeight = result.mHeight;
		mRoundCornerSize = result.mRoundCornerSize;
		mImageType = result.mImageType;
		mBitmap = result.mBitmap;
		mImageCallback = result.mImageCallback;
		mStoreFile = result.mStoreFile;
	}
	
}
