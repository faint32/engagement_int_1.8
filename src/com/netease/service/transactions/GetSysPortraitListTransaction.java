package com.netease.service.transactions;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.pkgRelated.ErrorToString;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.SysPortraitListResult;

/**
 * 获取系统头像列表事务
 * @author gordondu
 *
 */
public class GetSysPortraitListTransaction extends EgmBaseTransaction {

	public GetSysPortraitListTransaction() {
		super(TARNSACTION_TYPE_GET_SYSPORTRAIT_LIST);
	}

	@Override
	public void onTransact() {
		THttpRequest request = EgmProtocol.getInstance().createGetSysPortraitList();
		sendRequest(request);
	}

	@Override
	protected void onEgmTransactionSuccess(int code, Object obj) {
		SysPortraitListResult result = null;
		if (obj != null && obj instanceof JsonElement) {
			Gson gson = new Gson();
			JsonElement json = (JsonElement) obj;
			result = gson.fromJson(json, SysPortraitListResult.class);
		}
		if (result != null) {
			notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, result);
		} else {
			notifyError(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION, ErrorToString.getString(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION));
		}
	}
}
