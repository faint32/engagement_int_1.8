package com.netease.framework.skin;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

public class Skin {
	
	private boolean mIsInstalled;
	private boolean mIsUsing;
	private String mFilePath;
	private String mPackageName;
	private String mDisplayName;
	private String mVersion;
	private Drawable mPreviewPic;
	
	private IResources mRes;
	private Context mContext;
	public Resources mResources;

	public Skin(Context context) {
		mContext = context;
		mResources = context.getResources();
	}

	public Skin(String filePath, String packageName, String displayName,
			String version, Drawable previewPic, boolean isInstalled,
			boolean isUsing) {
		this.mFilePath = filePath;
		this.mPackageName = packageName;
		this.mDisplayName = displayName;
		this.mVersion = version;
		this.mPreviewPic = previewPic;
		this.mIsInstalled = isInstalled;
		this.mIsUsing = isUsing;
	}

	public String getFilePath() {
		return mFilePath;
	}

	public void setFilePath(String path) {
		this.mFilePath = path;
	}

	public String getPackageName() {
		return mPackageName;
	}

	public void setPackageName(String name) {
		this.mPackageName = name;
	}

	public String getDisplayName() {
		return mDisplayName;
	}

	public void setDisplayName(String name) {
		this.mDisplayName = name;
	}

	public String getVersion() {
		return mVersion;
	}

	public void setVersion(String version) {
		this.mVersion = version;
	}

	public Drawable getPreviewPic() {
		return mPreviewPic;
	}

	public void setPreviewPic(Drawable d) {
		this.mPreviewPic = d;
	}

	public boolean isInstalled() {
		return mIsInstalled;
	}

	public void setInstalled(boolean installed) {
		this.mIsInstalled = installed;
	}

	public boolean isUsing() {
		return mIsUsing;
	}

	public void setUsing(boolean using) {
		this.mIsUsing = using;
	}
	
	public IResources getResources() {
		if (null != mRes) {
			return mRes;
		}
		
		do {
			if (!TextUtils.isEmpty(mFilePath)) {
				try {
					ApkSkinResources res = new ApkSkinResources(mContext, mFilePath, mPackageName);
					mRes = res;
					mResources = res.mPkgRes;
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					mRes = null;
				}
				break;
			}
			
			if (mPackageName.equals(SkinConfig.SKIN_INTERNAL_BLACK)) {
				try {
					InternalSkinResources res = new InternalSkinResources(mContext);
					mRes = res;
					mResources = res.mRes;
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					mRes = null;
				}
				break;
			}
		} while (false);
		
		return mRes;
	}
	
	public void destroy() {
		mFilePath = null;
		mPackageName = null;
		mDisplayName = null;
		mVersion = null;
		mPreviewPic = null;
		mIsInstalled = false;
		mIsUsing = false;
	}
}