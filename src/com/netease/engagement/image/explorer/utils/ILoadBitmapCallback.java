package com.netease.engagement.image.explorer.utils;

import android.graphics.Bitmap;
import android.widget.ImageView;

public interface ILoadBitmapCallback {
	public abstract void dealBitmap(ImageView imageView, Bitmap bitmap);
}
