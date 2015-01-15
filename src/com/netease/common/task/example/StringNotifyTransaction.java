package com.netease.common.task.example;

import java.util.List;

import com.netease.common.http.cache.HttpCache;
import com.netease.common.http.cache.HttpCacheManager;
import com.netease.common.task.AsyncTransaction;
import com.netease.common.task.NotifyTransaction;
import com.netease.common.task.TransTypeCode;
import com.netease.service.Utils.StreamUtil;

public class StringNotifyTransaction extends NotifyTransaction {
	
	public StringNotifyTransaction(AsyncTransaction tran, Object data,
			int type, int code) {
		super(tran, data, type, code);
	}
	
	public StringNotifyTransaction(List<AsyncTransaction> trans, Object data,
			int type, int code) {
		super(trans, data, type, code);
	}

	@Override
	public void doBeforeTransact() {
		if (isSuccessNotify()) {
			Object data = getData();
			if (data != null) {
				try {
					if (data instanceof String) {
//						resetData((String) data);
					} else if (data instanceof HttpCache) {
						String str = StreamUtil.readString(
								((HttpCache) data).LocalFile.openInputStream());
						resetData(str);
					} else {
						setNotifyTypeAndCode(NOTIFY_TYPE_ERROR, 0);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					if (data instanceof HttpCache) {
						HttpCacheManager.deleteHttpCache((HttpCache) data); 
					}
					
					setNotifyTypeAndCode(NOTIFY_TYPE_ERROR, 
							TransTypeCode.ERR_CODE_DATA_PARSE_EXCEPTION);
				}
			} else {
				setNotifyTypeAndCode(NOTIFY_TYPE_ERROR, 0);
			}
		}
	}
	
}
