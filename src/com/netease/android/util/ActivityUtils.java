package com.netease.android.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.IBinder;
import android.text.Selection;
import android.text.Spannable;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ActivityUtils{
	

	/**
	 * 显示toast提示，多行文字会居中对齐
	 * @param context
	 * @param tip
	 * @param ok 提醒类型, true:对号，false:感叹号
	 */
	public static void showToastWithIcon(Context context, String text, boolean ok) {
		showToastWithIcon(context, text, ok, Toast.LENGTH_SHORT);
	}
	
	/**
	 * 显示toast提示，多行文字会居中对齐
	 * @param context
	 * @param tip
	 * @param ok 提醒类型, true:对号，false:感叹号
	 * @param durationType 持续时间  Toast.LENGTH_SHORT或者Toast.LENGTH_LONG
	 */
	public static void showToastWithIcon(Context context, String text, boolean ok, int durationType) {
		if(text == null || "".equals(text.trim())){
			return;
		}
		
		Toast.makeText(context, text, durationType).show();
	}
	
	/**
	 * 显示alertdialog
	 * 
	 * @param title
	 * @param message
	 */
	public static void showAlertDialog(Activity activity, String title, String message) {
		Dialog dialog = new AlertDialog.Builder(activity)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(title).setMessage(message)
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface arg0, int arg1) {

					}
				}).create();
		dialog.show();
	}

	/**
	 * 显示progressdialog
	 * 
	 * @param message
	 */
	public static void showProgressDialog(Activity activity, final ProgressDialog progressDialog, final String message) {
		showProgressDialog(activity, progressDialog, message,false);
	}
	
	
	/**
	 * 显示progressdialog
	 * 
	 * @param message
	 */
	public static void showProgressDialog(Activity activity, final ProgressDialog progressDialog, final String message,final boolean cancelable) {
		if(progressDialog == null || activity == null){
			return;
		}
		activity.runOnUiThread(new Runnable() {
			public void run() {
				
				Window window = progressDialog.getWindow();
				window.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.setIndeterminate(false);
				progressDialog.setCancelable(cancelable);
				progressDialog.setMessage(message);
				progressDialog.show();
			}
		});
	}

	/**
	 * 取消progressdialog
	 * 
	 * @param message
	 */
	public static void cancelProgressDialog(Activity activity, final ProgressDialog progressDialog) {
		if(progressDialog == null || activity == null){
			return;
		}
		activity.runOnUiThread(new Runnable() {
			public void run() {
				if (progressDialog != null)
					progressDialog.cancel();
			}
		});
	}
	
	public static void showDialog(Activity activity, final Dialog progressDialog){
		activity.runOnUiThread(new Runnable() {
			public void run() {
				if(progressDialog != null && !progressDialog.isShowing()){
					progressDialog.show();
				}
			}
		});
	}
	
	
	public static void cancelDialog(Activity activity, final Dialog progressDialog){
		activity.runOnUiThread(new Runnable() {
			public void run() {
				if(progressDialog != null && progressDialog.isShowing())
					progressDialog.cancel();
			}
		});
	}
	
	/**
	 * 判断EditText是否为空
	 * 
	 * @param editText
	 * @return true : false
	 */
	public static boolean isNotBlank(EditText editText) {
		return editText.getText() != null
				&& editText.getText().toString() != null
				&& editText.getText().toString().length() > 0;
	}
	
	/**
	 * 异步隐藏键盘
	 * @param activity
	 */
	public static void hideSoftInputFromWindow(final Activity activity){
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				syncHideSoftInputFromWindow(activity);
			}
		});
	}
	
	/**
	 * 同步隐藏键盘   在调用者的线程内执行
	 * @param activity
	 */
	public static void syncHideSoftInputFromWindow(final Activity activity){
		InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);  
		// 处理没有虚拟键盘，只有物理键盘会挂掉的问题
		View view = activity.getCurrentFocus();
		if(view == null){
			return;
		}
		IBinder ib = view.getWindowToken();
		if(ib != null){
			imm.hideSoftInputFromWindow(ib, 0);
		}
	}
	
	/**
	 * 隐藏键盘
	 * @param activity
	 */
	public static void showSoftInputFromWindow(final Activity activity){
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);  
				// 处理没有虚拟键盘，只有物理键盘会挂掉的问题
				View view = activity.getCurrentFocus();
				if(view == null){
					return;
				}
				IBinder ib = view.getWindowToken();
				if(ib != null){
					imm.showSoftInputFromInputMethod(ib, 0);
				}
			}
		});
	}
	

    /**
     * 文本输入框获取焦点并弹出软键盘， textView为自定义控件的子元素时，调用requestFocus不起作用，需要isCustomView设置为true
     * @param textView
     */
	public static void showSoftInput4EditText(final EditText editText) {
		editText.requestFocus();
		// 输入位置定位在最后
		CharSequence text = editText.getText();
		if (text instanceof Spannable) {
			Spannable spanText = (Spannable) text;
			Selection.setSelection(spanText, text.length());
		}
		InputMethodManager inputManager = (InputMethodManager) editText
				.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.showSoftInput(editText, 0);
	}
	
	public static int getSnapshotHeight(Activity activity){
		// 获取状态栏高度
//		Rect frame = new Rect();
//		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
//		int statusBarHeight = frame.top;
//		// 获取屏幕长和高
//
//		int height = activity.getWindowManager().getDefaultDisplay().getHeight();
//		return height - statusBarHeight;
		return activity.getWindowManager().getDefaultDisplay().getHeight();
	}
	
	public static int getSnapshotWidth(Activity activity){
		return activity.getWindowManager().getDefaultDisplay().getWidth();
	}
    
}
