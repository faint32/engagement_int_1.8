package com.netease.engagement.dataMgr.cursorloader;

import com.netease.service.db.manager.MsgDBManager;

import android.content.Context;
import android.database.Cursor;

public class MsgListLoaderPage extends Loader{

	public MsgListLoaderPage(Context context) {
		super(context);
	}
	
	public MsgListLoaderPage(Context context ,int pageNo ,long receiveId ,long senderId){
		this(context);
		this.pageNo = pageNo ;
		this.receiveId = receiveId ;
		this.senderId = senderId ;
	}
	
	private int pageNo ;
	private long receiveId ;
	private long senderId ;

	@Override
	public Cursor getCursor() {
		//Cursor cursor = MsgDBManager.getMsgList(pageNo, receiveId ,senderId);
		return null;
	}
}
