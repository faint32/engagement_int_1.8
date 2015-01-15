package com.netease.framework.skin;

import com.netease.common.log.NTLog;
import com.netease.service.Utils.StackTraceUtil;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.ContextThemeWrapper;
import android.view.View;

public class SkinReflect {
	public static final String TAG = SkinReflect.class.getSimpleName();
	
	/**
	 * 获取皮肤包里面的资源
	 * @param ctx
	 * @param path
	 * @return
	 */
	public static Resources getResourcesFromApk(Context ctx, String path) {
		long start = System.currentTimeMillis();
		
		Resources resources = null;
		AssetManager am = null;
		
		try {
			am = AssetManager.class.newInstance();
			ReflectUtils.invokeMethod(AssetManager.class, am, "addAssetPath", new Class<?>[] {String.class}, new Object[]{path});
			resources = new Resources(am, ctx.getResources().getDisplayMetrics(), ctx.getResources().getConfiguration());
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		
		if (SkinConfig.DEBUG) {
			NTLog.d(TAG, StackTraceUtil.getMethodName() + " spend time=" + (System.currentTimeMillis() - start));
		}
		
		return resources;
	}
	
	/**
	 * 设置ContextImpl上资源
	 * @param ctx
	 * @param resources
	 */
	public static void setContextImplResources(Context ctx, Resources resources) {
		Object obj = null;
		
        try {
        	obj = ReflectUtils.fieldGet(ContextThemeWrapper.class, ctx, "mBase");
			ReflectUtils.fieldSet(Class.forName("android.app.ContextImpl"), obj, "mResources", resources);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * 设置ContextThemeWrapper上资源
	 * @param ctx
	 * @param resources
	 */
	public static void setContextThemeWrapperResources(Context ctx, Resources resources) {
		ReflectUtils.fieldSet(ContextThemeWrapper.class, ctx, "mResources", resources);
	}
	
	/**
	 * 设置actionBar背景
	 * @param v
	 * @param d
	 */
	public static void setActionBar(String className, View v, Drawable d) {
		Class<?> cls = null;
		
		try {
			cls = Class.forName(className);
			ReflectUtils.invokeMethod(cls, 
					v, 
					"setPrimaryBackground", 
					new Class<?>[] {Drawable.class}, 
					new Object[]{d});
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
