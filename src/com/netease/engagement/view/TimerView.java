package com.netease.engagement.view;

import java.util.Timer;
import java.util.TimerTask;

import com.netease.date.R;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

public class TimerView extends TextView{

	public TimerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public TimerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public TimerView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context){
		this.setGravity(Gravity.CENTER);
		this.setText("00:00");
		this.setTextSize(TypedValue.COMPLEX_UNIT_SP,15);
		this.setTextColor(context.getResources().getColor(R.color.white));
	}
	
	private Timer mTimer;
	private TimerTask mTimerTask;
	public static final int MESSAGE = 0 ;
	
	private int mSeconds = 0 ;
	private int mMaxVideoTime = 60; // 最大视频录制时间
	
	public void setMaxVideoTime(int time) {
		if (time > 0) {
			mMaxVideoTime = time;
		}
	}
	
	/**
	 * 开始计时
	 */
	public void startTiming(){
		mTimer = new Timer();
		mTimerTask = new TimerTask(){
			@Override
			public void run() {
				mHandler.sendEmptyMessage(MESSAGE);
			}
		};
		mTimer.schedule(mTimerTask,1000,1000);
	}
	
	/**
	 * 停止计时
	 */
	public void stopTiming(){
		if(mTimerTask != null){
			mTimerTask.cancel();
		}
		if(mTimer != null){
			mTimer.cancel();
		}
	}
	
	/**
	 * 重置时间
	 */
	public void resetTiming(){
		if(mTimerTask != null){
			mTimerTask.cancel();
		}
		if(mTimer != null){
			mTimer.cancel();
		}
		mSeconds = -1 ;
		invalidateLayout();
	}
	
	/**
	 * 获取录制时间
	 */
	public int getTotalTime(){
		return mSeconds ;
	}
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			invalidateLayout();
			super.handleMessage(msg);
		}
	};
	
	/**
	 * 根据时间刷新界面显示
	 */
	private void invalidateLayout(){
		mSeconds++ ;
		if(mSeconds > mMaxVideoTime){
			if(listener != null){
				listener.onTimeMax();
			}
			return ;
		}
		String secondStr = mSeconds > 9 ? String.valueOf(mSeconds):"0"+mSeconds ;
		this.setText(String.format(getContext().getString(R.string.record_time),secondStr));
	}
	
	public void clear(){
		if(mTimerTask != null){
			mTimerTask = null ;
		}
		if(mTimer != null){
			mTimer = null ;
		}
		if(mHandler != null){
			mHandler = null ;
		}
	}
	
	private OnTimeMaxListener listener ;
	public interface OnTimeMaxListener{
		public void onTimeMax();
	}
	public void setOnTimeMaxListener(OnTimeMaxListener listener){
		this.listener = listener ;
	}
	
}
