package com.netease.engagement.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.netease.date.R;

/**
 * 主页上的Tab导航栏
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class HomeTabNavigationBar extends AbsTabNavigationBar {

    public HomeTabNavigationBar(Context context) {
        super(context);
        this.setBackgroundResource(R.drawable.home_tab_bar_bg);
    }
    
    public HomeTabNavigationBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setBackgroundResource(R.drawable.home_tab_bar_bg);
    }
    
    public HomeTabNavigationBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setBackgroundResource(R.drawable.home_tab_bar_bg);
    }
    
    /**
     * 添加一个Tab View
     * @param titleResId 标题文字
     * @param iconResId 图标
     */
    public HomeTabView addTab(int index, int titleResId, int iconResId){
        LayoutInflater inflater = LayoutInflater.from(mContext);
        
        HomeTabView tabView = (HomeTabView)inflater.inflate(R.layout.view_home_tab, this, false);
        tabView.setIndex(index);
        tabView.setIcon(iconResId);
        tabView.setTitle(titleResId);
        tabView.setFocusable(true);
        tabView.setOnClickListener(mTabClickListener);
        
        tabView.setITabBarOnDoubleTapListener(mDoubleTapListener);
        
        mTabViews.add(tabView);
        
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.weight = 1;
        
        addView(tabView, lp);
        
        return tabView;
    }
    
    public void addShortcutSwitcher(View view){
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp.weight = 1;
        lp.gravity = Gravity.CENTER;
        addView(view, lp);
    }
    
    /** Tab点击监听 */
    private final View.OnClickListener mTabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            HomeTabView tabView = (HomeTabView)v;
            
            int index = tabView.getIndex();
            if(mCurrentIndex == index){ // 选中当前的
                if(mTabReselectedListener != null){
                    mTabReselectedListener.onTabReselected(index, v);
                }
            }
            else{
                // 原先的失去焦点，当前的获得焦点
                View formerTabView = mTabViews.get(mCurrentIndex);
                formerTabView.setSelected(false);
                tabView.setSelected(true);
                
                mCurrentIndex = index;
                
                if(mTabSelectedListener != null){
                    mTabSelectedListener.onTabSelected(index, v);
                }
            }
            
        }
    };
    
    private final HomeTabView.ITabBarOnDoubleTapListener mDoubleTapListener = new HomeTabView.ITabBarOnDoubleTapListener() {
		
		@Override
		public void onTabBarDoubleTap(int index) {
			if(mTabClickListener != null) {
				mTabDoubleTapListener.onTabDoubleTap(index);
			}
		}
	};

}
