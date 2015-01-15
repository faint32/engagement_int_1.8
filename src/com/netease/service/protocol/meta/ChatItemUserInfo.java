package com.netease.service.protocol.meta;

import android.os.Parcel;
import android.os.Parcelable;

public class ChatItemUserInfo implements Parcelable{
	public long uid ;//用户id
	public String nick ;//对方昵称
	public boolean isVip ;//对方是否vip
	public boolean isNew ;//对方是否新用户
	public String portraitUrl192 ;//用户192x192头像裁剪图url
	public int crownId ;//当前戴的皇冠的id
	
	public ChatItemUserInfo(){}
	
	private ChatItemUserInfo(Parcel in){
		uid = in.readLong();
		nick = in.readString();
		isVip = (Boolean)in.readValue(boolean.class.getClassLoader());
		isNew = (Boolean)in.readValue(boolean.class.getClassLoader());
		portraitUrl192 = in.readString();
		crownId = in.readInt();
	}

	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Parcelable.Creator<ChatItemUserInfo> CREATOR = new Parcelable.Creator<ChatItemUserInfo>() {
        @Override
        public ChatItemUserInfo createFromParcel(Parcel in) {
            return new ChatItemUserInfo(in);
        }
        @Override
        public ChatItemUserInfo[] newArray(int size) {
            return new ChatItemUserInfo[size];
        }
    };

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(uid);
		dest.writeString(nick);
		dest.writeValue(isVip);
		dest.writeValue(isNew);
		dest.writeString(portraitUrl192);
		dest.writeInt(crownId);
	}
}
