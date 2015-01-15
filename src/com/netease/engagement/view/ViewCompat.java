package com.netease.engagement.view;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;


/**
 * 适配不同的版本
 */
public class ViewCompat {
	
	public static void setBackground(View view, Drawable background) {
		if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
			SDK16.setBackground(view, background);
		} else {
			view.setBackgroundDrawable(background);
		}
	}
	
	@TargetApi(16)
	static class SDK16 {
		public static void setBackground(View view, Drawable background) {
			view.setBackground(background);
		}
	}
}
