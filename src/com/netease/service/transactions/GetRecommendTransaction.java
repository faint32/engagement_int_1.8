package com.netease.service.transactions;

import java.util.List;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.DebugData;
import com.netease.service.protocol.meta.RecommendListInfo;
import com.netease.service.protocol.meta.RecommendUserInfo;

/**
 * 获取推荐列表
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class GetRecommendTransaction extends EgmBaseTransaction {
    private static final boolean DEBUG = EgmProtocolConstants.RANDOM_DEBUG_DATA;
    private boolean isLogin;

    public GetRecommendTransaction(boolean isLogin) {
        super(TRANSACTION_TYPE_GET_RECOMMEND);
        
        this.isLogin = isLogin;
    }

    @Override
    public void onTransact() {
        if (DEBUG) {
            RecommendUserInfo.generateTestInfo();
            setTestFileName(DebugData.FILENAME_RECOMMEND_JSON);
        }
        
        THttpRequest request = EgmProtocol.getInstance().createGetRecommendRequest(isLogin);
        sendRequest(request);
    }
    
    @Override
    public void onEgmTransactionSuccess(int code, Object obj){
        
        RecommendListInfo mRecommendListInfo=new RecommendListInfo();
        if(obj != null && obj instanceof JsonElement){
            Gson gson = new Gson();
            mRecommendListInfo = gson.fromJson((JsonElement)obj, RecommendListInfo.class);
        }
        if(mRecommendListInfo.list != null){
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, mRecommendListInfo);
        }
        else{
            notifyDataParseError();
        }
    }
}
