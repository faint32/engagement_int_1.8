package com.netease.engagement.view;

import com.netease.date.R;

import android.R.mipmap;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public class PagerIndicator extends AbsIndicator {

    private Drawable mIndBmp;
    private Drawable mHighlightBmp;
    private int mCount;

    public PagerIndicator(Context context) {
        this(context, null);
    }

    public PagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        mIndBmp = getContext().getResources().getDrawable(R.drawable.icon_page_control_dot_off);
        mHighlightBmp = getContext().getResources().getDrawable(R.drawable.icon_page_control_dot_on);
        mIndBmp.setBounds(0, 0, mIndBmp.getIntrinsicWidth(), mIndBmp.getIntrinsicHeight());
        mHighlightBmp.setBounds(0, 0, mHighlightBmp.getIntrinsicWidth(),
                mHighlightBmp.getIntrinsicHeight());
    }

    public void setCount(int count) {
        mCount = count;
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public Drawable getIndicator() {
        return mIndBmp;
    }

    @Override
    public Drawable getHighlight() {
        return mHighlightBmp;
    }

    public void clear() {
        mIndBmp = null;
        mHighlightBmp = null;
    }

    public void setHighlight(Drawable drawable) {
        mHighlightBmp = drawable;
        mHighlightBmp.setBounds(0, 0, mHighlightBmp.getIntrinsicWidth(),
                mHighlightBmp.getIntrinsicHeight());
        invalidate();
    }

    public void setIndicator(Drawable drawable) {
        mIndBmp = drawable;
        mIndBmp.setBounds(0, 0, mIndBmp.getIntrinsicWidth(), mIndBmp.getIntrinsicHeight());
        invalidate();
    }
}
