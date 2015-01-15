package com.netease.engagement.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ScrollView;

public class UserScrollView extends ScrollView {

	private static final boolean DEBUG = false;
	private static final String TAG = "UserScrollView";
	
	private static final int SCOLL_INIT = 0;
	private static final int SCROLL_VIEWPAGER = 1;
	private static final int SCROLL_SCOLLVIEW = 2;
	private static final int SCROLL_NONE = 3;
	
	private static final float MAX_RATIO = 1.7f; // 60
	private static final float MIN_RATIO = 0.58f; // 30
	
	private int mTouchSlop;
	
	private int mDownX;
	private int mDownY;
	
	private UserScrollListener mListener;
	
	private int mMode;
	
	public UserScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init(context);
	}

	public UserScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init(context);
	}

	public UserScrollView(Context context) {
		super(context);
		
		init(context);
	}
	
	private void init(Context context) {
		ViewConfiguration configuration = ViewConfiguration.get(context);
		mTouchSlop = configuration.getScaledTouchSlop();
	}

	public void setUserScrollListener(UserScrollListener listener) {
		mListener = listener;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		boolean ret = false;
		
		switch (ev.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			mDownX = (int) ev.getX();
			mDownY = (int) ev.getY();
			
			mMode = SCOLL_INIT;
			break;
		case MotionEvent.ACTION_MOVE:
			final float xDiff = Math.abs(ev.getX() - mDownX);
			final float yDiff = Math.abs(ev.getY() - mDownY);
			final float mRatio = xDiff == 0 ? Short.MAX_VALUE : (yDiff / xDiff);
			
			if (yDiff > mTouchSlop && Math.abs(mRatio) > MAX_RATIO) {
				mMode = SCROLL_SCOLLVIEW;
			}
			else if (xDiff > mTouchSlop && Math.abs(mRatio) < MIN_RATIO) {
				mMode = SCROLL_VIEWPAGER;
			}
			else if (yDiff > mTouchSlop || xDiff > mTouchSlop) {
				mMode = SCROLL_NONE;
				ret = true;
			}
			break;
		}
		
		switch (mMode) {
		case SCOLL_INIT:
		case SCROLL_SCOLLVIEW:
			if (mListener != null) {
				ret = mListener.onInterceptTouchEvent(ev);
			}
			
			if (!ret) {
				try {
					ret = super.onInterceptTouchEvent(ev);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;
		}
		
		return ret;
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		boolean ret = false;
		
		if (mMode == SCROLL_NONE) {
			return true;
		}
		
		if (mListener != null) {
			ret = mListener.onTouchEvent(ev);
		}
		
		if (! ret) {
			try {
				ret = super.onTouchEvent(ev);
			} catch (Exception e) {
			}
		}
		
		if (DEBUG)
			Log.e(TAG, "onTouchEvent: " + ret + " sy: " + getScrollY() 
					+ " ev: " + ev.getAction()
					+ " x: " + ev.getX() + " y: " + ev.getY());
		
		
		return ret;
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		
		if (mListener != null) {
			mListener.onScrollChanged(this, l, t, oldl, oldt);
		}
	}
	
	public static interface UserScrollListener {
		
		public boolean onInterceptTouchEvent(MotionEvent ev);
		
		public boolean onTouchEvent(MotionEvent ev);
		
		public void onScrollChanged(ScrollView view, 
				int l, int t, int oldl, int oldt); 
	}
	
}
