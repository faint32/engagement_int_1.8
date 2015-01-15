package com.netease.service.transactions;

import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;

public class QueryInvitorTransaction extends EgmBaseTransaction {
	
	private long inviteCode;
	
	public QueryInvitorTransaction(long inviteCode) {
		super(TRANSACTION_QUERY_INVITOR);
		this.inviteCode = inviteCode;
	}

	@Override
	public void onTransact() {
		THttpRequest request = EgmProtocol.getInstance().createQueryInvitor(inviteCode);
		sendRequest(request);
	}
	
	@Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
        super.onEgmTransactionSuccess(code, obj);
        
        Boolean exist = null;
        if(obj != null && obj instanceof JsonElement) {
        	JsonElement existJson = (JsonElement) obj;
        	exist = existJson.getAsJsonObject().get("exist").getAsBoolean();
        }
        
        if(exist != null) {
        	notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, exist);
        } else {
        	notifyDataParseError();
        }
    }

}
