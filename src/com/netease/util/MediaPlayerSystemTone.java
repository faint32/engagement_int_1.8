package com.netease.util;

import java.io.IOException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.util.Log;

public class MediaPlayerSystemTone {

	private final String TAG = "MediaPlayerSystemTone";
	// 采用单例模式，不用隐名内部类，防止被过快的回收 MediaPlayer finalized without being released
	private static MediaPlayerSystemTone toneMediaPlayer;
	private AssetManager am;
	private static MediaPlayer mediaPlayer;
	private static Context context;

	/**
	 * 播放系统音效， 防止：mediaplayer went away with unhandled events 防止：MediaPlayer
	 * finalized without being released
	 * 
	 */
	public void playWelecomTone(String assetName) {
		mediaPlayer = new MediaPlayer();
//		mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
		try {
			
			am = context.getAssets();
			AssetFileDescriptor afd = am.openFd(assetName);
			// 使用MediaPlayer加载指定的声音文件
			if (mediaPlayer != null) {
				mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {
						// TODO Auto-generated method stub
						mp.stop();
						mp.reset();
						mp.release();
						mp = null;
						Log.v(TAG, "finish");
					}
				});
				mediaPlayer.setOnPreparedListener(new OnPreparedListener() {

					@Override
					public void onPrepared(MediaPlayer mp) {
						// TODO Auto-generated method stub
						mp.start();
					}
				});

				mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
				mediaPlayer.prepareAsync();

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param con
	 *            getAseet需要上下文，因此此处传入Context引用
	 * @return
	 */
	public static MediaPlayerSystemTone instance(Context con) {
		context = con.getApplicationContext();
		
		if (toneMediaPlayer == null)
			toneMediaPlayer = new MediaPlayerSystemTone();
		return toneMediaPlayer;
	}

}
