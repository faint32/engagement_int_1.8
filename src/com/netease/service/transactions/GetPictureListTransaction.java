package com.netease.service.transactions;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.pkgRelated.ErrorToString;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.PictureInfos;

public class GetPictureListTransaction extends EgmBaseTransaction{
	
	private static final boolean DEBUG = false ;
	
	private String userId ;
	private int type ;
	private int page ;
	
	public GetPictureListTransaction(String userId ,int type ,int page) {
		super(TARNSACTION_GET_PICTURE_LIST);
		this.userId = userId ;
		this.type = type ;
		this.page = page ;
	}

	@Override
	public void onTransact() {
		if (EgmProtocolConstants.RANDOM_DEBUG_DATA && DEBUG) {
			PictureInfos.generateTestData();
            setTestFileName(PictureInfos.FILE_NAME);
        }
        THttpRequest request = EgmProtocol.getInstance().createGetPictureList(userId,type,page);
        sendRequest(request);
	}

	@Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
		PictureInfos data = null;
        if (obj != null && obj instanceof JsonElement) {
            Gson gson = new Gson();
            JsonElement json = (JsonElement)obj;
            data = gson.fromJson(json, PictureInfos.class);
        }
        if (data != null) {
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, data);
        } else {
            notifyError(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION,
                    ErrorToString.getString(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION));
        }
    }
}
