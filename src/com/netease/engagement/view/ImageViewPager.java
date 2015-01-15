package com.netease.engagement.view;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class ImageViewPager extends ViewPager {
	
	private static final boolean DEBUG = false;
	private static final String TAG = "ImageViewPager";
	
	private int mTouchSlop = 0;
	
	private int mFrontColor;

	public ImageViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init(context);
	}

	public ImageViewPager(Context context) {
		super(context);
		
		init(context);
	}
	
	private void init(Context context) {
		 mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		 
		 if (DEBUG)
			Log.e(TAG, "init: " + "mTouchSlop: " + mTouchSlop);
		
	}
	
	public void setFrontColor(int argb) {
		if (mFrontColor != argb) {
			mFrontColor = argb;
			
			invalidate();
		}
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		
		if (mFrontColor != 0) {
			canvas.drawColor(mFrontColor);
			
//			canvas.drawARGB(mFrontColor >> 24, 0xFF | (mFrontColor >> 16),
//					0xFF | (mFrontColor >> 8), 0xFF | mFrontColor);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		boolean ret = false;
		try {
			ret = super.onInterceptTouchEvent(ev);
		} catch (Exception e) {
		}
		
		if (DEBUG)
			Log.e(TAG, "onInterceptTouchEvent: " + ret + " ev: " + ev.getAction()
					+ " x: " + ev.getX() + " y: " + ev.getY());
		
		return ret;
	}
//	
//	private void requestParentDisallowInterceptTouchEvent(boolean value) {
//		ViewParent view = getParent();
//		if (view != null) {
//			view.requestDisallowInterceptTouchEvent(value);
//		}
//	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		boolean ret = false;
		try {
			ret = super.onTouchEvent(ev);
		} catch (Exception e) {
		}
		
		if (DEBUG)
			Log.e(TAG, "onTouchEvent: " + ret + " ev: " + ev.getAction()
					+ " x: " + ev.getX() + " y: " + ev.getY());
		
		return ret;
	}
	
}
