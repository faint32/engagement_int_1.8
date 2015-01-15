package com.netease.common.http;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.utils.URLEncodedUtils;

import android.net.Uri;

import com.netease.common.cache.file.StoreTmpFileInfo;
import com.netease.common.http.cache.HttpCache;
import com.netease.common.nio.NioListener;
import com.netease.util.priority.Priority;

public class THttpRequest implements Priority, Comparable<THttpRequest> {
	
	private static int NextRequestID = 0;
	
	int mRequestID;
	
	THttpMethod mMethod;
	String mUrl;
	HttpEntity mHttpEntity; // POST DELETE PUT
	HttpCallBack mHttpCallBack;
	
	int mPriority;
	int mGroupID;
	int mRetryCount;
	
	List<THttpHeader> mParameters;
	List<THttpHeader> mHeaders;
	
	String mTmpUrl;
	String mUrlLocalParam;
	
	int mTransacionType;
	String mCacheUrl;
	
	private static final int STATE_CANCELED = 0x01;
	private static final int STATE_CACHE_FILE= 0x02; // 基于文件存在，判断是否缓存有效
	private static final int STATE_CACHE_DATABASE = 0x04; // 基于缓存是否过期判断缓存是否有效
	private static final int STATE_CACHE_GZIP = 0x08; // 是否cache使用gzip方式
	
	int mState;
	
	NioListener mNioListener;
	
	/*************************************************************************
	 * 以下多线程断点续传相关
	 ************************************************************************/
	
	THttpRequest mParentRequest;
	HttpCache mHttpCache;
	StoreTmpFileInfo mStoreTmpFileInfo;
	long mRangeStart;
	long mRangeEnd;
	AtomicBoolean mNotified = new AtomicBoolean(true);
	
	public THttpRequest(THttpRequest request) {
		mRequestID = getNextRequestID();
		
		mMethod = request.mMethod;
		mUrl = request.mUrl;
		mTmpUrl = request.mUrl;
		
		mParentRequest = request;
	}
	
	public void setStoreTmpFileInfo(StoreTmpFileInfo tmpInfo) {
		mStoreTmpFileInfo = tmpInfo;
	}
	
	public void setHttpCache(HttpCache httpCache) {
		mHttpCache = httpCache;
	}
	
	public HttpCache getHttpCache() {
		if (mParentRequest != null) {
			return mParentRequest.getHttpCache();
		}
		
		return mHttpCache;
	}
	
	public THttpRequest getParentRequest() {
		return mParentRequest;
	}
	
	public void addRangeStart(int count) {
		mRangeStart += count;
	}
	
	public boolean shouldNotify() {
		if (mParentRequest != null) {
			return mParentRequest.shouldNotify();
		}
		
		return mNotified.compareAndSet(true, false); 
	}
	
	public StoreTmpFileInfo getStoreTmpFileInfo() {
		if (mParentRequest != null) {
			return mParentRequest.getStoreTmpFileInfo();
		}
		
		return mStoreTmpFileInfo;
	}
	
	public THttpRequest requestNextRequest(int defaultSize) {
		if (mParentRequest != null) {
			return mParentRequest.requestNextRequest(defaultSize);
		}
		
		THttpRequest request = null;
		
		if (mStoreTmpFileInfo != null) {
			long[] seeks = mStoreTmpFileInfo.requestNextFragement(defaultSize);
			
			if (seeks != null) {
				request = new THttpRequest(this);
				request.mRangeStart = seeks[0];
				request.mRangeEnd = seeks[1];
			}
		}
		
		return request;
	}
	
	/*************************************************************************
	 * 以上多线程断点续传相关
	 ************************************************************************/
	
	public THttpRequest(String url) {
		this(url, THttpMethod.GET);
	}
	
	public THttpRequest(String url, THttpMethod type) {
		if (url == null || url.length() == 0) {
			throw new IllegalArgumentException("url");
		}
		
		mMethod = type;
		mUrl = url;
		mTmpUrl = url;
		mRequestID = getNextRequestID();
	}
	
	public boolean isDivided() {
		return mParentRequest != null;
	}
	
	public long getRangeStart() {
		return mRangeStart;
	}
	
	public long getRangeEnd() {
		return mRangeEnd;
	}
	
	private void setState(int state, boolean value) {
		if (value) {
			mState |= state;
		}
		else {
			mState &= ~ state;
		}
	}
	
	private boolean isState(int state) {
		return (mState & state) != 0;
	}
	
	public int getRequestID() {
		return mRequestID;
	}
	
	public void addParameter(String key, String value) {
		if (key == null || value == null) {
			return ;
		}
		if (mParameters == null) {
			mParameters = new LinkedList<THttpHeader>();
		}
		
		mParameters.add(new THttpHeader(key, value));
	}
	
	public String onRedirectUrl(String url, THttpRequest request) {
		return url;
	}
	
	protected List<THttpHeader> getHttpParamters() {
		return mParameters;
	}
	
	public void setGroupID(int groupID) {
		mGroupID = groupID;
	}
	
	protected String getUrlLocalParam() {
		if (mUrlLocalParam == null) {
			return "";
		}
		return mUrlLocalParam;
	}

