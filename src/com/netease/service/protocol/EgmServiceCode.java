package com.netease.service.protocol;

import com.netease.common.task.TransTypeCode;


public class EgmServiceCode implements TransTypeCode{
    /* 公共消息段定义 */
    public static final int SERVICE_MSG_CODE = 0x1000;//客户端本地生成的code
    public static final int SERVICE_HTTP_CODE = 0x2000;//http协议本身返回code
    public static final int SERVICE_PROTOCOL_CODE_COMMON = 0x3000; //协议返回的全局code
    public static final int SERVICE_PROTOCOL_CODE_API = 0x6000; //具体api返回的code
    
    /* Net 网络错误值 */
    public static final int NETWORK_ERR = SERVICE_HTTP_CODE;
    public static final int NETWORK_ERR_COMMON = SERVICE_HTTP_CODE + 1 ;
     
         
     
	/* Transaction code 公共部分 通用段0-600*/
    /** 成功 */
    public static final int TRANSACTION_SUCCESS = SERVICE_MSG_CODE;
    public static final int TRANSACTION_FAIL    = SERVICE_MSG_CODE +1;
    
    /** 请求资源没有修改 */
    public static final int TRANSACTION_RES_UNCHANGED = 304;
    
    /* protocol全局code 通用的错误类型段500-599*/
    /** 成功 */
    public static final int PROTOCOL_CODE_SUCCESS = 0;
    
    /** 失败 */
    public static final int TRANSACTION_COMMON_SERVER_ERROR = 500;
    /** 未登录 */
    public static final int TRANSACTION_COMMON_NOT_LOGIN = 501;
    /** 未注册 */
    public static final int TRANSACTION_COMMON_NOT_REGISTER = 502;
    /** 已注册 */
    public static final int TRANSACTION_COMMON_REGISTER_ALREADY = 503;
    /** 密码错误 */
    public static final int TRANSACTION_COMMON_PASSWORD_ERROR = 504;
    /** 密码不合法 */
    public static final int TRANSACTION_COMMON_PASSWORD_ILLEGAL = 505;
    /** 余额不足 */
    public static final int TRANSACTION_COMMON_BALANCE_NOT_ENOUGHT = 506;
    /** 507 用户被冻结 */
    public static final int TRANSACTION_COMMON_USER_BLOCK = 507;
    /** 未注册完成，需要补全资料 */
    public static final int TRANSACTION_COMMON_REGISTER_NOT_FINISH = 508;
    /** 请求参数错误 */
    public static final int TRANSACTION_COMMON_PARAMS_ERROR = 509;
    /** 用户被删除 */
    public static final int TRANSACTION_COMMON_USER_DELETED = 510;
    /** 第三方账户未完成注册 */
    public static final int TRANSACTION_COMMON_EXTERNAL_REGISTER_NOT_FINISH = 511;
    /** 用户当前版本过低，需要升级（客户端判断如果返回这个code,需要去请求/setting/getversion读取升级数据 */
	public static final int TRANSACTION_COMMON_FORCEUPDADE = 514;

    
    /* Transaction code 4位状态码具体业务逻辑部分 1000-9999 */
    /** 验证码错误 */
    public static final int TRANSACTION_VERIFY_CODE_ERROR = 1010;
    /** 短信请求次数超过系统限制 */
    public static final int TRANSACTION_SMS_REQUIRE_TOO_MANY = 1011;
    /** 请求间隔时间过短，请过后再获取验证码 */
    public static final int TRANSACTION_SMS_REQUIRE_INTERVAL_TOO_SHORT = 1012;
    /** 手机号码格式错误 */
    public static final int TRANSACTION_MOBILE_FORMAT_ERROR = 1013;
    /** 手机号码已注册了网易通行证 */
    public static final int TRANSACTION_MOBILE_REGISTER_NETEASE_ALREADY = 1014;
    /** 邀请码无效 */
    public static final int TRANSACTION_INVITE_CODE_ILLEGAL = 1015;
    /** AESkey加密参数错误或过期（过期时间30分钟) */
    public static final int TRANSACTION_AES_KEY_ERROR = 1016;
    /** 手机号码已绑定yuehui帐号 */
    public static final int TRANSACTION_MOBILE_BIND_YUEHUI_ALREADY = 1017;
    
