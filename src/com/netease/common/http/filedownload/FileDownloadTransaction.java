package com.netease.common.http.filedownload;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.netease.common.cache.file.StoreDir;
import com.netease.common.cache.file.StoreFile;
import com.netease.common.http.THttpRequest;
import com.netease.common.http.cache.HttpCache;
import com.netease.common.nio.NioListener;
import com.netease.common.service.BaseService;
import com.netease.common.task.AsyncTransaction;
import com.netease.common.task.NotifyTransaction;
import com.netease.common.task.TransTypeCode;
import com.netease.util.PlatformUtil;

/**
 *
 */
public class FileDownloadTransaction extends AsyncTransaction {

	private String mUrl;
    private ArrayList<FileDownloadListener> mCallback;
    
    private long mTotal, mCurrent;
    private int mSpeed, mPercent, mLastmPercent;
    private long mLastNotifyTime;
    
    private FileResult mResult;
    
    private String mPath ;
    
    private String mName ;
	
	public FileDownloadTransaction(String url, String path, String name,FileDownloadListener callback) {
		super(TransTypeCode.TYPE_FILE_DOWNLOAD);
		
		mPath = path ;
		
		mName = name;
		
		mCallback = new ArrayList<FileDownloadListener>();
		
		mUrl = url;
		mCallback.add(callback);
	}
	
	@Override
	protected void onTransactionError(int errCode, Object obj) {
		mResult.errCode = errCode;
		notifyError(errCode, mResult);
	}

	@Override
	protected void onTransactionSuccess(int code, Object obj) {
		if (obj != null && obj instanceof FileResult) {
		    FileResult result = (FileResult) obj;
			result = new FileResult(result);
			if(!TextUtils.isEmpty(mName)){
			    result.mStoreFile.renameTo(new File(mPath, mName));
			    result.mStoreFile = new StoreFile(new StoreDir(mPath),mName);
			}
			notifySuccess(result);
		} else {
			notifyError(0, null);
		}
	}
	
	@Override
	public void onTransact() {
        if (mUrl != null) {
            if (URLUtil.isFileUrl(mUrl)) {
                String path = mUrl.substring("file://".length());
                File file = new File(path);
                StoreFile sf = new StoreFile(new StoreDir(file.getPath()), file.getName());
                mResult = new FileResult();
                mResult.mCallback.addAll(mCallback);
                mResult.mStoreFile = sf;
                mResult.mUrl = mUrl;
                
                notifySuccess(mResult);
            }
            else {
                if(!TextUtils.isEmpty(mName)){
                    File file = new File(mPath + "/" + mName);
                    if(file!= null && file.exists()){
                        mResult = new FileResult();
                        mResult.mCallback.addAll(mCallback);
                        mResult.mStoreFile = new StoreFile(new StoreDir(mPath),mName);
                        mResult.mUrl = mUrl;
                        notifySuccess(mResult);
                        return;
                    }
                } 
                
//                    StoreFile storeFile = CacheManager.getStoreFile(mUrl);
//                    if (storeFile != null && storeFile.exists()) {
//                        mResult = new FileResult();
//                        mResult.mCallback.addAll(mCallback);
//                        mResult.mStoreFile = storeFile;
//                        mResult.mUrl = mUrl;
//                        notifySuccess(mResult);
//                    } else {
                        mResult = new FileResult();
                        mResult.mCallback.addAll(mCallback);
                        mResult.mUrl = mUrl;
                        sendRequest(getHttpRequest());
//                    }
            }
        }
        else {
            doEnd();
        }
    }
	
	@Override
	protected void sendRequest(Object obj) {
        Context context = BaseService.getServiceContext();
        
        switch (FileDownloadManager.DownloadType) {
            case FileDownloadManager.NO_DOWNLOAD:
                notifyError(mUrl, FileDownloadManager.NO_DOWNLOAD);
                return;
            case FileDownloadManager.WIFI_DOWNLOAD:
                if (!PlatformUtil.isWifiNetWork(context)) {
                    notifyError(mUrl, FileDownloadManager.WIFI_DOWNLOAD);
                    return;
                }
                break;
            case FileDownloadManager.CACHE_ONLY:
                notifyError(mUrl, FileDownloadManager.CACHE_ONLY);
                return;
        }
        
        super.sendRequest(obj);
    }
	
