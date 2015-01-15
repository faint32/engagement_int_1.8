package com.netease.service.transactions;

import java.io.File;
import java.util.List;

import android.text.TextUtils;

import com.netease.common.cache.CacheManager;
import com.netease.common.cache.file.StoreFile;
import com.netease.common.http.THttpRequest;
import com.netease.common.task.AsyncTransaction;
import com.netease.common.task.NotifyTransaction;
import com.netease.engagement.dataMgr.MsgAttach;
import com.netease.service.db.manager.MsgDBManager;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.meta.MessageInfo;

public class DownloadMsgResTransaction extends AsyncTransaction {

	private MessageInfo mInfo;
	
	private MsgAttach mAttach;
	
	private boolean mForce;
	
	public DownloadMsgResTransaction(MessageInfo info, boolean force) {
		super(EgmBaseTransaction.TRANSACTION_DOWNLOAD_MSG_RES);
		
		mForce = force;
		mInfo = info;
	}
	
	public DownloadMsgResTransaction(MessageInfo info) {
		this(info, false);
	}

	@Override
	protected void onTransactionError(int errCode, Object obj) {

	}

	@Override
	protected void onTransactionSuccess(int code, Object obj) {

	}

	@Override
	public void onTransact() {
		do {
			if (mInfo == null 
					|| mInfo.getType() != EgmProtocolConstants.MSG_TYPE.MSG_TYPE_AUDIO
					|| mInfo.getMediaUrl() == null) {
				break;
			}
			
			mAttach = MsgDBManager.getMsgAttach(mInfo.getSender(), 
					mInfo.getMsgId());
			
			if (! TextUtils.isEmpty(mInfo.getAttach())) {
				mAttach = MsgAttach.toMsgAttach(mInfo.getAttach());
				
				String path = mAttach.getAudioPath();
				
				if (! TextUtils.isEmpty(path)) {
					if (new File(path).exists()) {
						return ;
					}
					else if (! mForce) {
						break;
					}
				}
			}
			
			THttpRequest request = new THttpRequest(mInfo.getMediaUrl());
			request.setCacheFile();
			sendRequest(request);
		} while (false);
		
		doEnd();
	}
	
	@Override
	public NotifyTransaction createNotifyTransaction(
			List<AsyncTransaction> trans, Object data, int notifyType, int code) {
		return new MsgResNotifyTransaction(trans, data, notifyType, code);
	}
	
	@Override
	public NotifyTransaction createNotifyTransaction(Object data,
			int notifyType, int code) {
		return new MsgResNotifyTransaction(this, data, notifyType, code);
	}
	
	class MsgResNotifyTransaction extends NotifyTransaction {

		public MsgResNotifyTransaction(AsyncTransaction tran, Object data,
				int type, int code) {
			super(tran, data, type, code);
		}

		public MsgResNotifyTransaction(List<AsyncTransaction> trans,
				Object data, int type, int code) {
			super(trans, data, type, code);
		}
		
		@Override
		public void doBeforeTransact() {
			if (isSuccessNotify()) {
				StoreFile file = CacheManager.getStoreFile(mInfo.getMediaUrl());
				
				if (file != null && file.exists()) {
					MsgAttach attach = mAttach;
					if (mAttach == null) {
						attach = new MsgAttach();
					}
					
					attach.setAudioPath(file.getPath());
					
					MsgDBManager.updateMsgAttach(mInfo, attach);
					resetData(file);
				}
			}
		}
		
	}

}
