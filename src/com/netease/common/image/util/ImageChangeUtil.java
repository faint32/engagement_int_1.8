package com.netease.common.image.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * 图像变换的工具类
 * 
 * @author dingding
 *
 */
public class ImageChangeUtil {

	/**
	 * 从时钟正上顺时旋转角度
	 * 
	 * @param bitmap
	 * @param degree
	 * @return
	 */
	public static Bitmap rotateBitmap(Bitmap bitmap, int degree, boolean recycle) {
		Bitmap ret = bitmap;
		
		if (bitmap != null) {
			Matrix matrix = new Matrix();
	        
	        //旋转图片
	        matrix.postRotate(degree);
	        
	        try {
				ret = Bitmap.createBitmap(bitmap, 
						0, 0, bitmap.getWidth(), bitmap.getHeight(), 
						matrix, false);
			} catch (OutOfMemoryError e) {
			}
		}
		
		if (recycle && ret != bitmap) {
			bitmap.recycle();
		}
		
		return ret;
	}
	
	/**
	 * 按照一定比例拉伸或者缩放图片
	 * 
	 * @param bitmap
	 * @param scale
	 * @return
	 */
	public static Bitmap scaleBitmap(Bitmap bitmap, float scale, boolean recycle) {
		Bitmap ret = bitmap;
		
		if (bitmap != null) {
			Matrix matrix = new Matrix();
	        
	        //旋转图片
	        matrix.postScale(scale, scale);
	        
	        try {
				ret = Bitmap.createBitmap(bitmap, 
						0, 0, bitmap.getWidth(), bitmap.getHeight(), 
						matrix, false);
			} catch (OutOfMemoryError e) {
			}
		}
		
		if (recycle && ret != bitmap) {
			bitmap.recycle();
		}
		
		return ret;
	}
	
	/**
	 * 拉伸或者缩放图片
	 * 
	 * @param bitmap
	 * @param width
	 * @param height
	 * @return
	 */
	public static Bitmap scaleBitmap(Bitmap bitmap, int width, int height, boolean recycle) {
		Bitmap ret = bitmap;
		
		if (bitmap != null) {
			if (width > 0) {
				if (height > 0) {
					// x轴中间裁剪，y轴上3分之一裁剪
					int w = bitmap.getWidth();
					int h = bitmap.getHeight();
					
					if (width != w || height != h) {
						// create the matrix to scale it
						Matrix matrix = new Matrix();
						
						float tmp_w = ((float) w) / width;
						float tmp_h = ((float) h) / height;
						float tmp = tmp_w < tmp_h ? tmp_w : tmp_h;
						
						int clipWidth = (int) (width * tmp);
						int clipHeight = (int) (height * tmp);
						
						clipWidth = clipWidth > w ? w : clipWidth;
						clipHeight = clipHeight > h ? h : clipHeight;
						
						matrix.setScale(1 / tmp, 1 / tmp);
						
						int pading_x = (w - clipWidth) >> 1;
						int pading_y = h / 3 - clipHeight / 2;
						pading_y = pading_y < 0 ? 0 : pading_y;
						
						try {
							ret = Bitmap.createBitmap(bitmap, pading_x, pading_y,
									clipWidth, clipHeight, matrix, true);
						} catch (java.lang.OutOfMemoryError e) {
						}
					}
					
					
				} else {
					int w = bitmap.getWidth();
					if (w != width) {
						ret = scaleBitmap(bitmap, 1.0f * width / w, false);
					}
				}
			}
			else {
				if (height > 0) {
					int h = bitmap.getHeight();
					if (h != height) {
						ret = scaleBitmap(bitmap, 1.0f * height / h, false);
					}
				}
				else {
					
				}
			}
		}
		
		if (recycle && ret != bitmap) {
			bitmap.recycle();
		}
		
		return ret;
	}
	
	
	
}
