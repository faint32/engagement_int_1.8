package com.netease.engagement.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.date.R;

/**
 * 封装主页Tab导航栏里的Tab
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class HomeTabView extends FrameLayout{
    private Context mContext;
    /** 该Tab在导航栏中的index */
    private int mIndex = 0;
    
    private ImageView mTabIcon;
    /** 上面图标下面文字标题 */
    private TextView mTabTitle;
    
    /** 右上角用作提醒的图标 带数字的大红点 */
    private FrameLayout mTipLayout ;
    private TextView mTipIcon;
    
    /** 右上角用作提醒的图标 不带数字的小红点 */
    private FrameLayout mNewFunctionLayout ;
    
    private GestureDetector mDetector;
    
    private ITabBarOnDoubleTapListener mListener;
    
    public HomeTabView(Context context) {
        super(context);
        mContext = context;
    }
    
    public HomeTabView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }
    
    public HomeTabView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    @Override
    protected void onFinishInflate(){
        super.onFinishInflate();
        
        mTabIcon = (ImageView)findViewById(R.id.home_tabview_icon);
        mTabTitle = (TextView)findViewById(R.id.home_tabview_title);
        mTipLayout = (FrameLayout)findViewById(R.id.unread_layout);
        mTipIcon = (TextView)findViewById(R.id.home_tabview_tip_icon);
        mNewFunctionLayout = (FrameLayout)findViewById(R.id.new_function_layout); 
        
        mDetector = new GestureDetector(mContext, new MySimpleGestureDetector());
        this.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				if(mDetector != null) {
					mDetector.onTouchEvent(event);
				}
				return false;
			}
		});
        
    }
    
    public void setIndex(int index){
        mIndex = index;
    }
    
    public int getIndex(){
        return mIndex;
    }
    
    public void setTitle(int titleResId){
        mTabTitle.setText(titleResId);
    }
    
    public void setTitle(String title){
        mTabTitle.setText(title);
    }
    
    public void setIcon(int iconResId){
        mTabIcon.setImageResource(iconResId);
    }
    
    public void setIcon(Drawable icon){
        mTabIcon.setImageDrawable(icon);
    }
    
    public void setTipLayoutVisibility(int visibility){
    	mTipLayout.setVisibility(visibility);
    }
    
    public void setNewFunctionLayoutVisibility(int visibility){
    	mNewFunctionLayout.setVisibility(visibility);
    }
    
    public void setTipCount(int count){
    	mTipLayout.setVisibility(View.VISIBLE);
        mTipIcon.setText(String.valueOf(count));
    }
    
    private class MySimpleGestureDetector extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if(mListener != null) {
				mListener.onTabBarDoubleTap(getIndex());
			}
			return super.onDoubleTap(e);
		}
    	
    }
    
    public interface ITabBarOnDoubleTapListener {
    	public void onTabBarDoubleTap(int index);
    }
    
    public void setITabBarOnDoubleTapListener(ITabBarOnDoubleTapListener l) {
    	mListener = l;
    }
}
