package com.netease.service.transactions;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.RecommendActivityListInfo;

public class GetCompetitionListTransaction extends EgmBaseTransaction {


	public GetCompetitionListTransaction() {
		super(EgmBaseTransaction.TRANSACTION_GET_COMPETITION_LIST);
	}

	@Override
	public void onTransact() {
        THttpRequest request = EgmProtocol.getInstance().createGetCompetitionListRequest();
        sendRequest(request);
	}

	@Override
	protected void onEgmTransactionSuccess(int code, Object obj) {
		RecommendActivityListInfo mListInfo=null;
		if (obj != null && obj instanceof JsonElement) {
			Gson gson = new Gson();
			mListInfo = gson.fromJson((JsonElement) obj,RecommendActivityListInfo.class);
		}
        if(mListInfo.list != null){
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, mListInfo);
        }
        else{
            notifyDataParseError();
        }
	}
}
