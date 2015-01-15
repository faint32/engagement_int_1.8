package com.netease.engagement.activity;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.URLUtil;
import android.widget.TextView;

import com.mixin.helper.media.VideoConverter;
import com.netease.android.activity.CamcorderActivity;
import com.netease.android.activity.VideoEditActivity;
import com.netease.common.cache.CacheManager;
import com.netease.common.cache.file.StoreFile;
import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.app.EngagementApp;
import com.netease.engagement.widget.CustomDialog;
import com.netease.engagement.widget.LoadingImageView;
import com.netease.engagement.widget.RecordingView;
import com.netease.engagement.widget.RecordingView.OnRecordListener;
import com.netease.engagement.widget.SlideSwitchView;
import com.netease.engagement.widget.SlideSwitchView.Position;
import com.netease.engagement.widget.SlideSwitchView.StateChangerListener;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.media.MediaPlayerWrapper;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.AudioIntroduce;
import com.netease.service.protocol.meta.AudioVideoSelfMode;
import com.netease.service.protocol.meta.VideoIntroduce;
import com.netease.util.FileUtil;

public class ActivityMyShow extends ActivityEngagementBase {
	
	private static final String EXTRA_VIDEO = "extra_video";
	private static final String EXTRA_COVER = "extra_cover";
	private static final String EXTRA_DURATION = "extra_duration";
	private static final String EXTRA_CAMERA = "extra_camera";
	private static final String EXTRA_VIDEO_PARAM = "extra_video_param";
	
	private static final String EXTRA_MODE = "extra_mode";
	
	private static final int EXTRA_MODE_VIDEO = 1;
	private static final int EXTRA_MODE_AUDIO = 2;
	
	public static void startActivity(Context context){
        Intent intent = new Intent(context, ActivityMyShow.class);
        context.startActivity(intent);
    }
	
	public static void startActivity(Context context, boolean video) {
		Intent intent = new Intent(context, ActivityMyShow.class);
		intent.putExtra(EXTRA_MODE, video ? EXTRA_MODE_VIDEO : EXTRA_MODE_AUDIO);
        context.startActivity(intent);
	}
	
