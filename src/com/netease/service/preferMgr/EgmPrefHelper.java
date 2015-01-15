package com.netease.service.preferMgr;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.Time;

import com.netease.common.log.NTLog;
import com.netease.util.PDEEngine;

/**
 * 本类定义、设置及读取engagement需要保存在preferences中的配置项
 * @author echo
 * @since  2013-04-03
 */
public class EgmPrefHelper extends PrefeHelper {

    /**
     * 是否首次使用本软件，默认是首次使用
     */
    public static final String FIRST_USE ="first_use";
    /**
     * 设置界面各设置项 start
     */

    //本地部分
    public static final String SettingPicHighWhenWifi = "setting_pichigh_when_wifi";//当wifi时以高清质量上传
    
    public static final String PushOn ="push_on";//开启通知推送
    public static final String ShockOn ="shock_on";//开启震动提醒
    public static final String SoundOn ="sound_on";//开启声音提醒
    public static final String NoDisturbingOn ="no_disturbing_on";//开启免打扰
    public static final String NoDisturbingTimeStart ="no_disturbing_start";//免打扰开始时间
    public static final String NoDisturbingTimeEnd ="no_disturbing_end";//开启免结束时间
    public static final String ReceiverModeOn ="receiver_mode_on";//开启听筒模式
   //本地部分 以上设置跟设备不跟帐号（只有一份设置）
    public static final String GiftPicOn ="gifts_pic";//开启发送礼物 解锁私照

    //微博帐户信息
    public static final String KEY_UID           = "uid";
    public static final String KEY_ACCESS_TOKEN  = "access_token";
    public static final String KEY_EXPIRES_IN    = "expires_in";

    /**
     * 设置界面各设置项 end
     */
    /**
     * 版本更新，最近一次提示的版本号
     */
    public static final String UPDATE_PRE_VERSION  = "version"; //最近一次提示版本号
    public static final String UPDATE_ALERT_TIMES  = "alert_times"; //提醒次数

    public static boolean getFirstUse(Context context) {
        return getBoolean(context, FIRST_USE, true);
    }

    public static void putFirstUse(Context context, boolean value) {
        putBoolean(context, FIRST_USE, value);
    }
    
    public static boolean getPushOn(Context context) {
        return getBoolean(context, PushOn, true);
    }
    public static void putPushOn(Context context, boolean value) {
        putBoolean(context, PushOn, value);
    }
    
    public static boolean getShockOn(Context context) {
        return getBoolean(context, ShockOn, true);
    }
    public static void putShockOn(Context context, boolean value) {
        putBoolean(context, ShockOn, value);
    }
    
    public static boolean getSoundOn(Context context) {
        return getBoolean(context, SoundOn, true);
    }
    public static void putSoundOn(Context context, boolean value) {
        putBoolean(context, SoundOn, value);
    }
    public static boolean getNoDisturbingOn(Context context) {
        return getBoolean(context, NoDisturbingOn, false);
    }
    public static void putNoDisturbingOn(Context context, boolean value) {
        putBoolean(context, NoDisturbingOn, value);
    }
    public static int getNoDisturbingStart(Context context) {
        return getInt(context, NoDisturbingTimeStart, 23);
    }
    public static void putNoDisturbingStart(Context context, int value) {
        putInt(context, NoDisturbingTimeStart, value);
    }
    public static int getNoDisturbingEnd(Context context) {
        return getInt(context, NoDisturbingTimeEnd, 9);
    }
    public static void putNoDisturbingEnd(Context context, int value) {
        putInt(context, NoDisturbingTimeEnd, value);
    }
    public static boolean getReceiverModeOn(Context context) {
        return getBoolean(context, ReceiverModeOn, false);
    }
    public static void putReceiverModeOn(Context context, boolean value) {
        putBoolean(context, ReceiverModeOn, value);
    }
    
