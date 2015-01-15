package com.netease.android.video.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.netease.android.video.ClipStackManager;
import com.netease.android.video.ClipStackManager.ClipStackManagerChangeListener;
import com.netease.android.video.camera.ClipRecorderStateListener;
import com.netease.android.video.model.Clip;
import com.netease.date.R;

public class VideoShutterButton extends ImageView implements
		ClipStackManagerChangeListener, ClipRecorderStateListener {
	private ClipRecorderState mClipRecorderState = ClipRecorderState.STOPPED;
	private ClipStackManager mClipStackManager;

	public VideoShutterButton(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
	}

	private void configure() {
		if (mClipStackManager == null) {
			setBackgroundResource(R.drawable.btn_pgchat_video_record);
			setEnabled(false);
		} else {
			if (mClipStackManager.getHasImportedClips()) {
				setBackgroundResource(R.drawable.btn_pgchat_video_record);
				setEnabled(false);
			} else if (mClipStackManager.isAlmostFull()) {
				setBackgroundResource(R.drawable.btn_pgchat_video_record);
				setEnabled(false);
			} else if ((mClipRecorderState == ClipRecorderState.RECORDING)
					|| (mClipRecorderState == ClipRecorderState.PREPARING)) {
				setBackgroundResource(R.drawable.btn_pgchat_video_record_stop);
				setEnabled(true);
			} else if ((mClipRecorderState == ClipRecorderState.STOPPED)
					|| (mClipRecorderState == ClipRecorderState.STOPPING)) {
				setBackgroundResource(R.drawable.btn_pgchat_video_record);
				setEnabled(true);
			}
		}
	}

	private void updateState() {
		configure();
		requestLayout();
	}

	@Override
	public boolean isEnabled() {
		if ((mClipRecorderState == ClipRecorderState.RECORDING)
				|| (mClipRecorderState == null)
				|| (mClipStackManager.isAlmostFull())) {
			return false;
		}
		return super.isEnabled();
	}

	@Override
	public void onClipAdded(Clip paramClip) {
		updateState();
	}

	@Override
	public void onClipChanged(Clip paramClip, Clip.ClipState paramClipState) {
		updateState();
	}

	@Override
	public void onClipDurationChanged(Clip paramClip) {
	}

	@Override
	public void onClipRecorderStateChange(ClipRecorderState clipRecorderState) {
		mClipRecorderState = clipRecorderState;
		updateState();
	}

	@Override
	public void onClipRemoved(Clip paramClip) {
		updateState();
	}

	@Override
	public void onClipStackFull() {
		updateState();
	}

	public void setClipStackManager(ClipStackManager clipStackManager) {
		mClipStackManager = clipStackManager;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
//		if (enabled) {
//			getBackground().setAlpha(255);
//		} else {
//			getBackground().setAlpha(128);
//		}
	}
}