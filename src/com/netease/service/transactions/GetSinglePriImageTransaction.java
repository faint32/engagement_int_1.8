package com.netease.service.transactions;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.pkgRelated.ErrorToString;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.GetSigPriResult;

public class GetSinglePriImageTransaction extends EgmBaseTransaction{

	private long userId ;
	private long picId ;
	
	public GetSinglePriImageTransaction(long userId ,long picId) {
		super(TRANSACTION_GET_SINGLE_PRI_IMAGE);
		this.userId = userId ;
		this.picId = picId ;
	}

	@Override
	public void onTransact() {
		THttpRequest request = EgmProtocol.getInstance().createGetSinglePriImage(userId,picId);
        sendRequest(request);
	}

	@Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
		GetSigPriResult result = null;
        if (obj != null && obj instanceof JsonElement) {
            Gson gson = new Gson();
            JsonElement json = (JsonElement)obj;
            result = gson.fromJson(json,GetSigPriResult.class);
        }
        if (result != null) {
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, result);
        } else {
            notifyError(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION,
                    ErrorToString.getString(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION));
        }
    }
}
