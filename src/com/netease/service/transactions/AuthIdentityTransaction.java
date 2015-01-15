package com.netease.service.transactions;


import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;

/** 验证身份证 */
public class AuthIdentityTransaction extends EgmBaseTransaction {
    private String name;
    private String idCardNo;
    private String idCardPic1;
    private String idCardPic2;
    private String video;

    public AuthIdentityTransaction(String name, String idCardNo, 
    		String idCardPic1, String idCardPic2, String video) {
        super(TRANSACTION_TYPE_AUTH_IDENTITY);
        
        this.name = name;
        this.idCardNo = idCardNo;
        this.idCardPic1 = idCardPic1;
        this.idCardPic2 = idCardPic2;
        this.video = video;
    }

    @Override
    public void onTransact() {
        THttpRequest request = EgmProtocol.getInstance().createAuthIdentityRequest
        		(name, idCardNo, idCardPic1, idCardPic2, video);
        sendRequest(request);
    }

    @Override
    public void onEgmTransactionSuccess(int code, Object obj){
        notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, null);
    }

}
