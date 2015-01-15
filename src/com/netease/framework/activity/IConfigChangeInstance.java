package com.netease.framework.activity;

/**
 * 保存数据接口，当Activity横竖屏变化时会调用到onSave
 * @author user
 *
 */
public interface IConfigChangeInstance {
	public void onSave();
}
