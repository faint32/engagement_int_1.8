package com.netease.service.transactions;

import android.content.Context;

import com.netease.common.http.THttpRequest;
import com.netease.engagement.app.EgmConstants;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;

/**
 * 退出登录
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class LogoutTransaction extends EgmBaseTransaction {
    private Context mContext;
    
    public LogoutTransaction(Context context){
        super(TRANSACTION_TYPE_LOGOUT);
        mContext = context;
    }
    
    @Override
    public void onTransact() {
        THttpRequest request = EgmProtocol.getInstance().createLogoutRequest();
        sendRequest(request);
    }

    @Override
    public void onEgmTransactionSuccess(int code, Object obj){
        super.onEgmTransactionSuccess(code, obj);
        
        clearSearchHistory();
        notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, obj);
    }
    
    /** 注销后清除搜索条件记录 */
    private void clearSearchHistory(){
        EgmPrefHelper.putSearchAgeStart(mContext, EgmConstants.SEARCH_START_AGE_DEFAULT);
        EgmPrefHelper.putSearchAgeEnd(mContext, EgmConstants.SEARCH_END_AGE_DEFAULT);
        EgmPrefHelper.putSearchAstro(mContext, 0);
        EgmPrefHelper.putSearchArea(mContext, 0);
        EgmPrefHelper.putSearchIncome(mContext, 0);
        EgmPrefHelper.putSearchPrivate(mContext, false);
    }
}
