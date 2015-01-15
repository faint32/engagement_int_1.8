package com.netease.engagement.app;

import com.netease.date.R;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmProtocolConstants;

/**
 * 主要存放UI和Bundle键值相关的常量
 */
public class EgmConstants {
    
    public static final boolean init_crashlytics = false;//是否开启异常捕获，调试时不开始，打签名包时角本里改成true
    /* 固化在客户端的url */
    /** 忘记密码的链接 */
    public static final String ACCOUNT_FORGET_PASSWORD_LINK = EgmProtocol.URS_DOMAIN_HTTP + "getpasswd/RetakePassword.jsp";
    /** 女性兑现使用条款 */
    public static final String URL_CASH_TERMS = "http://y.163.com/views/webviews/helper/datecashagreement.html";
    /** 服务协议 */
    public static final String URL_SERVICE_TERMS = "http://y.163.com/views/webviews/helper/dateuserpolicy.html";
    /** URS通过ticket登录的url */
    public static final String URL_URS_TICKET_LOGIN = EgmProtocol.URS_DOMAIN_HTTPS + "services/ticketlogin";
    /** 易信首页url */
    public static final String URL_YIXIN = "http://www.yixin.im/";
    /** 微信、易信等第三方登录 */
    public static final String URL_EXTERNAL_LOGIN = EgmProtocol.URS_DOMAIN_HTTP + "outerLogin/oauth2/connect.do";
    
    /** 内部webview里的返回按钮自定义url */
    public static final String URL_REDIRECT_BACK = "neteasedate://callback_personal_center";
    
    /** 内部webview里的实力榜跳转按钮自定义url */
    public static final String URL_REDIRECT_GOTO_STRENGTH_RANK = "neteasedate://callback_rank_page?rankId";
    
    /** 易信包名 */
    public static final String YIXIN_PACKAGE = "im.yixin";
	
	 /**设给cache模块的缓存根目录*/
    public static final String CACHE_CHILD_DIR = "/egm/.cache";
    public static final String TEMP_PROFILE_NAME = "temp_profile.jpg";
    
    /** 行政区域本地数据库raw资源id */
    public static final int AREA_DB_RAW_ID = R.raw.area;
    /** 该应用放置数据库文件的文件夹名称 */
    public static final String LOCAL_DATABASE_FOLDER = "databases";
    /** 搜索条件年龄的起始默认值 */
    public static final int SEARCH_START_AGE_DEFAULT = 18;
    /** 搜索条件年龄的终止默认值 */
    public static final int SEARCH_END_AGE_DEFAULT = 25;
    
    /** 默认胸围 */
    public static final int DEFAULT_BUST = 90;
    /** 默认罩杯 */
    public static final String DEFAULT_CUP = "B";
    /** 默认腰围 */
    public static final int DEFAULT_WAIST = 70;
    /** 默认臀围 */
    public static final int DEFAULT_HIP = 90;
    
	/*
	 * requestCode
     */
    public static final int REQUEST_BASE           = 0x1000; 
    /** 选择本地照片activity request码 */
    public static final int REQUEST_SELECT_PICTURE = REQUEST_BASE;
    /** 发送图片到预览界面的activity request码 */
    public static final int REQUEST_IMAGE_PREVIEW  = REQUEST_BASE + 1;
    /** 获取相机图片的Requset code */
    public static final int REQUEST_CAPTURE_PHOTO  = REQUEST_BASE + 2;
    /** 获取裁剪图片的Request code */
    public static final int REQUEST_CROP_IMAGE     = REQUEST_BASE + 3;
    public static final int REQUEST_RECORD_AUDIO = REQUEST_BASE + 4;//录制音频
    /** 选择地理位置 */
    public static final int REQUEST_SELECT_POSITION = REQUEST_BASE + 5;
    public static final int REQUEST_SELECT_VIDEO = REQUEST_BASE + 6;
    public static final int REQUEST_RECORD_VIDEO = REQUEST_BASE + 7;
    
    public static final int REQUEST_GET_PRI_PIC = REQUEST_BASE + 8 ;
    
    
    /** 获取相机图片的Requset code */
    public static final int REQUEST_CAPTURE_PHOTO_AVATAR  = REQUEST_BASE + 9;
    
    public static final int REQUEST_SELECT_PICTURE_AVATAR  = REQUEST_BASE + 10;
    
