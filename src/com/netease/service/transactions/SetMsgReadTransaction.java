package com.netease.service.transactions;


import com.netease.common.http.THttpRequest;
import com.netease.service.db.manager.LastMsgDBManager;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;

/**
 * 将消息设置为已读
 */
public class SetMsgReadTransaction  extends EgmBaseTransaction{
	
	private long uid ;
	private String messageId ;
	
	public SetMsgReadTransaction(long uid ,String messageId) {
		super(TRANSACTION_SET_MSG_READ);
		this.uid = uid ;
		this.messageId = messageId ;
	}

	@Override
	public void onTransact() {
		THttpRequest request = EgmProtocol.getInstance().createSetMsgRead(uid,messageId);
	    sendRequest(request);
	}
	
	@Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
		LastMsgDBManager.setUnReadNumZero(uid);
        notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, null);
    }

}
