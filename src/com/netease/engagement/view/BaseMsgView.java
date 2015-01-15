package com.netease.engagement.view;

import com.netease.date.R;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout.LayoutParams;

public abstract class BaseMsgView {

    public final static int DEFAULT_DURATION = 3000;
    public final static int DEFAULT_ANIM_DURATION = 300;

    private HideMsg mHideRunnable;
    private ViewGroup mRootView;
    private View mMsgView;
    private int mDuration = DEFAULT_DURATION;
    private Animation mInAnim, mOutAnim;
    
    public BaseMsgView(ViewGroup root) {
        if (root != null) {
            mRootView = root;
            init(root.getContext());
        }
    }

    public BaseMsgView(Activity activity) {
        if (activity != null) {
            mRootView = (ViewGroup)activity.findViewById(android.R.id.content);
            if (mRootView != null) {
                init(mRootView.getContext());
            }
        }
    }

    private void init(Context context) {
        mInAnim = AnimationUtils.loadAnimation(mRootView.getContext(), R.anim.push_down_in);
        mOutAnim = AnimationUtils.loadAnimation(mRootView.getContext(), R.anim.push_up_out);
        mInAnim.setDuration(DEFAULT_ANIM_DURATION);
        mOutAnim.setDuration(DEFAULT_ANIM_DURATION);
        initView(context);
    }

    private void initView(Context context) {
        mMsgView = getView(context);
        if(mMsgView != null)mMsgView.setVisibility(View.GONE);
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    public void setAnimDuration(int duration) {
        mInAnim.setDuration(duration);
        mOutAnim.setDuration(duration);
    }

    public void showAndAutoHide(ViewGroup.LayoutParams lp) {
        if (mRootView != null) {
            if (mMsgView == null) {
                initView(mRootView.getContext());
            }
            if (mMsgView.getParent() == null) {
                mRootView.addView(mMsgView,lp);
            }
            if (mHideRunnable != null) {
                mMsgView.removeCallbacks(mHideRunnable);
            }
            mMsgView.setVisibility(View.VISIBLE);
            mMsgView.startAnimation(mInAnim);
            mHideRunnable = new HideMsg();
            mMsgView.postDelayed(mHideRunnable, mDuration);
        }
    }
    
    public void show(ViewGroup.LayoutParams lp){
    	if (mRootView != null) {
            if (mMsgView == null) {
                initView(mRootView.getContext());
            }
            if (mMsgView.getParent() == null) {
                mRootView.addView(mMsgView,lp);
            }
            mMsgView.setVisibility(View.VISIBLE);
            mMsgView.startAnimation(mInAnim);
        }
    }

    public void continueShow() {
        if (mMsgView != null && mHideRunnable != null) {
            mMsgView.removeCallbacks(mHideRunnable);
        }
        mHideRunnable = new HideMsg();
        mMsgView.postDelayed(mHideRunnable, mDuration);
    }

    private class HideMsg implements Runnable {
        @Override
        public void run() {
            hide();
        }
    }

    public void hide() {
        hide(true);
    }

    public void hide(boolean anim) {
        if (mRootView != null && mMsgView != null) {
            if (anim && mMsgView.getVisibility() == View.VISIBLE) {
                mMsgView.startAnimation(mOutAnim);
            }
            if (mHideRunnable != null) {
                mMsgView.removeCallbacks(mHideRunnable);
            }
            mMsgView.setVisibility(View.GONE);
        }
        mHideRunnable = null;
    }

    public boolean isShowing() {
        if (mMsgView == null)
            return false;
        return mMsgView.getVisibility() == View.VISIBLE ? true : false;
    }

    protected abstract View getView(Context context);

}
