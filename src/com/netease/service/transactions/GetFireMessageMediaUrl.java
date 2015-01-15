package com.netease.service.transactions;

import android.text.TextUtils;

import com.google.gson.JsonElement;
import com.netease.common.cache.CacheManager;
import com.netease.common.cache.file.StoreFile;
import com.netease.common.http.THttpRequest;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.MsgAttach;
import com.netease.service.db.manager.MsgDBManager;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.LoopBack;
import com.netease.service.protocol.meta.MessageInfo;
import com.netease.util.PDEEngine;

public class GetFireMessageMediaUrl extends EgmBaseTransaction {
	
	private MessageInfo msg;
	
	public GetFireMessageMediaUrl(MessageInfo info) {
		super(TRANSACTION_GET_FIRE_MESSAGE_MEDIA_URL);
		this.msg = info;
	}

	@Override
	public void onTransact() {
		MsgAttach attach = MsgDBManager.getMsgAttach(msg.sender, msg.msgId);
		
		if (attach != null && ! TextUtils.isEmpty(attach.mediaResUrl)) {
			notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, attach.mediaResUrl);
			doEnd();
			return ;
		}
		
		THttpRequest request = EgmProtocol.getInstance().createGetFireMessageMediaUrl(
				msg.getMsgId());
		
        sendRequest(request);
	}
	
	@Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
		super.onEgmTransactionSuccess(code, obj);
		
		String mediaUrl = "";
		if(obj != null && obj instanceof JsonElement) {
			JsonElement accountJson = (JsonElement) obj;
			mediaUrl = accountJson.getAsJsonObject().get("mediaUrl").getAsString();
			mediaUrl = PDEEngine.PXDecrypt(mediaUrl);
		}
		
		if(!TextUtils.isEmpty(mediaUrl)) {
			updateMediaUrl(msg, mediaUrl);
			
			notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, mediaUrl);
			
			delFireMsg();
		} else {
			notifyDataParseError();
		}
    }

	private void updateMediaUrl(MessageInfo msg, String mediaUrl) {
		MsgAttach attach = msg.getMsgAttach();
		if (attach != null) {
			if (! TextUtils.isEmpty(attach.getMediaResUrl())) {
				StoreFile oldFile = CacheManager.getStoreFile(attach.getMediaResUrl());
				StoreFile newFile = CacheManager.getStoreFile(mediaUrl);
				
				if (oldFile != null && newFile != null && oldFile.exists()) {
					oldFile.renameTo(newFile);
				}
			}
		}
		else {
			attach = new MsgAttach();
		}
		
		attach.setMediaResUrl(mediaUrl);
		
		MsgDBManager.updateMsgAttach(msg, attach);
	}

	@Override
	protected void onEgmTransactionError(int errCode, Object obj) {
		super.onEgmTransactionError(errCode, obj);
	}
	
	private void delFireMsg(){
		LoopBack lp = new LoopBack();
		lp.mType = EgmConstants.LOOPBACK_TYPE.msg_fire_delete ;
		lp.mData = msg;
		EgmService.getInstance().doLoopBack(lp);
	}
}
