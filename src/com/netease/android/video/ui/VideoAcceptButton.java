package com.netease.android.video.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import com.netease.android.video.camera.ClipRecorderStateListener;

public class VideoAcceptButton extends Button implements
		ClipRecorderStateListener {
	public static final String TAG = "VideoAcceptButton";
	private ClipRecorderState mClipRecorderState = ClipRecorderState.STOPPED;

	public VideoAcceptButton(Context context) {
		super(context);
		configure();
	}

	public VideoAcceptButton(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		configure();
	}

	public VideoAcceptButton(Context context, AttributeSet attributeSet,
			int defStyle) {
		super(context, attributeSet, defStyle);
		configure();
	}

	private void configure() {
		if (mClipRecorderState == ClipRecorderState.STOPPED) {
			setEnabled(true);
		} else {
			setEnabled(false);
		}
	}

	public void onClipRecorderStateChange(ClipRecorderState recorderState) {
		this.mClipRecorderState = recorderState;
		configure();
	}
}