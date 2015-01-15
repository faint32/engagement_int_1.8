package com.netease.service.protocol.meta;

import android.os.Parcel;
import android.os.Parcelable;

public class ApplyWithdrawError implements Parcelable {
	public String sampleDesc;
	public String samplePicUrl;
	public String name;
	public String idCardNo;
	public String idCardPic1;
	public String idCardPic2;
	
	public String errMessage; 
	
	public ApplyWithdrawError() {
		
	}
	
	public ApplyWithdrawError(Parcel source) {
		sampleDesc = source.readString();
		samplePicUrl = source.readString();
		name = source.readString();
		idCardNo = source.readString();
		idCardPic1 = source.readString();
		idCardPic2 = source.readString();
		errMessage = source.readString();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(sampleDesc);
		dest.writeString(samplePicUrl);
		dest.writeString(name);
		dest.writeString(idCardNo);
		dest.writeString(idCardPic1);
		dest.writeString(idCardPic2);
		dest.writeString(errMessage);
	}
	
	public static final Creator<ApplyWithdrawError> CREATER 
			= new Creator<ApplyWithdrawError>() {
		
		@Override
		public ApplyWithdrawError[] newArray(int size) {
			return new ApplyWithdrawError[size];
		}
		
		@Override
		public ApplyWithdrawError createFromParcel(Parcel source) {
			return new ApplyWithdrawError(source);
		}
	}; 
}
