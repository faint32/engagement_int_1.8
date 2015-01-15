package com.netease.android.util;

import android.content.Context;

import com.netease.android.activity.LofterApplication;

public class DpAndPxUtils {

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(float dpValue) {
		Context context = LofterApplication.getInstance();
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(float pxValue) {
		Context context = LofterApplication.getInstance();
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static int getScreenHeightPixels() {
		Context context = LofterApplication.getInstance();
		return context.getResources().getDisplayMetrics().heightPixels;
	}

	public static int getScreenWidthPixels() {
		Context context = LofterApplication.getInstance();
		return context.getResources().getDisplayMetrics().widthPixels;
	}
}
