package com.netease.service.protocol;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.netease.common.http.THttpMethod;
import com.netease.common.http.THttpRequest;
import com.netease.common.http.Entities.FilePart;
import com.netease.common.http.Entities.MultipartEntity;
import com.netease.common.http.Entities.Part;
import com.netease.common.http.Entities.StringPart;
import com.netease.common.log.NTLog;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.app.BaseApplication;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.meta.AntiHarassmentInfo;
import com.netease.service.protocol.meta.MessageInfo;
import com.netease.util.PDEEngine;
import com.netease.util.PlatformUtil;


public class EgmProtocol
{
      
	public static String SERVER_DOMAIN = EgmProtocolConstants.isDebugServer?"http://t.y.163.com/":"http://y.163.com/";

	public static String URS_DOMAIN_HTTP = "http://reg.163.com/";
	public static String URS_DOMAIN_HTTPS = "https://reg.163.com/";
    private final static String sUserAgent = "NETS_Android";

    //反馈产品ID
    public static String FEED_PRODUCT_ID = "23001";

    private static String ZIP_LOG_FILE_NAME = "egm_android_log.zip";

    public static String PRODUCT_ID = "";
    public static String PROTOCOL_NUMBER = "1.0.0";
    
    private static EgmProtocol mInstance;
    //手机登录TOKEN（采用URS提供的手机版登录方案的TOKEN）
    private   String mUrsToken = null;
    //设备ID（采用URS提供的手机版登录方案返回的ID）
    private   String mUrsId = null;
    //应用服务器生成的秘钥，密钥使用16进制的字符串表示，（跟设备对应，与用户无关，每次应用启动获取一次）
    private   String encryptedKey = null;

    /**************************************API*******************************/
    /* ----------登录注册---------- */
    /** 初始化URS */
    private static final String URL_INIT_URS = URS_DOMAIN_HTTP + "services/initMobApp";
    /** 登录URS */
    private static final String URL_LOGIN_URS = URS_DOMAIN_HTTPS + "services/safeUserLoginForMob";
    
    /** 获取手机号验证码 */
    private static final String URL_GET_MOBILE_VERIFY_CODE = "login/getverifycode";
    /** 手机号注册 */
    private static final String URL_MOBILE_REGISTER = "login/register";
    /** 是否是同城约会1.0帐号 */
    private static final String URL_IS_YUEHUI_ACCOUNT = "login/ismember";
    /** 注册——补全用户资料 */
    private static final String URL_FILL_USER_INFO = "login/filluserinfo";
    /** 注册——绑定手机号 */
    private static final String URL_BIND_MOBILE = "login/bindmobile";
    /** 登录——上传地理位置并获取用户资料 */
    private static final String URL_LOGIN_GET_USER_INFO = "login/getmyinfo";
    /** 上传地理位置 */
    private static final String URL_UPLOAD_LOCATION = "login/uploadlocation";
    /** 退出登录 */
    private static final String URL_LOGOUT = "login/logout";
    /** 获取push所需参数 */
    private static final String URL_GET_PUSH_PARAMS = "login/getpushinfo";
    /** 获取手机绑定的通行证帐号 */
    private static final String URL_QUERY_ACCOUNT = "login/queryaccount";
    /**读取第三方登录用户资料接口,第三方账户第一次登录后跳到注册补全资料页面时调用 */
    private static final String URL_GET_OAUTHUSERINFO = "login/getoauthuserinfo";
    
    /* ------------推荐----------- */
    /** 获取登录之前的推荐列表 */
    private static final String URL_GET_RECOMMEND_WITHOUT_LOGIN = "/recommend/regrecommedlist";
    /** 获取推荐列表 */
    private static final String URL_GET_RECOMMEND = "recommend/getrecommendlist";
    /** 获取碰缘分状态数据 */
    private static final String URL_GET_YUANFEN_INFO = "fate/getfateinfo";
    /** 通知服务器碰缘分开关状态 */
    private static final String URL_INFORM_YUANFEN_SWITCHER = "fate/fateswitch";
    /** 通知服务器碰缘分类型 */
    private static final String URL_INFORM_YUANFEN_TYPE = "fate/fatetypeswitch";
    /** 上传碰缘分数据 */
    private static final String URL_SEND_YUANFEN = "fate/updatefate";
    /** 获取推荐页活动列表 */
    private static final String URL_GET_COMPETITION_LIST = "activity/getlist";
    /** 获取主页排行列表 */
    private static final String URL_GET_RANK_LIST_IN_HOME = "rank/getranklist";
    
    /* -----------排行榜---------- */
    /** 获取排行榜 */
    private static final String URL_GET_RANK = "rank/getrankuserlist";
    /** 搜索 */
    private static final String URL_SEARCH = "search/search";
    /** 排行榜背景图 */
    private static final String URL_RANK_PICTURE = "config/getrankpic";
    
