package com.netease.common.task;

/**
 * TransactionListener 在事务中采用了弱引用方式，所以请不要使用方法内的内部匿名类作为TransactionListener
 * 
 * @author dingding
 *
 */
public interface TransactionListener {

	/**
	 * 事务消息回调接口
	 * 
	 * @param code
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public void onTransactionMessage(int code, int type, int tid, Object arg3);

	/**
	 * 事务消息回调接口
	 * 
	 * @param errCode
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public void onTransactionError(int errCode, int type, int tid, Object arg3);
}
