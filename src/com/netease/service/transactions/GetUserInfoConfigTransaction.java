package com.netease.service.transactions;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.engagement.dataMgr.ConfigDataManager;
import com.netease.pkgRelated.ErrorToString;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.UserInfoConfig;

public class GetUserInfoConfigTransaction extends EgmBaseTransaction{

	private static final boolean DEBUG = false ;
	
	//个人资料配置项版本号
	private String version ;
	
	public GetUserInfoConfigTransaction(String version) {
		super(TARNSACTION_TYPE_GET_USERINFO_CONGFIG);
		this.version = version ;
	}
	
	@Override
	public void onTransact() {
        THttpRequest request = EgmProtocol.getInstance().createGetUserInfoConfig(version);
        sendRequest(request);
	}

	@Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
		UserInfoConfig userInfoConfig = null;
        if (obj != null && obj instanceof JsonElement) {
            Gson gson = new Gson();
            JsonElement json = (JsonElement)obj;
            userInfoConfig = gson.fromJson(json, UserInfoConfig.class);
            ConfigDataManager.saveUserConfigToData(json.toString());
        }
        if (userInfoConfig != null) {
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, userInfoConfig);
        } else {
            notifyError(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION,
                    ErrorToString.getString(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION));
        }
    }
}
