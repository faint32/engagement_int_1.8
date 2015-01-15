package com.netease.service.transactions;


import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;

public class ComplainTransaction extends EgmBaseTransaction{

	public int type ;//举报类型
	public String content ;//举报内容
	public long postId ;//被举报人id
	
	public ComplainTransaction(int type,String content ,long postId) {
		super(TRANSACTION_COMPLAIN);
		this.type = type ;
		this.content = content ;
		this.postId = postId ;
	}

	@Override
	public void onTransact() {
		THttpRequest request;
		request = EgmProtocol.getInstance().createComplain(type,content,postId);
		sendRequest(request);
	}
	
	@Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
        notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, null);
    }

}
