package com.netease.android.video.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.netease.android.util.DpAndPxUtils;
import com.netease.android.video.ClipStackManager;
import com.netease.android.video.ClipStackManager.ClipStackManagerChangeListener;
import com.netease.android.video.camera.ClipRecorderStateListener;
import com.netease.android.video.model.Clip;
import com.netease.android.video.model.Clip.ClipState;
import com.netease.date.R;

public class CamcorderBlinker extends ImageView implements
		ClipStackManagerChangeListener, ClipRecorderStateListener {
	private Animation mAnimation;
	private ClipStackManager mClipStackManager;
	private int mScreenWidth = DpAndPxUtils.getScreenWidthPixels();

	public CamcorderBlinker(Context context) {
		super(context);
		configure();
	}

	public CamcorderBlinker(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		configure();
	}

	public CamcorderBlinker(Context context, AttributeSet attributeSet,
			int defStyle) {
		super(context, attributeSet, defStyle);
		configure();
	}

	private void configure() {
		mAnimation = AnimationUtils.loadAnimation(getContext(),
				R.anim.video_camcorder_blinker);
	}

	private void move() {
		if ((this != null) && (mClipStackManager != null)) {
			int totalClipLength = mClipStackManager.getTotalClipLength();
			int initialLeftOffset = (int) getResources().getDimension(
					R.dimen.blinker_initial_left_offset);
			double xPosition = ((double) totalClipLength / ClipView.MAX_RECORD_DURATION)
					* mScreenWidth - initialLeftOffset;
			int marginRight = initialLeftOffset
					+ (mScreenWidth - getMeasuredWidth());
			if ((int) xPosition < marginRight) {
				RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
				layoutParams.setMargins((int) xPosition, mScreenWidth,
						-marginRight, 0);
				layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				setLayoutParams(layoutParams);
			} else {
				Log.v("CamcorderBlinker", "xPosition:" + xPosition);
			}
		}
	}

	private void show() {
		startAnimation(mAnimation);
		setVisibility(View.VISIBLE);
	}

	public void hide() {
		clearAnimation();
		setVisibility(View.GONE);
	}

	public void onClipAdded(Clip clip) {
	}

	public void onClipChanged(Clip clip, ClipState clipState) {
		if (clipState == ClipState.SOFT_DELETED) {
			hide();
		} else {
			if (!mClipStackManager.isAlmostFull())
				show();
		}
	}

	public void onClipDurationChanged(Clip clip) {
		move();
	}

	public void onClipRecorderStateChange(ClipRecorderState state) {
		if (state == ClipRecorderState.RECORDING) {
			clearAnimation();
			return;
		}
		if (!mClipStackManager.isAlmostFull())
			startAnimation(mAnimation);
	}

	public void onClipRemoved(Clip paramClip) {
		show();
		move();
	}

	public void onClipStackFull() {
		hide();
	}

	public void resume() {
		if (mClipStackManager.isFull()) {
			hide();
		} else {
			show();
			move();
		}
	}

	public void setClipStackManager(ClipStackManager clipStackManager) {
		mClipStackManager = clipStackManager;
		move();
	}
}