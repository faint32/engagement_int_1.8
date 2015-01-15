package com.netease.engagement.widget;

import java.io.File;
import com.netease.engagement.dataMgr.MsgDataManager;
import com.netease.service.media.AudioRecorderWrapper;
import com.netease.service.media.MediaPlayerWrapper;
import com.netease.service.protocol.EgmProtocolConstants;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

/**
 * 聊天界面录制语音
 */
public class RecordingView2 extends ImageView implements OnTouchListener{

	private static final long MAX_DURATION = 60 * 1000 ;
	private static final long MIN_DURATION = 1 * 1000 ;
	
	private AudioRecorderWrapper mAudioRecorderWrapper ;
	private String mFilePath ;
	
	private long mStartTime ;
	
	// 标示是否正在录音，手指按下为true，手指松开（或取消）为false。
	// 因为手指移动而改变值。而实际录音时间，就是手指按下到松开（或取消）的整个过程，不因为手指移动而中指录音。
	public boolean mIsRecording = false ;    
	
	// 标示手指是否在按键上。手指在按键上为false；手指移开为true。
	// 手指按下时，先初始化为false。
	// 如果手指移开，设置为true；如果手指又重新回来，设置为false。
	// 当为false时，不显示红色警告；当为true时，显示红色警告；
	// 手指松开（或取消）时，根据cancel的值，确认是否需要发送
	// 但有个例外的情况，如果时间到60秒了，直接发送，不理会cancel的值
	private boolean mStateCancel = false ;   
	
	// 标识是否到了最长时间长度（60s），如果是设置为true
	private boolean max_duration = false ;
	
	public RecordingView2(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public RecordingView2(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public RecordingView2(Context context) {
		super(context);
		init();
	}
	
	private void init(){
		setOnTouchListener(this);
		mAudioRecorderWrapper = AudioRecorderWrapper.getInstance();
	}
	
	private void startRecord(){
		//停止语音播放
		MediaPlayerWrapper.getInstance().stop();
		mAudioRecorderWrapper.stop();
		this.removeCallbacks(mRunnable);
		
		this.post(mRunnable);
		
		mStartTime = System.currentTimeMillis() ;
		mFilePath = MsgDataManager.getInstance().convertPath(
				EgmProtocolConstants.MSG_TYPE.MSG_TYPE_AUDIO, 
				String.valueOf(System.currentTimeMillis()));
		
		mAudioRecorderWrapper.setFilePath(mFilePath);
		
		mAudioRecorderWrapper.start();
		if(mListener != null){
			mListener.onRecordStart();
		}
	}
	
	/**
	 * 用来记时
	 */
	private Runnable mRunnable = new Runnable(){
		@Override
		public void run() {
			long duration = System.currentTimeMillis() - mStartTime ;
			if(duration >= MAX_DURATION){
				stopRecord();
				String filePath = mAudioRecorderWrapper.getFilePath();
				if(mListener != null && !TextUtils.isEmpty(filePath)){
					// 满60秒，直接发送，不判断mStateCancel的值
					mListener.onRecordStoped(MAX_DURATION, false, filePath);
				}
				max_duration = true ;
				return ;
			}
			postDelayed(mRunnable,5);
		}
	};
	
	private void stopRecord(){
		this.removeCallbacks(mRunnable);
		mAudioRecorderWrapper.stop();
	}

	private int downX ;
	private int downY ;
	private int x ;
	private int y ;
	
	private long lastDownTime ;
	private long lastUpTime ;
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
				lastDownTime = System.currentTimeMillis();
				if(lastDownTime - lastUpTime <= 1000){
					return false ;
				}
				
				this.setSelected(true);
				downX = (int) event.getRawX();
				downY = (int) event.getRawY();
				mStateCancel = false ;
				mIsRecording = true;
				startRecord();
				break;
			case MotionEvent.ACTION_MOVE:
				x = (int) event.getRawX();
				y = (int) event.getRawY();
				boolean isOutside = false;
				if(Math.abs(y - downY) > Math.abs(x - downX)
						&& y - downY < 0
						&& Math.abs(y - downY) > 50){
					isOutside = true;
				}
				if (isOutside) {
					if (!mStateCancel) {
						mStateCancel = true;
						if(mListener != null){
							mListener.onRecordCancel(mStateCancel);
						}
					}
				} else {
					if (mStateCancel) {
						mStateCancel = false;
						if(mListener != null){
							mListener.onRecordCancel(mStateCancel);
						}
					}
				}
				
//				if(Math.abs(y - downY) > Math.abs(x - downX)
//						&& y - downY < 0
//						&& Math.abs(y - downY) > 50){
//					//取消发送
//					if(mListener != null){
//						mListener.onRecordCancel();
//						stopRecord();
//					}
//					mStateCancel = true ;
//				}
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				lastUpTime = System.currentTimeMillis();
				
				if(max_duration){
					max_duration = false;  // bug fix #140954  by gzlichangjie
					return false ;
				}
				
				this.setSelected(false);
				stopRecord();
				long duration = System.currentTimeMillis() - mStartTime ;
				//检查录音文件是否存在
				String filePath = mAudioRecorderWrapper.getFilePath();
				if(TextUtils.isEmpty(filePath) || !new File(filePath).exists()){
					break ;
				}
				if(duration < MIN_DURATION){
					if(mListener != null){
						mListener.onRecordTooShort();
						new File(filePath).delete() ;
					}
					break ;
				}else{
					if(mListener != null){
						mListener.onRecordStoped(duration, mStateCancel,filePath);
					}
				}
				
				mStateCancel = false;
				mIsRecording = false;
				break;
		}
		return true;
	}
	
	private OnRecordListener mListener ;
	public void setOnRecordListener(OnRecordListener listener){
		mListener = listener ;
	}
	public interface OnRecordListener{
		//录音开始
		public void onRecordStart();
		//松开手指停止录音
		public void onRecordStoped(
				long duration,
				boolean cancelState,
				String filePath
				);
		//手指上滑，取消发送
		public void onRecordCancel(boolean cancel);
		//录音时间太短
		public void onRecordTooShort();
	}
}
