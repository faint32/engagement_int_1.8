package com.netease.engagement.dataMgr.cursorloader;

import com.netease.service.db.manager.LastMsgDBManager;

import android.content.Context;
import android.database.Cursor;

/**
 * 按照豪气值进行排序
 */
public class LastMsgRichLoader extends Loader{
	
	private boolean onlyNew ;
	
	public LastMsgRichLoader(Context context , 
			boolean onlyNew){
		this(context);
		this.onlyNew = onlyNew ;
	}

	public LastMsgRichLoader(Context context) {
		super(context);
	}

	@Override
	public Cursor getCursor() {
		Cursor cursor = LastMsgDBManager.getLastMsgListSortByRich(onlyNew,true);
		return cursor ;
	}
}
