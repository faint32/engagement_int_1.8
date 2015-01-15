package com.netease.engagement.image;

import android.content.Context;
import android.graphics.PointF;
import android.os.Handler;
import android.util.FloatMath;
import android.view.MotionEvent;


public class MultiTouchController {
     private static final int NONE_MODE = 0;  
     private static final int DRAG_MODE = 1;  
     private static final int ZOOM_MODE = 2;  
     private int mMode = NONE_MODE;  

    private PointF start = new PointF();  
    private PointF mid = new PointF();  
    private float mLastDist = 1.0f;
    private float mLastScale = 0f;
	private ImageViewTouch mImageView;
	private Handler mHandler;
	private OnZoomCallback mCb;
	
	public interface OnZoomCallback{
		public void OnZoom();
	}
	
	public MultiTouchController(Context context, ImageViewTouch imageview) {
		mImageView = imageview;
		mHandler = new Handler();
	}
	
	public void setOnZoomCallback(OnZoomCallback cb) {
		mCb = cb;
	}
	
	public boolean onMultiTouchEvent(MotionEvent rawEvent) {
		//Log.v("onMultiTouchEvent", "evnet" + (event.getAction() & MotionEvent.ACTION_MASK));
		MotionEventWrapper event = MotionEventWrapper.create(rawEvent);
		
		int action = event.getAction();
		if (action == MotionEvent.ACTION_DOWN) {
			doActionDown(event);
			
		} else if (action == MotionEventWrapper.ACTION_POINTER_DOWN) {
			doActionPointerDown(event);
			
		} else if (action == MotionEvent.ACTION_UP 
				|| action == MotionEventWrapper.ACTION_POINTER_UP 
				|| action == MotionEvent.ACTION_CANCEL) {
			mMode = NONE_MODE;
			
		} else if (action == MotionEvent.ACTION_MOVE) {
			doActionMove(event);
		} 
		return true;
	}

	private void doActionDown(MotionEventWrapper event) {
		start.set(event.getX(), event.getY());
		mMode = DRAG_MODE;
		mLastScale = 0f;
	}
	
	private void doActionPointerDown(MotionEventWrapper event) {
		//Enter into ZOOM_MODE
		mLastDist = distance(event);
		if (mLastDist > 10f) {
			// savedMatrix.set(matrix);
			midPoint(mid, event);
			mMode = ZOOM_MODE;
		}
	}
	
	private void doActionMove(MotionEventWrapper event){
		if (mMode == DRAG_MODE) {
			mImageView.postTranslateCenter(event.getX() - start.x,
					event.getY() - start.y);
			start.set(event.getX(), event.getY());
		} else if (mMode == ZOOM_MODE) {
			//if has ACTION_POINTER_DOWN EVENT , then enter into ZOOM_MODE
			float newDist = distance(event);
			if (newDist > 10f) {
				final float scale = newDist / mLastDist;
				mLastScale = scale;
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							mImageView.zoomToCenter(scale * mImageView.getScale(), mid.x, mid.y);
							if (mCb != null) {
								mCb.OnZoom();
							}
						}
					});
			}
			mLastDist = newDist;
		}
	}
	
	private float distance(MotionEventWrapper event) {  
	        float x = event.getX(0) - event.getX(1);  
	        float y = event.getY(0) - event.getY(1);  
	        return FloatMath.sqrt(x * x + y * y);  
	}  
	
	
	private void midPoint(PointF point, MotionEventWrapper event) {  
	        float x = event.getX(0) + event.getX(1);  
	        float y = event.getY(0) + event.getY(1);  
	        point.set(x / 2, y / 2);  
	}  
	
	public void clear() {
		mImageView = null;
		mHandler = null;
	}
}