    /** 照片大小超过限制 */
    public static final int TRANSACTION_ACCOUNT_PICTURE_FILE_TOO_BIG = 1020;
    /** 图片尺寸不符合要求 */
    public static final int TRANSACTION_ACCOUNT_PICTURE_SIZE_ILLEGAL = 1021;
    /** 裁剪坐标错误 */
    public static final int TRANSACTION_ACCOUNT_PICTURE_CROP_COORDINATE_ERROR = 1022;
    /** 昵称不符合规范 */
    public static final int TRANSACTION_ACCOUNT_NICKNAME_ILLEGAL = 1023;
    /** 生日日期格式错误 */
    public static final int TRANSACTION_ACCOUNT_BIRTHDAY_FORMAT_ERROR = 1024;
    
    /** 非APP 会员 */
    public static final int TRANSACTION_ACCOUNT_NOT_MENBER = 1030;
    /** 没有绑定手机 */
    public static final int TRANSACTION_ACCOUNT_NOT_BIND_MOBILE = 1031;
    /** 同城会员首次注册资料导入成功但未绑定手机(绑定手机界面需要显示邀请码输入框) */
    public static final int TRANSACTION_ACCOUNT_NOT_BIND_MOBILE2 = 1033;
    /** 非yuehui.163.com会员 */
    public static final int TRANSACTION_ACCOUNT_NOT_YUEHUI_MENBER = 1032;
    
    /* 上传照片1100-1199 */
    /** 照片大小超过限制 */
    public static final int TRANSACTION_PICTURE_FILE_TOO_BIG = 1100;
    /** 图片尺寸不符合要求 */
    public static final int TRANSACTION_PICTURE_SIZE_ILLEGAL = 1101;
    /** 公共照片最多只能上传10张 */
    public static final int TRANSACTION_PICTURE_PUBLIC_LIMIT = 1102;
    /** 私密照片最多只能上传300张 */
    public static final int TRANSACTION_PICTURE_PRIVATE_LIMIT = 1103;
    /** 相册已满 */
    public static final int TRANSACTION_PICTURE_FULL = 1104;
    /** 照片不存在 */
    public static final int TRANSACTION_PICTURE_NOT_EXITS = 1105;
    /** 已经赞过，不能再赞*/
    public static final int TRANSACTION_PICTURE_HAS_PRAISED = 1106;
    /** 私密照片需要查看过后才能发送赞*/
    public static final int TRANSACTION_PICTURE_NO_RIGHT = 1107;
    /** 自己不能赞自己*/
    public static final int TRANSACTION_PICTURE_CANNOT_PRAISE_SELF = 1108;
    /** 踩私照，已经踩过*/
    public static final int TRANSACTION_PICTURE_HAS_UNLIKED = 1109;
    /** 照片没有解锁，不能踩*/
    public static final int TRANSACTION_PICTURE_NOT_UNLOCK = 1110;
    
