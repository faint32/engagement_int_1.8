

package com.netease.framework.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;

import com.netease.framework.activity.ActivityBase;

/**
 * 系统dialog封装
 * --------------warning----------------
 * 使用中发现ActivityGroup层叠超过两层后(把Activity嵌入到别的activity中), 
 * 用当前Activity的context传给dialog, dialog会出错, 所以精力用此封装的dialog
 * 
 * @author Panjf
 * @date   2011-10-9
 */
public class DialogUtil {
	public static Context getValidParent(Context context){
		if(context instanceof ActivityBase){
			Activity newActivity = (Activity)context;
			Activity parent = newActivity;
			while(parent != null && parent.getParent() != null){
				parent = parent.getParent();
			}
			
			return parent;
		}
		
		return context;
	}
	public static Builder newAlertDiag(Context context){
		Context parent = getValidParent(context);
		
		return new AlertDialog.Builder(parent);
	}
	
	public static ProgressDialog newProgressDialog(Context context){
		Context parent = getValidParent(context);
		return new ProgressDialog(parent);
	}
	
	public static ProgressDialog showProgressDialog(Context context,
			CharSequence title, CharSequence message) {
		Context parent = getValidParent(context);
		return ProgressDialog.show(parent, title, message, false);
	}

	public static ProgressDialog showProgressDialog(Context context, CharSequence title,
			CharSequence message, boolean indeterminate) {
		Context parent = getValidParent(context);
		return ProgressDialog.show(parent, title, message, indeterminate,
				false, null);
	}

	public static ProgressDialog showProgressDialog(Context context, CharSequence title,
			CharSequence message, boolean indeterminate, boolean cancelable) {
		Context parent = getValidParent(context);
		return ProgressDialog.show(parent, title, message, indeterminate,
				cancelable, null);
	}

	public static ProgressDialog showProgressDialog(Context context, CharSequence title,
			CharSequence message, boolean indeterminate, boolean cancelable,
			OnCancelListener cancelListener) {
		Context parent = getValidParent(context);
		return ProgressDialog.show(parent, title, message, indeterminate,
				cancelable, cancelListener);
	}
	    
}