	public static void startActivity(Context context, String video, 
			String cover, long duration, boolean camera, 
			VideoConverter.Params params) {
		Intent intent = new Intent(context, ActivityMyShow.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		intent.putExtra(EXTRA_VIDEO, video);
		intent.putExtra(EXTRA_COVER, cover);
		intent.putExtra(EXTRA_DURATION, duration);
		intent.putExtra(EXTRA_CAMERA, camera);
		intent.putExtra(EXTRA_VIDEO_PARAM, params);
		
		intent.putExtra(EXTRA_MODE, EXTRA_MODE_VIDEO);
		context.startActivity(intent);
	}
	
    private View mVideoPart;
    private View mAudioPart;
    
    private View mAddVideoLay;
    private View mShowVideoLay;
    
    private RecordingView mRecordView;
    private TextView mRerecordView;
    
    // 顶部状态
  	private TextView mRecordStateTip;
  	
  	// 音频部分
  	private int mAudioDuration;
	private String mAudioFilePath;
	private boolean mAudioUploadFailed;
	
	// 视频部分
	private String mVideoFilePath;
	private String mVideoCoverFile;
	private long mVideoDuration;
	private VideoConverter.Params mVideoParams;
	private boolean mVideoIsCamera;
	private boolean mVideoUploadFailed;
	
	// 视频显示部分
	private LoadingImageView mShowVidowCover;
	
	private CustomDialog mCustomDialog;
	
	// 初始定位模式
	private int mExtraMode;
	
	// 底部重传标识
	private View reUpLoadTips;
	private List<Integer> mTids;
	private SlideSwitchView mSwitchView;
	
    private int recordMode;
    
    private static final int VIDEO_MODE = 1;
    private static final int AUDIO_MODE = 2;
	    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar.hide();
		
		mTids = new LinkedList<Integer>();
		
		handleIntent(getIntent());
		
		setContentView(R.layout.activity_my_show);
		initView();
		EgmService.getInstance().addListener(mCallBack);
		
		mTids.add(EgmService.getInstance().doGetAudioVideoMode());
		
		updateViewShow();
		MediaPlayerWrapper.getInstance().doBindService(EngagementApp.getAppInstance());
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		handleIntent(intent);
		
		updateViewShow();
	}
	
	
    /** 防止闪烁，同时由于采用了获取View位置，所以必须获取焦点显示的时候才有具体position，
     *  而onCreate中全是0 */
	@Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (recordMode == EXTRA_MODE_AUDIO) {
                mSwitchView.setPositionStateInstance(Position.RIGHT);
            } else {
                mSwitchView.setPositionStateInstance(Position.LEFT);
            }
        }
    }
	
	private void handleIntent(Intent intent) {
		if (intent == null) {
			return ;
		}
		
		mExtraMode = intent.getIntExtra(EXTRA_MODE, 0);
		
		String video = intent.getStringExtra(EXTRA_VIDEO);
		if (TextUtils.isEmpty(video)) {
			return ;
		}
		
		reRecordVideo();
		
		mVideoFilePath = video;
		mVideoParams = intent.getParcelableExtra(EXTRA_VIDEO_PARAM);
		mVideoCoverFile = intent.getStringExtra(EXTRA_COVER);
		mVideoDuration = intent.getLongExtra(EXTRA_DURATION, 0);
		mVideoIsCamera = intent.getBooleanExtra(EXTRA_CAMERA, false);
		
		updateVideo();
	}
	
	/**
	 * 更新view 显示
	 */
	private void updateViewShow() {
		if (! TextUtils.isEmpty(mVideoFilePath)) {
			mShowVideoLay.setVisibility(View.VISIBLE);
			mAddVideoLay.setVisibility(View.GONE);
			
			if (! TextUtils.isEmpty(mVideoCoverFile)) {
				String url = mVideoCoverFile;
				if (! URLUtil.isNetworkUrl(url)) {
					File cover = new File(url);
					
					url = Uri.fromFile(cover).toString();
				}
				
				mShowVidowCover.setLoadingImage(url);
			}
		}
	}
	
	/**
	 * 上传视频
	 */
	private void updateVideo() {
		reRecordVideo();
		
		if (mVideoDuration <= 0) {
			return ;
		}
		
		mTids.add(EgmService.getInstance().doUpdateVideoIntroduce(
				mVideoFilePath, mVideoCoverFile, mVideoDuration, 
				mVideoIsCamera ? 1 : 0, mVideoParams));
		
		showCustomDialog(ActivityMyShow.this, getString(R.string.on_uploading));
	}
	
	/**
	 * 上传音频
	 */
	private void updateAudio() {
		reRecordAudio();
		
		if (mAudioDuration <= 0) {
			return ;
		}
		
		mTids.add(EgmService.getInstance().doUpdateAudioIntroduce(
        		mAudioFilePath, mAudioDuration));
		
        showCustomDialog(ActivityMyShow.this, getString(R.string.on_uploading));
	}

	private void initView(){
        findViewById(R.id.title_done).setOnClickListener(this);
        
//        mTabBar = findViewById(R.id.tab_bar);
        
        
        mVideoPart = findViewById(R.id.video_part);
        mAudioPart = findViewById(R.id.audio_part);
        mAddVideoLay = findViewById(R.id.add_video_lay);
        mShowVideoLay = findViewById(R.id.show_video_lay);
        mAddVideoLay.setOnClickListener(this);
        
        mShowVideoLay.findViewById(R.id.re_record_video).setOnClickListener(this);
        
        mShowVidowCover = (LoadingImageView) mShowVideoLay.findViewById(
        		R.id.video_cover);
        mShowVidowCover.setOnClickListener(this);
        
        mRecordView = (RecordingView)findViewById(R.id.audio_record_view);
        mRecordView.setOnRecordListener(mRecordListener);
        
        mRerecordView = (TextView)findViewById(R.id.re_record);
        mRerecordView.setOnClickListener(this);
        
        mRecordStateTip = (TextView)findViewById(R.id.record_state_tip);
        mRecordStateTip.setText("按住录音");
        

        
        reUpLoadTips = findViewById(R.id.reupload_tips);
        reUpLoadTips.setOnClickListener(this);
        
        mSwitchView=(SlideSwitchView)findViewById(R.id.slide_switch);
        mSwitchView.setOnStateChangerListener(new StateChangerListener() {
            
            @Override
            public void onStateChanged(boolean position) {
                changeToVideoMode(!position);
            }
        });
        
        if (mExtraMode == EXTRA_MODE_AUDIO) {
            changeToVideoMode(false);
        } else {
            changeToVideoMode(true);
        }
    }

	private void changeToVideoMode(boolean videoMode){
		if(videoMode){
	         
		     stopPlayMedia();
	           
		     mAudioPart.setVisibility(View.GONE);
		     mVideoPart.setVisibility(View.VISIBLE);
		     recordMode = VIDEO_MODE;
		} else{
			 mVideoPart.setVisibility(View.GONE);
		     mAudioPart.setVisibility(View.VISIBLE);
		     recordMode = AUDIO_MODE;
		}
		
		checkReUpload();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		mTids.clear();
		EgmService.getInstance().removeListener(mCallBack);
		closeCustomDialog();
	}
	
	private void showCustomDialog(Context context, String text) {
    	closeCustomDialog();
        mCustomDialog = new CustomDialog(context, text, null);
        mCustomDialog.show();
    }

    private void closeCustomDialog() {
        if (mCustomDialog != null) {
            mCustomDialog.dismiss();
            mCustomDialog = null;
        }
    }
    
    private void reRecordAudio() {
		mAudioUploadFailed = false;
		
		checkReUpload();
	}
    
    private void reRecordVideo() {
    	mVideoUploadFailed = false;
		
		checkReUpload();
    }
    
	private void checkReUpload() {
		if (reUpLoadTips == null) {
			return ;
		}
		
		if (recordMode == VIDEO_MODE) {
			if (mVideoUploadFailed) {
				reUpLoadTips.setVisibility(View.VISIBLE);
			}
			else {
				reUpLoadTips.setVisibility(View.INVISIBLE);
			}
		}
		else {
			if (mAudioUploadFailed) {
				reUpLoadTips.setVisibility(View.VISIBLE);
			}
			else {
				reUpLoadTips.setVisibility(View.INVISIBLE);
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.video_cover:
			ActivityVideoPlay.startActivity(this, mVideoFilePath);
			break;
		case R.id.title_done:
			mTids.add(EgmService.getInstance().doSwitchAudioVideo(recordMode));
			finish();
			break;
		case R.id.re_record:
			/**
			 * 重新录制
			 */
			mRecordView.reRecord();
			mRecordStateTip.setVisibility(View.VISIBLE);
			mRerecordView.setVisibility(View.GONE);
			mRecordStateTip.setText("按住录音");

			reRecordAudio();
			break;
			
		case R.id.re_record_video:
		case R.id.add_video_lay:
			EgmUtil.createEgmMenuDialog(this, getString(R.string.send_video),
					getResources().getStringArray(R.array.send_pub_pic_array),
					mSelectVideoListener, true).show();
			break;
			
		case R.id.reupload_tips:
			if (recordMode == VIDEO_MODE) {
				updateVideo();
			}
			else {
				updateAudio();
			}
			break;
		}
	}
	
	OnClickListener mSelectVideoListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			int which = (Integer) v.getTag();
			switch (which) {
			case 0:
				/**  拍摄视频  **/
				CamcorderActivity.startActivity(ActivityMyShow.this);
				break;
			case 1:
				/** 从相册选择视频 **/
				ActivityVideoList.startActivityForResult(ActivityMyShow.this,
						EgmConstants.REQUEST_SELECT_VIDEO,
						EgmConstants.SELEC_VIDEO_TYPE.TYPE_MY_SHOW);
				break;
			}
		}
	};
	
	 
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		
		String filePath = null;
		long duration = 0;
		
		switch (requestCode) {
		case EgmConstants.REQUEST_SELECT_VIDEO:
			/** 选择相册中的视频 **/
			if (data == null) {
				return;
			}
			
			filePath = data.getStringExtra(EgmConstants.BUNDLE_KEY.CHAT_VIDEO_PATH);
			duration = data.getLongExtra(EgmConstants.BUNDLE_KEY.CHAT_VIDEO_DURATION, 0);
			
			Intent intent = new Intent(this, VideoEditActivity.class);
			intent.putExtra("duration", String.valueOf(duration));
			intent.putExtra("path", filePath);
			startActivity(intent);
			break;
		}
	}
	
	private EgmCallBack mCallBack = new EgmCallBack() {
		
		@Override
		public void onUpdateVideoSucess(int transactionId, VideoIntroduce obj) {
			if (! mTids.remove(Integer.valueOf(transactionId))) {
				return ;
			}
			
			mVideoUploadFailed = false;
			
			closeCustomDialog();
			
			ToastUtil.showToast(ActivityMyShow.this, R.string.video_saved);
			
			checkReUpload();
		}
		
		@Override
		public void onUpdateVideoError(int transactionId, int errCode, String err) {
			if (! mTids.remove(Integer.valueOf(transactionId))) {
				return ;
			}
			
			mVideoUploadFailed = true;
			
			closeCustomDialog();
		    
			ToastUtil.showToast(ActivityMyShow.this, err);
			checkReUpload();
		}
		
		@Override
		public void onUpdateAudioSucess(int transactionId, AudioIntroduce obj) {
			if (! mTids.remove(Integer.valueOf(transactionId))) {
				return ;
			}
			
			mAudioUploadFailed = false;
			
			closeCustomDialog();
			ToastUtil.showToast(ActivityMyShow.this, R.string.audio_saved);
			
			checkReUpload();
		}

		@Override
		public void onUpdateAudioError(int transactionId, int errCode,
				String err) {
			if (! mTids.remove(Integer.valueOf(transactionId))) {
				return ;
			}
			
			mAudioUploadFailed = true;
			
			closeCustomDialog();
			
			checkReUpload();
		}

		@Override
		public void onGetAudioVideoSucess(int transactionId,
				AudioVideoSelfMode info) {
			if (! mTids.remove(Integer.valueOf(transactionId))) {
				return;
			}

			if (! TextUtils.isEmpty(info.voiceIntroduce)) {
				mRecordView.stopPlay();
				mRecordView.setPlayInitState(info.duration * 1000,
						info.voiceIntroduce);
				mRecordStateTip.setVisibility(View.INVISIBLE);
				mRerecordView.setVisibility(View.VISIBLE);
			}
			
			if (! TextUtils.isEmpty(info.videoIntroduce)) {
				StoreFile file = CacheManager.getStoreFile(info.videoIntroduce);
				if (file != null && file.exists()) {
					mVideoFilePath = file.getPath();
				}
				else {
					mVideoFilePath = info.videoIntroduce;
				}
				
				mVideoCoverFile = info.videoCover;
				
				updateViewShow();
			}
			
			if (mExtraMode == 0) {
				if (info.introduceType == 2) {
					changeToVideoMode(false);
				}
			}
		}

		@Override
		public void onGetAudioVideoError(int transactionId, int errCode,
				String err) {
			if (!mTids.remove(Integer.valueOf(transactionId))) {
				return;
			}

			ToastUtil.showToast(ActivityMyShow.this, err);
		}

	};
		
	private final OnRecordListener mRecordListener = new OnRecordListener() {
		@Override
		public void onRecordStart() {
			mRecordStateTip.setText("正在录音");
		}

		@Override
		public void onRecording(long milSec) {// 正在录制语音状态
			if (milSec >= RecordingView.LEAST_DURATION) {
				mRecordStateTip.setText(R.string.release_to_save);
			}
		}

		@Override
		public void onRecordEnd(boolean success, long duration, String filePath) {
			if (TextUtils.isEmpty(filePath)) {
				return;
			}
			
			if (success) {
				// 界面变化
				mRecordStateTip.setVisibility(View.INVISIBLE);
				mRerecordView.setVisibility(View.VISIBLE);
				// 上传语音文件
				mAudioDuration = (int) (duration / 1000);
				mAudioFilePath = filePath;

				if (FileUtil.getLength(filePath) <= 0) {
					return;
				}

				updateAudio();
			} else {
				mRecordStateTip.setText(R.string.record_time_short);
			}
		}
	};

    @Override
	protected void onStop() {
        super.onStop();
        if (mRecordView != null) {
            mRecordView.stopPlay();
        }
    };

    private void stopPlayMedia() {
        if (mRecordView != null) {
            mRecordView.stopPlay();
        }
    }
}
