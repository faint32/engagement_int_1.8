package com.netease.common.http;

import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import com.netease.common.task.AsyncTransaction;
import com.netease.common.task.DataChannel;
import com.netease.common.task.NotifyTransaction;
import com.netease.common.task.TransactionEngine;
import com.netease.util.priority.Priority;

public class HttpDataChannel extends DataChannel implements HttpCallBack {

	static final String TAG = "HttpDataChannel";
	/**
	 * Hashtable<RequestID, Transaction>
	 */
	Hashtable<Integer, AsyncTransaction> mTransactionMap;

	/**
	 * Hashtable<TransactionID, HttpRequest>
	 */
	Hashtable<Integer, THttpRequest> mRequestMap;
	
	HttpEngine mHttpEngine;


	public HttpDataChannel(TransactionEngine engine, HttpEngine httpEngine) {
		super(engine);
		mTransactionMap = new Hashtable<Integer, AsyncTransaction>();
		mRequestMap = new Hashtable<Integer, THttpRequest>();
		mHttpEngine = httpEngine;
	}
	
	public void sendRequest(Object request,
			AsyncTransaction transaction) {
		if (request == null || ! (request instanceof THttpRequest)) {
			return ;
		}
		
		THttpRequest httpRequest = (THttpRequest) request;
		if (transaction == null) {
			httpRequest.setPriority(Priority.LOW);
			mHttpEngine.addRequest(httpRequest);
		}
		else {
			Integer requestID = Integer.valueOf(httpRequest.getRequestID());
			Integer tID = Integer.valueOf(transaction.getId());
			
			httpRequest.setHttpCallBack(this);
			httpRequest.setPriority(transaction.getPriority());
			
			mTransactionMap.put(requestID, transaction);
			mRequestMap.put(tID, httpRequest);
			
			mHttpEngine.addRequest(httpRequest);
		}
	}

	@Override
	public Object onPreError(THttpRequest request, int errCode, Object data) throws IOException {
		AsyncTransaction trans = mTransactionMap.get(Integer.valueOf(
				request.getRequestID()));
		Object ret = null;
		if (trans != null) {
			ret = trans.onDataChannelPreNotify(request, data, 
					NotifyTransaction.NOTIFY_TYPE_ERROR, errCode);
		}
		return ret;
	}
	
	@Override
	public Object onPreReceived(THttpRequest request, int code, THttpResponse response) throws IOException {
		AsyncTransaction trans = mTransactionMap.get(Integer.valueOf(
				request.getRequestID()));
		Object ret = null;
		if (trans != null) {
			ret = trans.onDataChannelPreNotify(request, response, 
					NotifyTransaction.NOTIFY_TYPE_SUCCESS, code);
		}
		return ret;
	}
	
	/**
	 * 出错返回
	 */
	public boolean onError(THttpRequest request, int errCode, Object object) {
		return onRequestNotify(request, object, NotifyTransaction.NOTIFY_TYPE_ERROR, errCode);
	}

	/**
	 * 当网络数据接收完成返回
	 */
	public boolean onReceived(THttpRequest request, int code, Object object) {
		return onRequestNotify(request, object, NotifyTransaction.NOTIFY_TYPE_SUCCESS, code);
	}
	
	/**
	 * 取消一个网络连接.
	 * 
	 * @param transactionId
	 */
	public void cancelRequest(AsyncTransaction t) {
		Object key = Integer.valueOf(t.getId());

		THttpRequest request = null;

		request = (THttpRequest) mRequestMap.remove(key);

		if (request != null) {
			Integer requestId = Integer.valueOf(request.getRequestID());
			request.doCancel();
			mTransactionMap.remove(requestId);
		}
	}

	public void close() {
		mTransactionMap.clear();
		mRequestMap.clear();
		
		if (mHttpEngine != null) {
			mHttpEngine.shutdown();
		}
	}

	@Override
	public void adjustPriorityByGID(int gid, int priority) {
		mHttpEngine.adjustPriorityByGID(gid, priority);
	}

	@Override
	public boolean onError(List<THttpRequest>  requests, int errCode, Object object) {
		return onRequestNotify(requests, object, NotifyTransaction.NOTIFY_TYPE_ERROR, errCode);
	}

	@Override
	public boolean onReceived(List<THttpRequest> requests, int code, Object object) {
		return onRequestNotify(requests, object, NotifyTransaction.NOTIFY_TYPE_SUCCESS, code);
	}
	
	private boolean onRequestNotify(THttpRequest request, Object object, 
			int notifyType, int code) {
		
		AsyncTransaction tran = null;
		Integer key = Integer.valueOf(request.getRequestID());

		tran = mTransactionMap.remove(key);

		if (tran != null) {
			mRequestMap.remove(Integer.valueOf(tran.getId()));
		}
		
		boolean ret = false;
		if (tran != null) {
			ret = true;
			NotifyTransaction notify = tran.createNotifyTransaction(object, 
					notifyType, code);
			mTransactionEngine.beginTransaction(notify);
		}
		
		return ret;
	}
	
	private boolean onRequestNotify(List<THttpRequest> requests, Object object, 
			int notifyType, int code) {
		LinkedList<AsyncTransaction> trans = new LinkedList<AsyncTransaction>();
		
		for (THttpRequest request : requests) {
			AsyncTransaction tran = mTransactionMap.remove(Integer
					.valueOf(request.getRequestID()));
			if (tran != null) {
				mRequestMap.remove(Integer.valueOf(tran.getId()));
				trans.add(tran);
			}
		}
		
		boolean ret = false;
		if (trans.size() > 0) {
			ret = true;
			NotifyTransaction notify = trans.getFirst().createNotifyTransaction(
					trans, object, notifyType, code);
			mTransactionEngine.beginTransaction(notify);
		}
		
		return ret;
	}


}
