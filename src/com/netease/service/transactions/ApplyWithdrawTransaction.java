package com.netease.service.transactions;


import com.google.gson.Gson;
import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.ApplyWithdrawError;
import com.netease.service.protocol.meta.BaseData;

/** 申请提现 */
public class ApplyWithdrawTransaction extends EgmBaseTransaction {

    public ApplyWithdrawTransaction(){
        super(TRANSACTION_TYPE_APPLY_WITHDRAW);
    }
    
    @Override
    public void onTransact() {
        THttpRequest request = EgmProtocol.getInstance().createApplyWithdrawRequest();
        sendRequest(request);
    }

    @Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
        notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, null);
    }
    
    @Override
    protected boolean onEgmTransactionError(int errCode, BaseData data) {
    	switch (errCode) {
    	case EgmServiceCode.TRANSACTION_WITHDRAW_IDENTITY_VIDEO_ERROR:
    	case EgmServiceCode.TRANSACTION_WITHDRAW_NEED_IDENTITY_AUTH:
    		if (data != null && data.data != null) {
    			Gson gson = new Gson();
    			ApplyWithdrawError error = gson.fromJson(data.data, 
    					ApplyWithdrawError.class);
    			error.errMessage = data.message;
    			
    			notifyError(errCode, error);
    			return true;
    		}
    	}
    	
    	return super.onEgmTransactionError(errCode, data);
    }
}

