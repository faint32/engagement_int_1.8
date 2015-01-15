package com.netease.android.widget.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class SquareFrameLayout extends FrameLayout {

	public SquareFrameLayout(Context context) {
		super(context);
	}

	public SquareFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SquareFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int screenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
		int screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
		super.onMeasure(View.MeasureSpec.makeMeasureSpec(screenWidth,
				MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(
				screenHeight, MeasureSpec.EXACTLY));
		setMeasuredDimension(screenWidth, screenWidth);
	}

}
