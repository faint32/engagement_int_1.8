package com.netease.service.transactions;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.AntiHarassmentInfo;

public class GetAntiHarassmentTransaction extends EgmBaseTransaction {

	public GetAntiHarassmentTransaction() {
		super(TRANSACTION_TYPE_GET_ANTIHARASSMENT);
	}

	@Override
	public void onTransact() {
		THttpRequest request = EgmProtocol.getInstance().createGetAntiHarassment();
		sendRequest(request);
	}
	@Override
	protected void onEgmTransactionSuccess(int code, Object obj) {
		AntiHarassmentInfo user = null;
		if (obj != null && obj instanceof JsonElement) {
			Gson gson = new Gson();
			JsonElement json = (JsonElement) obj;
			user = gson.fromJson(json, AntiHarassmentInfo.class);
		}
		if (user != null) {
			notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, user);
		} else {
			notifyDataParseError();
		}
	}
}
