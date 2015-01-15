package com.netease.engagement.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

import com.netease.date.R;


public class EditTextWithClear extends EditText {

    private float lastPosX, lastPosY, desinty;
    private Drawable x;
    private Drawable xPrs;
    /** 是否带Clear */
    private boolean mIsWithClear = true;

    public EditTextWithClear(Context context) {
        this(context, null);
       
    }

    public EditTextWithClear(Context context, AttributeSet attrs) {
        super(context, attrs);
        desinty = getResources().getDisplayMetrics().density;
        x = context.getResources().getDrawable(R.drawable.text_del);
        xPrs = context.getResources().getDrawable(R.drawable.text_del);
        displayX(null);
    }
    
    /** 设置是否带clear */
    public void setWithClear(boolean is){
        mIsWithClear = is;
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (getText().length() > 0 && isFocused()) {
            displayX(false);
        } else {
            displayX(null);
        }
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused && getText().length() > 0) {
            displayX(false);
        } else {
            displayX(null);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    private boolean isInBound(float ex, float ey) {
        return ex >= getMeasuredWidth() - getPaddingRight() - x.getIntrinsicWidth() && ex <= getMeasuredWidth() - getPaddingRight() &&
                ey >= (getMeasuredHeight() - x.getIntrinsicHeight()) / 2 && ey <= (getMeasuredHeight() + x.getIntrinsicHeight()) / 2;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mIsWithClear){
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastPosX = event.getX();
                    lastPosY = event.getY();
                    if (isInBound(event.getX(), event.getY()) && getCompoundDrawables()[2] != null) {
                        displayX(true);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (Math.abs(lastPosX - event.getX()) <= 3 * desinty && Math.abs((lastPosY) - event.getY()) <= 3 * desinty) {
                        if (getCompoundDrawables()[2] == null) { // cross is not being shown so no need to handle
                            break;
                        }
                        if (isInBound(event.getX(), event.getY())) {
                            setText("");
                        }
                        break;
                    } else {
                        displayX(false);
                    }
                    break;
            }
        }
        
        return super.onTouchEvent(event);
    }

    private void displayX(Boolean isPressed) {
        if(mIsWithClear){
            if (isPressed == null) {
                setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            } else {
                if (getText().length() > 0) {
                    setCompoundDrawablesWithIntrinsicBounds(null, null, isPressed ? xPrs : x, null);
                }
            }
        }
        else{
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
    }

}
