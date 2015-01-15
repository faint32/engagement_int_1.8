package com.netease.common.share.renren;

import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

import android.text.TextUtils;

import com.netease.common.config.IConfig;
import com.netease.common.share.ShareBind;
import com.netease.common.share.ShareResult;
import com.netease.common.share.ShareType;
import com.netease.common.share.base.ShareBaseChannel;
import com.netease.common.share.bind.ShareWebView;
import com.netease.util.KeyValuePair;

public class ShareChannelRenren extends ShareBaseChannel implements IConfig {

	/*************************************************************************
	 * IConfig
	 ************************************************************************/
	/**
	 * Client ID
	 */
	public static String CLIENT_ID = "02e7b37b38d6443880ade561e7489b23";
	
	/**
	 * Client ID
	 */
	public static String CLIENT_SECRET = "b2a618ba7aca48dd910b916c2d9f6f54";
	
	/**
	 * Redirect Uri
	 */
	public static String REDIRECT_URI = "http://graph.renren.com/oauth/login_success.html";
	
	/**
	 * 参见
	 * <a href="http://wiki.dev.renren.com/wiki/%E6%9D%83%E9%99%90%E5%88%97%E8%A1%A8">人人网权限列表</a>
	 */
	public static String SCOPES = "read_user_feed+publish_feed+status_update";
	
	/************************* IConfig ***************************/
	
	private static final String HOST = "https://graph.renren.com";
	private static final String AUTHORIZE = "/oauth/authorize";
	private static final String ACCESS_TOKEN = "/oauth/token";
	private static final String USER_SHOW = "/users.getInfo";
	
	private static final String RESTSERVER = "https://api.renren.com/restserver.do";
	
