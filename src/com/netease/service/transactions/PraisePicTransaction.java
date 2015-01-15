package com.netease.service.transactions;


import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;

public class PraisePicTransaction extends EgmBaseTransaction{
	
	private String userId ;
	private String picId ;
	
	public PraisePicTransaction(String userId,String picId) {
		super(TRANSACTION_PRAISE_PIC);
		this.userId = userId ;
		this.picId = picId ;
	}

	@Override
	public void onTransact() {
		THttpRequest request;
		request = EgmProtocol.getInstance().createPraisePic(userId,picId);
		sendRequest(request);
	}

	@Override
	protected void onEgmTransactionSuccess(int code, Object obj) {
		notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, code);
	}
}
