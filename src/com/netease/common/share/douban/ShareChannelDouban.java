package com.netease.common.share.douban;

import org.json.JSONObject;

import com.netease.common.config.IConfig;
import com.netease.common.share.ShareBind;
import com.netease.common.share.ShareResult;
import com.netease.common.share.ShareType;
import com.netease.common.share.base.ShareBaseChannel;
import com.netease.common.share.bind.ShareWebView;

/**
 * 暂不支持
 * 
 * @author dingding
 *
 */
public class ShareChannelDouban extends ShareBaseChannel implements IConfig {

	/******************************************************************
	 * IConfig 配置项
	 *****************************************************************/
	
	/**
	 * Client ID
	 */
	public static String CLIENT_ID = "0323ebfc7046a07f096bc3f550d3e17d";
	
	/**
	 * Client Secret
	 */
	public static String CLIENT_SECRET = "c7ddab85bb0fd85c";
	
	/**
	 * Redirect Uri
	 */
	public static String REDIRECT_URI = "http://weibotool.yuedu.163.com/callback";
	
	/**
	 * 参见
	 */
	public static String SCOPES = "shuo_basic_r,shuo_basic_w,douban_basic_common";
	
	/******************************************************************
	 * 
	 *****************************************************************/
	
	private static final String HOST = "https://www.douban.com";
	
	private static final String AUTHORIZE = "/service/auth2/auth";
	private static final String ACCESS_TOKEN = "/service/auth2/token";
	private static final String DOUBAN_API = "https://api.douban.com";
	private static final String USER_SHOW = "/v2/user/~me";
	private static final String SEND_MBLOG = "/shuo/v2/statuses/";
	
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
			.append("&response_type=code");
		
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

	@Override
	public String getUserShowUrl() {
		StringBuffer url = new StringBuffer()
			.append(DOUBAN_API).append(USER_SHOW);
		return url.toString();
	}
	
	@Override
	public String getSendMBlogUrl() {
		StringBuffer url = new StringBuffer()
			.append(DOUBAN_API).append(SEND_MBLOG);
		return url.toString();
	}
	
	@Override
	public String getFollowingListUrl() {
		return null;
	}

	@Override
	public String onRedirectUrl(String url) {
		String retMsg = null;
		
		String code = getCodeFromUrl(url);
		if (code == null) {
			retMsg = "绑定失败";
		} else {
			beginTransaction(new ShareDoubanLoginTransaction(this, code));
		}
		
		return retMsg;
	}

	@Override
	public ShareType getShareType() {
		return ShareType.Douban;
	}
	
	@Override
	public int sendMBlog(String title, String content, String imgPath, String url) {
		return sendMBlog(null, title, content, imgPath, url);
	}
	
	@Override
	public int sendMBlog(ShareBind shareBind, String title, String content,
			String imgPath, String url) {
		ShareDoubanMBlogTransaction t = new ShareDoubanMBlogTransaction(
				this, shareBind, content, imgPath);
		return beginTransaction(t);
	}
	
	@Override
	public int getFollowingList(ShareBind shareBind) {
		return -1;
	}
	
	@Override
	public ShareResult getErrorShareResult(int errCode, Object msg) {
		ShareResult result = new ShareResult(ShareType.Douban, false);
		result.setCode(errCode);
		
		switch (errCode) {
		case 400:
		case 403:
		case 404:
			JSONObject json = getJSONObject(msg);

			if (json != null) {
				String message = null;
				
				result.setMessageCode(json.optString("code"));
				
				switch (json.optInt("code")) {
				case 999:
					message = "未知错误";
					break;
				case 1000:
					message = "需要权限";
					break;
				case 1001:
					message = "资源不存在";
					break;
				case 1002:
					message = "参数不全";
					break;
				case 1003:
					message = "上传的图片太大";
					break;
				case 1004:
					message = "输入有违禁词";
					break;
				case 1005:
					message = "输入为空，或者输入字数不够";
					break;
				case 1006:
					message = "相关的对象不存在，比如回复帖子时，发现小组被删掉了";
					break;
				case 1007:
					message = "需要验证码，验证码有误";
					break;
				case 1008:
					message = "不支持的图片格式";
					break;
				case 1009:
					message = "照片格式有误(仅支持JPG,JPEG,GIF,PNG或BMP)";
					break;
				case 1010:
					message = "访问私有图片ck验证错误";
					break;
				case 1011:
					message = "访问私有图片ck过期";
					break;
				case 1012:
					message = "题目为空";
					break;
				case 1013:
					message = "描述为空";
					break;
				}
				
				result.setMessage(message);
			}
			break;
		}
		
		return result;
	}

}
