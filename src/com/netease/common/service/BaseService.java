package com.netease.common.service;

import android.content.Context;

import com.netease.common.http.ApnReference;
import com.netease.common.task.AsyncTransaction;
import com.netease.common.task.DataChannel;
import com.netease.common.task.Transaction;
import com.netease.common.task.TransactionEngine;
import com.netease.util.ByteArrayPool;
import com.netease.util.CharArrayPool;

/**
 * 注意，需要在Application启动的时候注册ApplicationContext，即调用静态方法
 * BaseService.initServiceContext(Context)
 * 
 * 应用相关的Service需要继承该BaseService
 * 
 * @author dingding
 *
 */
public abstract class BaseService {

	TransactionEngine mTransactionEngine;
	DataChannel mDataChanel;
	
	private static Context mAppContext;
	
	private static ByteArrayPool mArrayPool = new ByteArrayPool(1024 << 2);
	
	private static CharArrayPool mCharPool = new CharArrayPool(1024 << 2);
	
	public static void initServiceContext(Context appContext) {
		if (mAppContext == null) {
			ApnReference.getInstance(appContext);
		}
		mAppContext = appContext;
		
	}
	
	public static byte[] getByteBuf(int len) {
		return mArrayPool.getBuf(len);
	}
	
	public static void returnByteBuf(byte[] buf) {
		mArrayPool.returnBuf(buf);
	}
	
	public static char[] getCharBuf(int len) {
		return mCharPool.getBuf(len);
	}
	
	public static void returnCharBuf(char[] buf) {
		mCharPool.returnBuf(buf);
	}
	
	public static Context getServiceContext() {
		return mAppContext;
	}
	
	public BaseService(DataChannel dataChanel) {
		mDataChanel = dataChanel;
		mTransactionEngine = dataChanel.getTransactionEngine();
	}
	
	public int beginTransaction(Transaction trans) {
		int ret = -1;
		
		if (trans != null) {
			ret = trans.getId();
			
			if (trans instanceof AsyncTransaction) {
				((AsyncTransaction) trans).setDataChannel(mDataChanel);
			}
			if (mTransactionEngine != null) {
				mTransactionEngine.beginTransaction(trans);
			}
		}
		
		return ret;
	}
	
	public void cancelTransaction(int tid) {
		if (mTransactionEngine != null) {
			mTransactionEngine.cancelTransaction(tid);
		}
	}
	
}
