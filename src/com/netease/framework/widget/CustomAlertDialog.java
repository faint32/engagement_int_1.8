package com.netease.framework.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;

public class CustomAlertDialog implements DialogInterface.OnClickListener {

	public static interface DialogListener {
		public void onClick(DialogInterface DialogListn,
				int which, int key, View view);
	}
	
	DialogListener mDialogListner;
	int mKey;
	View mView;
	
	public CustomAlertDialog(int key, DialogListener dialogListener) {
		mKey = key;
		mDialogListner = dialogListener;
	}
	
	private static AlertDialog.Builder builder(Context context, String title, String message) {
		AlertDialog.Builder builder = null;
		builder = DialogUtil.newAlertDiag(context);
		
		if (null != title) {
			builder.setTitle(title);
		}
		
		if (null != message) {
			builder.setMessage(message);
		}
		return builder;
	}
	
	private static AlertDialog.Builder builder(Context context,
			String title, String message, int icon) {
		AlertDialog.Builder builder = builder(context, title, message);
		if (icon > 0) {
		    builder.setIcon(icon);
		}
        return builder;
	}
	
	private static AlertDialog.Builder builder(Context context,
            String title, int message, int icon, int key, View view,
            int positive, int nagetive, DialogListener listener) {
	    return builder(context, title, context.getResources().getString(message), icon, key, view, positive, nagetive, listener);
	}
	private static AlertDialog.Builder builder(Context context,
            String title, int message, int icon, int key, View view,
            String positive, int nagetive, DialogListener listener) {
        return builder(context, title, context.getResources().getString(message), icon, key, view, positive, nagetive, listener);
    }
	
	private static AlertDialog.Builder builder(Context context,
			String title, String message, int icon, int key, View view,
			int positive, int nagetive, DialogListener listener) {
		AlertDialog.Builder builder = builder(context,
				title, message, icon);
		
		CustomAlertDialog dialog = null;
		if (null != listener) {
			dialog = new CustomAlertDialog(key, listener);
		}
		
		if (null != view) {
			if (null != dialog) {
				dialog.mView = view;
			}
			builder.setView(view);
		}
		if (positive > 0 ) {
		    builder.setPositiveButton(positive, dialog);
		}
		
		if (nagetive > 0) {
		    builder.setNegativeButton(nagetive, dialog);
		}
		
        return builder;
	}
	private static AlertDialog.Builder builder(Context context,
            String title, String message, int icon, int key, View view,
            String positive, int nagetive, DialogListener listener) {
        AlertDialog.Builder builder = builder(context,
                title, message, icon);
        
        CustomAlertDialog dialog = null;
        if (null != listener) {
            dialog = new CustomAlertDialog(key, listener);
        }
        
        if (null != view) {
            if (null != dialog) {
                dialog.mView = view;
            }
            builder.setView(view);
        }
        if (!TextUtils.isEmpty(positive)) {
            builder.setPositiveButton(positive, dialog);
        }
        
        if (nagetive > 0) {
            builder.setNegativeButton(nagetive, dialog);
        }
        
        return builder;
    }
	
	private static AlertDialog.Builder builder(Context context,
			int title, int message) {
		AlertDialog.Builder builder = null;
//        builder = new AlertDialog.Builder(context);
		builder = DialogUtil.newAlertDiag(context);
        if (title > 0 ) {
        	builder.setTitle(title);
        }
        
        if (message > 0 ) {
        	builder.setMessage(message);
        }
        
        return builder;
	}
	
	private static AlertDialog.Builder builder(Context context,
			int title, int message, int icon) {
		AlertDialog.Builder builder = builder(context, title, message);
		if (icon > 0) {
		    builder.setIcon(icon);
		}
        return builder;
	}
	
