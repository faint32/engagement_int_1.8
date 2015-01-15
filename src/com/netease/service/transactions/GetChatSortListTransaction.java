package com.netease.service.transactions;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.pkgRelated.ErrorToString;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.SortChatListResult;

public class GetChatSortListTransaction extends EgmBaseTransaction{

	private int sortType ;
	private long[] uids ; 
	
	public GetChatSortListTransaction(int sortType ,long[] uids) {
		super(TRANSACTION_SORT_CHAT_LIST);
		this.sortType = sortType ;
		this.uids = uids ;
	}

	@Override
	public void onTransact() {
		THttpRequest request;
		request = EgmProtocol.getInstance().createSortChatList(sortType,uids);
		sendRequest(request);
	}

	@Override
	protected void onEgmTransactionSuccess(int code, Object obj) {
		SortChatListResult result = null ;
		if (obj != null && obj instanceof JsonElement) {
            Gson gson = new Gson();
            JsonElement json = (JsonElement)obj;
            result = gson.fromJson(json, SortChatListResult.class);
        }
        if (result != null) {
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, result);
        } else {
            notifyError(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION,
                    ErrorToString.getString(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION));
        }
	}

}
