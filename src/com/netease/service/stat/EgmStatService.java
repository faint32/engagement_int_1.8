package com.netease.service.stat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.netease.service.protocol.EgmService;

public class EgmStatService {

	private static final int MSG_STORE_LOG = 0x01; // store logs to local file
	private static final int MSG_SEND_LOG = 0x02; // send logs to remote server
	
	protected static final String SPLIT = "\u0001";
	
	protected static final String OS = "android";
	
	protected static final String TIME = "egm_time";
	
	private static long mUserId;
	
	private static List<JSONObject> mLogs = Collections.synchronizedList(
			new LinkedList<JSONObject>());
	
	private static Handler mHandler;
	
	public static void init(long userid) {
		mUserId = userid;
		mLogs.clear();
		
		if (mHandler == null) {
			mHandler = new InternalHandler(Looper.getMainLooper());
		}
		else {
			mHandler.removeCallbacksAndMessages(null);
		}
	}
	
	/**
	 * 需要在ui执行，减少同步判断
	 * @param json
	 */
	protected static void log(JSONObject json) {
		try {
			json.put(TIME, System.currentTimeMillis());
		} catch (Exception e) {
		}
		
		mLogs.add(json);
		
		if (mLogs.size() > 30) {
			storeLogs();
		}
		
		if (mHandler != null) {
			if (! mHandler.hasMessages(MSG_STORE_LOG)) {
				mHandler.sendEmptyMessageDelayed(MSG_STORE_LOG, 30000);
			}
			if (! mHandler.hasMessages(MSG_SEND_LOG)) {
				mHandler.sendEmptyMessageDelayed(MSG_SEND_LOG, 300000);
			}
		}
	}
	
	private static void storeLogs() {
		if (mLogs.size() > 0) {
			List<JSONObject> list = new ArrayList<JSONObject>(mLogs);
			mLogs.clear();
			
			EgmService.getInstance().beginTransaction(new EgmStatTransaction(
					false, mUserId, list));
		}
	}

	private static void sendLogs() {
		List<JSONObject> list = new ArrayList<JSONObject>(mLogs);
		
		if (mLogs.size() > 0) {
			mLogs.clear();
		}
		
		EgmService.getInstance().beginTransaction(new EgmStatTransaction(
				true, mUserId, list));
	}
	
	private static class InternalHandler extends Handler {
		
		public InternalHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_STORE_LOG:
				storeLogs();
				break;
				
			case MSG_SEND_LOG:
				sendLogs();
				break;
			}
		}
	}
	
}
