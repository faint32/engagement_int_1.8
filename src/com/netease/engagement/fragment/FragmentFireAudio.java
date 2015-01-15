package com.netease.engagement.fragment;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.engagement.widget.ProgressImageView;
import com.netease.service.media.MediaPlayerWrapper;
import com.netease.service.media.MediaPlayerWrapper.MediaListener;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.MessageInfo;

public class FragmentFireAudio extends FragmentBase implements MediaListener {

	public static FragmentFireAudio newInstance(MessageInfo msgInfo){
		FragmentFireAudio fragment = new FragmentFireAudio();
		Bundle bundle = new Bundle();
		bundle.putSerializable(EgmConstants.BUNDLE_KEY.MESSAGE_INFO, msgInfo);
		fragment.setArguments(bundle);
		return fragment ;
	}
	
	private MessageInfo msgInfo;
	
	private TextView durationTv;
	private ImageView playIv;
	private ImageView stopIv;
	private TextView fireTv;
	private ProgressImageView progressIv;
	
	private Timer timer;
	private long startPlayTs;
	
	private float startAngle = -90;
	private float sweepAngle;
	
	private boolean mBstartPlay = false;//防止通过距离传感器检测自动切换播放模式重新播放的进度重新开始
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			
		msgInfo = (MessageInfo) this.getArguments().getSerializable(EgmConstants.BUNDLE_KEY.MESSAGE_INFO);

		EgmService.getInstance().addListener(mCallBack);

		EgmService.getInstance().doGetFireMessageMediaUrl(msgInfo);
		showWatting("加载中");
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
//	    CustomActionBar mCustomActionBar = ((ActivityEngagementBase)getActivity()).getCustomActionBar();
//	    mCustomActionBar.hide();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = (View) inflater.inflate(R.layout.fragment_fire_audio_layout,container,false);
		init(view);
		return view;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		EgmService.getInstance().removeListener(mCallBack);
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}
	
	private void init(View root){
		if(root == null){
			return ;
		}
		durationTv = (TextView) root.findViewById(R.id.durationTv);
		playIv = (ImageView) root.findViewById(R.id.playIv);
		stopIv = (ImageView) root.findViewById(R.id.stopIv);
		fireTv = (TextView) root.findViewById(R.id.fireTv);
		progressIv = (ProgressImageView) root.findViewById(R.id.progressIv);
		
		durationTv.setText(getStringByDuration(msgInfo.duration * 1000));
		playIv.setOnClickListener(clickListener);
		stopIv.setOnClickListener(clickListener);
		fireTv.setOnClickListener(clickListener);
		
		MediaPlayerWrapper mediaPlayer = MediaPlayerWrapper.getInstance();
		mediaPlayer.registerMediaListener(this);
		
		if (TextUtils.isEmpty(msgInfo.mediaUrl)) {
			playIv.setClickable(false);
		}
	}
	
	
	@Override
	public void onMediaPrePare() {
	}

	@Override
	public void onMediaPlay() {
		if(mBstartPlay){
			mBstartPlay = false;
			startProgressImageView();
		}
	}

	@Override
	public void onMediaPause() {
	}

	@Override
	public void onMediaRelease() {
		if(playIv != null && stopIv != null){
			playIv.setVisibility(View.VISIBLE);
			stopIv.setVisibility(View.GONE);
			stopProgressImageView();
		}
	}

	@Override
	public void onMediaCompletion() {
		progressIv.postDelayed(new Runnable() {
			@Override
			public void run() {
				playIv.setVisibility(View.VISIBLE);
				stopIv.setVisibility(View.GONE);
				stopProgressImageView();
			}
		}, 60);
	}
	
	private void startProgressImageView() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		startPlayTs = System.currentTimeMillis();
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				long current = System.currentTimeMillis();
				long duration = current - startPlayTs;
				float total = (msgInfo.duration - 1) * 1000;
				float percent = (float)duration / total;
				sweepAngle = percent * 360;
				if (sweepAngle >= 360) {
					sweepAngle = 359.9999f;
				}
				progressIv.post(new Runnable() {
					@Override
					public void run() {
						progressIv.setAngle(startAngle, sweepAngle);
					}
				});
				
			}
		}, 0, 50);
	}
	
	private void stopProgressImageView() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}
	
	
	OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.playIv) {
				playIv.setVisibility(View.GONE);
				stopIv.setVisibility(View.VISIBLE);
				mBstartPlay = true;
				MediaPlayerWrapper.getInstance().play(msgInfo.mediaUrl,MediaPlayerWrapper.getAudioStreamType(getActivity()));
			} else if (v.getId() == R.id.stopIv) {
				playIv.setVisibility(View.VISIBLE);
				stopIv.setVisibility(View.GONE);
				stopProgressImageView();
				MediaPlayerWrapper.getInstance().stop();
			} else if (v.getId() == R.id.fireTv) {
				if (stopIv.getVisibility() == View.VISIBLE) {
					stopProgressImageView();
					MediaPlayerWrapper.getInstance().stop();
				}
				FragmentFireAudio.this.getActivity().finish();
			}
		}
	};
	
	/**
	 * 获取语音消息显示时长
	 * @param duration
	 * @return
	 */
	public static String getStringByDuration(long duration) {
        String value = null;

        if (duration <= 0) {
            value = "0\"";
            return value;
        }

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC +08:00"));
        value = formatter.format(duration);
        String[] splits = value.split(":");
        if (splits[0].endsWith("00")) {
        	value = splits[1] + ":" + splits[2];
        } else {
            value = splits[0] + ":" + splits[1] + "'" + splits[2] + "\"";
        }
        return value;
    }
	
	private EgmCallBack mCallBack = new EgmCallBack(){
		@Override
		public void onGetFireMessageMediaUrlSucess(int transactionId, String mediaUrl) {
			msgInfo.mediaUrl = mediaUrl;
			playIv.setClickable(true);
			playIv.performClick();
			
			// 音频文件预下载
			EgmService.getInstance().doDowloadMsgRes(msgInfo);
			
			FragmentFireAudio.this.stopWaiting();
		}
		
		@Override
		public void onGetFireMessageMediaUrlError(int transactionId, int errCode, String err) {
			FragmentFireAudio.this.stopWaiting();
			Toast.makeText(FragmentFireAudio.this.getActivity(), err, Toast.LENGTH_SHORT).show();
		}
	};
	
}
