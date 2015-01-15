
package com.netease.common.http.filedownload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.netease.common.cache.CacheManager;
import com.netease.common.cache.file.StoreFile;
import com.netease.common.config.IConfig;
import com.netease.common.http.HttpDataChannel;
import com.netease.common.http.HttpEngine;
import com.netease.common.task.AsyncTransaction;
import com.netease.common.task.TransTypeCode;
import com.netease.common.task.TransactionEngine;
import com.netease.common.task.TransactionListener;

/**
 * 使用FileDownloadManager作为文件下载管理类型，由外部注册事务引擎方式后可使用
 * 
 * @author
 */
public class FileDownloadManager implements TransactionListener, IConfig {

    public static final int ALL_DOWNLOAD = 0x00; // 所有情况下全下
    public static final int NO_DOWNLOAD = 0x01; // 所有情况下都不下载
    public static final int WIFI_DOWNLOAD = 0x02; // WIFI 情况下下载
    public static final int CACHE_ONLY = 0x03;  //只允许获取缓存，不下载

    /************************ 以下 IConfig 配置项 *******************************/

    /**
     * 下载Http线程个数
     */
    public static int FileDownloadHttpThread = 2;

    /**
     * 下载类型 ALL_DOWNLOAD = 0x00; // 所有情况下全下 NO_DOWNLOAD = 0x01; // 所有情况下都不下载
     * WIFI_DOWNLOAD = 0x02; // WIFI 情况下下载
     */
    public static int DownloadType = ALL_DOWNLOAD;

    public static final int TYPE_FILE_DOWNLOAD_SUCCESS = -100;
    public static final int TYPE_FILE_DOWNLOAD_ERROR = -101;
    public static final int TYPE_FILE_DOWNLOAD_PROGRESS = -102;

    /**
     * 下载Http线程优先级
     */
    public static int FileDownloadHttpPriority = Thread.NORM_PRIORITY - 1;

    /************************ 以上 IConfig 配置项 *******************************/

    /**************************** 以下私有属性 ********************************/
    private static FileDownloadManager mInstance;

    private TransactionEngine mTransEngine;
    private HttpEngine mHttpEngine;
    private HttpDataChannel mHttpDataChannel;

    private Handler mHandler;

    private HashMap<String, FileDownloadTransaction> mDownloadMap;

    /**************************** 以上私有属性 ********************************/

    /**************************************************************************
     * FileDownloadManager 开放外部调用接口
     *************************************************************************/
    
    /**
     * 获取单实例
     */
    public static FileDownloadManager getInstance() {
        if (mInstance == null) {
            mInstance = new FileDownloadManager();
        }

        return mInstance;
    }

    /**
     * 下载文件
     * 
     * @param url
     * @param path
     * @param listener
     */
    public void downloadFile(String url, String path, String name,FileDownloadListener callback) {
        if (checkIsDownloading(url)) {
            return;
        }
        
        FileDownloadTransaction downloadTrans = new FileDownloadTransaction(url,
        		path, name, callback);
        mDownloadMap.put(url, downloadTrans);
        startDownloadTransaction(downloadTrans);
    }
    
    /**
     * 取消下载
     * @param url
     * @return
     */
    public boolean cancelDownload(String url){
        if(mDownloadMap.containsKey(url)){
            FileDownloadTransaction trans = mDownloadMap.get(url);
            mDownloadMap.remove(url);
            if(trans != null){
                trans.getCurrentListener().clear();
                trans.doCancel();
                return true;
            }
        }
        
        return false;
    }
    
    public ArrayList<String> cancelAllDownload(){
        ArrayList<String> cancelList = new ArrayList<String>();
        Set<Entry<String, FileDownloadTransaction>> set = mDownloadMap.entrySet();
        Iterator<Entry<String, FileDownloadTransaction>> ite = set.iterator();
        Entry<String, FileDownloadTransaction> ent;
        while(ite.hasNext()){
            ent = ite.next();
            cancelList.add(new String(ent.getKey()));
            if(ent.getValue() != null){
                ent.getValue().doCancel();
                ent.getValue().getCurrentListener().clear();
            }
        }
        
        mDownloadMap.clear();
        
        return cancelList;
    }
    
    /**
     * 指定url是否正在下载
     * @param url
     * @return
     */
    public boolean checkIsDownloading(String url) {
        return mDownloadMap.containsKey(url);
    }
    
    /**
     * 指定url的任务下载进度
     * @param url
     * @return
     */
    public int getDownloadingProgress(String url){
        FileDownloadTransaction trans = mDownloadMap.get(url);
        if(trans == null)
            return 0;
        return trans.getCurrentProgress();
    }
    
    public void addDownloadingCallback(String url, FileDownloadListener listener){
        FileDownloadTransaction trans = mDownloadMap.get(url);
        if(trans == null)
            return;
        
        trans.addListener(listener);
    }
    
    public void removeDownloadingCallback(String url, FileDownloadListener listener){
        FileDownloadTransaction trans = mDownloadMap.get(url);
        if(trans == null)
            return;
        
        trans.removeListener(listener);
    }
    
