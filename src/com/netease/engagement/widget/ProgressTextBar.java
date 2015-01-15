package com.netease.engagement.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.netease.date.R;

/**
 * 带文字的进度条
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class ProgressTextBar extends ProgressBar{
    private String mText = "";
    private Paint mPaint;
     
    public ProgressTextBar(Context context) {
        super(context);
        initText(); 
    }
     
    public ProgressTextBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initText();
    }
 
 
    public ProgressTextBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initText();
    }
     
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        Rect rect = new Rect();
        mPaint.getTextBounds(mText, 0, mText.length(), rect);
        int x = (getWidth() / 2) - rect.centerX();  
        int y = (getHeight() / 2) - rect.centerY();  
        canvas.drawText(mText, x, y, this.mPaint);  
    }
     
    //初始化，画笔
    private void initText(){
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(getContext().getResources().getDimensionPixelSize(R.dimen.text_size_17));
    }
     
    public void setText(String text){
        mText = text;
    }
}