    /**
     * 检查是否需要push提醒,消息开关打开且不在免打扰时段
     */
    public static boolean getNeedPush(Context context) {
        boolean pushOpen = true;
        if(getPushOn(context)){
            if(getNoDisturbingOn(context)){
                int start = getNoDisturbingStart(context);
                int end = getNoDisturbingEnd(context);
                Time t=new Time(); //
                t.setToNow(); // 取得系统时间。
                int hour = t.hour; // 0-23
                if(start < end){//不跨天
                    if(hour >= start && hour < end){
                        pushOpen = false;
                    }
                } else{//跨天
                    if((hour >= start && hour <= 23) || (hour >= 0 && hour < end)){
                        pushOpen = false;
                    }
                }
            }  
        } else {
            pushOpen = false;
        }
        return pushOpen;
    }
    
    /**
     * 取得最后一条push消息的id
     * @param context
     * @return
     */
    public static final String getMaxMsgId(Context context,String userId){
        return getString(context, userId, null);
    }
    
    /**
     * 设置最后一条push消息的id,帐号做key
     * @param context
     * @param pushId
     */
    public static final void putMaxMsgId(Context context, String userId,String pushId){
        putString(context, userId, pushId);
    }
    
    public static boolean getPicHighWhenWifi(Context context) {
        return getBoolean(context, SettingPicHighWhenWifi, true);
    }
    public static void putPicHighWhenWifi(Context context, boolean value) {
        putBoolean(context, SettingPicHighWhenWifi, value);
    }
   
    
    /**
     * 设置界面各设置项 end
     */
    
    /**
     * 取得最近一次更新信息的版本号
     * @param context
     * @return
     */
    public static final String getPreUpdateVersion(Context context){
        return getString(context, UPDATE_PRE_VERSION, "0.0.0");
    }
    
    /**
     * 设置最近一次更新信息的版本号
     * @param context
     * @param version
     */
    public static final void putPreUpdateVersion(Context context, String version){
        putString(context, UPDATE_PRE_VERSION, version);
    } 
    /**
     * 取得最近一次更新自动提醒次数
     * @param context
     * @return
     */
    public static final int getUpdateAlertTime(Context context){
        return getInt(context, UPDATE_ALERT_TIMES, 0);
    }
    
    /**
     * 设置最近一次更新自动提醒次数
     * @param context
     * @param version
     */
    public static final void putUpdateAlertTimes(Context context, int times){
        putInt(context, UPDATE_ALERT_TIMES, times);
    } 
    
    /** 存放URS的id */
    public static final void putURSId(Context context, String id){
        putString(context, "urs_id", id);
    }
    /** 取得URS的id，默认是空值"" */
    public static final String getURSId(Context context){
        return getString(context, "urs_id", "");
    }
    
    /** 存放URS的key */
    public static final void putURSKey(Context context, String key){
        putString(context, "urs_key", key);
    }
    /** 取得URS的key，默认是空值"" */
    public static final String getURSKey(Context context){
        return getString(context, "urs_key", "");
    }
    
    // token改为放到数据库里
//    /** 存放当前URS登录用户的token */
//    public static final void putURSToken(Context context, String token){
//        Log.d("Egm", "Prefer putUrsToken=" + token);
//        token = PDEEngine.PEncrypt(context, token);
//        putString(context, "urs_user_token", token);
//    }
//    /** 获取当前URS登录用户的token，默认是空值"" */
//    public static final String getURSToken(Context context){
//        String token = getString(context, "urs_user_token", "");
//        
//        if(!TextUtils.isEmpty(token)){
//            token = PDEEngine.PDecrypt(context, token);
//            Log.d("Egm", "Prefer getUrsToken1=" + token);
//        }
//        else{
//            Log.d("Egm", "Prefer getUrsToken2=" + token);
//        }
//        
//        return token;
//    }
    
    /** 存放随机字符串 */
    public static final void putNonce(Context context, String nonce){
        putString(context, "user_nonce", nonce);
    }
    /** 获取随机字符串，默认是空值"" */
    public static final String getNonce(Context context){
        return getString(context, "user_nonce", "");
    }
    
    /** 存放签名过期时间(时间戳) */
    public static final void putExpire(Context context, long expire){
        putLong(context, "user_signature_expire", expire);
    }
    /** 获取签名过期时间(时间戳)，默认是0 */
    public static final long getExpire(Context context){
        return getLong(context, "user_signature_expire", 0);
    }
    
