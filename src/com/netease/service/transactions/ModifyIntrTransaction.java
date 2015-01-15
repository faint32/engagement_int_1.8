package com.netease.service.transactions;


import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;

public class ModifyIntrTransaction extends EgmBaseTransaction {

	private static final boolean DEBUG = false ;
	
	private String content;

	public ModifyIntrTransaction(String content) {
		super(TRANSACTION_MODIFY_INTRODUCE);
		this.content = content;
	}

	@Override
	public void onTransact() {
		THttpRequest request = EgmProtocol.getInstance().createModifyIntr(content);
		sendRequest(request);
	}

	@Override
	protected void onEgmTransactionSuccess(int code, Object obj) {
		notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, null);
	}
}
