package com.netease.android.widget.ui.landscape;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.netease.date.R;

public class VerticalTextView extends TextView {
    boolean reverse = false;

    public VerticalTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.VerticalOrientation);
        if (ta.hasValue(R.styleable.VerticalOrientation_reverse)) {
            reverse = ta.getBoolean(R.styleable.VerticalOrientation_reverse, false);
        }
        ta.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        TextPaint paint = getPaint();
        paint.setColor(getCurrentTextColor());
        paint.drawableState = getDrawableState();
        canvas.save();
        if (reverse) {
            canvas.translate((getWidth() + getLayout().getLineTop(getLayout().getLineCount())) / 2, 0);
            canvas.rotate(90);
        } else {
            canvas.translate((getWidth() - getLayout().getLineTop(getLayout().getLineCount())) / 2, getHeight());
            canvas.rotate(-90);
        }

        getLayout().draw(canvas);
        canvas.restore();

    }


}
