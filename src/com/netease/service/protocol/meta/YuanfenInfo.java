package com.netease.service.protocol.meta;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * 碰缘分状态数据
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class YuanfenInfo implements Parcelable{
    /** 是否开户碰缘份功能 */
    public boolean isOpen = false;
    /** 类型：0，语音；1，文字 */
    public int type;
    /** 群发文字内容 */
    public String text;
    /** 群发语音url */
    public String voiceUrl; 
    /** 录音时长 */
    public int duration;
    
    public YuanfenInfo(){}
    
    public void set(YuanfenInfo info){
        this.isOpen = info.isOpen;
        this.type = info.type;
        this.text = info.text;
        this.voiceUrl = info.voiceUrl;
        this.duration = info.duration;
    }

    private YuanfenInfo(Parcel in){
        isOpen = (Boolean)in.readValue(boolean.class.getClassLoader());
        type = in.readInt();
        text = in.readString();
        voiceUrl = in.readString();
        duration = in.readInt();
    }
    
    /**
     * 从JSON字符串中生成数据结构
     * @param jsonStr
     * @return
     */
    public static YuanfenInfo fromJson(JsonElement json){
        if(json == null)
            return null;
        
        Gson gson = new Gson();
        return gson.fromJson(json, YuanfenInfo.class);
    }
    
    public static final Parcelable.Creator<YuanfenInfo> CREATOR = new Parcelable.Creator<YuanfenInfo>() {
        @Override
        public YuanfenInfo createFromParcel(Parcel in) {
            return new YuanfenInfo(in);
        }
        @Override
        public YuanfenInfo[] newArray(int size) {
            return new YuanfenInfo[size];
        }
    };
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(isOpen);
        dest.writeInt(type);
        dest.writeString(text);
        dest.writeString(voiceUrl);
        dest.writeInt(duration);
    }
}
