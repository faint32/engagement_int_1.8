package com.netease.service.app;

import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.netease.engagement.app.EgmConstants;

public abstract class BaseApplication extends Application {
	private static BaseApplication sBaseApp;
	static public BaseApplication getAppInstance(){
		if(sBaseApp == null)
			throw new NullPointerException("sBaseApp not create or be terminated!");
		return sBaseApp;
	}
	
	@Override
    public void onCreate() {
        super.onCreate();
        sBaseApp = this;
        if(EgmConstants.init_crashlytics){
	        try {
	            Crashlytics.start(this);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
//	        BaseException.init(this);
        }
    }
	
	public static class BaseException implements UncaughtExceptionHandler {
        private BaseApplication mBaseApplication;
        public BaseException(BaseApplication app) {
        	mBaseApplication = app;
        }

        @Override
        public void uncaughtException(Thread thread, Throwable exception) {
        	mBaseApplication.onExceptionExit(exception);
        }

        public static void init(BaseApplication app) {
            Thread.setDefaultUncaughtExceptionHandler(new BaseException(app));
        }
    }
	/**
     * 当程序异常退出时调用
     * s
     * @param e
     */
    public abstract void onExceptionExit(Throwable exception) ;
}