    /* ----------用户资料---------- */
    /** 获取用户资料 */
    private static final String URL_GET_USER_INFO = "user/getuserinfo";
    /** 获取用户资料配置项 */
    private static final String URL_GET_USER_INFO_CONFIG = "config/getuserconfig";
    /** 获取系统头像列表 */
    private static final String URL_GET_SYSPORTRAIT_LIST = "config/getsysportrait";
    /** 设置系统头像 */
    private static final String URL_UPDATE_SYSPORTRAIT_LIST = "user/updatesysportrait";
    /** 获取礼物列表 */
    private static final String URL_GET_USER_GIFT_LIST = "gift/giftlist";
    /** 修改会员详细资料 */
    private static final String URL_MODIFY_USER_DETAIL_INFO = "user/updateuserinfo";
    /** 修改会员头像*/
    private static final String URL_MODIFY_USER_PROFILE = "user/updateportrait";
    /** 上传语音介绍*/
    private static final String URL_UPDATE_AUDIO_INTRODUCE = "user/uploadvoiceintroduce";
    /** 上传视频介绍*/
    private static final String URL_UPDATE_VIDEO_INTRODUCE = "user/uploadvideointroduce";
    /** 切换音频/视频*/
    private static final String URL_SWITH_AUDIO_VIDEO_MODE = "user/switchintroduce";
    /** 获取音频/视频*/
    private static final String URL_GET_AUDIO_VIDEO_MODE = "user/getintroduce";
    /** 删除语音介绍*/
    private static final String URL_DEL_AUDIO_INTRODUCE = "user/delvoiceintroduce";
    /** 获取表情配置数据*/
    private static final String URL_GET_EMOT_CONFIG = "config/getemotionconfig";
    /** 获取礼物配置数据*/
    private static final String URL_GET_GIFT_CONFIG = "config/getgiftconfig";
    /** 获取个人中心数据*/
    private static final String URL_GET_PRIVATE_DATA = "user/geteditinfo";
    /** 获取照片列表*/
    private static final String URL_GET_PICTURE_LIST = "album/getphotolist";
    /** 获取已经解锁私照列表*/
    private static final String URL_GET_UNLOCK_PICTURE_LIST = "album/getunlocklist";
    /** 获取单张私照*/
    private static final String URL_GET_SINGLE_PRI_IMAGE = "album/getphotoinfo";
    /** 赠送礼物*/
    private static final String URL_SEND_GIFT = "gift/sendgift";
    /** 赠送礼物*/
    private static final String URL_MODIFY_INTRODUCE = "user/updateintroduce";
    /** 上传照片*/
    private static final String URL_UPLOAD_PIC = "album/uploadphoto";
    /** 删除照片*/
    private static final String URL_DELETE_PIC = "album/delphoto";
    /** 赞私照*/
    private static final String URL_PRAISE_PIC = "album/praisephoto";
    /** 踩私照*/
    private static final String URL_UNLIKE_PIC = "album/step";
    /** 获取聊天列表*/
    private static final String URL_GET_CHAT_LIST= "chat/getlist";
    /** 删除聊天列表*/
    private static final String URL_DEL_CHAT_LIST= "chat/delchat";
    /** 聊天列表排序*/
    private static final String URL_SORT_CHAT_LIST= "chat/getsortlist";
    /** 删除消息*/
    private static final String URL_DEL_MSG= "chat/delmsg";
    /** 阅后即焚消息销毁接口*/
    private static final String URL_DEL_FIRE_MSG= "chat/firemsg";
    /** 发送消息*/
    private static final String URL_SEND_MSG= "chat/sendmsg";
    /** 获取阅后即焚消息多媒体链接接口*/
    private static final String URL_GET_FIRE_MESSAGE_MEDIA_URL= "chat/getfiremsg";
    /** 获取聊天和碰缘分文字模板*/
    private static final String URL_GET_TOPIC_DATA= "config/gettopic";
    /** 获取消息列表*/
    private static final String URL_GET_MSG_LIST= "chat/getmsglist";
    /** 举报*/
    private static final String URL_COMPLAIN= "setting/complain";
    /** 加黑*/
    private static final String URL_BLOCK= "setting/blockuser";
    /** 将消息设置为已读*/
    private static final String URL_SET_MSG_READ = "chat/setreadflag";
    /** 读取用户聊天技的列表 */
    private static final String URL_GET_TALK_SKILLS_LIST = "user/getchatskills";
    /** 读取用户聊天技的列表 */
    private static final String URL_UPDATE_TALK_SKILLS_LIST = "user/updatechatskills";
    /** 获取女性账户信息 */
    private static final String URL_GET_MONEY_ACCOUNT = "user/getaccountinfo";
    /** 女性申请提现 */
    private static final String URL_APPLY_WITHDRAW = "user/withdrawapply";
    /** 验证身份证 */
    private static final String URL_AUTH_IDENTITY = "user/setwithdrawaccount";
    /** 使用Token置换登录ticket */
    private static final String URL_EXCHANGE_TICKET = URS_DOMAIN_HTTP + "interfaces/mobileapp/exchangeTicketByMobToken.do";
    /** access_token置换手机登录token接口,暂时没用 */
    private static final String URL_EXCHANGE_MOBILE_TOKEN = URS_DOMAIN_HTTP + "outerLogin/oauth2/exchageMobLoginToken.do";
    /** 获取女性现金收支记录 */
    private static final String URL_GET_MONEY_HISTORY = "user/getmoneyrecords";
    /** 易信添加用户 */
    private static final String URL_YIXIN_ADD_FRIEND = "yixin/addfriend";
    /** 易信注册 */
    private static final String URL_YIXIN_REGISTER = "yixin/regyixin";
    /** 易信绑定，用于加易信好友验证手机号 */
    private static final String URL_YIXIN_BIND = "yixin/bindyixin";
    /** 查看小爱助手的易信消息 */
    private static final String URL_YIXIN_CHECK = "yixin/readyixinnotice";
    /** 查询邀请者*/
    private static final String URL_QUERY_INVITOR = "user/queryinviteuser";
    /* ------------设置------------- */
    //上传日志
    private static final String URL_POST_LOG = "http://fankui.163.com/ft/file.fb?op=up";
    //反馈
    private static final String URL_FEEDBACK = "setting/feedback";
    //检查版本更新
    private static final String URL_CHECKVERION = "setting/getversion";
    //读取用户防骚扰配置
    private static final String URL_GET_ANTIHARASSMENT = "setting/getantiharassment";
    //更新用户防骚扰配置
    private static final String URL_UPDATE_ANTIHARASSMENT = "setting/updateantiharassment";
    
	//日志上报
    private static final String URL_RECOMMAND_LOGGER = "recommend/logger";

    /**************************************************/


    EgmProtocol()
    {
    }



    public static EgmProtocol getInstance()
    {
        if (mInstance == null)
        {
            mInstance = new EgmProtocol();
        }
        return mInstance;
    }

    /**
     *
     * @return
     */
    public String getSever_Domain() {
        return SERVER_DOMAIN;
    }


    /**
     * 获取完整请求URL
     * @param url
     * @return
     */
    private String getRequestUrl(String url) {
        return SERVER_DOMAIN + url;
    }

    public  String getUrsToken() {
        if(TextUtils.isEmpty(mUrsToken)){
//            mUrsToken = EgmPrefHelper.getURSToken(BaseApplication.getAppInstance());
            mUrsToken = ManagerAccount.getInstance().getCurrentAccountToken();
        }
            
        return mUrsToken;
    }
    /**
     * 设置当前登录帐号的token
     * @param token
     */
    public  void setUrsToken(String token) {
        mUrsToken = token;
    }
    public  void setUrsId(String ursId) {
        mUrsId = ursId;
    }
    public  String getUrsId() {
        if(TextUtils.isEmpty(mUrsId))
            mUrsId = EgmPrefHelper.getURSId(BaseApplication.getAppInstance());
        return mUrsId;
    }
    public  void setEncryptedKey(String key) {
        encryptedKey = key;
    }
    public  String getEncryptedKey() {
        return encryptedKey;
    }
    
