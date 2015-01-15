package com.netease.common.share.db;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.netease.common.db.provider.BaseDBOpenHelper;
import com.netease.util.KeyValuePair;

public class ShareBindSQLiteHelper extends BaseDBOpenHelper {

	private static final String SHARE_DB_NAME = "db_" + ShareBindTable.TABLE_NAME + ".db";
	private static final int VERSION = 2;
	
	public ShareBindSQLiteHelper(Context context) {
		super(context, SHARE_DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		onCreateShareBindTable(db);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		destroyTables(db, new String[]{ShareBindTable.TABLE_NAME});
		onCreateShareBindTable(db);
	}
	
	private void onCreateShareBindTable(SQLiteDatabase db) {
		List<KeyValuePair> columns = new LinkedList<KeyValuePair>();
		columns.add(new KeyValuePair(BaseColumns._ID, INTEGER_KEY_AUTO_INC));
		columns.add(new KeyValuePair(ShareBindTable.C_SHARE_KEY, TEXT));
		columns.add(new KeyValuePair(ShareBindTable.C_SHARE_TYPE, INT));
		columns.add(new KeyValuePair(ShareBindTable.C_NAME, TEXT));
		columns.add(new KeyValuePair(ShareBindTable.C_USERID, TEXT));
		columns.add(new KeyValuePair(ShareBindTable.C_ACCESS_TOKEN, TEXT));
		columns.add(new KeyValuePair(ShareBindTable.C_REFRESH_TOKEN, TEXT));
		columns.add(new KeyValuePair(ShareBindTable.C_DOMAIN, TEXT));
		columns.add(new KeyValuePair(ShareBindTable.C_PROFILE, TEXT));
		columns.add(new KeyValuePair(ShareBindTable.C_EXPIRES, LONG));
		columns.add(new KeyValuePair(ShareBindTable.C_BIND_TIME, LONG));
		columns.add(new KeyValuePair(ShareBindTable.C_STATE, INT));
		columns.add(new KeyValuePair(ShareBindTable.C_JSON, TEXT));
		columns.add(new KeyValuePair(ShareBindTable.C_DATA1, TEXT));
		columns.add(new KeyValuePair(ShareBindTable.C_DATA2, TEXT));
		
		createTable(db, ShareBindTable.TABLE_NAME, columns, 
				"UNIQUE (" + ShareBindTable.C_SHARE_KEY + "," 
						+ ShareBindTable.C_SHARE_TYPE + ") ON CONFLICT REPLACE");
	}

}
