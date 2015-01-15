package com.netease.engagement.activity;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.widget.MovieViewControl;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.media.MediaPlayerWrapper;
import com.netease.util.AudioUtil;

public class ActivityVideoPlay extends ActivityEngagementBase{
	
	public static void startActivity(Context context,String videoUri) {
    	Intent intent = new Intent();
        intent.setClass(context, ActivityVideoPlay.class);
        intent.putExtra(EgmConstants.BUNDLE_KEY.CHAT_VIDEO_PATH, videoUri);
        context.startActivity(intent);
        MediaPlayerWrapper.getInstance().pause();
    }
	
	public static void startActivityForFire(Context context,String videoUri) {
    	Intent intent = new Intent();
        intent.setClass(context, ActivityVideoPlay.class);
        intent.putExtra(EgmConstants.BUNDLE_KEY.CHAT_VIDEO_PATH, videoUri);
        intent.putExtra(EgmConstants.BUNDLE_KEY.CHAT_VIDEO_FOR_FIRE, true);
        context.startActivity(intent);
        MediaPlayerWrapper.getInstance().pause();
    }
	
	private String videoUri ;
	
	private MovieViewControl mControl ;
	
	private ImageView mClose ;
	
	private boolean mResumed ;
	private boolean mControlResumed ;
	
	private int mAudioRingerMode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.getSupportActionBar().hide();
		
		setContentView(R.layout.activity_video_play);
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		videoUri = this.getIntent().getStringExtra(EgmConstants.BUNDLE_KEY.CHAT_VIDEO_PATH);
		if(TextUtils.isEmpty(videoUri)){
			finish();
			return ;
		}
		
		boolean isForFire = this.getIntent().getBooleanExtra(EgmConstants.BUNDLE_KEY.CHAT_VIDEO_FOR_FIRE, false);
		if (isForFire) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
		}
		
		mClose = (ImageView)this.findViewById(R.id.video_close);
		mClose.setOnClickListener(mOnClickListener);
		
		View rootView = this.findViewById(R.id.root);
		playVideo(rootView,videoUri);
	}

	
	private OnClickListener mOnClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			finish();
		}
	};
	
	private void playVideo(View rootView , String videoUri){
		mControl = new MovieViewControl(rootView,this, videoUri){
			@Override
			public boolean onError(MediaPlayer player, int arg1, int arg2) {
				super.onError(player, arg1, arg2);
				ToastUtil.showToast(ActivityVideoPlay.this,R.string.video_can_not_play);
				finish();
				return true ;
			}

			@Override
			public void onCompletion(MediaPlayer mp) {
				super.onCompletion(mp);
				finish();
				return ;
			}

			@Override
			public void onPrepared(MediaPlayer mp) {
				super.onPrepared(mp);
			}
		};
	}
	
	@Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus && mResumed && !mControlResumed) {
        	if (null != mControl) {
        		mControl.onResume();
        	}
            mControlResumed = true;
        }
    }

	@Override
	protected void onResume() {
		super.onResume();
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		mResumed = true ;
		
		mAudioRingerMode = AudioUtil.setMuteAll(this, true, mAudioRingerMode);
	}
	
	@Override
    public void onPause() {
		super.onPause();
        mResumed = false;
        if (mControlResumed) {
        	if (null != mControl) {
        		mControl.onPause();
        	}
            mControlResumed = false;
        }
        
        mAudioRingerMode = AudioUtil.setMuteAll(this, false, mAudioRingerMode);
    }
	
	@Override
    protected void onDestroy() {
    	if (null != mControl) {
    		mControl.clear();;
    	}
        super.onDestroy();
    }
}
