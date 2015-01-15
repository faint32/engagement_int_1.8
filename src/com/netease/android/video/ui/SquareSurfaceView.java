package com.netease.android.video.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

public class SquareSurfaceView extends SurfaceView
{

    public SquareSurfaceView(Context context)
    {
        super(context);
    }

    public SquareSurfaceView(Context context, AttributeSet attributeset)
    {
        super(context, attributeset);
    }

    public SquareSurfaceView(Context context, AttributeSet attributeset, int i)
    {
        super(context, attributeset, i);
    }

    protected void onMeasure(int i, int j)
    {
        super.onMeasure(i, j);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }
}