    /** 存放签名 */
    public static final void putSignature(Context context, String signature){
        signature = PDEEngine.PEncrypt(context, signature);
        putString(context, "user_signature", signature);
        if(TextUtils.isEmpty(signature)){
            NTLog.i("EgmPrefHelper", "putSignature Signature is empty");
        }
    }
    /** 获取签名，默认是空值"" */
    public static final String getSignature(Context context){
        String signature = getString(context, "user_signature", "");
        if(!TextUtils.isEmpty(signature)){
            signature = PDEEngine.PDecrypt(context, signature);
        }
        
        return signature;
    }
    
    /** 存储用户配置数据版本号 */
    public static final void putUserConfigVersion(Context context, String version){
        putString(context, "user_config_version", version);
    }
    /** 获取用户配置数据版本号，默认是空值"" */
    public static final String getUserConfigVersion(Context context){
        return getString(context, "user_config_version", "");
    }
    
    /** 存储礼物数据版本号 */
    public static final void putGiftDataVersion(Context context, String version){
        putString(context, "gift_data_version", version);
    }
    /** 获取礼物数据版本号，默认是空值"" */
    public static final String getGiftDataVersion(Context context){
        return getString(context, "gift_data_version", "");
    }
    
    /** 存储话题数据版本号 */
    public static final void putTopicDataVersion(Context context, String version){
        putString(context, "topic_data_version", version);
    }
    /** 获取话题数据版本号，默认是空值"" */
    public static final String getTopicDataVersion(Context context){
        return getString(context, "topic_data_version", "");
    }
    
    /** 存储Emoji表情数据版本号 */
    public static final void putEmotionDataVersion(Context context, String version){
        putString(context, "emotion_data_version", version);
    }
    /** 获取Emoji表情数据版本号，默认是空值"" */
    public static final String getEmotionDataVersion(Context context){
        return getString(context, "emotion_data_version", "");
    }
    
    /** 存储表情数据版本号 */
    public static final void putFaceDataVersion(Context context, String version){
        putString(context, "face_data_version", version);
    }
    /** 获取表情数据版本号，默认是空值"" */
    public static final String getFaceDataVersion(Context context){
        return getString(context, "face_data_version", "1.0.0");
    }
    
    /** 存储文字模板数据版本号 */
    public static final void putTextTemplateDataVersion(Context context, String version){
        putString(context, "text_template_version", version);
    }
    /** 获取文字模板数据版本号，默认是空值"" */
    public static final String getTextTemplateDataVersion(Context context){
        return getString(context, "text_template_version", "");
    }
    
    /** 存储打开app的时间，用于判断是否是每天第一次打开 */
    public static final void putOpenAppTime(Context context, long time){
        putLong(context, "open_app_time", time);
    }
    /** 获取打开app的时间，用于判断是否是每天第一次打开，默认是0 */
    public static final long getOpenAppTime(Context context){
        return getLong(context, "open_app_time", 0);
    }
    
    /** 存储首页提示上传照片的时间 */
    public static final void putTipUploadPicTime(Context context, long time){
        putLong(context, "tip_upload_pic_time", time);
    }
    /** 获取首页提示上传照片的时间，默认是0 */
    public static final long getTipUploadPicTime(Context context){
        return getLong(context, "tip_upload_pic_time", 0);
    }
    
    /** 存储首页提示开启碰缘分的时间 */
    public static final void putTipOpenYuanfenTime(Context context, long time){
        putLong(context, "tip_open_yuanfen_time", time);
    }
    /** 获取首页提示开启碰缘分的时间，默认是0 */
    public static final long getTipOpenYuanfenTime(Context context){
        return getLong(context, "tip_open_yuanfen_time", 0);
    }
    
    /** 存储首页提示完善个人资料的时间 */
    public static final void putTipSelfInfoTime(Context context, long time){
        putLong(context, "tip_self_info", time);
    }
    /** 获取首页提示完善个人资料的时间，默认是0 */
    public static final long getTipSelfInfoTime(Context context){
        return getLong(context, "tip_self_info", 0);
    }
    
