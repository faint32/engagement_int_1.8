package com.netease.engagement.fragment;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityVideoPlay;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.MsgDataManager;
import com.netease.engagement.view.TimerView;
import com.netease.engagement.view.TimerView.OnTimeMaxListener;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.util.AudioUtil;

/**
 * 录制视频
 */
public class FragmentVideoRec extends FragmentBase implements SurfaceHolder.Callback{
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private Camera mCamera ;
	private Camera.Size mPreviewSize ;
	private MediaRecorder mMediaRecorder;
	
	//开始录制
	private ImageView mStartRecord;
	
	//停止录制
	private ImageView mEndRecord;
	
	// 录制完成，进入播放状态容器
	private View mPlayContainer;
	
	//前后摄像头切换
	private TextView mSwitch ;
	//标识是否切换了前后摄像头
	private boolean mSwitchFlag = false;  
	
	private TimerView mTimerView ;
	
	private File mRecAudioFile;
	
	private boolean isRecording = false ;
	boolean isFront = true;
	private boolean prepared = false ;
	
	//标识是否在录影过程中离开了页面
	private boolean isLeaveInRecording = false;
	
	// time limit
	private int mTimeLimit;
	
	// 
	private int mAudioRingerMode;
	
	public static FragmentVideoRec newInstance(){
		FragmentVideoRec fragment = new FragmentVideoRec();
		return fragment ;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(!VideoRecorderTools.getInstance().checkCameraHardware(getActivity())){
			ToastUtil.showToast(getActivity(),"当前手机没有摄像头，无法进行视频录制");
			this.getActivity().finish();
			return ;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_video_rec, container,false);
		initViews(root);
		return root ;
	}
	
	private void initViews(View root){
		mSurfaceView = (SurfaceView)root.findViewById(R.id.recview);
		mSurfaceView.setFocusable(true);
		
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		mSwitch = (TextView)root.findViewById(R.id.switch_camera);
		mSwitch.setOnClickListener(mOnClickListener);
		
		mStartRecord = (ImageView)root.findViewById(R.id.start_record);
		mStartRecord.setOnClickListener(mOnClickListener);
		
		mEndRecord = (ImageView)root.findViewById(R.id.end_record);
		mEndRecord.setOnClickListener(mOnClickListener);
		
		mTimerView = (TimerView)root.findViewById(R.id.timer_view);
		mTimerView.setOnTimeMaxListener(mOnTimeMaxListener);
		mTimerView.setMaxVideoTime(mTimeLimit);
		
		mPlayContainer = root.findViewById(R.id.start_play_container);
		
		root.findViewById(R.id.start_play).setOnClickListener(mOnClickListener);
		root.findViewById(R.id.re_record).setOnClickListener(mOnClickListener);
		root.findViewById(R.id.use_video).setOnClickListener(mOnClickListener);
		
		mStartRecord.setVisibility(View.VISIBLE);
        mEndRecord.setVisibility(View.GONE);
	}
	
	private OnTimeMaxListener mOnTimeMaxListener = new OnTimeMaxListener(){
		@Override
		public void onTimeMax() {
			stopToUpload();
		}
	};
	
	private void stopToUpload(){
		if(isRecording){
			mEndRecord.setVisibility(View.GONE);
			mSwitch.setVisibility(View.GONE);
			mTimerView.stopTiming();
			releaseMediaRecorder();
            isRecording = false;
            prepared = false ;
            
            // showDelPicDialog();
            mPlayContainer.setVisibility(View.VISIBLE);
		}
	}

	private OnClickListener mOnClickListener = new OnClickListener(){
		@Override
		public void onClick(View view) {
			switch(view.getId()){
				case R.id.switch_camera:
					mSwitchFlag = !mSwitchFlag;
					if(mRecAudioFile != null && mRecAudioFile.exists()){
						mRecAudioFile.delete();
					}
					mTimerView.resetTiming();
					releaseCamera();
					if((mCamera = VideoRecorderTools.getInstance().getNextCamera()) != null){
						try {
							mCamera.setPreviewDisplay(mSurfaceHolder);
						} catch (IOException e) {
							e.printStackTrace();
						}
						setDisplayOrientation(mCamera);
						mCamera.startPreview();
					}
					break;
				case R.id.start_record:
					mStartRecord.setVisibility(View.GONE);
                    mEndRecord.setVisibility(View.VISIBLE);
					if (prepareVideoRecoder()) {
	                    mMediaRecorder.start();
	                    mTimerView.startTiming();
	                    mSwitch.setVisibility(View.GONE);
	                    isRecording = true;
	                } else {
	                    releaseMediaRecorder();
	                    mStartRecord.setVisibility(View.VISIBLE);
	                    mEndRecord.setVisibility(View.GONE);
	                }
					break;
				case R.id.end_record:
					stopToUpload();
					break;
					
				case R.id.re_record:
					cancel();
					break;
					
				case R.id.use_video:
					showDelPicDialog();
					break;
					
				case R.id.start_play:
					if (mRecAudioFile != null) {
						ActivityVideoPlay.startActivity(getActivity(), 
								Uri.fromFile(mRecAudioFile).toString());
					}
					break;
			}
		}
	};
	
