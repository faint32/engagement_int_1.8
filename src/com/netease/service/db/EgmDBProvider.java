package com.netease.service.db;

import android.database.sqlite.SQLiteOpenHelper;

import com.netease.common.db.provider.BaseDBProvider;



public class EgmDBProvider extends BaseDBProvider {
	
	public static final String AUTHORITY ="com.netease.date";

	public EgmDBProvider() {
		super(AUTHORITY, EgmDBTables.TableNames);
	}

	@Override
	protected SQLiteOpenHelper getSQLiteOpenHelper() {
		return EgmDBOpenHelper.getInstance(getContext());
	}

}
