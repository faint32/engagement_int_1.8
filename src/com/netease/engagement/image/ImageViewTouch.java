package com.netease.engagement.image;

import com.netease.engagement.image.MultiTouchController.OnZoomCallback;
import com.netease.engagement.image.cropimage.ImageViewTouchBase;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ImageViewTouch extends ImageViewTouchBase {

	private MultiTouchController mController;
	private boolean mAllow = false;

	public void allowTouch(boolean allow) {
		mAllow = allow;
		if (mAllow) {
			mController = new MultiTouchController(getContext(), this);
		}
	}
	public ImageViewTouch(Context context) {
		super(context);
	}
	
	public ImageViewTouch(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public  void postTranslateCenter(float dx, float dy) {
		super.postTranslate(dx, dy);
		center(true, true);
	}
	
	public void setOnImageZoomCb(OnZoomCallback cb) {
		if (mAllow && mController != null) {
			mController.setOnZoomCallback(cb); 
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mAllow && mController != null) {
			return mController.onMultiTouchEvent(event);
		} 
		return super.onTouchEvent(event);
	}
	
	@Override
	public void clear() {
		super.clear();
		if (mController != null) {
			mController.clear();
			mController = null;
		}
	}
}
