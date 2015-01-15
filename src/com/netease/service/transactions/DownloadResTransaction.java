package com.netease.service.transactions;

import android.text.TextUtils;

import com.netease.common.cache.CacheManager;
import com.netease.common.cache.file.StoreFile;
import com.netease.common.http.THttpRequest;
import com.netease.common.task.AsyncTransaction;
import com.netease.service.protocol.EgmServiceCode;

public class DownloadResTransaction extends AsyncTransaction {

	private String mUrl;
	
	public DownloadResTransaction(String url) {
		super(EgmBaseTransaction.TRANSACTION_DOWNLOAD_RES);
		mUrl = url;
	}

	@Override
	protected void onTransactionError(int errCode, Object obj) {
		notifyMessage(EgmServiceCode.TRANSACTION_FAIL, 1);
	}

	@Override
	protected void onTransactionSuccess(int code, Object obj) {
		notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, -1);
	}

	@Override
	public void onTransact() {
		do {
			if (!TextUtils.isEmpty(mUrl)) {
				StoreFile file = CacheManager.getStoreFile(mUrl);
				if (file != null && file.exists()) {
					notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, 1);
					break;
				}
				THttpRequest request = new THttpRequest(mUrl);
				request.setCacheFile();
				sendRequest(request);
			}
		} while (false);
		doEnd();
	}

}
