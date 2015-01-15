package com.netease.common.share.sina;

import org.json.JSONObject;

import android.text.TextUtils;

import com.netease.common.config.IConfig;
import com.netease.common.share.ShareBind;
import com.netease.common.share.ShareResult;
import com.netease.common.share.ShareType;
import com.netease.common.share.base.ShareBaseChannel;
import com.netease.common.share.bind.ShareWebView;
import com.netease.common.task.TransactionListener;

public class ShareChannelSina extends ShareBaseChannel implements IConfig {

	/******************************************************************
	 * IConfig 配置项
	 *****************************************************************/
	
	/**
	 * Client ID
	 */
	public static String CLIENT_ID = "3626227865";
	
	/**
	 * Client Secret
	 */
	public static String CLIENT_SECRET = "f4736bfcd787da4dfd225be6b10c0cd5";
	
	public static String REDIRECT_URI = "http://weibotool.yuedu.163.com/callback";
	
	public static boolean FOLLOW_OFFICIAL_MBLOG = false;
	
	/******************************************************************
	 * 
	 *****************************************************************/
	
	private static final String HOST = "https://api.weibo.com";
	
	private static final String AUTHORIZE = "/oauth2/authorize";
	private static final String ACCESS_TOKEN = "/oauth2/access_token";
	private static final String USER_SHOW = "/2/users/show.json";
	private static final String SEND_MBLOG = "/2/statuses/update.json";
	private static final String SEND_PIC_MBLOG = "/2/statuses/upload.json";
	private static final String CREATE_FRIENDSHIP = "/2/friendships/create.json";
	private static final String SHOW_FRIENDSHIP = "/2/friendships/show.json";
	private static final String FRIENDSHIP_FRIENDS = "/2/friendships/friends.json";
	
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
			.append("&display=mobile");
		
		if (FOLLOW_OFFICIAL_MBLOG) {
			url.append("&scope=follow_app_official_microblog");
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
		StringBuffer buffer = new StringBuffer();
		buffer.append(HOST).append(FRIENDSHIP_FRIENDS);
		return buffer.toString();
	}
	
	@Override
	public int getFollowingList(ShareBind shareBind) {
		ShareSinaUserTransaction t = ShareSinaUserTransaction.createGetFollowingList(
				this, shareBind);
		return beginTransaction(t);
	}
	
