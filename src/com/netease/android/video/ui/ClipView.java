package com.netease.android.video.ui;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.netease.android.video.model.Clip;
import com.netease.android.video.model.Clip.ClipState;
import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;

public class ClipView extends LinearLayout implements Clip.ClipListener {
	private final View mActiveClipView;
	private final Clip mClip;
	public static final int MAX_RECORD_DURATION = EgmConstants.VIDEO_MAX_LENGH*1000 + 1000;//加1s,否则full处理时长度不够

	public ClipView(Context context, Clip clip) {
		super(context);
		setTag(clip);
		mClip = clip;
		mClip.addListener(this);
		mActiveClipView = new View(context);
		addView(mActiveClipView);
		initState(clip.getState(), mActiveClipView);
	}

	private void initState(ClipState clipState, View view) {
		if ((clipState == ClipState.RECORDING)
				|| (clipState == ClipState.RECORDED)) {
			view.setBackgroundResource(R.color.video_progress);
		} else if (clipState == ClipState.SOFT_DELETED) {
			view.setBackgroundResource(R.drawable.video_camera_video_progress_red);
		}
	}

	@Override
	public void onClipDurationChanged(Clip clip, long paramLong) {
		requestLayout();
	}

	@Override
	public void onClipStateChange(Clip clip, ClipState clipState) {
		initState(clipState, mActiveClipView);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		int measuredWidth = (int) (width * ((float) mClip.getDuration() / MAX_RECORD_DURATION));
		int widthSpec = MeasureSpec.makeMeasureSpec(measuredWidth,
				MeasureSpec.EXACTLY);
		mActiveClipView.measure(widthSpec, heightMeasureSpec);
		setMeasuredDimension(measuredWidth, height);
	}
}