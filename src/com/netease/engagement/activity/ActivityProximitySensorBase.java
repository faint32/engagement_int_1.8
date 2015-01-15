package com.netease.engagement.activity;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.app.EngagementApp;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.service.media.MediaPlayerWrapper;
import com.netease.service.media.MediaPlayerWrapper.MediaListener;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
/**
 * 需要根据距离优传感器自动切换音频通道的activity继承此类(只手动切换可不继承)
 * @author echo_chen
 * @since 2014-11-26
 */

public abstract class ActivityProximitySensorBase extends ActivityEngagementBase {
	 // sensor
	 private SensorManager mSensorManager;

	 private Sensor mProximitySensor;

	 protected SensorEventListener mProximitySensorEventListener;
	 
	 private int mVolumeCtrolOrginal;
	 
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initProximitySensor();
		EgmService.getInstance().addListener(mCallBack);
		MediaPlayerWrapper.getInstance().doBindService(EngagementApp.getAppInstance());
		mVolumeCtrolOrginal = getVolumeControlStream();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(EgmPrefHelper.getReceiverModeOn(this)){
			 setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
		 } else {
//			 setVolumeControlStream(AudioManager.STREAM_MUSIC);
			 registerProximitySensorListener();
		 }
		MediaPlayerWrapper.getInstance().registerMediaListener(mMediaPlayListener);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterProximitySensorListener();
		MediaPlayerWrapper.getInstance().removeMediaListener(mMediaPlayListener);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		EgmService.getInstance().removeListener(mCallBack);
		 if(mPopupWindow != null && mPopupWindow.isShowing()){
             mPopupWindow.dismiss();
         }
         mPopupWindow = null;
	}
	 private void initProximitySensor() {
		 if (mSensorManager != null) {
			 return;
		 }
		 mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		 mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

		 final float PROXIMITY_THRESHOLD = 5.0f;
		 

		 mProximitySensorEventListener = new SensorEventListener() {

			 @Override
			 public void onAccuracyChanged(Sensor sensor, int accuracy) {
			 }

			 @Override
			 public void onSensorChanged(SensorEvent event) {
				 if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
					 float distance = event.values[0];
					 if (distance < PROXIMITY_THRESHOLD && distance < event.sensor.getMaximumRange()) {
						 if(MediaPlayerWrapper.getInstance().isPlaying()){
							 gotoReciverMode();
						 }
					 } else {
						 exitReciverMode();
					 }

				 }
			 }
		 };
	 }
	 
	 private void gotoReciverMode() {
//		 getWindow().setFlags(WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES,
//				 WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES);
//		 getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		 boolean changed = MediaPlayerWrapper.getInstance().setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
		 if (changed) {
			 setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
		 }
	 }

	 private void exitReciverMode() {
//	 	getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES);
//	 	getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		 boolean changed = MediaPlayerWrapper.getInstance().setAudioStreamType(AudioManager.STREAM_MUSIC);
		 if (changed) {
			 setVolumeControlStream(mVolumeCtrolOrginal);
		 }
	 }
	 
	 private void registerProximitySensorListener() {
		 if (mSensorManager != null && mProximitySensor != null && mProximitySensorEventListener != null) {
			 mSensorManager.registerListener(mProximitySensorEventListener, mProximitySensor,
					 SensorManager.SENSOR_DELAY_NORMAL);
		 }
	 }

	 private void unregisterProximitySensorListener() {
		 if (mSensorManager != null && mProximitySensor != null && mProximitySensorEventListener != null) {
			 mSensorManager.unregisterListener(mProximitySensorEventListener);
		 }
	 }
	 
	 private MediaListener mMediaPlayListener = new MediaListener(){
			@Override
			public void onMediaPrePare() {
			}
			@Override
			public void onMediaPlay() {
				 
				if(EgmPrefHelper.getReceiverModeOn(ActivityProximitySensorBase.this)){
					CustomActionBar actionbar = getCustomActionBar();
					if(actionbar != null){
						 if(mPopupWindow != null && mPopupWindow.isShowing()){
			                    mPopupWindow.dismiss();
			                }
						 showReceiverModePopupWindow(actionbar.getCustomView(),true);
					} else{
						View topView = findViewById(android.R.id.content);
//						View topView =  getWindow().getDecorView();//被statusbar覆盖一半
						showReceiverModePopupWindow(topView,false);
					}
				}
			}
			@Override
			public void onMediaPause() {

			}
			@Override
			public void onMediaRelease() {

			}
			@Override
			public void onMediaCompletion() {

			}
		};
		private PopupWindow mPopupWindow ;	
		private Handler mHandler;
		private static final int AUDIO_TIP_SHOW_DURATION = 3*1000;//3秒钟
		
		/*听筒模式提示
		 * anchor,会在该view的下部显示，左侧和该view对齐
		 */
		private void showReceiverModePopupWindow(final View anchor,boolean alignAnchorBotom) {
			if(mPopupWindow == null){
		        final View tipsLayout = View.inflate(this, R.layout.view_chat_receivermode_tip, null);
		        // 创建PopupWindow
		        mPopupWindow = new PopupWindow(tipsLayout, LinearLayout.LayoutParams.MATCH_PARENT,
		                LinearLayout.LayoutParams.WRAP_CONTENT);
		        mPopupWindow.setBackgroundDrawable(new ColorDrawable());
		        mPopupWindow.setOutsideTouchable(false);
		        mPopupWindow.setFocusable(false);
		        mPopupWindow.setClippingEnabled(false);
		        mPopupWindow.setAnimationStyle(R.style.popwin_anim_style);
			}
	        // 设置PopupWindow的位置
	        mPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
	        final int anchorLocation[] = new int[2];
	        anchor.getLocationInWindow(anchorLocation);
	        if(alignAnchorBotom){
	        		mPopupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, anchorLocation[0], anchorLocation[1] + anchor.getHeight());
	        } else {
	        		mPopupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, anchorLocation[0], anchorLocation[1]);
	        }
	        
	        if(mHandler == null){
	        		mHandler = new Handler();
	        }
	        mHandler.removeCallbacks(mRunnable);
	        mHandler.postDelayed(mRunnable, AUDIO_TIP_SHOW_DURATION);
	    }
		private Runnable mRunnable = new Runnable() {
			@Override
			public void run() {
				if(mPopupWindow != null && mPopupWindow.isShowing()){
					mPopupWindow.dismiss();
                }
			}
		};	
		private EgmCallBack mCallBack = new EgmCallBack(){
			@Override
			public void onLoopBack(int transactionId, com.netease.service.protocol.meta.LoopBack obj) {
				if(obj != null){
					switch(obj.mType){
					case EgmConstants.LOOPBACK_TYPE.change_audiostrem:
						if(EgmPrefHelper.getReceiverModeOn(ActivityProximitySensorBase.this)){
							unregisterProximitySensorListener();
							Toast.makeText(ActivityProximitySensorBase.this, R.string.changed_to_receiver_mode, Toast.LENGTH_LONG).show();
							setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
							
						} else{
							registerProximitySensorListener();
							Toast.makeText(ActivityProximitySensorBase.this, R.string.changed_to_speaker_mode, Toast.LENGTH_LONG).show();
						}
						MediaPlayerWrapper.getInstance().setAudioStreamType(MediaPlayerWrapper.getAudioStreamType(ActivityProximitySensorBase.this));
						break;
					}
				}
			}
			
		};
}
