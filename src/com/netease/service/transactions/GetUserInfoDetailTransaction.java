package com.netease.service.transactions;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.pkgRelated.ErrorToString;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.DebugData;
import com.netease.service.protocol.meta.UserInfo;
import com.netease.service.protocol.meta.UserInfoDetail;

public class GetUserInfoDetailTransaction extends EgmBaseTransaction{

	private static final boolean DEBUG = false ;
	
	//用户id
	private long uid ;
	
	public GetUserInfoDetailTransaction(long uid) {
		super(TARNSACTION_TYPE_GET_USERINFO_DETAIL);
		this.uid = uid ;
	}
	
	@Override
	public void onTransact() {
		if (EgmProtocolConstants.RANDOM_DEBUG_DATA && DEBUG) {
            UserInfoDetail.generateTestData(uid);
            setTestFileName(DebugData.FILENAME_USERINFO_JSON + uid);
        }
        THttpRequest request = EgmProtocol.getInstance().createGetUserInfoDetail(uid);
        sendRequest(request);
	}

	@Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
        UserInfoDetail userInfoDetail = null;
        if (obj != null && obj instanceof JsonElement) {
            Gson gson = new Gson();
            JsonElement json = (JsonElement)obj;
            userInfoDetail = gson.fromJson(json, UserInfoDetail.class);
        }
        if (userInfoDetail != null) {
        	// 剔除视频和语音同时返回的情况
        	UserInfo info = userInfoDetail.userInfo;
        	if (info != null) {
        		if (info.hasVideo()) {
        			info.voiceIntroduce = null;
        		}
        		else {
        			info.videoIntroduce = null;
        		}
        	}
        	
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, userInfoDetail);
        } else {
            notifyError(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION,
                    ErrorToString.getString(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION));
        }
    }
}
