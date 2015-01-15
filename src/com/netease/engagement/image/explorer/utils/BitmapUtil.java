package com.netease.engagement.image.explorer.utils;

import com.netease.engagement.image.explorer.ExplorerPhotoType;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;
import android.widget.ImageView;

public class BitmapUtil {
	
	private static BitmapUtil instance;
	
	public static BitmapUtil getInstance() {
		if(instance == null) {
			instance = new BitmapUtil();
		}
		return instance;
	}
	
	public int readPictureDegree(String path) {
		int degree = 0;
		
		if(path.equalsIgnoreCase("")) {
			return degree;
		}
		
		try{
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			default:
				break;
			}
		} catch(Exception e) {
			Log.e("BitmapFunc.readPictureDegree", e.getMessage());
		}
		return degree;
	}
	
	public Bitmap rotateBitmap(int angle, Bitmap origBitmap) {
		
		if(origBitmap == null) {
			return null;
		}
		
		Bitmap rotatedBitmap = null;
		
		try {
			Matrix matrix = new Matrix();
			matrix.postRotate(angle);
			rotatedBitmap = Bitmap.createBitmap(origBitmap, 0, 0,
					origBitmap.getWidth(), origBitmap.getHeight(), matrix, true);
			if (origBitmap != rotatedBitmap) {
				origBitmap.recycle();
			}
			
			System.gc();
		} catch(OutOfMemoryError e) {
			Log.e("BitmapFunc.rotateBitmap", e.getMessage());
		}
		
		return rotatedBitmap;
	}
	
	public void loadBitmap(long origId, String path, ImageView imageView, int max, 
			ExplorerPhotoType type, ILoadBitmapCallback callback) {
		if(path == null || path.length() == 0) {
			Log.e("loadBitmap", "error : path is empty");
		}
		if(imageView == null) {
			Log.e("loadBitmap", "error : imageView is null");
		}
		if(callback == null) {
			Log.e("loadBitmap", "error : callback is null");
		}
		
		try{
			
			String key = path + "_" + type.ordinal();
			
			// 尝试在内存中拿图片
			Bitmap bitmap = null;
			bitmap = BitmapMemoryLruCache.getInstance().getBitmapFromMemCache(key);
			imageView.setContentDescription(path);
			
			if(bitmap != null) {
				callback.dealBitmap(imageView, bitmap);
//				Log.e("TEST", "Get bitmap from Cache" + key);
			} else {
//				Log.e("TEST", "Get bitmap from decoding" + key);
				BitmapLoadRunnable  command = new BitmapLoadRunnable(path, imageView, max, type, callback);
				BitmapExecutorService.getInstance().execute(command);
			}
		} catch(Exception e) {
			
		}
	}
	
	public Bitmap cropBitmapFix(Bitmap bitmap, int width, int height) {
        Bitmap newbmp = null;
        
        if (bitmap == null) {
            Log.e("BitmapFunc.cropBitmapFix", "bitmap is null");
            return null;
        }
        if (width <= 0) {
            Log.e("BitmapFunc.cropBitmapFix", "width:" + width);
            return null;
        }
        if (height <= 0) {
            Log.e("BitmapFunc.cropBitmapFix", "height:" + height);
            return null;
        }
        
        try {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            
            if(w >= width && h >= height) {
                int x = (w - width) / 2;
                int y = (h - height) / 2;
                newbmp = Bitmap.createBitmap(bitmap, x, y, width, height);
                
            } else if(w >= width || h >= height) {
                if(w >= h) {
                    int x = (w - width) / 2;
                    newbmp = Bitmap.createBitmap(bitmap, x, 0, width, h);
                } else {
                    int y = (h - height) / 2;
                    newbmp = Bitmap.createBitmap(bitmap, 0, y, w, height);
                }
            } else {
           return bitmap;
        
            }
            
        } catch (OutOfMemoryError e) {
        	Log.e("BitmapFunc.cropBitmapFix", e.toString());    	
        	return bitmap;
		}
//        android 4.1.1 或者还有别的小米，主动回收图片存在有问题 ，不回收反而没事。。。？？？，让系统自己回收吧，不过度优化
//        bitmap.recycle();
//        System.gc();
        if(bitmap!=newbmp){
           	bitmap.recycle();
        }
        return newbmp;
        
    }
	
}
