package com.netease.common.share.qqmblog;

import java.util.Map;

import org.json.JSONObject;

import android.text.TextUtils;

import com.netease.common.config.IConfig;
import com.netease.common.share.ShareBind;
import com.netease.common.share.ShareResult;
import com.netease.common.share.ShareType;
import com.netease.common.share.base.ShareBaseChannel;
import com.netease.common.share.bind.ShareWebView;

public class ShareChannelQqmblog extends ShareBaseChannel implements IConfig {

	/******************************************************************
	 * IConfig 配置项
	 *****************************************************************/
	
	/**
	 * Client ID
	 */
	public static String CLIENT_ID = "801349171";
	
	/**
	 * Client Secret
	 */
//	public static String CLIENT_SECRET = "";
	
	/**
	 * Redirect Uri
	 */
	public static String REDIRECT_URI = "http://yuedu.163.com";
	
	
	/******************************************************************
	 * 
	 *****************************************************************/
	
	private static final String HOST = "https://open.t.qq.com";
	
	private static final String AUTHORIZE = "/cgi-bin/oauth2/authorize";
//	private static final String ACCESS_TOKEN = "/cgi-bin/oauth2/access_token";
	private static final String USER_SHOW = "/api/user/info";
	private static final String SEND_MBLOG = "/api/t/add";
	private static final String SEND_PIC_MBLOG = "/api/t/add_pic";
	private static final String ADD_FRIEND = "api/friends/add";
	private static final String FRIENDSHIP_FRIENDS = "/api/friends/idollist_s";
	
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
			.append("&response_type=token")
			.append("&redirect_uri=").append(REDIRECT_URI)
			.append("&wap=2");
		
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

	@Override
	public String getUserShowUrl() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(HOST).append(USER_SHOW);
		return buffer.toString();
	}
	
	public String getSendPicMBlogUrl() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(HOST).append(SEND_PIC_MBLOG);
		return buffer.toString();
	}
	
	@Override
	public String getSendMBlogUrl() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(HOST).append(SEND_MBLOG);
		return buffer.toString();
	}
	
	public String getCreateFriendShipUrl() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(HOST).append(ADD_FRIEND);
		return buffer.toString();
	}
	
	@Override
	public String getFollowingListUrl() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(HOST).append(FRIENDSHIP_FRIENDS);
		return buffer.toString();
	}
	
	@Override
	public int getFollowingList(ShareBind shareBind) {
		ShareQqmblogUserTransaction t = ShareQqmblogUserTransaction.createGetFollowingList(
				this, shareBind);
		return beginTransaction(t);
	}
	
	/**
	 * 添加关注
	 * @param uid
	 * @return
	 */
	public int createFriendShip(String uid) {
		ShareQqmblogUserTransaction t = ShareQqmblogUserTransaction.createCreateFriendShip(
				this, uid);
		return beginTransaction(t);
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
				
				beginTransaction(new ShareQqmblogLoginTransaction(this, params.get("openid")));
				
				errMsg = null;
			}
		}
		
		return errMsg;
	}

	@Override
	public ShareType getShareType() {
		return ShareType.Qqmblog;
	}
	
	@Override
	public int sendMBlog(String title, String content, String imgPath, String url) {
		return sendMBlog(null, title, content, imgPath, url);
	}
	
	@Override
	public int sendMBlog(ShareBind shareBind, String title, String content,
			String imgPath, String url) {
		ShareQqmblogMBlogTransaction t = new ShareQqmblogMBlogTransaction(
				this, shareBind, content, imgPath);
		return beginTransaction(t);
	}

	@Override
	public ShareResult getErrorShareResult(int errCode, Object msg) {
		ShareResult result = new ShareResult(ShareType.Qqmblog, false);
		result.setCode(errCode);
		
		JSONObject json = getJSONObject(msg);

		if (json != null) {
			result.setMessageCode(json.optString("errcode"));
			result.setMessage(getDescription(json.optInt("ret"),
					json.optInt("errcode")));
		}
		
		return result;
	}
	
	private static String getDescription(int ret, int errcode){
		if(1 == ret){
			return "参数错误";
		}
		else if(2 == ret){
			return "频率受限";
		}
		else if (3 == ret) {
			switch (errcode) {
			case 1:
				return "无效TOKEN,被吊销";
			case 2:
				return "请求重放";
			case 3:
				return "access_token不存在";
			case 4:
				return "access_token超时";
			case 5:
				return "oauth 版本不对";
			case 6:
				return "oauth 签名方法不对";
			case 7:
				return "参数错";
			case 9:
				return "验证签名失败";
			case 10:
				return "网络错误";
			case 11:
				return "参数长度不对";
			default:
				return "处理失败";
			}
		}
		else if(4 == ret){
			switch(errcode){
			case 4: return "脏话太多";
			case 5: return "禁止访问";
			case 6: return "该记录不存在";
			case 8: return "内容超过最大长度";
			case 9: return "包含垃圾信息";
			case 10: return "发表太快，被频率限制";
			case 11: return "源消息已删除";
			case 12: return "源消息审核中";
			case 13: return "重复发表";
			}
		}
		
		return "未知错误";
	}

}
