package com.netease.engagement.widget;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.netease.date.R;

/**
 * 带有这样效果的纵向ScrollView：滑动时第一个完整可见的child放大，滑出屏幕的child缩小。用于排行榜列表的滑动。
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class EgmScrollView extends ScrollView {
    private int HEIGHT_MAX, HEIGHT_MIN;
    private float SIZE_MAX, SIZE_MIN, SIZE_DELTA;
    private float SIZE_MAX_SUB, SIZE_MIN_SUB;
    
    private ViewGroup mContainer;
    private int mScallTextViewId, mScallSubTextViewId;
    private boolean mIsSetLastChild = false;
    
    public EgmScrollView(Context context) {
        super(context);
    }
    
    public EgmScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EgmScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public void setScallTextViewId(int id, int subId){
        mScallTextViewId = id;
        mScallSubTextViewId = subId;
    }
    
    private void init(){
        mContainer = (ViewGroup)this.getChildAt(0);
        
        Resources resources = this.getContext().getResources();
        
        HEIGHT_MAX = resources.getDimensionPixelSize(R.dimen.rank_item_max_height);
        HEIGHT_MIN = resources.getDimensionPixelSize(R.dimen.rank_item_min_height);
        
        SIZE_MAX = resources.getDimensionPixelSize(R.dimen.rank_item_text_max_size);
        SIZE_MIN = resources.getDimensionPixelSize(R.dimen.rank_item_text_min_size);
        SIZE_DELTA = resources.getDimension(R.dimen.rank_item_text_size_delta);
        
        SIZE_MAX_SUB = resources.getDimensionPixelSize(R.dimen.rank_item_text_sub_max_size);
        SIZE_MIN_SUB = resources.getDimensionPixelSize(R.dimen.rank_item_text_sub_min_size);
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }
    
    @Override  
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {  
        super.onMeasure(widthMeasureSpec, heightMeasureSpec); 
        
        int height = this.getHeight();
        
        // 设置升级vip按钮区域的高度，使得列表拉到最上面的时候，保证只显示一个item的最大尺寸
        View lastChild = mContainer.getChildAt(mContainer.getChildCount() - 1);
        if(!mIsSetLastChild){
            int lastHeight = height - HEIGHT_MAX;
            if(lastHeight > 0){
                ViewGroup.LayoutParams lp = lastChild.getLayoutParams();
                lp.height = lastHeight;
                lastChild.setLayoutParams(lp);
                
                mIsSetLastChild = true;
            }
        }
    } 
    
    private float Y;
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch(action){
            case MotionEvent.ACTION_DOWN:
                Y = ev.getY();
                break;
                
            case MotionEvent.ACTION_MOVE:
                if(mIsClampedY){
                    int delta = (int)(ev.getY() - Y);
                    
                    boolean isUp = delta <= 0;
                    int index = getFirstAllVisiableChild();
                    if(delta < 0) delta = -delta;
                    scallChildren(index, isUp, delta);
                }
                break;
        }
        
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        
        int delta = oldt - t;
        boolean isUp = delta <= 0;
        
        int index = getFirstAllVisiableChild();
        if(delta < 0) delta = -delta;
        scallChildren(index, isUp, delta);
    }

    private boolean mIsClampedY = false;
    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        mIsClampedY = clampedY;
    }

    /**
     * 获取顶部第一个完整显示的View的index
     * @return
     */
    private int getFirstAllVisiableChild(){
        int index = 0;
        View child;
        
        
        int scrollY = this.getScrollY();
        
        int size = mContainer.getChildCount() - 1;
        for(int i = 0; i < size; i++){
            child = mContainer.getChildAt(i);
            int top = child.getTop();
            
            if(scrollY - top <= 0){ 
                index = i;
                break;
            }
        }
        
        return index;
    }
    
    /**
     * 滑动的时候缩放子View
     * @param index 顶部第一个完整显示的View的index
     * @param isUp true：向上滚动；false：向下滚动
     */
    private void scallChildren(int index, boolean isUp, int delta){
        int largeIndex, smallIndex;
        int size = mContainer.getChildCount() - 1;  // 最后一个是升级vip按钮，不用缩放
        float textSize = 0;
        float subTextSize = 0;
        
        if(isUp){   // 向上滑动
            largeIndex = index;
            smallIndex = index - 1;
        }
        else{
            if(index == 0){  // 第一个，已经不能再往下滑了，所以这一个用来放大，也就是把第一个拉到最大
                largeIndex = index;
                smallIndex = index + 1;
            }
            else{   
                largeIndex = index - 1;
                smallIndex = index;
            }
        }   
        
        for(int i = 0; i < size; i++){
            View child = mContainer.getChildAt(i);
            int currentHeight = child.getHeight();
            
            TextView textView = (TextView)child.findViewById(mScallTextViewId);
            if(textView != null){
                textSize = textView.getTextSize();
            }
            
            TextView subTextView = (TextView)child.findViewById(mScallSubTextViewId);
            if(subTextView != null){
                subTextSize = subTextView.getTextSize();
            }
            
            if(i == largeIndex){
                if(currentHeight < HEIGHT_MAX){ // 放大高度
                    ViewGroup.LayoutParams lp = child.getLayoutParams();
                    lp.height = Math.min(currentHeight + delta, HEIGHT_MAX);
                    child.setLayoutParams(lp);
                }
                if(textView != null && textSize < SIZE_MAX){ // 放大文字
                    float newSize = textSize + SIZE_DELTA;
                    newSize = Math.min(newSize, SIZE_MAX);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
                }
                if(subTextView != null && subTextView.getVisibility() == View.VISIBLE && subTextSize < SIZE_MAX_SUB){// 放大文字
                    float newSize = subTextSize + SIZE_DELTA;
                    newSize = Math.min(newSize, SIZE_MAX_SUB);
                    subTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
                }
            }
            else if(i == smallIndex){
                if(currentHeight > HEIGHT_MIN){ // 缩小高度
                    ViewGroup.LayoutParams lp = child.getLayoutParams();
                    lp.height = Math.max(currentHeight - delta, HEIGHT_MIN);
                    child.setLayoutParams(lp);
                }
                if(textView != null && textSize > SIZE_MIN){// 缩小文字
                    float newSize = textSize - SIZE_DELTA;
                    newSize = Math.max(newSize, SIZE_MIN);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
                }
                if(subTextView != null && subTextView.getVisibility() == View.VISIBLE && subTextSize > SIZE_MIN_SUB){// 缩小文字
                    float newSize = subTextSize - SIZE_DELTA;
                    newSize = Math.max(newSize, SIZE_MIN_SUB);
                    subTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
                }
            }
            else{
                if(currentHeight != HEIGHT_MIN){ // 恢复高度
                    ViewGroup.LayoutParams lp = child.getLayoutParams();
                    lp.height = HEIGHT_MIN;
                    child.setLayoutParams(lp);
                }
                if(textView != null && textSize != SIZE_MIN){       // 恢复文字大小
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, SIZE_MIN);
                }
                if(subTextView != null && subTextView.getVisibility() == View.VISIBLE && subTextSize != SIZE_MIN_SUB){// 恢复文字大小
                    subTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, SIZE_MIN_SUB);
                }
            }
        }
    }
    
}
