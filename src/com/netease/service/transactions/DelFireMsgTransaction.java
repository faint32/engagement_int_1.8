package com.netease.service.transactions;

import com.netease.common.http.THttpRequest;
import com.netease.service.db.manager.MsgDBManager;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.meta.MessageInfo;

public class DelFireMsgTransaction extends EgmBaseTransaction {
	
	private MessageInfo msg;
	private boolean notifyRemote;
	
	public DelFireMsgTransaction(MessageInfo msg, boolean notifyRemote) {
		super(TRANSACTION_DELETE_FIRE_MESSAGE);
		this.msg = msg;
		this.notifyRemote = notifyRemote;
	}

	@Override
	public void onTransact() {
		msg.setFireMsgOpened(true);
		
		MsgDBManager.updateMsgReserve3(msg);
		
		if (notifyRemote) {
			THttpRequest request = EgmProtocol.getInstance().createDeleteFireMessage(msg.msgId);
	        sendRequest(request);
		}
		else {
			doEnd();
		}
	}

	@Override
	protected void onEgmTransactionSuccess(int code, Object obj) {
		super.onEgmTransactionSuccess(code, obj);
	}
	@Override
	protected void onEgmTransactionError(int errCode, Object obj) {
		super.onEgmTransactionError(errCode, obj);
	}
}