    /** 存储首页提示更新照片的时间 */
    public static final void putTipUpdatePicTime(Context context, long time){
        putLong(context, "tip_update_picture", time);
    }
    /** 获取首页提示更新照片的时间，默认是0 */
    public static final long getTipUpdatePicTime(Context context){
        return getLong(context, "tip_update_picture", 0);
    }
    
    /** 存储更新照片的时间 */
    public static final void putUpdatePicTime(Context context, long time){
        putLong(context, "update_picture", time);
    }
    /** 获取更新照片的时间，默认是0 */
    public static final long getUpdatePicTime(Context context){
        return getLong(context, "update_picture", 0);
    }
    
    /**
     * 更新检查配置数据时间
     * @param context
     * @param time
     */
    public static final void putCheckConfigDataTime(Context context , long time){
    	putLong(context,"check_config_data",time);
    }
    
    /**
     * 获取上次检查配置数据的时间
     * @param context
     * @return
     */
    public static final long getCheckConfigDataTime(Context context){
    	return getLong(context,"check_config_data",0);
    }
    
    /** 存储排行榜背景图片版本号 */
    public static final void putRankPictureVersion(Context context, String version){
        putString(context, "rank_picture_version", version);
    }
    /** 获取排行榜背景图片版本号，默认是“” */
    public static final String getRankPictureVersion(Context context){
        return getString(context, "rank_picture_version", "");
    }
    
    /** 存储排行榜0背景图片地址 */
    public static final void putRankPicture0(Context context, String url){
        putString(context, "rank0_picture_url", url);
    }
    /** 获取排行榜0背景图片地址，默认是“” */
    public static final String getRankPicture0(Context context){
        return getString(context, "rank0_picture_url", "");
    }
    
    /** 存储排行榜1背景图片地址 */
    public static final void putRankPicture1(Context context, String url){
        putString(context, "rank1_picture_url", url);
    }
    /** 获取排行榜1背景图片地址，默认是“” */
    public static final String getRankPicture1(Context context){
        return getString(context, "rank1_picture_url", "");
    }
    
    /** 存储排行榜2背景图片地址 */
    public static final void putRankPicture2(Context context, String url){
        putString(context, "rank2_picture_url", url);
    }
    /** 获取排行榜2背景图片地址，默认是“” */
    public static final String getRankPicture2(Context context){
        return getString(context, "rank2_picture_url", "");
    }
    
    /** 存储排行榜3背景图片地址 */
    public static final void putRankPicture3(Context context, String url){
        putString(context, "rank3_picture_url", url);
    }
    /** 获取排行榜3背景图片地址，默认是“” */
    public static final String getRankPicture3(Context context){
        return getString(context, "rank3_picture_url", "");
    }
    
    /** 存储排行榜4背景图片地址 */
    public static final void putRankPicture4(Context context, String url){
        putString(context, "rank4_picture_url", url);
    }
    /** 获取排行榜4背景图片地址，默认是“” */
    public static final String getRankPicture4(Context context){
        return getString(context, "rank4_picture_url", "");
    }
    
