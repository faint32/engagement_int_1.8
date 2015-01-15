package com.netease.service.protocol.meta;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

/**
 * 聊天扩展内容
 */
public class MsgExtra implements Parcelable{
	public int sysMsgType ;//系统消息类型
	public String title ;//活动标题
	public String url ;//活动url
	public String button1;//按钮1描述
	public String button2;//按钮2描述
	
	public static MsgExtra toMsgExtra(String json){
		MsgExtra extra = null ;
		Gson gson = new Gson();
		extra = gson.fromJson(json,MsgExtra.class);
		return extra ;
	}
	public MsgExtra(){
	    
	}
	private MsgExtra(Parcel in){
	    sysMsgType = in.readInt();
	    title = in.readString();
	    url = in.readString();
	    button1 = in.readString();
	    button2 = in.readString();
    }
	
	public static final Parcelable.Creator<MsgExtra> CREATOR = new Parcelable.Creator<MsgExtra>() {
        @Override
        public MsgExtra createFromParcel(Parcel in) {
            return new MsgExtra(in);
        }
        @Override
        public MsgExtra[] newArray(int size) {
            return new MsgExtra[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(sysMsgType);
        dest.writeString(title);
        dest.writeString(url);
        dest.writeString(button1);
        dest.writeString(button2);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
