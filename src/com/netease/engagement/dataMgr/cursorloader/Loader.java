package com.netease.engagement.dataMgr.cursorloader;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Loader抽象类
 */
public abstract class Loader extends AsyncTaskLoader<Cursor>{

	public Loader(Context context) {
		super(context);
	}

	@Override
	public Cursor loadInBackground() {
		return getCursor();
	}
	
	public abstract Cursor getCursor();


	@Override
	protected void onStartLoading() {
		forceLoad();
	}
}
