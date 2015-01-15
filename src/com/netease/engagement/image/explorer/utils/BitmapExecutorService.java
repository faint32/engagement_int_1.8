package com.netease.engagement.image.explorer.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.util.Log;


public class BitmapExecutorService {
protected static int size = 3;
	
	private static ExecutorService instance;
	private static LoaderDeque loaderDeque;
	protected BitmapExecutorService() {
	}
	
	public static ExecutorService getInstance() {
		if(instance == null) {
			synchronized(BitmapExecutorService.class) {
				if(instance == null) {
					loaderDeque=new LoaderDeque();
					instance = new ThreadPoolExecutor(size, size, 60, TimeUnit.SECONDS, loaderDeque);
				}
			}
		}
		return instance;
	}
	
	private static class LoaderDeque extends LinkedBlockingDeque<Runnable> {

		private static final long serialVersionUID = 407869453026612142L;

		@Override
		public boolean offer(Runnable e) {
			return super.offerFirst(e);
		}

		@Override
		public Runnable remove() {
			return super.removeFirst();
		}

	}

	/**
	 * 退出图片上传界面时回收资源
	 */
	public static void closeService() {

		if (instance != null) {
			synchronized (BitmapExecutorService.class) {
				if (!instance.isShutdown()) {
					instance.shutdownNow();
					instance = null;
				}
				if (loaderDeque != null) {
					loaderDeque.clear();
					loaderDeque = null;
				}
				BitmapMemoryLruCache.closeBitmapMemCache();
				System.gc();
			}
		}
	}

}
