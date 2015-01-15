package com.netease.common.task;

import java.io.IOException;
import java.util.List;

public abstract class AsyncTransaction extends Transaction {

	DataChannel mDataChannel;

	protected AsyncTransaction(int type) {
		super(type);
	}

	public void setDataChannel(DataChannel dataChannel) {
		mDataChannel = dataChannel;
	}

	protected void sendRequest(Object obj) {
		if (mDataChannel != null) {
			mDataChannel.sendRequest(obj, this);
		}
	}
	
	protected void sendRequest(Object obj, AsyncTransaction trans) {
		if (mDataChannel != null) {
			mDataChannel.sendRequest(obj, trans);
		}
	}
	
	protected void cancelRequest() {
		if (mDataChannel != null) {
			mDataChannel.cancelRequest(this);
		}
	}
	
	@Override
	public void doCancel() {
		super.doCancel();
		cancelRequest();
	}

	public final void run() {
		try {
			if (! isCancel()) {
				onTransact();
			}
		} catch (Exception e) {
			e.printStackTrace();

			doCancel();
			onTransactException(0, e);
			doEnd();
		}
	}
	
	/**
	 * 派发出错事务
	 */
	protected abstract void onTransactionError(int errCode, Object obj);

	/**
	 * 派发数据回应事务
	 */
	protected abstract void onTransactionSuccess(int code, Object obj);

	/**
	 * 
	 * @param data
	 * @param notifyType
	 * @param code
	 * @return 返回null时，表示按后续数据按
	 */
	public Object onDataChannelPreNotify(Object request, Object data, 
			int notifyType, int code) throws IOException {
		return null;
	}
	
	public NotifyTransaction createNotifyTransaction(Object data,
			int notifyType, int code) {
		return new NotifyTransaction(this, data, notifyType, code);
	}

	public NotifyTransaction createNotifyTransaction(
			List<AsyncTransaction> trans, Object data, int notifyType, int code) {
		return new NotifyTransaction(trans, data, notifyType, code);
	}
	
	@Override
	public void onTransactException(int errorCode, Exception e) {
		
	}

}
