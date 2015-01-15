package com.netease.engagement.dataMgr;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 视频相关信息封装
 */
public class VideoInfo implements Parcelable{
	public String filePath ;
	public long size ;
	public long duration ;
	public String resolution ;
	
	public VideoInfo(){}
	
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	
	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(filePath);
		dest.writeLong(size);
		dest.writeLong(duration);
	}
	
	public VideoInfo(Parcel in){
		filePath = in.readString();
		size = in.readLong();
		duration = in.readLong();
	}
	
	public static final Parcelable.Creator<VideoInfo> CREATOR = new Parcelable.Creator<VideoInfo>() {
        @Override
        public VideoInfo createFromParcel(Parcel in) {
            return new VideoInfo(in);
        }
        @Override
        public VideoInfo[] newArray(int size) {
            return new VideoInfo[size];
        }
    };
}
