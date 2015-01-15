package com.handmark.pulltorefresh.library;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ListView;


public class AutoFillListLinearLayout extends LinearLayout {
    
    public static final int STATE_AUTO_FILL = 0;
    public static final int STATE_EMPTY = 1;
    public static final int STATE_WRAP = 2;
    
    private int mState = STATE_AUTO_FILL;

    
    public void setEmptyState(){
        mState = STATE_EMPTY;
    }
    
    public void setAutoFillState(){
        mState = STATE_AUTO_FILL;
    }
    
    public void setWrapState(){
        mState = STATE_WRAP;
    }
    
    public AutoFillListLinearLayout(Context context) {
        this(context, null);
    }

    public AutoFillListLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(mState == STATE_EMPTY){
            super.onMeasure(MeasureSpec.makeMeasureSpec(getResources().getDisplayMetrics().widthPixels, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY));
            return;
        }
        if (mState == STATE_WRAP || getParent() == null || !(getParent() instanceof ListView)) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        ListView list = (ListView) getParent();
        if (list.getMeasuredHeight() <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int listHeight = list.getMeasuredHeight();
        int childHeightTotal = 0;
        int headViewCount = list.getHeaderViewsCount();
        int firstVisiablePosition = list.getFirstVisiblePosition();
        int measuredHeight = 0;
        if(firstVisiablePosition > headViewCount){
            measuredHeight = 0;
        }else{
            int childStartPosition = 0;
//            if(false){
//                是否忽略headView高度
//                childStartPosition = headViewCount - firstVisiablePosition > 0 ? headViewCount - firstVisiablePosition : 0;//忽略HeadView的高度
//            }
            for(int i = childStartPosition; i < list.getChildCount() - 1; i++){
                childHeightTotal += list.getChildAt(i).getMeasuredHeight();
            }
            measuredHeight = listHeight - childHeightTotal > 0 ? listHeight - childHeightTotal : 0 - 5;
        }
        measuredHeight = getMeasuredHeight() > measuredHeight ? getMeasuredHeight() : measuredHeight;
//        Log.e("test", "onMeasure:" + measuredHeight);
        super.onMeasure(MeasureSpec.makeMeasureSpec(getResources().getDisplayMetrics().widthPixels, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY));
    }

}
