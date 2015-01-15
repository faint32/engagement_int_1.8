package com.netease.engagement.dataMgr.cursorloader;

import com.netease.service.db.manager.MsgDBManager;

import android.content.Context;
import android.database.Cursor;

public class MsgListLoader extends Loader{

	public MsgListLoader(Context context) {
		super(context);
	}
	
	public MsgListLoader(Context context ,long time ,int pageNo,long receiveId ,long senderId){
		this(context);
		this.time = time ;
		this.pageNo = pageNo ;
		this.receiveId = receiveId ;
		this.senderId = senderId ;
	}
	
	private long time ;
	private int pageNo ;
	private long receiveId ;
	private long senderId ;

	@Override
	public Cursor getCursor() {
		Cursor cursor = MsgDBManager.getMsgList(time,pageNo, receiveId ,senderId);
		return cursor;
	}
}
