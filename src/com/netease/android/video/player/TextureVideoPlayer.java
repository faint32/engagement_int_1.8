package com.netease.android.video.player;

import java.io.IOException;

import com.netease.common.service.BaseService;
import com.netease.util.AudioUtil;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.FrameLayout;

@TargetApi(14)
public class TextureVideoPlayer extends VideoPlayer implements
		TextureView.SurfaceTextureListener {
	// private ViewGroup mParent;
	private SurfaceTexture mSurfaceTexture;
	private Surface mSurface;
	private TextureView mTextureView;
	private String mPath;

	public TextureVideoPlayer() {
		super();
	}

	public TextureVideoPlayer(TextureView textureView) {
		this();
		this.mTextureView = textureView;
		this.mTextureView.setSurfaceTextureListener(this);
	}

	private void releaseSurface() {
		if (this.mSurfaceTexture != null) {
			this.mSurfaceTexture.release();
			this.mSurfaceTexture = null;
		}
	}

	protected void onRelease() {
//		releaseSurface();
	}

	protected void onReset() {
		this.mMediaPlayer.setSurface(null);
		releaseSurface();
	}

    public void release() {
    	if(mMediaPlayer != null){
    		this.mMediaPlayer.release();
    	}
        onRelease();
        this.mMediaPlayer = null;
        this.mState = VideoPlayer.State.END;
        
        AudioUtil.setMuteAll(BaseService.getServiceContext(), false, 0);
    }
	
	@Override
	public void setUrl(String paramString) throws IOException {
		mPath = paramString;
		openVideo();
	}

	private void openVideo() {
		if (TextUtils.isEmpty(mPath) || mSurface == null) {
			return;
		}
		stopPlayback();
		try {
			// if(this.mMediaPlayer == null){
			this.mMediaPlayer = new MediaPlayer();
//			Surface localSurface = new Surface(mSurfaceTexture);
			this.mMediaPlayer.setSurface(mSurface);
			// }

			// if (this.mState != VideoPlayer.State.IDLE)
			// this.mMediaPlayer.reset();

			this.mMediaPlayer.setOnPreparedListener(this);
			this.mMediaPlayer.setOnCompletionListener(this);
			this.mMediaPlayer.setOnErrorListener(this);
			this.mMediaPlayer.setOnInfoListener(this);

			// this.mState = VideoPlayer.State.IDLE;
			this.mMediaPlayer.setDataSource(mPath);
			this.mMediaPlayer.prepareAsync();
			this.mState = VideoPlayer.State.PREPARING;
			
			AudioUtil.setMuteAll(BaseService.getServiceContext(), true, 0);
		} catch (Exception e) {
			this.mState = VideoPlayer.State.ERROR;
			onError(mMediaPlayer, 0, 0);
		}

	}

	public void resume() {
		openVideo();
	}

	public void onSurfaceTextureAvailable(SurfaceTexture paramSurfaceTexture,
			int paramInt1, int paramInt2) {
		mSurfaceTexture = paramSurfaceTexture;
		mSurface = new Surface(paramSurfaceTexture);
		openVideo();
	}

	public boolean onSurfaceTextureDestroyed(SurfaceTexture paramSurfaceTexture) {
		mSurface = null;
		release();
		return false;
	}

	public void onSurfaceTextureSizeChanged(SurfaceTexture paramSurfaceTexture,
			int paramInt1, int paramInt2) {
	}

	public void onSurfaceTextureUpdated(SurfaceTexture paramSurfaceTexture) {
	}

	public void play() {
		if (this.mTextureView.getWidth() == 0) {
			Log.d("VideoPlayer", "The 0-height texture view bug happened");
			this.mTextureView.getParent().getParent().requestLayout();
		}
		super.play();
		
		AudioUtil.setMuteAll(BaseService.getServiceContext(), true, 0);
	}

	public void stopPlayback() {
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mState = State.IDLE;
		}
		
		AudioUtil.setMuteAll(BaseService.getServiceContext(), false, 0);
	}

	public int getCurrentPosition() {
		if (isInPlaybackState()) {
			return mMediaPlayer.getCurrentPosition();
		}
		return 0;
	}

	public boolean isInPlaybackState() {
		return (mMediaPlayer != null && this.mState != VideoPlayer.State.ERROR
				&& this.mState != VideoPlayer.State.IDLE && this.mState != VideoPlayer.State.PREPARING);
	}

	public void seekTo(int msec) {
		if (isInPlaybackState()) {
			mMediaPlayer.seekTo(msec);
			// mSeekWhenPrepared = 0;
		} else {
			// mSeekWhenPrepared = msec;
		}
	}

	public boolean canPause() {
		return (this.mState == VideoPlayer.State.STARTED)
				|| (this.mState == VideoPlayer.State.PAUSED);
	}

	@Override
	public void bindView(FrameLayout paramFrameLayout, int paramInt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeView() {
		// TODO Auto-generated method stub

	}
}
