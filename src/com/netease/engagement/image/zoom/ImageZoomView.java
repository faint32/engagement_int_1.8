package com.netease.engagement.image.zoom;

import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * <p>可进行缩放操作的图片显示控件。
 * <p>它以观察者模式，通过ZoomState来控制缩放状态。
 */
public class ImageZoomView extends ImageView implements Observer {

    private ZoomState mZoomState;
    private final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final Rect mRectSrc = new Rect();
    private final Rect mRectDst = new Rect();
    private AspectQuotient mAspectQuotient = new AspectQuotient();
    
   

    public ImageZoomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setZoomState(new ZoomState());
    }

    public void setZoomState(ZoomState state) {
        if (mZoomState != null) {
            mZoomState.deleteObserver(this);
        }

        mZoomState = state;
        if(mZoomState != null) {
            mZoomState.addObserver(this);
        }

        invalidate();
    }

    public ZoomState getZoomState() {
        return mZoomState;
    }


    @Override
	public void update(Observable observable, Object data) {
        if(isShown()) {
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
    	Bitmap bitmap = null;
    	Drawable drawable = getDrawable();
    	if(drawable !=null)
    		bitmap = ((BitmapDrawable) getDrawable()).getBitmap();
    	
        if (mZoomState != null) {
            if (bitmap != null && !bitmap.isRecycled()) {
            	/* 控件尺寸 */
                final int viewWidth = getWidth();
                final int viewHeight = getHeight();
                /* 图片尺寸 */
                final int bitmapWidth = bitmap.getWidth();
                final int bitmapHeight = bitmap.getHeight();

                final float panX = mZoomState.getPanX();
                final float panY = mZoomState.getPanY();
                
                final float zoomX = mZoomState.getZoomX(mAspectQuotient.get()) * viewWidth / bitmapWidth;
                final float zoomY = mZoomState.getZoomY(mAspectQuotient.get()) * viewHeight / bitmapHeight;

                mRectSrc.left = (int) (panX * bitmapWidth - viewWidth
                        / (zoomX * 2));
                mRectSrc.top = (int) (panY * bitmapHeight - viewHeight
                        / (zoomY * 2));
                mRectSrc.right = (int) (mRectSrc.left + viewWidth / zoomX);
                mRectSrc.bottom = (int) (mRectSrc.top + viewHeight / zoomY);
                mRectDst.left = getLeft();
                mRectDst.top = getTop();
                mRectDst.right = getRight();
                mRectDst.bottom = getBottom();

                if (mRectSrc.left < 0) {
                    mRectDst.left += -mRectSrc.left * zoomX;
                    mRectSrc.left = 0;
                }
                if (mRectSrc.right > bitmapWidth) {
                    mRectDst.right -= (mRectSrc.right - bitmapWidth) * zoomX;
                    mRectSrc.right = bitmapWidth;
                }
                if (mRectSrc.top < 0) {
                    mRectDst.top += -mRectSrc.top * zoomY;
                    mRectSrc.top = 0;
                }
                if (mRectSrc.bottom > bitmapHeight) {
                    mRectDst.bottom -= (mRectSrc.bottom - bitmapHeight) * zoomY;
                    mRectSrc.bottom = bitmapHeight;
                }

                canvas.drawBitmap(bitmap, mRectSrc, mRectDst, mPaint);
            }
        } else {
            super.draw(canvas);
        }
    }

    private void calculateAspectQuotient() {
    	Bitmap bitmap = null;
    	Drawable drawable = getDrawable();
    	if(drawable != null)
    		bitmap = ((BitmapDrawable) getDrawable()).getBitmap();
        if (bitmap != null) {
            mAspectQuotient.updateAspectQuotient(getWidth(), getHeight(), bitmap.getWidth(), bitmap.getHeight());
            mAspectQuotient.notifyObservers();
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        calculateAspectQuotient();
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        calculateAspectQuotient();
        
        if(mZoomState != null)
        {
        	mZoomState.doCallback();
        }
    }

    public AspectQuotient getAspectQuotient() {
        return mAspectQuotient;
    }
    
}
