package com.netease.service.transactions;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.MoneyRecordListInfo;

/** 女性现金收支记录 */
public class GetMoneyHistoryTransaction extends EgmBaseTransaction {
    private int type;
    private int page;

    public GetMoneyHistoryTransaction(int type, int page) {
        super(TRANSACTION_TYPE_GET_MONEY_HISTORY);
        
        this.type = type;
        this.page = page;
    }

    @Override
    public void onTransact() {
        THttpRequest request = EgmProtocol.getInstance().createGetMoneyHistoryRequest(type, page);
        sendRequest(request);
    }
    
    @Override
    public void onEgmTransactionSuccess(int code, Object obj){
        MoneyRecordListInfo info = null;
        
        if(obj != null && obj instanceof JsonElement){
            Gson gson = new Gson();
            info = gson.fromJson((JsonElement)obj, new TypeToken<MoneyRecordListInfo>(){}.getType());
        }
        
        if(info != null && info.records != null){
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, info);
        }
        else{
            notifyDataParseError();
        }
    }
}