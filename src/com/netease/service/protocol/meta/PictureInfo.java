package com.netease.service.protocol.meta;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.engagement.app.EgmConstants;

/**
 * 照片数据结构
 */
public class PictureInfo implements Parcelable{
	public long id ;//照片id
	public String name ;//照片名称（后端）
	public int type ;//照片类型 0：公开照 1：私密照
	public int status ;//照片状态：0未审，1通过，2未通过（后端）
	public String picUrl ;//照片Url
	public String smallPicUrl ;//私密照片小图
	public boolean isViewed ;//是否浏览过
	public long praiseCount ;//赞的个数 （私照）
	public long time ;//照片上传时间（后端）
	public int needCoins ;//私照解锁需要的金币数量
	public int giftId;	//私照解锁需要的礼物ID
	public int stepCount; //踩的个数 (私照)
	public long unlockCount; //私照解锁次数
	public int isCamera; //现拍照片:0:普通文件上传，1：拍照上传
	
	//用在界面控制上
	public boolean choosed ;
	public boolean stateChanged ;
	public String oldPrivateUrl; // 解锁后私照照片，用于平滑过渡，不需要Parcelable
	public boolean praised = false;
	public boolean unliked = false;
	
	public int vipFreeTime ;  
	
	public PictureInfo(){
//		Random random = new Random ();
//		id = random.nextLong();
//		type = random.nextInt(2);
//		picUrl = DebugData.getBigImage();
//		smallPicUrl = DebugData.getBigImage();
//		isViewed = random.nextBoolean();
//		praiseCount = random.nextLong();
//		
//		choosed = false ;
	}
	
	private PictureInfo(Parcel in){
		id = in.readLong();
		name = in.readString();
		type = in.readInt();
		status = in.readInt();
		picUrl = in.readString();
		smallPicUrl = in.readString();
		isViewed = (Boolean)in.readValue(boolean.class.getClassLoader());
		praiseCount = in.readLong();
		choosed = (Boolean)in.readValue(boolean.class.getClassLoader());
		time = in.readLong();
		needCoins = in.readInt() ;
		vipFreeTime = in.readInt();
		giftId = in.readInt();
		stepCount = in.readInt();
		unlockCount = in.readLong();
		isCamera = in.readInt();
	}
	
	public boolean isCamera() {
		return this.isCamera == EgmConstants.IsCameraPhotoFlag.CameraPhoto && EgmConstants.PhotoStatus.Success == this.status;
	}
	
	/**
     * 从JSON字符串中生成数据结构
     * @param jsonStr
     * @return
     */
    public static PictureInfo fromJson(JsonElement json){
        if(json == null)
            return null;
        
        Gson gson = new Gson();
        return gson.fromJson(json, PictureInfo.class);
    }
	
	public static final Parcelable.Creator<PictureInfo> CREATOR = new Parcelable.Creator<PictureInfo>() {
        @Override
        public PictureInfo createFromParcel(Parcel in) {
            return new PictureInfo(in);
        }
        @Override
        public PictureInfo[] newArray(int size) {
            return new PictureInfo[size];
        }
    };
    
    @Override
	public int describeContents() {
		return 0;
	}
    
    @Override
	public void writeToParcel(Parcel dest, int flags) {
    	dest.writeLong(id);
    	dest.writeString(name);
    	dest.writeInt(type);
    	dest.writeInt(status);
    	dest.writeString(picUrl);
    	dest.writeString(smallPicUrl);
    	dest.writeValue(isViewed);
    	dest.writeLong(praiseCount);
    	dest.writeValue(choosed);
    	dest.writeLong(time);
    	dest.writeInt(needCoins);
    	dest.writeInt(vipFreeTime);
    	dest.writeInt(giftId);
    	dest.writeInt(stepCount);
    	dest.writeLong(unlockCount);
    	dest.writeInt(isCamera);
    }

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof PictureInfo) {
			return ((PictureInfo)o).id == this.id;
		}
		return false;
	}
    
}