	public String getSendPicMBlogUrl() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(HOST).append(SEND_PIC_MBLOG);
		return buffer.toString();
	}
	
	public String getShowFriendShipUrl() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(HOST).append(SHOW_FRIENDSHIP);
		return buffer.toString();
	}
	
	public String getCreateFriendShipUrl() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(HOST).append(CREATE_FRIENDSHIP);
		return buffer.toString();
	}

	@Override
	public String onRedirectUrl(String url) {
		String retMsg = null;
		
		String code = getCodeFromUrl(url);
		if (code == null) {
			retMsg = "绑定失败";
		} else {
			beginTransaction(new ShareSinaLoginTransaction(this, code, null));
		}
		return retMsg;
	}

	@Override
	public ShareType getShareType() {
		return ShareType.Sina;
	}
	
	/**
	 * sso 登录方式中获取用户信息和检查是否关注官方微博
	 * 
	 * @param accessToken
	 * @param refreshToken
	 * @param expires
	 * @param uid
	 * @param officialUid 非空时检查是否关注官方微博，检查结果在ShareResult.isFollowingOfficial()方法
	 * @return
	 */
	public static int ssoLogin(String accessToken, String refreshToken,
			long expires, String uid, String officialUid,
			TransactionListener listener) {
		ShareChannelSina mChannel = new ShareChannelSina();
		mChannel.setShareListener(listener);
		mChannel.setToken(accessToken, refreshToken, expires);
		
		return mChannel.beginTransaction(new ShareSinaLoginTransaction(
				mChannel, uid, officialUid));
	}
	
	@Override
	public int sendMBlog(String title, String content, String imgPath, String url) {
		return sendMBlog(null, title, content, imgPath, url);
	}
	
	@Override
	public int sendMBlog(ShareBind shareBind, String title, String content,
			String imgPath, String url) {
		ShareSinaMBlogTransaction t = new ShareSinaMBlogTransaction(
				this, shareBind, content, imgPath);
		return beginTransaction(t);
	}
	
	public int createFriendShip(String uid) {
		ShareSinaUserTransaction t = ShareSinaUserTransaction.createCreateFriendShip(
				this, uid);
		return beginTransaction(t);
	}
	
	@Override
	public ShareResult getErrorShareResult(int errCode, Object msg) {
		ShareResult result = new ShareResult(ShareType.Sina, false);
		result.setCode(errCode);
		
		JSONObject json = getJSONObject(msg);

		if (json != null) {
			result.setMessageCode(json.optString("error_code"));
			result.setMessage(json.optString("error"));
			result.setMessage(getDescription(errCode, result.getMessageCode(),
					result.getMessage()));
		}

		return result;
	}

	final static String getDescription(int errcode, String messageCode, String defaultDes){
		int err = errcode;
		try {
			err = Integer.valueOf(messageCode);
		} catch (Exception e) {
			
		}
		
		switch(err){
		//oauth2.0
		case	10001: return "系统错误";
		case	10002: return "服务端资源不可用";
		case	10003: return "远程服务出错";
		case	10005: return "该资源需要appkey拥有更高级的授权";
//        case    10006: return "缺少 source参数(appkey)";
		case	10006: return "无效授权";
		case	10007: return "不支持的 MediaType (%s)";
		case	10008: return "错误:参数错误，请参考API文档";
		case	10009: return "任务过多，系统繁忙";
		case	10010: return "任务超时";
		case	10011: return "RPC错误";
		case	10012: return "非法请求";
		case	10013: return "不合法的微博用户";
		case	10014: return "第三方应用访问api接口权限受限制";
		case	10016: return "错误:缺失必选参数:%s，请参考API文档";
		case	10017: return "错误:参数值非法,希望得到 (%s),实际得到 (%s)，请参考API文档";
		case	10018: return "请求长度超过限制";
		case	10020: return "接口不存在";
		case	10021: return "请求的HTTP METHOD不支持";
		case	10022: return "IP请求超过上限";
		case	10023: return "用户请求超过上限";
		case	10024: return "用户请求接口%s超过上限";
		case	10025: return "内部接口参数错误";
		case	20001: return "IDS参数为空";
		case	20002: return "uid参数为空";
		case	20003: return "用户不存在";
		case	20005: return "不支持的图片类型,仅仅支持JPG,GIF,PNG";
		case	20006: return "图片太大";
		case	20007: return "请确保使用multpart上传了图片";
		case	20008: return "内容为空";
		case	20009: return "id列表太长了";
		case	20012: return "输入文字太长，请确认不超过140个字符";
		case	20013: return "输入文字太长，请确认不超过300个字符";
		case	20014: return "传入参数有误，请再调用一次";
		case	20016: return "发微博太多啦，休息一会儿吧";
		case	20017: return "你刚刚已经发送过相似内容了哦，先休息一会吧";
		case	20019: return "不要太贪心哦，发一次就够啦";
		case	20023: return "很抱歉，此功能暂时无法使用，如需帮助请联系@微博客服 或者致电客服电话400 690 0000";
		case	20031: return "需要弹出验证码";
		case	20032: return "微博发布成功。目前服务器数据同步可能会有延迟，请耐心等待1-2分钟。谢谢";
		case	20033: return "登陆状态异常";
		case	20101: return "不存在的微博";
		case	20102: return "不是你发布的微博";
		case	20103: return "不能转发自己的微博";
		case	20109: return "微博 id为空";
		case	20111: return "不能发布相同的微博";
		case	20112: return "由于作者隐私设置，你没有权限查看此微博";
		case	20114: return "标签名太长";
		case	20115: return "标签不存在";
		case	20116: return "标签已存在";
		case	20117: return "最多200个标签";
		case	20118: return "最多5个标签";
		case	20119: return "标签搜索失败";
		case	20120: return "由于作者设置了可见性，你没有权限转发此微博";
		case	20121: return "visible参数非法";
		case	20122: return "应用不存在";
		case	20123: return "最多屏蔽200个应用";
		case	20124: return "最多屏蔽500条微博";
		case	20125: return "没有屏蔽过此应用";
		case	20126: return "不能屏蔽新浪应用";
		case	20127: return "已添加了此屏蔽";
		case	20128: return "删除屏蔽失败";
		case	20129: return "没有屏蔽任何应用";
		case	20201: return "不存在的微博评论";
		case	20203: return "不是你发布的评论";
		case	20204: return "评论ID为空";
		case	20206: return "作者只允许关注用户评论";
		case	20207: return "作者只允许可信用户评论";
		case	20401: return "域名不存在";
		case	20402: return "verifier错误";
		case	20403: return "屏蔽用户列表中存在此uid";
		case	20404: return "屏蔽用户列表中不存在此uid";
		case	20405: return "uid对应用户不是登录用户的好友";
		case	20406: return "屏蔽用户个数超出上限";
		case	20407: return "没有合适的uid";
		case	20408: return "从feed屏蔽列表中，处理用户失败";
		case	20501: return "错误:source_user 或者target_user用户不存在";
		case	20502: return "必须输入目标用户id或者 screen_name";
		case	20503: return "关系错误，user_id必须是你关注的用户";
		case	20504: return "你不能关注自己";
		case	20505: return "加关注请求超过上限";
		case	20506: return "已经关注此用户";
		case	20507: return "需要输入验证码";
		case	20508: return "根据对方的设置，你不能进行此操作";
		case	20509: return "悄悄关注个数到达上限";
		case	20510: return "不是悄悄关注人";
		case	20511: return "已经悄悄关注此用户";
		case	20512: return "你已经把此用户加入黑名单，加关注前请先解除";
		case	20513: return "你的关注人数已达上限";
		case	20522: return "还未关注此用户";
		case	20523: return "还不是粉丝";
		case	20601: return "列表名太长，请确保输入的文本不超过10个字符";
		case	20602: return "列表描叙太长，请确保输入的文本不超过70个字符";
		case	20603: return "列表不存在";
		case	20604: return "不是对象所属者";
		case	20605: return "列表名或描叙不合法";
		case	20606: return "记录已存在";
		case	20607: return "错误:数据库错误，请联系系统管理员";
		case	20608: return "列表名冲突";
		case	20610: return "目前不支持私有分组";
		case	20611: return "创建list失败";
		case	20612: return "目前只支持私有分组";
		case	20613: return "错误:不能创建更多的列表";
		case	20614: return "已拥有列表上下，请参考API文档";
		case	20615: return "成员上线，请参考API文档";
		case	20616: return "不支持的分组类型";
		case	20617: return "最大返回300条";
		case	20618: return "uid 不在列表中";
		case	20701: return "不能提交相同的标签";
		case	20702: return "最多两个标签";
		case	20704: return "您已经收藏了此微博";
		case	20705: return "此微博不是您的收藏";
		case	20706: return "操作失败";
		case	20801: return "trend_name是空值";
		case	20802: return "trend_id是空值";
		case	21001: return "标签参数为空";
		case	21002: return "标签名太长，请确保每个标签名不超过14个字符";
		case	21103: return "该用户已经绑定手机";
		case	21104: return "verifier错误";
		case	21105: return "你的手机号近期频繁绑定过多个帐号，如果想要继续绑定此帐号，请拨打客服电话400 690 0000申请绑定";
		case	21108: return "原始密码错误";
		case	21109: return "新密码错误";
		case	21110: return "此用户暂时没有绑定手机";
		case	21113: return "教育信息不存在";
		case	21115: return "职业信息不存在";
		case	21117: return "此用户没有qq信息";
		case	21120: return "此用户没有微号信息";
		case	21121: return "此微号已经存在";
		case	21301: return "认证失败";
		case	21302: return "用户名或密码不正确";
		case	21303: return "用户名密码认证超过请求限制";
		case	21304: return "版本号错误";
		case	21305: return "缺少必要的参数";
		case	21306: return "Oauth参数被拒绝";
		case	21307: return "时间戳不正确";
		case	21308: return "nonce参数已经被使用";
		case	21309: return "签名算法不支持";
		case	21310: return "签名值不合法";
		case	21311: return "consumer_key不存在";
		case	21312: return "consumer_key不合法";
		case	21313: return "consumer_key缺失";
		case	21314: return "授权已被使用";
		case	21315: return "授权已过期";
		case	21316: return "授权不合法";
		case	21317: return "授权不合法";
		case	21318: return "Pin码认证失败";
		case	21319: return "授权关系已经被解除";
		case	21320: return "不支持的协议";
		case	21321: return "未审核的应用使用人数超过限制";
		case	21322: return "重定向地址不匹配";
		case	21323: return "请求不合法";
		case	21324: return "client_id或client_secret参数无效";
		case	21325: return "提供的Access Grant是无效的、过期的或已撤销的";
		case	21326: return "客户端没有权限";
		case	21327: return "授权失效";
		case	21328: return "不支持的 GrantType";
		case	21329: return "不支持的 ResponseType";
		case	21330: return "用户或授权服务器拒绝授予数据访问权限";
		case	21331: return "服务暂时无法访问";
		case	21332: return "授权无效";
		case	21333: return "禁止使用此认证方式";

		//oauth1.0
		/*
		case 304: return "没有数据返回";
		case 400: return "请求数据不合法，或者超过请求频率限制";
		case 40028: return "内部接口错误";
		case  40033: return "source_user或者target_user用户不存在 ";
		case 40031: return "调用的微博不存在 ";
		case 40036: return "调用的微博不是当前用户发布的微博 ";
		case 40034: return "不能转发自己的微博 ";
		case 40038: return "不合法的微博 ";
		case 40037: return "不合法的评论 ";
		case 40015: return "该条评论不是当前登录用户发布的评论 ";
		case 40017: return "不能给不是你粉丝的人发私信 ";
		case 40019: return "不合法的私信 ";
		case 40021: return "不是属于你的私信 ";
		case 40022: return "source参数(appkey)缺失 ";
		case 40007: return "格式不支持，仅仅支持XML或JSON格式 ";
		case 40009: return "图片错误，请确保使用multipart上传了图片 ";
		case 40011: return "私信发布超过上限 ";
		case 40012: return "内容为空 ";
		case 40016: return "微博id为空 ";
		case 40018: return "ids参数为空 ";
		case 40020: return "评论ID为空 ";
		case 40023: return "用户不存在 ";
		case 40024: return "ids过多，请参考API文档 ";
		case 40025: return "不能发布重复的内容 ";
		case 40026: return "请传递正确的目标用户uid或者screen name ";
		case 40045: return "不支持的图片类型,支持的图片类型有JPG,GIF,PNG ";
		case 40008: return "图片大小错误，上传的图片大小上限为5M ";
		case 40001: return "参数错误，请参考API文档 ";
		case 40002: return "不是对象所属者，没有操作权限 ";
		case 40010: return "私信不存在 ";
		case 40013: return "微博太长，请确认不超过140个字符 ";
		case 40039: return "地理信息输入错误 ";
		case 40040: return "IP限制，不能请求该资源 ";
		case 40041: return "uid参数为空 ";
		case 40042: return "token参数为空 ";
		case 40043: return "domain参数错误 ";
		case 40044: return "appkey参数缺失 ";
		case 40029: return "verifier错误 ";
		case 40027: return "标签参数为空 ";
		case 40032: return "列表名太长，请确保输入的文本不超过10个字符";
		case 40030: return "列表描述太长，请确保输入的文本不超过70个字符";
		case 40035: return "列表不存在 ";
		case 40053: return "权限不足，只有创建者有相关权限";
		case 40054: return "参数错误，请参考API文档 ";
		case 40059: return "插入失败，记录已存在 ";
		case 40060: return "数据库错误，请联系系统管理员";
		case 40061: return "列表名冲突 ";
		case 40062: return "id列表太长了 ";
		case 40063: return "urls是空的 ";
		case 40064: return "urls太多了 ";
		case 40065: return "ip是空值 ";
		case 40066: return "url是空值 ";
		case 40067: return "trend_name是空值";
		case 40068: return "trend_id是空值 ";
		case 40069: return "userid是空值 ";
		case 40070: return "第三方应用访问api接口权限受限制";
		case 40071: return "关系错误，user_id必须是你关注的用户";
		case 40072: return "授权关系已经被删除 ";
		case 40073: return "目前不支持私有分组 ";
		case 40074: return "创建list失败 ";
		case 40075: return "需要系统管理员的权限";
		case 40076: return "含有非法词 ";
		case 40082: return "无效分类!";
		case 40083: return "无效状态码 ";
		case 40084: return "目前只支持私有分组";
		case 401:  return "没有进行身份验证";
		case 40101: return "Oauth版本号错误";
		case 40102: return " Oauth缺少必要的参数";
		case 40103: return " Oauth参数被拒绝 ";
		case 40104: return " Oauth时间戳不正确 ";
		case 40105: return " nonce参数已经被使用 ";
		case 40106: return " Oauth签名算法不支持 ";
		case 40107: return " Oauth签名值不合法 ";
		case 40108: return " Oauth consumer_key不存在";
		case 40109: return " Oauth consumer_key不合法 ";
		case 40110: return " Oauth Token已经被使用 ";
		case 40111: return "Oauth Token已经过期 ";
		case 40112: return "Oauth Token不合法 ";
		case 40113: return "Oauth Token不合法 ";
		case 40114: return "Oauth Pin码认证失败 ";

		case 402: return "没有开通微博 ";
		case 403: return "没有权限访问对应的资源";
		case 40301: return "已拥有列表上限 ";
		case 40302: return "认证失败 ";
		case 40303: return "已经关注此用户 ";
		case 40304: return "发布微博超过上限 ";
		case 40305: return "发布评论超过上限 ";
		case 40306: return "用户名密码认证超过请求限制 ";
		case 40307: return "请求的HTTP METHOD不支持 ";
		case 40308: return "发布微博超过上限 ";
		case 40309: return "密码不正确 ";
		case 40314: return "该资源需要appkey拥有更高级的授权 ";

		case 404: return "请求的资源不存在";
		case 500: return "服务器内部错误";
		case 502: return "微博接口API关闭或正在升级 ";
		case 503: return "服务端资源不可用";
		 */
		
		default: return TextUtils.isEmpty(defaultDes) ? "未知错误" : defaultDes;
		}

	}


}
