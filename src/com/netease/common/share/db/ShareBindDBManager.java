package com.netease.common.share.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.netease.common.service.BaseService;
import com.netease.common.share.ShareBind;
import com.netease.common.share.ShareType;

public class ShareBindDBManager implements ShareBindManager {

	@Override
	public Cursor getAllShareBind(String key) {
		return getWriteDatabase().query(ShareBindTable.TABLE_NAME,
				ShareBind.Projection, 
				ShareBindTable.C_SHARE_KEY + "=?", 
				new String[] {key}, 
				null, null, ShareBindTable.C_SHARE_TYPE + " asc");
	}
	
	@Override
	public Cursor getAllValidShareBind(String key) {
		return getWriteDatabase().query(ShareBindTable.TABLE_NAME,
				ShareBind.Projection, 
				ShareBindTable.C_SHARE_KEY + "=? AND " + ShareBindTable.C_STATE + ">=0", 
				new String[] {key}, 
				null, null, ShareBindTable.C_SHARE_TYPE + " asc");
	}

	@Override
	public void addShareBind(String key, ShareBind shareBind) {
		long id = 0;
		
		SQLiteDatabase db = getWriteDatabase();
		
		db.beginTransaction();
		
		try {
			ContentValues cv = getContentValues(shareBind);
			cv.put(ShareBindTable.C_SHARE_KEY, key);
			
			id = db.insert(ShareBindTable.TABLE_NAME, null, cv);
			
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		
		if (id != -1) {
			BaseService.getServiceContext().getContentResolver()
				.notifyChange(ShareBindTable.CONTENT_URI, null);
		}
	}

	@Override
	public void updateShareBind(String key, ShareBind shareBind) {
		int count = 0;
		
		SQLiteDatabase db = getWriteDatabase();
		
		db.beginTransaction();
		
		try {
			ContentValues cv = getContentValues(shareBind);
			
			count = db.update(ShareBindTable.TABLE_NAME, cv,
					ShareBindTable.C_SHARE_KEY + "=? AND " 
							+ ShareBindTable.C_SHARE_TYPE + "=" + shareBind.getShareType().value(), 
					new String[] {key});
			
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		
		if (count > 0) {
			BaseService.getServiceContext().getContentResolver()
				.notifyChange(ShareBindTable.CONTENT_URI, null);
		}
	}

	@Override
	public void removeShareBind(String key, ShareType shareType) {
		int count = 0;
		
		SQLiteDatabase db = getWriteDatabase();
		
		db.beginTransaction();
		
		try {
			count = db.delete(ShareBindTable.TABLE_NAME,
				ShareBindTable.C_SHARE_KEY + "=? AND " 
						+ ShareBindTable.C_SHARE_TYPE + "=" + shareType.value(), 
				new String[] {key});
			
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		
		if (count > 0) {
			BaseService.getServiceContext().getContentResolver()
				.notifyChange(ShareBindTable.CONTENT_URI, null);
		}
	}

	@Override
	public void removeShareBind(String key) {
		int count = 0;
		
		SQLiteDatabase db = getWriteDatabase();
		
		db.beginTransaction();
		
		try {
			count = db.delete(ShareBindTable.TABLE_NAME,
				ShareBindTable.C_SHARE_KEY + "=?", 
				new String[] {key});
			
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		
		if (count > 0) {
			BaseService.getServiceContext().getContentResolver()
				.notifyChange(ShareBindTable.CONTENT_URI, null);
		}
	}
	
	@Override
	public ShareBind getShareBind(String key, ShareType shareType) {
		ShareBind shareBind = null;
		
		Cursor cursor = getWriteDatabase().query(ShareBindTable.TABLE_NAME,
				ShareBind.Projection, 
				ShareBindTable.C_SHARE_KEY + "=? AND " + ShareBindTable.C_SHARE_TYPE + "=" + shareType.value(), 
				new String[] {key}, 
				null, null, null);
		
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				shareBind = new ShareBind(cursor, ShareBind.CURSOR_TYPE_DEFAULT);
			}
			
			cursor.close();
		}
		
		return shareBind;
	}

	@Override
	public void copyShareBind(String keySource, String keyTarget) {
		SQLiteDatabase database = getWriteDatabase();

		StringBuilder builder = new StringBuilder();
		builder.append("INSERT OR IGNORE INTO ")
				.append(ShareBindTable.TABLE_NAME).append('(')
				.append(ShareBindTable.C_SHARE_KEY).append(',')
				.append(ShareBindTable.C_SHARE_TYPE).append(',')
				.append(ShareBindTable.C_ACCESS_TOKEN).append(',')
				.append(ShareBindTable.C_REFRESH_TOKEN).append(',')
				.append(ShareBindTable.C_NAME).append(',')
				.append(ShareBindTable.C_DOMAIN).append(',')
				.append(ShareBindTable.C_PROFILE).append(',')
				.append(ShareBindTable.C_USERID).append(',')
				.append(ShareBindTable.C_JSON).append(',')
				.append(ShareBindTable.C_EXPIRES).append(',')
				.append(ShareBindTable.C_BIND_TIME).append(',')
				.append(ShareBindTable.C_JSON).append(',')
				.append(ShareBindTable.C_STATE).append(',')
				.append(ShareBindTable.C_DATA1).append(',')
				.append(ShareBindTable.C_DATA2).append(')').append(" SELECT ")
				.append("'").append(DatabaseUtils.sqlEscapeString(keyTarget)).append("',")
				.append(ShareBindTable.C_SHARE_TYPE).append(',')
				.append(ShareBindTable.C_ACCESS_TOKEN).append(',')
				.append(ShareBindTable.C_REFRESH_TOKEN).append(',')
				.append(ShareBindTable.C_NAME).append(',')
				.append(ShareBindTable.C_DOMAIN).append(',')
				.append(ShareBindTable.C_PROFILE).append(',')
				.append(ShareBindTable.C_USERID).append(',')
				.append(ShareBindTable.C_JSON).append(',')
				.append(ShareBindTable.C_EXPIRES).append(',')
				.append(ShareBindTable.C_JSON).append(',')
				.append(ShareBindTable.C_STATE).append(',')
				.append(ShareBindTable.C_DATA1).append(',').append(" FROM ")
				.append(ShareBindTable.TABLE_NAME).append(" WHERE ")
				.append(ShareBindTable.C_SHARE_KEY).append("='")
				.append(DatabaseUtils.sqlEscapeString(keySource)).append('\'');

		database.beginTransaction();
		try {
			database.execSQL(builder.toString());
			database.setTransactionSuccessful();
		} finally {
			database.endTransaction();
		}

		BaseService.getServiceContext().getContentResolver()
				.notifyChange(ShareBindTable.CONTENT_URI, null);
	}
	
	private static ContentValues getContentValues(ShareBind sharebind) {
		ContentValues cv = new ContentValues();
		cv.put(ShareBindTable.C_SHARE_TYPE, sharebind.getShareType().value());
		cv.put(ShareBindTable.C_ACCESS_TOKEN, sharebind.getAccessToken());
		cv.put(ShareBindTable.C_REFRESH_TOKEN, sharebind.getRefreshToken());
		cv.put(ShareBindTable.C_EXPIRES, sharebind.getExpires());
		cv.put(ShareBindTable.C_BIND_TIME, sharebind.getBindTime());
		cv.put(ShareBindTable.C_NAME, sharebind.getName());
		cv.put(ShareBindTable.C_PROFILE, sharebind.getProfile());
		cv.put(ShareBindTable.C_DOMAIN, sharebind.getDomainUrl());
		cv.put(ShareBindTable.C_USERID, sharebind.getUserID());
		cv.put(ShareBindTable.C_STATE, sharebind.getState());
//		cv.put(ShareBindTable.C_DATA1, sharebind.getUserID());
//		cv.put(ShareBindTable.C_DATA2, sharebind.getState());
		return cv;
	}

	private static SQLiteOpenHelper mShareBindSQLiteHelper;
	
	private static SQLiteDatabase getWriteDatabase() {
		SQLiteDatabase db = null;
		
		SQLiteOpenHelper helper = mShareBindSQLiteHelper;
		if (helper == null) {
			helper = new ShareBindSQLiteHelper(BaseService.getServiceContext());
			mShareBindSQLiteHelper = helper;
		}
		
		db = helper.getWritableDatabase();
		
		return db;
	}
}