    /** 存储排行榜0背景图片地址 */
    public static final void putRankPicture5(Context context, String url){
        putString(context, "rank5_picture_url", url);
    }
    /** 获取排行榜5背景图片地址，默认是“” */
    public static final String getRankPicture5(Context context){
        return getString(context, "rank5_picture_url", "");
    }
    /** 存储实力榜背景图片地址 */
    public static final void putRankPictureStrength(Context context, String url){
        putString(context, "rank_strength_picture_url", url);
    }
    /** 获取实力榜背景图片地址，默认是"" */
    public static final String getRankPictureStrength(Context context){
        return getString(context, "rank_strength_picture_url", "");
    } 
    /** 存储私照榜背景图片地址 */
    public static final void putRankPrivPictureForFemale(Context context, String url){
        putString(context, "rank_priv_picture_url", url);
    }
    /** 获取私照榜背景图片地址，默认是"" */
    public static final String getRankPrivPictureForFemale(Context context){
        return getString(context, "rank_priv_picture_url", "");
    }
    /**微博帐户信息  start*/
    public static final void putUid(Context context, String uid){
        putString(context, KEY_UID, uid);
    }
    public static final String getUid(Context context){
        String uid = getString(context, KEY_UID, "");
        return uid;
    }
    public static final void putAccessToken(Context context, String token){
        token = PDEEngine.PEncrypt(context, token);
        putString(context, KEY_ACCESS_TOKEN, token);
    }
    public static final String getAccessToken(Context context){
        String token = getString(context, KEY_ACCESS_TOKEN, "");
        if(!TextUtils.isEmpty(token)){
            token = PDEEngine.PDecrypt(context, token);
        }
        return token;
    }
    public static final void putExpireIn(Context context, long expire){
        putLong(context, KEY_EXPIRES_IN, expire);
    }
    public static final long getExpireIn(Context context){
        return getLong(context, KEY_EXPIRES_IN, 0);
    }
    public static final void deleteWeiboAcc(Context context){
        remove(context, KEY_UID);
        remove(context, KEY_ACCESS_TOKEN);
        remove(context, KEY_EXPIRES_IN);
    }
    /**微博帐户信息  end*/
    
    
    /** 存储搜索条件：起始年龄 */
    public static final void putSearchAgeStart(Context context, int age){
        putInt(context, "search_age_start", age);
    }
    /** 获取搜索条件：起始年龄，默认是18 */
    public static final int getSearchAgeStart(Context context){
        return getInt(context, "search_age_start", 18);
    }
    
    /** 存储搜索条件：截至年龄 */
    public static final void putSearchAgeEnd(Context context, int age){
        putInt(context, "search_age_end", age);
    }
    /** 获取搜索条件：截至年龄，默认是25 */
    public static final int getSearchAgeEnd(Context context){
        return getInt(context, "search_age_end", 25);
    }
    
    /** 存储搜索条件：星座 */
    public static final void putSearchAstro(Context context, int astro){
        putInt(context, "search_astro", astro);
    }
    /** 获取搜索条件：星座，默认是0 */
    public static final int getSearchAstro(Context context){
        return getInt(context, "search_astro", 0);
    }
    
    /** 存储搜索条件：地区 */
    public static final void putSearchArea(Context context, int area){
        putInt(context, "search_area", area);
    }
    /** 获取搜索条件：地区，默认是0 */
    public static final int getSearchArea(Context context){
        return getInt(context, "search_area", 0);
    }
    
    /** 存储搜索条件：收入 */
    public static final void putSearchIncome(Context context, int income){
        putInt(context, "search_income", income);
    }
    /** 获取搜索条件：收入，默认是0 */
    public static final int getSearchIncome(Context context){
        return getInt(context, "search_income", 0);
    }
    
    /** 存储搜索条件：是否需要私照 */
    public static final void putSearchPrivate(Context context, boolean need){
        putBoolean(context, "search_private", need);
    }
    /** 获取搜索条件：是否需要私照，默认是false */
    public static final boolean getSearchPrivate(Context context){
        return getBoolean(context, "search_private", false);
    }
    
    /** 存储从服务器更新用户信息的时间 */
    public static final void putUpdateUserInfoTime(Context context, long time){
        putLong(context, "update_userinfo_time", time);
    }
    /** 获取从服务器更新用户信息的时间，默认为0 */
    public static long getUpdateUserInfoTime(Context context){
        return getLong(context, "update_userinfo_time", 0);
    }
    
    /** 存储当前用户的用户等级 */
    public static final void putUserLevel(Context context, long uid, int level) {
    	putInt(context, "Level_"+uid, level);
    }
    /** 获取当前用户的用户等级 */
    public static int getUserLevel(Context context, long uid) {
    	return getInt(context, "Level_"+uid, 0);
    }
    