    public static final String EXTRA_PATH = "extra_path";//图片地址
    public static final String EXTRA_OPERATE = "extra_operate";
    public static final String EXTRA_DURATION = "extra_duration";
    
    public static final String EXTRA_SEX_TYPE = "extra_sex_type";
    
    public static final String EXTRA_SEARCH_SEX_TYPE = "extra_search_sex_type";
    public static final String EXTRA_SEARCH_AGE_BEGIN = "extra_search_age_begin";
    public static final String EXTRA_SEARCH_AGE_END = "extra_search_age_end";
    public static final String EXTRA_SEARCH_CONSTE = "extra_search_conste";
    public static final String EXTRA_SEARCH_PROVINCE = "extra_search_province";
    public static final String EXTRA_SEARCH_PRIVATE = "extra_search_private";
    public static final String EXTRA_SEARCH_INCOME = "extra_search_income";
    
    public static final int SELF_INTRODUCE_TEXT_MAX_LENGTH = 400; //个人介绍的最大字数
    public static final int PRIVATE_MSG_EDIT_MAX = 250 ;//私信的最大字数（文本内容）
    public static final int SOCIAL_TEXT_EDIT_MAX = 200 ;//社交网络地址最大字数
    public static long CHECK_CONFIG_DURATION = 2*60*60*1000 ;//2小时
    //聊天界面消息每次加载10条
    public static int CHAT_LIST_PAGE_NUM = 10 ;
    public static long MSG_INTERVAL = 5*60*1000 ;//5分钟
    
    /** notification跳转到界面的类型 */
    public static final int NOTIFICATION_CHAT_TYPE_ONE = 1;//跳转到聊天会话
    public static final int NOTIFICATION_CHAT_TYPE_MULTI = 2;//跳转到聊天列表
    public static final int NOTIFICATION_ACTIVITIES = 3;//营销活动
    /** 两次切换到推荐及聊天刷新的间隔时间 */
    public static final int TABSWITCH_REFRESH_INTERVAL = 5*60*1000;//5分钟
    /** 女性首次进入聊天发语音引导显示时长 */
    public static final int AUDIO_GUIDE_SHOW_DURATION = 5*1000;//5秒钟
    /** 个人展示视频录制最小及最大时长 */
    public static final int VIDEO_MIN_LENGH = 3;//3s
    public static final int VIDEO_MAX_LENGH = 10;//10s
    /**
     * 首发渠道id
     */
    public static final String CHANNEL_360 = "qihu360";
    public static final String CHANNEL_NDUO = "nduo";
    public static final String CHANNEL_GOAPK = "goapk";//安智市场
    public static final String CHANNEL_BAIDU = "baidu";
    public static final String CHANNEL_ANDROID91 = "android91";
    public static final String CHANNEL_HIAPK = "hiapk";//安卓市场
    public static final String CHANNEL_QQ = "qq";//应用宝
    
    /**
     * 存放Bundle key
     */
    public interface BUNDLE_KEY{
	    	String IMAGE_URL = "image_urls" ;
	    	String POSITION = "position" ;
	    	String WB_LOGIN_TYPE = "wb_login_type";//数据传递，跳转到微博登录界面的类型 绑定/第三方登录
	    	String WB_BIND_TYPE = "wb_type";//跳转到微博绑定界面的微博类型传递
        String CELLPHONE_TYPE = "cellphone_type"; // 跳转到输入手机号码界面的类型
        String CELLPHONE_NUMBER = "cellphone_number";//数据传递，手机号码
        String CELLPHONE_CAPTCHA = "cellphone_captcha";//数据传递，手机验证码
        
        String SELF_PAGE_TAGID = "self_page_tagid" ;//个人介绍页面tag
        String SELF_PAGE_CONTENT = "self_page_content";//个人介绍页面个人介绍内容
        String SELF_PAGE_USERINFO = "self_page_userinfo";
        String SELF_PAGE_LEVEL_TAG = "self_page_level_tag" ;
        String SELF_PAGE_SELECTINDEX = "self_page_selectindex" ;
        String SELF_PAGE_SELECTPAGE = "self_page_selectpage" ;
        String SELF_PAGE_CHOICE = "self_page_choice" ;//修改喜欢的约会，兴趣爱好，想学的技能，擅长的技能
        String SELF_PAGE_DATA = "self_page_data";
        String IMAGE_LIST_GIRL_PAGE = "image_list_girl_page";
        String IMAGE_LSIT_CAN_DELETE = "image_list_can_delete";
        String NEED_STATISTIC = "need_statistic";
        
