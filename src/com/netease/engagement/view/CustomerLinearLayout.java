package com.netease.engagement.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class CustomerLinearLayout extends LinearLayout {

	public CustomerLinearLayout(Context context) {
		super(context);
	}
	
	public CustomerLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomerLinearLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}
	
	private IDispatchTouchEventListener dispatchTouchEventListener;
	
	public interface IDispatchTouchEventListener {
		public void dispatchTouchEvent(MotionEvent ev);
	}
	
	public void setDispatchTouchEventListener(
			IDispatchTouchEventListener dispatchTouchEventListener) {
		this.dispatchTouchEventListener = dispatchTouchEventListener;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (dispatchTouchEventListener != null) {
			dispatchTouchEventListener.dispatchTouchEvent(ev);
		}
		return super.dispatchTouchEvent(ev);
	}

}
