
package com.netease.service.protocol;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;

import com.netease.service.protocol.meta.AntiHarassmentInfo;
import com.netease.service.protocol.meta.ApplyWithdrawError;
import com.netease.service.protocol.meta.AudioIntroduce;
import com.netease.service.protocol.meta.AudioVideoSelfMode;
import com.netease.service.protocol.meta.ChatItemInfo;
import com.netease.service.protocol.meta.ChatSkillInfo;
import com.netease.service.protocol.meta.EmotConfigResult;
import com.netease.service.protocol.meta.GetSigPriResult;
import com.netease.service.protocol.meta.GetUnLockPicListResult;
import com.netease.service.protocol.meta.GiftConfigResult;
import com.netease.service.protocol.meta.GiftRecords;
import com.netease.service.protocol.meta.LoginUserInfo;
import com.netease.service.protocol.meta.LoopBack;
import com.netease.service.protocol.meta.MessageList;
import com.netease.service.protocol.meta.MoneyAccountInfo;
import com.netease.service.protocol.meta.MoneyRecordListInfo;
import com.netease.service.protocol.meta.OauthUserInfo;
import com.netease.service.protocol.meta.PictureInfo;
import com.netease.service.protocol.meta.PictureInfos;
import com.netease.service.protocol.meta.PortraitInfo;
import com.netease.service.protocol.meta.PushParamsInfo;
import com.netease.service.protocol.meta.RankListInfo;
import com.netease.service.protocol.meta.RankListInfoInHome;
import com.netease.service.protocol.meta.RankPictureInfo;
import com.netease.service.protocol.meta.RecommendActivityListInfo;
import com.netease.service.protocol.meta.RecommendListInfo;
import com.netease.service.protocol.meta.SearchListInfo;
import com.netease.service.protocol.meta.SendGiftResult;
import com.netease.service.protocol.meta.SendMsgResult;
import com.netease.service.protocol.meta.SortChatListResult;
import com.netease.service.protocol.meta.SysPortraitListResult;
import com.netease.service.protocol.meta.TopicConfigResult;
import com.netease.service.protocol.meta.UserInfo;
import com.netease.service.protocol.meta.UserInfoConfig;
import com.netease.service.protocol.meta.UserInfoDetail;
import com.netease.service.protocol.meta.UserPrivateData;
import com.netease.service.protocol.meta.VersionInfo;
import com.netease.service.protocol.meta.VideoIntroduce;
import com.netease.service.protocol.meta.YixinAddFriendInfo;
import com.netease.service.protocol.meta.YuanfenInfo;
import com.netease.service.transactions.EgmBaseTransaction;

public class EgmCallBack {

    /*************************************************************
     * success
     *************************************************************/
    
    /**
     * 初始化URS
     * @param transactionId
     * @param data [0]:urs_id；[1]:urs_key
     */
    public void onInitURS(int transactionId, String[] data){}
    
    /** 
     * 登录URS成功 
     * @param token 每次访问服务器所需的token
     */
    public void onLoginURS(int transactionId, String token){}
    
    /**
     * 获取手机号验证码
     * @param key 验证码类型为0：注册验证码时，这个key是AES key，String类型
     */
    public void onGetMobileVerify(int transactionId, Object key){}
    
    /** 手机号注册 */
    public void onRegisterMobile(int transactionId){}
    
    /** 判断是否是同城约会1.0版本帐号，以及是否绑定（验证）过手机号 */
    public void onIsYuehuiAccount(int transactionId,boolean isNewReg){}
    
    /**
     * 注册-补全用户资料
     * @param transactionId
     * @param obj
     */
    public void onFillUserInfo(int transactionId, UserInfo userInfo){}
    
    /** 绑定手机号 */
    public void onBindMobile(int transactionId){}
    
    /**
     * 登录后上传地理位置并获取用户数据
     * @param transactionId
     * @param obj
     */
    public void onLoginGetUserInfo(int transactionId, LoginUserInfo loginUserInfo){}
    
    /** 上传地理位置 */
    public void onUploadLocation(int transactionId){}
    
    /** 退出登录 */
    public void onLogout(int transactionId){}
    
    /** 获取Push参数 */
    public void onGetPushParams(int transactionId, PushParamsInfo pushInfo){}
    
    /** 获取推荐列表 */
    public void onGetRecommend(int transactionId,  RecommendListInfo recommendListInfo){}
    
    /** 获取碰缘分数据 */
    public void onGetYuanfenInfo(int transactionId, YuanfenInfo info){}
    
    /** 通知服务器碰缘分开关状态 */
    public void onInformYuanfenSwitcher(int transactionId, Boolean isOpen){}
    
    /** 通知服务器碰缘分类型 */
    public void onInformYuanfenType(int transactionId, Integer type){}
    
    /** 上传碰缘分数据 */
    public void onSendYuanfen(int transactionId){}
    
    /** 获取排行榜 */
    public void onGetRankList(int transactionId, RankListInfo info){}
    
    /** 获取搜索列表 */
    public void onGetSearchList(int transactionId, SearchListInfo info){}
    
    /** 获取排行榜背景图片 */
    public void onGetRankPicture(int transactionId, RankPictureInfo info){}
    
    /** 个人中心女性兑现帐户 */
    public void onGetMoneyAccount(int transactionId, MoneyAccountInfo info){}
    
    /** 个人中心女性申请提现 */
    public void onApplyWithdraw(int transactionId){}
    
