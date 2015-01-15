package com.netease.framework.widget;

import org.w3c.dom.Text;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;


/**
 * 系统Toast封装
 * @author Panjf 
 * @date   2011-10-9
 */
public class ToastUtil {
	//自定义toast参考
//	private void showRefreshToast(String text) {
//	dismissRefreshToast();
//	
//	mHomeRefreshToast = new Toast(this);
//
//	LayoutInflater inflate = (LayoutInflater) this
//			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//	View v = inflate.inflate(R.layout.toast_notify, null);
//	TextView tv = (TextView) v.findViewById(R.id.message);
//	tv.setText(text);
//
//	mHomeRefreshToast.setView(v);
//	mHomeRefreshToast.setDuration(Toast.LENGTH_SHORT);
//	int yOffset = ImageUtilities.dip2px(this, 46);
//	mHomeRefreshToast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, yOffset);
//	mHomeRefreshToast.show();
//}
	
	private static final int TOAST_DURATION = Toast.LENGTH_SHORT;
	
	public static void showToast(Context context, String message) {
		if (! TextUtils.isEmpty(message)) {
			Toast t = Toast.makeText(context, message, TOAST_DURATION);
			t.show();
		}
	}

	public static Toast showToast(Context context, int resId) {
		Toast t = Toast.makeText(context, resId, TOAST_DURATION);
		t.show();
		
		return t;
	}
}
