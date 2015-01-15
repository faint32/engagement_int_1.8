package com.netease.service.transactions;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.pkgRelated.ErrorToString;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.ChatSkillInfo;
import com.netease.service.protocol.meta.ChatSkillInfoList;

public class GetTalkSkillsTransaction extends EgmBaseTransaction {

	public GetTalkSkillsTransaction() {
		super(TRANSACTION_GET_CHAT_SKILLS_LIST);
	}

	@Override
	public void onTransact() {
		THttpRequest request;
		request = EgmProtocol.getInstance().createGetTalkSkills();
		sendRequest(request);
	}
	
	@Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
		ArrayList<ChatSkillInfo> chatSkills = null ;
        if (obj != null && obj instanceof JsonElement) {
            Gson gson = new Gson();
            JsonElement json = (JsonElement)obj;
            ChatSkillInfoList data = gson.fromJson(json, ChatSkillInfoList.class );
            if (data != null) {
            	chatSkills = data.chatSkills;
            }
        }
        if (chatSkills != null) {
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, chatSkills);
        } else {
            notifyError(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION,
                    ErrorToString.getString(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION));
        }
    }

}
