package com.netease.engagement.dataMgr.cursorloader;

import com.netease.service.db.manager.LastMsgDBManager;

import android.content.Context;
import android.database.Cursor;

/**
 * 时间排序loader
 */
public class LastMsgTimeLoader extends Loader{

	public LastMsgTimeLoader(Context context) {
		super(context);
	}
	
	public LastMsgTimeLoader(Context context , boolean onlyNew){
		this(context);
		this.onlyNew = onlyNew ;
	}
	
	private boolean onlyNew ;

	@Override
	public Cursor getCursor() {
		Cursor cursor = LastMsgDBManager.getLastMsgList(onlyNew);
		return cursor;
	}
}
