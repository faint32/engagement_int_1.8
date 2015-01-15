package com.netease.engagement.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.app.EngagementApp;

public class ProgressTextView extends TextView{
    private static final float mDentisy = EngagementApp.getAppInstance().getResources().getDisplayMetrics().density;

	public ProgressTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public ProgressTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ProgressTextView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context){
		ViewCompat.setBackground(this,context.getResources().getDrawable(R.drawable.bg_level_marks_progress_bar));
		mPaint = new Paint();
		mPaint.setColor(context.getResources().getColor(R.color.pri_preview_txt_color));
		mPaint.setStyle(Style.FILL);
		
		mTxtPaint = new TextPaint();
		mTxtPaint.setColor(Color.WHITE);
		mTxtPaint.setTextSize(12*mDentisy);
		
		mRectTxt = new Rect();
		mRectProgress = new RectF();
		mAdded = new RectF();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mWidth = MeasureSpec.getSize(widthMeasureSpec);
		mHeight = MeasureSpec.getSize(heightMeasureSpec);
	}

	public void setProgress(long progress ,long total){
		this.progress = progress ;
		this.total = total ;
		mProgress = 1.0f * progress/total ;
		mWidth = this.getWidth() ;
		mHeight = this.getHeight() ;
		invalidate();
	}
	
	public void setMaxLevel(){
		this.mProgress = 1.0f ;
		invalidate();
	}
	
	private Paint mPaint ;
	private TextPaint mTxtPaint ;
	
	private float mProgress ;
	private long progress ;
	private long total;
	
    private int mWidth ;
    private int mHeight ;
    private float mProWidth ;
    
    private RectF mRectProgress ;
    private RectF mAdded ;
    
    private Rect mRectTxt ;
    private String mStr ;
    
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if(mProgress >= 1.0){
			mRectProgress.set(0,0,mWidth,mHeight);
			canvas.drawRoundRect(mRectProgress, 5, 5, mPaint);
			return ;
		}
		
		mProWidth = mProgress * mWidth ;
		mRectProgress.set(0, 0 , mProWidth , mHeight);
		mAdded.set( mProWidth /2 ,0 , mProWidth ,mHeight);
		
		canvas.drawRoundRect(mRectProgress, 5, 5, mPaint);
		canvas.drawRect(mAdded, mPaint);
		
		mStr = ""+progress+"/"+total ;
		mTxtPaint.getTextBounds(mStr,0,mStr.length(),mRectTxt);
		canvas.drawText(
				mStr,
				(mWidth - mRectTxt.width())/2, 
				(mHeight - mRectTxt.height())/2 + 10*mDentisy, 
				mTxtPaint);
	}
}
