package com.netease.android.activity;

import android.content.Context;

public class LofterApplication {

	private static Context mContext;
	
	public static Context getInstance() {
		return mContext;
	}
	
	public static void init(Context context) {
		mContext = context;
	}

}
