package com.netease.service.media;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.netease.common.cache.CacheManager;
import com.netease.common.cache.file.StoreFile;
import com.netease.service.preferMgr.EgmPrefHelper;

/**
 * 封装播放功能，在这里控制了播放状态，调用处无需考虑
 */
public class MediaPlayerWrapper implements OnCompletionListener{
    public String TAG = MediaPlayerWrapper.class.getSimpleName();
    
	private String mFilePath;
	private String mName;
	private PlayStatus mPlayStatus;
	private ArrayList<MediaListener> mList = null;
	private static MediaPlayerWrapper sInstance = null;

	/**
	 * 播放状态
	 */
	public enum PlayStatus {
		IDLE, PREPARE, PLAYING, PAUSE;
	}

	synchronized public static MediaPlayerWrapper getInstance() {
	    if (null == sInstance) {
	        sInstance = new MediaPlayerWrapper();
	    }
	    
	    return sInstance;
	}
	
	private MediaPlayerWrapper() {
		mPlayStatus = PlayStatus.IDLE;
		mList = new ArrayList<MediaListener>();
	}
	
	public static void Destory() {
	    if (null != sInstance) {
	        sInstance.stop();
	        sInstance.clear();
	    }
	    sInstance = null;
	}
	
	static final int MSG_PREPARE = 0;
	static final int MSG_PLAY = 1;
	static final int MSG_PAUSE = 2;
    static final int MSG_STOP = 3;
    static final int MSG_CMP = 4;
    
