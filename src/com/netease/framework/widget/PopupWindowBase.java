package com.netease.framework.widget;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.netease.date.R;

/**
 * PopupWindow封装类
 * @author gordondu
 *
 */
public abstract class PopupWindowBase {
	
	private static final int CONTAINER_ID = R.id.popupwindow_bg_layout_id;
	
	private PopupWindow mPopupWindow;
	private RelativeLayout mBgLayout;
	
	protected Activity mContext;
	protected LayoutInflater mInflater;
	protected ProgressDialog mWaitingProgress;

	public PopupWindowBase(Activity context) {
		mContext = context;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mBgLayout = new RelativeLayout(context);
		mBgLayout.setId(CONTAINER_ID);
		mBgLayout.setBackgroundResource(R.color.popupwindow_bg_color);
	}
	
	/**
	 * 子类初始化界面
	 * @param root
	 */
	protected abstract void initViews(RelativeLayout root);

	public void showWindow() {
		initViews(mBgLayout);
		mBgLayout.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.alpha_0_to_1_slow));
		mPopupWindow = new PopupWindow(mBgLayout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
		// ColorDrawable dw = new ColorDrawable(R.color.popupwindow_bg_color);
		mPopupWindow.setBackgroundDrawable(new ColorDrawable());
		mPopupWindow.setTouchable(true);
		mPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
		mPopupWindow.setClippingEnabled(false);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.update();
		mPopupWindow.showAtLocation(mContext.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
		mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				mPopupWindow = null;
			}
		});
		mBgLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mPopupWindow != null && v.getId() == CONTAINER_ID) {
					mPopupWindow.dismiss();
				}
			}
		});
	}
	
	public void closeWindow() {
		stopWaiting();
		if (mPopupWindow != null) {
			mPopupWindow.dismiss();
		}
	}

	protected void showWatting(String message) {
		showWatting(null, message, 300);
	}

	protected void showWatting(String title, String message) {
		showWatting(title, message, 300);
	}

	protected void showWatting(String title, String message, long delay) {
		if (mWaitingProgress != null) {
			stopWaiting();
		}
		if (mPopupWindow != null) {
			mPopupWindow.dismiss();
		}
		mWaitingProgress = DelayedProgressDialog.show(mContext, title, message, delay);
	}

	protected boolean isWaiting() {
		return (mWaitingProgress != null && mWaitingProgress.isShowing());
	}
	
	protected void stopWaiting() {
		if (isWaiting()) {
			mWaitingProgress.dismiss();
		}
		if (mWaitingProgress != null) {
			mWaitingProgress.cancel();
			mWaitingProgress = null;
		}
	}
}
