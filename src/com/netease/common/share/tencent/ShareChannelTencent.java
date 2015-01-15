package com.netease.common.share.tencent;

import java.util.Map;

import org.json.JSONObject;

import android.text.TextUtils;

import com.netease.common.share.ShareBind;
import com.netease.common.share.ShareResult;
import com.netease.common.share.ShareType;
import com.netease.common.share.base.ShareBaseChannel;
import com.netease.common.share.bind.ShareWebView;

public class ShareChannelTencent extends ShareBaseChannel {

	/******************************************************************
	 * IConfig 配置项
	 *****************************************************************/
	
	/**
	 * Client ID
	 */
	public static String CLIENT_ID = "100263803";
	
	/**
	 * Client Secret
	 */
	public static String CLIENT_SECRET = "06669925ccb7b18a08a659219924ec94";
	
	/**
	 * Redirect Uri
	 */
	public static String REDIRECT_URI = "http://www.qq.com";
	
	/**
	 * Client Name
	 */
	public static String CLIENT_NAME = "网易云阅读";
	
	/**
	 * Client Web Url
	 */
	public static String CLIENT_URL = "http://yuedu.163.com";
	
	/**
	 * 
	 * http://wiki.opensns.qq.com/wiki/%E3%80%90QQ%E7%99%BB%E5%BD%95%E3%80%91API%E6%96%87%E6%A1%A3
	 */
	public static String SCOPES = "get_user_info,upload_pic,add_share";
	
	/******************************************************************
	 * 
	 *****************************************************************/
	
	private static final String HOST = "https://graph.qq.com";
	
	private static final String AUTHORIZE = "/oauth2.0/authorize";
	private static final String AUTHME = "/oauth2.0/me";
	private static final String USER_SHOW = "/user/get_user_info";
	private static final String SEND_MBLOG = "/share/add_share";
	private static final String UPLOAD_IMAGE = "/photo/upload_pic";
	
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
			.append("&response_type=token");
		
		if (SCOPES != null) {
			buffer.append("&scope=").append(SCOPES);
		}
		
		return buffer.toString();
	}

	@Override
	public String getAccessTokenUrl() {
//		StringBuffer buffer = new StringBuffer();
//		buffer.append(HOST).append(ACCESS_TOKEN);
//		return buffer.toString();
		
		return null;
	}

	@Override
	public String getRedirectPrefix() {
		return REDIRECT_URI;
	}

	public String getAuthMe() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(HOST).append(AUTHME);
		return buffer.toString();
	}
	
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
	
	public String getUpLoadLocalUrl(){
	    StringBuffer buffer = new StringBuffer();
        buffer.append(HOST).append(UPLOAD_IMAGE);
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
				
				beginTransaction(new ShareTencentLoginTransaction(this));
				errMsg = null;
			}
		} else {
		}
		
		return errMsg;
	}

	@Override
	public ShareType getShareType() {
		return ShareType.Tencent;
	}
	
	@Override
	public int sendMBlog(String title, String content, String imgPath, String url) {
		return sendMBlog(null, title, content, imgPath, url);
	}
	
	@Override
	public int sendMBlog(ShareBind shareBind, String title, String content,
			String imgPath, String url) {
		ShareTencentMBlogTransaction t = new ShareTencentMBlogTransaction(
				this, shareBind, title, content, imgPath, url);
		return beginTransaction(t);
	}
	
	@Override
	public ShareResult getErrorShareResult(int errCode, Object msg) {
		ShareResult result = new ShareResult(ShareType.Tencent, false);
		result.setCode(errCode);
		
		JSONObject json = getJSONObject(msg);

		if (json != null) {
			int code = json.optInt("ret");
			
//			result.setMessage(json.optString("msg"));
			result.setMessage("无效授权");
			result.setMessageCode(String.valueOf(code));
		}
		
		return result;
	}
	
	

}
