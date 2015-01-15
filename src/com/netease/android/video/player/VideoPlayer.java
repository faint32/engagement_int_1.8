package com.netease.android.video.player;


import android.media.MediaPlayer;
import android.util.Log;
import android.widget.FrameLayout;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.EnumSet;

public abstract class VideoPlayer
        implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnPreparedListener {
    protected static final String TAG = "VideoPlayer";
    protected MediaPlayer mMediaPlayer = new MediaPlayer();
    protected VideoPlayer.OnCompletionListener mOnCompletionListener;
    protected VideoPlayer.OnErrorListener mOnErrorListener;
    protected VideoPlayer.OnPreparedListener mOnPreparedListener;
    protected VideoPlayer.State mState;

    public VideoPlayer() {
        mMediaPlayer.setLooping(true);
        this.mMediaPlayer.setOnPreparedListener(this);
        this.mMediaPlayer.setOnCompletionListener(this);
        this.mMediaPlayer.setOnErrorListener(this);
        this.mMediaPlayer.setOnInfoListener(this);
        this.mState = VideoPlayer.State.IDLE;
    }

    public abstract void bindView(FrameLayout paramFrameLayout, int paramInt);

    public boolean canPlay() {
        return EnumSet.of(VideoPlayer.State.PREPARED, VideoPlayer.State.STARTED, VideoPlayer.State.PAUSED, VideoPlayer.State.PLAYBACK_COMPLETED).contains(this.mState);
    }

    public boolean isPaused() {
        return this.mState == VideoPlayer.State.PAUSED;
    }

    public boolean isPlaybackCompleted() {
       return this.mState == VideoPlayer.State.PLAYBACK_COMPLETED;
    }

    public boolean isPlaying() {
        return this.mMediaPlayer.isPlaying();
    }

    public boolean isPreparingOrPlaying() {
        return EnumSet.of(VideoPlayer.State.INITIALIZED, VideoPlayer.State.PREPARING, VideoPlayer.State.PREPARED, VideoPlayer.State.STARTED).contains(this.mState);
    }

    public void onCompletion(MediaPlayer paramMediaPlayer) {
        this.mState = VideoPlayer.State.PLAYBACK_COMPLETED;
        if (this.mOnCompletionListener != null)
            this.mOnCompletionListener.onCompletion(this);
    }

    public boolean onError(MediaPlayer paramMediaPlayer, int paramInt1, int paramInt2) {
        Log.e("VideoPlayer", "MediaPlayer Error: " + paramInt1 + " " + paramInt2);
        if (this.mOnErrorListener != null) {
            return this.mOnErrorListener.onError(this);
        }
        return true;
    }

    public boolean onInfo(MediaPlayer paramMediaPlayer, int paramInt1, int paramInt2) {
        if (paramInt1 == 700)
            Log.i("VideoPlayer", "MediaPlayer Info: LAGGING " + paramInt2);
        while (true) {
            return false;
            //Log.i("VideoPlayer", "MediaPlayer Info: " + paramInt1 + " " + paramInt2);
        }
    }

    public void onPrepared(MediaPlayer paramMediaPlayer) {
        this.mState = VideoPlayer.State.PREPARED;
        if (this.mOnPreparedListener != null)
            this.mOnPreparedListener.onPrepared(this);
    }

    protected abstract void onRelease();

    protected abstract void onReset();

    public void pause() {
        if ((this.mState == VideoPlayer.State.STARTED) || (this.mState == VideoPlayer.State.PAUSED)) {
            this.mState = VideoPlayer.State.PAUSED;
            this.mMediaPlayer.pause();
        }
    }

    public void play() {
        if ((this.mState == VideoPlayer.State.PREPARED) || (this.mState == VideoPlayer.State.STARTED) || (this.mState == VideoPlayer.State.PAUSED) || (this.mState == VideoPlayer.State.PLAYBACK_COMPLETED)) {
            this.mMediaPlayer.start();
            this.mState = VideoPlayer.State.STARTED;
            return;
        }
        throw new IllegalStateException("VideoPlayer cannot play in state " + this.mState);
    }

    public void release() {
        this.mMediaPlayer.release();
        onRelease();
        this.mMediaPlayer = null;
        this.mState = VideoPlayer.State.END;
    }

    public abstract void removeView();

    public void reset() {
        if (this.mState != VideoPlayer.State.IDLE) {
            this.mMediaPlayer.reset();
            onReset();
            this.mState = VideoPlayer.State.IDLE;
        }
    }

    public void setFileDescriptor(FileDescriptor paramFileDescriptor) throws IOException {
        if (this.mState != VideoPlayer.State.IDLE)
            this.mMediaPlayer.reset();
        this.mMediaPlayer.setDataSource(paramFileDescriptor);
        this.mMediaPlayer.prepareAsync();
        this.mState = VideoPlayer.State.PREPARING;
    }

    public void setOnCompletionListener(VideoPlayer.OnCompletionListener paramOnCompletionListener) {
        this.mOnCompletionListener = paramOnCompletionListener;
    }

    public void setOnErrorListener(VideoPlayer.OnErrorListener paramOnErrorListener) {
        this.mOnErrorListener = paramOnErrorListener;
    }

    public void setOnPreparedListener(VideoPlayer.OnPreparedListener paramOnPreparedListener) {
        this.mOnPreparedListener = paramOnPreparedListener;
    }

    public void setUrl(String paramString) throws IOException {
        if (this.mState != VideoPlayer.State.IDLE)
            this.mMediaPlayer.reset();
        this.mMediaPlayer.setDataSource(paramString);
        this.mMediaPlayer.prepareAsync();
        this.mState = VideoPlayer.State.PREPARING;
    }

    public void setVolume(float paramFloat1, float paramFloat2) {
        this.mMediaPlayer.setVolume(paramFloat1, paramFloat2);
    }

    public static interface OnCompletionListener {
        void onCompletion(VideoPlayer paramVideoPlayer);
    }

    public static interface OnErrorListener {
        boolean onError(VideoPlayer paramVideoPlayer);
    }

    public static interface OnPreparedListener {
        void onPrepared(VideoPlayer paramVideoPlayer);
    }

    public static enum State {
        IDLE, INITIALIZED, PREPARING, PREPARED, STARTED, PAUSED, STOPPED, PLAYBACK_COMPLETED, ERROR, END
    }
}
