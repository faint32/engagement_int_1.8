package com.netease.service.protocol.meta;


import com.google.gson.Gson;
import com.netease.service.protocol.meta.DebugData.BaseDataTest;

public class UserInfoDetail{
	public UserInfo userInfo ;//用户信息
	public PictureInfo[] publicPicList ;//公开照片列表
	public PictureInfo[] privatePicList ;//私照列表，仅女性
	public GiftRecord[] giftList ;//礼物列表,女性为收到列表，男性为送
	public AdmirerUserInfo[] loveList ;//爱慕者列表（仅女性）
	public boolean IsYixinFriend ;//是否易信好友
	public boolean isBlack ;//此用户是否在访问者的黑名单里
	public String animat;//动画效果图片url
	public SpecialGift[] specialGifts ;//特殊礼物列表
	
	public static String toJsonString(UserInfoDetail user) {
        Gson gson = new Gson();
        return gson.toJson(user);
    }
	
	public static String generateTestData(long uid){
        Gson gson = new Gson();
        UserInfoDetail userInfoDetail = new UserInfoDetail();
        BaseDataTest baseData = new BaseDataTest();
        baseData.code = 0;
        baseData.message = "success";
        baseData.data = userInfoDetail;
        String jsonString = gson.toJson(baseData);
        DebugData.saveTestData(DebugData.FILENAME_USERINFO_JSON + uid, jsonString);
        return jsonString;    
    }

}
