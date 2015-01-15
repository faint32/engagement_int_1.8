package com.netease.common.task.example;

import java.util.List;

import com.netease.common.task.AsyncTransaction;
import com.netease.common.task.NotifyTransaction;

public abstract class JSONAsyncTransaction extends StringAsyncTransaction {

	public JSONAsyncTransaction(int type) {
		super(type);
	}
	
	@Override
	public NotifyTransaction createNotifyTransaction(Object data,
			int notifyType, int code) {
		return new JSONNotifyTransaction(this, data, notifyType, code);
	}
	
	@Override
	public NotifyTransaction createNotifyTransaction(
			List<AsyncTransaction> trans, Object data, int notifyType, int code) {
		return new JSONNotifyTransaction(trans, data, notifyType, code);
	}
}
