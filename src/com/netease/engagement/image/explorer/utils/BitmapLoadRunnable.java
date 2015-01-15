package com.netease.engagement.image.explorer.utils;

import com.netease.common.image.util.ImageUtil;
import com.netease.engagement.image.explorer.ExplorerPhotoType;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

public class BitmapLoadRunnable implements Runnable {
    
    private String path;
    private ImageView imageView;
    private ILoadBitmapCallback callback;
    private Bitmap bitmap;
    private int max;
	private ExplorerPhotoType type;
    
    public BitmapLoadRunnable(String path, ImageView imageView, int max, ExplorerPhotoType type,
    		ILoadBitmapCallback callback) {
        this.path = path;
        this.imageView = imageView;
        this.callback = callback;
        this.max = max;
        this.type = type;
    }
    
    @Override
    public void run() {
        if(path == null || path.length() == 0) {
            Log.e("loadBitmap", "error : path is empty");
        }
        if(imageView == null) {
            Log.e("loadBitmap", "error : imageView is null");
        }
        if(callback == null) {
            Log.e("loadBitmap", "error : callback is null");
        }
        
        bitmap = ImageUtil.getBitmapFromFileLimitSize(path, max);
        int degree = BitmapUtil.getInstance().readPictureDegree(path);
        if(degree != 0) {
            bitmap = BitmapUtil.getInstance().rotateBitmap(degree, bitmap);
        }
        
        String key = path + "_" + type.ordinal();
        
        if(type == ExplorerPhotoType.FILELIST) {
        	bitmap = BitmapUtil.getInstance().cropBitmapFix(bitmap, max/2, max/2);
        } else if(type == ExplorerPhotoType.GRID) {
        	bitmap = BitmapUtil.getInstance().cropBitmapFix(bitmap, max, max);
        } else if(type == ExplorerPhotoType.GALLERY) {
        	
        }
        if(bitmap != null) {
        	BitmapMemoryLruCache.getInstance().addBitmapToMemoryCache(key, bitmap);
        }
 
     	
        new Handler(Looper.getMainLooper()).post(new Runnable() {
//        imageView.post(new Runnable() {
//	This method can be invoked from outside of the UI thread only when this View is attached to a window.
// View.post 可能导致不执行，内存泄露等问题
//     imageView.getHandler().post(new Runnable() {
            @Override
            public void run() {    
            	if(imageView.getContentDescription().toString().equalsIgnoreCase(path)) {
            		callback.dealBitmap(imageView, bitmap);
            	}
            	path = null;
                imageView = null;
                callback = null;
                bitmap = null;
                max = 0;
            }
            
        });
        
    }
 
}