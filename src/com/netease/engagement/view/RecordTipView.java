package com.netease.engagement.view;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.date.R;

/**
 * 聊天录音界面
 * 手势和录音时间提示
 */
public class RecordTipView extends LinearLayout{

	public RecordTipView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public RecordTipView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public RecordTipView(Context context) {
		super(context);
		init(context);
	}
	
	//左边状态标识
	private TextView icon ;
	//录音时间
	private TextView time ;
	//手势提示
	private TextView tip ;
	
	private void init(Context context){
		this.setGravity(Gravity.CENTER);
		
		LayoutInflater inflater = (LayoutInflater) 
			context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View root = inflater.inflate(R.layout.view_record_tip_layout,this,true);
		icon = (TextView) root.findViewById(R.id.icon);
		time = (TextView) root.findViewById(R.id.time);
		tip = (TextView) root.findViewById(R.id.gesture_tip);
		setInitState();
	}
	
	private Timer mTimer;
	private TimerTask mTimerTask;
	public static final int MESSAGE = 0 ;
	
	private int mSeconds = 0 ;
	
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
	 * 设置初始状态
	 */
	private void setInitState(){
		mSeconds = 0 ;
		ViewCompat.setBackground(icon,getContext().getResources().getDrawable(R.drawable.icon_mesg_float_window_voice_circle));
		time.setText(String.format(getContext().getString(R.string.record_time),"00"));
		ViewCompat.setBackground(tip,getContext().getResources().getDrawable(R.drawable.bg_pgchat_mesg_float_window_bottom_black));
		tip.setText(getContext().getString(R.string.slide_to_cancel));
	}
	
	/**
	 * 设置为取消状态
	 */
	public void setCancelState(boolean cancel){
		if (cancel) {
			ViewCompat.setBackground(icon,getContext().getResources().getDrawable(R.drawable.icon_mesg_float_window_back_circle));
			ViewCompat.setBackground(tip,getContext().getResources().getDrawable(R.drawable.bg_pgchat_mesg_float_window_bottom_red));
			tip.setText(getContext().getString(R.string.release_to_cancel));
		} else {
			ViewCompat.setBackground(icon,getContext().getResources().getDrawable(R.drawable.icon_mesg_float_window_voice_circle));
			ViewCompat.setBackground(tip,getContext().getResources().getDrawable(R.drawable.bg_pgchat_mesg_float_window_bottom_black));
			tip.setText(getContext().getString(R.string.slide_to_cancel));
		}
		
//		mHandler.removeMessages(MESSAGE);
//		if(mTimerTask != null){
//			mTimerTask.cancel();
//			mTimerTask = null ;
//		}
//		if(mTimer != null){
//			mTimer.cancel();
//			mTimer = null ;
//		}
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
		String secondStr = mSeconds > 9 ? String.valueOf(mSeconds):"0"+mSeconds ;
		time.setText(String.format(getContext().getString(R.string.record_time),secondStr));
	}
	
	/**
	 * 重置状态
	 */
	public void clear(){
		if(mTimerTask != null){
			mTimerTask.cancel();
			mTimerTask = null ;
		}
		if(mTimer != null){
			mTimer.cancel();
			mTimer = null ;
		}
		setInitState();
	}
}
