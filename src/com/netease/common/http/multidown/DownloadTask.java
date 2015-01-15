package com.netease.common.http.multidown;

import android.os.Parcel;
import android.os.Parcelable;

public class DownloadTask implements Parcelable {

	private String mTitle;
	private String mUrl;
	private String mTargetPath;
	
	public DownloadTask(String title, String url, String targetPath) {
		mTitle = title;
		mUrl = url;
		mTargetPath = targetPath;
	}
	
	public DownloadTask(Parcel source) {
		mTitle = source.readString();
		mUrl = source.readString();
		mTargetPath = source.readString();
	}
	
	public String getUrl() {
		return mUrl;
	}
	
	public String getTargetPath() {
		return mTargetPath;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mTitle);
		dest.writeString(mUrl);
		dest.writeString(mTargetPath);
	}

	public static final Parcelable.Creator<DownloadTask> CREATOR 
		= new Parcelable.Creator<DownloadTask>() {

		@Override
		public DownloadTask createFromParcel(Parcel source) {
			return new DownloadTask(source);
		}

		@Override
		public DownloadTask[] newArray(int size) {
			return new DownloadTask[size];
		}
	
};
}