    /** 验证身份证 */
    public void onAuthIdentity(int transactionId){}

    /** 置换ticket */
    public void onExchangeTicket(int transactionId, String ticket){}
    
    /** 女性现金收支记录 */
    public void onGetMoneyRecord(int transactionId, MoneyRecordListInfo info){}
    
    /** 易信添加好友 */
    public void onYixinAddFriend(int transactionId, YixinAddFriendInfo info){}
    
    /** 注册易信 */
    public void onYixinRegister(int transactionId){}
    
    /** 查看小爱助手易信添加好友 */
    public void onYixinCheck(int transactionId, YixinAddFriendInfo info){}
   
    /**获取用户详细信息成功，包含公开照，私照等*/
    public void onGetUserInfoDetailSucess(int transactionId, UserInfoDetail obj){}
    /**获取用户信息配置项*/
    public void onGetUserInfoConfigSucess(int transactionId, UserInfoConfig obj){}
    /**获取礼物列表*/
    public void onGetGiftListSucess(int transactionId, GiftRecords obj){}
    /**获取照片*/
    public void onGetPictureListSucess(int transactionId, PictureInfos obj){}
    /**获取已解锁私照*/
    public void onGetUnLockPictureListSucess(int transactionId, GetUnLockPicListResult obj){}
    /**修改会员详细资料*/
    public void onModifyDetailInfoSucess(int transactionId, UserInfo obj){}
    /**修改会员头像*/
    public void onModifyProfileSucess(int transactionId,PortraitInfo portrait){}
    /**上传视频介绍*/
    public void onUpdateVideoSucess(int transactionId,VideoIntroduce obj){}
    /**上传语音介绍*/
    public void onUpdateAudioSucess(int transactionId,AudioIntroduce obj){}
    /**选择介绍方式*/
    public void onSwitchAudioVideo(int transactionId) {}
    /**删除语音介绍*/
    public void onDelAudioSucess(int transactionId,Object obj){}
    /**获取表情配置数据*/
    public void onGetEmotConfigSucess(int transactionId,EmotConfigResult obj){}
    /**获取礼物配置数据*/
    public void onGetGiftConfigSucess(int transactionId,GiftConfigResult obj){}
    /**获取个人中心数据*/
    public void onGetPrivateDataSucess(int transactionId,UserPrivateData obj){}
    /**赠送礼物*/
    public void onSendGiftSucess(int transactionId,SendGiftResult obj){}
    /**修改自我介绍*/
    public void onModifyIntrSucess(int transactionId,int code){}
    /**上传公开照和私照*/
    public void onUploadPicSucess(int transactionId,PictureInfo obj){}
    /**删除公开照和私照*/
    public void onDeletePicSucess(int transactionId,int code){}
    /**赞私照*/
    public void onPraisePicSucess(int transactionId,int code){}
    /**踩私照*/
    public void onUnlikePicSuccess(int transactionId, int code) {}
    /**获取聊天列表*/
    public void onGetChatListSucess(int transactionId,List<ChatItemInfo> obj){}
    /**删除聊天列表*/
    public void onDelChatListSucess(int transactionId,int code){}
    /**删除消息*/
    public void onDelMsgSucess(int transactionId,int code){}
    /**阅后即焚消息销毁接口*/
    public void onDelFireMsgSucess(int transactionId,int code){}
    /**聊天列表排序*/
    public void onSortChatListSucess(int transactionId,SortChatListResult obj){}
    /**发送消息*/
    public void onSendMsgSucess(int transactionId,SendMsgResult obj){}
    /**获取阅后即焚消息多媒体链接接口*/
    public void onGetFireMessageMediaUrlSucess(int transactionId, String mediaUrl){}
    /**获取聊天和碰缘分文字模板*/
    public void onGetTopicSucess(int transactionId,TopicConfigResult obj){}
    /**获取消息列表*/
    public void onGetMsgListSucess(int transactionId,MessageList obj){}
    /**举报*/
    public void onComplainSucess(int transactionId,int code){}
    /**加黑*/
    public void onBlockSucess(int transactionId,int code,long uid){}
    /**将消息设置为已读*/
    public void onSetMsgReadSucess(int transactionId,int code){}
    /**读取用户聊天技的列表*/
    public void onGetTalkSkillsSuccess(int transactionId, ArrayList<ChatSkillInfo> obj){}
    /**更新用户聊天技的列表*/
    public void onUpdateTalkSkillsSuccess(int transactionId,int code){}
    /**获取单张私照成功*/
    public void onGetSinPriImageSucess(int transactionId,GetSigPriResult obj){}
    /**收到push消息*/
    public void onPushMsgArrived(int transactionId,List<ChatItemInfo> obj){}
    /**环回成功*/
    public void onLoopBack(int transactionId, LoopBack obj) {}
    /**反馈成功*/
    public void onFeedBack(int transactionId) {}
    /**检查更新成功*/
    public void onCheckVersion(int transactionId,VersionInfo version) {}
    /**查询邀请者成功*/
    public void onQueryInvitor(int transactionId, Boolean exist) {}
    /**查询通行证帐号成功*/
    public void onQueryAccount(int transactionId, String account) {}
    /** 绑定易信 */
    public void onBindYixin(int transactionId, YixinAddFriendInfo info){}
    /** 读取第三方登录用户资料 */
    public void onGetOauthUserInfo(int transactionId, OauthUserInfo info){}
    /**获取系统头像列表*/
    public void onGetSysPortraitListSucess(int transactionId, SysPortraitListResult obj){}
    /**设置系统头像*/
    public void onUpdateSysPortraitSucess(int transactionId, int code){}
    /**获取下载资源*/
    public void onDownloadResSucess(int transactionId,int code){}
    /**获取推荐页活动*/
    public void onGetCompetitionListSucess(int transactionId, RecommendActivityListInfo info ){}
    public void onGetCompetitionListError(int transactionId, int errCode, String err){}
    /**读取用户防骚扰配置*/
    public void onGetAntiHarassment(int transactionId, AntiHarassmentInfo info){}
    /**更新用户防骚扰配置*/
    public void onUpdateAntiHarassment(int transactionId,int code){}
    
