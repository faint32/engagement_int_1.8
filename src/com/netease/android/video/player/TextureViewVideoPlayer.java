package com.netease.android.video.player;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.netease.android.video.ui.SquareTextureView;

@TargetApi(14)
public class TextureViewVideoPlayer extends VideoPlayer
        implements TextureView.SurfaceTextureListener
{
    private ViewGroup mParent;
    private SurfaceTexture mSurfaceToRelease;
    private TextureView mTextureView;

    private void releaseSurface()
    {
        if (this.mSurfaceToRelease != null)
        {
            this.mSurfaceToRelease.release();
            this.mSurfaceToRelease = null;
        }
    }

    public void bindView(FrameLayout paramFrameLayout, int paramInt)
    {
        this.mParent = paramFrameLayout;
        this.mTextureView = new SquareTextureView(this.mParent.getContext());
        this.mTextureView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        this.mTextureView.setSurfaceTextureListener(this);
        this.mParent.addView(this.mTextureView, paramInt);
    }

    protected void onRelease()
    {
        releaseSurface();
    }

    protected void onReset()
    {
        this.mMediaPlayer.setSurface(null);
        releaseSurface();
    }

    public void onSurfaceTextureAvailable(SurfaceTexture paramSurfaceTexture, int paramInt1, int paramInt2)
    {
        Surface localSurface = new Surface(paramSurfaceTexture);
        this.mMediaPlayer.setSurface(localSurface);
    }

    public boolean onSurfaceTextureDestroyed(SurfaceTexture paramSurfaceTexture)
    {
        this.mSurfaceToRelease = paramSurfaceTexture;
        return false;
    }

    public void onSurfaceTextureSizeChanged(SurfaceTexture paramSurfaceTexture, int paramInt1, int paramInt2)
    {
    }

    public void onSurfaceTextureUpdated(SurfaceTexture paramSurfaceTexture)
    {
    }

    public void play()
    {
        if (this.mTextureView.getWidth() == 0)
        {
            Log.d("VideoPlayer", "The 0-height texture view bug happened");
            this.mTextureView.getParent().getParent().requestLayout();
        }
        super.play();
    }

    public void removeView()
    {
        if (this.mParent != null) {
            this.mParent.removeView(this.mTextureView);
            this.mParent = null;
        }
        this.mTextureView = null;
    }
}
