package com.netease.service.transactions;

import java.util.List;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.netease.common.http.THttpRequest;
import com.netease.pkgRelated.ErrorToString;
import com.netease.service.db.manager.LastMsgDBManager;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.EgmProtocolConstants.MSG_TYPE;
import com.netease.service.protocol.meta.ChatItemInfo;
import com.netease.util.PDEEngine;


/**
 * 获取聊天列表
 */
public class GetChatListTransaction extends EgmBaseTransaction{
	
	public GetChatListTransaction() {
		super(TRANSACTION_GET_CHAT_LIST);
	}

	@Override
	public void onTransact() {
		 THttpRequest request = EgmProtocol.getInstance().createGetChatList();
	     sendRequest(request);
	}

	@Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
		List<ChatItemInfo> list = null;
        if (obj != null && obj instanceof JsonElement) {
            Gson gson = new Gson();
            JsonElement array = ((JsonElement)obj).getAsJsonObject().get("chatList");
            list = gson.fromJson(array, new TypeToken<List<ChatItemInfo>>(){}.getType());
            
            if (list != null) {
            	for (ChatItemInfo info : list) {
            		if (info.message != null) {
            			switch (info.message.type) {
    					case MSG_TYPE.MSG_TYPE_TEXT:
    					case MSG_TYPE.MSG_TYPE_SYS:
    						info.message.msgContent = PDEEngine.PXDecrypt(info.message.msgContent);
    						break;
    					}
            		}
            	}
            }
            
            LastMsgDBManager.updateLasgMsgList(list);
        }
        if (list != null) {
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS,list);
        } else {
            notifyError(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION,
                    ErrorToString.getString(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION));
        }
    }
	
}
