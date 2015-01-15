package com.netease.service.transactions;

import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;

/**
 * 设置系统头像事务
 * @author gordondu
 *
 */
public class UpdateSysPortraitTransaction extends EgmBaseTransaction {

	private int mPid;
	
	public UpdateSysPortraitTransaction(int pid) {
		super(TARNSACTION_TYPE_UPDATE_SYSPORTRAIT);
		mPid = pid;
	}

	@Override
	public void onTransact() {
		THttpRequest request = EgmProtocol.getInstance().createUpdateSysPortrait(mPid);
		sendRequest(request);
	}

	@Override
	protected void onEgmTransactionSuccess(int code, Object obj) {
		notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, code);
	}
}
