package com.netease.android.video.ui;

import android.content.Context;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import com.netease.android.video.ClipStackManager;

public class BackSpacePopupWindow extends PopupWindow {
	private final ClipStackManager mClipStackManager;
	private float mX;
	private float mY;

	public BackSpacePopupWindow(Context context,
			ClipStackManager stackManager, final View view) {
		super(new FrameLayout(context), FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);
		mClipStackManager = stackManager;
		final Rect rect = new Rect();
		view.getGlobalVisibleRect(rect);
		getContentView().setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mX = event.getX();
					mY = event.getY();
					if (rect.contains((int) mX, (int) mY)) {
						((Button)view).setPressed(true);
					}
				}
				return false;
			}
		});
		getContentView().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
				if (rect.contains((int)mX, (int)mY)) {
					mClipStackManager.deleteLastClip();
					((Button)view).setPressed(false);
				} else {
					mClipStackManager.cancelSoftDelete();
				}
			}
		});
	}
}