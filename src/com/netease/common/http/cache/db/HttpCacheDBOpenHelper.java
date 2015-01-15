package com.netease.common.http.cache.db;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.netease.common.db.provider.BaseDBOpenHelper;
import com.netease.common.http.cache.db.HttpCacheDBTables.CacheTable;
import com.netease.util.KeyValuePair;

public class HttpCacheDBOpenHelper extends BaseDBOpenHelper {

	private static final int DB_VERSION = 1;
	
	private static HttpCacheDBOpenHelper instance = null;

	static public HttpCacheDBOpenHelper getInstance(Context context) {
		if(instance == null)
		{
			instance = new HttpCacheDBOpenHelper(context);
		}
		
		return instance;
	}
	
	public HttpCacheDBOpenHelper(Context context) {
		super(context, HttpCacheDatabase.DataBaseName, null, DB_VERSION);
	}

	@Override
	protected void onDestroyOldDB(SQLiteDatabase db) {
		destroyTables(db, HttpCacheDBTables.TableNames);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// cache table
		onCreateHttpCacheTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		destroyTables(db, HttpCacheDBTables.TableNames);
		onCreateHttpCacheTable(db);
	}

	private void onCreateHttpCacheTable(SQLiteDatabase db) {
		List<KeyValuePair> columns = new LinkedList<KeyValuePair>();
		columns.add(new KeyValuePair(BaseColumns._ID, INTEGER_KEY_AUTO_INC));
		columns.add(new KeyValuePair(CacheTable.C_URL, TEXT_NOT_NULL));
		columns.add(new KeyValuePair(CacheTable.C_LASTMODIFY, TEXT));
		columns.add(new KeyValuePair(CacheTable.C_CONTENT_LENGTH, LONG));
		columns.add(new KeyValuePair(CacheTable.C_CONTENT_TYPE, TEXT));
		columns.add(new KeyValuePair(CacheTable.C_ETAG, TEXT));
		columns.add(new KeyValuePair(CacheTable.C_EXPIRES_STR, TEXT));
		columns.add(new KeyValuePair(CacheTable.C_EXPIRES, LONG));
		columns.add(new KeyValuePair(CacheTable.C_LOCAL_PATH, TEXT));
		columns.add(new KeyValuePair(CacheTable.C_CONTENT_ENCODING, TEXT));
		columns.add(new KeyValuePair(CacheTable.C_CHARSET, TEXT));
		columns.add(new KeyValuePair(CacheTable.C_MIME, TEXT));
		columns.add(new KeyValuePair(CacheTable.C_TYPE, INT));
		columns.add(new KeyValuePair(CacheTable.C_DATA, TEXT));
		createTable(db, CacheTable.TABLE_NAME, columns, 
				"UNIQUE (" + CacheTable.C_URL + ") ON CONFLICT REPLACE");
	}
	
}
