package com.netease.android.video.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.View;

@TargetApi(14)
public class SquareTextureView extends TextureView {
    public SquareTextureView(Context paramContext) {
        super(paramContext);
    }

    public SquareTextureView(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
    }

    public SquareTextureView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
    }

    protected void onMeasure(int paramInt1, int paramInt2) {
        int i = View.MeasureSpec.getSize(paramInt1);
        setMeasuredDimension(i, i);
    }
}

