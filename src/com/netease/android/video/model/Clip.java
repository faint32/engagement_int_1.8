package com.netease.android.video.model;

import java.util.ArrayList;
import java.util.List;

import com.netease.android.video.ui.VideoFileUtil;

public class Clip {
	private static int INVALID_DURATION = -1;
	private int mCameraId;
	private long mDurationMillis;
	private List<ClipListener> mListeners = new ArrayList<ClipListener>();
	private ClipState mState;
	private long mSystemStartTime;
	private String mVideoPath;

	public Clip(int cameraId, long mDuration, String videoPath) {
		mCameraId = cameraId;
		mDurationMillis = mDuration;
		mState = ClipState.RECORDED;
		mVideoPath = videoPath;
	}

	public Clip(long startTime) {
		mSystemStartTime = startTime;
	}

	public static Clip fromUri(String filePath) {
		Clip clip = null;
		try {
			clip = new Clip(-1, VideoFileUtil.getClipDurationMillis(filePath),
					filePath);
		} catch (Exception exception) {
			clip = new Clip(0, INVALID_DURATION, null);
		}
		return clip;
	}

	public void addListener(ClipListener listener) {
		mListeners.add(listener);
	}

	public int getCameraId() {
		return mCameraId;
	}

	public String getDescription() {
		return "mVideoFile: " + mVideoPath + " duration " + getDuration();
	}

	public long getDuration() {
		return mDurationMillis;
	}

	public Clip.ClipState getState() {
		return mState;
	}

	public String getVideoPath() {
		return mVideoPath;
	}

	public void removeListener(ClipListener listener) {
		mListeners.remove(listener);
	}

	public void setCameraId(int cameraId) {
		mCameraId = cameraId;
	}

	public void setDuration(long mDuration) {
		mDurationMillis = mDuration;
		for (ClipListener listener : mListeners)
			listener.onClipDurationChanged(this, mDuration);
	}

	public void setState(ClipState clipState) {
		mState = clipState;
		for (ClipListener listener : mListeners)
			listener.onClipStateChange(this, clipState);
	}

	public void setSystemStopTime(long stopTime) {
		setDuration(stopTime - mSystemStartTime);
	}

	public void setVideoFile(String videoPath) {
		mVideoPath = videoPath;
	}
	public void resetStartTime(){
		long curTime = System.currentTimeMillis();
		if(mSystemStartTime < curTime){
			mSystemStartTime  = curTime;
		}
	}
	public abstract interface ClipListener {
		public abstract void onClipDurationChanged(Clip clip, long mDuration);

		public abstract void onClipStateChange(Clip clip, ClipState state);
	}

	public enum ClipState {
		RECORDING, RECORDED, SOFT_DELETED, INVALID;
	}
}