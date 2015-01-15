package com.netease.framework.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class CircleProgressShaderView extends ImageView {
	
	private RectF mRectF; // 区域
	
	private int mProgressShaderColor; // 进度颜色
	
	private Bitmap mBitmap; // 原始bitmap
	
	private Paint mPaint;
	
	private Xfermode mXfermode;
	
	private PaintFlagsDrawFilter mDrawFilter;
	
	private int mProgress;
	
	private Bitmap mTmpBitmap;
	
	private Canvas mTmpCanvas;
	
	private Paint mTmpPaint; // 画笔
	
	private boolean mChanged;
	
	public CircleProgressShaderView(Context context) {
		super(context);
		
		init();
	}
	
	public CircleProgressShaderView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		init();
	}

	public CircleProgressShaderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init();
	}

	private void init() {
		mRectF = new RectF();
		mTmpPaint = new Paint();
		mPaint = new Paint();
		
		mXfermode = new PorterDuffXfermode(Mode.SRC_ATOP);
		mDrawFilter = new PaintFlagsDrawFilter(0, 
				Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
		
		mTmpPaint.setStyle(Paint.Style.FILL);
        mTmpPaint.setAntiAlias(true);
		
		if (android.os.Build.VERSION.SDK_INT >= 11) {
		     setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
	}

	public void setProgressShaderColor(int color) {
		if (mProgressShaderColor != color) {
			mChanged = true;
			mProgressShaderColor = color;
			mTmpPaint.setColor(color);
			invalidate();
		}
	}
	
	public void setProgress(int progress) {
		if (mProgress != progress) {
			mProgress = progress;
			mChanged = true;
			postInvalidateDelayed(20);
		}
	}
	
	public int getProgress() {
		return mProgress;
	}
	
	public void setBitmap(Bitmap bitmap) {
		if (bitmap == null) {
			return ;
		}
		
		if (mBitmap == null) {
			mBitmap = bitmap;
			requestLayout();
			
			updateCanvas(bitmap);
		}
		else if (mBitmap != bitmap) {
			mBitmap = bitmap;
			invalidate();
			
			updateCanvas(bitmap);
		}
	}
	
	private void updateCanvas(Bitmap bitmap) {
		mChanged = true;
		
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		
		mTmpBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		mTmpCanvas = new Canvas(mTmpBitmap);
		
		mRectF.left = - (width >> 1);
		mRectF.top = - (height >> 1);
		mRectF.right = width + (width >> 1);
		mRectF.bottom = height + (height >> 1);
	}
	
	private void redraw() {
		if (mTmpCanvas == null || ! mChanged) {
			return ;
		}
		mChanged = true;
		
		Canvas canvas = mTmpCanvas;
		
		canvas.clipRect(mRectF);
		
		mTmpPaint.setXfermode(null);
		if (mBitmap != null) {
			canvas.drawBitmap(mBitmap, 0, 0, mTmpPaint);
		}
		
		int progress = getProgress();
		
		if (progress >= 0 && progress < 100) {
			canvas.setDrawFilter(mDrawFilter);
			
	        mTmpPaint.setXfermode(mXfermode);
			
			canvas.drawArc(mRectF, -90, 360 * (progress - 100) / 100, true, mTmpPaint);
			canvas.drawCircle(getWidth() >> 1, getHeight() >> 1, (getWidth() * 3) >> 3, mTmpPaint);
		}
	}
	
	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		if (mBitmap != null) {
			setMeasuredDimension(mBitmap.getWidth(), mBitmap.getHeight());
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
//		super.onDraw(canvas);
		
		redraw();
		
		int saveCount = canvas.getSaveCount();
		canvas.save();
		
		if (mTmpBitmap != null) {
			canvas.drawBitmap(mTmpBitmap, 0, 0, mPaint);
		}
		
		canvas.restoreToCount(saveCount);
	}

	public void setBitmap(int id) {
		Resources res = getContext().getResources();
		BitmapDrawable drawable = (BitmapDrawable) res.getDrawable(id);
		
		setBitmap(drawable.getBitmap());
	}
}