	private Handler mHandler = new Handler() {
		@Override
		public void dispatchMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_PREPARE:
				onPrePare();
				break;
			case MSG_PLAY:
				onPlay();
				break;
			case MSG_PAUSE:
				onPause();
				break;
			case MSG_STOP:
				onStop();
				break;
			case MSG_CMP:
				onCompletion();
				break;
			default:
			    super.dispatchMessage(msg);
			}
		};
	};
	/*
	 * context 请使用appContext
	 */
	public void doBindService(Context context) {
	    context.bindService(new Intent(context, MediaPlayerService.class), mConnection, Context.BIND_AUTO_CREATE);
	    mIsBound = true;
	}
	/*
	 * doBindService 中使用了appContext,该方法不应该被调用，单实例，不需要解绑
	 */
	public void doUnbindService(Context context) {
	    if (mIsBound) {
	        context.unbindService(mConnection);
	    }
	}
	
	private boolean mIsBound;
	private MediaPlayerService mService = null;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((MediaPlayerService.LocalBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };
    
	private void setFilePath(String filePath) {
	    mFilePath = filePath;
	}
	
	public String getFilePath() {
        return mFilePath;
    }
	
	public void setName(String value) {
	    mName = value;
    }
    
    public String getName() {
        return mName;
    }
	
	public PlayStatus getPlayStatus() {
	    return mPlayStatus;
	}
	
	public boolean isPlaying(){
		if (PlayStatus.PLAYING == mPlayStatus){
			return true;
		}
		return false;
	}
	
	public void registerMediaListener(MediaListener listener) {
	    if (null == mList) {
	        mList = new ArrayList<MediaListener>();
	    }
	    
	    if (!mList.contains(listener)) {
	        mList.add(listener);
	    }
	}
	
	public void removeMediaListener(MediaListener listener) {
	    if (null != mList) {
	        mList.remove(listener);
	    }
	}
	
	public void removeAllMediaListener() {
		if (null != mList) {
			mList.clear();
		}
	}
	
	/**
	 * 强制播放新的音乐
	 * @param filePath
	 */
	public void play(String filePath) {
	    play(filePath, filePath,AudioManager.STREAM_MUSIC);
    }
	/**
	 * 强制播放新的音乐
	 * @param filePath
	 */
	public void play(String filePath,int audioStreamType) {
	    play(filePath, filePath,audioStreamType);
    }
	/**
	 * 强制播放新的音乐
	 * @param filePath
	 */
	public void play(String filePath, String url) {
		play(filePath,url,AudioManager.STREAM_MUSIC);
	}
	/**
	 * 强制播放新的音乐
	 * @param filePath
	 */
	public void play(String filePath, String url,int audioStreamType) {
		setFilePath(filePath);
		
		if (! TextUtils.isEmpty(url) && URLUtil.isNetworkUrl(url)) {
			StoreFile file = CacheManager.getStoreFile(url);
			if (file != null && file.exists()) {
				filePath = file.getPath();
			}
		}
		
        if (null != mService) {
            mService.play(filePath,audioStreamType);
        }
    }
	
	/**
	 * 播放暂停的音乐
	 */
	public void play() {
		if (null != mService && mService.isPlaying()) {
		    mService.play();
		}
	}

	public void pause() {
	    if (null != mService && mService.isPlaying()) {
            mService.pause();
        }
	}

	public void stop() {
	    // TODO 由于在网络不好情况下消息队列被阻塞。为了使界面更新,直接环回.
	    onStop();
	    mFilePath = null;
	    if (null != mService) {
            mService.stop();
        }
	}
	
	public void clear() {
	    if (null != mList) {
	        mList.clear();
	    }
	    mList = null;
	}
	
	public interface MediaListener {
		public void onMediaPrePare();
	    public void onMediaPlay();
	    public void onMediaPause();
	    public void onMediaRelease();
	    public void onMediaCompletion();
	    
	}
	
	private void onPrePare() {
		mPlayStatus = PlayStatus.PREPARE;
	    ArrayList<MediaListener> list = new ArrayList<MediaListener>();
        list.addAll(mList);
        if (list.size() > 0) { 
            for (MediaListener listener : list) {
                try {
                    listener.onMediaPrePare();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
	}
	
	private void onPlay() {
		mPlayStatus = PlayStatus.PLAYING;
	    ArrayList<MediaListener> list = new ArrayList<MediaListener>();
        list.addAll(mList);
        if (list.size() > 0) { 
            for (MediaListener listener : list) {
                try {
                    listener.onMediaPlay();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
	}
	
	private void onPause() {
		mPlayStatus = PlayStatus.PAUSE;
	    ArrayList<MediaListener> list = new ArrayList<MediaListener>();
        list.addAll(mList);
        if (list.size() > 0) { 
            for (MediaListener listener : list) {
                try {
                    listener.onMediaPause();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
	}
	
	private void onStop() {
		mPlayStatus = PlayStatus.IDLE;
        ArrayList<MediaListener> list = new ArrayList<MediaListener>();
        list.addAll(mList);
        if (list.size() > 0) { 
            for (MediaListener listener : list) {
                try {
                    if(listener != null){
                        listener.onMediaRelease();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
	
	private void onCompletion() {
        ArrayList<MediaListener> list = new ArrayList<MediaListener>();
        list.addAll(mList);
        if (list.size() > 0) { 
            for (MediaListener listener : list) {
                try {
                    listener.onMediaCompletion();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
	}
	
    @Override
    public void onCompletion(MediaPlayer mp) {
        mHandler.removeMessages(MSG_CMP);
    	mHandler.sendEmptyMessage(MSG_CMP);
    }
    
    public void PrePareMsg() {
        mHandler.removeMessages(MSG_PREPARE);
    	mHandler.sendEmptyMessage(MSG_PREPARE);
    }
    
    public void PlayMsg() {
        mHandler.removeMessages(MSG_PLAY);
    	mHandler.sendEmptyMessage(MSG_PLAY);
	}
	
	public void PauseMsg() {
	    mHandler.removeMessages(MSG_PAUSE);
		mHandler.sendEmptyMessage(MSG_PAUSE);
	}
	
	public void StopMsg() {
	    mHandler.removeMessages(MSG_STOP);
		mHandler.sendEmptyMessage(MSG_STOP);
    }
	
	public void CompMsg() {
	    mHandler.removeMessages(MSG_CMP);
		mHandler.sendEmptyMessage(MSG_CMP);
    }
	public boolean setAudioStreamType(int type){
		if(mService != null){
			return mService.setAudioStreamType(type);
		}
		return false;
	}
	
	public static int getAudioStreamType(Context context){
		if(EgmPrefHelper.getReceiverModeOn(context)){
			return AudioManager.STREAM_VOICE_CALL;
		} 
		return AudioManager.STREAM_MUSIC;
	}
}