	public void setUrlLocalParam(String param) {
		mUrlLocalParam = param;
	}

	@Override
	public int getGroupID() {
		return mGroupID;
	}
	
	@Override
    public void setPriority(int priority) {
		mPriority = priority;
	}
	
	@Override
    public int getPriority() {
		return mPriority;
	}
	
	public void setHttpCallBack(HttpCallBack callBack) {
		mHttpCallBack = callBack;
	}
	
	public HttpCallBack getHttpCallBack() {
		return mHttpCallBack;
	}
	
	public String getUrl() {
		return mTmpUrl;
	}
	
	/**
     * 设置用于计算缓存路径的url
     * @param cacheUrl
     */
    public void setCacheUrl(String cacheUrl) {
        mCacheUrl = cacheUrl;
    }
    
    public String getCacheUrl() {
        if (mCacheUrl != null) {
            return mCacheUrl + getUrlLocalParam();
        }
        
        return getUrl() + getUrlLocalParam();
    }
    
    /**
     * 实际请求地址
     * @return
     */
    public String getRequestUrl() {
    	return getUrl();
    }
	
	public String getHost() {
		return Uri.parse(mUrl).getHost();
	}
	
	public THttpMethod getMethod() {
		return mMethod;
	}
	
	public String getMethodName() {
		String method = "GET";
		switch (mMethod) {
		case GET:
			break;
		case POST:
			method = "POST";
			break;
		case DELETE:
			method = "DELETE";
			break;
		case HEAD:
			method = "HEAD";
			break;
		case OPTIONS:
			method = "OPTIONS";
			break;
		case PUT:
			method = "PUT";
			break;
		}
		return method;
	}
	
	public HttpEntity getHttpEntity() {
	    if (mHttpEntity == null && mMethod == THttpMethod.POST
                && mParameters != null && mParameters.size() > 0) {
            try {
                return new UrlEncodedFormEntity(mParameters, "utf-8");
            } catch (Exception e) {
            }
        }
		return mHttpEntity;
	}
	
	public void setHttpEntity(HttpEntity entity) {
		mHttpEntity = entity;
	}
	
	public void addHeader(String key, String value) {
		if (key == null || key.length() == 0
				|| value == null) {
			return ;
		}
		
		if (mHeaders == null) {
			mHeaders = new LinkedList<THttpHeader>();
		}
		
		mHeaders.add(new THttpHeader(key, value));
	}
	
	public List<THttpHeader> getRequestHeaders() {
		return mHeaders;
	}
	
	public void doCancel() {
		setState(STATE_CANCELED, true);
	}
	
	public boolean isCancel() {
		return mParentRequest == null ? isState(STATE_CANCELED) 
				: mParentRequest.isCancel();
	}
	
	public void setCacheFile() {
		setState(STATE_CACHE_FILE, true);
	}
	
	public boolean isCacheFile() {
		if (mParentRequest != null) {
			return mParentRequest.isCacheFile();
		}
		return isState(STATE_CACHE_FILE);
	}
	
	public void setCacheGzip() {
		setState(STATE_CACHE_GZIP, true);
	}
	
	public boolean isCacheGzip() {
		return isState(STATE_CACHE_FILE) && isState(STATE_CACHE_GZIP);
	}
	
	public void setCacheDatabase() {
		setState(STATE_CACHE_DATABASE, true);
	}
	
	public boolean isCacheDatabase() {
		if (mParentRequest != null) {
			return mParentRequest.isCacheDatabase();
		}
		return isState(STATE_CACHE_DATABASE);
	}
	
	public void doBefore() { // 在getUrl之前执行
        if (mParameters != null && mUrl != null) {
            if (mParameters.size() > 0 && 
                    ! (mMethod == THttpMethod.POST && mHttpEntity == null)) {
                StringBuilder builder = new StringBuilder(mUrl);
                if (mUrl.indexOf('?') < 0) {
                    builder.append('?');
                }
                else {
                    builder.append('&');
                }
                
                builder.append(URLEncodedUtils.format(mParameters, "utf-8"));
                
                mTmpUrl = builder.toString();
            }
            else {
                mTmpUrl = mUrl;
            }
        }
    }
	
	public void setNioListener(NioListener nioListener) {
		mNioListener = nioListener;
	}
	
	public NioListener getNioListener() {
		if (mParentRequest != null) {
			return mParentRequest.getNioListener();
		}
		return mNioListener;
	}
	
	public int getTryTimes() {
		return 2;
	}
	
	public void incTryFailed() {
		
	}

	public THttpHeader getAuthHeader(THttpResponse response) {
		return null;
	}

	public synchronized static int getNextRequestID() {
		if (NextRequestID >= Short.MAX_VALUE) {
			NextRequestID = 0;
		}
		
		return ++NextRequestID;
	}

	@Override
	public int compareTo(THttpRequest another) {
		int ret = -1;
		if (mPriority < another.mPriority) {
			ret = -1;
		} else if (mPriority > another.mPriority) {
			ret = 1;
		} else {
			ret = 0;
		}
		
		return ret;
	}
	
}
