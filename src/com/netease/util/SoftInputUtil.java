package com.netease.util;

import android.os.Handler;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class SoftInputUtil {

	/**
	 * 延迟显示输入法
	 * 
	 * @param manager
	 * @param edittext
	 */
	public static void showInputDelayed(final InputMethodManager manager, 
			final EditText edittext) {
		if (manager == null || edittext == null) {
			return ;
		}
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				manager.showSoftInput(edittext, 0);
			}
		}, 300);
	}
	
}
