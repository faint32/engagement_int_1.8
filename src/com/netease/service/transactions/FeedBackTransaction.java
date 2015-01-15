package com.netease.service.transactions;


import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;


public class FeedBackTransaction extends EgmBaseTransaction {

    private String content;

    public FeedBackTransaction(String content) {
        super(TRANSACTION_TYPE_FEEDBACK);
        this.content = content;
    }

    @Override
    public void onTransact() {
        THttpRequest request = EgmProtocol.getInstance().createFeedback(content);
        sendRequest(request);
    }

    @Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
        notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, null);
    }

}