    /** 存储当前用户的礼物分享标识 */
    public static final void putUserGiftShareFlag(Context context, long uid, long giftId) {
    	putBoolean(context, "Gift_"+uid+"_"+giftId, true);
    }
    /** 获取当前用户的礼物分享标识 */
    public static boolean getUserGiftShareFlag(Context context, long uid, long giftId) {
    	return getBoolean(context, "Gift_"+uid+"_"+giftId, false);
    }
    
    /** 存储当前用户的收入分享标识 */
    public static final void putUserMoneyShareFlag(Context context, long uid, String month) {
    	putBoolean(context, "Money_"+uid+"_"+month, true);
    }
    /** 获取当前用户的收入分享标识 */
    public static boolean getUserMoneyShareFlag(Context context, long uid, String month) {
    	return getBoolean(context, "Money_"+uid+"_"+month, false);
    }
    
    /** 存储当前男性用户是否已经出现过聊天页面送礼物弹层标识 */
    public static final void putHasShownTipInPrivateSession(Context context, long uid) {
    	putBoolean(context, "HasShown_" + uid, true);
    }
    /** 获取当前男性用户是否已经出现过聊天页面送礼物弹层标识 */
    public static boolean getHasShownTipInPrivateSession(Context context, long uid) {
    	return getBoolean(context, "HasShown_" + uid, false);
    }
    
    /** 存储当前用户最后一次退出聊天页面时，是否显示录音layout */
    public static final void putIsShownRecordingInlastPrivateSession(Context context, long uid, boolean b) {
    	putBoolean(context, "IsShownRecordingInlast_" + uid, b);
    }
    /** 获取存储当前用户最后一次退出聊天页面时，是否显示录音layout */
    public static boolean getIsShownRecordingInlastPrivateSession(Context context, long uid) {
    	return getBoolean(context, "IsShownRecordingInlast_" + uid, false);
    }
    
    /** 存储当前用户是否已经出现过双击录音提示 */
    public static final void putHasShownDoubleClickGuide(Context context, long uid) {
    	putBoolean(context, "HasShownDoubleClickGuide_" + uid, true);
    }
    /** 获取当前是否已经出现过双击录音提示*/
    public static boolean getHasShownDoubleClickGuide(Context context, long uid) {
    	return getBoolean(context, "HasShownDoubleClickGuide_" + uid, false);
    }
    
	/** 存储当前用户是否开启发送礼物、解锁私照音效 */
	public static void putGiftsPicOn(Context context, boolean value,long uid) {
		putBoolean(context, GiftPicOn+uid, value);
	}
	/** 获取当前用户是否开启发送礼物、解锁私照音效 */
	public static boolean getGiftsPicOn(Context context,long uid) {
		return getBoolean(context, GiftPicOn+uid, true);
	}
	
	/** 存储聊天页面打开次数 */
	public static void putPrivateSessionCount(Context context,long uid, int count) {
		putInt(context, "PrivateSessionCount_"+uid, count);
	}

	/** 获取聊天页面打开次数 */
	public static int getPrivateSessionCount(Context context,long uid) {
		return getInt(context, "PrivateSessionCount_"+uid, -1);
	}
	/** 存储当前用户是否显示过索要礼物提醒 */
	public static void putGirlGiftsTipOn(Context context,long uid) {
		putBoolean(context, "GirlGiftsTipOn_"+uid, true);
	}

	/** 获取当前用户是否显示过索要礼物提醒 */
	public static boolean getGirlGiftsTipOn(Context context,long uid) {
		return getBoolean(context, "GirlGiftsTipOn_"+uid, false);
	}
	/** 存储当前用户是否需要显示索要礼物提醒 */
	public static void putShouldGirlGiftsTipOn(Context context,long uid) {
		putBoolean(context, "ShouldGirlGiftsTipOn_"+uid, true);
	}

	/** 获取当前用户是否需要显示索要礼物提醒 */
	public static boolean getShouldGirlGiftsTipOn(Context context,long uid) {
		return getBoolean(context, "ShouldGirlGiftsTipOn_"+uid, false);
	}
	
	/** 存储当前用户是否需要在聊天页面显示已是易信好友提醒 */
    public static void putShouldYixinFriendTip(Context context,long uid) {
        putBoolean(context, "ShouldYixinFriendTip"+uid, false);
    }

