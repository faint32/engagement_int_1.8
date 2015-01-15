package com.netease.common.share.sohu;

import java.util.Map;

import org.json.JSONObject;

import android.text.TextUtils;

import com.netease.common.share.ShareBind;
import com.netease.common.share.ShareResult;
import com.netease.common.share.ShareType;
import com.netease.common.share.base.ShareBaseChannel;
import com.netease.common.share.bind.ShareWebView;

public class ShareChannelSohu extends ShareBaseChannel {

	/******************************************************************
	 * IConfig 配置项
	 *****************************************************************/
	
	/**
	 * Client ID
	 */
	public static String CLIENT_ID = "06OLahOS0IQB9l5OT0uB";
	
	/**
	 * Client Secret
	 */
	public static String CLIENT_SECRET = "Kh7c#n8I3N1OpkyYgK*PXr4^ID5!9B(PWkMPla5E";
	
	/**
	 * Redirect Uri
	 */
	public static String REDIRECT_URI = "http://weibotool.yuedu.163.com/callback";
	
	
	/******************************************************************
	 * 
	 *****************************************************************/
	
	private static final String HOST = "https://api.t.sohu.com";
	
	private static final String AUTHORIZE = "/oauth2/authorize";
	private static final String ACCESS_TOKEN = "/oauth2/access_token";
	private static final String USER_SHOW = "/users/show.json";
	private static final String SEND_MBLOG = "/statuses/update.json";
	private static final String SEND_PIC_MBLOG = "/statuses/upload.json";
	
	@Override
	public String getClientID() {
		return CLIENT_ID;
	}
	
	@Override
	public String getClientSecret() {
		return CLIENT_SECRET;
	}
	
	@Override
	public String getAuthorizeUrl(ShareWebView webVuew) {
		StringBuffer buffer = new StringBuffer()
			.append(HOST).append(AUTHORIZE)
			.append("?client_id=").append(CLIENT_ID)
			.append("&redirect_uri=").append(REDIRECT_URI)
			.append("&response_type=token")
			.append("&display=mobile")
			.append("&isChangeUser=true")
			.append("&scope=basic");
		return buffer.toString();
	}

	@Override
	public String getAccessTokenUrl() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(HOST).append(ACCESS_TOKEN);
		return buffer.toString();
	}

	@Override
	public String getRedirectPrefix() {
		return REDIRECT_URI;
	}
	
	/**
	 * 获取用户资料的URL
	 * 
	 * @return
	 */
	@Override
	public String getUserShowUrl() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(HOST).append(USER_SHOW);
		return buffer.toString();
	}
	
	@Override
	public String getSendMBlogUrl() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(HOST).append(SEND_MBLOG);
		return buffer.toString();
	}
	
	public String getSendPicMBlogUrl() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(HOST).append(SEND_PIC_MBLOG);
		return buffer.toString();
	}
	
	@Override
	public String getFollowingListUrl() {
		return null;
	}
	
	@Override
	public int getFollowingList(ShareBind shareBind) {
		return -1;
	}

	@Override
	public String onRedirectUrl(String url) {
		Map<String, String> params = parseUrlParams(url);
		String errMsg = "绑定失败";
		
		if (params != null) {
			if (! TextUtils.isEmpty(params.get("access_token"))) {
				String expires_in = params.get("expires_in");
				long expires = 0;
				if (expires_in != null) {
					try {
						expires = Integer.parseInt(expires_in);
					} catch (NumberFormatException e) { }
				}
				
				setToken(params.get("access_token"), 
						params.get("refresh_token"), expires);
				
				beginTransaction(new ShareSohuLoginTransaction(this));
				
				errMsg = null;
			}
		}
		
		return errMsg;
	}

	@Override
	public ShareType getShareType() {
		return ShareType.Sohu;
	}
	
	@Override
	public int sendMBlog(String title, String content, String imgPath, String url) {
		return sendMBlog(null, title, content, imgPath, url);
	}
	
	@Override
	public int sendMBlog(ShareBind shareBind, String title, String content,
			String imgPath, String url) {
		ShareSohuMBlogTransaction t = new ShareSohuMBlogTransaction(
				this, shareBind, content, imgPath);
		return beginTransaction(t);
	}

	@Override
	public ShareResult getErrorShareResult(int errCode, Object msg) {
		ShareResult result = new ShareResult(ShareType.Sohu, false);
		result.setCode(errCode);
		
		JSONObject json = getJSONObject(msg);

		if (json != null) {
			result.setMessage(json.optString("error"));
			result.setMessageCode(String.valueOf(errCode));
		}
		
		return result;
	}


}
