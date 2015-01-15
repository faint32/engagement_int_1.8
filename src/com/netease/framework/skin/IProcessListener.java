package com.netease.framework.skin;

/**
 * 下载回调接口，可以得到进度信息和是否成功失败信息。
 *
 */
public interface IProcessListener {
	public void onProcess(Object tag, long size, long total);
	
	public void onResult(Object tag, boolean success, int code, Object ret);
}
