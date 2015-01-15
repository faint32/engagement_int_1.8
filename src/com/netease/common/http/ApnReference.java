package com.netease.common.http;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.net.Uri;
import android.os.Handler;

import com.netease.common.log.NTLog;
import com.netease.util.VersionUtils;

public class ApnReference {
	public static final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");
	
	private static ApnReference sInstance;
	
	private Cursor mCursor;
	private Context mContext;
	private ApnWrapper mApnWrapper;
	ConnectivityManager mCm;
	
	private ChangeObserver mChangeObserver = new ChangeObserver();

	private int mSDKVersion;
	
	synchronized public static ApnReference getInstance(Context context){
		if(sInstance == null)
			sInstance = new ApnReference(context);
		
		return sInstance;
	}
	
	private ApnReference(Context context){
		mContext = context;
		mSDKVersion = VersionUtils.getSDKVersionNumber();
		mCm =(ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
	}
	
	synchronized public void ini(){
		if(mSDKVersion >= VersionUtils.Android_SDK_4_2){
			return;
		}
		
		String[] projection = new String[]{"apn", "name", "port", "proxy"};
		
		unIni();
		
		mCursor = mContext.getContentResolver().query(PREFERRED_APN_URI, projection, null, null, null);
		if(mCursor != null){
			mCursor.registerContentObserver(mChangeObserver);
//			mCursor.registerDataSetObserver(observer)
		}
	}
	
	synchronized public void unIni(){
		if(mCursor != null){
			mCursor.unregisterContentObserver(mChangeObserver);
			mCursor.close();
			mCursor = null;
			mApnWrapper = null;
		}
	}
	
	
	synchronized public ApnWrapper getCurrApn(){
		if(mSDKVersion >= VersionUtils.Android_SDK_4_2){
			return null;
		}
		
		NetworkInfo nInfo = mCm.getActiveNetworkInfo();
		if(nInfo == null || nInfo.getType() != ConnectivityManager.TYPE_MOBILE)
			return null;
			
		if(mApnWrapper != null)
			return mApnWrapper;
		
		if(mCursor == null){
			ini();
		}
		
		if(mCursor!= null && mCursor.moveToFirst()){
			ApnWrapper aw = new ApnWrapper();
			aw.apn = mCursor.getString(mCursor.getColumnIndex("apn"));
			aw.name = mCursor.getString(mCursor.getColumnIndex("name"));
			String port = mCursor.getString(mCursor.getColumnIndex("port"));
			try {
				aw.port = Integer.parseInt(port);
			} catch (NumberFormatException e) {
				aw.port = Proxy.getDefaultPort();
				//e.printStackTrace();
			}
			
			aw.proxy = mCursor.getString(mCursor.getColumnIndex("proxy"));
			NTLog.i("ApnReference", "apn:" + aw.apn + "-name:" + aw.name + "-port:" + aw.port + "-proxy:" + aw.proxy);
			mApnWrapper = aw;
			return aw;
		}
		
		return null;
	}
	
	/*
	public ApnWrapper getCurrApn_level_17() {
		Class clazz = null;
		
		try {
			clazz = Class.forName("android.provider.Settings");
			Class[] clazzs = clazz.getClasses();
			for(Class c : clazzs){
				if(c.getName().endsWith("Global")){
					Method method = c.getMethod("getString", ContentResolver.class,
							String.class);
					if (method != null) {
						String proxy = (String) method.invoke(null,
								mContext.getContentResolver(), "http_proxy");
						Log.i("ApnReference", proxy);
					}
					
					return null;
				}
			}
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	*/
	
	private class ChangeObserver extends ContentObserver {
        public ChangeObserver() {
            super(new Handler());
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
        	/*?mCursor.requery()  why???*/
//            if(mCursor != null && !mCursor.isClosed()){
//            	PalLog.i("ApnReference", "NET work changed");
//            	mCursor.requery();
//            	mApnWrapper = null;
//            }
        	
        	ini();
        }
    }
	
	
	public class ApnWrapper {
		public String apn;
		public String name;
		public int port;
		public String proxy;
	}
}
