package com.netease.android.video.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.netease.android.video.ClipStackManager.ClipStackManagerChangeListener;
import com.netease.android.video.model.Clip;
import com.netease.android.video.model.ClipStack;

@TargetApi(11)
public class ClipStackView extends LinearLayout implements
		ClipStackManagerChangeListener {
	private ClipStack mClipStack;

	public ClipStackView(Context context) {
		super(context);
	}

	public ClipStackView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
	}

	public ClipStackView(Context context, AttributeSet attributeSet,
			int defStyle) {
		super(context, attributeSet, defStyle);
	}

	private void initStackView() {
		for (Clip clip : mClipStack) {
			addView(new ClipView(getContext(), clip));
		}
	}

	public void destroyStackView() {
		for (int i = 0; i < getChildCount(); i++) {
			ClipView clipView = (ClipView) getChildAt(i);
			((Clip) clipView.getTag()).removeListener(clipView);
		}
	}

	public void onClipAdded(Clip clip) {
		addView(new ClipView(getContext(), clip));
	}

	public void onClipChanged(Clip paramClip, Clip.ClipState paramClipState) {
	}

	public void onClipDurationChanged(Clip paramClip) {
	}

	public void onClipRemoved(Clip clip) {
		ClipView clipView = (ClipView) findViewWithTag(clip);
		clip.removeListener(clipView);
		removeView(clipView);
	}

	public void onClipStackFull() {
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int mMeasureWidth = View.MeasureSpec.makeMeasureSpec(
				getMeasuredWidth(), MeasureSpec.AT_MOST);
		int mMeasureHeight = View.MeasureSpec.makeMeasureSpec(
				getMeasuredHeight(), MeasureSpec.AT_MOST);
		for (int i = 0; i < getChildCount(); i++)
			getChildAt(i).measure(mMeasureWidth, mMeasureHeight);
	}

	public void setClipStack(ClipStack clipStack) {
		this.mClipStack = clipStack;
		initStackView();
	}
}