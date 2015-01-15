package com.netease.framework.app;

import android.app.Application;
import android.os.Handler;
import android.os.Process;

import com.netease.common.log.NTLog;
import com.netease.framework.activity.FrameworkActivityManager;

/**
 * 
 * @author Panjf
 * @date   2011-10-9
 */
public class App extends Application {
	private final String TAG = "App";
	
	private static App sApp;
	
	static public App getAppInstance(){
		if(sApp == null)
			throw new NullPointerException("app not create or be terminated!");
		return sApp;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
//		Thread.setDefaultUncaughtExceptionHandler(new WbUncaughtExceptionHandler());
		 
		sApp = this;
		appIni();
		
		NTLog.i(TAG, "onCreate");
	}
	
	@Override
	public void onTerminate() {
		NTLog.i(TAG, "onTerminate");
		super.onTerminate();
	}
	
	private void appIni(){
		//ini log
		String packetName = getPackageName();
        NTLog.init(packetName);
	}
	
	public void finishApp(){
		FrameworkActivityManager.getInstance().FinishAllActivity();
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				KillApplication.killApplicationByPackageName(App.this, getPackageName());
			}
		}, 1000);

	}
	
	
	public class WbUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler{

		@Override
		public void uncaughtException(Thread thread, Throwable ex) {
			NTLog.i("WbUncaughtExceptionHandler", "WbUncaughtExceptionHandler");
			ex.printStackTrace();
			
			finishApp();
			
			Process.killProcess(Process.myPid());
		}
		
	}
}
