package com.netease.engagement.widget;

import com.netease.date.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CircleProgress extends View {

	// 画实心圆的画笔
	private Paint mCirclePaint;
	// 画圆环的画笔
	private Paint mRingPaint;
	// 画字体的画笔
	private Paint mTextPaint;
	// 背景圆环颜色
	private int mBackCircleColor;
	// 前景圆环颜色
	private int mFrontRingColor;
	// 半径
	private float mRadius;
	// 圆环半径
	private float mRingRadius;
	// 圆环宽度
	private float mStrokeWidth;
	// 圆心x坐标
	private int mXCenter;
	// 圆心y坐标
	private int mYCenter;
	// 字的长度
	private float mTxtWidth;
	// 字的高度
	private float mTxtHeight;
	// 总进度
	private float mTotalProgress = 100.0f;
	// 当前进度
	private int mProgress;

	private float scale = getResources().getDisplayMetrics().density;

	public CircleProgress(Context context) {
		super(context);
		mRadius = 20;
//		mStrokeWidth = 8/2*(scale+0.5f);
//		mRingRadius = 80/2*(scale+0.5f);
		mBackCircleColor = Color.GRAY;
		mFrontRingColor = Color.WHITE;
		mStrokeWidth = context.getResources().getDimensionPixelSize(R.dimen.circle_progressbar_stroke_width);
		mRingRadius = context.getResources().getDimensionPixelSize(R.dimen.circle_progressbar_radius);
		init();
	}

	public CircleProgress(Context context, AttributeSet attrs) {
		super(context, attrs);
		mRadius = 20;
//		mStrokeWidth = 8/2*(scale+0.5f);
//		mRingRadius = 88/2*(scale+0.5f);
		mStrokeWidth = context.getResources().getDimensionPixelSize(R.dimen.circle_progressbar_stroke_width);
		mRingRadius = context.getResources().getDimensionPixelSize(R.dimen.circle_progressbar_radius);
		mBackCircleColor = Color.GRAY;
		mFrontRingColor = Color.WHITE;
		init();
	}

	private void init() {

		mRadius = mRingRadius - mStrokeWidth / 2;

		mCirclePaint = new Paint();
		mCirclePaint.setAntiAlias(true);
		mCirclePaint.setColor(mBackCircleColor);
		mCirclePaint.setStyle(Paint.Style.STROKE);
		mCirclePaint.setStrokeWidth(mStrokeWidth);

		mRingPaint = new Paint();
		mRingPaint.setAntiAlias(true);
		mRingPaint.setColor(mFrontRingColor);
		mRingPaint.setStyle(Paint.Style.STROKE);
		mRingPaint.setStrokeWidth(mStrokeWidth);

		mTextPaint = new Paint();
		mTextPaint.setAntiAlias(true);
		mTextPaint.setStyle(Paint.Style.FILL);
		mTextPaint.setARGB(0xFF, 0x1C, 0x86, 0xEE);
		mTextPaint.setTextSize(mRadius * 11 / 14);
		FontMetrics fm = mTextPaint.getFontMetrics();
		mTxtHeight = (int) Math.ceil(fm.descent - fm.ascent);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		mXCenter = getWidth() / 2;
		mYCenter = getHeight() / 2;

		if (mProgress >= 0) {
			RectF oval = new RectF();
			oval.left = (mXCenter - mRingRadius);
			oval.top = (mYCenter - mRingRadius);
			oval.right = mRingRadius + mXCenter;
			oval.bottom = mRingRadius + mYCenter;
			canvas.drawArc(oval, -90, 360, false, mCirclePaint);
			canvas.drawArc(oval, -90, ((float) mProgress / mTotalProgress) * 360, false, mRingPaint);

			String txt = mProgress + "";
			mTxtWidth = mTextPaint.measureText(txt, 0, txt.length());
//			canvas.drawText(txt, mXCenter - mTxtWidth / 2, mYCenter + mTxtHeight / 4, mTextPaint);
		}
	}

	public void setProgress(int progress) {
		mProgress = progress;
		postInvalidate();
	}

	public int getProgress() {
		return mProgress;
	}

	public void setRadius(int r) {
		mRingRadius = r;
		init();
		postInvalidate();
	}

	public void setCircleColor(int resId) {
		mCirclePaint.setColor(getContext().getResources().getColor(resId));
		postInvalidate();
	}

	public void setRingColor(int resId) {
		mRingPaint.setColor(getContext().getResources().getColor(resId));
		postInvalidate();
	}

	public float getmRingRadius() {
		return mRingRadius;
	}

	public void setmRingRadius(float mRingRadius) {
		this.mRingRadius = mRingRadius*(scale+0.5f);
	}

	public float getmStrokeWidth() {
		return mStrokeWidth;
	}

	public void setmStrokeWidth(float mStrokeWidth) {
		this.mStrokeWidth = mStrokeWidth*(scale+0.5f);
	}
}
