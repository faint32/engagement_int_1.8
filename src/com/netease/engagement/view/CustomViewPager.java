package com.netease.engagement.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Gallery;

/**
 * 自定义的ViewPager，可控制内嵌的水平滑动控件的滑动属性。
 * @author marui
 * @version 1.0
 */
public class CustomViewPager extends ViewPager {
    /** 内嵌ViewPager是否能够横向滑动 */
    private boolean bCanScrollInerViewPager = true;
    /** 内嵌Gallery是否能够横向滑动 */
    private boolean bCanScollInerGallery = true;
    /** ViewPager本身是否可以滑动 */
    private boolean mAllowedScrolling = true;
    
    public CustomViewPager(Context context) {
        this(context, null);
    }
    
    public CustomViewPager(Context context, AttributeSet attr) {
        super(context, attr);
    }
    
    public void setCanScrollInerViewPager(boolean b) {
        bCanScrollInerViewPager = b;
    }
    
    public void setCanScrollInerGallery(boolean b) {
        bCanScollInerGallery = b;
    }

    public void setAllowedScrolling(boolean b) {
        mAllowedScrolling = b;
    }
    
    /**
     * 重载canScroll是为了解决ViewPager内嵌横向滑动控件问题
     * 1 被内嵌的ViewPager能够横向滑动
     * 2 被内嵌的Gallery能够横向滑动
     */
    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if (v != this && v instanceof ViewPager && bCanScrollInerViewPager) {
            
            int cur = ((ViewPager) v).getCurrentItem();
            if (dx < 0) {
                /**
                 * 向右滑动，判断是否向右滑到最后一个pager
                 */
                int count = ((ViewPager) v).getAdapter().getCount();
                if (cur == (count - 1)) {
                    return super.canScroll(v, checkV, dx, x, y);
                }
            } else {
                /**
                 * 向左滑动，判断是否向左滑到最前一个pager
                 */
                if (0 == cur) {
                    return super.canScroll(v, checkV, dx, x, y);
                }
            }
            return true;
        } else if (v instanceof Gallery && bCanScollInerGallery) {
            return true;
        }
        
        return super.canScroll(v, checkV, dx, x, y);
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if(mAllowedScrolling || event.getAction() == MotionEvent.ACTION_DOWN){
            return super.onInterceptTouchEvent(event);
        }else{
            return false;
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        if(mAllowedScrolling){
            return super.onTouchEvent(arg0);
        }else{
            return false;
        }
    }
}
