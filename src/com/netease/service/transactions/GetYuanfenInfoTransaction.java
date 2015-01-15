package com.netease.service.transactions;


import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.YuanfenInfo;

/**
 * 获取碰缘分状态数据
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class GetYuanfenInfoTransaction extends EgmBaseTransaction {
    
    public GetYuanfenInfoTransaction(){
        super(TRANSACTION_TYPE_GET_YUANFEN_INFO);
    }
    
    @Override
    public void onTransact() {
        THttpRequest request = EgmProtocol.getInstance().createGetYuanfenRequest();
        sendRequest(request);
    }

    @Override
    public void onEgmTransactionSuccess(int code, Object obj){
        super.onEgmTransactionSuccess(code, obj);
        
        YuanfenInfo info = null;
        if (obj != null && obj instanceof JsonElement) {
            info = YuanfenInfo.fromJson((JsonElement)obj);
        }
        
        if (info != null) {
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, info);
        } else {
            notifyDataParseError();
        }
    }
}
