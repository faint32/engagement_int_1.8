package com.netease.common.share.base;

import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.netease.common.share.ShareBind;
import com.netease.common.share.ShareResult;
import com.netease.common.share.ShareService;
import com.netease.common.share.ShareType;
import com.netease.common.share.bind.ShareWebView;
import com.netease.common.task.Transaction;
import com.netease.common.task.TransactionListener;

public abstract class ShareBaseChannel {
	
	private int mTransId;
	
	public ShareBaseChannel() {
		
	}
	
	protected void setTransId(int transId) {
		mTransId = transId;
	}
	
	public void cancel() {
		ShareService.getShareService().cancelTransaction(mTransId);
	}
	
	public int beginTransaction(Transaction trans) {
		mTransId = trans.getId();
		trans.setListener(mListener != null ? mListener.get() : null);
		
		return ShareService.getShareService().beginTransaction(trans);
	}

	/**
	 * 获取 ClientID
	 * 
	 * @return
	 */
	public abstract String getClientID();
	
	/**
	 * 获取Client Secret
	 * 
	 * @return
	 */
	public abstract String getClientSecret();
	
	/**
	 * 获取授权URL
	 * 
	 * @return
	 */
	public abstract String getAuthorizeUrl(ShareWebView webVuew);
	
	/**
	 * 获取Access Token使用的Url
	 * 
	 * @return
	 */
	public abstract String getAccessTokenUrl();
	
	/**
	 * 返回重定向前缀判断匹配URL
	 * 
	 * @return
	 */
	public abstract String getRedirectPrefix(); 
	
	/**
	 * 获取用户信息
	 * @return
	 */
	public abstract String getUserShowUrl();
	
	/**
	 * 获取发送微博的Url
	 * @return
	 */
	public abstract String getSendMBlogUrl();
	
	/**
	 * 获取关注列表Url
	 * @return
	 */
	public abstract String getFollowingListUrl();
	
	/**
	 * 匹配成功后数据
	 * 
	 * @param url 重定向URL
	 * @return 非空时表示出错，一般使用Toast等提示
	 */
	public abstract String onRedirectUrl(String url);
	
	/**
	 * 获取当前的ShareType
	 * @return
	 */
	public abstract ShareType getShareType();
	
	/**
	 * 分享
	 * 
	 * @param content 
	 * @param imgPath 图片的本地地址
	 * @return transaction id
	 */
	public abstract int sendMBlog(String title, String content, 
			String imgPath, String url);
	
	/**
	 * 分享
	 * 
	 * @param shareBind
	 * @param title
	 * @param content
	 * @param imgPath
	 * @param url
	 * @return
	 */
	public abstract int sendMBlog(ShareBind shareBind, String title, 
			String content, String imgPath, String url);
	
	/**
	 * 获取关注列表
	 * 
	 * @return
	 */
	public abstract int getFollowingList(ShareBind shareBind);
	
	/**
	 * 
	 * @param errCode
	 * @param msg
	 * @return
	 */
	public abstract ShareResult getErrorShareResult(int errCode, Object msg);
	
	/**
	 * 获取ShareBind
	 * @return
	 */
	public ShareBind getShareBind() {
		ShareBind shareBind = new ShareBind(getShareType());
		shareBind.setAccessToken(getAccessToken());
		shareBind.setRefreshToken(getRefreshToken());
		shareBind.setExpires(mExpires);
		
		return shareBind;
	}
	
	/**
	 * 
	 * @param accessToken
	 * @param refreshToken
	 * @param expires ms 秒级
	 */
	public void setToken(String accessToken, String refreshToken, long expires) {
		mAccessToken = accessToken;
		mRefreshToken = refreshToken;
		
		if (expires > 0) {
			mExpires = System.currentTimeMillis() + expires * 1000 - 600 * 1000; // 多减10分钟 
		} else {
			mExpires = -1;
		}
	}
	
	/**
	 * 获取有效的Access Token
	 */
	public String getAccessToken() {
		return mAccessToken;
	}
	
	/**
	 * 获取Refresh Token
	 * @return
	 */
	public String getRefreshToken() {
		return mRefreshToken;
	}
	
	public long getExpires() {
		return mExpires;
	}
	
	private String mAccessToken;
	private String mRefreshToken;
	private long mExpires;
	
	protected WeakReference<TransactionListener> mListener;
	
	/**
	 * Share Transaction Listener
	 * @param listener
	 */
	public void setShareListener(TransactionListener listener) {
		mListener = new WeakReference<TransactionListener>(listener);
	}
	
	/**
	 * 获取JSONObject
	 * 
	 * @param obj
	 * @return
	 */
	protected static JSONObject getJSONObject(Object obj) {
		JSONObject json = null;
		
		if (obj != null) {
			if (obj instanceof JSONObject) {
				json = (JSONObject) obj;
			} else if (obj instanceof String){
				try {
					json = new JSONObject((String) obj);
				} catch (JSONException e) {
				}
			}
		}
		
		return json;
	}
	
	/**
	 * 
	 * @param url
	 * @return
	 */
	protected static Map<String, String> parseUrlParams(String url) {
		int index = url.indexOf('#');
		if (index < 0) {
			index = url.indexOf('?');
		}
		
		HashMap<String, String> map = null;
		
		if (index > 0) {
			StringTokenizer st = new StringTokenizer(
					url.substring(index + 1), "&" );
			
			map = new HashMap<String, String>();
			
			String key = null, value = null;
			
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				index = token.indexOf('=');
				if (index < 0) {
					key = token;
					value = null; 
				} else {
					key = token.substring(0, index);
					value = URLDecoder.decode(token.substring(index + 1)); 
				}
				key = URLDecoder.decode(key);
				
//				if (! map.containsKey(key)) {
				map.put(key, value);	
//				}
			}
		}
		
		return map;
	}
	
	/**
	 * 从Url中获取code
	 * 
	 * @param url
	 * @return
	 */
	protected static String getCodeFromUrl(String url) {
		String code = null;
		
		Map<String, String> params = parseUrlParams(url);
		
		if (params != null) {
			code = params.get("code");
		}
		
		return code;
	}
	
	/**
	 * 从Url中获取access_token
	 * 
	 * @param url
	 * @return
	 */
	protected static String getAccessTokenFromUrl(String url) {
		String token = null;
		
		Map<String, String> params = parseUrlParams(url);
		
		if (params != null) {
			token = params.get("access_token");
		}
		
		return token;
	}
	
	/**
	 * 一般用于AuthorizeUrl拼接
	 * @param prefix
	 * @param params
	 * @return
	 */
	protected static String getUrl(String prefix, List<NameValuePair> params) {
		if (params != null && params.size() > 0) {
			if (prefix.indexOf('?') > -1) {
				prefix += '&';
			} else {
				prefix += '?';
			}
			
			prefix += URLEncodedUtils.format(params, "utf-8");
		}
		
		return prefix;
	}
}