    /**获取主页面排行榜信息*/
    public void onGetRankListInHomeSucess(int transactionId,RankListInfoInHome info){};
    public void onGetRankListInhomeError(int transactionId, int errCode, String err){}
    
    /**获取音频/视频模式*/
    public void onGetAudioVideoSucess(int transactionId,AudioVideoSelfMode info){};
    public void onGetAudioVideoError(int transactionId, int errCode, String err){}
    /*************************************************************
     * error
     *************************************************************/
    
    public void onInitURSError(int transactionId, int errCode, String err){}
    
    public void onLoginURSError(int transactionId, int errCode, String err){}
    
    /**
     * 获取手机号验证码
     * @param transactionId
     * @param errCode： 
     * <br>EgmServiceCode.TRANSACTION_COMMON_REGISTER_ALREADY
     * <br>EgmServiceCode.TRANSACTION_SMS_REQUIRE_TOO_MANY
     * <br>EgmServiceCode.TRANSACTION_SMS_REQUIRE_INTERVAL_TOO_SHORT
     * <br>EgmServiceCode.TRANSACTION_MOBILE_FORMAT_ERROR
     * <br>EgmServiceCode.TRANSACTION_MOBILE_REGISTER_NETEASE_ALREADY
     * @param err
     */
    public void onGetMobileVerifyError(int transactionId, int errCode, String err){}
    
    /**
     * 手机号注册
     * @param transactionId
     * @param errCode：
     * <br>EgmServiceCode.TRANSACTION_COMMON_PASSWORD_ILLEGAL
     * <br>EgmServiceCode.TRANSACTION_VERIFY_CODE_ERROR
     * <br>EgmServiceCode.TRANSACTION_MOBILE_FORMAT_ERROR
     * <br>EgmServiceCode.TRANSACTION_MOBILE_REGISTER_NETEASE_ALREADY
     * <br>EgmServiceCode.TRANSACTION_AES_KEY_ERROR
     * @param err
     */
    public void onRegisterMobileError(int transactionId, int errCode, String err){}
    
    /**
     * 判断是否是同城约会1.0版本帐号，以及是否绑定（验证）过手机号
     * @param transactionId
     * @param errCode: 
     *  <br>EgmServiceCode.TRANSACTION_ACCOUNT_NOT_MENBER
     *  <br>EgmServiceCode.TRANSACTION_ACCOUNT_NOT_BIND_MOBILE 
     *  <br>EgmServiceCode.TRANSACTION_ACCOUNT_NOT_YUEHUI_MENBER
     * @param err
     */
    public void onIsYuehuiAccountError(int transactionId, int errCode, String err){}
    
    /**
     * 注册-补全用户资料
     * @param transactionId
     * @param errCode:
     *  <br>EgmServiceCode.TRANSACTION_ACCOUNT_PICTURE_FILE_TOO_BIG
     *  <br>EgmServiceCode.TRANSACTION_ACCOUNT_PICTURE_SIZE_ILLEGAL
     *  <br>EgmServiceCode.TRANSACTION_ACCOUNT_PICTURE_CROP_COORDINATE_ERROR
     *  <br>EgmServiceCode.TRANSACTION_ACCOUNT_NICKNAME_ILLEGAL
     *  <br>EgmServiceCode.TRANSACTION_INVITE_CODE_ILLEGAL
     *  <br>EgmServiceCode.TRANSACTION_ACCOUNT_BIRTHDAY_FORMAT_ERROR
     * @param err
     */
    public void onFillUserInfoError(int transactionId, int errCode, String err){}
    
    /**
     * 绑定手机号
     * @param transactionId
     * @param errCode:
     *  <br>EgmServiceCode.TRANSACTION_VERIFY_CODE_ERROR
     *  <br>EgmServiceCode.TRANSACTION_MOBILE_BIND_ALREADY
     * @param err
     */
    public void onBindMobileError(int transactionId, int errCode, String err){}
    
    /**
     * 登录后上传地理位置并获取用户数据
     * @param transactionId
     * @param errCode:
     * <br>EgmServiceCode.TRANSACTION_COMMON_REGISTER_NOT_FINISH
     * @param err
     */
    public void onLoginGetUsrInfoError(int transactionId, int errCode, String err){}
    
    /** 上传地理位置出错 */
    public void onUploadLocationError(int transactionId, int errCode, String err){}
    
    /** 退出登录 */
    public void onLogoutError(int transactionId, int errCode, String err){}
    
    /** 获取Push参数 */
    public void onGetPushParamsError(int transactionId, int errCode, String err){}
    
    /** 获取推荐列表 */
    public void onGetRecommendError(int transactionId, int errCode, String err){}
    
    /** 获取碰缘分数据 */
    public void onGetYuanfenInfoError(int transactionId, int errCode, String err){}
    