        String USER_INFO = "user_info";
        String CHAT_ITEM_USER_INFO = "chat_item_userinfo";
        String USER_ID = "user_id";//会员id
        String USER_GENDER = "user_gender" ;//性别
        String PICTURE_INFO = "picture_info" ;//PictureInfo
        String PICTURE_INFO_LIST = "picture_info_list" ;
        String CHAT_FROM_SCREEN_SHOT_NOTIFICATION = "CHAT_FROM_SCREEN_SHOT_NOTIFICATION";
        
        String PRIVATE_IMAGE_LIST = "private_image_list" ;//私照列表
        String AUDIO_FILE_PATH = "audio_file_path" ;//录制的音频的地址
        String AUDIO_FILR_DURATION = "audio_file_duration";//录制的音频的时长
        String AUDIO_RECORDER_NAME= "adudio_recorder_name";//录音路径
        String VIDEO_INFO= "video_info" ;//视频文件结构
        String SYSTEM_ACTION_URL= "system_action_url" ;//活动url
        String SYSTEM_ACTION_TITLE = "system_action_url_title" ;//活动主题
        String CHAT_IMAGE_IS_PRIVATE = "chat_image_is_private" ;
        String CHAT_PRIVATE_IMAGE_ID = "chat_private_image_id" ;
        
        String CHAT_IMAGE_URL = "chat_image_url" ;//聊天界面大图路径
        String CHAT_VIDEO_PATH = "chat_video_path" ;//聊天视频路径
        String CHAT_VIDEO_FOR_FIRE = "chat_video_for_fire" ;//聊天视频路径
        String CHAT_VIDEO_DURATION = "chat_video_duration" ;//聊天视频时长
        String UPDATE_URL = "update_url" ;//跳转到下载页面
        String DOWNLOAD_CANCELABLE = "download_cancelable" ;//下载是否可以取消
        String PUSH_ACTIVITY_CONTENT = "push_activity_content" ;//产品活动内容
        String PUSH_ACTIVITY_EXTRA = "push_activity_extra" ;//产品活动扩展
        
        String SELF_INTRODUCE_TYPE = "self_introduce_type" ;//自我介绍类型
        String EXTERNAL_LOGIN_ACC_TYPE = "external_login_acc_type";//跳转到第三方登录的帐号类型
        
        String MESSAGE_INFO = "message_info";
        String FIRE_TYPE = "fire_type";
        String FIRE_START_TIME = "fire_start_time";
        
        String CHAT_IMAGE_IS_CAMERA_PHOTO = "chat_image_is_camera" ;
        
        
        String PIC_UPLOAD_MODE = "pic_upload_mode_tag" ;
        String SELECT_VIDEO_TYPE = "select_video_type" ;//跳转到选择视频界面的类型
    }
    
    public interface LOOPBACK_TYPE {
        int acc_logout = 1;// 退出帐号
        int chat_send_pri_pic = 2 ;//聊天界面发送私照
        int msg_resend = 3 ;//重新发送消息
        int msg_delete = 4 ;//删除消息
        int send_gift = 5 ;//发送礼物
        int usercp_change = 6 ;//亲密度改变，聊天界面亲密度改变，个人中心跟着改变
        int pri_pic_unlocked = 7 ;//私照解锁后，个人主页私照改变
        int pri_pic_praise = 8; //私照大图点赞成功后，列表的赞数改变
        int pri_pic_unlike = 9; //私照大图点踩成功后，列表的擦数改变
        int msg_fire_delete = 10; //阅后即焚消息销毁
        int change_audiostrem = 11; //听筒和扬声器模式切换
        int update_talk_skill = 12; //更新聊天技
    }
    /** save key 进程被杀需要save的内容 */
    public static final String KEY_TAB_INDEX = "key_chat_tab_index";
    public static final String KEY_IS_ONLYNEW = "key_is_onlynew";
    /** save key */
    
