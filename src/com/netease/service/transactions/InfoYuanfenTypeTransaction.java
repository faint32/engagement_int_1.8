package com.netease.service.transactions;


import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;

/**
 * 通知服务器碰缘分类型
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class InfoYuanfenTypeTransaction extends EgmBaseTransaction {
    /** 碰缘分类型 */
    private int fateType;
    
    public InfoYuanfenTypeTransaction(int type){
        super(TRANSACTION_TYPE_INFORM_YUANFEN_TYPE);
        
        fateType = type;
    }
    
    @Override
    public void onTransact() {
        THttpRequest request = EgmProtocol.getInstance().createInformYuanfenTypeRequest(fateType);
        sendRequest(request);
    }

    @Override
    public void onEgmTransactionSuccess(int code, Object obj){
        super.onEgmTransactionSuccess(code, obj);
        
        Integer fateType = null;
        if (obj != null && obj instanceof JsonElement) {
            JsonElement typeJson = (JsonElement)obj;
            fateType = typeJson.getAsJsonObject().get("fateType").getAsInt();
        }
        
        if (fateType != null) {
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, fateType);
        } 
        else {
            notifyDataParseError();
        }
    }
}
