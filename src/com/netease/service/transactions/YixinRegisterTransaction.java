package com.netease.service.transactions;

import android.text.TextUtils;

import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.util.EnctryUtil;

/** 易信注册 */
public class YixinRegisterTransaction extends EgmBaseTransaction {
    private String mobile;
    private String password;
    private String key;
    
    public YixinRegisterTransaction(String mobile, String password, String key){
        super(TRANSACTION_TYPE_YIXIN_REGISTER);
        
        this.mobile = mobile;
        this.password = password;
        this.key = key;
    }
    
    @Override
    public void onTransact() {
        // AES加密
        StringBuffer sb = new StringBuffer();
        sb.append("mobile=").append(mobile).append("&password=").append(password);
        
        String encrypt = "";
        if(!TextUtils.isEmpty(key)){
            try {
                encrypt = EnctryUtil.encryptForAES(sb.toString(), key);
            } 
            catch (Exception e) {}
        }
        
        THttpRequest request = EgmProtocol.getInstance().createYixinRegisterRequest(mobile, encrypt);
        sendRequest(request);
    }

    @Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
        notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, obj);
    }

}
