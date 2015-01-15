package com.netease.android.video.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.netease.android.util.DpAndPxUtils;
import com.netease.date.R;

public class VideoPopup extends PopupWindow {
	private VideoPopup.Config mConfig;
	public VideoPopup(Context context, Config config) {
		super(getContentView(context, config.getBackgroundResId(),
				config.getTxtResId()), getWidth(context,config),
				ViewGroup.LayoutParams.WRAP_CONTENT, false);
		this.mConfig = config;
	}

	private static View getContentView(Context context, int bgResId,
			int txtResId) {
		View view = LayoutInflater.from(context).inflate(
				R.layout.video_popup_camcorder, null, false);
		view.findViewById(R.id.popup_root).setBackgroundResource(bgResId);
		((TextView) view.findViewById(R.id.popup_textview)).setText(txtResId);
		return view;
	}
	private static int getWidth(Context context,Config config){
		int width = ViewGroup.LayoutParams.WRAP_CONTENT;
		if (config == Config.TAP_TO_RECORD) {
			width = DpAndPxUtils.dip2px(70);
		}
		return width;
	}
	public VideoPopup.Config getConfig() {
		return this.mConfig;
	}

	public enum Config {
		TAP_TO_RECORD(R.drawable.video_tap_bottom_center,
				R.string.video_tap_record_tips), MIN_VIDEO_LENGTH(
				R.drawable.video_tap_bottom_center, R.string.video_minimum_tips), TAP_TO_CONTINUE(
						R.drawable.video_tap_bottom_right, R.string.video_maximum_tips), VIDEO_COVER_TIPS(
								R.drawable.video_tap_bottom_center, R.string.video_cover_tips), VIDEO_EDIT_END_TIPS(
										R.drawable.video_tap_bottom_center, R.string.video_edit_end_tips), VIDEO_EDIT_MIN_TIPS(
												R.drawable.video_tap_bottom_center, R.string.video_edit_min_tips),
										VIDEO_EDIT_END_RIGHT_TIPS(
												R.drawable.video_tap_bottom_right, R.string.video_edit_end_tips),
										VIDEO_EDIT_START_TIPS(
												R.drawable.video_tap_bottom_center, R.string.video_edit_start_tips);
		private int backgroundResId;
		private int txtResId;

		private Config(int bgResId, int mTxtResId) {
			backgroundResId = bgResId;
			txtResId = mTxtResId;
		}

		public int getBackgroundResId() {
			return backgroundResId;
		}

		public int getTxtResId() {
			return txtResId;
		}
	}
}