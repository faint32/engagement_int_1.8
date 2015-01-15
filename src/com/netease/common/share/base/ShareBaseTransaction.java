package com.netease.common.share.base;

import com.netease.common.task.example.JSONAsyncTransaction;

public abstract class ShareBaseTransaction extends JSONAsyncTransaction {

	public static final int TRANS_TYPE_LOGIN = 0x01;
	public static final int TRANS_TYPE_MBLOG = 0x02;
	public static final int TRANS_TYPE_FRIENDSHIPS_CREATE = 0x03;
	public static final int TRANS_TYPE_GET_FOLLOWLING_LIST = 0x04;
	
	protected ShareBaseChannel mChannel;
	
	protected ShareBaseTransaction(int type, ShareBaseChannel channel) {
		super(type);
		
		mChannel = channel;
	}
	
	@Override
	protected void onTransactionError(int errCode, Object obj) {
		notifyError(errCode, mChannel.getErrorShareResult(errCode, obj));	
	}

}
