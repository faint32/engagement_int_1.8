package com.netease.framework.skin;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;

import com.netease.common.log.NTLog;
import com.netease.service.Utils.StackTraceUtil;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.URLUtil;


public class SkinManager implements IResources {
	public static final String TAG = SkinManager.class.getSimpleName();
	
	private static String mSkinName;
	private static SkinManager mSkinManager = null;

	synchronized public static SkinManager getInstance(Context context) {
		if (null != mSkinManager) {
			return mSkinManager;
		}
		initialize(context);
		return mSkinManager;
	}

	private static void initialize(Context context) {
		mSkinManager = null;
		if (null != context) {
			mSkinManager = new SkinManager(context.getApplicationContext());
		}
	}

	// 是否在皮肤设置项改变时通知已经生成的view进行皮肤切换
    private boolean mNeedSkinChange = true;
	private String mPkgName;
	private Context mContext = null;
	private Skin mSkin;
	private PackageManager mPkgMgr;
	private ViewManager mViewManager;
	private WeakHashMap<IViewFilter, Void> mFilters;

	private SkinManager(Context context) {
		mContext = context;
		mPkgName = mContext.getPackageName();
		mPkgMgr = mContext.getPackageManager();
		mViewManager = new ViewManager(mContext, null, mContext.getResources());
		mFilters = new WeakHashMap<IViewFilter, Void>();
		initTest();
		initSkin();
	}

	private void initTest() {
		if (SkinConfig.TEST_ASSET) {
			SkinConfig.unZipAssetSkin(mContext);
		} else if (SkinConfig.TEST_URL) {
			downloadSkin(SkinConfig.SKIN_URL, null);
		}
	}
	
	/**
	 * 初始化皮肤
	 * 顺序：1 如果没有设置则使用应用默认皮肤
	 *      2 如果是夜间模式则使用夜间皮肤
	 *      3 使用apk文件皮肤，如果加载失败就使用默认皮肤。
	 */
	public void initSkin() {
		if (SkinConfig.DEBUG) {
			NTLog.d(TAG, StackTraceUtil.getMethodName());
		}
		Skin skin = null;
		
		do {
			
			mSkinName = getSkinName(mContext);
			if (TextUtils.isEmpty(mSkinName) || mPkgName.equals(mSkinName)) {
				break;
			}
			
			if (mSkinName.equals(SkinConfig.SKIN_INTERNAL_BLACK)) {
				skin = getInternalBlackSkin();
				break;
			}
			
			skin = getApkSkinForPath(mSkinName);
		} while (false);

		setSkin(skin);
	}
	
	private Skin getDefaultSkin() {
		PackageInfo pkgInfo = null;
		try {
			pkgInfo = mPkgMgr.getPackageInfo(mContext.getPackageName(), 0);
		} catch (NameNotFoundException e1) {
			e1.printStackTrace();
		}
		
		return getSkinInfo(null, mPkgMgr, pkgInfo, false);
	}
	
	private Skin getInternalBlackSkin() {
		PackageInfo pkgInfo = null;
		try {
			pkgInfo = mPkgMgr.getPackageInfo(mContext.getPackageName(), 0);
		} catch (NameNotFoundException e1) {
			e1.printStackTrace();
		}
		
		return getSkinInfo(null, mPkgMgr, pkgInfo, true);
	}

	public void setSkin(Skin skin) {
		mSkin = (null == skin) ? getDefaultSkin() : skin;
		if (null != mSkin && null != mViewManager) {
			mViewManager.setSkinRes(mSkin.getResources());
		}
	}

	public Skin getSkin() {
		if (null == mSkin) {
			mSkin = getDefaultSkin();
		}
		return mSkin;
	}

	public boolean isDefaultSkin() {
		boolean res = false;
		do {
			if (SkinConfig.TEST_URL) {
				break;
			}
			
			if (null == mSkin) {
				mSkin = getDefaultSkin();
				res =  true;
				break;
			}
			
			res = mPkgName.equals(mSkin.getPackageName());
		} while(false);
		
		return res;
	}