	private static AlertDialog.Builder builder(Context context,
			int title, int message, int icon, int key, View view,
			int positive, int nagetive, DialogListener listener) {
		AlertDialog.Builder builder = builder(context,
				title, message, icon);
		
		CustomAlertDialog dialog = null;
		if (null != listener) {
			dialog = new CustomAlertDialog(key, listener);
		}
		
		if (null != view) {
			if (null != dialog) {
				dialog.mView = view;
			}
			builder.setView(view);
		}
		if (positive > 0 ) {
		    builder.setPositiveButton(positive, dialog);
		}
		
		if (nagetive > 0) {
		    builder.setNegativeButton(nagetive, dialog);
		}
		
        return builder;
	}
    
    public static void showAlertDialog(Context context, String title, String message, int icon, String positive) {
        builder(context, title, message, icon, -1, null, positive, -1, null).show();
    }
	
	public static void showAlertDialog(Context context, String title, String message, int icon, int positive) {
		builder(context, title, message, icon, -1, null, positive, -1, null).show();
	}
	
	public static void showAlertDialog(Context context, int title, int message) {
		builder(context, title, message, -1, -1, null, -1, -1, null).show();
	}
	
	public static void showAlertDialog(Context context, int title, int message, int icon) {
		builder(context, title, message, icon, -1, null, -1, -1, null).show();
	}
	
	public static void showAlertDialog(Context context, int title, int message, int icon, int positive) {
		builder(context, title, message, icon, -1, null, positive, -1, null).show();
	}
	
	public static void showAlertDialog(Context context, int title, int message, int icon, int positive, int nagetive) {
		builder(context, title, message, icon, -1, null, positive, nagetive, null).show();
	}
	
	public static void showAlertDialog(Context context, int message, int positive, DialogListener listener) {
		builder(context, -1, message, -1, -1, null, positive, -1, listener).show();
	}
	
	public static void showAlertDialog(Context context, int key,
			int title, int message, int icon, int positive,
			DialogListener listener) {
		builder(context, title, message, icon, key, null, positive, -1, listener).show();
	}
	
	public static AlertDialog showAlertDialog(Context context, int key,
			int title, int message, int icon, int positive, int nagetive,
			DialogListener listener) {
		return builder(context, title, message, icon, key, null, positive, nagetive, listener).show();
	}
	public static AlertDialog showAlertDialog(Context context, int key,
	        String title, int message, int icon, int positive, int nagetive,
            DialogListener listener) {
        return builder(context, title, message, icon, key, null, positive, nagetive, listener).show();
    }
	public static AlertDialog showAlertDialog(Context context, int key,
	        String title, String message, int icon, int positive, int nagetive,
            DialogListener listener) {
        return builder(context, title, message, icon, key, null, positive, nagetive, listener).show();
    }
	
	/**
	 * 模式对话框，屏蔽返回键。
	 * @param context
	 * @param key
	 * @param title
	 * @param message
	 * @param icon
	 * @param positive
	 * @param nagetive
	 * @param listener
	 * @return
	 */
	public static AlertDialog showAlertDialogModel(Context context, int key,
			int title, int message, int icon, int positive, int nagetive,
			DialogListener listener) {
		AlertDialog dialog = builder(context, title, message, icon, key, null, positive, nagetive, listener).show();
		dialog.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
					return true;
				}
				
				return false;
			}
		});
		return dialog;
	}
	
	public static AlertDialog showAlertDialog(Context context, int key,
			int title, int message, int icon, View view, int positive,
			DialogListener listener) {
		return builder(context, title, message, icon, key, view, positive, -1, listener).show();
	}
	
	public static AlertDialog showAlertDialog(Context context, int key,
			int title, int message, int icon, View view,
			int positive, int nagetive, DialogListener listener) {
		return builder(context, title, message, icon, key, view, positive, nagetive, listener).show();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (mDialogListner != null) {
			mDialogListner.onClick(dialog, which, mKey, mView);
		}
		
		mDialogListner = null;
		mView = null;
		dialog.dismiss();
	}
	
}
