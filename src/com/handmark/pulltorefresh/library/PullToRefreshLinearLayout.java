package com.handmark.pulltorefresh.library;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.netease.date.R;


public class PullToRefreshLinearLayout extends PullToRefreshBase<LinearLayout> {

    public PullToRefreshLinearLayout(Context context) {
        super(context);
    }

    public PullToRefreshLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullToRefreshLinearLayout(Context context, Mode mode) {
        super(context, mode);
    }

    public PullToRefreshLinearLayout(Context context, Mode mode, AnimationStyle style) {
        super(context, mode, style);
    }

    @Override
    public Orientation getPullToRefreshScrollDirection() {
        return Orientation.VERTICAL;
    }

    @Override
    protected LinearLayout createRefreshableView(Context context, AttributeSet attrs) {
        LinearLayout l = new LinearLayout(context, attrs);
        l.setId(R.id.linearlayout);
        return l;
    }

    @Override
    protected boolean isReadyForPullEnd() {
        return true;
    }

    @Override
    protected boolean isReadyForPullStart() {
        return true;
    }

}
