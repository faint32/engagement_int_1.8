package com.netease.service.transactions;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.pkgRelated.ErrorToString;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.GiftRecords;

public class GetGiftListTransaction extends EgmBaseTransaction{

	private int page ;
	
	public GetGiftListTransaction(int page) {
		super(TARNSACTION_TYPE_GET_GIFT_LIST);
		this.page = page ;
	}

	@Override
	public void onTransact() {
        THttpRequest request = EgmProtocol.getInstance().createGetGiftList(page);
        sendRequest(request);
	}

	@Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
		GiftRecords records = null ;
        if (obj != null && obj instanceof JsonElement) {
            Gson gson = new Gson();
            JsonElement json = (JsonElement)obj;
            records = gson.fromJson(json,GiftRecords.class);
        }
        if (records != null) {
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, records);
        } else {
            notifyError(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION,
                    ErrorToString.getString(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION));
        }
    }
}
