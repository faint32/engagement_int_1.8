package com.netease.service.media;

import android.media.MediaRecorder;

/**
 * 封装录音功能
 */
public class AudioRecorderWrapper {
    public static final String TAG = AudioRecorderWrapper.class.getSimpleName();
	private boolean bStart = false;
	private long mStartTime = -1;
	private long mStopTime = -1;
	
	private String mFilePath;
	private MediaRecorder mRecord;
	private static AudioRecorderWrapper sInstance = null;

	synchronized public static AudioRecorderWrapper getInstance() {
        if (null == sInstance) {
            sInstance = new AudioRecorderWrapper();
        }
        return sInstance;
    }
	
	private AudioRecorderWrapper() {
	}

	public void setFilePath(String filePath) {
        mFilePath = filePath;
    }
	
	public String getFilePath() {
	    return mFilePath;
	}
	
	/**
	 * 获取录音时长
	 * @return
	 */
	public long getDuration() {
	    if (mStopTime > 0 && mStartTime > 0) {
	        return mStopTime - mStartTime;
	    }
	    
	    return -1;
	}
	
	public synchronized boolean start() {
		boolean bRes = false;
		if (bStart) {
			return bRes;
		}
		mRecord = new MediaRecorder();
		mRecord.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecord.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
		mRecord.setOutputFile(mFilePath);
		mRecord.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

		try {
			mRecord.prepare();
			mRecord.start();
			bStart = true;
			bRes = true;
			mStartTime = System.currentTimeMillis();
			mStopTime = -1;
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return bRes;
	}

	public synchronized void stop() {
	    bStart = false;
		if (mRecord != null) {
			try {
				mRecord.stop();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				mRecord.release();
			}
		}
		mRecord = null;
		mStopTime = System.currentTimeMillis();
	}
	
	public boolean isRecord() {
	    return bStart;
	}
	
	public interface MediaRecorderListener {
        public void onMaxAmplitude(int value);
        
    }
}
