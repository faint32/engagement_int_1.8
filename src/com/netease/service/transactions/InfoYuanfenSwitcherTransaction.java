package com.netease.service.transactions;


import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;

/**
 * 通知服务器碰缘分开关状态
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class InfoYuanfenSwitcherTransaction extends EgmBaseTransaction {
    /** 碰缘分开关 */
    private boolean isOpen;
    
    public InfoYuanfenSwitcherTransaction(boolean isOpen){
        super(TRANSACTION_TYPE_INFORM_YUANFEN_SWITCHER);
        
        this.isOpen = isOpen;
    }
    
    @Override
    public void onTransact() {
        THttpRequest request = EgmProtocol.getInstance().createInformYuanfenSwitcherRequest(isOpen);
        sendRequest(request);
    }

    @Override
    public void onEgmTransactionSuccess(int code, Object obj){
        super.onEgmTransactionSuccess(code, obj);
        
        Boolean isOpen = null;
        if (obj != null && obj instanceof JsonElement) {
            JsonElement openJson = (JsonElement)obj;
            isOpen = openJson.getAsJsonObject().get("isOpen").getAsBoolean();
        }
        
        if(isOpen != null){
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, isOpen);
        }
        else {
            notifyDataParseError();
        }
    }
}