    /**
     * 指定url是否已经下载
     * @param url
     * @return
     */
    public boolean checkIsDownloaded(String url){
        StoreFile storeFile = CacheManager.getStoreFile(url);
        if (storeFile != null && storeFile.exists()) {
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * 指定url的缓存文件
     * @param url
     * @return
     */
    public StoreFile getCacheFile(String url){
        StoreFile storeFile = CacheManager.getStoreFile(url);
        if (storeFile != null && storeFile.exists()) {
            return storeFile;
        }
        else {
            return null;
        }
    }

    /*************************************************************************/

    public FileDownloadManager() {
        mHttpEngine = new HttpEngine(FileDownloadHttpThread, FileDownloadHttpPriority);
//        HttpEngine.CheckAcceptRange = false;
        mHandler = new InternalHandler(Looper.getMainLooper());
        mTransEngine = TransactionEngine.Instance();
        mHttpDataChannel = new HttpDataChannel(mTransEngine, mHttpEngine);

        mDownloadMap = new HashMap<String, FileDownloadTransaction>();
    }

    private static final int TRANSACTION_ERROR = 0x100;
    protected static class InternalHandler extends Handler {

        public InternalHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case TRANSACTION_ERROR:{
                    FileResult result = (FileResult)msg.obj;
                    if(result.mCallback == null)
                        return;
                    for(FileDownloadListener listener : result.mCallback){
                        listener.onFailed(result.mError, result.errCode);
                    }
                }
                    break;
                case TYPE_FILE_DOWNLOAD_SUCCESS:{
                    FileResult result = (FileResult)msg.obj;
                    if(result.mCallback == null)
                        return;
                    for(FileDownloadListener listener : result.mCallback){
                        listener.onSuccess(result.mStoreFile.getPath());
                        listener.onProgress(0, 0, 100, 0);
                    }
                    break;
                }
                case TYPE_FILE_DOWNLOAD_ERROR:{
                    Object[] o = (Object[])msg.obj;
                    FileResult result = (FileResult)o[0];
                    String url = (String)o[1];
                    int errorType = (Integer)o[2];
                    if(result.mCallback == null)
                        return;
                    String error = "";
                    switch(errorType){
                        case NO_DOWNLOAD:
                            error = "禁止下载";
                            break;
                        case WIFI_DOWNLOAD:
                            error = "只允许wifi下载";
                            break;
                        case CACHE_ONLY:
                            error = "只允许获取本地缓存";
                            break;
                    }
                    for(FileDownloadListener listener : result.mCallback){
                        listener.onFailed(error, result.errCode);
                    }
                }
                    break;
                case TYPE_FILE_DOWNLOAD_PROGRESS:{
                    Object[] o = (Object[])msg.obj;
                    FileResult result = (FileResult)o[0];
                    long current = (Long)o[1];
                    long total = (Long)o[2];
                    int percent = (Integer)o[3];
                    int speed = (Integer)o[4];
                    if(result.mCallback == null)
                        return;
                    for(FileDownloadListener listener : result.mCallback){
                        listener.onProgress(current, total, percent, speed);
                    }
                }
                    break;
            }
        }
    }

    private void startDownloadTransaction(FileDownloadTransaction trans) {
        if (trans != null) {
            trans.setListener(this);
            beginTransaction(trans);
        }
    }

    private void beginTransaction(AsyncTransaction trans) {
        if (mTransEngine != null) {
            trans.setDataChannel(mHttpDataChannel);
            mTransEngine.beginTransaction(trans);
        }
    }

    /****************************** 文件下载回调处理 ****************************/

    @Override
    public void onTransactionMessage(int code, int type, int tid, Object arg3) {
        if (type == TransTypeCode.TYPE_FILE_DOWNLOAD
                && code == TYPE_FILE_DOWNLOAD_SUCCESS
                && arg3 != null && arg3 instanceof FileResult) {
            FileResult result = (FileResult)arg3;

            if (result.mCallback != null) {
                mHandler.obtainMessage(code, type, tid, result).sendToTarget();
            }

            if(mDownloadMap != null){
                mDownloadMap.remove(result.mUrl);
            }
        } else if (type == TransTypeCode.TYPE_FILE_DOWNLOAD
                && code == TYPE_FILE_DOWNLOAD_PROGRESS
                && arg3 != null && arg3 instanceof Object[]) {
            Object[] o = (Object[])arg3;
            FileResult result = (FileResult)o[0];

            if (result != null && result.mCallback != null) {
                mHandler.obtainMessage(code, type, tid, arg3).sendToTarget();
            }

        } else if (type == TransTypeCode.TYPE_FILE_DOWNLOAD
                && code == TYPE_FILE_DOWNLOAD_ERROR
                && arg3 != null && arg3 instanceof Object[]) {
            Object[] o = (Object[])arg3;
            FileResult result = (FileResult)o[0];
            String url = (String)o[1];
            int errorType = (Integer)o[2];

            if (result.mCallback != null) {
                mHandler.obtainMessage(code, type, tid, arg3).sendToTarget();
            }

            if(mDownloadMap != null){
                mDownloadMap.remove(result.mUrl);
            }
        }
    }

    @Override
    public void onTransactionError(int errCode, int type, int tid, Object err) {
        if (type == TransTypeCode.TYPE_FILE_DOWNLOAD
                && err != null && err instanceof FileResult){
            FileResult result = (FileResult)err;
            if(mDownloadMap != null){
                mDownloadMap.remove(result.mUrl);
            }
            result.mError = errorToString(errCode);
            if (result.mCallback != null) {
                mHandler.obtainMessage(TRANSACTION_ERROR, type, tid, result).sendToTarget();
            }
        }
        
    }
    
    class ResultInfo {
        public int mcode = -1;
        public Object mObject = null;

        public ResultInfo(int code, Object object) {
            // TODO Auto-generated constructor stub
            mcode = code;
            mObject = object;
        }
    }

    
    public String errorToString(int code){
        return "error";
    }
    
}
