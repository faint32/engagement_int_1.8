package com.netease.engagement.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * 送礼物ViewPager
 */
public class GiftViewPager extends ViewPager{

	public GiftViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GiftViewPager(Context context) {
		super(context);
	}

	@Override
	protected boolean canScroll(View arg0, boolean arg1, int arg2, int arg3,
			int arg4) {
		int curPage = this.getCurrentItem();
		int count = this.getAdapter().getCount();
		if(curPage == count - 1 && arg2 < 0 && count == 3){
			if(mListener != null){
				mListener.onLastLeftToRight();
			}
		}
		if(curPage == 0 && arg2 > 0 && count == 1){
			if(mListener != null){
				mListener.onLastRightToLeft();
			}
		}
		return super.canScroll(arg0, arg1, arg2, arg3, arg4);
	}
	
	public OnScrollListener mListener ;
	public interface OnScrollListener{
		public void onLastLeftToRight();
		public void onLastRightToLeft();
	}
	public void setOnScrollListener(OnScrollListener listener){
		mListener = listener ;
	}
}
