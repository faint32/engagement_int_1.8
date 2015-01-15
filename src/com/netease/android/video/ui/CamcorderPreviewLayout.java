package com.netease.android.video.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.RelativeLayout;

public class CamcorderPreviewLayout extends RelativeLayout {
	private double mAspectRatio;

	public CamcorderPreviewLayout(Context context) {
		super(context);
	}

	public CamcorderPreviewLayout(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
	}

	public CamcorderPreviewLayout(Context context, AttributeSet attributeSet,
			int defStyle) {
		super(context, attributeSet, defStyle);
	}

	protected void onMeasure(int widthSpec, int heightSpec) {
		int previewWidth = MeasureSpec.getSize(widthSpec);
        int previewHeight = MeasureSpec.getSize(heightSpec);
        // Get the padding of the border background.
        int hPadding = getPaddingLeft() + getPaddingRight();
        int vPadding = getPaddingTop() + getPaddingBottom();

        // Resize the preview frame with correct aspect ratio.
        previewWidth -= hPadding;
        previewHeight -= vPadding;
        if (previewWidth > previewHeight * mAspectRatio) {
            previewWidth = (int) (previewHeight * mAspectRatio + .5);
        } else {
            previewHeight = (int) (previewWidth / mAspectRatio + .5);
        }

        // Add the padding of the border.
        previewWidth += hPadding;
        previewHeight += vPadding;

        // Ask children to follow the new preview dimension.
        super.onMeasure(MeasureSpec.makeMeasureSpec(previewWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(previewHeight, MeasureSpec.EXACTLY));
	}

	public void setAspectRatio(double ratio) {
		if (ratio <= 0.0D)
			throw new IllegalArgumentException();
		if (this.mAspectRatio != ratio) {
			double d = 1.0D / ratio;
			this.mAspectRatio = d;
			requestLayout();
		}
	}
}