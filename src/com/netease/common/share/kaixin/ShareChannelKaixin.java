package com.netease.common.share.kaixin;

import org.json.JSONObject;

import com.netease.common.config.IConfig;
import com.netease.common.share.ShareBind;
import com.netease.common.share.ShareResult;
import com.netease.common.share.ShareType;
import com.netease.common.share.base.ShareBaseChannel;
import com.netease.common.share.bind.ShareWebView;

public class ShareChannelKaixin extends ShareBaseChannel implements IConfig  {

	/*************************************************************************
	 * IConfig
	 ************************************************************************/
	/**
	 * Client ID
	 */
	public static String CLIENT_ID = "62132178751342391b4b0f857a432ffd";
	
	/**
	 * Client ID
	 */
	public static String CLIENT_SECRET = "c7eeac4a6188136da93adc7799578455";
	
	/**
	 * Redirect Uri
	 */
	public static String REDIRECT_URI = "http://yuedu.163.com/redirect_uri";
	
	public static String SCOPES = "basic+create_records";
	
	/************************* IConfig ***************************/
	
	private static final String HOST = "https://api.kaixin001.com";
	private static final String AUTHORIZE = "/oauth2/authorize";
	private static final String ACCESS_TOKEN = "/oauth2/access_token";
	private static final String USER_SHOW = "/users/me.json";
	private static final String SEND_MBLOG = "/records/add.json";
	
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
		StringBuffer url = new StringBuffer(HOST)
			.append(AUTHORIZE)
			.append("?client_id=").append(CLIENT_ID)
			.append("&redirect_uri=").append(REDIRECT_URI)
			.append("&response_type=code")
			.append("&oauth_client=1");
		
		if (SCOPES != null) {
			url.append("&scope=").append(SCOPES);
		}
		
		return url.toString();
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
		String retMsg = null;
		
		String code = getCodeFromUrl(url);
		if (code == null) {
			retMsg = "绑定失败";
		} else {
			beginTransaction(new ShareKaixinLoginTransaction(this, code));
		}
		
		return retMsg;
	}

	@Override
	public ShareType getShareType() {
		return ShareType.Kaixin;
	}
	
	@Override
	public int sendMBlog(String title, String content, String imgPath, String url) {
		return sendMBlog(null, title, content, imgPath, url);
	}
	
	@Override
	public int sendMBlog(ShareBind shareBind, String title, String content,
			String imgPath, String url) {
		ShareKaixinMBlogTransaction t = new ShareKaixinMBlogTransaction(
				this, shareBind, content, imgPath);
		return beginTransaction(t);
	}
	
	@Override
	public ShareResult getErrorShareResult(int errCode, Object msg) {
		ShareResult result = new ShareResult(ShareType.Kaixin, false);
		result.setCode(errCode);
		
		JSONObject json = getJSONObject(msg);

		if (json != null) {
			result.setMessageCode(json.optString("error_code"));

			String errorDes = json.optString("error");
			if (errorDes != null) {
				String[] arr = errorDes.split(":");
				if (arr.length >= 3) {
					result.setMessageCode(arr[0]);
					result.setMessage(arr[2]);
				}
			}
		}
		
		return result;
	}

}
