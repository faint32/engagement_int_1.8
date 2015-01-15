package com.netease.service.transactions;

import com.netease.common.http.THttpRequest;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.AntiHarassmentInfo;

public class UpdateAntiHarassmentTransaction extends EgmBaseTransaction {
	private AntiHarassmentInfo mInfo;

	public UpdateAntiHarassmentTransaction(AntiHarassmentInfo info) {
		super(TRANSACTION_TYPE_SET_ANTIHARASSMENT);
		mInfo = info;
	}

	@Override
	public void onTransact() {
		if(mInfo != null){
			THttpRequest request = EgmProtocol.getInstance().createUpadateAntiHarassment(mInfo);
			sendRequest(request);
		} else {
			notifyError(EgmServiceCode.TRANSACTION_FAIL, null);
			doEnd();
		}

	}
	@Override
	protected void onEgmTransactionSuccess(int code, Object obj) {
		notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, null);
	}
}
