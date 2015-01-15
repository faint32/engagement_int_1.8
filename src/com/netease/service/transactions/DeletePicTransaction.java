package com.netease.service.transactions;


import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;

public class DeletePicTransaction extends EgmBaseTransaction{

	private static final boolean DEBUG = false ;
	private int type ;
	private long[] pictureIds ;
	
	public DeletePicTransaction(int type ,long[] pictureIds) {
		super(TRANSACTION_DELETE_PIC);
		this.type = type ;
		this.pictureIds = pictureIds ;
	}

	@Override
	public void onTransact() {
		THttpRequest request;
		request = EgmProtocol.getInstance().createDeletePic(type,pictureIds);
		sendRequest(request);
	}

	@Override
	protected void onEgmTransactionSuccess(int code, Object obj) {
		notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, code);
	}
}
