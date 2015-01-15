package com.netease.service.transactions;

import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;

public class UnlikePicTransaction extends EgmBaseTransaction {
	
	private String userId, picId;
	
	public UnlikePicTransaction(String userId, String picId) {
		super(TRANSACTION_UNLIKE_PIC);
		this.userId = userId;
		this.picId = picId;
	}

	@Override
	public void onTransact() {
		THttpRequest request;
		request = EgmProtocol.getInstance().createUnlikePic(userId, picId);
		sendRequest(request);
	}
	
	@Override
	protected void onEgmTransactionSuccess(int code, Object obj) {
		notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, code);
	}
}
