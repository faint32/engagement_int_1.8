package com.netease.service.transactions;


import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.MoneyAccountInfo;

/** 个人中心女性账户 */
public class GetMoneyAccountTransaction extends EgmBaseTransaction {

    public GetMoneyAccountTransaction(){
        super(TRANSACTION_TYPE_GET_MONEY_ACCOUNT_INFO);
    }
    
    @Override
    public void onTransact() {
        THttpRequest request = EgmProtocol.getInstance().createGetMoneyAccountRequest();
        sendRequest(request);
    }

    @Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
        MoneyAccountInfo info = null;
        
        if(obj != null && obj instanceof JsonElement){
            info = MoneyAccountInfo.fromGson((JsonElement)obj);
        }
        
        if(info != null){
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, info);
        }
        else{
            notifyDataParseError();
        }
    }
}
