package com.netease.android.video.model.pendingmedia;

import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

public class FaceBox implements Parcelable {
	public static final Parcelable.Creator<FaceBox> CREATOR = new Creator<FaceBox>() {

		@Override
		public FaceBox[] newArray(int size) {
			return new FaceBox[size];
		}

		@Override
		public FaceBox createFromParcel(Parcel source) {
			return new FaceBox(source);
		}
	};
	private RectF mDetectionBox;

	public FaceBox(RectF paramRectF) {
		this.mDetectionBox = paramRectF;
	}

	private FaceBox(Parcel parcel) {
		this.mDetectionBox = new RectF();
		readRectF(parcel, this.mDetectionBox);
	}

	private void readRectF(Parcel parcel, RectF paramRectF) {
		paramRectF.left = parcel.readFloat();
		paramRectF.top = parcel.readFloat();
		paramRectF.right = parcel.readFloat();
		paramRectF.bottom = parcel.readFloat();
	}

	private void writeRectF(Parcel parcel, RectF paramRectF) {
		parcel.writeFloat(paramRectF.left);
		parcel.writeFloat(paramRectF.top);
		parcel.writeFloat(paramRectF.right);
		parcel.writeFloat(paramRectF.bottom);
	}

	public int describeContents() {
		return 0;
	}

	public RectF getDetectionBox() {
		return this.mDetectionBox;
	}

	public void writeToParcel(Parcel parcel, int paramInt) {
		writeRectF(parcel, this.mDetectionBox);
	}
}