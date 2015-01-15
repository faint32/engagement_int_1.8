package com.netease.service.transactions;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.netease.common.log.NTLog;
import com.netease.common.task.example.StringAsyncTransaction;
import com.netease.service.Utils.DeviceUtil;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.app.BaseApplication;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmService;
import com.netease.util.PlatformUtil;

abstract public class EncryptBaseTransaction extends StringAsyncTransaction {

	public EncryptBaseTransaction(int type) {
		super(type);

	}

	@Override
	public void onTransact() {
		Context context = BaseApplication.getAppInstance();
		String ursId = EgmPrefHelper.getURSId(context);
		String ursKey = EgmPrefHelper.getURSKey(context);
		if (TextUtils.isEmpty(ursId) || TextUtils.isEmpty(ursKey)) { // 还未初始化URS
			EgmService.getInstance().addListener(mGetIdKeyCallback);
			EgmService.getInstance().doInitURS();
		} else {
			realOntransact();
		}
	}

	private EgmCallBack mGetIdKeyCallback = new EgmCallBack() {
		/** 
         * 初始化URS 
         * data[0]：ursId，是该设备上的该版本的app在URS上的id
         * data[1]：ursKey，是URS返回的用于登录时AES加密的key
         */
		@Override
		public void onInitURS(int transactionId, String[] data) {
			NTLog.i("EncryptBaseTransaction", "onGetIdKey");
			getTransactionEngine()
					.beginTransaction(EncryptBaseTransaction.this);
			EgmService.getInstance().removeListener(mGetIdKeyCallback);
		}

		@Override
		public void onInitURSError(int transactionId, int errCode, String err) {
			NTLog.i("EncryptBaseTransaction", "onGetIdKeyError");
			notifyError(errCode, err);
			EgmService.getInstance().removeListener(mGetIdKeyCallback);
			EncryptBaseTransaction.this.doEnd();
		};
	};

	abstract void realOntransact();
}
