package com.netease.service.protocol;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.os.Parcelable;

import com.netease.common.http.HttpDataChannel;
import com.netease.common.http.HttpEngine;
import com.netease.common.service.BaseService;
import com.netease.common.task.Transaction;
import com.netease.common.task.TransactionEngine;
import com.netease.service.protocol.meta.AntiHarassmentInfo;
import com.netease.service.protocol.meta.ChatItemUserInfo;
import com.netease.service.protocol.meta.LoopBack;
import com.netease.service.protocol.meta.MessageInfo;
import com.netease.service.transactions.ApplyWithdrawTransaction;
import com.netease.service.transactions.AudioVedioSwitchTransaction;
import com.netease.service.transactions.AuthIdentityTransaction;
import com.netease.service.transactions.BlockTransaction;
import com.netease.service.transactions.CheckVersionTransaction;
import com.netease.service.transactions.ComplainTransaction;
import com.netease.service.transactions.DelAudioIntrTransaction;
import com.netease.service.transactions.DelChatItemTransaction;
import com.netease.service.transactions.DelFireMsgTransaction;
import com.netease.service.transactions.DelMsgTransaction;
import com.netease.service.transactions.DeletePicTransaction;
import com.netease.service.transactions.DownloadMsgResTransaction;
import com.netease.service.transactions.DownloadResTransaction;
import com.netease.service.transactions.EgmBaseTransaction;
import com.netease.service.transactions.ExchangeTicketFromUrsTransaction;
import com.netease.service.transactions.FeedBackTransaction;
import com.netease.service.transactions.GetAntiHarassmentTransaction;
import com.netease.service.transactions.GetAudioVedioModeTransaction;
import com.netease.service.transactions.GetChatListTransaction;
import com.netease.service.transactions.GetChatSortListTransaction;
import com.netease.service.transactions.GetCompetitionListTransaction;
import com.netease.service.transactions.GetEmotConfigTransaction;
import com.netease.service.transactions.GetFireMessageMediaUrl;
import com.netease.service.transactions.GetGiftConfigTransaction;
import com.netease.service.transactions.GetGiftListTransaction;
import com.netease.service.transactions.GetMobileVerifyTransaction;
import com.netease.service.transactions.GetMoneyAccountTransaction;
import com.netease.service.transactions.GetMoneyHistoryTransaction;
import com.netease.service.transactions.GetMsgListTransaction;
import com.netease.service.transactions.GetOauthUserInfoTransaction;
import com.netease.service.transactions.GetPictureListTransaction;
import com.netease.service.transactions.GetPrivateDataTransaction;
import com.netease.service.transactions.GetPushParamsTransaction;
import com.netease.service.transactions.GetRankInfoInHomeTransaction;
import com.netease.service.transactions.GetRankPictureTransaction;
import com.netease.service.transactions.GetRankTransaction;
import com.netease.service.transactions.GetRecommendTransaction;
import com.netease.service.transactions.GetSinglePriImageTransaction;
import com.netease.service.transactions.GetSysPortraitListTransaction;
import com.netease.service.transactions.GetTalkSkillsTransaction;
import com.netease.service.transactions.GetTopicDataTransaction;
import com.netease.service.transactions.GetUnLockPicListTransaction;
import com.netease.service.transactions.GetUserInfoConfigTransaction;
import com.netease.service.transactions.GetUserInfoDetailTransaction;
import com.netease.service.transactions.GetUserInfoTransaction;
import com.netease.service.transactions.GetYuanfenInfoTransaction;
import com.netease.service.transactions.InfoYuanfenSwitcherTransaction;
import com.netease.service.transactions.InfoYuanfenTypeTransaction;
import com.netease.service.transactions.LoginGetUserInfoTransaction;
import com.netease.service.transactions.LoginIsYuehuiAccountTransaction;
import com.netease.service.transactions.LogoutTransaction;
import com.netease.service.transactions.LoopBackTransaction;
import com.netease.service.transactions.ModifyDetailInfoTransaction;
import com.netease.service.transactions.ModifyIntrTransaction;
import com.netease.service.transactions.ModifyProfileTransaction;
import com.netease.service.transactions.PraisePicTransaction;
import com.netease.service.transactions.PushMsgTransaction;
import com.netease.service.transactions.QueryAccountTransaction;
import com.netease.service.transactions.QueryInvitorTransaction;
import com.netease.service.transactions.RegisterBindMobileTransaction;
import com.netease.service.transactions.RegisterFillUserInfoTransaction;
import com.netease.service.transactions.RegisterMobileTransaction;
import com.netease.service.transactions.SearchTransaction;
import com.netease.service.transactions.SendGiftTransaction;
import com.netease.service.transactions.SendMsgTransaction2;
import com.netease.service.transactions.SendYuanfenTransaction;
import com.netease.service.transactions.SetMsgReadTransaction;
import com.netease.service.transactions.URSInitTransaction;
import com.netease.service.transactions.URSLoginTransaction;
import com.netease.service.transactions.UnlikePicTransaction;
import com.netease.service.transactions.UpdateAntiHarassmentTransaction;
import com.netease.service.transactions.UpdateAudioIntrTransaction;
import com.netease.service.transactions.UpdateSysPortraitTransaction;
import com.netease.service.transactions.UpdateTalkSkillsTransaction;
import com.netease.service.transactions.UpdateVideoIntrTransaction;
import com.netease.service.transactions.UploadLoctaionTransaction;
import com.netease.service.transactions.UploadPicTransaction;
import com.netease.service.transactions.YixinAddFriendTransaction;
import com.netease.service.transactions.YixinCheckTransaction;
import com.netease.service.transactions.YixinRegisterTransaction;
import com.netease.service.transactions.YxBindTransaction;

