package com.netease.common.db.provider;

import java.util.List;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

/**
 * 对单表简单处理进行统一查询、删除、更新操作
 * 
 * @author dingding
 *
 */
public abstract class BaseDBProvider extends ContentProvider {
	private static final boolean DEBUG = false;
	private static final String TAG = "BaseDBProvider";
	
	private static final String RAWQUERY = "rawquery";
	private static final int MATCH_RAW = 0;
	
	SQLiteOpenHelper mOpenHelper;
	UriMatcher sUriMatcher;
	String[] mTables;
	Context mContext;
	
	public BaseDBProvider(String authority, String[] tables) {
		 this (null, authority, tables);
	}
	
	public BaseDBProvider(Context context, String authority, String[] tables) {
		mContext = context;

		mTables = tables;

		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(authority, RAWQUERY, MATCH_RAW);

		if (tables != null) {
			int i = 0;
			for (String table : tables) {
				sUriMatcher.addURI(authority, table, ++i);
				sUriMatcher.addURI(authority, table + "/#", ++i);
			}
		}
	}
	
	protected abstract SQLiteOpenHelper getSQLiteOpenHelper();
	
	protected final Context getSQLiteContext() {
		Context context = mContext;
		if (context == null) {
			context = getContext();
		}
		
		return context;
	}
	
	@Override
	public String getType(Uri uri) {
		return null;
	}
	
	@Override
	public boolean onCreate() {
		try {
			getSQLiteOpenHelper().getWritableDatabase();
		} catch (SQLiteException e) {
			e.printStackTrace();
			if (null != mOpenHelper) {
				mOpenHelper.close();
				mOpenHelper = null;
			}
		}

		return true;
	}
	
	private boolean needBaseColumnsID(int match) {
		boolean ret = false;
		if (match > 0) {
			ret = (match % 2) == 0;
		}
		return ret;
	}
	
	private String getTableName(int match) {
		String tableName = null;
		if (match > 0) {
			tableName = mTables[(match - 1) >> 1];
		}
		return tableName;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		if (DEBUG) Log.d(TAG, "query: " + uri.toString());
		
		boolean bRaw = false;
		Cursor c = null;
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		int match = sUriMatcher.match(uri);
		if (match > 0) {
			if (needBaseColumnsID(match)) {
				selection = appendBaseColumnsID(uri, selection);
			}
			qb.setTables(getTableName(match));
		} else if (match == 0) {
			bRaw = true;
		} else {
			throw new UnsupportedOperationException("Cannot query that URI: "
					+ uri);
		}
		
		SQLiteDatabase db = getSQLiteOpenHelper().getReadableDatabase();
		if (bRaw) {
			c = db.rawQuery(selection, selectionArgs);
		} else {
			c = qb.query(db, projection, selection, selectionArgs, null, null,
					sortOrder);
		}

		c.setNotificationUri(getSQLiteContext().getContentResolver(), uri);

		return c;
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (DEBUG) Log.d(TAG, "insert: " + uri.toString());
		
		long rowId = -1;
		
		SQLiteDatabase db = getSQLiteOpenHelper().getWritableDatabase();
		int match = sUriMatcher.match(uri);
		if (match > 0) {
			rowId = db.insert(getTableName(match), null, values);
		}
		
		if (rowId > 0) {
			getSQLiteContext().getContentResolver().notifyChange(uri, null);

			return ContentUris.withAppendedId(uri, rowId);
		}
		else {
			return null;
		}
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		if (DEBUG) Log.d(TAG, "delete: " + uri.toString());
		
		int count = 0;
		SQLiteDatabase db = getSQLiteOpenHelper().getWritableDatabase();
		int match = sUriMatcher.match(uri);
		
		if (match > 0) {
			if (needBaseColumnsID(match)) {
				selection = appendBaseColumnsID(uri, selection);
			}
			count = db.delete(getTableName(match), selection, selectionArgs);
		}
		
		if (count > 0) {
			getSQLiteContext().getContentResolver().notifyChange(uri, null);
		}
		
		return count;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		if (DEBUG) Log.d(TAG, "update: " + uri.toString());
		
		int count = updateSilent(uri, values, selection, selectionArgs);
		
		if (count > 0) {
			getSQLiteContext().getContentResolver().notifyChange(uri, null);
		}
		
		return count;
	}
	
	public int updateSilent(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		if (DEBUG) Log.d(TAG, "update silent: " + uri.toString());
		
		int count = 0;
		SQLiteDatabase db = getSQLiteOpenHelper().getWritableDatabase();
		int match = sUriMatcher.match(uri);
		
		if (match > 0) {
			if (needBaseColumnsID(match)) {
				selection = appendBaseColumnsID(uri, selection);
			}
			count = db.update(getTableName(match), values, selection, selectionArgs);
		}
		
		return count;
	}
	
	
	
	/**
	 * 从uri取出最后一段id部分，追加到where进行查询等操作
	 * 
	 * @param uri
	 * @param where
	 * @return
	 */
	protected static String appendBaseColumnsID(Uri uri, String where) {
		if (!TextUtils.isEmpty(where)) {
			where = where + " AND ";
		} else {
			where = "";
		}
		
		List<String> seg = uri.getPathSegments();
		String append = BaseColumns._ID + " = "	+ seg.get(seg.size() - 1);
		where = where + append;
		
		return where;
	}
}
