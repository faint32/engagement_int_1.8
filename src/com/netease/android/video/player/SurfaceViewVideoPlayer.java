package com.netease.android.video.player;

import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.netease.android.video.ui.SquareSurfaceView;

public class SurfaceViewVideoPlayer extends VideoPlayer
        implements android.media.MediaPlayer.OnCompletionListener, android.media.MediaPlayer.OnErrorListener, android.media.MediaPlayer.OnInfoListener, android.media.MediaPlayer.OnPreparedListener, android.view.SurfaceHolder.Callback {
    private ViewGroup mParent;
    private SurfaceView mSurfaceView;

    public void bindView(FrameLayout framelayout, int i) {
        mParent = framelayout;
        mSurfaceView = new SquareSurfaceView(mParent.getContext());
        mSurfaceView.setLayoutParams(new android.view.ViewGroup.LayoutParams(-1, -1));
        mSurfaceView.getHolder().addCallback(this);
        mSurfaceView.getHolder().setType(3);
        mParent.addView(mSurfaceView, i);
    }

    protected void onRelease() {
    }

    protected void onReset() {
        mMediaPlayer.setDisplay(null);
    }

    public void removeView() {
        if (mParent != null) {
            mParent.removeView(mSurfaceView);
        }
        mParent = null;
        mSurfaceView = null;
    }

    public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
        mMediaPlayer.setDisplay(surfaceholder);
    }

    public void surfaceCreated(SurfaceHolder surfaceholder) {
    }

    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
    }

}