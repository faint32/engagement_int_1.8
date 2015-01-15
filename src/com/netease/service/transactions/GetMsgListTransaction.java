package com.netease.service.transactions;


import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.pkgRelated.ErrorToString;
import com.netease.service.db.manager.MsgDBManager;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.EgmProtocolConstants.MSG_TYPE;
import com.netease.service.protocol.meta.MessageInfo;
import com.netease.service.protocol.meta.MessageList;
import com.netease.util.PDEEngine;

public class GetMsgListTransaction extends EgmBaseTransaction {

	private long uid ;
	
	public GetMsgListTransaction(long uid) {
		super(TRANSACTION_GET_MSG_LIST);
		this.uid = uid ;
	}

	@Override
	public void onTransact() {
		THttpRequest request = EgmProtocol.getInstance().createGetMsgList(uid);
	    sendRequest(request);
	}
	
	@Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
		MessageList data = null ;
        if (obj != null && obj instanceof JsonElement) {
            Gson gson = new Gson();
            JsonElement json = (JsonElement)obj;
            data = gson.fromJson(json, MessageList.class);
        }
        if(data != null && data.msgList.size() > 0){
        	List<MessageInfo> msgList = data.msgList ;
			for(MessageInfo info : msgList){
				switch (info.type) {
				case MSG_TYPE.MSG_TYPE_TEXT:
				case MSG_TYPE.MSG_TYPE_SYS:
					info.msgContent = PDEEngine.PXDecrypt(info.msgContent);
					break;
				}
				
				MsgDBManager.insertMsg(info);
			}
		}
        if (data != null) {
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, data);
        } else {
            notifyError(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION,
                    ErrorToString.getString(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION));
        }
    }
}