	private Skin getSkinInfo(String filePath, PackageManager pm, PackageInfo pkgInfo, boolean isBlack) {
		if (null == pm || null == pkgInfo) {
			return null;
		}
		Skin skin = new Skin(mContext);
		skin.setFilePath(filePath);
		ApplicationInfo appInfo = pkgInfo.applicationInfo;

		String pkgName = appInfo.packageName;
		if (isBlack) {
			skin.setPackageName(SkinConfig.SKIN_INTERNAL_BLACK);
		} else {
			skin.setPackageName(pkgName);
		}
		
		if (mPkgName.equals(pkgName)) {
			skin.setDisplayName(null);
			skin.setPreviewPic(null);
		} else {
			// Issue 9151,必须加入下面这两句赋值才能从未安装的apk文件中读取label和icon
			appInfo.sourceDir = filePath;
			appInfo.publicSourceDir = filePath;
			skin.setDisplayName(pm.getApplicationLabel(appInfo).toString());
			skin.setPreviewPic(pm.getApplicationIcon(appInfo));
		}

		skin.setVersion(pkgInfo.versionName);
		skin.setInstalled(true);
		skin.setUsing(true);

		return skin;
	}
	
	private Skin getApkSkinForPath(String path) {
		if (TextUtils.isEmpty(path)) {
			return null;
		}

		// 进行路径判断，如果是网络地址要转换成本地路径
		if (URLUtil.isNetworkUrl(path)) {
			IPathConvert convert = SkinDownLoadManager.getInstance().getIPathConvert();
			if (null != convert) {
				path = convert.convert(path);
			}
		}
		
		PackageInfo pkgInfo = null;
		pkgInfo = mPkgMgr.getPackageArchiveInfo(path, 0);
		
		return getSkinInfo(path, mPkgMgr, pkgInfo, false);
	}
	
	public void addView(String name, View v, AttributeSet attrs) {
		if (SkinConfig.DEBUG) {
			NTLog.d(TAG, StackTraceUtil.getMethodName() + " name=" + name + " v=" + v);
		}
		if (isDefaultSkin() || null == v || null == attrs) {
			return;
		}
		
		/**
		 * 对加入的view进行过滤，如果过滤返回true，直接返回.
		 */
		synchronized (mFilters) {
			Set<Map.Entry<IViewFilter,Void>> set = mFilters.entrySet();
		Iterator<Entry<IViewFilter, Void>> iter = set.iterator();
			Map.Entry<IViewFilter, Void> en = null;
			IViewFilter key;
			while (iter.hasNext()) {
				en = iter.next();
				key = en.getKey();
				if (null != key && key.onFilter(name, v, attrs)) {
					if (SkinConfig.DEBUG) {
						NTLog.d(TAG, StackTraceUtil.getMethodName() + " onFilter is true");
					}
					return;
				}
			}
		}
		
		/**
		 * 是否在皮肤设置项改变时通知已经生成的view进行皮肤切换，如果false，直接返回。
		 */
		if (!mNeedSkinChange) {
			if (SkinConfig.DEBUG) {
				NTLog.d(TAG, StackTraceUtil.getMethodName() + "mNeedSkinChange=" + mNeedSkinChange);
			}
			return;
		}
		
		if (null != mViewManager) {
			ViewSkinResource resMap = mViewManager.addView(v, attrs);
			mViewManager.setViewSkin(v, resMap);
		}
	}

	public void setNeedSkinChange(boolean needSkinChange) {
		mNeedSkinChange = needSkinChange;
	}
	
	public void onSkinChange() {
		if (SkinConfig.DEBUG) {
			NTLog.d(TAG, StackTraceUtil.getMethodName());
		}
		if (null != mViewManager) {
			mViewManager.onSkinChange();
		}
	}

	/**
	 * 设置皮肤包名字,皮肤包名字可以是url地址,如果皮肤包不同立即换肤
	 * @param context
	 * @param name
	 */
	public static void setSkinName(Context context, String name) {
		if (SkinConfig.DEBUG) {
			NTLog.d(TAG, StackTraceUtil.getMethodName());
		}
		if (!TextUtils.isEmpty(mSkinName) && !mSkinName.equals(name)) {
			SkinConfig.setSkinName(context, name);
			getInstance(context).initSkin();
			getInstance(context).onSkinChange();
		}
	}

	/**
	 * 获取皮肤包名字
	 * @param context
	 * @return
	 */
	public static String getSkinName(Context context) {
		if (SkinConfig.DEBUG) {
			NTLog.d(TAG, StackTraceUtil.getMethodName());
		}
		return SkinConfig.getSkinName(context);
	}
	
	/**
	 * 注册过滤器
	 * @param filter
	 */
	synchronized public void registerFilter(IViewFilter filter) {
		if (SkinConfig.DEBUG) {
			NTLog.d(TAG, StackTraceUtil.getMethodName());
		}
		if (null != filter) {
			if (null != mFilters) {
				mFilters.put(filter, null);
			}
		}
	}
	
