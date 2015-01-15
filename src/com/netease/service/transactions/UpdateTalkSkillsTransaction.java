package com.netease.service.transactions;

import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;

public class UpdateTalkSkillsTransaction extends EgmBaseTransaction {
	
	private int skills[];

	public UpdateTalkSkillsTransaction(int skills[]) {
		super(TRANSACTION_UPDATE_CHAT_SKILLS_LIST);
		this.skills = skills;
	}

	@Override
	public void onTransact() {
		THttpRequest request;
		request = EgmProtocol.getInstance().createUpdateTalkSkills(skills);
		sendRequest(request);
	}
	
	@Override
    protected void onEgmTransactionSuccess(int code, Object obj) {
        notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, null);
    }

}