    /** 通知服务器碰缘分开关状态 */
    public void onInformYuanfenSwitcherError(int transactionId, int errCode, String err){}
    
    /** 通知服务器碰缘分类型 */
    public void onInformYuanfenTypeError(int transactionId, int errCode, String err){}
    
    /** 上传碰缘分数据 */
    public void onSendYuanfenError(int transactionId, int errCode, String err){}
    
    /** 获取排行榜 */
    public void onGetRankListError(int transactionId, int errCode, String err){}
    
    /** 获取搜索列表 */
    public void onGetSearchListError(int transactionId, int errCode, String err){}
    
    /** 获取排行榜背景图片 */
    public void onGetRankPictureError(int transactionId, int errCode, String err){}
    
    /** 个人中心女性兑现帐户 */
    public void onGetMoneyAccountError(int transactionId, int errCode, String err){}
    
    /** 个人中心女性申请提现 */
    public void onApplyWithdrawError(int transactionId, int errCode, String err, ApplyWithdrawError info){}
    
    /** 验证身份证 */
    public void onAuthIdentityError(int transactionId, int errCode, String err){}

    /** 置换ticket */
    public void onExchangeTicketError(int transactionId, int errCode, String err){}
    
    /** 女性现金收支记录 */
    public void onGetMoneyRecordError(int transactionId, int errCode, String err){}
    
    /** 易信添加好友 */
    public void onYixinAddFriendError(int transactionId, int errCode, String err){}
    
    /** 注册易信 */
    public void onYixinRegisterError(int transactionId, int errCode, String err){}
    
    /** 查看小爱助手易信添加好友 */
    public void onYixinCheckError(int transactionId, int errCode, String err){}
    
    /**获取用户详细信息失败*/
    public void onGetUserInfoDetailError(int transactionId, int errCode, String err){}
    /**获取用户信息配置项失败*/
    public void onGetUserInfoConfigError(int transactionId, int errCode, String err){}
    /**获取礼物列表失败*/
    public void onGetGiftListError(int transactionId, int errCode, String err){}
    /**获取照片*/
    public void onGetPictureListError(int transactionId, int errCode, String err){}
    /**获取已解锁私照*/
    public void onGetUnLockPictureListError(int transactionId, int errCode, String err){}
    /**修改会员详细资料*/
    public void onModifyDetailInfoError(int transactionId, int errCode, String err){}
    /**修改会员头像失败*/
    public void onModifyProfileError(int transactionId,int errCode, String err){}
    /**修改会员头像失败*/
    public void onModifyIntroError(int transactionId,int errCode, String err){}
    /**上传语音介绍失败*/
    public void onUpdateAudioError(int transactionId,int errCode, String err){}
    /**上传视频介绍失败*/
    public void onUpdateVideoError(int transactionId,int errCode, String err){}
    /**删除语音介绍*/
    public void onDelAudioError(int transactionId,int errCode, String err){}
    /**获取表情配置数据*/
    public void onGetEmotConfigError(int transactionId,int errCode, String err){}
    /**获取礼物配置数据*/
    public void onGetGiftConfigError(int transactionId,int errCode, String err){}
    /**获取个人中心数据*/
    public void onGetPrivateDataError(int transactionId,int errCode, String err){}
    /**赠送礼物*/
    public void onSendGiftError(int transactionId,int errCode, String err){}
    /**上传公开照和私照*/
    public void onUploadPicError(int transactionId,int errCode, String err){}
    /**删除公开照和私照*/
    public void onDeletePicError(int transactionId,int errCode, String err){}
    /**赞私照*/
    public void onPraisePicError(int transactionId,int errCode, String err){}
    /**踩私照*/
    public void onUnlikePicError(int transactionId, int errCode, String err) {}
    /**获取聊天列表*/
    public void onGetChatListError(int transactionId,int errCode, String err){}
    /**删除聊天列表*/
    public void onDelChatListError(int transactionId,int errCode, String err){}
    /**删除消息*/
    public void onDelMsgError(int transactionId,int errCode, String err){}
    /**阅后即焚消息销毁接口*/
    public void onDelFireMsgError(int transactionId,int errCode, String err){}
    /**聊天列表排序*/
    public void onSortChatListError(int transactionId,int errCode, String err){}
    /**发送消息*/
    public void onSendMsgError(int transactionId,int errCode, String err){}
    /**获取阅后即焚消息多媒体链接接口*/
    public void onGetFireMessageMediaUrlError(int transactionId, int errCode, String err){}
    /**获取聊天和碰缘分文字模板*/
    public void onGetTopicError(int transactionId,int errCode, String err){}
    /**获取消息列表*/
    public void onGetMsgListError(int transactionId,int errCode, String err){}
    /**举报*/
    public void onComplainError(int transactionId,int errCode, String err){}
    /**加黑*/
    public void onBlockError(int transactionId,int errCode, String err){}
    /**将消息设置为已读*/
    public void onSetMsgReadError(int transactionId,int errCode, String err){}
    /**读取用户聊天技的列表*/
    public void onGetTalkSkillsError(int transactionId,int errCode, String err){}
    /**更新用户聊天技的列表*/
    public void onUpdateTalkSkillsError(int transactionId,int errCode, String err){}
    /**获取单张私照*/
    public void onGetSinPriImageError(int transactionId,int errCode, String err){}
    /**反馈失败*/
    public void onFeedBackError(int transactionId,int errCode, String err) {}
    /**检查更新失败*/
    public void onCheckVersionError(int transactionId,int errCode, String err){}
    /**返回用户未登录，需重新登录*/
    public void onRelogin(int transactionId,int errCode, String err){}
    /**查询邀请者失败*/
    public void onQueryInvitorError(int transactionId,int errCode, String err) {}
    /**查询通行证帐号失败*/
    public void onQueryAccountError(int transactionId,int errCode, String err) {}
    /** 绑定易信失败*/
    public void onBindYixinError(int transactionId, int errCode, String err){}
    /** 读取第三方登录用户资料 */
    public void onGetOauthUserInfoError(int transactionId, int errCode, String err){}
    /** 获取系统头像列表 */
    public void onGetSysPortraitListError(int transactionId, int errCode, String err){}
    /** 设置系统头像 */
    public void onUpdateSysPortraitError(int transactionId, int errCode, String err){}
    /**获取下载资源*/
    public void onDownloadResError(int transactionId,int errCode, String err){}
    /**读取用户防骚扰配置*/
    public void onGetAntiHarassmentError(int transactionId,int errCode, String err){}
    /**更新用户防骚扰配置*/
    public void onUpdateAntiHarassmentError(int transactionId,int errCode, String err){}
    /**强制升级*/
    public void onForceUpdate(int transactionId,int errCode, String err){}
    
