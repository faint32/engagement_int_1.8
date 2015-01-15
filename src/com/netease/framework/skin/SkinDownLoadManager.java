package com.netease.framework.skin;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;


public class SkinDownLoadManager implements IProcessListener {
	private static SkinDownLoadManager sSkinDownLoadManager = null;
	
	synchronized public static SkinDownLoadManager getInstance() {
		if (null == sSkinDownLoadManager) {
			sSkinDownLoadManager = new SkinDownLoadManager();
		}
		
		return sSkinDownLoadManager;
	}
	
	private UrlToPath mUrlToPath;
	private WeakHashMap<IProcessListener, Void> mGroupListener;
	
	private SkinDownLoadManager() {
		mUrlToPath = new UrlToPath();
		mGroupListener = new WeakHashMap<IProcessListener, Void>();
	}
	
	synchronized public void registerProcessListener(IProcessListener listener) {
		if (null != listener && null != mGroupListener) {
			mGroupListener.put(listener, null);
		}
	}
	
	synchronized public void unregisterProcessListener(IProcessListener listener) {
		if (null != listener && null != mGroupListener) {
			mGroupListener.remove(listener);
		}
	}

	public IPathConvert getIPathConvert() {
		return mUrlToPath;
	}
	
	/**
	 * 执行下载皮肤包
	 * @param url
	 * @param tag
	 */
	public int execute(String url, Object tag) {
		SkinDownloadTask task = new SkinDownloadTask(tag, mUrlToPath, this);
		task.execute(url);
		return task.getId();
	}
	
	@Override
	synchronized public void onProcess(Object tag, long size, long total) {
		if (null == mGroupListener) {
			return;
		}
		
		Set<IProcessListener> set = mGroupListener.keySet();
		List<IProcessListener> list = new LinkedList<IProcessListener>();
        list.addAll(set);
        
        if (list.size() > 0) {
        	for (IProcessListener listener : list) {
        		try {
        			listener.onProcess(tag, size, total);
        		} catch (Exception e) {
        			e.printStackTrace();
        		}
        	}
		}
        
        if (null != list) {
        	list.clear();
        }
        list = null;
	}

	@Override
	synchronized public void onResult(Object tag, boolean success, int code, Object ret) {
		if (null == mGroupListener) {
			return;
		}
		
		Set<IProcessListener> set = mGroupListener.keySet();
		List<IProcessListener> list = new LinkedList<IProcessListener>();
        list.addAll(set);
        
        if (list.size() > 0) {
        	for (IProcessListener listener : list) {
        		try {
        			listener.onResult(tag, success, code, ret);
        		} catch (Exception e) {
        			e.printStackTrace();
        		}
        	}
		}
        
        if (null != list) {
        	list.clear();
        }
        list = null;
	}
	
	public void destroy() {
		if (null != mGroupListener) {
			mGroupListener.clear();
		}
		mGroupListener = null;
		
		sSkinDownLoadManager = null;
	}
}
