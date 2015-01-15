package com.netease.service.protocol;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.netease.common.log.NTLog;
import com.netease.common.task.TransTypeCode;
import com.netease.common.task.TransactionListener;
import com.netease.pkgRelated.ErrorToString;

public class GroupTransactionListener implements TransactionListener {
	WeakHashMap<EgmCallBack, Void> mCallbacks = 
            new WeakHashMap<EgmCallBack, Void>();
	Handler mHandler = new InternalHandler();
		
	@Override
	synchronized public void onTransactionError(int errCode, int type, int tid, Object err) {
//		NTLog.i("GroupTransactionListener", "onTransactionError errCode = " + errCode);
		switch (errCode) {
		case EgmServiceCode.ERR_CODE_NO_NETWORK:
		case EgmServiceCode.ERR_CODE_NETWORK_IOEXCEPTION:	
		case EgmServiceCode.ERR_CODE_NETWORK_EXCEPTION:	
			  errCode = EgmServiceCode.NETWORK_ERR_COMMON;
	          err = null;
			break;
		case EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION:
			err = null;
			break;
		case EgmServiceCode.ERR_CODE_FILE_CREATE_EXCEPTION:
			err = ErrorToString.getString(errCode);
			break;
		default:
			if (errCode < EgmServiceCode.ERR_CODE_HTTP) {
			    err = Integer.toHexString(errCode);
			    errCode = EgmServiceCode.TRANSACTION_COMMON_SERVER_ERROR;
			    err = ErrorToString.getString(errCode) + " " + err;
			    NTLog.i("HTTP", "SERVER_ERROR err =" +  err);
	        } 
			break;
		} 
        
		String errStr = null;
		if(err != null && err instanceof String) {
			errStr = (String) err;
		}
		
		if(TextUtils.isEmpty(errStr)){
			NTLog.i("GroupTransactionListener", "onTransactionError get errStr");
			errStr = ErrorToString.getString(errCode);
		}
		
		if (err == null || err instanceof String) {
			err = errStr;
		}
		
		ResultInfo info = new ResultInfo(errCode, err);
		mHandler.obtainMessage(TRANSACTION_ERROR, type, tid, info)
				.sendToTarget();
	}

	@Override
	synchronized public void onTransactionMessage(int code, int type, int tid, Object obj) {
		
		if(EgmServiceCode.TRANSACTION_SUCCESS != code)
			return;
		
		ResultInfo info = new ResultInfo(code,obj);
		Message message = mHandler.obtainMessage(TRANSACTION_SUCCESS, type, tid, info);
		message.sendToTarget();
	}
	
	private void onSuccess(int type, int tid, int code, Object obj) {
		Set<EgmCallBack> set = mCallbacks.keySet();
        List<EgmCallBack> list = new LinkedList<EgmCallBack>();
        list.addAll(set);
		if (list.size() > 0) {
			for (EgmCallBack callback : list) {
				try {
					callback.onSuccess(type, tid, code, obj);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void onError(int type, int tid, int errCode, Object errStr) {
//		List<XoneCallBack> list = new LinkedList<XoneCallBack>();
//		list.addAll(mListener);
		
		Set<EgmCallBack> set = mCallbacks.keySet();
        List<EgmCallBack> list = new LinkedList<EgmCallBack>();
        list.addAll(set);
        
		if (list.size() > 0) {
			String errMsg = null;
			Object param = errStr;
			
			if (errStr != null && errStr instanceof String) {
				errMsg = (String) errStr;
				param = null;
			}
			
			for (EgmCallBack callback : list) {
				try {
					callback.onError(type, tid, errCode, errMsg, param);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 
	 * @param type
	 * @param listener
	 */
	synchronized public void addListener(EgmCallBack listener){
	    if (listener != null) {
            mCallbacks.put(listener, null);
        }
//		if(mListener == null){
//			mListener = new LinkedList<XoneCallBack>();
//		}
//		
//		if(!mListener.contains(listener))
//			mListener.add(listener);
	}
	
	synchronized public void removeListener(EgmCallBack listener){
		if(listener != null){
		    mCallbacks.remove(listener);
		}
	}
	
	static class ResultInfo {
		public int mcode = -1;
		public Object mObject = null;

		public ResultInfo(int code, Object object) {
			// TODO Auto-generated constructor stub
			mcode = code;
			mObject = object;
		}
	}
	
	private static final int TRANSACTION_ERROR = 0x1;
	private static final int TRANSACTION_SUCCESS = 0x2;
	
	private class InternalHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			try {
				switch (msg.what) {
				case TRANSACTION_ERROR:
					onError(msg.arg1, msg.arg2, ((ResultInfo) msg.obj).mcode,
							((ResultInfo) msg.obj).mObject);
					break;

				case TRANSACTION_SUCCESS:
					onSuccess(msg.arg1, msg.arg2, ((ResultInfo) msg.obj).mcode,
							((ResultInfo) msg.obj).mObject);
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