    /**
     * 初始化URS
     * @param product 必传 产品代号
     * @param pdtVersion 必传 产品版本
     * @param mac 必传 手机Mac地址
     * @param deviceType 必传 手机机型
     * @param systemName 必传 系统名称
     * @param systemVersion 必传 系统版本
     * @param resolution  必传 分辨率，格式：WIDTH*HEIGHT
     * @return
     */
    public THttpRequest createInitURSRequest(String product, String pdtVersion, String mac, 
            String deviceType, String systemName, String systemVersion, String resolution){
        EgmHttpRequest request = new EgmHttpRequest(URL_INIT_URS, THttpMethod.GET);
        request.addParameter("product", product);
        request.addParameter("pdtVersion", pdtVersion);
        request.addParameter("mac", mac);
        request.addParameter("deviceType", deviceType);
        request.addParameter("systemName", systemName);
        request.addParameter("systemVersion", systemVersion);
        request.addParameter("resolution", resolution);
        
        return request;
    }
    
    /**
     * 登录URS
     * @param ursId URS Id
     * @param params 加过密的用户名密码
     * @return
     */
    public THttpRequest createLoginURSRequest(String ursId, String params){
        EgmHttpRequest request = new EgmHttpRequest(URL_LOGIN_URS, THttpMethod.GET);
        
        request.addParameter("id", ursId);
        request.addParameter("params", params);
        
        return request;
    }
    