    public static final int SIZE_MAX_PICTURE = EgmProtocolConstants.SIZE_MAX_PICTURE;
    public static final int SIZE_MIN_PICTURE = EgmProtocolConstants.SIZE_MIN_PICTURE;
    public static final int SIZE_MAX_AVATAR = EgmProtocolConstants.SIZE_MAX_AVATAR;
    public static final int SIZE_MIN_AVATAR_FEMALE = EgmProtocolConstants.SIZE_MIN_AVATAR_FEMALE;
    public static final int SIZE_MIN_AVATAR_MALE = EgmProtocolConstants.SIZE_MIN_AVATAR_MALE;
    public static final int SIZE_MIN_AVATAR_CROPED = 200;
    
    /** 私照最大张数 */
    public static final int COUNT_MAX_PRIVATE_PICTURE = 300;
    /** 公照最大张数 */
    public static final int COUNT_MAX_PUBLIC_PICTURE = 10;
    /** 每次连续上传的最多张数 */
    public static final int COUNT_MAX_UPLOAD_PICTURE = 9;
    /** 主界面四个tab index */
    public static final int INDEX_RECOMMEND = 0;
    public static final int INDEX_DISCOVER = 1;
    public static final int INDEX_CHAT = 2;
    public static final int INDEX_MYSELF = 3;
    /** 搜索的最小年龄 */
    public static final int SEARCH_AGE_MIN = 18;
    /** 搜索的最大年龄 */
    public static final int SEARCH_AGE_MAX = 50;
    
    
    //聊天列表排序类型
    public interface Chat_Sort_Type{
    	public int TYPE_TIME = 0 ;
    	public int TYPE_RICH = 1 ;
    	public int TYPE_IN = 2 ;
    }
    
    public interface System_Sender_Id{
    	public int TYPE_XIAOAI = 0 ;
    	public int TYPE_YIXIN = 1 ;
    }
    
    //聊天界面缩略图宽度
    public interface Chat_Image_Width{
    	public int MAX_WIDTH = 240 ;
    	public int MIN_WIDTH = 120 ;
    }
    
    public interface Cannot_Chat_Type{
    	public int BLOCK = 1 ;
    	public int DONGJIE = 2 ;
    }
    
    //举报
    public interface Complain_Type{
    	public int COMPLAIN_COMPLAINT = 0 ;  // 其他（对应原来的举报投诉）
    	public int COMPLAIN_CHEAT = 1 ;  // 诈骗（对应原来的诈骗钱财）
    	public int COMPLAIN_PORN = 2 ;  // 淫秽图片（对应原来的色情交易）
    	public int COMPLAIN_ADVERTISEMENT = 3 ; // 商业广告
    	public int COMPLAIN_INSULT = 4 ; // 文字语音不文明侮辱他人
    	public int COMPLAIN_SHAM = 5 ; // 资料虚假
    	public int COMPLAIN_POLITICAL = 6 ; // 不当政治言论
    	public int COMPLAIN_SEXTRADE = 7 ; // 性交易
    }
    
    //消息发送状态
    public interface Sending_State{
    	public int SENDING = 0 ;
    	public int SEND_SUCCESS = 1 ;
    	public int SEND_FAIL = 2 ;
    }
    
    //聊天视频大小
    public interface Video_Size{
    	public int MAX_SIZE = 15 * 1024 *1024 ;
//    	public int MAX_DURATION = 60 * 1000 ;
    	public int MAX_DURATION = 61 * 1000 ;   // bug fix #141199
    }
  //个人展示视频大小
    public interface VIDEO_SIZE_MY_SHOW{
	    	public static final int MAX_SIZE = 200 * 1024 *1024 ;
	    	public static final int MAX_DURATION = (5*60+1) * 1000 ;   // 五分钟
	    	public static final int MIN_DURATION = VIDEO_MIN_LENGH *1000;   
	    	public static final int MIN_SIZE = 100 *1024 ;//最小100k
	    	public static final int MIN_RESOLUTION = 400;//最小边长
    }
  //选择视频类型
    public interface SELEC_VIDEO_TYPE{
    	public int TYPE_CHAT = 0 ;//聊天
    	public int TYPE_MY_SHOW = 1 ;//个人展示
    }
    
    //照片类型
    public interface Photo_Type{
    	public int TYPE_PUBLIC = 0 ;
    	public int TYPE_PRIVATE = 1 ;
    	public int TYPE_AVATAR = 2 ;
    	public int TYPE_CHAT_PUBLIC_PIC = 3 ;
    }
    
