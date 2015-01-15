package com.netease.service.transactions;


import android.text.TextUtils;

import com.netease.common.http.THttpRequest;
import com.netease.service.db.manager.LastMsgDBManager;
import com.netease.service.db.manager.MsgDBManager;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;

/**
 * 删除聊天列表中某项
 */
public class DelChatItemTransaction extends EgmBaseTransaction {

	private String userId ;
	
	public DelChatItemTransaction(String userId) {
		super(TRANSACTION_DEL_CHAT_LIST);
		this.userId = userId ;
	}

	@Override
	public void onTransact() {
		THttpRequest request;
		request = EgmProtocol.getInstance().createDelChatList(userId);
		sendRequest(request);
	}

	@Override
	protected void onEgmTransactionSuccess(int code, Object obj) {
		if(!TextUtils.isEmpty(userId)){
			LastMsgDBManager.delMsgByUid(Long.parseLong(userId));
			MsgDBManager.delMsgByUid(Long.parseLong(userId));
		}
		notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, code);
	}

}
