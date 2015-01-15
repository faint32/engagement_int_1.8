package com.netease.engagement.dataMgr.cursorloader;

import com.netease.service.db.manager.LastMsgDBManager;

import android.content.Context;
import android.database.Cursor;

public class LastMsgInitLoader extends Loader{

	private boolean onlyNew ;
	
	public LastMsgInitLoader(Context context , 
			boolean onlyNew ){
		this(context);
		this.onlyNew = onlyNew ;
	}
	
	public LastMsgInitLoader(Context context) {
		super(context);
	}

	@Override
	public Cursor getCursor() {
		Cursor cursor = LastMsgDBManager.getLastMsgListSortByRich(onlyNew, false);
		return cursor ;
	}
}
