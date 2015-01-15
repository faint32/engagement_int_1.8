package com.netease.android.util;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;

/**
 * 线程相关工具类
 * 
 * @author lvyusheng
 * 
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ThreadUtil {

	private static ExecutorService executorService = Executors
			.newFixedThreadPool(3);

	/**
	 * 提交一个任务，并等待结果返回
	 * 
	 * @param callable
	 * @return 有异常时返回null
	 */
	public static <T> T getResult(Callable<T> callable) {
		Future<T> future = executorService.submit(callable);
		T t = null;
		try {
			t = future.get();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return t;
	}

	/**
	 * 异步执行任务
	 * 
	 * @param runnable
	 */
	public static void execute(Runnable runnable) {
		executorService.execute(runnable);
	}

}