    /*************************************************************
     *
     *
     *************************************************************/
    Handler mHandler;

    public EgmCallBack() {
        // mHandler = new InternalHandler();
    }

    public void onSuccess(int type, int transactionId, int code, Object obj) {
        switch (type) {
            case EgmBaseTransaction.TRANSACTION_TYPE_INIT_URS:
                onInitURS(transactionId, (String[])obj);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_LOGIN_URS:
                onLoginURS(transactionId, (String)obj);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_MOBILE_VERIFY_CODE:
                onGetMobileVerify(transactionId, obj);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_MOBILE_REGISTER:
                onRegisterMobile(transactionId);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_IS_YUEHUI_ACCOUNT:
                onIsYuehuiAccount(transactionId,(Boolean)obj);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_FILL_USER_INFO:
                onFillUserInfo(transactionId, (UserInfo)obj);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_BIND_MOBILE:
                onBindMobile(transactionId);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_LOGIN_GET_USER_INFO:
                onLoginGetUserInfo(transactionId, (LoginUserInfo)obj);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_UPLOAD_LOCATION:
                onUploadLocation(transactionId);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_LOGOUT:
                onLogout(transactionId);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_GET_PUSH_PARAMS:
                onGetPushParams(transactionId, (PushParamsInfo)obj);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_GET_RECOMMEND:
                onGetRecommend(transactionId, (RecommendListInfo) obj);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_GET_YUANFEN_INFO:
                onGetYuanfenInfo(transactionId, (YuanfenInfo)obj);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_INFORM_YUANFEN_SWITCHER:
                onInformYuanfenSwitcher(transactionId, (Boolean)obj);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_INFORM_YUANFEN_TYPE:
                onInformYuanfenType(transactionId, (Integer)obj);
                break;  
            case EgmBaseTransaction.TRANSACTION_TYPE_SEND_YUANFEN:
                onSendYuanfen(transactionId);
                break;  
            case EgmBaseTransaction.TRANSACTION_TYPE_GET_RANK_LIST:
                onGetRankList(transactionId, (RankListInfo)obj);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_SEARCH:
                onGetSearchList(transactionId, (SearchListInfo)obj);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_RANK_PICTURE:
                onGetRankPicture(transactionId, (RankPictureInfo)obj);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_GET_MONEY_ACCOUNT_INFO:
                onGetMoneyAccount(transactionId, (MoneyAccountInfo)obj);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_AUTH_IDENTITY:
                onAuthIdentity(transactionId);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_EXCHANGE_TICKET:
                onExchangeTicket(transactionId, (String)obj);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_GET_MONEY_HISTORY:
                onGetMoneyRecord(transactionId, (MoneyRecordListInfo)obj);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_APPLY_WITHDRAW:
                onApplyWithdraw(transactionId);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_YIXIN_ADD_FRIEND:
                onYixinAddFriend(transactionId, (YixinAddFriendInfo)obj);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_YIXIN_REGISTER:
                onYixinRegister(transactionId);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_YIXIN_CHECK:
                onYixinCheck(transactionId, (YixinAddFriendInfo)obj);
                break;
            case EgmBaseTransaction.TARNSACTION_TYPE_GET_USERINFO_DETAIL://获取用户详细信息
            	onGetUserInfoDetailSucess(transactionId,(UserInfoDetail)obj);
            	break;
            case EgmBaseTransaction.TARNSACTION_TYPE_GET_USERINFO_CONGFIG:
            	onGetUserInfoConfigSucess(transactionId,(UserInfoConfig)obj);
            	break;
            case EgmBaseTransaction.TARNSACTION_TYPE_GET_GIFT_LIST:
            	onGetGiftListSucess(transactionId,(GiftRecords)obj);
            	break;
            case EgmBaseTransaction.TARNSACTION_TYPE_MODIFY_DETAIL_INFO:
            	onModifyDetailInfoSucess(transactionId,(UserInfo)obj);
            	break;
            case EgmBaseTransaction.TARNSACTION_TYPE_MODIFY_PROFILE:
            	onModifyProfileSucess(transactionId,(PortraitInfo)obj);
            	break;
            case EgmBaseTransaction.TRANSACTION_DOWNLOAD_RES:
            	onDownloadResSucess(transactionId, code);
            	break;
            case EgmBaseTransaction.TARNSACTION_TYPE_UPDATE_VIDEO:
            	onUpdateVideoSucess(transactionId, (VideoIntroduce) obj);
            	break;
            case EgmBaseTransaction.TARNSACTION_TYPE_UPDATE_AUDIO:
            	onUpdateAudioSucess(transactionId,(AudioIntroduce)obj);
            	break;
            case EgmBaseTransaction.TRANSACTION_SWITCH_AUDIO_VIDEO:
            	onSwitchAudioVideo(transactionId);
            	break;
            case EgmBaseTransaction.TARNSACTION_DEL_AUDIO_INTR:
            	onDelAudioSucess(transactionId,obj);
            	break;
            case EgmBaseTransaction.TARNSACTION_GET_EMOT_CONFIG:
            	onGetEmotConfigSucess(transactionId, (EmotConfigResult)obj);
            	break;
            case EgmBaseTransaction.TARNSACTION_GET_GIFT_CONFIG:
            	onGetGiftConfigSucess(transactionId, (GiftConfigResult)obj);
            	break;
            case EgmBaseTransaction.TARNSACTION_GET_PRIVATE_DATA:
            	onGetPrivateDataSucess(transactionId,(UserPrivateData)obj);
            	break;
            case EgmBaseTransaction.TARNSACTION_GET_PICTURE_LIST:
            	onGetPictureListSucess(transactionId,(PictureInfos)obj);
            	break;
            case EgmBaseTransaction.TARNSACTION_GET_UNLOCK_PICTURE_LIST:
            	onGetUnLockPictureListSucess(transactionId, (GetUnLockPicListResult)obj);
            	break;
            case EgmBaseTransaction.TARNSACTION_SEND_GIFT:
            	onSendGiftSucess(transactionId,(SendGiftResult)obj);
            	break;
            case EgmBaseTransaction.TRANSACTION_MODIFY_INTRODUCE:
            	onModifyIntrSucess(transactionId, code);
            	break;
            case EgmBaseTransaction.TRANSACTION_UPLOAD_PIC:
            	onUploadPicSucess(transactionId, (PictureInfo)obj);
            	break;
            case EgmBaseTransaction.TRANSACTION_DELETE_PIC:
            	onDeletePicSucess(transactionId,code);
            	break;
            case EgmBaseTransaction.TRANSACTION_PRAISE_PIC:
            	onPraisePicSucess(transactionId,code);
            	break;
            case EgmBaseTransaction.TRANSACTION_UNLIKE_PIC:
            	onUnlikePicSuccess(transactionId, code);
            	break;
            case EgmBaseTransaction.TRANSACTION_GET_CHAT_LIST:
            	onGetChatListSucess(transactionId,(List<ChatItemInfo>)obj );
            	break;
            case EgmBaseTransaction.TRANSACTION_DEL_CHAT_LIST:
            	onDelChatListSucess(transactionId,code);
            	break;
            case EgmBaseTransaction.TRANSACTION_SORT_CHAT_LIST:
            	onSortChatListSucess(transactionId,(SortChatListResult)obj);
            	break;
            case EgmBaseTransaction.TRANSACTION_DEL_MSG:
            	onDelMsgSucess(transactionId,code);
            	break;
            case EgmBaseTransaction.TRANSACTION_DELETE_FIRE_MESSAGE:
            	onDelFireMsgSucess(transactionId, code);
            	break;
            case EgmBaseTransaction.TRANSACTION_SEND_MSG:
            	onSendMsgSucess(transactionId,(SendMsgResult)obj);
            	break;
            case EgmBaseTransaction.TRANSACTION_GET_FIRE_MESSAGE_MEDIA_URL:
            	onGetFireMessageMediaUrlSucess(transactionId, (String)obj);
            	break;
            case EgmBaseTransaction.TRANSACTION_GET_TOPIC_DATA:
            	onGetTopicSucess(transactionId,(TopicConfigResult)obj);
            	break;
            case EgmBaseTransaction.TRANSACTION_GET_MSG_LIST:
            	onGetMsgListSucess(transactionId,(MessageList)obj);
            	break;
            case EgmBaseTransaction.TRANSACTION_COMPLAIN:
            	onComplainSucess(transactionId,code);
            	break;
            case EgmBaseTransaction.TRANSACTION_BLOCK:
            	onBlockSucess(transactionId,code,(Long)obj);
            	break;
            case EgmBaseTransaction.TRANSACTION_SET_MSG_READ:
            	onSetMsgReadSucess(transactionId,code);
            	break;
            case EgmBaseTransaction.TRANSACTION_GET_CHAT_SKILLS_LIST:
            	onGetTalkSkillsSuccess(transactionId, (ArrayList<ChatSkillInfo>) obj);
            	break;
            case EgmBaseTransaction.TRANSACTION_UPDATE_CHAT_SKILLS_LIST:
            	onUpdateTalkSkillsSuccess(transactionId, code);
            	break;
            case EgmBaseTransaction.TRANSACTION_TYPE_HANDLE_PUSH_MSG:
                onPushMsgArrived(transactionId,(List<ChatItemInfo>)obj);
                break;
            case EgmBaseTransaction.TRANSACTION_GET_SINGLE_PRI_IMAGE:
                onGetSinPriImageSucess(transactionId,(GetSigPriResult)obj);
                break;
            case EgmBaseTransaction.TRANSACTION_UI_BROADCAST:
                onLoopBack(transactionId, (LoopBack)obj);	
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_FEEDBACK:
                onFeedBack(transactionId); 
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_CHECKVERSION:
                onCheckVersion(transactionId,(VersionInfo)obj); 
                break;
            case EgmBaseTransaction.TRANSACTION_QUERY_INVITOR:
            	onQueryInvitor(transactionId, (Boolean)obj);
            	break;
            case EgmBaseTransaction.TRANSACTION_QUERY_ACCOUNT:
            	onQueryAccount(transactionId, (String)obj);
            	break;
            case EgmBaseTransaction.TRANSACTION_TYPE_YIXIN_BIND:
            	onBindYixin(transactionId, (YixinAddFriendInfo)obj);
             	break;
            case EgmBaseTransaction.TRANSACTION_TYPE_GET_OAUTHUSERINFO:
            	onGetOauthUserInfo(transactionId, (OauthUserInfo)obj);
            	break;
            case EgmBaseTransaction.TARNSACTION_TYPE_GET_SYSPORTRAIT_LIST:
            	onGetSysPortraitListSucess(transactionId, (SysPortraitListResult)obj);
        		break;
            case EgmBaseTransaction.TARNSACTION_TYPE_UPDATE_SYSPORTRAIT:
            	onUpdateSysPortraitSucess(transactionId, code);
        		break;
            case EgmBaseTransaction.TRANSACTION_GET_COMPETITION_LIST:
            	onGetCompetitionListSucess(transactionId, (RecommendActivityListInfo) obj);
            	break;
            case EgmBaseTransaction.TRANSACTION_TYPE_GET_ANTIHARASSMENT:
            	onGetAntiHarassment(transactionId, (AntiHarassmentInfo)obj);
            	break;
            case EgmBaseTransaction.TRANSACTION_TYPE_SET_ANTIHARASSMENT:
                onUpdateAntiHarassment(transactionId, code);
                break;
            case EgmBaseTransaction.TRANSACTION_GET_RANK_LIST_INFO_IN_HOME:
                onGetRankListInHomeSucess(transactionId, (RankListInfoInHome)obj);
                break;
            case EgmBaseTransaction.TRANSACTION_GET_AUDIO_VIDEO:
                onGetAudioVideoSucess(transactionId, (AudioVideoSelfMode)obj);
                break;
            default:
                break;
        }
    }
   
