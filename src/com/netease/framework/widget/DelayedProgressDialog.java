package com.netease.framework.widget;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

/**
 * 延迟显示Dialog，如果任务执行速度快，则在完成是取消显示Dialog
 * @author gordondu
 * @see http://www.cis.gvsu.edu/~dulimarh/Okto/blog/android-progressdialog/
 * 
 */
public class DelayedProgressDialog extends ProgressDialog {
	
	private static Handler mHandler = new Handler(Looper.getMainLooper());
	
	private DelayedProgressDialog(Context context) {
		super(context);
	}
	
	public static ProgressDialog show(final Context context, final CharSequence title, final CharSequence msg, long delayMilliSec) {
		final DelayedProgressDialog dialog = new DelayedProgressDialog(context);
		dialog.setTitle(title);
		dialog.setMessage(msg);
		dialog.setCancelable(true);
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				try {
					dialog.show();
				} catch (Exception e) {}
			}
		}, delayMilliSec);
		return dialog;
	}
	
	@Override
	public void cancel() {
		mHandler.removeCallbacksAndMessages(null);
		super.cancel();
	}
	
	
	
}
