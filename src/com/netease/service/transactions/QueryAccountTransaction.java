package com.netease.service.transactions;

import android.text.TextUtils;

import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;

public class QueryAccountTransaction extends EgmBaseTransaction {

	private String mobile;
	
	public QueryAccountTransaction(String mobile) {
		super(TRANSACTION_QUERY_ACCOUNT);
		this.mobile = mobile;
	}

	@Override
	public void onTransact() {
		THttpRequest request = EgmProtocol.getInstance().createQueryAccount(mobile);
		sendRequest(request);
	}

	@Override
	protected void onEgmTransactionSuccess(int code, Object obj) {
		super.onEgmTransactionSuccess(code, obj);
		
		String account = "";
		if(obj != null && obj instanceof JsonElement) {
			JsonElement accountJson = (JsonElement) obj;
			account = accountJson.getAsJsonObject().get("account").getAsString();
		}
		
		if(!TextUtils.isEmpty(account)) {
			notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, account);
		} else {
			notifyDataParseError();
		}
	}
	
	

}
