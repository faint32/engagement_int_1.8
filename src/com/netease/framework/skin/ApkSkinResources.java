package com.netease.framework.skin;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.netease.common.log.NTLog;
import com.netease.service.Utils.StackTraceUtil;

public class ApkSkinResources implements IResources {
	public static final String TAG = ApkSkinResources.class.getSimpleName();
	
	private Context mContext;
	// 本应用Resources
	private Resources mDefaultRes;
	// Apk文件包名
	private String mPkgNme;
	// Apk文件Resources
	public Resources mPkgRes;
	
	public ApkSkinResources(Context ctx, String path, String packageName) throws IllegalArgumentException{
		if (null == ctx || TextUtils.isEmpty(path) || TextUtils.isEmpty(packageName)) {
			throw new IllegalArgumentException("The parameter can not be null");
		}
		
		mContext = ctx.getApplicationContext();
		mDefaultRes = mContext.getResources();
		mPkgNme = packageName;
		mPkgRes = SkinReflect.getResourcesFromApk(mContext, path);
		if (null == mPkgRes) {
			throw new IllegalArgumentException("mPkgRes can not be null");
		}
	}

	@Override
	public Drawable getDrawable(int id) throws Resources.NotFoundException {
		long start = System.currentTimeMillis();
		
		int k = -1;
		k = getId(id);

		if (k <= 0) {
			return null;
		}
		
		Drawable d = mPkgRes.getDrawable(k);
		
		if (SkinConfig.DEBUG_TIME) {
			NTLog.d(TAG, StackTraceUtil.getMethodName() + " spend time=" + (System.currentTimeMillis() - start));
		}
		
		return d;
	}

	@Override
	public int getColor(int id) throws Resources.NotFoundException {
		long start = System.currentTimeMillis();
		
		int k = -1;
		k = getId(id);

		if (k <= 0) {
			throw new Resources.NotFoundException("no resource found for: " + id);
		}

		int color = mPkgRes.getColor(k);
		
		if (SkinConfig.DEBUG_TIME) {
			NTLog.d(TAG, StackTraceUtil.getMethodName() + " spend time=" + (System.currentTimeMillis() - start));
		}
		
		return color;
	}
	
	@Override
	public ColorStateList getColorStateList(int id) throws NotFoundException {
		long start = System.currentTimeMillis();
		
		int k = -1;
		k = getId(id);
		
		if (k <= 0) {
			throw new Resources.NotFoundException("no resource found for: " + id);
		}
		
		ColorStateList colorList = mPkgRes.getColorStateList(k);
		
		if (SkinConfig.DEBUG_TIME) {
			NTLog.d(TAG, StackTraceUtil.getMethodName() + " spend time=" + (System.currentTimeMillis() - start));
		}
		
		return colorList;
	}

	@Override
	public float getDimension(int id) throws NotFoundException {
		long start = System.currentTimeMillis();
		
		int k = -1;
		k = getId(id);
		
		if (k <= 0) {
			throw new Resources.NotFoundException("no resource found for: " + id);
		}
		
		float dim = mPkgRes.getDimension(k);
		
		if (SkinConfig.DEBUG_TIME) {
			NTLog.d(TAG, StackTraceUtil.getMethodName() + " spend time=" + (System.currentTimeMillis() - start));
		}
		
		return dim;
	}
	
	@Override
	public boolean getBoolean(int id) throws NotFoundException {
		long start = System.currentTimeMillis();
		
		int k = -1;
		k = getId(id);
		
		if (k <= 0) {
			throw new Resources.NotFoundException("no resource found for: " + id);
		}
		
		boolean b = mPkgRes.getBoolean(k);
		
		if (SkinConfig.DEBUG_TIME) {
			NTLog.d(TAG, StackTraceUtil.getMethodName() + " spend time=" + (System.currentTimeMillis() - start));
		}
		
		return b;
	}
	
	@Override
	public int getInteger(int id) throws NotFoundException {
		long start = System.currentTimeMillis();
		
		int k = -1;
		k = getId(id);
		
		if (k <= 0) {
			throw new Resources.NotFoundException("no resource found for: " + id);
		}
		
		int i = mPkgRes.getInteger(k);
		
		if (SkinConfig.DEBUG_TIME) {
			NTLog.d(TAG, StackTraceUtil.getMethodName() + " spend time=" + (System.currentTimeMillis() - start));
		}
		
		return i;
	}
	
	@Override
	public int getIdentifier(int id) {
		return getId(id);
	}
	
	private int getId(int id) {
		if (null == mPkgRes) {
			return -1;
		}
		
		String resTypeName = mDefaultRes.getResourceTypeName(id);
		String resEntryName = mDefaultRes.getResourceEntryName(id);
		int newId = mPkgRes.getIdentifier(resEntryName, resTypeName, mPkgNme);
		
		// 打印出皮肤包没有的资源
		if (newId <= 0) {
			if (SkinConfig.DEBUG) {
				NTLog.d(TAG, StackTraceUtil.getMethodName() + 
						" no such resource was found" +
						" TypeName=" + resTypeName +
						" EntryName=" + resEntryName);
			}
		}
		
		return newId;
	}
}
