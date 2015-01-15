package com.netease.engagement.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public abstract class AbsIndicator extends View {

    private Context mContext;
    private int mCurrentItem = -1;

    private int mGapWidth;
    private int mWidth;
    private int mHeight;
    private int mCellWidth;
    private int mCellHeight;

    public AbsIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        setGap(10);
    }

    public AbsIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AbsIndicator(Context context) {
        this(context, null);
    }

    private void init() {
        mCellWidth = getIndicator().getIntrinsicWidth() > getHighlight().getIntrinsicWidth() ? getIndicator()
                .getIntrinsicWidth() : getHighlight().getIntrinsicWidth();
        mCellHeight = getIndicator().getIntrinsicHeight() > getHighlight().getIntrinsicHeight() ? getIndicator()
                .getIntrinsicHeight() : getHighlight().getIntrinsicHeight();
        mWidth = mCellWidth * getCount() + mGapWidth * (getCount() - 1) + getPaddingLeft()
                + getPaddingRight();
        mHeight = mCellHeight + getPaddingBottom() + getPaddingTop();
    }

    public abstract int getCount();

    public abstract Drawable getIndicator();

    public abstract Drawable getHighlight();

    public int getCurrentItem() {
        return mCurrentItem;
    }

    /**
     * 设置指示器之间的距离，单位dp
     *
     * @param gap
     */
    public void setGap(int gap) {
        mGapWidth = (int)(gap * mContext.getResources().getDisplayMetrics().density);
    }

    public void setCurrentItem(int currentItem) {
        if (currentItem == mCurrentItem)
            return;
        if (currentItem < 0)
            currentItem = 0;
        else if (currentItem >= getCount())
            currentItem = getCount() - 1;

        mCurrentItem = currentItem;
        if (mCurrentItem >= 0)
            invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        init();
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());
        for (int i = 0; i < getCount(); i++) {
            drawCell(canvas, getIndicator());
            if (i == mCurrentItem) {
                drawCell(canvas, getHighlight());
            }
            canvas.translate(mCellWidth + mGapWidth, 0);
        }
        canvas.restore();
    };

    private void drawCell(Canvas canvas, Drawable cell) {
        canvas.save();
        int xRevise = (mCellWidth - cell.getIntrinsicWidth()) / 2;
        int yRevise = (mCellHeight - cell.getIntrinsicHeight()) / 2;
        canvas.translate(xRevise, yRevise);
        cell.draw(canvas);
        canvas.restore();
    }

}
