package com.netease.common.task;

public abstract class DataChannel {

	protected TransactionEngine mTransactionEngine;

	public DataChannel(TransactionEngine engine) {
		mTransactionEngine = engine;
	}
	
	public TransactionEngine getTransactionEngine() {
		return mTransactionEngine;
	}

	/**
	 * 发送数据请求.
	 * 
	 * @param obj
	 */
	public abstract void sendRequest(Object obj, AsyncTransaction t);

	/**
	 * 取消数据请求
	 * 
	 * @param transactionId
	 */
	public abstract void cancelRequest(AsyncTransaction t);


	/**
	 * 按组调节优先级
	 * 
	 * @param transactionId
	 */
	public abstract void adjustPriorityByGID(int tid, int priority);
	
	/**
	 * 关闭数据通道.
	 */
	public abstract void close();
}
