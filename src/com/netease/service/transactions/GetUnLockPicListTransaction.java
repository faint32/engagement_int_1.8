package com.netease.service.transactions;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.pkgRelated.ErrorToString;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.GetUnLockPicListResult;

/**
 * 获取已解锁私照列表事务
 * @author gordondu
 *
 */
public class GetUnLockPicListTransaction extends EgmBaseTransaction {
	
	private String mUserId;
	private String mPicId;

	public GetUnLockPicListTransaction(String userId, String picId) {
		super(TARNSACTION_GET_UNLOCK_PICTURE_LIST);
		mUserId = userId;
		mPicId = picId;
	}

	@Override
	public void onTransact() {
		THttpRequest request = EgmProtocol.getInstance().createGetUnLockPicList(mUserId, mPicId);
		sendRequest(request);
	}

	@Override
	protected void onEgmTransactionSuccess(int code, Object obj) {
		GetUnLockPicListResult result = null;
		if (obj != null && obj instanceof JsonElement) {
			Gson gson = new Gson();
			JsonElement json = (JsonElement) obj;
			result = gson.fromJson(json, GetUnLockPicListResult.class);
		}
		if (result != null) {
			notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, result);
		} else {
			notifyError(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION, ErrorToString.getString(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION));
		}
	}
}
