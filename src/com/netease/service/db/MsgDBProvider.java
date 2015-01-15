package com.netease.service.db;

import java.util.List;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.netease.common.db.provider.BaseDBProvider;
import com.netease.service.app.BaseApplication;
import com.netease.service.db.MsgDBTables.LastMsgTable;



public class MsgDBProvider extends BaseDBProvider {
    public static final String AUTHORITY ="com.netease.date.msg";

    public MsgDBProvider() {
        super(BaseApplication.getAppInstance(),AUTHORITY, MsgDBTables.TableNames);
    }

    @Override
    public SQLiteOpenHelper getSQLiteOpenHelper() {
        return MsgDBOpenHelper.getInstance(getSQLiteContext());
    }
    
    public void bullInsert(List<ContentValues> list){
    	if(list == null || list.size() == 0){
    		return ;
    	}
    	SQLiteDatabase db = getSQLiteOpenHelper().getWritableDatabase();
    	
    	db.beginTransaction();
    	for(ContentValues value : list){
    		db.insert(LastMsgTable.TABLE_NAME, null, value);
    	}
    	db.setTransactionSuccessful();
    	db.endTransaction();
    }
}
