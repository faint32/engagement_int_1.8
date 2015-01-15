package com.netease.framework.skin;

import com.netease.common.log.NTLog;
import com.netease.service.Utils.StackTraceUtil;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;

public class InternalSkinResources implements IResources {
	public static final String TAG = InternalSkinResources.class.getSimpleName();
	
	private Context mContext;
	public Resources mRes;

	public InternalSkinResources(Context ctx) throws IllegalArgumentException {
		if (null == ctx) {
			throw new IllegalArgumentException("The parameter can not be null");
		}
		
		mContext = ctx.getApplicationContext();
		mRes = mContext.getResources();
	}

	@Override
	public Drawable getDrawable(int id) throws Resources.NotFoundException {
		int k = -1;
		k = getId(id);

		if (k <= 0) {
			return null;
		}
		
		return mRes.getDrawable(k);
	}

	@Override
	public int getColor(int id) throws Resources.NotFoundException {
		int k = -1;
		k = getId(id);

		if (k <= 0) {
			throw new Resources.NotFoundException("no resource found for: " + id);
		}

		return mRes.getColor(k);
	}
	
	@Override
	public ColorStateList getColorStateList(int id) throws NotFoundException {
		int k = -1;
		k = getId(id);
		
		if (k <= 0) {
			throw new Resources.NotFoundException("no resource found for: " + id);
		}
		
		return mRes.getColorStateList(k);
	}
	
	@Override
	public float getDimension(int id) throws NotFoundException {
		int k = -1;
		k = getId(id);
		
		if (k <= 0) {
			throw new Resources.NotFoundException("no resource found for: " + id);
		}
		
		return mRes.getDimension(k);
	}
	
	@Override
	public boolean getBoolean(int id) throws NotFoundException {
		int k = -1;
		k = getId(id);
		
		if (k <= 0) {
			throw new Resources.NotFoundException("no resource found for: " + id);
		}
		
		return mRes.getBoolean(k);
	}
	
	@Override
	public int getInteger(int id) throws NotFoundException {
		int k = -1;
		k = getId(id);
		
		if (k <= 0) {
			throw new Resources.NotFoundException("no resource found for: " + id);
		}
		
		return mRes.getInteger(k);
	}
	
	@Override
	public int getIdentifier(int id) {
		return getId(id);
	}

	protected String getResourceEntryName(String name) {
		String pName = name + SkinConfig.SUFFIX;
		return pName;
	}

	private int getId(int id) {
		String resTypeName = mRes.getResourceTypeName(id);
		String resEntryName = mRes.getResourceEntryName(id);

		String newName = getResourceEntryName(resEntryName);
		int newId = mRes.getIdentifier(newName, resTypeName, mContext.getPackageName());
	
		// 打印出夜间模式没有的资源
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
