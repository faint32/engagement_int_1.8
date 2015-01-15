package com.netease.service.transactions;


import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.common.log.NTLog;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;

/**
 * 获取手机号验证码
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class GetMobileVerifyTransaction extends EgmBaseTransaction {
    private final String TAG = "GetMobileVerifyTransaction";
    
    /** 注册验证码（此时会返回AES key）*/
    public static final int TYPE_REGISTER = 0;
    /** 手机绑定验证码 */
    public static final int TYPE_BIND = 1;
    /** 易信验证手机号码 */
    public static final int TYPE_YIXIN_BIND = 2;
    
    private int mType;
    private String mMobile;

    public GetMobileVerifyTransaction(int type, String mobile) {
        super(TRANSACTION_TYPE_MOBILE_VERIFY_CODE);
        
        mType = type;
        mMobile = mobile;
    }

    @Override
    public void onTransact() {
        NTLog.i(TAG, "onTransact");
        
        THttpRequest request = EgmProtocol.getInstance().createGetMobileVerifyCodeRequest(String.valueOf(mType), mMobile);
        sendRequest(request);
    }

    @Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
        NTLog.i(TAG, "onEgmTransactionSuccess");
        
        if(mType == TYPE_REGISTER){ // 返回AES key
            String aes = null;
            if(obj != null && obj instanceof JsonElement){
                JsonElement aesJson = (JsonElement)obj;
                aes = aesJson.getAsJsonObject().get("AESKey").getAsString();
                if(aes != null){
                    notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, aes);
                }
                else{
                    notifyDataParseError();
                }
            }
            else{
                notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, obj);
            }
        } else {    // 无需aes key
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, obj);
        }
    }
}
