package com.netease.android.video.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

public class PreviewSurfaceView extends SurfaceView {
	public PreviewSurfaceView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		setZOrderMediaOverlay(true);
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	private void setLayoutSize(int size) {
		ViewGroup.LayoutParams layoutParams = getLayoutParams();
		if ((layoutParams.width != size) || (layoutParams.height != size)) {
			layoutParams.width = size;
			layoutParams.height = size;
			setLayoutParams(layoutParams);
		}
	}

	public void expand() {
		setLayoutSize(-1);
	}

	public void shrink() {
		setLayoutSize(1);
	}
}