	private static final String API_2 = "https://api.renren.com/v2/";
	private static final String SEND_STATUS = "status/put";
	private static final String FIRENDS_LIST = "user/friend/list";
	
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
			.append("&display=touch");
		
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
		return new StringBuffer().append(API_2).append(SEND_STATUS).toString();
	}
	
	@Override
	public String getFollowingListUrl() {
		return new StringBuffer().append(API_2).append(FIRENDS_LIST).toString();
	}
	
	protected static void addNormalParams(List<KeyValuePair> list,
			String method, ShareBind shareBind) {
		list.add(new KeyValuePair("method", method));
		list.add(new KeyValuePair("access_token", shareBind.getAccessToken()));
		list.add(new KeyValuePair("format", "JSON"));
		list.add(new KeyValuePair("v", "1.0"));
		list.add(new KeyValuePair("call_id", String.valueOf(System.currentTimeMillis())));
		
		Collections.sort(list);
	}
	
	@Override
	public int getFollowingList(ShareBind shareBind) {
		ShareRenrenUserTransaction t = ShareRenrenUserTransaction.createGetFollowingList(
				this, shareBind);
		return beginTransaction(t);
	}

	@Override
	public String onRedirectUrl(String url) {
		String retMsg = null;
		
		String code = getCodeFromUrl(url);
		if (code == null) {
			retMsg = "绑定失败";
		} else {
			beginTransaction(new ShareRenrenLoginTransaction(this, code));
		}
		
		return retMsg;
	}

	@Override
	public ShareType getShareType() {
		return ShareType.Renren;
	}
	
	@Override
	public int sendMBlog(String title, String content, String imgPath, String url) {
		return sendMBlog(null, title, content, imgPath, url);
	}
	
	@Override
	public int sendMBlog(ShareBind shareBind, String title, String content,
			String imgPath, String url) {
		ShareRenrenMBlogTransaction t = new ShareRenrenMBlogTransaction(
				this, shareBind, content, imgPath);
		return beginTransaction(t);
	}
	
    public int sendFeed(String title, String content,
            String imgPath, String url) {
        ShareRenrenMBlogTransaction t = new ShareRenrenMBlogTransaction(
                this, null, title, content, imgPath, url);
        return beginTransaction(t);
    }
	
	// "error_code":10231,"error_msg":"url是必须参数"
	@Override
	public ShareResult getErrorShareResult(int errCode, Object msg) {
		ShareResult result = new ShareResult(ShareType.Renren, false);
		result.setCode(errCode);
		
		JSONObject json = getJSONObject(msg);

		if (json != null) {
			int error_code = json.optInt("error_code");

			result.setMessageCode(String.valueOf(error_code));
			result.setMessage(json.optString("error_msg"));
			result.setMessage(getDescription(error_code, result.getMessage()));

			switch (json.optInt("error_code")) {
			case 200: // 没有权限进行操作
			case 201: // 没有权限
			case 202: // 需要用户授予权限
			case 450: // 当前用户的sessionKey过期
			case 452: // Session key 无效. 可能传入的sessionKey格式出现错误
			case 453: // 调用此方法时，session key 是一个必须的参数
			case 2000: // 没有传入access_token参数
			case 2001: // access_token无效
			case 2002: // access_token过期
			case 1005: // 帐号处于被封禁的状态
			case 1006: // 帐号处于注销的状态
				break;
			}
		}
		
		return result;
	}
	
		
	private static String getDescription(int errcode, String defaultDes) {
		switch (errcode) {
		case 1:
			return "抱歉,服务临时不可用";
		case 2:
			return "调用数据服务出现异常，请稍候再试";
		case 3:
			return "请求未知方法";
		case 4:
			return "应用已达到设定的请求上限";
		case 5:
			return "请求来自未经授权的IP地址";
		case 8:
			return "服务器繁忙，请稍后再试";
		case 100:
			return "请求包含无效参数";
		case 101:
			return "提交的api_key不属于任何已知的应用";
		case 103:
			return "必须是POST提交";
		case 104:
			return "sig认证失败";
		case 107:
			return "无效的文本内容";
		case 110:
			return "所操作资源已经不存在";
		case 200:
			return "没有权限进行操作";
		case 450:
			return "当前用户的sessionKey过期";
		case 452:
			return "Session key 无效";
		case 453:
			return "调用此方法时，session key 是一个必须的参数";
		case 2000:
			return "没有传入access_token参数";
		case 2001:
			return "access_token无效";
		case 2002:
			return "access_token过期";
		case 10200:
			return "当前请求缺少必需参数";
		case 10300:
			return "日志所有者不存在";
		case 10301:
			return "日志所有者的用户ID是必须的参数";
		case 10302:
			return "日志的ID是必须参数";
		case 10303:
			return "你没有权限阅读此篇日志";
		case 10304:
			return "发表的日志可能含有非法信息";
		case 10305:
			return "日志标题和内容不能为空或不能过长";
		case 10306:
			return "删除日志时发生未知异常";
		case 10307:
			return "日志评论的ID是必须的参数";
		case 10308:
			return "删除日志评论时发生未知异常";
		case 10309:
			return "评论超过了规定的次数";
		case 10310:
			return "日志标题为必须参数";
		case 10311:
			return "日志正文为必须参数";
		case 10312:
			return "密码错误";
		case 10313:
			return "此日志不允许分享";
		case 10400:
			return "状态更新过于频繁";
		case 10401:
			return "状态字数超过限定长度";
		case 10402:
			return "状态的内容含有非法字符";
		case 10500:
			return "照片所有者的用户ID是必须的参数";
		case 10501:
			return "照片所有者不存在";
		case 10502:
			return "照片未知异常";
		case 10503:
			return "相册ID是必须的参数";
		case 10504:
			return "相册不存在";
		case 10505:
			return "相册所有者不存在";
		case 10506:
			return "你没有权限查看此相册";
		case 10507:
			return "照片的ID是必须的参数";
		case 10508:
			return "照片不存在";
		case 10509:
			return "你没有权限评论此照片";
		case 10510:
			return "你发表的评论含有违禁信息";
		case 10511:
			return "无效的照片格式,照片的宽和高不能小于50,照片的宽与高的比不能小于1:3";
		case 10512:
			return "相册空间不足";
		case 10513:
			return "盗用的照片";
		case 10514:
			return "相册名为必须的参数";
		case 10516:
			return "密码错误";
		case 10517:
			return "此相册或照片不允许分享";
		case 10518:
			return "删除失败，可能是相册或照片不存在，或者是不允许删除";
		case 10600:
			return "此接口的调用超过了限定的次数";
		case 10601:
			return "Feed标题模板是无效的";
		case 10602:
			return "显示内容应该在100个字符之内";
		case 10604:
			return "title_data 参数不是一个有效的JSON 格式数组";
		case 10606:
			return "只能包含case a>或者case xn:name>标签";
		case 10607:
			return "内容部分是可选的，内容部分的最终显示的字符数应该控制在200个以内";
		case 10609:
			return "传入title_data或者Feed标题模板中含有非法标签";
		case 10610:
			return "传入内容中含有非法标签";
		case 10615:
			return "Feed内容模板中定义的变量和JSON数组中的定义不匹配";
		case 10616:
			return " Feed标题模板中定义的变量和title_data JSON数组中的定义不匹配";
		case 10617:
			return "根据传入的模板号，没有找到对应的模板";
		case 10618:
			return "无效的URL";
		case 10619:
			return "参数不是一个有效的JSON 格式数组";
		case 10621:
			return "feed_Token过期";
		case 10700:
			return "XNML片段格式不符合要求，传入文本信息错误";
		case 10701:
			return "to_ids 参数的格式不符合要求";
		case 10702:
			return "发送者已超过当天发送配额，此接口调用过于频繁，请明天再试";
		case 10703:
			return " AppToUser已超过发送配额";
		case 10704:
			return "通知发送过快";
		case 10709:
			return "根据传入的模板号，没有找到对应的模板";
		case 10711:
			return "email内容中只能包含case a>或者case xn:name>标签";
		case 10712:
			return "email标题只能是文本，不能包括其它标签";
		case 10713:
			return "email模板中的xnml语法有问题";
		case 10714:
			return "body_data参数不是一个有效的JSON 格式数组";
		case 10716:
			return "接收者当天接收的通知量超过配额";
		case 10800:
			return "传递订单号已失效，无法获取到token";
		case 10801:
			return " 无效的订单号 (小于零)";
		case 10802:
			return "消费金额无效:不支持大笔消费金额>1000或者小于零";
		case 10803:
			return "人人网支付平台应用资料审核未通过，没有使用平台的资格";
		case 10804:
			return "该订单不存在";
		case 10900:
			return "站内信不存在";
		case 10901:
			return "站内信的发送超过了限制";
		case 20000:
			return "可能有违禁信息";
		case 20200:
			return "需要用户授权";
		case 20201:
			return "需要用户授予status_update权限";
		case 20202:
			return "需要用户授予email权限";
		case 20203:
			return "需要用户授予publish_stream权限";
		case 20204:
			return "需要用户授予read_stream权限";
		case 20205:
			return "需要用户授予photo_upload权限";
		case 20207:
			return "需要用户授予offline_access权限";
		case 20213:
			return "需要用户授予feed_publish权限";
		case 20300:
			return "不是已开放的公共主页";
		case 20301:
			return "该用户不是此公共主页的管理员";
		case 20302:
			return "该公共主页不存在";
		case 20303:
			return "该公共主页关闭了留言或评论";
		case 20304:
			return "只有粉丝才能留言或评论";
		case 20305:
			return "你没有权限留言，请联系管理员";
		case 20306:
			return "管理员只能回复，不能留言";
		case 20307:
			return "此接口不允许公共主页调用";
		case 20308:
			return "操作太过频繁";
		case 1001:
			return "输入的帐号或者密码不对";
		case 1002:
			return "当前登录的ip地址处于封禁状态";
		case 1003:
			return "用户不存在";
		case 1004:
			return "帐号处于未激活的状态";
		case 1005:
			return "帐号处于被封禁的状态";
		case 1006:
			return "帐号处于注销的状态";
		case 1007:
			return "登录服务暂时不可用，请重试";
		case 1008:
			return "请检查传入的api_key参数是否正确";
		case 1009:
			return "提交必须是Post方式";
		case 1010:
			return "帐号存在安全问题";
		case 1011:
			return "用户输入不合法";
		case 1012:
			return "产品处于测试阶段，请耐心等待";
		case 1013:
			return "您的账号存在异常行为";

		default:
			return TextUtils.isEmpty(defaultDes) ? "未知错误" : defaultDes;
		}

	}

}