    /* 对话聊天1200-1299 */
    /** 接收方被冻结 */
    public static final int TRANSACTION_CHAT_RECEIVE_SIDE_FROZEN = 1200;
    /** 发送方被冻结 */
    public static final int TRANSACTION_CHAT_SEND_SIDE_FROZEN = 1201;
    /** 字数超过限制 */
    public static final int TRANSACTION_CHAT_WORD_TOO_MANY = 1202;
    /** 图片大小超限制 */
    public static final int TRANSACTION_CHAT_PIC_FILE_TOO_BIG = 1203;
    /** 语音大小超限制 */
    public static final int TRANSACTION_CHAT_VOICE_FILE_TOO_BIG = 1204;
    /** 视频大小超限制 */
    public static final int TRANSACTION_CHAT_VEDIO_FILE_TOO_BIG = 1205;
    /** 对方已经被自己加黑*/
    public static final int TRANSACTION_CHAT_ANOTHER_IS_BLOCKED = 1207 ;
    /** 命中关键字，发送不成功*/
    public static final int TRANSACTION_CHAT_KEYWORDS_BLOCKED = 1210 ;
    /** 没有头像不能与对方聊天*/
    public static final int TRANSACTION_CHAT_NO_AVATAR = 1212 ;
    /** 等级不够不能与对方聊天*/
    public static final int TRANSACTION_CHAT_LOW_LEVEL = 1213 ;
    /** 头像正在审核中还不能与对方聊天*/
    public static final int TRANSACTION_CHAT_AVATAR_CHECKING = 1214 ;
    
    
    /* 提现1300-1399 */
    /** 用户等级不够，不能提现 */
    public static final int TRANSACTION_WITHDRAW_LEVEAL_TOO_LOW = 1300;
    /** 错过申请提交日期 */
    public static final int TRANSACTION_WITHDRAW_MISS_DATE = 1301;
    /** 用户还没有现金收入 */
    public static final int TRANSACTION_WITHDRAW_NOT_CASH_INCOME = 1302;
    /** 需要上传身份证资料 */
    public static final int TRANSACTION_WITHDRAW_NEED_IDENTITY_AUTH = 1303;
    /** 身份证照片尺寸不符合要求 */
    public static final int TRANSACTION_WITHDRAW_PIC_DIMENS_ERROR = 1304;
    /** 身份证照片大小不符合要求 */
    public static final int TRANSACTION_WITHDRAW_PIC_SIZE_ERROR = 1305;
    /** 身份证号码错误 */
    public static final int TRANSACTION_WITHDRAW_IDENTITY_NUM_ERROR = 1306;
    /** 真实姓名错误 */
    public static final int TRANSACTION_WITHDRAW_IDENTITY_NAME_ERROR = 1307;
    /** 版本太低，无法申请提现，请升级到最新版本 **/
    public static final int TRANSACTION_WITHDRAW_IDENTITY_VERSION_ERROR = 1309;
    /** 上次申请在处理中，无需重复提交 **/
    public static final int TRANSACTION_WITHDRAW_IDENTITY_DOING = 1310;
    /** 因【根据警告类型填充内容】你被取消本月提现资格 **/
    public static final int TRANSACTION_WITHDRAW_IDENTITY_WARNING = 1311;
    /** 身份信息已通过审核，请完成视频验证 **/
    public static final int TRANSACTION_WITHDRAW_IDENTITY_VIDEO_ERROR = 1312;
    
    /* 送礼物 */
    /** 购买的王冠礼物比当前级别低 */
    public static final int TRANSACION_CROWN_LOWTO_NOW = 1500 ;
    /** 礼物不存在 */
    public static final int TRANSACTION_GIFT_UNEXIST = 1501 ;
    /** 限次数礼物已经发送完*/
    public static final int TRANSACTION_GIFT_SEND_VOER = 1502 ;
    
    /* 易信相关8000-8199 */
    // 0表示这次添加成功，双方成为好友关系；8001表示申请发送了，但是对方还没确认；8002表示之前双方就已经是好友关系了
    /** 手机号对应的易信号已被其它用户绑定 */
    public static final int TRANSACTION_YIXIN_MOBILE_BINE_BY_OTHER = 8000;
    /** 好友申请已发送，但是对方还没确认（此时客户端应判断本地是否安装了易信，据此出不同的提示语）*/
    public static final int TRANSACTION_YIXIN_SEND_FRIEND_APPLY = 8001;
    /** 之前双方就已经是好友关系了 */
    public static final int TRANSACTION_YIXIN_IS_FRIEND = 8002;
    /** 用户未注册（此时客户端应显示注册易信界面） */
    public static final int TRANSACTION_YIXIN_NO_REGISTER = 8003;
    /** 用户已注册 */
    public static final int TRANSACTION_YIXIN_HAS_REGISTER = 8004;
    /** 每天只能对同一用户发送一次好友申请 */
    public static final int TRANSACTION_YIXIN_APPLY_ONCE = 8005;
    /** 朋友数量超过限制 */
    public static final int TRANSACTION_YIXIN_FRIEND_OVERFLOW = 8006;
    /** 未验证易信手机帐号 */
    public static final int TRANSACTION_YIXIN_NO_BIND_MOBILE = 8007;
    /** 对方易信账号与你相同 */
    public static final int TRANSACTION_YIXIN_SAME = 8008;
}
