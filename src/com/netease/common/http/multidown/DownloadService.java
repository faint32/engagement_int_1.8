package com.netease.common.http.multidown;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.netease.common.config.IConfig;

public class DownloadService extends Service implements IConfig, DownloadListener {
	
	private static final String TAG = "DownloadService";

	/************************以下 IConfig可配置项******************************/
	// 下载线程个数
	public static int DownloadThreadCount = 3;
	
	// 最小多线程下载文件大小
	public static int MinMultiDownloadSize = 4 * 1024; // 100KB
	
	/************************以上 IConfig可配置项******************************/
	
	public static final String ACTION_START_DOWNLOAD = "start";
	public static final String ACTION_STOP_DOWNLOAD = "stop";
	
	public static final String DOWNLOAD_TASK = "task";
	
	private HashMap<String, DownloadState> mDownloadingMap 
		= new HashMap<String, DownloadState>();
	private LinkedList<DownloadState> mWaitingTasks 
		= new LinkedList<DownloadState>();
	
	ThreadPoolExecutor mPoolExecutor;
	LinkedBlockingQueue<Runnable> mBlockingQueue;
	
	private static HashSet<DownloadListener> mListeners = new HashSet<DownloadListener>();
	
	public static void registerDownloatListener(DownloadListener listener) {
		if (listener != null) {
			mListeners.add(listener);
		}
	}
	
	public static void unRegisterDownloatListener(DownloadListener listener) {
		mListeners.remove(listener);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.e(TAG, "onCreate ");
		
		mBlockingQueue = new LinkedBlockingQueue<Runnable>();
		mPoolExecutor = new ThreadPoolExecutor(DownloadThreadCount, 
				DownloadThreadCount, 1000, TimeUnit.MICROSECONDS, mBlockingQueue);
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		Log.e(TAG, "onStart " + intent);
		
		if (intent != null && intent.getAction() != null) {
			String action = intent.getAction().intern();
			DownloadTask task = intent.getParcelableExtra(DOWNLOAD_TASK);
			if (action == ACTION_START_DOWNLOAD) {
				startDownload(task);
			} else if (action == ACTION_STOP_DOWNLOAD) {
				stopDownload(task);
			}
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		Log.e(TAG, "onDestroy");
		
		if (mPoolExecutor != null) {
			mPoolExecutor.shutdown();
			mPoolExecutor = null;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.e(TAG, "obBind " + intent);
		if (intent != null && intent.getAction() != null) {
			String action = intent.getAction().intern();
			DownloadTask task = intent.getParcelableExtra(DOWNLOAD_TASK);
			if (action == ACTION_START_DOWNLOAD) {
				startDownload(task);
			} else if (action == ACTION_STOP_DOWNLOAD) {
				stopDownload(task);
			}
		}
		return null;
	}

	private void startDownload(DownloadTask task) {
		Log.e(TAG, "startDownload url: " + task.getUrl());
		if (mDownloadingMap.containsKey(task.getUrl())) {
			return ;
		}
		
		DownloadState state = DownloadPreference.getDownloadState(getApplicationContext(), task.getUrl());
		if (state != null && new File(state.mTargetPath + "_tmp").length() == state.mTotalSize) {
			notifyDownloadState(state);
		} else {
			state = new DownloadState();
			state.mUrl = task.getUrl();
			state.mTargetPath = task.getTargetPath();
		}
		
		state.mTitle = task.getTitle();

		mDownloadingMap.put(state.mUrl, state);
		
		DownloadRunnable runnable = new DownloadRunnable(getApplicationContext(),
        		mPoolExecutor, state, -1, this);
		mPoolExecutor.execute(runnable);
	}
	
	private void stopDownload(DownloadTask task) {
		Log.e(TAG, "stopDownload url: " + task.getUrl());
		
		DownloadState state = mDownloadingMap.remove(task.getUrl());
		if (state != null) {
			state.doCancel();
			notifyDownloadState(state);
		}
	}

	@Override
	public void notifyDownloadState(DownloadState state) {
		Log.e(TAG, "DownJson: " + state.toJSONString());
		mHandler.sendMessage(mHandler.obtainMessage(0, state));
	}
	
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			DownloadState state = (DownloadState) msg.obj;
			List<DownloadListener> list = new LinkedList<DownloadListener>();
			list.addAll(mListeners);
			if (list.size() > 0) {
				for (DownloadListener listener : list) {
					listener.notifyDownloadState(state);
				}
			}
		}
	};
	
}
