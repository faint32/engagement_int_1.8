package com.netease.service.transactions;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.common.log.NTLog;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.DebugData;
import com.netease.service.protocol.meta.UserInfo;

/**
 * 获取用户资料
 * @author echo_chen
 * @since 2014-04-09
 * @version 1.0
 */


public class GetUserInfoTransaction extends EgmBaseTransaction {
    private static final boolean DEBUG = EgmProtocolConstants.RANDOM_DEBUG_DATA;
    private long mUid;

    public GetUserInfoTransaction(long uid) {
        super(TRANSACTION_TYPE_GET_USER_INFO);
        mUid = uid;
    }
    
    @Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
        NTLog.i("GetUserInfoTransaction", "onEgmTransactionSuccess");
        
        UserInfo user = null;
        if (obj != null && obj instanceof JsonElement) {
            Gson gson = new Gson();
            JsonElement json = (JsonElement)obj;
            user = gson.fromJson(json, UserInfo.class);
           
        }
        if (user != null) {
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, user);
        } else {
            notifyDataParseError();
        }
    }
    

    @Override
    public void onTransact() {
        if (DEBUG) {
            UserInfo.generateTestUserInfo();
            setTestFileName(DebugData.FILENAME_USERINFO_JSON);
        }
        THttpRequest request = EgmProtocol.getInstance().createGetUserInfoRequest(mUid);
        sendRequest(request);
    }

}
