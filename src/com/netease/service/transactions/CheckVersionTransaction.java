package com.netease.service.transactions;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.app.BaseApplication;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.VersionInfo;


public class CheckVersionTransaction extends EgmBaseTransaction {

    public CheckVersionTransaction() {
        super(TRANSACTION_TYPE_CHECKVERSION);
    }

    @Override
    public void onTransact() {
        String channel = EgmUtil.getAppChannelID(BaseApplication.getAppInstance());
        THttpRequest request = EgmProtocol.getInstance().createCheckVersion(channel);
        sendRequest(request);
    }

    @Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
        
        VersionInfo version = null;
        if (obj != null && obj instanceof JsonElement) {
            Gson gson = new Gson();
            JsonElement json = (JsonElement)obj;
            version = gson.fromJson(json, VersionInfo.class);
        }
        if (version != null) {
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, version);
        } else {
            notifyDataParseError();
        }
    }

}
