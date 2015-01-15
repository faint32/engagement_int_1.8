package com.netease.engagement.widget;

import com.netease.service.Utils.EgmUtil;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * 仅仅起提供高度的作用
 */
public class GiftRecGridView extends GridView {
	
	private int mHeight = 160 ;

	public GiftRecGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public GiftRecGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public GiftRecGridView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context){
		mHeight = EgmUtil.dip2px(context, mHeight);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int specSize_Widht = MeasureSpec.getSize(widthMeasureSpec);
		setMeasuredDimension(specSize_Widht, mHeight);
	}
}
