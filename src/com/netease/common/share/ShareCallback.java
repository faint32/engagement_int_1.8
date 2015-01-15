package com.netease.common.share;

import android.os.Handler;
import android.os.Message;

import com.netease.common.share.base.ShareBaseTransaction;
import com.netease.common.task.TransactionListener;

public abstract class ShareCallback implements TransactionListener {
	
	private static final int SHARE_ERROR_MSG = 0x01;
	private static final int SHARE_SUCCESS_MSG = 0x02;
	private static final int SHARE_MBLOG_ERROR_MSG = 0x03;
	private static final int SHARE_MBLOG_SUCCESS_MSG = 0x04;
	private static final int SHARE_FOLLOWING_LIST_ERROR_MSG = 0x05;
	private static final int SHARE_FOLLOWING_LIST_SUCCESS_MSG = 0x06;
	
	Handler mHandler;
	
	public ShareCallback() {
		mHandler = new InnerHandler(this);
	}
	
	public ShareCallback(Handler handler) {
		mHandler = handler;
	}
	
	private static class InnerHandler extends Handler {
		ShareCallback mCallback;
		
		public InnerHandler(ShareCallback callback) {
			mCallback = callback;
		}
		
		@Override
		public void handleMessage(Message msg) {
//			super.handleMessage(msg);
			switch(msg.what) {
			case SHARE_ERROR_MSG:
				mCallback.onShareError(msg.arg2, (ShareResult) msg.obj);
				break;
			case SHARE_SUCCESS_MSG:
				mCallback.onShareSuccess(msg.arg2, (ShareResult) msg.obj);
				break;
			case SHARE_MBLOG_ERROR_MSG:
				mCallback.onShareMBlogError(msg.arg2, (ShareResult) msg.obj);
				break;
			case SHARE_MBLOG_SUCCESS_MSG:
				mCallback.onShareMBlogSuccess(msg.arg2, (ShareResult) msg.obj);
				break;
			case SHARE_FOLLOWING_LIST_ERROR_MSG:
				mCallback.onShareFollowingListError(msg.arg2, (ShareResult) msg.obj);
				break;
			case SHARE_FOLLOWING_LIST_SUCCESS_MSG:
				mCallback.onShareFollowingListSuccess(msg.arg2, (ShareResult) msg.obj);
				break;
			}
		}
	}
	

	/**
	 * 分享（绑定）成功
	 * @param tid
	 * @param shareResult
	 */
	public void onShareSuccess(int tid, ShareResult shareResult) {
		
	}
	
	/**
	 * 分享（绑定）失败
	 * 
	 * @param tid
	 * @param shareResult
	 */
	public void onShareError(int tid, ShareResult shareResult) {
		
	}
	
	/**
	 * 分享微博成功
	 * 
	 * @param tid
	 * @param shareResult
	 */
	public void onShareMBlogSuccess(int tid, ShareResult shareResult) {
		
	}
	
	/**
	 * 分享微博失败
	 * 
	 * @param tid
	 * @param shareResult
	 */
	public void onShareMBlogError(int tid, ShareResult shareResult) {
		
	}
	
	/**
	 * 获取关注列表成功
	 * 
	 * @param tid
	 * @param shareResult
	 */
	public void onShareFollowingListSuccess(int tid, ShareResult shareResult) {
		
	}
	
	/**
	 * 获取关注列表失败
	 * 
	 * @param tid
	 * @param shareResult
	 */
	public void onShareFollowingListError(int tid, ShareResult shareResult) {
		
	}
	
	/**
	 * @param arg3 instance of ShareResult
	 */
	@Override
	public void onTransactionError(int errCode, int type, int tid, Object arg3) {
		int what = 0;
		switch (type) {
		case ShareBaseTransaction.TRANS_TYPE_LOGIN:
			what = SHARE_ERROR_MSG;
			break;
		case ShareBaseTransaction.TRANS_TYPE_MBLOG:
			what = SHARE_MBLOG_ERROR_MSG;
			break;
		case ShareBaseTransaction.TRANS_TYPE_GET_FOLLOWLING_LIST:
			what = SHARE_FOLLOWING_LIST_ERROR_MSG;
			break;
		default:
			what = SHARE_ERROR_MSG;
		}
		
		mHandler.obtainMessage(what, type, tid, arg3).sendToTarget();
	}
	
	/**
	 * @param arg3 instance of ShareResult
	 */
	@Override
	public void onTransactionMessage(int code, int type, int tid, Object arg3) {
		int what = 0;
		switch (type) {
		case ShareBaseTransaction.TRANS_TYPE_LOGIN:
			what = SHARE_SUCCESS_MSG;
			break;
		case ShareBaseTransaction.TRANS_TYPE_MBLOG:
			what = SHARE_MBLOG_SUCCESS_MSG;
			break;
		case ShareBaseTransaction.TRANS_TYPE_GET_FOLLOWLING_LIST:
			what = SHARE_FOLLOWING_LIST_SUCCESS_MSG;
			break;
		default:
			what = SHARE_SUCCESS_MSG;
		}
		
		mHandler.obtainMessage(what, type, tid, arg3).sendToTarget();
	}
	
}
