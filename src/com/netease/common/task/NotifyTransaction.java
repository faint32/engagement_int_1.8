package com.netease.common.task;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.netease.util.priority.Priority;

public class NotifyTransaction extends Transaction {

	public static final int NOTIFY_TYPE_SUCCESS = 0x00; // 成功通知，调用onTransactionSuccess方法通知
	public static final int NOTIFY_TYPE_ERROR = 0x1; // 出错通知，调用onTransactionError方法通知
	
	private static final int NOTIFY_TYPE_NOTEND = 0x10; // 通知前不执行doEnd()；
	
	List<AsyncTransaction> mTrans;
	AsyncTransaction mTran;

	Object mData;
	int mNotifyType;
	int mCode;

	public NotifyTransaction(List<AsyncTransaction> trans, Object data, int type, int code) {
		super(TransTypeCode.TYPE_NOTIFY);
		setPriority(Priority.EMERGENCY);

		mTrans = trans;
		mData = data;
		mNotifyType = type;
		mCode = code;
	}
	
	public NotifyTransaction(AsyncTransaction tran, Object data, int type, int code) {
		super(TransTypeCode.TYPE_NOTIFY);
		setPriority(Priority.EMERGENCY);
		
		mTran = tran;
		mData = data;
		mNotifyType = type;
		mCode = code;
	}
	
	protected int getCode() {
		return mCode;
	}
	
	public boolean isSuccessNotify() {
		return mNotifyType == NOTIFY_TYPE_SUCCESS;
	}
	
	protected void setNotifyTypeAndCode(int notifyType, int code) {
		mNotifyType = notifyType & 0x01;
		mCode = code;
	}
	
	/**
	 * 
	 * @param notifyType
	 * @param code
	 * @param end 是否在通知前移除TransactionEngine的Map表，移除后不可取消（找不到映射关系）
	 */
	protected void setNotifyTypeAndCode(int notifyType, int code, boolean end) {
		mNotifyType = (notifyType & 0x01) | (end ? 0 : NOTIFY_TYPE_NOTEND);
		mCode = code;
	}
	
	private boolean isNeedEnd() {
		return (mNotifyType & NOTIFY_TYPE_NOTEND) == 0;
	}
	
	protected void resetData(Object data) {
		resetData(data, true);
	}
	
	protected void resetData(Object data, boolean close) {
		if (close && mData != null && mData instanceof InputStream ) {
			try {
				((InputStream) mData).close();
			} catch (IOException e) { }
		}
		
		mData = null;
		
		mData = data;
	}
	
	protected Object getData() {
		return mData;
	}

	/**
	 * 自定义Notify继承这个方法
	 */
	public void doBeforeTransact() {
	}
	
	public final void onTransact() {
		doBeforeTransact();
		
		boolean doEnd = isNeedEnd();
		
		if (mTrans == null) {
			if (doEnd) {
				mTran.doEnd();
			}
			if (!mTran.isCancel()) {
				try {
					if (mNotifyType == NOTIFY_TYPE_SUCCESS) {
						mTran.onTransactionSuccess(mCode, mData);
					} else {
						mTran.onTransactionError(mCode, mData);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			for (AsyncTransaction tran : mTrans) {
				if (tran != null) {
					if (doEnd) {
						tran.doEnd();
					}

					if (!tran.isCancel()) {
						try {
							if (mNotifyType == NOTIFY_TYPE_SUCCESS) {
								tran.onTransactionSuccess(mCode, mData);
							} else {
								tran.onTransactionError(mCode, mData);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		resetData(null);
	}

	@Override
	public void onTransactException(int errorCode, Exception e) {
		
	}

}
