package com.netease.android.video.model.pendingmedia;

public class ClipInfo {
	private float mAspectRatio;
	private int mCameraId;
	private int mEndTime;
	private int mHeight;
	private float mPan = 0.5F;
	private int mRotation;
	private int mStartTime;
	private String mVideoFilePath;
	private int mWidth;

	public float getAspectRatio() {
		return this.mAspectRatio;
	}

	public int getCameraId() {
		return this.mCameraId;
	}

	public int getDuration() {
		return this.mEndTime - this.mStartTime;
	}

	public int getEndTime() {
		return this.mEndTime;
	}

	public int getHeight() {
		return this.mHeight;
	}

	public float getPan() {
		return this.mPan;
	}

	public int getRotation() {
		return this.mRotation;
	}

	public int getStartTime() {
		return this.mStartTime;
	}

	public String getVideoFilePath() {
		return this.mVideoFilePath;
	}

	public int getWidth() {
		return this.mWidth;
	}

	public boolean isPortrait() {
		if (this.mWidth < this.mHeight) {
			return true;
		}
		return false;
	}

	public ClipInfo setCameraId(int cameraId) {
		this.mCameraId = cameraId;
		return this;
	}

	public void setDimensions(int paramInt1, int paramInt2) {
		this.mWidth = paramInt1;
		this.mHeight = paramInt2;
		this.mAspectRatio = (paramInt1 / paramInt2);
	}

	public ClipInfo setEndTime(int paramInt) {
		this.mEndTime = paramInt;
		return this;
	}

	public void setExtractedDimensions(int paramInt1, int paramInt2) {
		if ((this.mRotation == 1) || (this.mRotation == 3)) {
			setDimensions(paramInt2, paramInt1);
		} else {
			setDimensions(paramInt1, paramInt2);
		}
	}

	public ClipInfo setPan(float paramFloat) {
		this.mPan = paramFloat;
		return this;
	}

	public ClipInfo setRotation(int paramInt) {
		this.mRotation = paramInt;
		return this;
	}

	public ClipInfo setStartTime(int paramInt) {
		this.mStartTime = paramInt;
		return this;
	}

	public ClipInfo setVideoFilePath(String paramString) {
		this.mVideoFilePath = paramString;
		return this;
	}
}