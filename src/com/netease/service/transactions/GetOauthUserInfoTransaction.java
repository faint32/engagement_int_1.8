package com.netease.service.transactions;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.common.log.NTLog;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.OauthUserInfo;

public class GetOauthUserInfoTransaction extends EgmBaseTransaction {

	public GetOauthUserInfoTransaction() {
		super(TRANSACTION_TYPE_GET_OAUTHUSERINFO);
	}

	@Override
	protected void onEgmTransactionSuccess(int code, Object obj) {
		OauthUserInfo user = null;
		if (obj != null && obj instanceof JsonElement) {
			Gson gson = new Gson();
			JsonElement json = (JsonElement) obj;
			user = gson.fromJson(json.getAsJsonObject().get("oauthUserInfo"), OauthUserInfo.class);
		}
		if (user != null) {
			notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, user);
		} else {
			notifyDataParseError();
		}
	}

	@Override
	public void onTransact() {
		THttpRequest request = EgmProtocol.getInstance()
				.createGetOauthUserInfo();
		sendRequest(request);
	}
}
