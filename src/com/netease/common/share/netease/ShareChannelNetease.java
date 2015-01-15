package com.netease.common.share.netease;

import java.util.Map;

import org.json.JSONObject;

import android.text.TextUtils;

import com.netease.common.config.IConfig;
import com.netease.common.share.ShareBind;
import com.netease.common.share.ShareResult;
import com.netease.common.share.ShareType;
import com.netease.common.share.base.ShareBaseChannel;
import com.netease.common.share.bind.ShareWebView;

public class ShareChannelNetease extends ShareBaseChannel implements IConfig {

	/*************************************************************************
	 * IConfig
	 ************************************************************************/
	/**
	 * Client ID
	 */
	public static String CLIENT_ID = "5ySYtxNLIioLil8h";
	
	/**
	 * Redirect Uri
	 */
	public static String REDIRECT_URI = "http://yuedu.163.com";
	
	/************************* IConfig ***************************/
	
	private static final String HOST = "https://api.t.163.com";
	
	private static final String AUTHORIZE = "/oauth2/authorize";
	private static final String USER_SHOW = "/users/show.json";
	private static final String SEND_MBLOG = "/statuses/update.json";
	private static final String UPLOAD_IMAGE = "/statuses/upload.json";
	
	@Override
	public String getClientID() {
		return CLIENT_ID;
	}
	
	@Override
	public String getClientSecret() {
		return null;
	}
	
	@Override
	public String getAuthorizeUrl(ShareWebView webVuew) {
		StringBuffer buffer = new StringBuffer()
			.append(HOST).append(AUTHORIZE)
			.append("?client_id=").append(CLIENT_ID)
			.append("&redirect_uri=").append(REDIRECT_URI)
			.append("&response_type=token")
			.append("&display=mobile");
		
		return buffer.toString();
	}

	@Override
	public String getAccessTokenUrl() {
		return null;
	}

	@Override
	public String getRedirectPrefix() {
		return REDIRECT_URI;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public String getUserShowUrl() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(HOST).append(USER_SHOW);
		return buffer.toString();
	}
	
	/**
	 * 获取上传图片的请求地址
	 * 
	 * @return
	 */
	public String getUploadImgUrl() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(HOST).append(UPLOAD_IMAGE);
		return buffer.toString();
	}
	
	@Override
	public String getSendMBlogUrl() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(HOST).append(SEND_MBLOG);
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
				
				beginTransaction(new ShareNeteaseLoginTransaction(this));
				
				errMsg = null;
			}
		}
		
		return errMsg;
	}

	@Override
	public ShareType getShareType() {
		return ShareType.Netease;
	}
	
	@Override
	public int sendMBlog(String title, String content, String imgPath, String url) {
		return sendMBlog(null, title, content, imgPath, url);
	}
	
	@Override
	public int sendMBlog(ShareBind shareBind, String title, String content,
			String imgPath, String url) {
		ShareNeteaseMBlogTransaction t = new ShareNeteaseMBlogTransaction(
				this, shareBind, content, imgPath);
		return beginTransaction(t);
	}

	@Override
	public ShareResult getErrorShareResult(int errCode, Object msg) {
		ShareResult result = new ShareResult(ShareType.Douban, false);
		result.setCode(errCode);
		
		JSONObject json = getJSONObject(msg);

		if (json != null) {
			result.setMessageCode(json.optString("message_code"));
			result.setMessage(json.optString("error"));

			result.setMessage(getDescription(result.getMessageCode(),
					result.getMessage()));
		}
		
		return result;
	}
	
	private static String getDescription(String messageCode, String defaultDes){
		int err = 0;
		try {
			if (messageCode != null && messageCode.length() >= 5) {
				err = Integer.valueOf(messageCode.substring(0, 5));
			}
		} catch (Exception e) {
			
		}
		
		switch(err){
			case 401:
			case 1401: return "绑定失效，请重新绑定。";
			default: return defaultDes;
		}
	}

}