public class EgmService extends BaseService {
    public static final String TAG = "EgmService";

    private static EgmService mInstance = null;
    private GroupTransactionListener mGroupListener;
    
    /**
     * TransactionEngine核心线程个数
     */
    public static int CoreThreadCount = 3;

    /**用于后台重登录时需要等待登录成功才能执行的请求缓存*/
    private AtomicBoolean mIsLoging = new AtomicBoolean(false);
    private LinkedList<Transaction> mWattingLoginList;


    public static EgmService getInstance() {
        if (mInstance == null) {
            mInstance = new EgmService();
        }
        return mInstance;
    }

    private EgmService() {
        super(new HttpDataChannel(new TransactionEngine(TransactionEngine.Priority, CoreThreadCount), new HttpEngine(
                3, Thread.NORM_PRIORITY - 1)));
        mGroupListener = new GroupTransactionListener();
        mWattingLoginList = new LinkedList<Transaction>();
    }

    public void addListener(EgmCallBack listener){
        mGroupListener.addListener(listener);
    }

    public void removeListener(EgmCallBack listener){
        mGroupListener.removeListener(listener);
    }
    public void addToWattingList(Transaction trans){
        synchronized (mWattingLoginList) {
            mWattingLoginList.add(trans);
        }
    }
    public LinkedList<Transaction> getWattingList(){
        synchronized (mWattingLoginList) {
            LinkedList<Transaction> list = new LinkedList<Transaction>(mWattingLoginList);
            mWattingLoginList.clear();
            return list;
        }
    }
    @Override
    public int beginTransaction(Transaction trans) {
        trans.setListener(mGroupListener);
        return super.beginTransaction(trans);
    }
    public void setIsLoging(boolean isloging){
        mIsLoging.set(isloging);
    }
    public boolean isLoging() {
        return mIsLoging.get();
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
    public int doInitURS(){
        
        URSInitTransaction t = new URSInitTransaction();
        beginTransaction(t);
        
        return t.getId();
    }
    
    /**
     * 登录URS
     * @param ursId URS id
     * @param ursKey URS key
     * @param name 用户名
     * @param password 密码
     * @return
     */
    public int doLoginURS(String name, String password){
        URSLoginTransaction t = new URSLoginTransaction(name, password);
        beginTransaction(t);
        
        return t.getId();
    }

    /**
     * 获取手机号注册验证码
     * @param type 验证码类型：0 注册验证码（此时会返回AES key）; 1 手机绑定验证码
     * @param mobile 手机号
     * @return
     */
    public int doGetMobileVerifyCode(int type, String mobile){
        GetMobileVerifyTransaction t = new GetMobileVerifyTransaction(type, mobile);
        beginTransaction(t);
        
        return t.getId();
    }
    
    /**
     * 手机号注册
     * @param mobile 手机号
     * @param passward 密码
     * @param verifyCode 验证码
     * @param inviteCode 邀请码
     * @param key 加密密钥
     * @return
     */
    public int doRegisterMobile(String mobile, String password, String verifyCode, String key){
        RegisterMobileTransaction t = new RegisterMobileTransaction(mobile, password, verifyCode, key);
        beginTransaction(t);
        
        return t.getId();
    }
    
    /**
     * 判断是否是同城约会1.0帐号，以及是否绑定了手机号
     * @param account
     * @return
     */
    public int doGetIsYuehuiAccount(){
        LoginIsYuehuiAccountTransaction t = new LoginIsYuehuiAccountTransaction();
        beginTransaction(t);
        
        return t.getId();
    }
   
    /**
     * 注册——补全用户资料
     * 如果没有数据，那就不要上传，用空字符串或者null表示。
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
    public int doFillUserInfo(String userName, String password, String token, int accountType, 
            int sexType, String nick, String avatarPath, String x, String y, String w, String h, long birthday, 
            String inviteCode, String latitude, String longtitude, String province, String city ,String district){
        
        RegisterFillUserInfoTransaction t = new RegisterFillUserInfoTransaction(userName, password, token, accountType,
                sexType, nick, avatarPath, 
                x, y, w, h, birthday, inviteCode, latitude, longtitude, province, city, district);
        beginTransaction(t);
        
        return t.getId();
    }
    
    /**
     * 绑定手机号
     * @param mobile 手机号
     * @param verifyCode 验证码
     */
    public int doBindMobile(String mobile, String verifyCode, String inviteCode){
        RegisterBindMobileTransaction t = new RegisterBindMobileTransaction(mobile, verifyCode, inviteCode);
        beginTransaction(t);
        
        return t.getId();
    }
    /**
     * 绑定手机号
     * @param mobile 手机号
     * @param verifyCode 验证码
     */
    public int doBindYixin(String mobile, String verifyCode){
        Transaction t = new YxBindTransaction(mobile, verifyCode);
        beginTransaction(t);
        
        return t.getId();
    }
    
    /**
     * 登录—上传地理位置并获取用户数据。
     * 如果没有数据，那就不要上传，用空字符串或者null表示。
     * @param latitude 纬度
     * @param longtitude 经度
     * @param province 省代码
     * @param city 市代码
     * @param district 区县代码
     */
    public int doLoginGetUserInfo(String userName, String password, String token, int accountType, 
            String latitude, String longtitude, String provinceCode, String cityCode, String districtCode){
        
        LoginGetUserInfoTransaction t = new LoginGetUserInfoTransaction(userName, password, token, accountType, 
                latitude, longtitude, provinceCode, cityCode, districtCode);
        beginTransaction(t);
        
        return t.getId();
    }
    
    /**
     * 上传地理位置.
     * 如果没有数据，那就不要上传，用空字符串或者null表示。
     * @param latitude 纬度
     * @param longtitude 经度
     * @param province 省代码
     * @param city 市代码
     * @param district 区县代码
     */
    public int doUploadLocation(String latitude, String longtitude, String provinceCode, String cityCode, String districtCode){
        UploadLoctaionTransaction t = new UploadLoctaionTransaction(latitude, longtitude, 
                provinceCode, cityCode, districtCode);
        beginTransaction(t);
        
        return t.getId();
    }
    
    /** 退出登录 */
    public int doLogout(Context context){
        LogoutTransaction t = new LogoutTransaction(context);
        beginTransaction(t);
        
        return t.getId();
    }
    
    /** 获取push所需的参数 */
    public int doGetPushParams(){
        GetPushParamsTransaction t = new GetPushParamsTransaction();
        beginTransaction(t);
        
        return t.getId();
    }
    
    /** 获取推荐列表 */
    public int doGetRecommend(boolean isLogin){
        GetRecommendTransaction t = new GetRecommendTransaction(isLogin);
        beginTransaction(t);
        
        return t.getId();
    }
    
    /** 获取推荐活动列表 */
    public int doGetCompetitionList(){
    	GetCompetitionListTransaction t = new GetCompetitionListTransaction();
        beginTransaction(t);
        
        return t.getId();
    }
    /**
     * 获取排行榜
     * @param rankId
     * @param page
     * @return
     */
    public int doGetRank(int rankId, int rankType, int page){
        GetRankTransaction t = new GetRankTransaction(rankId, rankType, page);
        beginTransaction(t);
        
        return t.getId();
    }
    
    /**
     * 获取用户资料
     * @param uid
     * @return
     */
    public int doGetUserInfo(long uid){
        EgmBaseTransaction t = new GetUserInfoTransaction(uid);
        beginTransaction(t);
        
        return t.getId();
    }
    
    /**
     * 获取碰缘分状态数据
     * @return
     */
    public int doGetYuanfenInfo(){
        GetYuanfenInfoTransaction t = new GetYuanfenInfoTransaction();
        beginTransaction(t);
        
        return t.getId();
    }
    
    /**
     * 通知服务器用户正在使用客户端，可以进行碰缘分
     * @return
     */
    public int doInformServer(){
        GetYuanfenInfoTransaction t = new GetYuanfenInfoTransaction();
        beginTransaction(t);
        
        return t.getId();
    }
    
    /**
     * 通知服务器碰缘分开关状态
     * @return
     */
    public int doInformYuanfenSwitcher(boolean isOpen){
        InfoYuanfenSwitcherTransaction t = new InfoYuanfenSwitcherTransaction(isOpen);
        beginTransaction(t);
        
        return t.getId();
    }
    
    /**
     * 通知服务器碰缘分类型
     * @return
     */
    public int doInformYuanfenType(int type){
        InfoYuanfenTypeTransaction t = new InfoYuanfenTypeTransaction(type);
        beginTransaction(t);
        
        return t.getId();
    }
    
    
    /**
     * 上传碰缘分的群发语音或者帮我写文字
     * @return
     */
    public int doSendYuanfen(int type, String voicePath, String text, int duration){
        SendYuanfenTransaction t = new SendYuanfenTransaction(type, voicePath, text, duration);
        beginTransaction(t);
        
        return t.getId();
    }
    
    /**
     * 搜索
     * @return
     */
    public int doSearch(int ageBegin, int ageEnd, int constellation, int provinceCode, int hasPrivatePic, int income, int page){
        SearchTransaction t = new SearchTransaction(ageBegin, ageEnd, constellation, provinceCode, hasPrivatePic, income, page);
        beginTransaction(t);
        
        return t.getId();
    }
    
    /**
     * 获取排行榜背景图
     * @return
     */
    public int doGetRankPicture(String version){
        GetRankPictureTransaction t = new GetRankPictureTransaction(version);
        beginTransaction(t);
        
        return t.getId();
    }
    
    /**
     * 获取排行相关信息
     * @return
     */
    public int doGetRankListInfoInHome(String userId){
        GetRankInfoInHomeTransaction t = new GetRankInfoInHomeTransaction(userId);
        beginTransaction(t);

        return t.getId();
    }
    /** 获取个人中心女性兑现账户 */
    public int doGetMoneyAccount(){
        GetMoneyAccountTransaction t = new GetMoneyAccountTransaction();
        beginTransaction(t);
        
        return t.getId();
    }
    
    /** 获取个人中心女性申请兑现 */
    public int doApplyWithdraw(){
        ApplyWithdrawTransaction t = new ApplyWithdrawTransaction();
        beginTransaction(t);
        
        return t.getId();
    }
    
    /** 验证身份证 */
    public int doAuthIdentity(String name, String idCardNo, 
    		String idCardPic1, String idCardPic2, String video){
        AuthIdentityTransaction t = new AuthIdentityTransaction(name, idCardNo, 
        		idCardPic1, idCardPic2, video);
        beginTransaction(t);
        
        return t.getId();
    }
    
    /** 使用Token置换登录ticket */
    public int doExchangeTicketFromUrs(String productId, String key, String token){
        ExchangeTicketFromUrsTransaction t = new ExchangeTicketFromUrsTransaction(productId, key, token);
        beginTransaction(t);
        
        return t.getId();
    }
    
    /** 女性现金收支记录 */
    public int doGetMoneyHistory(int type, int page){
        GetMoneyHistoryTransaction t = new GetMoneyHistoryTransaction(type, page);
        beginTransaction(t);
        
        return t.getId();
    }
    
    /** 添加易信好友 */
    public int doYixinAddFriend(long uid){
        YixinAddFriendTransaction t = new YixinAddFriendTransaction(uid);
        beginTransaction(t);
        
        return t.getId();
    }
    
    /** 易信注册 */
    public int doYixinRegister(String mobile, String password, String key){
        YixinRegisterTransaction t = new YixinRegisterTransaction(mobile, password, key);
        beginTransaction(t);
        
        return t.getId();
    }
    
    /** 查看小爱助手的易信去看看消息 */
    public int doYixinCheck(){
        YixinCheckTransaction t = new YixinCheckTransaction();
        beginTransaction(t);
        
        return t.getId();
    }
    
    
    //-----------------------------------------------------------------------------------------
    /**
     * 获取用户的详细资料，包含私照，公开照，礼物等
     */
    public int doGetUserInfoDetail(long uid){
    	EgmBaseTransaction t = new GetUserInfoDetailTransaction(uid);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 获取个人资料配置项数据
     */
    public int doGetUserInfoConfig(String version){
    	EgmBaseTransaction t = new GetUserInfoConfigTransaction(version);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 获取系统头像列表
     */
    public int doGetSysPortraitList(){
        EgmBaseTransaction t = new GetSysPortraitListTransaction();
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 设置系统头像
     */
    public int doUpdateSysPortrait(int pid){
        EgmBaseTransaction t = new UpdateSysPortraitTransaction(pid);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 获取礼物列表
     */
    public int doGetGiftList(int page){
    	EgmBaseTransaction t = new GetGiftListTransaction(page);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 获取照片列表
     */
    public int doGetPictureList(String userId ,int type,int page){
    	EgmBaseTransaction t = new GetPictureListTransaction(userId ,type ,page);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 修改会员详细资料
     */
    public int doModifyDetailInfo(HashMap<String,String> map){
    	EgmBaseTransaction t = new ModifyDetailInfoTransaction(map);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 修改头像
     */
    public int doModifyPortrait(String filePath,String x ,String y,String w ,String h){
    	EgmBaseTransaction t = new ModifyProfileTransaction(filePath,x,y,w,h);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 上传语音介绍
     */
    public int doUpdateAudioIntroduce(String audioPath, int duration){
    	EgmBaseTransaction t = new UpdateAudioIntrTransaction(audioPath,duration);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 上传视频介绍
     * @param videoPath
     * @param coverPath
     * @param duration 毫秒
     * @return
     */
    public int doUpdateVideoIntroduce(String video, String cover, 
    		long duration, int isCamera, Parcelable params) {
    	EgmBaseTransaction t = new UpdateVideoIntrTransaction(video, cover, 
    			duration, isCamera, params);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 删除语音介绍
     */
    public int doDelAudioIntroduce(){
    	EgmBaseTransaction t = new DelAudioIntrTransaction();
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 切换音视频状态
     */
    public int doSwitchAudioVideo(int mode){
        EgmBaseTransaction t = new AudioVedioSwitchTransaction(mode);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 切换音视频状态
     */
    public int doGetAudioVideoMode(){
        EgmBaseTransaction t = new GetAudioVedioModeTransaction();
        beginTransaction(t);
        return t.getId();
    }
    /**
     * 获取礼物配置数据
     */
    public int doGetGiftConfig(String version){
    	EgmBaseTransaction t = new GetGiftConfigTransaction(version);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 获取表情配置数据
     */
    public int doGetEmotConfig(String version, String faceVersion){
    	EgmBaseTransaction t = new GetEmotConfigTransaction(version, faceVersion);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 获取个人中心数据
     */
    public int doGetPrivateData(){
    	EgmBaseTransaction t = new GetPrivateDataTransaction();
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 送礼物
     */
    public int doSendGift(String toUserId ,String giftId ,String picId ,int type){
    	EgmBaseTransaction t = new SendGiftTransaction(toUserId ,giftId ,picId ,type);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 修改自我介绍
     */
    public int doModifyIntroduce(String content){
    	EgmBaseTransaction t = new ModifyIntrTransaction(content);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 上传照片
     */
    public int upLoadPicture(String filePath ,int picType, int isCamera){
    	EgmBaseTransaction t = new UploadPicTransaction(filePath, picType, isCamera);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 删除照片
     */
    public int deletePicture(int type,long[] pictureIds){
    	EgmBaseTransaction t = new DeletePicTransaction(type,pictureIds);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 赞私照
     */
    public int doPraisePriPic(String userId ,String picId){
    	EgmBaseTransaction t = new PraisePicTransaction(userId,picId);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 踩私照
     */
    public int doUnlikePriPic(String userId, String picId) {
    	EgmBaseTransaction t = new UnlikePicTransaction(userId, picId);
    	beginTransaction(t);
    	return t.getId();
    }
    
    /**
     * 获取聊天列表
     */
    public int doGetChatList(){
    	EgmBaseTransaction t = new GetChatListTransaction();
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 删除聊天项
     */
    public int doDelChatListItem(String userId){
    	EgmBaseTransaction t = new DelChatItemTransaction(userId);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 聊天记录列表排序
     */
    public int doGetSortList(int sortType ,long[] uids){
    	EgmBaseTransaction t = new GetChatSortListTransaction(sortType,uids);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 删除某条消息
     */
    public int doDelMsg(MessageInfo info,ChatItemUserInfo anotherUserInfo){
        return doDelMsg(info, anotherUserInfo, true);
    }
    
    /**
     * 删除某条消息
     */
    public int doDelMsg(MessageInfo info,ChatItemUserInfo anotherUserInfo, 
    		boolean notifyRemote){
    	EgmBaseTransaction t = new DelMsgTransaction(info, anotherUserInfo, notifyRemote);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 阅后即焚消息销毁
     */
    public int doDelFireMsg(MessageInfo msg){
    	EgmBaseTransaction t = new DelFireMsgTransaction(msg, true);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 发送消息
     */
    public int doSendMsg(
            MessageInfo info,String filePath){
        EgmBaseTransaction t = new SendMsgTransaction2(
                info, filePath);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 获取阅后即焚消息多媒体链接接口
     */
    public int doGetFireMessageMediaUrl(MessageInfo msg) {
    	EgmBaseTransaction t = new GetFireMessageMediaUrl(msg);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 获取单张私照
     * @param version
     * @return
     */
    public int doGetPrivateImage(long userId ,long picId){
    	EgmBaseTransaction t = new GetSinglePriImageTransaction(userId,picId);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 获取已解锁私照列表
     * @param userId
     * @param picId
     * @return
     */
    public int doGetUnLockPicList(String userId, String picId) {
    	EgmBaseTransaction t = new GetUnLockPicListTransaction(userId, picId);
    	beginTransaction(t);
    	return t.getId();
    }
    
    /**
     * 获取聊天和碰缘分话题数据
     */
    public int doGetTopicData(String version){
    	EgmBaseTransaction t = new GetTopicDataTransaction(version);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 获取与某人的消息列表
     */
    public int doGetMsgList(long uid){
    	EgmBaseTransaction t = new GetMsgListTransaction(uid);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 举报
     */
    public int doComplain(int type ,String content ,long postId){
    	EgmBaseTransaction t = new ComplainTransaction(type,content,postId);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 加黑
     */
    public int doAddBlack(int type ,long bid){
    	EgmBaseTransaction t = new BlockTransaction(type ,bid);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 将消息设置为已读
     * @param uid
     * @param messageId
     * @return
     */
    public int doSetMsgRead(long uid ,String messageId){
    	EgmBaseTransaction t = new SetMsgReadTransaction(uid ,messageId);
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 读取用户聊天技的列表
     */
    public int doGetChatSkills() {
    	EgmBaseTransaction t = new GetTalkSkillsTransaction();
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 更新用户聊天技的列表
     */
    public int doUpdateChatSkills(int skills[]) {
    	EgmBaseTransaction t = new UpdateTalkSkillsTransaction(skills);
        beginTransaction(t);
        return t.getId();
    }
    
  //-----------------------------------------------------------------------------------------
    /**
     * 处理push通道发来的消息
     * jsonMsg，消息体
     */
    public int doHandlePushMsg(String jsonMsg){
        Transaction t = new PushMsgTransaction(jsonMsg);
        beginTransaction(t);
        return t.getId();
    }
    /**
     * 环回接口，用于ui不同界面间的通知
     * @param obj
     * @return
     */
    public int doLoopBack(LoopBack lp) {
        Transaction t = new LoopBackTransaction(lp);
        beginTransaction(t);
        return t.getId();
    }
    /**
     * 用户反馈
     * @param content
     * @return
     */
    public int doFeedBack(String content) {
        Transaction t = new FeedBackTransaction(content);
        beginTransaction(t);
        return t.getId();
    }
    /**
     * 用户反馈
     * @return
     */
    public int doCheckVersion() {
        Transaction t = new CheckVersionTransaction();
        beginTransaction(t);
        return t.getId();
    }
    
    /**
     * 根据邀请码uid查询用户接口
     * @param uid
     * @return
     */
    public int doQueryInvitor(long inviteCode) {
    	Transaction t = new QueryInvitorTransaction(inviteCode);
    	beginTransaction(t);
    	return t.getId();
    }
    
    /**
     * 获取手机绑定的通行证帐号接口
     * @param mobile
     * @return
     */
    public int doQueryAccount(String mobile) {
    	Transaction t = new QueryAccountTransaction(mobile);
    	beginTransaction(t);
    	return t.getId();
    }
    
    /**
     * 下载消息的音频文件等资源
     * 
     * @param info
     * @return
     */
    public int doDowloadMsgRes(MessageInfo info) {
    	return beginTransaction(new DownloadMsgResTransaction(info));
    }
    
    /**
     * 下载资源文件
     * 
     * @param url
     * @return
     */
    public int doDownloadRes(String url) {
    	return beginTransaction(new DownloadResTransaction(url));
    }
    /**
     * 获取第三方登录用户信息
     * 
     * @return
     */
    public int doGetOauthUserInfo() {
    	 	Transaction t = new GetOauthUserInfoTransaction();
        beginTransaction(t);
        return t.getId();
    }
    /**
     * 读取用户防骚扰配置
     * @return
     */
    public int doGetAntiHarassment() {
    	 	Transaction t = new GetAntiHarassmentTransaction();
        beginTransaction(t);
        return t.getId();
    }
    /**
     *更新用户防骚扰配置
     * @return
     */
    public int doUpdateAntiHarassment(AntiHarassmentInfo info) {
    	 	Transaction t = new UpdateAntiHarassmentTransaction(info);
        beginTransaction(t);
        return t.getId();
    }
}