package com.netease.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

/**
 * 
 * 密码加密解密方案
 * 对称加密解密，通过jni获得的app name和key进行加解密运算
 * 
 * 需要以下权限
 * <uses-permission android:name="android.permission.READ_PHONE_STATE" />
 * 
 * @author dingding
 *
 */

public final class PDEEngine {
	
	static {
		System.loadLibrary("PDEEngine");
	}
	
	private static final String DEFAULT_PDEENGINE = "peapp_share_value";
	private static final String DEFAULT_PDEENGINE_KEY = "peapp_share_value_key";
	
	static public String getPhoneIMEI(Context context) {
		TelephonyManager mTelephonyMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = mTelephonyMgr.getDeviceId();
		return imei;
	}
	
	public static String PEncrypt(Context context, String data) {
		if (data == null || context == null) {
			return null;
		}
		
		SharedPreferences pref = context.getSharedPreferences(
				DEFAULT_PDEENGINE, Context.MODE_PRIVATE);
		String key = pref.getString(DEFAULT_PDEENGINE_KEY, null);
		if (key == null) {
			key = String.valueOf(System.currentTimeMillis()) + getPhoneIMEI(context);
			Editor editor = pref.edit();
			editor.putString(DEFAULT_PDEENGINE_KEY, key);
			editor.commit();
		}
		
		return new String(Base64.encode(PEncrypt(context, data.getBytes(), key)));
	}
	
	public static String PDecrypt(Context context, String data) {
		if (data == null || context == null) {
			return null;
		}
		
		SharedPreferences pref = context.getSharedPreferences(
				DEFAULT_PDEENGINE, Context.MODE_PRIVATE);
		String key = pref.getString(DEFAULT_PDEENGINE_KEY, null);
		if (key == null) {
			return null;
		}
		
		return new String(PEncrypt(context, Base64.decode(data), key));
	}
	
//	public static String PEncrypt(Context context, String data, String key) {
//		if (data == null) {
//			return null;
//		}
//		
//		return new String(Base64.encode(PEncrypt(context, data.getBytes(), key)));
//	}
//	
//	public static String PDecrypt(Context context, String data, String key) {
//		if (data == null) {
//			return null;
//		}
//		
//		return new String(PEncrypt(context, Base64.decode(data), key));
//	}
//	
	public static byte[] PEncrypt(Context context, byte[] data, String key) {
		if (data == null) {
			return data;
		}
		if (key == null) {
			key = "";
		}
		return encrypt(context, data, key);
	}
	
	public static byte[] PDecrypt(Context context, byte[] data, String key) {
		if (data == null) {
			return data;
		}
		if (key == null) {
			key = "";
		}
		
		return decrypt(context, data, key);
	}
	
	public static String PXDecrypt(String value) {
		if (TextUtils.isEmpty(value)) {
			return value;
		}
		
		try {
			value = new String(xdecrypt(EnctryUtil.stringToBytes(value)), "utf-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return value;
	}
	
	public static String PXEncrypt(String value) {
		if (TextUtils.isEmpty(value)) {
			return value;
		}
		
		try {
			value = EnctryUtil.bytesToHexString(xencrypt(value.getBytes("utf-8")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return value;
	}
	
	private static native byte[] encrypt(Object context, byte[] data, String key);
	
	private static native byte[] decrypt(Object context, byte[] data, String key);
	
	private static native byte[] xencrypt(byte[] data);
	
	private static native byte[] xdecrypt(byte[] data);
}
