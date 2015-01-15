package com.netease.android.widget.ui.landscape;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.netease.date.R;

public class VerticalImageView extends ImageView {
    boolean reverse = false;

    public VerticalImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.VerticalOrientation);
        if (ta.hasValue(R.styleable.VerticalOrientation_reverse)) {
            reverse = ta.getBoolean(R.styleable.VerticalOrientation_reverse, false);
        }
        ta.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (reverse) {
            canvas.translate(getWidth(), 0);
            canvas.rotate(90);
        } else {
            canvas.translate(0, getHeight());
            canvas.rotate(-90);
        }

        super.onDraw(canvas);
    }
}
