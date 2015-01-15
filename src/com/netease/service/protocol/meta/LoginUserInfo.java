package com.netease.service.protocol.meta;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.meta.DebugData.BaseDataTest;

/**
 * 登录URS完成后从同城服务器获取到的用户数据的数据结构，包括push所需的数据
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class LoginUserInfo implements Parcelable {
    private static final boolean DEBUG = EgmProtocolConstants.RANDOM_DEBUG_DATA;
    
    public UserInfo userInfo;
    /** 随机字符串，用于push */
    public String nonce;
    /** 表示签名过期时间(时间戳)，用于push */
    public long expire;
    /** 签名，用于push */
    public String signature;
    
    /** 碰缘分标识，用于判断女性是否开启碰缘分 */
    public boolean  isOpenFate;
    
    public LoginUserInfo(){
        if(DEBUG){
            userInfo = new UserInfo();
            nonce = DebugData.getText();
            expire = System.currentTimeMillis() + 24 * 60 * 60 * 60 * 1000 * 10;
            signature = DebugData.getText();
        }
    }
    
    public LoginUserInfo(Parcel in){
        userInfo = in.readParcelable(UserInfo.class.getClassLoader());
        nonce = in.readString();
        expire = in.readLong();
        signature = in.readString();
    }
    
    /**
     * 从JSON字符串中生成数据结构
     * @param jsonStr
     * @return
     */
    public static LoginUserInfo fromJson(JsonElement json){
        if(json == null)
            return null;
        
        Gson gson = new Gson();
        return gson.fromJson(json, LoginUserInfo.class);
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(userInfo, flags);
        dest.writeString(nonce);
        dest.writeLong(expire);
        dest.writeString(signature);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Parcelable.Creator<LoginUserInfo> CREATOR = new Parcelable.Creator<LoginUserInfo>() {
        @Override
        public LoginUserInfo createFromParcel(Parcel source) {
            return new LoginUserInfo(source);
        }

        @Override
        public LoginUserInfo[] newArray(int size) {
            return new LoginUserInfo[size];
        }
    };
    
    public static String generateTestInfo(){
        Gson gson = new Gson();
        LoginUserInfo info = new LoginUserInfo();
        BaseDataTest baseData = new BaseDataTest();
        baseData.code = 0;
        baseData.message = "success";
        baseData.data = info;
        String jsonString = gson.toJson(baseData);
        DebugData.saveTestData(DebugData.FILENAME_LOGIN_USERINFO_JSON, jsonString);
        return jsonString;    
    }
}
