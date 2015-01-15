package com.netease.service.transactions;

import android.text.TextUtils;

import com.netease.common.http.THttpRequest;
import com.netease.common.log.NTLog;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.util.EnctryUtil;

/**
 * 手机号注册
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class RegisterMobileTransaction extends EgmBaseTransaction {
    private final String TAG = "RegisterMobileTransaction";
    
    private String mobile;
    private String password;
    private String verifyCode;
    private String key;

    public RegisterMobileTransaction(String mobile, String password, String verifyCode, String key) {
        super(TRANSACTION_TYPE_MOBILE_REGISTER);
        
        this.mobile = mobile;
        this.password = password;
        this.verifyCode = verifyCode;
        this.key = key;
    }

    @Override
    public void onTransact() {
        NTLog.i(TAG, "onTransact");
        
        // AES加密
        StringBuffer sb = new StringBuffer();
        sb.append("mobile=").append(mobile)
            .append("&password=").append(password)
            .append("&verifyCode=").append(verifyCode);
        
        String encrypt = "";
        if(!TextUtils.isEmpty(key)){
            try {
                encrypt = EnctryUtil.encryptForAES(sb.toString(), key);
            } 
            catch (Exception e) {
                
            }
        }
        
        THttpRequest request;
        request = EgmProtocol.getInstance().createMobileRegisterRequest(mobile, encrypt);
        
        sendRequest(request);
    }

    @Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
        NTLog.i(TAG, "onEgmTransactionSuccess");
        notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, obj);
    }
}