    //删除的消息类型
    public interface Del_Msg_Type{
    	public int TYPE_RECEIVE = 0 ;
    	public int TYPE_SEND = 1 ;
    }
    
    //发送礼物源头
    public interface Send_Gift_From{
    	public int TYPE_UNLOCK_PRIVATE_IMAGE = 0 ;
    	public int TYPE_USERPAGE_SEND_GIFT = 1 ;
    }
    
    //录音源头
    public interface Audio_Res{
    	public int RES_SELF_PAGE = 1;
    	public int RES_PRIVATE_MSG = 2;
    }
    
    public interface Height_Range{
    	public int MAX = 250 ;
    	public int MIN = 120 ;
    	public int DEFAULT = 160 ;
    }
    
    public interface Weight_Range{
    	public int MAX = 180 ;
    	public int MIN = 25 ;
    	public int DEFAULT = 50 ;
    }
    
    public interface Waist_Range{
    	public int MAX = 150 ;
    	public int MIN = 50 ;
    	public int DEFAULT = 50 ;
    }
    
    public interface WB_LOGIN_TYPE {
        int Bind = 0;// 绑定
        int Login = 1;// 登录
    }
    public interface INPUT_CELLPHONE_TYPE {
        public int Register = 1;// 手机号注册
        public int FindPw = 2;// 找回密码
    }
    
//    /** 帐号类型 */
//    public interface AccountType{
//        public int Mobile = 0;
//        public int Yuehui = 1;
//        public int YiXin = 2;
//    }
    
    /** 性别类型 */
    public interface SexType{
        public int Female = 0;
        public int Male = 1;
    }

	public interface Img_Size {
		public int IMG_SIZE_BIG = 0;
		public int IMG_SIZE_SMALL = 1;
	}

    /** 碰缘分类型 */
    public interface YuanfenType{
        public int Voice = 0;
        public int Text = 1;
    }
    
    /** 排行榜类型 */
    public interface RankID{
        /** 新秀榜 */
        public int NEW_FEMALE = 0;
        /** 魅力榜 */
        public int HOT = 1;
        /** 红人榜 */
        public int STAR = 2;
        /** 女神榜 */
        public int TOP_FEMALE = 3;
        /** 新贵榜 */
        public int NEW_MALE = 4;
        /** 富豪榜 */
        public int TOP_MALE = 5;
        /** 实力榜 */
        public int STRENGTH_MALE = 6;
        /** 私照榜 */
        public int PRIVATE_PIC_FEMALE = 7;
    }

	//榜单列表类型 日榜/月榜
	public interface RankListType {

		public int RANK_LIST_DAY = 0;
		public int RANK_LIST_MONTH = 1;
	}
    /** 金钱记录类型 */
    public interface MoneyRecordType{
        /** 全部（收入和兑现） */
        public int ALL = 0;
        /** 收入 */
        public int INCOME = 1;
        /** 兑现 */
        public int CASH = 2;
    }
    
    /** 资产明细类型 */
    public interface MoneyType{
        /** 收入 */
        public int INCOME = 0;
        /** 兑现 */
        public int CASH = 1;
        /** 扣除 */
        public int DEDUCT = 2;
    }
    
    /** 分享类型 */
    public interface ShareType{
        /** 微信 */
        public int WeChat = 0;
        /** 易信 */
        public int YiXin = 1;
        /** 微博 */
        public int WeiBo = 2;
    }
    
    /** 头像审核状态 */
    public interface PortraitStatus {
    	/** 未上传 */
    	public int NotUpload = 0;
    	/** 待审核 */
    	public int Waitting = 1;
    	/** 审核成功 */
    	public int Success = 2;
    	/** 审核失败 */
    	public int Fail = 3;
    }
    
    /** 照片审核状态 */
    public interface PhotoStatus {
    	/** 未审核 */
    	public int Waitting = 0;
    	/** 审核通过 */
    	public int Success = 1;
    	/** 审核未通过 */
    	public int Fail = 2;
    }
    
    /** 是否是现拍照片 */
    public interface IsCameraPhotoFlag {
    	/** 非现拍照片 */
    	public int OtherPhoto = 0;
    	/** 现拍照片 */
    	public int CameraPhoto = 1;

    }
}
