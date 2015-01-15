package com.netease.common.task;

import java.lang.ref.WeakReference;

import com.netease.common.log.NTLog;
import com.netease.util.priority.Priority;

public abstract class Transaction implements Runnable, Comparable<Transaction> {

	private static final boolean DEBUG = false;

	/*************************************************************************
	 * mType 0, -1 ~ -200 为保留类型，建议应用相关的type取值 1 ~ 30000
	 ************************************************************************/
	
	/**
	 * 事务类型，建议（24主类型 + 子类型）
	 */
	int mType;

	/**
	 * Transaction 唯一标识.
	 */
	private int mId;

	/**
	 * 是否被取消标记
	 */
	private boolean isCancel;

	/**
	 * 事务管理器
	 */
	TransactionEngine mTransMgr;

	/**
	 * 事务监听器
	 */
	WeakReference<TransactionListener> mListener;
	
	/**
	 * 事物优先级
	 */
	int mPriority;
	
	/**
	 * 是否序列的
	 */
	boolean mSerial;
	
	/**
	 * 分组ID, 非0有效
	 */
	int mGroupID;

	static int mTransactionId = 0;

	protected Transaction(int type) {
		mType = type;
		mId = getNextTransactionId();
		mPriority = Priority.NORMAL;
		mSerial = true;
	}

	public void setTransactionEngine(TransactionEngine transMgr) {
		mTransMgr = transMgr;
	}

	public TransactionEngine getTransactionEngine() {
		return mTransMgr;
	}

	public void doEnd() {
		if (mTransMgr != null) {
			mTransMgr.endTransaction(this);
		}
	}
	
	public TransactionListener getListener() {
		return mListener != null ? mListener.get() : null;
	}

	public void setListener(TransactionListener listener) {
		mListener = new WeakReference<TransactionListener>(listener);
	}
	
	public void setPriority(int priority) {
		mPriority = priority;
	}
	
	public int getPriority() {
		return mPriority;
	}
	
	public void setSerial(boolean serial) {
		mSerial = serial;
	}
	
	public boolean getSerial() {
		return mSerial;
	}
	
	public void setGroupID(int gid) {
		mGroupID = gid;
	}
	
	public int getGroupID() {
		return mGroupID;
	}
	
	/**
	 * Gets the id of this transaction.
	 * 
	 * @return the id of this transaction.
	 */
	public int getId() {
		return mId;
	}

	//    
	/**
	 * Get the type of this transaction
	 * 
	 * @return
	 */
	public int getType() {
		return mType;
	}

	/**
	 * 是否取消
	 * 
	 * @return 如果取消返回true,否则返回false
	 */
	public boolean isCancel() {
		return isCancel;
	}

	/**
	 * 取消事务
	 */
	public void doCancel() {
		isCancel = true;
	}

	public void run() {
		try {
			if ( !isCancel()) {
				onTransact();
			}
		} catch (Exception e) {
			e.printStackTrace();

			doCancel();
			onTransactException(0, e);
		}

		mTransMgr.endTransaction(this);
	}

	/**
	 * Transaction 任务执行入口
	 */
	public abstract void onTransact();

	/**
	 * Transaction 执行异常.
	 * 
	 * @param e
	 */
	public void onTransactException(int errorCode, Exception e) {
		
	}

	public void notifyMessage(int msgCode, Object arg3) {
		TransactionListener listener = getListener();
		
		if (listener != null) {
//			if (DEBUG) NTLog.e("notify msg", "t-" + mType + "c-" + msgCode);
			listener.onTransactionMessage(msgCode, mType, mId, arg3);
		}
	}
	
	public void notifyError(int errCode, Object arg3) {
		TransactionListener listener = getListener();
		if (listener != null) {
			if (DEBUG) NTLog.e("notify err", "t-" + mType + "c-" + errCode);
			listener.onTransactionError(errCode, mType, mId, arg3);
		}
	}
	
	protected void notifyMessage(int msgCode, int type, int tid, Object arg3) {
		TransactionListener listener = getListener();
		
		if (listener != null) {
//			if (DEBUG) NTLog.e("notify msg", "t-" + mType + "c-" + msgCode);
			listener.onTransactionMessage(msgCode, type, tid, arg3);
		}
	}

	protected void notifyError(int errCode, int type, int tid, Object arg3) {
		TransactionListener listener = getListener();
		
		if (listener != null) {
			if (DEBUG) NTLog.e("notify err", "t-" + mType + "c-" + errCode);
			listener.onTransactionError(errCode, type, tid, arg3);
		}
	}
	
	/**
	 * 
	 * @return
	 */
	private synchronized static int getNextTransactionId() {
		if (mTransactionId >= Short.MAX_VALUE) {
			mTransactionId = 0;
		}
		return ++mTransactionId;
	}
	
	
	long mCreateTime = System.currentTimeMillis();
	
	@Override
	public int compareTo(Transaction another) {
		int priority = mPriority & 0xFF;
		int aPriority = another.mPriority & 0xFF;
		if (priority > aPriority) {
			return -1;
		}
		else if (priority < aPriority) {
			return 1;
		}
		
		if (mCreateTime < another.mCreateTime) {
			return -1;
		}
		else if (mCreateTime > another.mCreateTime) {
			return 1;
		}
		
		return 0;
	}
}