    /**
     * 获取手机号注册验证码
     * @param type 验证码类型：0 注册验证码（此时会返回AES key）; 1 手机绑定验证码
     * @param mobile 手机号
     */
    public THttpRequest createGetMobileVerifyCodeRequest(String type, String mobile){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_GET_MOBILE_VERIFY_CODE), THttpMethod.GET);
        request.addParameter("type", type);
        request.addParameter("mobile", mobile);
        
        return request;
    }
    
    /**
     * 手机号注册
     * @param encrypt 密文
     * @param mobile 手机号
     */
    public THttpRequest createMobileRegisterRequest(String mobile, String encrypt){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_MOBILE_REGISTER), THttpMethod.GET);
        request.addParameter("mobile", mobile);
        request.addParameter("encrypt", encrypt);
        
        return request;
    }
    
    /**
     * 判断是否是同城约会1.0帐号，并且是否已经验证（绑定）过手机号
     * @param account
     */
    public THttpRequest createIsYuehuiAccountRequest(){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_IS_YUEHUI_ACCOUNT), THttpMethod.GET);
        
        return request;
    }
    
    /**
     * 注册——补全用户资料
     * @param sex 性别
     * @param nick 昵称
     * @param avatarPath 头像文件地址
     * @param x 裁剪头像左上角的x坐标
     * @param y 裁剪头像左上角的y坐标
     * @param w 裁剪头像的宽度
     * @param h 裁剪头像的高度
     * @param birthday 生日
     * @param latitude 地理位置-纬度
     * @param longtitude 地理位置-经度
     * @param province 所在省份代码
     * @param city 所在城市代码
     * @param district 所在区代码
     */
    public THttpRequest createFillUserInfoRequest(int sex, String nick, String avatarPath, String x, String y, String w, String h, long birthday, 
            String inviteCode, String latitude, String longtitude, String province, String city ,String district){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_FILL_USER_INFO), THttpMethod.POST);
        
        ArrayList<Part> tempList = new ArrayList<Part>();
        
        if(!TextUtils.isEmpty(avatarPath) && !TextUtils.isEmpty(x) && !TextUtils.isEmpty(y) && !TextUtils.isEmpty(w) && !TextUtils.isEmpty(h)){
            File file = null;
            if (URLUtil.isFileUrl(avatarPath)) {
                file = new File(URI.create(avatarPath));
            } 
            else {
                file = new File(avatarPath);
            }
            
            FilePart filePart = null;
            try {
                filePart = new FilePart("portrait", file);
            } 
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            
            if(filePart != null){
                tempList.add(filePart);
                tempList.add(new StringPart("x", x));
                tempList.add(new StringPart("y", y));
                tempList.add(new StringPart("w", w));
                tempList.add(new StringPart("h", h));
            }
        }
        
        tempList.add(new StringPart("sex", String.valueOf(sex)));
        tempList.add(new StringPart("birthday", String.valueOf(birthday)));
        
        StringPart nickSp = new StringPart("nick", nick);
        nickSp.setCharSet("utf-8");
        tempList.add(nickSp);
        
        if(!TextUtils.isEmpty(inviteCode)){
            tempList.add(new StringPart("inviteCode", inviteCode));
        }
        
        if(!TextUtils.isEmpty(latitude)){
            tempList.add(new StringPart("latitude", latitude));
        }
        
        if(!TextUtils.isEmpty(longtitude)){
            tempList.add(new StringPart("longtitude", longtitude));
        }
        
        if(!TextUtils.isEmpty(province)){
            tempList.add(new StringPart("province", province));
        }
        
        if(!TextUtils.isEmpty(city)){
            tempList.add(new StringPart("city", city));
        }
        
        if(!TextUtils.isEmpty(district)){
            tempList.add(new StringPart("district", district));
        }
        
        Part[] parts = new Part[tempList.size()];
        for(int i = 0; i < tempList.size(); i++){
            parts[i] = tempList.get(i);
        }
        
        request.setHttpEntity(new MultipartEntity(parts));
        
        return request;
    }
    
    /**
     * 注册-绑定手机号
     * @param mobile 手机号
     * @param verifyCode 验证码
     */
    public THttpRequest createBindMobileRequest(String mobile, String verifyCode, String inviteCode){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_BIND_MOBILE), THttpMethod.POST);
        request.addParameter("mobile", mobile);
        request.addParameter("verifyCode", verifyCode);
        
        if(!TextUtils.isEmpty(inviteCode)){
            request.addParameter("inviteCode", inviteCode);
        }
        
        return request;
    }
    
    /**
     * 登录—上传地理位置并获取用户数据
     * @param latitude 纬度
     * @param longtitude 经度
     * @param province 省代码
     * @param city 市代码
     * @param district 区县代码
     * @return
     */
    public THttpRequest createLoginGetUserInfoRequest(String latitude, String longtitude, String provinceCode, String cityCode, String districtCode){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_LOGIN_GET_USER_INFO), THttpMethod.GET);
        if(!TextUtils.isEmpty(latitude)){
            request.addParameter("latitude", latitude);
        }
        
        if(!TextUtils.isEmpty(longtitude)){
            request.addParameter("longtitude", longtitude);
        }
        
        if(!TextUtils.isEmpty(provinceCode)){
            request.addParameter("province", provinceCode);
        }
        
        if(!TextUtils.isEmpty(cityCode)){
            request.addParameter("city", cityCode);
        }
        
        if(!TextUtils.isEmpty(districtCode)){
            request.addParameter("district", districtCode);
        }
        
        return request;
    }
    
    /**
     * 上传地理位置
     * @param latitude 纬度
     * @param longtitude 经度
     * @param province 省代码
     * @param city 市代码
     * @param district 区县代码
     * @return
     */
    public THttpRequest createUploadLocationRequest(String latitude, String longtitude, String provinceCode, String cityCode, String districtCode){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_UPLOAD_LOCATION), THttpMethod.POST);
        if(!TextUtils.isEmpty(latitude)){
            request.addParameter("latitude", latitude);
        }
        
        if(!TextUtils.isEmpty(longtitude)){
            request.addParameter("longtitude", longtitude);
        }
        
        if(!TextUtils.isEmpty(provinceCode)){
            request.addParameter("province", provinceCode);
        }
        
        if(!TextUtils.isEmpty(cityCode)){
            request.addParameter("city", cityCode);
        }
        
        if(!TextUtils.isEmpty(districtCode)){
            request.addParameter("district", districtCode);
        }
        
        return request;
    }
    
    /** 退出登录 */
    public THttpRequest createLogoutRequest(){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_LOGOUT), THttpMethod.GET);
        return request;
    }
    
    /** 获取push所需参数 */
    public THttpRequest createGetPushParamsRequest(){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_GET_PUSH_PARAMS), THttpMethod.GET);
        return request;
    }
    
    /** 获取推荐列表 */
    public THttpRequest createGetRecommendRequest(boolean isLogin){
        EgmHttpRequest request;
        
        if(isLogin){
            request = new EgmHttpRequest(getRequestUrl(URL_GET_RECOMMEND), THttpMethod.GET);
        }
        else{
            request = new EgmHttpRequest(getRequestUrl(URL_GET_RECOMMEND_WITHOUT_LOGIN), THttpMethod.GET);
        }
        
        request.setCacheDatabase();
        request.setCacheFile();
        
        return request;
    }
    
    /** 获取推荐活动列表 */
    public THttpRequest createGetCompetitionListRequest(){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_GET_COMPETITION_LIST), THttpMethod.GET);
        request.setCacheDatabase();
        request.setCacheFile();
        
        return request;
    }
    /** 获取主页面排行榜信息 */
    public THttpRequest createGetRankListInHomeRequest(String userId){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_GET_RANK_LIST_IN_HOME), THttpMethod.GET);
        request.setUrlLocalParam(userId);
        request.setCacheDatabase();
        request.setCacheFile();
        
        return request;
    }
    
    /**
     * 获取排行榜
     * @param rankId 排行榜Id
     * @param page 分页页码
     */
    public THttpRequest createGetRankRequest(int rankId, int rankType, int page){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_GET_RANK), THttpMethod.GET);
        request.addParameter("rankId", String.valueOf(rankId));
        request.addParameter("page", String.valueOf(page));
        request.addParameter("type", String.valueOf(rankType));
        
        return request;
    }
    
    /** 获取碰缘分状态数据 */
    public THttpRequest createGetYuanfenRequest(){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_GET_YUANFEN_INFO), THttpMethod.GET);
        return request;
    }
    
    /** 通知服务器碰缘分开关状态 */
    public THttpRequest createInformYuanfenSwitcherRequest(boolean isOpen){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_INFORM_YUANFEN_SWITCHER), THttpMethod.GET);
        request.addParameter("isOpen", String.valueOf(isOpen));
        
        return request;
    }
    
    /** 通知服务器碰缘分类型 */
    public THttpRequest createInformYuanfenTypeRequest(int fateType){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_INFORM_YUANFEN_TYPE), THttpMethod.GET);
        request.addParameter("fateType", String.valueOf(fateType));
        
        return request;
    }
    
    public THttpRequest createSendYuanfenRequest(int type, String voicePath, String text, int duration){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_SEND_YUANFEN), THttpMethod.POST);
        
        request.addParameter("type", String.valueOf(type));
        
        if(!TextUtils.isEmpty(voicePath)){
            File file = null;
            if (URLUtil.isFileUrl(voicePath)) {
                file = new File(URI.create(voicePath));
            } 
            else {
                file = new File(voicePath);
            }
            
            Part[] parts = null;
            FilePart filePart = null;
            try {
                filePart = new FilePart("voice", file);
            } 
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            
            parts = new Part[] {filePart, new StringPart("duration", String.valueOf(duration))};
            request.setHttpEntity(new MultipartEntity(parts));
        }
        
        if(!TextUtils.isEmpty(text)){
            Part[] parts = null;
            StringPart stringPart = new StringPart("text", text);
            stringPart.setCharSet("utf-8");
            parts = new Part[] {stringPart};
            request.setHttpEntity(new MultipartEntity(parts));
        }
        
        return request;
    }
    
    /**
     * 搜索
     */
    public THttpRequest createSearchRequest(int ageBegin, int ageEnd, int constellation, int provinceCode, int hasPrivatePic, int income, int page){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_SEARCH), THttpMethod.POST);
        
        request.addParameter("ageBegin", String.valueOf(ageBegin));
        request.addParameter("ageEnd", String.valueOf(ageEnd));
        
        if(constellation > 0){
            request.addParameter("constellation", String.valueOf(constellation));
        }
        
        if(provinceCode > 0){
            request.addParameter("province", String.valueOf(provinceCode));
        }
        
        if(income > -1){
            request.addParameter("income", String.valueOf(income));
        }
        
        if(hasPrivatePic > -1){
            request.addParameter("hasPrivatePhoto", String.valueOf(hasPrivatePic));
        }
        
        request.addParameter("page", String.valueOf(page));
        
        return request;
    }
    
    /**
     * 排行榜背景图片
     */
    public THttpRequest createGetRankPictureRequest(String version){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_RANK_PICTURE), THttpMethod.GET);
        
        if(!TextUtils.isEmpty(version)){
            request.addParameter("version", version);
        }
        
        request.setCacheDatabase();
