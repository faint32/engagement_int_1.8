package com.netease.android.camera;

import java.io.IOException;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;

public class SoundPlayer implements Runnable {
	private static final String TAG = "SoundPlayer";
	private AssetFileDescriptor mAfd;
	private int mAudioStreamType;
	private boolean mExit;
	private int mPlayCount = 0;
	private MediaPlayer mPlayer;
	private Thread mThread;

	public SoundPlayer(AssetFileDescriptor assetFileDescriptor) {
		this.mAfd = assetFileDescriptor;
		this.mAudioStreamType = 3;
	}

	public SoundPlayer(AssetFileDescriptor assetFileDescriptor,
			boolean paramBoolean) {
		this.mAfd = assetFileDescriptor;
		if (paramBoolean) {
			this.mAudioStreamType = 7;
		} else {
			this.mAudioStreamType = 3;
		}
	}

	public void play() {
		if (this.mThread == null) {
			this.mThread = new Thread(this);
			this.mThread.start();
		}
		this.mPlayCount += 1;
		synchronized (this) {
			notifyAll();
		}
	}

	public void release() {
		if (mThread == null) {
			return;
		}
		try {
			mExit = true;
			synchronized (this) {
				notifyAll();
			}
			mThread.join();
		} catch (InterruptedException e) {
		} finally {
			if (mAfd != null) {
				try {
					mAfd.close();
				} catch (IOException e) {
				}
				mAfd = null;
			}
			if (mPlayer != null) {
				mPlayer.release();
				mPlayer = null;
			}
		}
	}

	public void run() {
		while (true) {
			try {
				if (this.mPlayer == null) {
					MediaPlayer mediaPlayer = new MediaPlayer();
					mediaPlayer.setAudioStreamType(this.mAudioStreamType);
					mediaPlayer.setDataSource(this.mAfd.getFileDescriptor(),
							this.mAfd.getStartOffset(), this.mAfd.getLength());
					mediaPlayer.setLooping(false);
					mediaPlayer.prepare();
					this.mPlayer = mediaPlayer;
					this.mAfd.close();
					this.mAfd = null;
				}
				if (this.mExit)
					break;
				if (this.mPlayCount > 0) {
					this.mPlayCount -= 1;
					this.mPlayer.start();
				}
				synchronized (this) {
					wait();
				}
			} catch (Exception exception) {
				Log.e(TAG, "Error playing sound", exception);
			}
		}
	}
}