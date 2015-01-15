package com.netease.common.http.cache.db;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import com.netease.common.db.provider.BaseDBProvider;

public class HttpCacheDBProvider extends BaseDBProvider {

	public HttpCacheDBProvider() {
		super(HttpCacheDatabase.Authority, 
				HttpCacheDBTables.TableNames);
	}
	
	public HttpCacheDBProvider(Context context) {
		super(context, HttpCacheDatabase.Authority, 
				HttpCacheDBTables.TableNames);
	}
	
	@Override
	protected SQLiteOpenHelper getSQLiteOpenHelper() {
		return HttpCacheDBOpenHelper.getInstance(getSQLiteContext());
	}

}
