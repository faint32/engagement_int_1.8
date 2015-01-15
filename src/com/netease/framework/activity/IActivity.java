package com.netease.framework.activity;

/**
 * 定义项目中通用的activity接口
 * @author Panjf
 * @date   2011-10-9
 */
public interface IActivity {
	/**
	 * 要求刷新界面
	 */
	void refreshContent();
	/**
	 * 有数据传递过来
	 * @param obj
	 */
	void onActivityNotify(Object obj);
}