	private THttpRequest getHttpRequest() {
		THttpRequest request = new THttpRequest(mUrl) {
			@Override
			public String onRedirectUrl(String url, THttpRequest request) {
				return super.onRedirectUrl(url, request);
			}
		};
		
		request.setCacheFile();
		request.setCacheDatabase();
		request.setNioListener(new NioListener() {
            
            @Override
            public void onSpeedChange(byte type, int speed) {
                mSpeed = speed;
            }
            
            @Override
            public void onSizeIncrease(byte type, long size) {
                mCurrent += size;
                
                if(mTotal != 0){
                    mPercent = (int)(100 * mCurrent / mTotal);
                }
                
                if((mPercent - mLastmPercent) > 4
                        || mLastNotifyTime == 0
                        || (System.currentTimeMillis() - mLastNotifyTime) > 1000){
                    notifyProgress(mCurrent, mTotal, mPercent, mSpeed);
                    mLastmPercent = mPercent;
                    mLastNotifyTime = System.currentTimeMillis();
                }
            }
            
            @Override
            public void onContentLength(byte type, long length) {
                mTotal = length;
                
                notifyProgress(mCurrent, mTotal, mPercent, mSpeed);
            }
        });
		
		return request;
	}
	private long tmp;
	
	private void notifySuccess(FileResult fr) {
		notifyMessage(FileDownloadManager.TYPE_FILE_DOWNLOAD_SUCCESS, fr);
		doEnd();
		clear();
	}
	private void notifyError(String url, int type){
        mResult = new FileResult();
        mResult.mCallback.addAll(mCallback);
        mResult.mUrl = mUrl;
	    Object[] o = new Object[]{
	            mResult,
	            url,
	            type
	    };
        notifyMessage(FileDownloadManager.TYPE_FILE_DOWNLOAD_ERROR, o);
        doEnd();
        clear();
	}
	private void notifyProgress(long current, long total, int percent, int speed){
	    Object[] o = new Object[]{
	        mResult,
            current, total, percent, speed
	    };
        notifyMessage(FileDownloadManager.TYPE_FILE_DOWNLOAD_PROGRESS, o);
	}
	
	
	private class DownloadNotifyTransaction extends NotifyTransaction {
        
        public DownloadNotifyTransaction(AsyncTransaction tran, Object data,
                int type, int code) {
            super(tran, data, type, code);
        }

        public DownloadNotifyTransaction(List<AsyncTransaction> trans,
                Object data, int type, int code) {
            super(trans, data, type, code);
        }
        
        @Override
        public void doBeforeTransact() {
            if (isSuccessNotify()) {
                StoreFile storeFile = null;
                
                Object data = getData();
                if (data != null) {
                    if (data instanceof HttpCache) {
                        storeFile = ((HttpCache) data).LocalFile;
                    } else {
                        setNotifyTypeAndCode(NOTIFY_TYPE_ERROR, 0);
                    }
                } else {
                    setNotifyTypeAndCode(NOTIFY_TYPE_ERROR, 0);
                }

                mResult.mStoreFile = storeFile;
                
                resetData(mResult);
            }
        }
    }

	@Override
	public NotifyTransaction createNotifyTransaction(Object data,
			int notifyType, int code) {
		return new DownloadNotifyTransaction(this, data, notifyType, code);
	}
	
	@Override
	public NotifyTransaction createNotifyTransaction(
			List<AsyncTransaction> trans, Object data, int notifyType,
			int code) {
		return new DownloadNotifyTransaction(trans, data, notifyType, code);
	}
	
	public int getCurrentProgress(){
	    return mPercent;
	}
	public ArrayList<FileDownloadListener> getCurrentListener(){
	    return mCallback;
	}
	public void addListener(FileDownloadListener listener){
	    mCallback.add(listener);
	    mResult.mCallback.add(listener);
	}
    public void removeListener(FileDownloadListener listener){
        mCallback.remove(listener);
        mResult.mCallback.remove(listener);
    }
	
	public void clear(){
	    if(mCallback != null)
	        mCallback.clear();
	    mCallback = null;
	    mResult = null;
	}
}
