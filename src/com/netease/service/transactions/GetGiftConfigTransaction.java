package com.netease.service.transactions;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.engagement.dataMgr.GiftInfoManager;
import com.netease.pkgRelated.ErrorToString;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.GiftConfigResult;

/**
 * 获取礼物配置数据
 */
public class GetGiftConfigTransaction extends EgmBaseTransaction{

	private static final boolean DEBUG = false ;
	
	private String version ;
	
	public GetGiftConfigTransaction(String version) {
		super(TARNSACTION_GET_GIFT_CONFIG);
		this.version = version ;
	}

	@Override
	public void onTransact() {
		THttpRequest request = EgmProtocol.getInstance().createGetGiftConfig(version);
	    sendRequest(request);
	}
	
	@Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
		GiftConfigResult data = null ;
        if (obj != null && obj instanceof JsonElement) {
            Gson gson = new Gson();
            JsonElement json = (JsonElement)obj;
            data = gson.fromJson(json, GiftConfigResult.class);
            
            GiftInfoManager.saveGiftConfigToData(json.toString());
        }
        if (data != null) {
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, data);
        } else {
            notifyError(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION,
                    ErrorToString.getString(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION));
        }
    }

}
