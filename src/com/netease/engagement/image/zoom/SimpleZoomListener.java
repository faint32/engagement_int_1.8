package com.netease.engagement.image.zoom;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;

public class SimpleZoomListener implements View.OnTouchListener {
	
	private boolean bTouchFinish = true;

    public enum ControlType {
        PAN, ZOOM
    }

    private ControlType mControlType = ControlType.ZOOM;

    private float mX;
    private float mY;

    /** Zoom control to manipulate */
    private BasicZoomControl mZoomControl;

    /** X-coordinate of latest down event */
    private float mDownX;

    /** Y-coordinate of latest down event */
    private float mDownY;
    private boolean isFinish;
    
    private Activity mActitity;

    public SimpleZoomListener(Activity activity){
    	mActitity = activity;
    }
    /**
     * Sets the zoom control to manipulate
     * 
     * @param control
     *            Zoom control
     */
    public void setZoomControl(BasicZoomControl control) {
        mZoomControl = control;
    }

    public void setControlType(ControlType controlType) {
        mControlType = controlType;
    }

    public ControlType getControlType() {
        return mControlType;
    }

    @Override
	public boolean onTouch(View v, MotionEvent event) {
        final int action = event.getAction();
        final float x = event.getX();
        final float y = event.getY();
        
        
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            mDownX = x;
            mDownY = y;
            mX = x;
            mY = y;
            isFinish = true;

        case MotionEvent.ACTION_MOVE: {
            final float dx = (x - mX) / v.getWidth();
            final float dy = (y - mY) / v.getHeight();

            if (mControlType == ControlType.ZOOM) {
                mZoomControl.zoom((float) Math.pow(20, -dy), mDownX
                        / v.getWidth(), mDownY / v.getHeight());
            } else {
            	 
                mZoomControl.pan(-dx, -dy);
            }

            mX = x;
            mY = y;
            if(Math.abs(mX - mDownX)>5 || Math.abs(mY - mDownY)>5)
            	isFinish = false;
//            PalLog.e("------", "move");
//            PalLog.e(mX - mDownX + "", mY - mDownY + "");
            break;
        }
        case MotionEvent.ACTION_UP:
        	if(isFinish && bTouchFinish){
        		mActitity.finish();
//        	PalLog.e("------", "up");
        	}
        	 break;
        }
        //返回false 被响应
        return true;
    }
    
    public void setTouchFinishEnable(boolean b){
    	bTouchFinish = b;
    }

}
