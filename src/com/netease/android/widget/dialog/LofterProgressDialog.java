package com.netease.android.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.netease.date.R;

/**
 * 自定义progressdialog
 * 
 * @author Administrator
 * 
 */
public class LofterProgressDialog extends Dialog {
	private String tips;
	
	public LofterProgressDialog(Context context) {
		super(context, R.style.lofter_progress_dialog);
	}

	public LofterProgressDialog(Context context, String tips) {
		super(context, R.style.lofter_progress_dialog);
		this.tips = tips;
	}
	
	public LofterProgressDialog(Context context, int style) {
		super(context, style);
	}

	public LofterProgressDialog(Context context, int style, String tips) {
		super(context, style);
		this.tips = tips;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_loading_layout);
		if (!TextUtils.isEmpty(tips)) {
			TextView txtTips = (TextView) this
					.findViewById(R.id.custom_progress_tips);
			if (txtTips != null) {
				txtTips.setVisibility(View.VISIBLE);
				txtTips.setText(tips);
			}
		}
	}
}
