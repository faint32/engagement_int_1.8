package com.netease.service.transactions;


import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.YixinAddFriendInfo;


public class YixinCheckTransaction extends EgmBaseTransaction {
    
    public YixinCheckTransaction(){
        super(TRANSACTION_TYPE_YIXIN_CHECK);
    }
    
    @Override
    public void onTransact() {
        THttpRequest request = EgmProtocol.getInstance().createYixinCheckRequest();
        sendRequest(request);
    }

    @Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
        YixinAddFriendInfo info = null;
        
        // 特殊处理，添加朋友失败的原因是未注册，需要返回数据
        if(code == EgmServiceCode.TRANSACTION_YIXIN_NO_REGISTER){   
            if(obj != null && obj instanceof JsonElement){
                info = YixinAddFriendInfo.fromGson((JsonElement)obj);
            }
            
            if(info != null){
                notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, info);
            }
            else{
                notifyDataParseError();
            }
        }
        else{
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, null);
        }
    }

}
