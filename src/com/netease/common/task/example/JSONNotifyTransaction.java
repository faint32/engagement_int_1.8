package com.netease.common.task.example;

import java.util.List;

import org.json.JSONObject;

import com.netease.common.http.cache.HttpCache;
import com.netease.common.http.cache.HttpCacheManager;
import com.netease.common.task.AsyncTransaction;
import com.netease.common.task.TransTypeCode;
import com.netease.service.Utils.StreamUtil;

public class JSONNotifyTransaction extends StringNotifyTransaction {

	public JSONNotifyTransaction(AsyncTransaction tran, Object data,
			int type, int code) {
		super(tran, data, type, code);
	}

	public JSONNotifyTransaction(List<AsyncTransaction> trans, Object data,
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
						JSONObject json = new JSONObject((String) data);
						resetData(json);
					} else if (data instanceof HttpCache) {
						String str = StreamUtil.readString(
								((HttpCache) data).LocalFile.openInputStream());
						resetData(new JSONObject(str));
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
