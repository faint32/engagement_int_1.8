package com.netease.engagement.view;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Tab导航栏的封装。
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class AbsTabNavigationBar extends LinearLayout{
    protected Context mContext;
    
    protected ITabSelectedListener mTabSelectedListener;
    protected ITabReselectedListener mTabReselectedListener;
    protected ITabDoubleTapListener mTabDoubleTapListener;
    
    protected ArrayList<View> mTabViews = new ArrayList<View>();
    protected int mCurrentIndex = 0;

    public AbsTabNavigationBar(Context context) {
        this(context, null);
        setOrientation(HORIZONTAL);
        mContext = context;
    }

    public AbsTabNavigationBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        mContext = context;
    }
    
    public AbsTabNavigationBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOrientation(HORIZONTAL);
        mContext = context;
    }

    public void setTabSelectedListener(ITabSelectedListener l){
        mTabSelectedListener = l;
    }
    
    public void setTabReselectedListener(ITabReselectedListener l){
        mTabReselectedListener = l;
    }
    
    public void setTabDoubleTapListener(ITabDoubleTapListener l) {
    	mTabDoubleTapListener = l;
    }
    
    public int getCurrentIndex(){
        return mCurrentIndex;
    }
    
    /**
     * 指定当前选中哪个Tab。<br>
     * <b>注意1：该方法必须在完全初始化完成后再调用，否则可能部分无效。<br>
     * <b>注意2：该方法不判断指定的index是否是当前的，都认为不是当前的，都是调用onTabSelected()接口。
     * @param index 指定tab的index
     */
    public void setCurrentTab(int index){
        // 注意，不要判断index == mCurrentIndex了，因为初始时两者可能相同，但是需要强制执行一遍以下的逻辑
        View tabView = getTabView(index);
        
        if(tabView != null){
            // 原先的失去焦点，当前的获得焦点
            View formerTabView = mTabViews.get(mCurrentIndex);
            formerTabView.setSelected(false);
            tabView.setSelected(true);
            
            if(mTabSelectedListener != null && mCurrentIndex != index){
                mTabSelectedListener.onTabSelected(index, tabView);
            }
            mCurrentIndex = index;
        }
    }
    
    /** 获取index指定的tabview，index非法则返回null */
    protected View getTabView(int index){
        if(mTabViews == null || index < 0 || index >= mTabViews.size()){  // 未初始化、越界
            return null;
        }
        
        return mTabViews.get(index);
    }
    
    // ===============================interface=======================================
    /** Tab导航栏的Tab当前处于非被选中状态时被选中的监听 */
    public interface ITabSelectedListener{
        /**
         * Tab当前处于非被选中状态时被选中。
         * @param index tab的index
         * @param tabView
         */
        public void onTabSelected(int index, View tabView);
    };
    
    /** Tab导航栏的Tab当前处于选中状态时又被选中的监听 */
    public interface ITabReselectedListener{
        /**
         * Tab当前处于选中状态时又被选中
         * @param index ab的index
         * @param tabView
         */
        public void onTabReselected(int index, View tabView);
    }
    
    public interface ITabDoubleTapListener {
    	public void onTabDoubleTap(int index);
    }
}
