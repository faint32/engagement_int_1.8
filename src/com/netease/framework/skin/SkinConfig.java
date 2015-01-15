package com.netease.framework.skin;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import com.netease.common.log.NTLog;
import com.netease.service.Utils.StackTraceUtil;
import com.netease.service.app.BaseApplication;
import com.netease.util.PreferenceUtils;

public class SkinConfig {
	public static final String TAG = SkinManager.class.getSimpleName();
	
	// 打印调试信息
	public static final boolean DEBUG = false;
	// 打印花费时间
	public static final boolean DEBUG_TIME = false;
	
	// 是否测试本地皮肤包
	public static final boolean TEST_ASSET = false;
	// 是否测试网络皮肤包
	public static final boolean TEST_URL = false;
	
	public static final String SKIN_URL = "http://nie.gdl.netease.com/kanyouxi_netease_1.1.2_20131114.apk";
	
	public static final String PREFS_NAME = "skin_prefs";
	public static final String KEY_SKIN = "skin";
	// 内置夜间模式的虚拟包名
	public static final String SKIN_INTERNAL_BLACK = "internal.skin.black";
	// 内置夜间模式资源后缀
	public static final String SUFFIX = "_black";
	// 皮肤包根路径
	public static final String DEST_ROOT = BaseApplication.getAppInstance().getCacheDir().getAbsolutePath();
	public static  final String BaseRootData = Environment.getDataDirectory() 
	        + "/data/" + "%s" + "/";
	public static  String DEST_BASE = BaseRootData + "skin/";
//	public static final String DEST_BASE = DEST_ROOT + File.separator;
	
	private static String [] sAssetPaths;
	
	// 配置预案装皮肤包
	static {
		sAssetPaths = new String [] {
				"skin.jpg"
		};
	}
	
	// 初始化皮肤拷贝地址
    public static void initSkinPath(String packageName) {
        DEST_BASE = String.format(DEST_BASE, packageName);
        File dir = new File(DEST_BASE);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }
	/**
	 * 从Asset拷贝皮肤包到sdcard上
	 */
	public static void unZipAssetSkin(Context context) {
		NTLog.d(TAG, StackTraceUtil.getMethodName());

		int len = sAssetPaths.length;
		for (int i = 0; i < len; i++) {
			ResourceFile.copyAssetsToDest(context, sAssetPaths[0], DEST_BASE + sAssetPaths[i]);
		}
	}
	
	public static void setSkinName(Context context, String name) {
		if (null == context) {
			return;
		}
		
	    SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
	    editor.putString(KEY_SKIN, name);
	    PreferenceUtils.apply(editor);
	}

	public static String getSkinName(Context context) {
		if (null == context) {
			return null;
		}
		
	    SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	    String packageName = null;
	    packageName = context.getPackageName();
	    if (TEST_ASSET) {
	    	packageName = DEST_BASE + sAssetPaths[0];
	    } else if (TEST_URL) {
	    	packageName = SKIN_URL;
	    }
	    return prefs.getString(KEY_SKIN, packageName);
	}
}
