package com.netease.util;

import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class ImageViewUtil {

	public static enum Scale {
		FIT_HEAD, // 填充，靠上，居中
	}
	
	public static void setScaleMetrix(ImageView view, Scale type) {
		if (view.getDrawable() == null) {
			return ;
		}
		
		Drawable d = view.getDrawable();
		
		final float viewWidth = getImageViewWidth(view);
        final float viewHeight = getImageViewHeight(view);
        final int drawableWidth = d.getIntrinsicWidth();
        final int drawableHeight = d.getIntrinsicHeight();
        
        final float widthScale = viewWidth / drawableWidth;
        final float heightScale = viewHeight / drawableHeight;
		
		Matrix matrix = new Matrix();
		
		final float scale = Math.max(widthScale, heightScale);
		
		switch (type) {
		case FIT_HEAD:
			if (widthScale != scale && scale > 0) {
				matrix.setTranslate(drawableWidth * (widthScale - scale) / 2 / scale, 0);
			}
			
			matrix.postScale(scale, scale);
			if (view.getScaleType() != ScaleType.MATRIX) {
				view.setScaleType(ScaleType.MATRIX);
			}
			view.setImageMatrix(matrix);
			break;
		}
	}
	
	private static int getImageViewWidth(ImageView imageView) {
        if (null == imageView)
            return 0;
        return imageView.getWidth() - imageView.getPaddingLeft() - imageView.getPaddingRight();
//        return imageView.getMeasuredWidth() - imageView.getPaddingLeft() - imageView.getPaddingRight();
    }

	private static int getImageViewHeight(ImageView imageView) {
        if (null == imageView)
            return 0;
        return imageView.getHeight() - imageView.getPaddingTop() - imageView.getPaddingBottom();
//        return imageView.getMeasuredHeight() - imageView.getPaddingTop() - imageView.getPaddingBottom();
    }
	
}
