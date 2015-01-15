package com.netease.service.media;

import java.io.File;
import java.io.IOException;

import com.netease.common.log.NTLog;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * 音频播放Service，提供全局绑定服务
 * 注意记得注册，记得添加权限
 */
public class MediaPlayerService extends Service implements MediaPlayer.OnErrorListener,  
            MediaPlayer.OnPreparedListener ,MediaPlayer.OnCompletionListener{
    
    public String TAG = MediaPlayerService.class.getSimpleName();

    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }
    
    private LocalBinder mBinder = new LocalBinder();
    private MediaPlayer mMediaPlayer = null;
    private HandlerThread mHandlerThread = null;
    private Handler mHandler = null;
    private int mAudioStreamType = AudioManager.STREAM_MUSIC;
    private int mSeekPosition = 0;
    private boolean mNeedSeek = false;
    private String mPath;
    private AudioManager mAudioManager;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        
        mHandlerThread = new HandlerThread("MeidaPalyer");
        mHandlerThread.start();
        
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void dispatchMessage(android.os.Message msg) {
                try {
                    switch (msg.what) {
                        case MSG_PLAY://强制播放新的文件
                            String path = (String)msg.obj;
                            onPlay(path);
                            break;
                        case MSG_PAUSE_PLAY:
                            if (isInitialized() && isPlaying()) {
                                onPlay();
                            }
                            break;
                        case MSG_PAUSE:
                            if (isPlaying()) {
                                onPause();
                            }
                            break;
                        case MSG_STOP:
                            onStop();
                            break;
                        default:
                            super.handleMessage(msg);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } 
            };
        };
        super.onCreate();
    }
    
    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        mMediaPlayer = null;
        
        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mHandler = null;
        super.onDestroy();
    }
    
    static final int MSG_PLAY = 1;
    static final int MSG_PAUSE_PLAY = 2;
    static final int MSG_PAUSE = 3;
    static final int MSG_STOP = 4;
    
    
    public void play(String path,int audioStreamType) {
        // 对path进行处理
    	    if(TextUtils.isEmpty(path))
    	    		return;
    	    if(mHandler == null){
	    	    	 NTLog.e(TAG, "mHandler is null");
	    	    	 return;
    	    }
        if (path.startsWith("/data/data")) {
            //String id = XoneUtil.getIdFromPath(path);
           // Uri uri = XoneDBProviderExport.getUri(XoneUtil.TYPE_RECORDER, id);
            /*if (null != uri) {
                path = uri.toString();
            }*/
        }
        if(audioStreamType != mAudioStreamType){
        	   restartPlay();
        }
        mAudioStreamType = audioStreamType;
        mPath = path;
        Message msg = mHandler.obtainMessage();
        msg.what = MSG_PLAY;
        msg.obj = path;
        mHandler.sendMessage(msg);
       
    }
    
    public void play() {
        mHandler.removeMessages(MSG_PAUSE_PLAY);
        mHandler.sendEmptyMessage(MSG_PAUSE_PLAY);
    }
    
    public void pause() {
        mHandler.removeMessages(MSG_PAUSE);
        mHandler.sendEmptyMessage(MSG_PAUSE);
    }
    
    public void stop() {
        if(mHandler != null){
            mHandler.removeMessages(MSG_STOP);
            mHandler.sendEmptyMessage(MSG_STOP);
        }
    }

    // 是否初始化
    private boolean mIsInitialized = false;
    
    public boolean isInitialized() {
        return mIsInitialized;
    }
    
    // 是否在播放
    private boolean mIsSupposedToBePlaying = false;
    
    public boolean isPlaying() {
        return mIsSupposedToBePlaying;
    }
    
    public void onPlay(String path) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setAudioStreamType(mAudioStreamType);  
            mAudioManager.requestAudioFocus(
        			onAudioFocusChangeListener, 
        			mAudioStreamType,
        			AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepareAsync();
            mIsInitialized = true;
            MediaPlayerWrapper.getInstance().PrePareMsg();
        } catch (IOException e) { 
            e.printStackTrace();
            mIsInitialized = false;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            mIsInitialized = false;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            mIsInitialized = false;
        }
    }
    
    public void onPlay() {
        mMediaPlayer.start();
        MediaPlayerWrapper.getInstance().PlayMsg();
    }

    public void onPause() {
        mMediaPlayer.pause();
        MediaPlayerWrapper.getInstance().PauseMsg();
    }

    public void onStop() {
    		mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        } else {
            mMediaPlayer.reset();
        }
        mIsSupposedToBePlaying = false;
        mIsInitialized = false;
        MediaPlayerWrapper.getInstance().StopMsg();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                mIsSupposedToBePlaying = false;
                mIsInitialized = false;
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
    		mp.start();
        mIsSupposedToBePlaying = true;
        if(mNeedSeek){
         	mNeedSeek = false;
         	mp.seekTo(mSeekPosition);
        }
        MediaPlayerWrapper.getInstance().PlayMsg();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        MediaPlayerWrapper.getInstance().CompMsg();
        mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
    }
    
    public void restartPlay(){
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mHandler.removeMessages(MSG_PLAY);
			mHandler.removeMessages(MSG_PAUSE_PLAY);
		}
	   mMediaPlayer = new MediaPlayer();
       mMediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
    }
    
    public boolean setAudioStreamType(int type){
    	 	if(type != mAudioStreamType){
    	 		if (mMediaPlayer != null && mMediaPlayer.isPlaying()){
    	 			mAudioStreamType = type;
	    	 		mSeekPosition = mMediaPlayer.getCurrentPosition();
	    			mNeedSeek = true;
	    			restartPlay();
	    			play(mPath,mAudioStreamType);
    	 			return true;
    	 		}
    	 	}
    	 	return false;
    }
    
	OnAudioFocusChangeListener onAudioFocusChangeListener = new OnAudioFocusChangeListener() {

		@Override
		public void onAudioFocusChange(int focusChange) {
			Log.e("MediaPlayerService", "onAudioFocusChange focusChange" + focusChange);
			switch (focusChange) {
			case AudioManager.AUDIOFOCUS_GAIN:
				// 获得音频焦点
				break;
			case AudioManager.AUDIOFOCUS_LOSS:
				// 长久的失去音频焦点，释放MediaPlayer
				onStop();
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				// 展示失去音频焦点，暂停播放等待重新获得音频焦点
				onStop();
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				// 失去音频焦点，无需停止播放，降低声音即可
				onStop();
				break;
			}
		}
	};
}