//        request.setCacheFile();
        
        return request;
    }
    
    /** 获取个人中心女性账户信息 */
    public THttpRequest createGetMoneyAccountRequest(){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_GET_MONEY_ACCOUNT), THttpMethod.GET);
        return request;
    }
    
    /** 个人中心女性申请提现 */
    public THttpRequest createApplyWithdrawRequest(){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_APPLY_WITHDRAW), THttpMethod.GET);
        return request;
    }
    
    /** 验证身份证 */
    public THttpRequest createAuthIdentityRequest(String name, String idCardNo, 
    		String idCardPic1, String idCardPic2, String video){
        EgmHttpRequest request = new EgmHttpRequest(
        		getRequestUrl(URL_AUTH_IDENTITY), THttpMethod.POST);
        
        if(!TextUtils.isEmpty(name) && 
                !TextUtils.isEmpty(idCardNo) && 
                !TextUtils.isEmpty(idCardPic1) && 
                !TextUtils.isEmpty(idCardPic2) &&
                !TextUtils.isEmpty(video)){
            
            StringPart nameSp = new StringPart("name", name);
            nameSp.setCharSet("utf-8");
            StringPart idCardSp = new StringPart("idCardNo", idCardNo);
            
            Part[] parts = null;
            try {
                parts = new Part[] {nameSp, idCardSp, 
                		new FilePart("idCardPic1", getFile(idCardPic1)), 
                		new FilePart("idCardPic2", getFile(idCardPic2)),
                		new FilePart("video", getFile(video))};
                request.setHttpEntity(new MultipartEntity(parts));
            } 
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (! TextUtils.isEmpty(video)) {
        	 try {
        		 Part[] parts = new Part[] {new FilePart("video", getFile(video))};
				 request.setHttpEntity(new MultipartEntity(parts));
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
        
        return request;
    }
    
    /** 使用Token置换登录ticke */
    public THttpRequest createExchangeTicketRequest(String id, String params){
        EgmHttpRequest request = new EgmHttpRequest(URL_EXCHANGE_TICKET, THttpMethod.GET);
        if(!TextUtils.isEmpty(id)){
            request.addParameter("id", id);
        }
        
        if(!TextUtils.isEmpty(params)){
            request.addParameter("params", params);
        }
        
        return request;
    }
    /**access_token置换手机登录token接口 */
    public THttpRequest createExchangeMobileTokenRequest(String id, String params){
        EgmHttpRequest request = new EgmHttpRequest(URL_EXCHANGE_MOBILE_TOKEN, THttpMethod.GET);
        if(!TextUtils.isEmpty(id)){
            request.addParameter("id", id);
        }
        
        if(!TextUtils.isEmpty(params)){
            request.addParameter("params", params);
        }
        
        return request;
    }
    
    /**
     * 获取女性现金收支记录
     * @param type 记录类型
     * @param page 分页页码
     */
    public THttpRequest createGetMoneyHistoryRequest(int type, int page){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_GET_MONEY_HISTORY), THttpMethod.GET);
        request.addParameter("type", String.valueOf(type));
        request.addParameter("page", String.valueOf(page));
        
        return request;
    }
    
    /**
     * 易信添加用户
     * @param uid 添加用户的uid
     */
    public THttpRequest createYixinAddFriendRequest(long uid){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_YIXIN_ADD_FRIEND), THttpMethod.GET);
        request.addParameter("uid", String.valueOf(uid));
        
        return request;
    }
    
    /**
     * 易信注册
     * @param mobile 
     * @param encrypt
     */
    public THttpRequest createYixinRegisterRequest(String mobile, String encrypt){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_YIXIN_REGISTER), THttpMethod.GET);
        request.addParameter("mobile", mobile);
        request.addParameter("encrypt", encrypt);
        
        return request;
    }
    /**
     * 易信绑定
     * @param mobile 手机号
     * @param verifyCode 验证码
     */
    public THttpRequest createBindYixinRequest(String mobile, String verifyCode){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_YIXIN_BIND), THttpMethod.GET);
        request.addParameter("mobile", mobile);
        request.addParameter("verifyCode", verifyCode);
        return request;
    }
    /**
     * 查看小爱助手的易信消息
     */
    public THttpRequest createYixinCheckRequest(){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_YIXIN_CHECK), THttpMethod.GET);
        
        return request;
    }
    
    /**
     * 获取用户资料
     * @param uid
     * @return request
     */
    public THttpRequest createGetUserInfoRequest(long uid){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_GET_USER_INFO), THttpMethod.GET);
        request.addParameter("uid", String.valueOf(uid));
        
        return request;
    }
    /**
     * 反馈接口:反馈log
     *
     * @param
     * @return
     */
    public THttpRequest createPostLogReqeust()
    {
        THttpRequest request = new THttpRequest(URL_POST_LOG, THttpMethod.POST);
        File upload = null;
        boolean isZip = NTLog.zipLogFile(ZIP_LOG_FILE_NAME);
        if(isZip){
            upload = NTLog.openAbsoluteFile(ZIP_LOG_FILE_NAME);
            NTLog.i("createPostLogReqeust upload=", upload.toString());
        } else {
            NTLog.i("createPostLogReqeust()", "zip fail");
        }

        Part part1 = null;
        try {
            part1 = new FilePart("Filedata",ZIP_LOG_FILE_NAME, upload,"application/zip",null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        MultipartEntity entity = new MultipartEntity(new Part[]{part1});
        request.setHttpEntity(entity);
        return request;

    }
    /**
     * 反馈接口:反馈意见
     *
     * @param
     * @return
     */
    public THttpRequest createFeedBackReqeust(String pid,String fid,String user,String title,String content,String fileId,String contact,Context context){
        THttpRequest request = new THttpRequest(URL_FEEDBACK, THttpMethod.POST);

        request.addHeader("Content-Type", "application/x-www-form-urlencoded");
        request.addHeader("User-Agent",sUserAgent);

        String resolution = PlatformUtil.getResolution(context) + ";" + android.os.Build.MODEL + "/android" + android.os.Build.VERSION.RELEASE  + ";" +  EgmUtil.getNumberVersion(context);


        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("feedbackId", fid));
        list.add(new BasicNameValuePair("productId", pid));
        list.add(new BasicNameValuePair("userName", user));
        list.add(new BasicNameValuePair("title", title));
        list.add(new BasicNameValuePair("content", content));
        list.add(new BasicNameValuePair("resolution", resolution));
        if (fileId != null) {
            list.add(new BasicNameValuePair("fileId", fileId));
            list.add(new BasicNameValuePair("fileName", ZIP_LOG_FILE_NAME));
        }
        if(contact != null){
            list.add(new BasicNameValuePair("contact", contact));
        }

        try {
            request.setHttpEntity(new UrlEncodedFormEntity(list, "GB2312"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return request;
    }
    
    /**
     * 获取用户详细资料，包含公开照，私照等
     */
    public THttpRequest createGetUserInfoDetail(long uid){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_GET_USER_INFO), THttpMethod.GET);
        request.addParameter("uid", String.valueOf(uid));
        return request;
    }
    
    /**
     * 获取个人资料配置数据
     */
    public THttpRequest createGetUserInfoConfig(String version){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_GET_USER_INFO_CONFIG), THttpMethod.GET);
        if(!TextUtils.isEmpty(version)){
        	request.addParameter("version",version);
        }
        return request;
    }
    
    /**
     * 获取系统头像列表
     */
    public THttpRequest createGetSysPortraitList() {
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_GET_SYSPORTRAIT_LIST), THttpMethod.GET);
        request.setCacheDatabase();
        request.setCacheFile();
        return request;
    }
    
    /**
     * 设置系统头像
     */
    public THttpRequest createUpdateSysPortrait(int pid) {
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_UPDATE_SYSPORTRAIT_LIST), THttpMethod.POST);
        request.addParameter("pid",String.valueOf(pid));
        return request;
    }
    
    
    
    /**
     * 获取礼物列表
     */
    public THttpRequest createGetGiftList(int page){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_GET_USER_GIFT_LIST), THttpMethod.GET);
        request.addParameter("page",String.valueOf(page));
        return request;
    }
    
    /**
     * 获取照片列表
     */
    public THttpRequest createGetPictureList(String userId ,int type ,int page){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_GET_PICTURE_LIST), THttpMethod.GET);
        request.addParameter("userId",userId);
        request.addParameter("type",String.valueOf(type));
        request.addParameter("page",String.valueOf(page));
        return request;
    }
    
    /**
     * 获取已解锁私照列表
     */
    public THttpRequest createGetUnLockPicList(String userId ,String picId){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_GET_UNLOCK_PICTURE_LIST), THttpMethod.GET);
        request.addParameter("userId", userId);
        request.addParameter("picId", picId);
        return request;
    }
    
    
    
    /**
     * 修改会员详细资料
     * @throws JSONException 
     * @throws NumberFormatException 
     */
    public THttpRequest createModifyDetailInfo(HashMap<String,String> paramsList) throws NumberFormatException, JSONException{
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_MODIFY_USER_DETAIL_INFO), THttpMethod.POST);
        
        String[] spKeys = new String[]{"favorDate", "hobby", "skill"};
        
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        for (String key : spKeys) {
        	if(!TextUtils.isEmpty(paramsList.get(key))){
        		String[] idStr = paramsList.get(key).split(",");
        		for(int i = 0 ;i< idStr.length ;i++){
        			list.add(new BasicNameValuePair(key,idStr[i]));
        		}
        		
        		idStr = null ;
        	}
        	paramsList.remove(key);
        }
        
        //遍历其余参数
        Iterator<Entry<String,String>> iterator = paramsList.entrySet().iterator();
        while(iterator.hasNext()){
        	Entry<String,String> entry = iterator.next();
        	list.add(new BasicNameValuePair(entry.getKey(),entry.getValue()));
        }
        
        try {
            request.setHttpEntity(new UrlEncodedFormEntity(list, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        return request;
    }
    
    /**
     * 修改会员头像
     */
    public THttpRequest createModifyProfile(String filePath,String x ,String y,String w ,String h){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_MODIFY_USER_PROFILE), THttpMethod.POST);
        
        File file = null;
        if (URLUtil.isFileUrl(filePath)) {
            file = new File(URI.create(filePath));
        } else {
            file = new File(filePath);
        }
        
        Part[] parts = null;
        FilePart filePart = null;
		try {
			filePart = new FilePart("portrait", file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        parts = new Part[] {
                new StringPart("x", x),
                new StringPart("y", y),
                new StringPart("w", w),
                new StringPart("h", h),
                filePart
        };
        request.setHttpEntity(new MultipartEntity(parts));
        return request;
    }
    
    /**
     * 上传照片
     */
    public THttpRequest createUploadPic(String filePath,int picType, int isCamera){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_UPLOAD_PIC), THttpMethod.POST);
        
        File file = null;
        if (URLUtil.isFileUrl(filePath)) {
            file = new File(URI.create(filePath));
        } else {
            file = new File(filePath);
        }
        
        Part[] parts = null;
        FilePart filePart = null;
		try {
			filePart = new FilePart("picture", file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        parts = new Part[] {
                new StringPart("type",String.valueOf(picType)),
                new StringPart("isCamera",String.valueOf(isCamera)),
                filePart
        };
        request.setHttpEntity(new MultipartEntity(parts));
        return request;
    }
    
    /**
     * 上传视频
     * 
     * @param video
     * @param cover
     * @param duration
     * @param isCamera
     * @return
     */
    public EgmHttpRequest createUploadVideo(String video, String cover, 
    		int duration, int isCamera) {
    	EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_UPDATE_VIDEO_INTRODUCE), 
        		THttpMethod.POST);
    	
    	File videoFile = null;
        if (URLUtil.isFileUrl(video)) {
            videoFile = new File(URI.create(video));
        } else {
            videoFile = new File(video);
        }
        
        File coverFile = null;
        if (URLUtil.isFileUrl(cover)) {
        	coverFile = new File(URI.create(cover));
        } else {
        	coverFile = new File(cover);
        }
    	
        Part[] parts = null;
        FilePart filePart = null;
        FilePart coverPart = null;
        
		try {
			filePart = new FilePart("videoIntroduce", videoFile);
			coverPart = new FilePart("videoCover", coverFile);
		} catch (Exception e) {
		}
		
        parts = new Part[] {
                new StringPart("videoDuration",String.valueOf(duration)),
//                new StringPart("isCamera",String.valueOf(isCamera)),
                filePart,
                coverPart,
        };
        
        request.setHttpEntity(new MultipartEntity(parts));
        return request;
    }
    
    /**
     * 上传语音介绍
     */
    public THttpRequest createUpdateAudio(String audioPath,int duration){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_UPDATE_AUDIO_INTRODUCE), 
        		THttpMethod.POST);
        
        File file = null;
        if (URLUtil.isFileUrl(audioPath)) {
            file = new File(URI.create(audioPath));
        } else {
            file = new File(audioPath);
        }
        
        Part[] parts = null;
        FilePart filePart = null;
		try {
			filePart = new FilePart("voiceIntroduce", file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        parts = new Part[] {
                new StringPart("duration",String.valueOf(duration)),
                filePart
        };
        request.setHttpEntity(new MultipartEntity(parts));
        return request;
    }
    
    /**
     * 删除语音介绍
     */
    public THttpRequest createDelAudio(){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_DEL_AUDIO_INTRODUCE), THttpMethod.GET);
        return request;
    }
    
    /**
     * 切换状态
     */
    public THttpRequest createAudioVideoSwitch(int mode){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_SWITH_AUDIO_VIDEO_MODE), THttpMethod.GET);
        request.addParameter("introduceType",""+mode);
        return request;
    }
    
    /**
     * 切换状态
     */
    public THttpRequest createGetAudioVideoMode(){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_GET_AUDIO_VIDEO_MODE), THttpMethod.GET);
        return request;
    }
    /**
     * 获取表情配置数据
     */
    public THttpRequest createGetEmotConfig(String version, String faceVersion){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_GET_EMOT_CONFIG), THttpMethod.GET);
        if(!TextUtils.isEmpty(version)){
        	request.addParameter("version",version);
        }
        if(!TextUtils.isEmpty(faceVersion)){
        	request.addParameter("faceVersion",faceVersion);
        }
        return request;
    }
    
    /**
     * 获取礼物配置数据
     */
    public THttpRequest createGetGiftConfig(String version){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_GET_GIFT_CONFIG), THttpMethod.GET);
        if(!TextUtils.isEmpty(version)){
        	request.addParameter("version",version);
        }
        return request;
    }
    
    /**
     * 获取单张私照
     */
    public THttpRequest createGetSinglePriImage(long userId ,long picId){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_GET_SINGLE_PRI_IMAGE), THttpMethod.GET);
        request.addParameter("userId",String.valueOf(userId));
        request.addParameter("picId",String.valueOf(picId));
        return request;
    }
    
    /**
     * 获取个人中心用户信息
     */
    public THttpRequest createGetPrivateData(){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_GET_PRIVATE_DATA), THttpMethod.GET);
        request.setCacheDatabase();
        request.setCacheFile();
        return request;
    }
    
    /**
     * 赠送礼物
     */
    public THttpRequest createSendGift(
    		String toUserId ,
    		String giftId ,
    		String picId ,
    		int type)
    {
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_SEND_GIFT), THttpMethod.GET);
        request.addParameter("toUserId",toUserId);
        request.addParameter("giftId",giftId);
        request.addParameter("picId",picId);
        request.addParameter("type",String.valueOf(type));
        return request;
    }
    
    /**
     * 修改自我介绍
     */
    public THttpRequest createModifyIntr(String content){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_MODIFY_INTRODUCE), THttpMethod.POST);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("content", content));
        try {
            request.setHttpEntity(new UrlEncodedFormEntity(list, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return request;
    }
    
    /**
     * 删除照片
     */
    public THttpRequest createDeletePic(int type ,long[] pictureIds){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_DELETE_PIC), THttpMethod.POST);
        request.addParameter("type",String.valueOf(type));
        request.addParametersLong("pictureIds",pictureIds);
        return request;
    }
    
    /**
     * 赞私照
     */
    public THttpRequest createPraisePic(String userId ,String picId){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_PRAISE_PIC), THttpMethod.GET);
        request.addParameter("userId",userId);
        request.addParameter("picId",picId);
        return request;
    }
    
    /**
     * 踩私照
     */
    public THttpRequest createUnlikePic(String userId, String picId) {
    	EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_UNLIKE_PIC), THttpMethod.GET);
    	request.addParameter("userId",userId);
        request.addParameter("picId",picId);
        return request;
    }
    
    /**
     * 获取聊天列表
     */
    public THttpRequest createGetChatList(){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_GET_CHAT_LIST), THttpMethod.GET);
        return request;
    }
    
    /**
     * 删除聊天列表
     */
    public THttpRequest createDelChatList(String userId){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_DEL_CHAT_LIST), THttpMethod.GET);
        if(!TextUtils.isEmpty(userId)){
        	request.addParameter("userId",userId);
        }
        return request;
    }
    
    /**
     * 聊天列表排序
     */
    public THttpRequest createSortChatList(int sortType ,long[] uids){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_SORT_CHAT_LIST), THttpMethod.POST);
        request.addParameter("sortType",String.valueOf(sortType));
        request.addParametersLong("uids",uids);
        return request;
    }
    
    /**
     * 删除某条消息
     */
    public THttpRequest createDelMsg(long messageId,int delType){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_DEL_MSG), THttpMethod.GET);
        request.addParameter("messageId",String.valueOf(messageId));
        request.addParameter("delType",String.valueOf(delType));
        return request;
    }
    
    /**
     * 阅后即焚消息销毁接口
     */
    public THttpRequest createDeleteFireMessage(long msgId){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_DEL_FIRE_MSG), THttpMethod.GET);
        request.addParameter("messageId",String.valueOf(msgId));
        return request;
    }
    
    /**
     * 发送消息
     */
    public THttpRequest createSendMsg(MessageInfo msg, String filePath){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_SEND_MSG), THttpMethod.POST);
       
        Part[] parts = null;
        FilePart filePart = null;
        File file = null;
        if(!TextUtils.isEmpty(filePath)){
        	if (URLUtil.isFileUrl(filePath)) {
        		file = new File(URI.create(filePath));
        	} else {
        		file = new File(filePath);
        	}
        	try {
        		filePart = new FilePart("data", file);
        	} catch (FileNotFoundException e) {
        		e.printStackTrace();
        	}
        }
        
        List<Part> list = new ArrayList<Part>();
        list.add(new StringPart("msgType",String.valueOf(msg.type)));
        list.add(new StringPart("toUserId",String.valueOf(msg.receiver)));
        list.add(new StringPart("sendType", String.valueOf(msg.sendType)));
        
        addNotEmptyStringPart(list, "isCameraPhoto", msg.isCameraPhoto);
        
        if(!TextUtils.isEmpty(msg.msgContent)) {
        	String content = PDEEngine.PXEncrypt(msg.msgContent);
        	StringPart newPart = new StringPart("text", content);
        	newPart.setCharSet("utf-8");
        	list.add(newPart);
        }
        
        switch (msg.type) {
        case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_GIFT:
        	addNotEmptyStringPart(list, "giftId", msg.extraId);
        	break;
        case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_PRIVATE_PIC:
        	addNotEmptyStringPart(list, "privacyId", msg.extraId);
        	break;
        case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_AUDIO:
        case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_VIDEO:
        	list.add(new StringPart("duration", String.valueOf(msg.duration)));
        	break;
        case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_FACE:
        	StringPart newPart = new StringPart("faceId", msg.faceId);
        	newPart.setCharSet("utf-8");
        	list.add(newPart);
        	break;
        }
        
        if (filePart != null){
        	list.add(filePart);
        }
        
        parts = new Part[list.size()];
        for(int i = 0; i<parts.length ;i++){
        	parts[i] = list.get(i);
        } 
        
        request.setHttpEntity(new MultipartEntity(parts));
        return request;
    }
    
    private static void addNotEmptyStringPart(List<Part> list, String key, long value) {
    	if (value != 0) {
    		list.add(new StringPart(key, String.valueOf(value)));
    	}
    }
    
    private static void addNotEmptyStringPart(List<Part> list, String key, String value) {
    	if (! TextUtils.isEmpty(value)) {
    		list.add(new StringPart(key, String.valueOf(value)));
    	}
    }
    
    /**
     * 获取阅后即焚消息多媒体链接接口
     */
    public THttpRequest createGetFireMessageMediaUrl(long msgId){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_GET_FIRE_MESSAGE_MEDIA_URL), THttpMethod.GET);
        request.addParameter("messageId",String.valueOf(msgId));
        return request;
    }
    
    /**
     * 获取聊天以及碰缘分文字模板
     */
    public THttpRequest createGetTopicData(String version){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_GET_TOPIC_DATA), THttpMethod.GET);
        if(!TextUtils.isEmpty(version)){
        	request.addParameter("version",version);
        }
        return request;
    }
    
    /**
     * 获取消息列表
     */
    public THttpRequest createGetMsgList(long uid){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_GET_MSG_LIST), THttpMethod.GET);
        request.addParameter("uid",String.valueOf(uid));
        return request;
    }
    
    /**
     * 举报
     */
    public THttpRequest createComplain(int type ,String content ,long postId){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_COMPLAIN), THttpMethod.POST);
        List<Part> list = new ArrayList<Part>();
        list.add(new StringPart("type",String.valueOf(type)));
        list.add(new StringPart("postId",String.valueOf(postId)));
        if(!TextUtils.isEmpty(content)){
        	StringPart newPart = new StringPart("content",content);
        	newPart.setCharSet("utf-8");
        	list.add(newPart);
        }
        Part[] parts = new Part[list.size()];
        for(int i = 0; i<parts.length ;i++){
        	parts[i] = list.get(i);
        } 
        request.setHttpEntity(new MultipartEntity(parts));
        return request;
    }
    
    /**
     * 加黑
     */
    public THttpRequest createBlock(int type ,long bid){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_BLOCK), THttpMethod.GET);
        request.addParameter("type",String.valueOf(type));
        request.addParameter("bid",String.valueOf(bid));
        return request;
    }
    
    /**
     * 将消息设置为已读
     */
    public THttpRequest createSetMsgRead(long uid ,String messageId){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_SET_MSG_READ), THttpMethod.GET);
        request.addParameter("uid",String.valueOf(uid));
        if(!TextUtils.isEmpty(messageId)){
        	request.addParameter("messageId",String.valueOf(messageId));
        }
        return request;
    }
    
    /**
     * 读取用户聊天技的列表
     */
    public THttpRequest createGetTalkSkills(){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_GET_TALK_SKILLS_LIST), THttpMethod.GET);
        return request;
    }
    
    /**
     * 更新用户聊天技的列表
     */
    public THttpRequest createUpdateTalkSkills(int skills[]){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_UPDATE_TALK_SKILLS_LIST), THttpMethod.GET);
        request.addParametersInt("chatSkills", skills);
        return request;
    }
    
    
    /**
     * 提交反馈
     */
    public THttpRequest createFeedback(String content){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_FEEDBACK), THttpMethod.POST);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new BasicNameValuePair("networkInfo", PlatformUtil.getNetWorkName(BaseApplication.getAppInstance())));
        list.add(new BasicNameValuePair("operatorInfo", PlatformUtil.getOperatorName(BaseApplication.getAppInstance())));
        list.add(new BasicNameValuePair("deviceInfo", PlatformUtil.getMobileName()));
        list.add(new BasicNameValuePair("systemInfo", PlatformUtil.getOSVersion()));
        list.add(new BasicNameValuePair("content", content));
        try {
            request.setHttpEntity(new UrlEncodedFormEntity(list, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return request;
    }
    /**
     * 检查新版本
     */
    public THttpRequest createCheckVersion(String channel){
        EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_CHECKVERION), THttpMethod.GET);
        request.addParameter("channelId",channel);
        return request;
    }
    
    /**
     * 日志上报
     */
    public THttpRequest createRecommendLogger() {
        EgmHttpRequest request = new EgmHttpRequest(
        		getRequestUrl(URL_RECOMMAND_LOGGER), THttpMethod.POST);
        return request;
    }
    
    /**
     * 查询邀请者
     */
    public THttpRequest createQueryInvitor(long inviteCode) {
    	EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_QUERY_INVITOR), THttpMethod.GET);
    	request.addParameter("inviteCode", String.valueOf(inviteCode));
    	return request;
    }
    
    /**
     * 获取手机绑定的通行证帐号
     */
    public THttpRequest createQueryAccount(String mobile) {
    	EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_QUERY_ACCOUNT), THttpMethod.GET);
    	request.addParameter("mobile", mobile);
    	return request;
    	
    }
    /**
     * 获取手机绑定的通行证帐号
     */
    public THttpRequest createGetOauthUserInfo() {
    	EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_GET_OAUTHUSERINFO), THttpMethod.GET);
    	return request;
    }
    /**
     * 读取用户防骚扰配置
     */
    public THttpRequest createGetAntiHarassment() {
	    	EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_GET_ANTIHARASSMENT), THttpMethod.GET);
	    	return request;
    }
    public THttpRequest createUpadateAntiHarassment(AntiHarassmentInfo info) {
	    	EgmHttpRequest request = new EgmHttpRequest(getRequestUrl(URL_UPDATE_ANTIHARASSMENT), THttpMethod.GET);
	    	request.addParameter("noPortrait", String.valueOf(info.noPortrait));
	    	request.addParameter("levelLimit", String.valueOf(info.levelLimit));
	    	return request;
    }
    
    private static File getFile(String url) {
    	File file = null;
    	if (URLUtil.isFileUrl(url)) {
    		file = new File(URI.create(url));
        } 
        else {
        	file = new File(url);
        }
    	
    	return file;
    }
}