    /** 获取当前用户是否需要在聊天页面显示已是易信好友提醒 */
    public static boolean getShouldYixinFriendTip(Context context,long uid) {
        return getBoolean(context, "ShouldYixinFriendTip"+uid, true);
    }

    /** 存储当前会话的阅后即焚开关标识 */
    public static final void putSessionFireFlag(Context context, long myid, long anotherid, boolean isOpen) {
    	putBoolean(context, "Fire_"+myid+"_"+anotherid, isOpen);
    }
    /** 获取当前会话的阅后即焚开关标识 */
    public static boolean getSessionFireFlag(Context context, long myid, long anotherid) {
    	return getBoolean(context, "Fire_"+myid+"_"+anotherid, false);
    }
    
    /** 存储当前用户是否曾开启阅后即焚的标识 */
    public static final void putSessionHasOpenFireFlag(Context context,long uid) {
    	putBoolean(context, "HasOpenFire_"+uid, true);
    }
    /** 获取当前用户是否曾开启阅后即焚的标识 */
    public static boolean getSessionHasOpenFireFlag(Context context,long uid) {
    	return getBoolean(context, "HasOpenFire_"+uid, false);
    }
    
    /** 存储当前用户是否曾收到阅后即焚的标识 */
    public static final void putSessionHasReceiveFireFlag(Context context,long uid) {
    	putBoolean(context, "HasReceiveFire_"+uid, true);
    }
    /** 获取当前用户是否曾收到阅后即焚的标识 */
    public static boolean getSessionHasReceiveFireFlag(Context context,long uid) {
    	return getBoolean(context, "HasReceiveFire_"+uid, false);
    }
    
    /** 存储当前用户是否收到阅后即焚截屏的标识 */
    public static final void putSessionHasScreenShoteFlag(Context context,long uid, boolean flag) {
    	putBoolean(context, "HasSessionShot_"+uid, flag);
    }
    /** 获取当前用户是否收到阅后即焚的标识 */
    public static boolean getSessionHasScreenShoteFlag(Context context,long uid) {
    	return getBoolean(context, "HasSessionShot_"+uid, false);
    }

    /** 存储当前用户已在聊天页面显示第一次礼物提醒 */
    public static void putNeedCheckFirstGift(Context context,long uid) {
        putBoolean(context, "NeedCheckFirstGift"+uid, false);
    }
    /** 获取当前用户是否需要在聊天页面显示第一份免费礼物提醒 */
    public static boolean getNeedCheckFirstGift(Context context,long uid) {
        return getBoolean(context, "NeedCheckFirstGift"+uid, true);
    }

    /** 更新活动（晒照片）获取数据时间 */
    public static final void putPicShowOffDataTime(Context context, long time,long uid) {
        putLong(context, "pic_showoff__data", time);
    }

    /** 获取活动（晒照片）获取数据时间 */
    public static final long getPicShowOffDataTime(Context context, long uid) {
        return getLong(context, "pic_showoff__data", 0);
    }
    
    /** 存储当前用户是否显示新功能－聊天技 */
    public static void putNewFunctionLiaoTianJiFlag(Context context,long uid) {
        putBoolean(context, "NewFunction_LiaoTianJiFlag"+uid, false);
    }
    /** 获取当前用户是否显示新功能－聊天技 */
    public static boolean getNewFunctionLiaoTianJiFlag(Context context,long uid) {
        return getBoolean(context, "NewFunction_LiaoTianJiFlag"+uid, true);
    }
    
    /** 存储当前用户是否显示新功能－个人展示 */
    public static void putNewFunctionPersonalPresentationFlag(Context context,long uid) {
        putBoolean(context, "NewFunction_PersonalPresentation"+uid, false);
    }
    /** 获取当前用户是否显示新功能－个人展示 */
    public static boolean getNewFunctionPersonalPresentationFlag(Context context,long uid) {
        return getBoolean(context, "NewFunction_PersonalPresentation"+uid, true);
    }
}
