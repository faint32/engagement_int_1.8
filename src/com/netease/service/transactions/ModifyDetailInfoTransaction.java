package com.netease.service.transactions;

import java.util.HashMap;

import org.json.JSONException;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.pkgRelated.ErrorToString;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.UserInfo;

public class ModifyDetailInfoTransaction extends EgmBaseTransaction{

	private static final boolean DEBUG = false ;
	
	private HashMap<String,String> map ;
	
	public ModifyDetailInfoTransaction(HashMap<String,String> map) {
		super(TARNSACTION_TYPE_MODIFY_DETAIL_INFO);
		this.map = map ;
	}

	@Override
	public void onTransact() {
		THttpRequest request;
		try {
			request = EgmProtocol.getInstance().createModifyDetailInfo(map);
			sendRequest(request);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	@Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
		UserInfo userinfo = null;
        if (obj != null && obj instanceof JsonElement) {
            Gson gson = new Gson();
            JsonElement json = (JsonElement)obj;
            userinfo = gson.fromJson(json, UserInfo.class);
        }
        if (userinfo != null) {
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, userinfo);
        } else {
            notifyError(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION,
                    ErrorToString.getString(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION));
        }
    }
}
