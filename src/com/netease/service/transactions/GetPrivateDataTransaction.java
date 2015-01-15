package com.netease.service.transactions;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.common.task.TransTypeCode;
import com.netease.framework.widget.ToastUtil;
import com.netease.pkgRelated.ErrorToString;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.UserPrivateData;


public class GetPrivateDataTransaction extends EgmBaseTransaction{

	private static final boolean DEBUG = false ;
	
	public GetPrivateDataTransaction() {
		super(TARNSACTION_GET_PRIVATE_DATA);
	}

	@Override
	public void onTransact() {
		if (EgmProtocolConstants.RANDOM_DEBUG_DATA && DEBUG) {
            UserPrivateData.generateTestData();
            setTestFileName(UserPrivateData.FILE_NAME);
        }
        THttpRequest request = EgmProtocol.getInstance().createGetPrivateData();
        sendRequest(request);
	}

	@Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
        UserPrivateData data = null;
        if (obj != null && obj instanceof JsonElement) {
            Gson gson = new Gson();
            JsonElement json = (JsonElement)obj;
            data = gson.fromJson(json, UserPrivateData.class);
        }
        if (data != null) {
        	if(code == EgmServiceCode.ERR_CODE_NO_NETWORK || 
        			code == EgmServiceCode.ERR_CODE_NETWORK_IOEXCEPTION || 
        					code == TransTypeCode.ERR_CODE_NETWORK_EXCEPTION) {
        		
        		notifyError(code, obj);
        	} else {
        		notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, data);
        		ManagerAccount.getInstance().updateLoginAccount(data.userInfo);
        	}
        } 
        else {
            notifyError(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION,
                    ErrorToString.getString(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION));
        }
    }
}
