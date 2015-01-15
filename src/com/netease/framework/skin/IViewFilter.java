package com.netease.framework.skin;

import android.util.AttributeSet;
import android.view.View;

public interface IViewFilter {
	
	// 是否对该view进行过滤，如果返回true代表已经过滤
	// 过滤器可以使用在对某类view做统一处理，比如统一换字体
	public boolean onFilter(String name, View v, AttributeSet attrs);
}
