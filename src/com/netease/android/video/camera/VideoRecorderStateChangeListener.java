package com.netease.android.video.camera;

public abstract interface VideoRecorderStateChangeListener {
	public enum VideoState {
		STOPPED(0), RECORDING(1);
		private int state;

		private VideoState(int state) {
			this.state = state;
		}
	}

	public abstract void onVideoRecorderStateChange(String paramString,
			VideoRecorderStateChangeListener.VideoState paramVideoState,
			long paramLong);
}