    public void onError(int type, int transactionId, int errCode, 
    		String errStr, Object param) {
        switch (type) {
            case EgmBaseTransaction.TRANSACTION_TYPE_INIT_URS:
                onInitURSError(transactionId, errCode, errStr);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_LOGIN_URS:
                onLoginURSError(transactionId, errCode, errStr);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_MOBILE_VERIFY_CODE:
                onGetMobileVerifyError(transactionId, errCode, errStr);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_MOBILE_REGISTER:
                onRegisterMobileError(transactionId, errCode, errStr);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_IS_YUEHUI_ACCOUNT:
                onIsYuehuiAccountError(transactionId, errCode, errStr);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_FILL_USER_INFO:
                onFillUserInfoError(transactionId, errCode, errStr);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_BIND_MOBILE:
                onBindMobileError(transactionId, errCode, errStr);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_LOGIN_GET_USER_INFO:
                onLoginGetUsrInfoError(transactionId, errCode, errStr);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_UPLOAD_LOCATION:
                onUploadLocationError(transactionId, errCode, errStr);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_LOGOUT:
                onLogoutError(transactionId, errCode, errStr);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_GET_PUSH_PARAMS:
                onGetPushParamsError(transactionId, errCode, errStr);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_GET_RECOMMEND:
                onGetRecommendError(transactionId, errCode, errStr);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_GET_YUANFEN_INFO:
                onGetYuanfenInfoError(transactionId, errCode, errStr);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_INFORM_YUANFEN_SWITCHER:
                onInformYuanfenSwitcherError(transactionId, errCode, errStr);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_INFORM_YUANFEN_TYPE:
                onInformYuanfenTypeError(transactionId, errCode, errStr);
                break;  
            case EgmBaseTransaction.TRANSACTION_TYPE_SEND_YUANFEN:
                onSendYuanfenError(transactionId, errCode, errStr);
                break; 
            case EgmBaseTransaction.TRANSACTION_TYPE_GET_RANK_LIST:
                onGetRankListError(transactionId, errCode, errStr);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_SEARCH:
                onGetSearchListError(transactionId, errCode, errStr);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_RANK_PICTURE:
                onGetRankPictureError(transactionId, errCode, errStr);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_GET_MONEY_ACCOUNT_INFO:
                onGetMoneyAccountError(transactionId, errCode, errStr);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_AUTH_IDENTITY:
                onAuthIdentityError(transactionId, errCode, errStr);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_EXCHANGE_TICKET:
                onExchangeTicketError(transactionId, errCode, errStr);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_GET_MONEY_HISTORY:
                onGetMoneyRecordError(transactionId, errCode, errStr);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_YIXIN_ADD_FRIEND:
                onYixinAddFriendError(transactionId, errCode, errStr);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_APPLY_WITHDRAW:
                onApplyWithdrawError(transactionId, errCode, errStr, 
                		(ApplyWithdrawError) param);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_YIXIN_REGISTER:
                onYixinRegisterError(transactionId, errCode, errStr);
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_YIXIN_CHECK:
                onYixinCheckError(transactionId, errCode, errStr);
                break;
            case EgmBaseTransaction.TARNSACTION_TYPE_GET_USERINFO_DETAIL://获取用户详细信息
            	onGetUserInfoDetailError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TRANSACTION_MODIFY_INTRODUCE:
            	onModifyIntroError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TARNSACTION_TYPE_GET_USERINFO_CONGFIG:
            	onGetUserInfoConfigError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TARNSACTION_TYPE_GET_GIFT_LIST:
            	onGetGiftListError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TARNSACTION_TYPE_MODIFY_DETAIL_INFO:
            	onModifyDetailInfoError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TARNSACTION_TYPE_MODIFY_PROFILE:
            	onModifyProfileError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TRANSACTION_DOWNLOAD_RES:
            	onDownloadResError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TARNSACTION_TYPE_UPDATE_VIDEO:
            	onUpdateVideoError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TARNSACTION_TYPE_UPDATE_AUDIO:
            	onUpdateAudioError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TARNSACTION_DEL_AUDIO_INTR:
            	onDelAudioError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TARNSACTION_GET_EMOT_CONFIG:
            	onGetEmotConfigError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TARNSACTION_GET_GIFT_CONFIG:
            	onGetGiftConfigError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TARNSACTION_GET_PRIVATE_DATA:
            	onGetPrivateDataError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TARNSACTION_GET_PICTURE_LIST:
            	onGetPictureListError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TARNSACTION_GET_UNLOCK_PICTURE_LIST:
            	onGetUnLockPictureListError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TARNSACTION_SEND_GIFT:
            	onSendGiftError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TRANSACTION_UPLOAD_PIC:
            	onUploadPicError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TRANSACTION_DELETE_PIC:
            	onDeletePicError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TRANSACTION_PRAISE_PIC:
            	onPraisePicError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TRANSACTION_UNLIKE_PIC:
            	onUnlikePicError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TRANSACTION_GET_CHAT_LIST:
            	onGetChatListError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TRANSACTION_DEL_CHAT_LIST:
            	onDelChatListError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TRANSACTION_SORT_CHAT_LIST:
            	onSortChatListError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TRANSACTION_DEL_MSG:
            	onDelMsgError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TRANSACTION_DELETE_FIRE_MESSAGE:
            	onDelFireMsgError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TRANSACTION_SEND_MSG:
            	onSendMsgError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TRANSACTION_GET_FIRE_MESSAGE_MEDIA_URL:
            	onGetFireMessageMediaUrlError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TRANSACTION_GET_TOPIC_DATA:
            	onGetTopicError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TRANSACTION_GET_SINGLE_PRI_IMAGE:
                onGetSinPriImageError(transactionId, errCode, errStr);
                break;
            case EgmBaseTransaction.TRANSACTION_GET_MSG_LIST:
            	onGetMsgListError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TRANSACTION_COMPLAIN:
            	onComplainError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TRANSACTION_BLOCK:
            	onBlockError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TRANSACTION_SET_MSG_READ:
            	onSetMsgReadError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TRANSACTION_GET_CHAT_SKILLS_LIST:
            	onGetTalkSkillsError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TRANSACTION_UPDATE_CHAT_SKILLS_LIST:
            	onUpdateTalkSkillsError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TRANSACTION_TYPE_FEEDBACK:
                onFeedBackError(transactionId, errCode, errStr); 
                break;
            case EgmBaseTransaction.TRANSACTION_TYPE_CHECKVERSION:
                onCheckVersionError(transactionId, errCode, errStr);
                break;
            case EgmBaseTransaction.TRANSACTION_QUERY_INVITOR:
            	onQueryInvitorError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TRANSACTION_QUERY_ACCOUNT:
            	onQueryAccountError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TRANSACTION_TYPE_YIXIN_BIND:
            	onBindYixinError(transactionId, errCode, errStr);
             	break;
            case EgmBaseTransaction.TRANSACTION_TYPE_GET_OAUTHUSERINFO:
            	onGetOauthUserInfoError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TARNSACTION_TYPE_GET_SYSPORTRAIT_LIST:
            	onGetSysPortraitListError(transactionId, errCode, errStr);
        		break;
            case EgmBaseTransaction.TARNSACTION_TYPE_UPDATE_SYSPORTRAIT:
            	onUpdateSysPortraitError(transactionId, errCode, errStr);
        		break;
            case EgmBaseTransaction.TRANSACTION_GET_COMPETITION_LIST:
            	onGetCompetitionListError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TRANSACTION_TYPE_GET_ANTIHARASSMENT:
            	onGetAntiHarassmentError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TRANSACTION_TYPE_SET_ANTIHARASSMENT:
            	onUpdateAntiHarassmentError(transactionId, errCode, errStr);
            	break;
            case EgmBaseTransaction.TRANSACTION_GET_RANK_LIST_INFO_IN_HOME:
                onGetRankListInhomeError(transactionId, errCode, errStr);
                break;
            case EgmBaseTransaction.TRANSACTION_GET_AUDIO_VIDEO:
                onGetAudioVideoError(transactionId, errCode, errStr);
                break;
            default:
                break;
        }
        switch(errCode){
            case EgmServiceCode.TRANSACTION_COMMON_NOT_LOGIN://需重新登录
                onRelogin(transactionId, errCode, errStr);
                break;
            case EgmServiceCode.TRANSACTION_COMMON_FORCEUPDADE://强制升级
            		onForceUpdate(transactionId, errCode, errStr);
            	 	break;
        }
    }

}