	/**
	 * 卸载过滤器
	 * @param filter
	 */
	synchronized public void unregisterFilter(IViewFilter filter) {
		if (SkinConfig.DEBUG) {
			NTLog.d(TAG, StackTraceUtil.getMethodName());
		}
		if (null != filter) {
			if (null != mFilters) {
				mFilters.remove(filter);
			}
		}
	}
	
	/**
	 * 注册下载监听
	 * @param listener
	 */
	synchronized public void registerProcessListener(IProcessListener listener) {
		if (SkinConfig.DEBUG) {
			NTLog.d(TAG, StackTraceUtil.getMethodName());
		}
		SkinDownLoadManager.getInstance().registerProcessListener(listener);
	}
	
	/**
	 * 卸载下载监听
	 * @param listener
	 */
	synchronized public void unregisterProcessListener(IProcessListener listener) {
		if (SkinConfig.DEBUG) {
			NTLog.d(TAG, StackTraceUtil.getMethodName());
		}
		SkinDownLoadManager.getInstance().unregisterProcessListener(listener);
	}
	
	/**
	 * 下载皮肤插件
	 * @param url
	 * @param tag
	 */
	public static int downloadSkin(String url, Object tag) {
		if (SkinConfig.DEBUG) {
			NTLog.d(TAG, StackTraceUtil.getMethodName());
		}
		return SkinDownLoadManager.getInstance().execute(url, tag);
	}
	
	/**
	 * 获取资源
	 * @return
	 */
	public Resources getResources() {
		Resources res = null;
		if (null != mSkin) {
			res = mSkin.mResources;
		}
		
		if (null == res) {
			res = mContext.getResources();
		}
		
		return res;
	}
	
	/**
	 * 释放资源
	 */
	public void destroy() {
		if (SkinConfig.DEBUG) {
			NTLog.d(TAG, StackTraceUtil.getMethodName());
		}
		mContext = null;
		mPkgName = null;
		mPkgMgr = null;
		if (null != mSkin) {
			mSkin.destroy();
		}
		mSkin = null;
		
		if (null != mViewManager) {
			mViewManager.destroy();
		}
		mViewManager = null;
		
		if (null != mFilters) {
			mFilters.clear();
		}
		mFilters = null;
		
		SkinDownLoadManager.getInstance().destroy();
		
		mSkinManager = null;
	}

	/**
	 * 根据资源id获取Drawable
	 */
	@Override
	public Drawable getDrawable(int id) throws NotFoundException {
		if (SkinConfig.DEBUG) {
			NTLog.d(TAG, StackTraceUtil.getMethodName());
		}
		if (null != mViewManager) {
			return mViewManager.getDrawable(id);
		}
		
		return null;
	}

	/**
	 * 根据资源id获取颜色值
	 */
	@Override
	public int getColor(int id) throws NotFoundException {
		if (SkinConfig.DEBUG) {
			NTLog.d(TAG, StackTraceUtil.getMethodName());
		}
		if (null != mViewManager) {
			return mViewManager.getColor(id);
		}
		return 0;
	}

	/**
	 * 根据资源id获取ColorStateList
	 */
	@Override
	public ColorStateList getColorStateList(int id) throws NotFoundException {
		if (SkinConfig.DEBUG) {
			NTLog.d(TAG, StackTraceUtil.getMethodName());
		}
		if (null != mViewManager) {
			return mViewManager.getColorStateList(id);
		}
		return null;
	}

	/**
	 * 根据资源id获取尺寸
	 */
	@Override
	public float getDimension(int id) throws NotFoundException {
		if (SkinConfig.DEBUG) {
			NTLog.d(TAG, StackTraceUtil.getMethodName());
		}
		if (null != mViewManager) {
			return mViewManager.getDimension(id);
		}
		return 0;
	}

	@Override
	public boolean getBoolean(int id) throws NotFoundException {
		if (SkinConfig.DEBUG) {
			NTLog.d(TAG, StackTraceUtil.getMethodName());
		}
		if (null != mViewManager) {
			return mViewManager.getBoolean(id);
		}
		return false;
	}
	
	@Override
	public int getInteger(int id) throws NotFoundException {
		if (SkinConfig.DEBUG) {
			NTLog.d(TAG, StackTraceUtil.getMethodName());
		}
		if (null != mViewManager) {
			return mViewManager.getInteger(id);
		}
		return 0;
	}
	
	/**
	 * 根据资源id获取映射后的id
	 */
	@Override
	public int getIdentifier(int id) {
		if (SkinConfig.DEBUG) {
			NTLog.d(TAG, StackTraceUtil.getMethodName());
		}
		if (null != mViewManager) {
			return mViewManager.getIdentifier(id);
		}
		return 0;
	}
}
