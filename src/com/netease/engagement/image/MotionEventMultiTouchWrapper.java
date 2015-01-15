package com.netease.engagement.image;

import android.view.MotionEvent;

public class MotionEventMultiTouchWrapper extends MotionEventWrapper {

	public MotionEventMultiTouchWrapper(MotionEvent event) {
		super(event);
	}

	public int getAction(){
		return event.getAction() & MotionEvent.ACTION_MASK;
	}
	
	public int getPointerCount() {
		return event.getPointerCount();
	}
	
	public float getX(int pos){
		return event.getX(pos); 
	}
	
	public float getY(int pos){
		return event.getY(pos);
	}	
}
