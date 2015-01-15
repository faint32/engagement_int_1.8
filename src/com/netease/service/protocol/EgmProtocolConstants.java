package com.netease.service.protocol;


/**
 * 协议相关的常量定义
 * @author echo_chen
 * @since  2014-03-17
 */

public class EgmProtocolConstants {
    
    public static final boolean RANDOM_DEBUG_DATA = false;
    
    //测试服务器和在线服务器开关
    public static boolean isDebugServer = true;
    
    //平台类型：0 IOS,1 Andriod
    public static final int PLATFORM_ANROID = 1;
    
    public static final int SIZE_MAX_PICTURE = 1280;
    public static final int SIZE_MIN_PICTURE = 480;
    public static final int SIZE_MAX_AVATAR = 1280;
    public static final int SIZE_MIN_AVATAR_FEMALE = 480;
    public static final int SIZE_MIN_AVATAR_MALE = 200;
    public static final int PIC_QULITY = 90;
    
    //在urs注册的产品名
    public static final String PRODUCT = "yuehui2_client";
    public static final String SYSTEM_NAME = "android";
    
    /** 登录用户类型*/
    public interface AccountType{
        public int Mobile = 0;
        public int Yuehui = 1;
        public int YiXin = 2;
    }
    /**
     * 第三方帐号类型，由urs定义
     * 腾讯= 1, 人人 = 2, 微博 = 3 腾讯微博 = 4, 奇虎360 = 5, 微信 = 6, 豆瓣= 7, 易信开放平台 = 8, 易信公众平台 = 9
     */
    public interface EXTERNAL_LOGIN_TYPE {
        int wechat = 6;// 微信
        int yixin_open = 8;// 易信开放平台
    }
    /**
     * 消息类型，聊天消息和push消息共用
     */
    public interface MSG_TYPE {
        int MSG_TYPE_TEXT = 0;// 文本
        int MSG_TYPE_PRIVATE_PIC = 1;// 私照
        int MSG_TYPE_LOCAL_PIC = 2;// 本地照片
        int MSG_TYPE_AUDIO = 3;// 音频
        int MSG_TYPE_VIDEO = 4;// 视频
        int MSG_TYPE_GIFT = 5;// 礼物
        int MSG_TYPE_SYS = 6;// 系统消息
        int MSG_TYPE_FACE = 7;// 表情消息
    }
    /**
     * 消息发送类型
     */
     public interface MSG_SENDTYPE {
    	 int MSG_SENDTYPE_COMMON = 0; // 普通消息
    	 int MSG_SENDTYPE_FIRE = 1; // 阅后即焚消息
     }
    /**
     * 系统消息类型，当消息类型是系统消息时附加的子类型
     */
    public interface SYS_MSG_TYPE {
        int SYSMSG_TYPE_FATE = 0;// 条件：碰缘分内容审核不通过； 跳转：碰缘分界面
        int SYSMSG_TYPE_INTRODUCE = 1;// 条件：自我介绍文字审核不通过；跳转：自我介绍文字填写界面
        int SYSMSG_TYPE_INTRODUCE_AUDIO = 2;// 条件：自我介绍语音审核不通过; 跳转：自我介绍语音录制界面
        int SYSMSG_TYPE_WITHDRAW_SUCCESS = 3;// 条件：提现成功;操作：跳转进网易宝账户界面（webview界面)
        int SYSMSG_TYPE_WITHDRAW_FAILED = 4;// 条件：提现申请被驳回
        int SYSMSG_TYPE_GOLD_WILL_EXPIRE = 5;// 条件：金币在30天后过期; 操作： 进入充值界面（跳浏览器）
        int SYSMSG_TYPE_PHOTO_NOT_PASS = 6;// 条件：照片/私照审核不通过; 操作：进入上传照片/私照流程（无需跳界面）
        int SYSMSG_TYPE_CASH_WILL_EXPIRE = 7;//条件：现金收入30天后过期; 操作：进入充值页面
        int SYSMSG_TYPE_ACTIVITIES = 8;//产品活动；操作：弹活动提醒页面
        int SYSMSG_TYPE_COMMON_TEXT = 9 ;//普通文本消息
        int SYSMSG_TYPE_ADD_YIXIN = 10 ;//加易信好友
        int SYSMSG_TYPE_INFORM_FEMALE_FRIEND = 11;//通知女性用户邀请朋友来赚钱
        int SYSMSG_TYPE_FIRE_SCREEN_SHOT_NOTICE = 12;//阅后即焚信息截屏系统通知
        int SYSMSG_TYPE_MSG_FIRED_NOTICE = 13;//阅后即焚信息系统通知发送方消息已被焚毁
        int SYSMSG_TYPE_INTRODUCE_VIDEO = 14;//视频自我介绍审核不通过
        int SYSMSG_TYPE_GET_SNOW_TIPS = 15;//情人节礼物领取
    }
    
    //加黑
    public interface Block_Type{
        public int BLOCK_CONFIRM = 0 ;
        public int BLOCK_CANCEL = 1 ;
    }
    //消息命中关键词类型
    public interface KeyWords_MatchType{
        public int MATCH_WEIXIN_IS_FRIENDS = 11330 ;//微信严禁词，且双方已是易信好友（发送失败）
        public int MATCH_WEIXIN_ISNOT_FRIENDS = 11331 ;//微信严禁词，且双方不是易信好友（发送失败）
        public int MATCH_MONEY = 1134 ;//命中财产关键词（发送成功，目前不用作处理）
    }
    //用户自我介绍类型
    public interface Introduce_Type{
	    	public static final int Introduce_Type_No = 0;//未录制
	    	public static final int Introduce_Type_Video = 1;//视频
	    	public static final int Introduce_Type_Audio = 2;//语音
    	
    }

    
}
