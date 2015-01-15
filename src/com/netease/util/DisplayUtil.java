package com.netease.util;

import android.content.Context;
import android.util.DisplayMetrics;

public class DisplayUtil {
	private static int mScreenWidth = -1;
	private static int mScreenHeight = -1;
	/**a
	 * 
	 * @param context
	 * @return
	 */
	public static int getDisplayWidth(Context context) {
		if (mScreenWidth == -1) {
			DisplayMetrics metrics = context.getResources().getDisplayMetrics();
			mScreenWidth = metrics.widthPixels;
		}
		
		return mScreenWidth;
	}
	
	public static int getDisplayHeight(Context context) {
		if (mScreenHeight == -1) {
			DisplayMetrics metrics = context.getResources().getDisplayMetrics();
			mScreenHeight = metrics.heightPixels;
		}

		return mScreenHeight;
	}
}