	private AlertDialog mUploadDialog ;
	private void showDelPicDialog(){
		if(mUploadDialog == null){
			LinearLayout layout = (LinearLayout) getActivity().getLayoutInflater().inflate(
					R.layout.view_delete_pics_dialog_layout,null);
			
			TextView title = (TextView)layout.findViewById(R.id.title);
			title.setText(String.format(getActivity().getResources().getString(R.string.send_video_size_tip), 
					getVideoSize()));
			
			TextView cancel = (TextView) layout.findViewById(R.id.cancel);
			cancel.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					cancel();
				}
			});
			
			TextView confirm = (TextView) layout.findViewById(R.id.ok);
			confirm.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					if (mTimerView.getTotalTime() < 1) {
						Toast.makeText(FragmentVideoRec.this.getActivity(), R.string.video_record_time_too_short, Toast.LENGTH_SHORT).show();
						cancel();
					} else {
						confirm();
					}
				}
			});
			mUploadDialog = new AlertDialog.Builder(getActivity()).setView(layout).create();
			mUploadDialog.setCancelable(false);
		}
		mUploadDialog.show();
	}
	
	private void cancel() {
		if (mRecAudioFile.exists()){
			mRecAudioFile.delete();
		}
		
		mPlayContainer.setVisibility(View.INVISIBLE);
		mTimerView.resetTiming();
		mSwitch.setVisibility(View.VISIBLE);
		try {
			mCamera.setPreviewDisplay(mSurfaceHolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		mCamera.startPreview();
		
		mStartRecord.setVisibility(View.VISIBLE);
		mEndRecord.setVisibility(View.GONE);
		
		if (mUploadDialog != null) {
			mUploadDialog.dismiss();
			mUploadDialog = null;
		}
	}
	
	private void confirm() {
		mUploadDialog.dismiss();
		Intent intent = new Intent();
		intent.putExtra(EgmConstants.EXTRA_PATH,mRecAudioFile.getPath());
		intent.putExtra(EgmConstants.EXTRA_DURATION,mTimerView.getTotalTime());
		getActivity().setResult(Activity.RESULT_OK,intent);
		getActivity().finish();
	}
	
	
	private String getVideoSize(){
		String result = null ;
		long size = 0 ;
		if(mRecAudioFile.exists()){
			size = mRecAudioFile.length();
		}
		result = bytes2kb(size);
		return result ;
	}
	
	public static String bytes2kb(long bytes){
        BigDecimal filesize = new BigDecimal(bytes);
        BigDecimal megabyte = new BigDecimal(1024 * 1024);
        float returnValue = filesize.divide(megabyte,2,BigDecimal.ROUND_UP).floatValue();
        if(returnValue > 1){
        	return (returnValue + "MB");
        }
        BigDecimal kilobyte = new BigDecimal(1024);
        returnValue = filesize.divide(kilobyte,2,BigDecimal.ROUND_UP).floatValue();
        return (returnValue + "KB");
	}
	
	private boolean prepareVideoRecoder(){
		if(prepared){
			return prepared ;
		}
		if(mCamera == null){
			ToastUtil.showToast(getActivity(),"摄像头不可用，暂时不能进行视频录制");
			getActivity().finish();
			return false ;
		}
		mMediaRecorder = new MediaRecorder();
	    mCamera.unlock();
	    mMediaRecorder.setCamera(mCamera);
	    setMeidaOriHint();

	    mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
	    mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
	    
	    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
	    
	    if(android.os.Build.VERSION.SDK_INT >= 14){
	    	mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
	    }else{
	    	mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H263);
	    }
	    
	    if(android.os.Build.VERSION.SDK_INT >= 14) {
	    	mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        }else{
            if(android.os.Build.DISPLAY != null && android.os.Build.DISPLAY.indexOf("MIUI") >= 0) {
            	mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            }else{
                mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            }
        }
	    
	    mMediaRecorder.setVideoEncodingBitRate(300000);
	    mMediaRecorder.setAudioEncodingBitRate(8000);
	    mMediaRecorder.setAudioSamplingRate(8000);
	    mMediaRecorder.setAudioChannels(1);
	    mMediaRecorder.setVideoFrameRate(15);
	    
	    if (mTimeLimit > 0) {
	    	mMediaRecorder.setMaxDuration(mTimeLimit * 1000);
	    }
	    else {
	    	mMediaRecorder.setMaxDuration(60*1000);
	    }
	    mMediaRecorder.setMaxFileSize(15*1024*1024);
	    
	    String filePath = MsgDataManager.getInstance().convertPath(
											EgmProtocolConstants.MSG_TYPE.MSG_TYPE_VIDEO, 
											String.valueOf(System.currentTimeMillis()));
	    
		mRecAudioFile = new File(filePath);
		mMediaRecorder.setOutputFile(mRecAudioFile.getAbsolutePath());
		
		mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
	    
	    try {
	        mMediaRecorder.prepare();
	    } catch (IllegalStateException e) {
	        releaseMediaRecorder();
	        return false;
	    } catch (IOException e) {
	        releaseMediaRecorder();
	        return false;
	    }
	    prepared = true ;
	    return prepared;
	}
	
	private void setMeidaOriHint(){
		int curCameraId = VideoRecorderTools.getInstance().getCameraId();
	    CameraInfo info = VideoRecorderTools.getInstance().getCameraInfo(curCameraId);
	    if(info != null){
	    	mMediaRecorder.setOrientationHint(info.orientation);
	    }
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
		if(mCamera != null){
			
			//mPreviewSize = VideoRecorderTools.getInstance().getPreviewSize(mCamera);
			//mPreviewSize = VideoRecorderTools.getInstance().getPreviewSizeFull(mCamera);
			/*if(mPreviewSize == null){
				ToastUtil.showToast(getActivity(),"全屏录制设置不可用");
				getActivity().finish();
				return ;
			}
			int preWidth = mPreviewSize.width ;
			int preHeight = mPreviewSize.height ;*/
			VideoRecorderTools.getInstance().getMaxFrameRate(mCamera);
			
			int preWidth = getActivity().getResources().getDisplayMetrics().heightPixels ;
			int preHeight = getActivity().getResources().getDisplayMetrics().widthPixels ;
			
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(preHeight ,preWidth);
			lp.addRule(RelativeLayout.CENTER_IN_PARENT);
			mSurfaceView.setLayoutParams(lp);
			mCamera.startPreview();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = VideoRecorderTools.getInstance().getCameraInstance(isFront);
		if(mCamera != null){
			try {
				mCamera.setPreviewDisplay(holder);
			} catch (IOException e) {
				e.printStackTrace();
				releaseCamera();
				ToastUtil.showToast(getActivity(),"预览创建失败，摄像头不可用");
				getActivity().finish();
				return ;
			}
			setDisplayOrientation(mCamera);
		}else{
			ToastUtil.showToast(getActivity(),"预览创建失败，摄像头不可用");
			getActivity().finish();
			return ;
		}
	}
	
	private void setDisplayOrientation(Camera camera){
		Camera.Parameters parameters = camera.getParameters();
		if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
			parameters.set("orientation", "portrait");
			camera.setDisplayOrientation(90);
		}
		camera.setParameters(parameters);
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		releaseCamera();
		releaseMediaRecorder();
	}
	
	private void releaseCamera(){
        if (mCamera != null){
        		mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
	
	private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
        	try {
	        	mMediaRecorder.stop();
	            mMediaRecorder.reset();
	            mMediaRecorder.release();
	            mMediaRecorder = null;
        	} catch (Exception e) {
        	}
        	
            if(mCamera != null){
            	try {
	            	mCamera.stopPreview();
	            	mCamera.lock();
            	} catch (Exception e) {
            	}
            }
        }
    }
	
	// 在拍摄过程中，如果突然离开页面（收到易信消息或者回home页），就先停止当前拍摄，否则回来后回崩毁。  bug fix #140648  by gzlichangjie
	@Override
	public void onPause() {
		if(isRecording) { 
			isRecording = false;
            prepared = false ;

			mTimerView.stopTiming();
			mTimerView.resetTiming();
       
            mStartRecord.setVisibility(View.GONE);
            mEndRecord.setVisibility(View.VISIBLE);
            
            mSwitch.setVisibility(View.VISIBLE);
            
            if(mRecAudioFile.exists()){
				mRecAudioFile.delete();
			}
            
            releaseMediaRecorder();
            try {
				mCamera.setPreviewDisplay(mSurfaceHolder);
			} catch (IOException e) {
				e.printStackTrace();
			}
			mCamera.startPreview();
			
			isLeaveInRecording = true;
		}
		super.onPause();
		
		mAudioRingerMode = AudioUtil.setMuteAll(getActivity(), false, mAudioRingerMode);
	}
	
	// 在拍摄过程中，如果突然离开页面（收到易信消息或者回home页），重新回来，需要判断对前后摄像头进行设置。  bug fix #140648  by gzlichangjie
	@Override
    public void onResume() {
		super.onResume();
		if(isLeaveInRecording) { 
			if(mSwitchFlag) { 
				mSwitch.postDelayed(new Runnable() {
					@Override
					public void run() {
						mSwitch.performClick();
					}
				}, 100);
			}
		}
		
		mAudioRingerMode = AudioUtil.setMuteAll(getActivity(), true, mAudioRingerMode);
	}
	

	@Override
	public void onDestroy() {
		super.onDestroy();
		mTimerView.clear();
		releaseCamera();
		releaseMediaRecorder();
	}
	
	// 从Activity中转发过来的Back键处理。
	// 按照交互要求，在拍摄中按Back键，等同与按下页面中的停止按钮。 bug fix #140783   by gzlichangjie
	public void onBackKeyDown() {
		if(isRecording) {
			mStartRecord.performClick();
		} else {
			this.getActivity().finish();
		}
	}

	public void setTimeLimit(int timeLimit) {
		mTimeLimit = timeLimit;
	}
}
