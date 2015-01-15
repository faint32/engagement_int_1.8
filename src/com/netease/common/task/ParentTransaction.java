package com.netease.common.task;

import java.util.LinkedList;
import java.util.List;

public abstract class ParentTransaction extends AsyncTransaction 
		implements TransactionListener {

	Object[] mLock = new Object[0];
	
	List<AsyncTransaction> mChildrenTrans;
	boolean mHasChildTrans;
	
	protected ParentTransaction(int type) {
		super(type);
	}

	protected void beginChildTranscation(AsyncTransaction trans) {
		beginChildTranscation(trans, true);
	}

	protected void beginChildTranscation(AsyncTransaction trans, boolean begin) {
		mHasChildTrans = true;
		trans.setGroupID(getGroupID());
		trans.setPriority(getPriority());
		trans.setListener(this);
		synchronized (mLock) {
			if (mChildrenTrans == null) {
				mChildrenTrans = new LinkedList<AsyncTransaction>();
			}
			mChildrenTrans.add(trans);
		}
		
		if (begin) {
			getTransactionEngine().beginTransaction(trans);
		}
	}
	
	protected boolean hasChildThrans() {
		return mHasChildTrans;
	}
	
	protected void beginAllChildTransaction() {
		if (mChildrenTrans == null) {
			return ;
		}
		
		synchronized (mLock) {
			for (AsyncTransaction t : mChildrenTrans) {
				if (getTransactionEngine().getTransaction(t.getId()) == null) {
					getTransactionEngine().beginTransaction(t);
				}
			}
		}
	}
	
	@Override
	public void doCancel() {
		super.doCancel();
		
		if (mChildrenTrans != null) {
			synchronized (mLock) {
				for (AsyncTransaction trans : mChildrenTrans) {
					trans.doCancel();
				}
			}
		}
	}
	
	/**
	 * 按事务id获取子事务
	 * @param tid
	 * @return
	 */
	protected AsyncTransaction getChildTrans(int tid) {
		AsyncTransaction trans = null;
		
		if (mChildrenTrans != null) {
			synchronized (mLock) {
				for (AsyncTransaction at : mChildrenTrans) {
					if (at.getId() == tid) {
						trans = at;
						break;
					}
				}
			}
		}
		
		return trans;
	}
	
	/**
	 * 按事务id移除子事务
	 * @param tid
	 */
	private void removeChildTrans(int tid) {
		if (mChildrenTrans != null) {
			AsyncTransaction trans = null;
			
			synchronized (mLock) {
				for (AsyncTransaction at : mChildrenTrans) {
					if (at.getId() == tid) {
						trans = at;
						break;
					}
				}
				
				if (trans != null) {
					mChildrenTrans.remove(trans);
				}
			}
		}
	}
	
	/**
	 * 判断是否还有子事务存在
	 * @return
	 */
	protected boolean isFinished() {
		return mChildrenTrans == null || mChildrenTrans.size() == 0;
	}
	
	/**
	 * 子事务消息回调
	 * @return false 清理child
	 */
	public abstract void onChildMessage(int code, int type, int tid, Object arg3);
	
	/**
	 * 事务消息回调接口
	 */
	public final void onTransactionMessage(int code, int type, int tid, Object arg3) {
		synchronized (mLock) {
			if (code != TransTypeCode.CODE_PARENT_CONTIUE) {
				removeChildTrans(tid);
			}
			
			onChildMessage(code, type, tid, arg3);
		}
		
		if (isFinished()) {
			doEnd();
		}
	}

	/**
	 * 子事务出错消息回调
	 * @return false 清理child
	 */
	public abstract void onChildError(int errCode, int type, int tid, Object arg3);
	
	/**
	 * 事务消息回调接口
	 */
	public final void onTransactionError(int errCode, int type, int tid, Object arg3) {
		synchronized (mLock) {
			if (errCode != TransTypeCode.CODE_PARENT_CONTIUE) {
				removeChildTrans(tid);
			}
			
			onChildError(errCode, type, tid, arg3);
		}
		
		if (isFinished()) {
			doEnd();
		}
	}
}
