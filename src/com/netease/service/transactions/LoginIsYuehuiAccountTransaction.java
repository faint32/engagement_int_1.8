package com.netease.service.transactions;


import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;

/**
 * 判断是否是同城约会1.0的帐号，同时会判断该帐号是否绑定（验证）过手机号，用于登录的时候。
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class LoginIsYuehuiAccountTransaction extends EgmBaseTransaction {

    public LoginIsYuehuiAccountTransaction() {
        super(TRANSACTION_TYPE_IS_YUEHUI_ACCOUNT);
    }

    @Override
    public void onTransact() {
        THttpRequest request = EgmProtocol.getInstance().createIsYuehuiAccountRequest();
        sendRequest(request);
    }
    
    @Override
    public void onEgmTransactionSuccess(int code, Object obj){
    		boolean isNewReg = false;
    	 	if(obj != null && obj instanceof JsonElement){
             JsonElement json = (JsonElement)obj;
             isNewReg = json.getAsJsonObject().get("isNewReg").getAsBoolean();
         }
        notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, isNewReg);
    }
}
