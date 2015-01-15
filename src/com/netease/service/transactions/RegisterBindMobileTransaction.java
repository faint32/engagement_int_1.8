package com.netease.service.transactions;


import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;

/**
 * 绑定手机号
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class RegisterBindMobileTransaction extends EgmBaseTransaction {
    private String mobile;
    private String verifyCode;
    private String inviteCode;
    
    public RegisterBindMobileTransaction(String mobile, String verifyCode, String inviteCode) {
        super(TRANSACTION_TYPE_BIND_MOBILE);
        
        this.mobile = mobile;
        this.verifyCode = verifyCode;
        this.inviteCode = inviteCode;
    }

    @Override
    public void onTransact() {
        THttpRequest request = EgmProtocol.getInstance().createBindMobileRequest(mobile, verifyCode, inviteCode);
        sendRequest(request);
    }

    @Override
    public void onEgmTransactionSuccess(int code, Object obj){
        notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, obj);
    }
}
