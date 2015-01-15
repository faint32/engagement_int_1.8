package com.netease.engagement.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * 自定义的ViewPager，layout_height支持wrap_content，以第一页的高度为准
 * 
 * @author gordondu
 * @version 1.0
 */
public class WrapContentViewPager extends ViewPager {

	public WrapContentViewPager(Context context) {
		super(context);
	}

	public WrapContentViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		View view = null;
		if (this.getChildCount() > 0) {
			view = this.getChildAt(0);
			if (view != null) {
				view.measure(widthMeasureSpec, heightMeasureSpec);
			}
		}
		int height = this.getPaddingTop() + this.getPaddingBottom();
		if (view != null) {
			height += measureHeight(heightMeasureSpec, view);
		}
		
		setMeasuredDimension(getMeasuredWidth(), height);
	}
	
	private int measureHeight(int measureSpec, View view) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		switch (specMode) {
			case MeasureSpec.UNSPECIFIED:
				if (view != null) {
					result = view.getMeasuredHeight();
				}
				break;
			case MeasureSpec.AT_MOST:
				if (view != null) {
					result = view.getMeasuredHeight();
				}
				result = Math.min(result, specSize);
				break;
			case MeasureSpec.EXACTLY:
				result = specSize;
				break;
		}
		return result;
	}
	
}
