package com.netease.android.widget.ui.landscape;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.netease.date.R;

public class VerticalProgressBar extends ProgressBar {

    private int progress = 0;

    RectF rect;
    Paint paint;
    boolean reverse = false;

    public VerticalProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        rect = new RectF();
        paint = new Paint();

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.VerticalOrientation);
        if (ta.hasValue(R.styleable.VerticalOrientation_reverse)) {
            reverse = ta.getBoolean(R.styleable.VerticalOrientation_reverse, false);
        }
        ta.recycle();
    }

    public void setProgress(int progress) {
        if (progress > getMax() || progress < 0) {
            return;
        }
        this.progress = progress;
        this.invalidate();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (reverse) {
            rect.left = 0;
            rect.top = 0;
            rect.right = getWidth();
            rect.bottom = (float) (getHeight() * 1.0 * progress / getMax());

            paint.setColor(Color.rgb(0x55, 0x55, 0x55));
            paint.setStrokeWidth(getWidth());
            paint.setStyle(Paint.Style.STROKE);

            canvas.drawRect(rect, paint);

            paint.setColor(Color.rgb(0x1c, 0x1c, 0x1c));
            rect.left = 0;
            rect.top = (float) (getHeight() * 1.0 * progress / getMax());
            rect.right = getWidth();
            rect.bottom = getHeight();

            canvas.drawRect(rect, paint);
        } else {
            rect.left = 0;
            rect.top = getHeight() - (float) (getHeight() * 1.0 * progress / getMax());
            rect.right = getWidth();
            rect.bottom = getHeight();

            paint.setColor(Color.rgb(0x55, 0x55, 0x55));
            paint.setStrokeWidth(getWidth());
            paint.setStyle(Paint.Style.STROKE);

            canvas.drawRect(rect, paint);

            paint.setColor(Color.rgb(0x1c, 0x1c, 0x1c));
            rect.left = 0;
            rect.top = 0;
            rect.right = getWidth();
            rect.bottom = getHeight() - (float) (getHeight() * 1.0 * progress / getMax());

            canvas.drawRect(rect, paint);
        }

    }
}
