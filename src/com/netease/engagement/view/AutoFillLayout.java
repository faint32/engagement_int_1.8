package com.netease.engagement.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public abstract class AutoFillLayout extends ViewGroup{
	
	private int mColumns;
	
    public void setColums(int columns){
        mColumns = columns;
    }

    public AutoFillLayout(Context context) {
        super(context);
    }

    public AutoFillLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoFillLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int specSize_Widht = MeasureSpec.getSize(widthMeasureSpec) ;
        
        int rows = getChildCount() % mColumns == 0 ? getChildCount()/mColumns : getChildCount()/mColumns +1;
        int height = getItemHeight()*rows ;
        
        setMeasuredDimension(specSize_Widht , height) ;
        
        for(int i=0 ; i < getChildCount() ; i++){
            measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec) ;
        }
    }
    
    public abstract int getItemHeight();

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(getChildCount() == 0)
            return;
        View child = getChildAt(0);
        int itemW = child.getMeasuredWidth();
        int itemH = child.getMeasuredHeight();
        
        int row, column;
        for(int i = 0; i < getChildCount(); i++){
            row = i / mColumns;
            column = i % mColumns;
            getChildAt(i).layout(
                    column * itemW,
                    row * itemH,
                    itemW + column * itemW,
                    itemH + row * itemH);
        }
    }
}
