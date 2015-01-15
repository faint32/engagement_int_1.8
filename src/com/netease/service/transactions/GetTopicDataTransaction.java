package com.netease.service.transactions;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.pkgRelated.ErrorToString;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.TopicConfigResult;

public class GetTopicDataTransaction extends EgmBaseTransaction{

	private String version ;
	
	public GetTopicDataTransaction(String version) {
		super(TRANSACTION_GET_TOPIC_DATA);
		this.version = version ;
	}

	@Override
	public void onTransact() {
		THttpRequest request;
		request = EgmProtocol.getInstance().createGetTopicData(version);
		sendRequest(request);
	}
	
	@Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
		TopicConfigResult data = null ;
        if (obj != null && obj instanceof JsonElement) {
            Gson gson = new Gson();
            JsonElement json = (JsonElement)obj;
            data = gson.fromJson(json, TopicConfigResult.class);
        }
        if (data != null) {
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, data);
        } else {
            notifyError(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION,
                    ErrorToString.getString(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION));
        }
    }
}
