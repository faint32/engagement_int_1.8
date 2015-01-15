package com.netease.android.camera.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import com.netease.date.R;

public class ActionBarCameraFlashButton extends ActionBarHighlightButton {
	private static final String TAG = "ActionBarCameraFlashButton";
	private FlashMode mCurrentMode;
	private Drawable mFlashAutoDrawable;
	private Drawable mFlashOffDrawable;
	private Drawable mFlashOnDrawable;
	private FlashButtonOnClickListener mOnClickListener = null;

	public ActionBarCameraFlashButton(Context context) {
		super(context);
		this.mCurrentMode = FlashMode.OFF;
		init();
	}

	public ActionBarCameraFlashButton(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		this.mCurrentMode = FlashMode.OFF;
		init();
	}

	private Drawable getDrawable(FlashMode flashMode) {
		Drawable drawable;
		switch (flashMode) {
			case AUTO:
				drawable = this.mFlashAutoDrawable;
				break;
			case ON:
				drawable = this.mFlashOnDrawable;
				break;
			default:
				drawable = this.mFlashOffDrawable;
		}
		return drawable;
	}

	private void init() {
		this.mFlashOffDrawable =  getButtonDrawable();
		this.mFlashOnDrawable = getContext().getResources().getDrawable(R.drawable.video_flash_on_selector);
		this.mFlashAutoDrawable =  getContext().getResources().getDrawable(R.drawable.video_flash_auto_selector);
		setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				Log.d(TAG, "isChecked:" + isChecked);
			}
		});
		super.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showMode(mCurrentMode);
				if(mOnClickListener != null){
					mOnClickListener.onClick(v, mCurrentMode);
				}
			}
		});
	}

	@Deprecated
	public void setOnClickListener(View.OnClickListener onClickListener) {
		throw new RuntimeException("Use FlashButtonOnClickListener");
	}

	public void setOnClickListener(
			FlashButtonOnClickListener flashButtonOnClickListener) {
		this.mOnClickListener = flashButtonOnClickListener;
	}

	public void showMode(FlashMode flashMode) {
		if (flashMode != this.mCurrentMode){
			setBackgroundDrawable(getDrawable(flashMode));
			this.mCurrentMode = flashMode;
		}
	}

	public static abstract interface FlashButtonOnClickListener {
		public abstract void onClick(View view, FlashMode flashMode);

	}

	public static enum FlashMode {
		OFF(1), AUTO(2), ON(3);
		private final int code;

		private FlashMode(int code) {
			this.code = code;
		}
	}
}