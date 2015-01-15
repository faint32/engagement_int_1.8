package com.netease.android.video.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.android.activity.VideoEditActivity;
import com.netease.android.activity.VideoPreviewActivity;
import com.netease.android.camera.CameraErrorCallback;
import com.netease.android.camera.CameraHolder;
import com.netease.android.camera.CameraSettings;
import com.netease.android.camera.Thumbnail;
import com.netease.android.camera.ThumbnailHolder;
import com.netease.android.camera.Util;
import com.netease.android.service.PendingMediaStore;
import com.netease.android.util.ActivityUtils;
import com.netease.android.util.DpAndPxUtils;
import com.netease.android.video.ClipStackManager;
import com.netease.android.video.ClipStackManager.ClipStackManagerChangeListener;
import com.netease.android.video.camera.ClipRecorderStateListener;
import com.netease.android.video.camera.ClipRecorderStateListener.ClipRecorderState;
import com.netease.android.video.model.Clip;
import com.netease.android.video.model.Clip.ClipState;
import com.netease.android.video.model.pendingmedia.ClipInfo;
import com.netease.android.video.model.pendingmedia.PendingMedia;
import com.netease.android.video.ui.VideoPopup.Config;
import com.netease.android.video.util.CamcorderUtil;
import com.netease.android.video.util.VideoEditUtil;
import com.netease.android.video.util.VideoFeatureUtil;
import com.netease.android.widget.dialog.LofterProgressDialog;
import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentBase;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class CamcorderFragment extends FragmentBase implements
		MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener,
		MultiClipVideoRecorder, ClipStackManagerChangeListener,OnClickListener {
	private static final String TAG = "CamcorderFragment";
	private static final int MESSAGE_CLIP_PROGRESS = 1;
	private static final int MESSAGE_CLIP_PROGRESS_DELAY = 40;
	private static final int MESSAGE_CLEAR_SCREEN_DELAY = 2;
	private static final int CHECK_DISPLAY_ROTATION = 4;
	private static final int MESSAGE_CLEAR_FOCUS_INDICATOR = 5;
	private static final int MESSAGE_DISMISS_NUX = 15;
	private static final int MESSAGE_SHOW_TAP_TO_RECORD_NUX = 16;
	private int mNumberOfCameras;
	private Camera mCamera;
	private CamcorderPreviewLayout mCamcorderPreviewLayout;
//	private CamcorderBlinker mBlinker;
	boolean mPreviewing;
	private VideoShutterButton mVideoShutterButton;
	private long mOnResumeTime;
	private int mOriginalRingerMode;
	private boolean mPaused;
	private SharedPreferences mPreferences;
	protected CamcorderProfile mProfile;
	private CameraErrorCallback mErrorCallback = new CameraErrorCallback();
	private Handler mHandler;
	private Camera.Parameters mParameters;
	private int mDesiredPreviewHeight;
	private int mDesiredPreviewWidth;
	private ImageView switch_camera_button;
	private MediaRecorder mMediaRecorder;
	private boolean mMediaRecorderRecording;
	private boolean mHasPerformedFocus;
	private ClipRecorderState mClipRecorderState;
	private List<ClipRecorderStateListener> mClipRecorderStateListeners;
	private ClipStackManager mClipStackManager = new ClipStackManager();
	private ClipStackView mClipStackView;
	private TextView mCancelButton;
	private PendingMedia mCurrentSession;
	private PreviewSurfaceView mPreviewSurfaceView;
	private Callback mSurfaceViewCallback;
	private int mDisplayRotation;
	private int mCameraDisplayOrientation;
	private boolean mStartPreviewAfterInitialized = false;
	private ImageView mSwitchCameraButton;
	private boolean mViewsInitialized;
	private String mVideoFilePath;
	private VideoPopup mNuxPopup;
	private LofterProgressDialog progressDialog;
	private Thumbnail mThumbnail;
	protected AsyncTask<Void, Void, Thumbnail> mLoadThumbnailTask;
//	private String queueId;// 日志发布队列id
	private boolean toNexting;
	

	public CamcorderFragment() {
		mErrorCallback = new CameraErrorCallback();
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MESSAGE_CLIP_PROGRESS:
					mClipStackManager.updateClip();
					if (!mClipStackManager.isFull()) {
						mHandler.sendEmptyMessageDelayed(MESSAGE_CLIP_PROGRESS,
								MESSAGE_CLIP_PROGRESS_DELAY);
					}
					break;
				case MESSAGE_CLEAR_SCREEN_DELAY:
					getActivity().getWindow().clearFlags(
							WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
					break;
				case MESSAGE_CLEAR_FOCUS_INDICATOR:
//					((FocusIndicatorView) getView().findViewById(
//							R.id.focus_indicator)).clear();
					break;
				case MESSAGE_DISMISS_NUX:
					if (mNuxPopup != null)
						mNuxPopup.dismiss();
					break;
				case CHECK_DISPLAY_ROTATION:
					if ((Util.getDisplayRotation(getActivity()) != mCameraDisplayOrientation)
							&& !mMediaRecorderRecording)
						startPreview();
					if (SystemClock.uptimeMillis() - mOnResumeTime < 5000L) {
						mHandler.sendEmptyMessageDelayed(
								CHECK_DISPLAY_ROTATION, 100L);
					}
					break;
				case MESSAGE_SHOW_TAP_TO_RECORD_NUX:
					if (getView().getWindowToken() == null) {
						sendEmptyMessageDelayed(MESSAGE_SHOW_TAP_TO_RECORD_NUX,
								500L);
					} else {
						showTapToRecordPopup();
					}
					break;
				}
			}
		};
		mPreviewing = false;
		mProfile = null;
		mPaused = false;
		mOriginalRingerMode = 0;
	}

	@Override
	public boolean onBackPressed() {
        getActivity().finish();
        return true;
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		mClipStackManager.saveInstanceState(bundle);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!VideoFileUtil.isFileSystemAvailable(getActivity())) {
			ActivityUtils.showToastWithIcon(getActivity(), "无法启动录像机", false);
			getActivity().finish();
		}
		mNumberOfCameras = getNumberOfCameras();
		mPreferences = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		mClipStackManager.addClipStackListener(this);
		Editor edit = mPreferences.edit();
		edit.putInt("media_mode", 0);
		edit.remove(CameraSettings.KEY_FLASH_MODE);
		edit.commit();
		mClipRecorderState = ClipRecorderState.STOPPED;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setRetainInstance(true);
		final View convertView = inflater.inflate(R.layout.video_fragment_camcorder,
				container, false);

		switch_camera_button = (ImageView) convertView
				.findViewById(R.id.switch_camera_button);

//		convertView.findViewById(R.id.black_bg).setBackgroundColor(
//				getResources().getColor(R.color.black));

		mCamcorderPreviewLayout = (CamcorderPreviewLayout) convertView
				.findViewById(R.id.ics_preview);
		mVideoShutterButton = (VideoShutterButton) convertView
				.findViewById(R.id.fragment_camera_shutter_button);
		mVideoShutterButton.setClipStackManager(mClipStackManager);
		mVideoShutterButton.setEnabled(false);
		mVideoShutterButton.setOnClickListener(this);

		mCancelButton = (TextView) convertView.findViewById(R.id.button_cancel);
		mCancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().onBackPressed();
			}
		});
		setDisplayOrientation();
		return convertView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		view.post(new Runnable() {

			@Override
			public void run() {
				initializeViews(getView());
				if (mStartPreviewAfterInitialized) {
					startPreview();
					mStartPreviewAfterInitialized = false;
				}
				mViewsInitialized = true;
			}
		});
	}

	public void recoverSession(List<File> files) {
		final ArrayList<Clip> clips = new ArrayList<Clip>();
		int cameraId = getCameraId();
		int remainingDuration = mClipStackManager.getRemainingDuration();
		for (File file : files) {
			try {
				long clipDuration = VideoFileUtil.getClipDurationMillis(file);
				if ((clipDuration > 0L) && (clipDuration <= remainingDuration)) {
					if (remainingDuration - clipDuration <= 300L) {
						clipDuration = remainingDuration;
					}
					clips.add(new Clip(cameraId, clipDuration, file.getPath()));
					remainingDuration = (int) (remainingDuration - clipDuration);
				}
			} catch (Exception localException) {
			}
		}
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				try {
					mClipStackManager.restoreClips(clips);
				} catch (Exception e) {
					startNewSession();
				}
			}
		});
	}

	public void initializeViews(View view) {
		configureBackspaceButton();
		if (VideoFileUtil.isFileSystemAvailable(getActivity())) {
			new AsyncTask<String, Void, Boolean>() {

				@Override
				protected Boolean doInBackground(String... params) {
//					if (!mClipStackManager.hasRecordedClips()) {
//						PendingMedia pendingMedia = PendingMediaStore
//								.getInstance().getAbandonedSession();
//						List<File> list = VideoFileUtil
//								.getPreviousRecordingForRestoringSession(
//										pendingMedia, getActivity());
//						if (list.size() > 0) {
//							try {
//								recoverSession(list);
//								mCurrentSession = pendingMedia;
//								return true;
//							} catch (Exception e) {
//								Log.e(TAG, "Failed to recover clips :(", e);
//							}
//						}
//						Log.v(TAG, list.size()
//								+ " clips available. Trying to recover.");
//					}
					return false;
				}

				@Override
				protected void onPostExecute(Boolean result) {
					super.onPostExecute(result);
					if (!result) {
						startNewSession();
					}
					PendingMediaStore.getInstance().clearUnusedSessions();
					if ((mVideoShutterButton != null)
							&& (!mClipStackManager.isAlmostFull()))
						mVideoShutterButton.setEnabled(true);
				}
			}.execute(new String[0]);
		}
		initializeSurfaceView();
		mClipStackView = ((ClipStackView) view
				.findViewById(R.id.clip_stack_view));
		mClipStackView.setClipStack(mClipStackManager.getClipStack());
		mClipStackManager.addClipStackListener(mClipStackView);
		mClipStackManager.addClipStackListener(mVideoShutterButton);
		mErrorCallback.setActivity(getActivity());
		mClipRecorderStateListeners = new ArrayList<ClipRecorderStateListener>();
		mClipRecorderStateListeners.add(mVideoShutterButton);
		mSwitchCameraButton = (ImageView) view
				.findViewById(R.id.switch_camera_button);
		mSwitchCameraButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				switchCamera();
			}
		});
		ImageView miniClip = (ImageView) view
				.findViewById(R.id.minimum_clip_length_image);
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) miniClip
				.getLayoutParams();
		layoutParams.setMargins(getMinVideoIndicatorXPos(), 0, 0, 0);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		miniClip.setLayoutParams(layoutParams);
		miniClip.setVisibility(View.VISIBLE);
	}

	private void gotoNext() {
		if (!toNexting) {
			toNexting = true;
			stopVideoRecording();
			PendingMedia[] medias = new PendingMedia[1];
			medias[0] = mCurrentSession;
			new ProcessMoviesTask().execute(medias);
		}
	}

	private void initializeSurfaceView() {
		mPreviewSurfaceView = (PreviewSurfaceView) (getView()
				.findViewById(R.id.surfaceview));
		if (mSurfaceViewCallback == null) {
			mSurfaceViewCallback = new Callback() {

				@Override
				public void surfaceDestroyed(SurfaceHolder holder) {

				}

				@Override
				public void surfaceCreated(SurfaceHolder holder) {
					rejustBottomBar();
					// startPreview();
				}

				@Override
				public void surfaceChanged(SurfaceHolder holder, int format,
						int width, int height) {
					if (holder.getSurface() == null) {
						Log.d(TAG, "holder.getSurface() == null");
						return;
					}
					Log.v(TAG, "surfaceChanged. w=" + width + ". h=" + height);
					// The mCameraDevice will be null if it fails to connect to
					// the camera
					// hardware. In this case we will show a dialog and then
					// finish the
					// activity, so it's OK to ignore it.
					if (mCamera == null)
						return;

					// Sometimes surfaceChanged is called after onPause or
					// before onResume.
					// Ignore it.
					if (mPaused || getActivity().isFinishing())
						return;
					if (!mPreviewing) {
						startPreview();
					} else {
						if (Util.getDisplayRotation(getActivity()) != mDisplayRotation)
							setDisplayOrientation();
						if (holder.isCreating())
							try {
								mCamera.setPreviewDisplay(holder);
							} catch (IOException e) {
								e.printStackTrace();
							}
					}
				}
			};
		}
		mPreviewSurfaceView.getHolder().addCallback(mSurfaceViewCallback);
		getView().findViewById(R.id.surfaceview_frame).setVisibility(
				View.VISIBLE);
		getView().findViewById(R.id.ics_preview).setVisibility(View.VISIBLE);
//		getView().findViewById(R.id.non_ics_preview).setVisibility(View.GONE);
		rejustBottomBar();// 3.0系统以下需要在这边调用
	}

	private void rejustBottomBar() {
		int height = (getView().findViewById(R.id.camcorder_root).getHeight()
				- getView().findViewById(R.id.surfaceview).getWidth() 
				- getResources().getDimensionPixelSize(R.dimen.video_record_top_bar)
				- getResources().getDimensionPixelSize(R.dimen.video_record_bottom_bar));
		getView().findViewById(R.id.surfaceview_bottom_bar).getLayoutParams().height = height;
		getView().findViewById(R.id.surfaceview_frame).requestLayout();
	}

	@Override
	public void onPause() {
		Log.d(TAG, "onPause");
		onPauseBeforeSuper();
		super.onPause();
		onPauseAfterSuper();
	}

	public void onPauseBeforeSuper() {
		mPaused = true;
		if (mNuxPopup != null && mNuxPopup.isShowing()) {
			mNuxPopup.dismiss();
		}
		mHandler.removeCallbacksAndMessages(null);
		if (mMediaRecorderRecording) {
			onStopVideoRecording();
		} else {
			closeCamera();
		}
		// this.mGLRootView.onPause();
		releasePreviewResources();
//		getView().findViewById(R.id.black_bg).setVisibility(View.VISIBLE);
		resetScreenOn();
		setMuteAll(false);
	}

	private void onPauseAfterSuper() {
		if (mLoadThumbnailTask != null) {
			mLoadThumbnailTask.cancel(true);
			mLoadThumbnailTask = null;
		}
	}

	private void onStopVideoRecording() {
		stopVideoRecording();
	}

	private void releasePreviewResources() {
		/*
		 * CameraScreenNail cameraScreenNail = (CameraScreenNail)
		 * this.mCameraScreenNail; if (cameraScreenNail.getSurfaceTexture() !=
		 * null) cameraScreenNail.releaseSurfaceTexture();
		 */
	}

	private void resetScreenOn() {
		mHandler.removeMessages(MESSAGE_CLEAR_SCREEN_DELAY);
		getActivity().getWindow().clearFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	public void onResume() {
		onResumeBeforeSuper();
		super.onResume();
		onResumeAfterSuper();
//		configureLibraryButton();
		configureBackspaceButton();
//		getLastThumbnail();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mClipStackManager.removeClipStackListener(this);
//		stopWakeLock();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mSurfaceViewCallback = null;
		mPreviewSurfaceView = null;
		mClipRecorderStateListeners = null;
		mClipStackManager.removeClipStackListener(mClipStackView);
		mClipStackManager.removeClipStackListener(mVideoShutterButton);
		if (mClipStackView != null)
			mClipStackView.destroyStackView();
		mCancelButton = null;
		mClipStackView = null;
		mVideoShutterButton = null;
		if (mSwitchCameraButton != null)
			mSwitchCameraButton.clearAnimation();
		mSwitchCameraButton = null;
		mCamcorderPreviewLayout = null;
		mHasPerformedFocus = false;
		mParameters = null;
	}

	public void onResumeAfterSuper() {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (mCamcorderPreviewLayout != null) {
							// mCamcorderPreviewLayout.findViewById(R.id.black_bg)
							// .setVisibility(View.GONE);
							Log.d(TAG, "fade in");
						}
					}
				});
			}
		};
		new Timer().schedule(task, 300L);
		configureSwitchCameraButton();
		if (!mPreviewing) {
			openCamera();
			readVideoPreferences();
			resizeForPreviewAspectRatio();
			if (mViewsInitialized) {
				startPreview();
			} else {
				mStartPreviewAfterInitialized = true;
			}
		} else {
			mOnResumeTime = SystemClock.uptimeMillis();
			mHandler.sendEmptyMessageDelayed(CHECK_DISPLAY_ROTATION, 100L);
		}
		keepScreenOnAwhile();
		Log.d(TAG, "onResumeAfterSuper");
	}

	private void keepScreenOn() {
		mHandler.removeMessages(MESSAGE_CLEAR_SCREEN_DELAY);
		getActivity().getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	private void keepScreenOnAwhile() {
		mHandler.removeMessages(MESSAGE_CLEAR_SCREEN_DELAY);
		getActivity().getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		mHandler.sendEmptyMessageDelayed(MESSAGE_CLEAR_SCREEN_DELAY, 120000L);
	}

	private void configureSwitchCameraButton() {
		if (switch_camera_button != null) {
			switch_camera_button.clearAnimation();
			if (mNumberOfCameras <= 1) {
				switch_camera_button.setVisibility(View.GONE);
			} else {
				if (mClipStackManager.size() == 0) {
					switch_camera_button.setVisibility(View.VISIBLE);
				} else {
					switch_camera_button.setVisibility(View.GONE);
				}
			}
		}
	}



	private void configureBackspaceButton() {
		
	}

	public void onResumeBeforeSuper() {
		mOriginalRingerMode = ((AudioManager) getActivity().getSystemService(
				"audio")).getRingerMode();
		setMuteAll(true);
		mPaused = false;
		toNexting = false;
		// mGLRootView.onResume();
	}

	private void setMuteAll(boolean state) {
		AudioManager audiomanager = (AudioManager) getActivity()
				.getSystemService(Context.AUDIO_SERVICE);
		audiomanager.setStreamSolo(AudioManager.STREAM_SYSTEM, state);
		int i = mOriginalRingerMode;
		if (state)
			i = 0;
		if (i != audiomanager.getRingerMode())
			audiomanager.setRingerMode(i);
		audiomanager.setStreamMute(AudioManager.STREAM_SYSTEM, state);
	}

	@Override
	public void onInfo(MediaRecorder mr, int what, int extra) {
		if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {// 自动拍满8秒
			gotoNext();
		}
	}

	@Override
	public void onError(MediaRecorder mr, int what, int extra) {

	}

	@TargetApi(9)
	private static int getNumberOfCameras() {
		return Camera.getNumberOfCameras();
	}

	private void openCamera() {
		try {
			mCamera = Util.openCamera(getActivity(), getCameraId());
		} catch (Exception exception) {
			ActivityUtils.showToastWithIcon(getActivity(), "无法连接到相机", false);
			getActivity().finish();
		}
	}

	private int getCameraId() {
		return CameraSettings.readPreferredCameraId(mPreferences);
	}

	private void readVideoPreferences() {
		int cameraId = getCameraId();
		if ((Build.MODEL.equals("Nexus 4"))
				&& (false)) {
			Log.d(TAG, "Profile selected: 720P.");
			mProfile = CamcorderProfile.get(cameraId,
					CamcorderProfile.QUALITY_720P);
		} else {
			mProfile = CamcorderUtil
					.selectBestSupportedCamcorderProfile(cameraId);
		}
		getDesiredPreviewSize();
		if (Build.BOARD.equals("smdk4x12") || (Build.BOARD.startsWith("DB85")))
			mProfile.audioChannels = 2;
		if (Build.VERSION.SDK_INT > 10
				&& (VideoFeatureUtil.needsLegacyRendering())
				&& (!CamcorderUtil.isExceptionForAAC()))
			mProfile.audioCodec = 3;
	}

	private void resizeForPreviewAspectRatio() {
		double ratio = (double) mProfile.videoFrameWidth
				/ (double) mProfile.videoFrameHeight;
		mCamcorderPreviewLayout.setAspectRatio(ratio);
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void setCameraParameters() {
		mParameters.setPreviewSize(mDesiredPreviewWidth, mDesiredPreviewHeight);
		mParameters.setPreviewFrameRate(mProfile.videoFrameRate);
		CamcorderUtil.setWhiteBalance(mParameters);
		CamcorderUtil.setFocusModeForCamera(mParameters);
//		setFlashMode();
		if (Build.VERSION.SDK_INT >= 14) {
			mParameters.setRecordingHint(true);
		}
		if (Build.VERSION.SDK_INT >= 15)
			setStabilizationParams();
		Log.d(TAG, "Setting camera parameters");
		mCamera.setParameters(mParameters);
		mParameters = mCamera.getParameters();
		updateCameraScreenNailSize(mDesiredPreviewWidth, mDesiredPreviewHeight);
	}

	@TargetApi(15)
	private void setStabilizationParams() {
		if ((mParameters.isVideoStabilizationSupported()))
			mParameters.setVideoStabilization(false);
	}


	private void updateCameraScreenNailSize(int previewWidth, int previewHeight) {
		if (mCameraDisplayOrientation % 180 != 0) {
			int tmp = previewHeight;
			previewHeight = previewWidth;
			previewWidth = tmp;
		}
//		 int j = mPreviewSurfaceView.getWidth();
//		 int k = mPreviewSurfaceView.getHeight();
//		if ((j != previewWidth) || (k != previewHeight)) {
//			mPreviewSurfaceView.setSize(previewWidth, previewHeight);
//			mPreviewSurfaceView.forceLayout();
//		}
		// if (localCameraScreenNail.getSurfaceTexture() == null)
		// localCameraScreenNail.acquireSurfaceTexture();
		// }
	}

	boolean hasSurfaceTexture() {
		return false;
	}

	private void startPreview() {
		Log.v(TAG, "startPreview");
		mCamera.setErrorCallback(mErrorCallback);
		if (mPreviewing)
			stopPreview();
		setDisplayOrientation();
		mCamera.setDisplayOrientation(90);
		setCameraParameters();
//		clearTapToFocusState();
		try {
			if (hasSurfaceTexture()) {
				// SurfaceTexture localSurfaceTexture = ((CameraScreenNail)
				// this.mCameraScreenNail)
				// .getSurfaceTexture();
				// mCamera.setPreviewTexture(localSurfaceTexture);
			} else {
				mCamera.setPreviewDisplay(mPreviewSurfaceView.getHolder());
				mCamera.setDisplayOrientation(CamcorderUtil
						.getDisplayOrientation(mDisplayRotation, getCameraId()));
			}
			mCamera.startPreview();
			mPreviewing = true;
		} catch (Throwable throwable) {
			// if (getCameraId() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			// mHandler.postDelayed(new Runnable() {
			// @Override
			// public void run() {
			// mCamera.startPreview();
			// mPreviewing = true;
			// }
			// }, 100l);
			// } else {
			closeCamera();
			throw new RuntimeException("startPreview failed", throwable);
			// }
		}
	}

	private void stopPreview() {
		mCamera.stopPreview();
		mPreviewing = false;
	}

	private void setDisplayOrientation() {
		mDisplayRotation = Util.getDisplayRotation(getActivity());
		mCameraDisplayOrientation = CamcorderUtil.getDisplayOrientation(
				mDisplayRotation, getCameraId());
	}

	private void closeCamera() {
		Log.v(TAG, "closeCamera");
		if (mCamera == null) {
			Log.v(TAG, "already stopped");
		} else {
			CameraHolder.instance().release();
			mCamera.setZoomChangeListener(null);
			mCamera.setErrorCallback(null);
			mCamera = null;
			mPreviewing = false;
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void getDesiredPreviewSize() {
		mParameters = mCamera.getParameters();
		if (Build.VERSION.SDK_INT >= 11 && mParameters != null) {
			if (mParameters.getSupportedVideoSizes() == null) {
				mDesiredPreviewWidth = mProfile.videoFrameWidth;
				mDesiredPreviewHeight = mProfile.videoFrameHeight;
			} else {
				List<Size> sizes = mParameters.getSupportedPreviewSizes();

				Camera.Size preferred = CamcorderUtil
						.getCorrectedPreferredPreviewSizeForVideo(mParameters,
								sizes);
				int product = preferred.width * preferred.height;
				Iterator<Size> iterator = sizes.iterator();
				while (iterator.hasNext()) {
					Camera.Size size = iterator.next();
					if (size.height * size.width > product)
						iterator.remove();
				}
				Camera.Size optimalSize = Util.getOptimalPreviewSize(
						getActivity(), sizes, (double) mProfile.videoFrameWidth
								/ mProfile.videoFrameHeight);
				mDesiredPreviewWidth = optimalSize.width;
				mDesiredPreviewHeight = optimalSize.height;
			}
		} else {
			mDesiredPreviewWidth = mProfile.videoFrameWidth;
			mDesiredPreviewHeight = mProfile.videoFrameHeight;
		}
		Log.v(TAG, "mDesiredPreviewWidth=" + mDesiredPreviewWidth
				+ ". mDesiredPreviewHeight=" + mDesiredPreviewHeight);
	}

	private boolean stopVideoRecording() {
		boolean isStop = false;
		Log.v(TAG, "stopVideoRecording");
		if ((mMediaRecorderRecording) && (mMediaRecorder != null)) {
			try {
				mMediaRecorder.setOnErrorListener(null);
				mMediaRecorder.setOnInfoListener(null);
				long l = System.currentTimeMillis();
				mMediaRecorder.stop();
				CamcorderUtil.updateStopRecordingDelay(
						(int) (System.currentTimeMillis() - l), mPreferences);
				if (Build.VERSION.SDK_INT > 10)
					mCamera.reconnect();
				lockExposureIfCustomFocus(false);
				Log.v(TAG,
						"stopVideoRecording: Setting current video filename: "
								+ mVideoFilePath);
				mClipStackManager.setVideoFile(mVideoFilePath);
			} catch (Exception e) {
				Log.e(TAG, "stop fail", e);
				if (mVideoFilePath != null)
					VideoFileUtil.deleteFileAsync(mVideoFilePath);
				isStop = true;
			}
			mMediaRecorderRecording = false;
			if ((!isStop) && (!mClipStackManager.isFull()))
				correctCurrentClipView();
			if (mPaused)
				closeCamera();
			keepScreenOnAwhile();
			releaseMediaRecorder();
			if (!mPaused)
				mCamera.lock();
		}
		if (!mPaused)
			mParameters = mCamera.getParameters();
		mClipStackManager.checkLastClip();
		setClipRecorderState(ClipRecorderState.STOPPED);
		return isStop;
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void lockExposureIfCustomFocus(boolean paramBoolean) {
		if ((mHasPerformedFocus) && (CamcorderUtil.supportsAutoFocus())) {
			mParameters = mCamera.getParameters();
			mParameters.setAutoExposureLock(paramBoolean);
			mCamera.setParameters(mParameters);
		}
	}

	private void releaseMediaRecorder() {
		Log.v(TAG, "Releasing media recorder.");
		if (mMediaRecorder != null) {
			VideoFileUtil.cleanupEmptyFileAsync(mVideoFilePath);
			mMediaRecorder.reset();
			mMediaRecorder.release();
			mMediaRecorder = null;
		}
		mVideoFilePath = null;
	}

	public void correctCurrentClipView() {
		try {
			int realDuration = (int) VideoFileUtil
					.getClipDurationMillis(mVideoFilePath);
			int remainDuration = mClipStackManager.getRemainingDuration();
			long curDuration = mClipStackManager.getCurrentClip().getDuration();
			if (Math.min(remainDuration, curDuration
					+ (remainDuration - realDuration)) <= 300L)
				mClipStackManager.getCurrentClip().setDuration(
						curDuration + remainDuration);
			else
				mClipStackManager.getCurrentClip().setDuration(realDuration);
		} catch (Exception exception) {
			ActivityUtils.showToastWithIcon(getActivity(), "未知错误", false);
			cancelClip();
		}
	}

	private void setClipRecorderState(ClipRecorderState clipRecorderState) {
		mClipRecorderState = clipRecorderState;
		configureBackspaceButton();
		if (mClipRecorderStateListeners != null) {
			for (ClipRecorderStateListener listener : mClipRecorderStateListeners)
				listener.onClipRecorderStateChange(mClipRecorderState);
		}
	}

	public ClipRecorderState getClipRecorderState() {
		return mClipRecorderState;
	}

	@Override
	public void cancelClip() {
		mClipStackManager.deleteLastClip();
	}

	@Override
	public void endClip(boolean isFull) {
		mHandler.removeMessages(MESSAGE_CLIP_PROGRESS);
		setClipRecorderState(ClipRecorderState.STOPPING);
		mClipStackManager.finishClip();
		long delay = 300L;
		if (isFull) {
			delay = 1000L;
		}
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				stopVideoRecording();
			}
		}, delay);
	}

	@Override
	public void startClip() {
		int delayMillis = Math
				.max(0, 800 - CameraSettings
						.getStopRecordingDelayAverage(mPreferences));
		mClipStackManager.addNewClip(delayMillis);
		setClipRecorderState(ClipRecorderState.PREPARING);
		if (mClipRecorderState != ClipRecorderState.RECORDING) {
			startRecording();
			Log.v(TAG, "markStartOfRecording");
			setClipRecorderState(ClipRecorderState.RECORDING);
		}

	}

	public void startRecording() {
		Log.v(TAG, "startVideoRecording");
		lockExposureIfCustomFocus(true);
		initializeRecorder();
		if (mMediaRecorder != null) {
			pauseAudioPlayback();
			prepareShutter();
			try {
				mMediaRecorder.start();
				// mClipStackManager.getCurrentClip().resetStartTime();
				mMediaRecorderRecording = true;
				mHandler.sendEmptyMessageDelayed(MESSAGE_CLIP_PROGRESS, 40);
				keepScreenOn();
			} catch (RuntimeException runtimeException) {
				Log.e(TAG, "Could not start media recorder. ", runtimeException);
				releaseMediaRecorder();
				try {
					mCamera.reconnect();
				} catch (IOException exception) {
					Log.e(TAG, "Could not reconnect camera.", exception);
				}
			}
		} else {
			Log.e(TAG, "Fail to initialize media recorder");
		}

	}

	public void switchCamera() {
		int i = 1;
		if (CameraSettings.readPreferredCameraId(mPreferences) == i)
			i = 0;
		CameraSettings.writePreferredCameraId(mPreferences, i);
		closeCamera();
		openCamera();
		readVideoPreferences();
		startPreview();
		resizeForPreviewAspectRatio();
	}

	private void prepareShutter() {
		mVideoShutterButton.setEnabled(false);
	}

	private void pauseAudioPlayback() {
		Intent localIntent = new Intent("com.android.music.musicservicecommand");
		localIntent.putExtra("command", "pause");
		getActivity().sendBroadcast(localIntent);
	}

	private void initializeRecorder() {
		Log.v(TAG, "initializeRecorder");
		if (mCamera != null) {
			try {
				mMediaRecorder = new MediaRecorder();

				// 设置视频方向--begin
				android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
				android.hardware.Camera.getCameraInfo(getCameraId(), info);
				// 魅族特殊处理
				if("M040".equalsIgnoreCase(android.os.Build.MODEL)){
					mMediaRecorder.setOrientationHint(mCameraDisplayOrientation);
				}else{
					mMediaRecorder.setOrientationHint(info.orientation);
				}
				// 设置视频方向--end

				mMediaRecorder.setOnErrorListener(this);
				mMediaRecorder.setOnInfoListener(this);
				setupMediaRecorderPreviewDisplay();
				mCamera.unlock();
				mMediaRecorder.setCamera(mCamera);
				mMediaRecorder
						.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
				mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
				mMediaRecorder.setProfile(mProfile);
				mVideoFilePath = VideoFileUtil.generateVideoFilename(
						mCurrentSession.getVideoSessionName(), getActivity());
				mMediaRecorder.setOutputFile(mVideoFilePath);
				mMediaRecorder.setMaxDuration(mClipStackManager
						.getRemainingDuration());
				mMediaRecorder.prepare();
			} catch (IOException ioexception) {
				StringBuilder sb = (new StringBuilder()).append(
						"prepare failed for ").append(mVideoFilePath);
				Log.e(TAG, sb.toString(), ioexception);
				releaseMediaRecorder();
				Log.e(TAG, "Couldn't unlock camera");
				throw new RuntimeException(ioexception);
			}
		}
	}

	private void setupMediaRecorderPreviewDisplay() {
		if (!hasSurfaceTexture()) {
			if(Build.VERSION.SDK_INT < 16){
				stopPreview();
			}
			mPreviewing = true;
			mMediaRecorder.setPreviewDisplay(mPreviewSurfaceView.getHolder()
					.getSurface());
		}
	}

	@Override
	public void onClipAdded(Clip clip) {
		configureSwitchCameraButton();
//		configureLibraryButton();
		configureBackspaceButton();
	}

	@Override
	public void onClipChanged(Clip clip, ClipState clipState) {
		configureBackspaceButton();
	}

	@Override
	public void onClipDurationChanged(Clip clip) {
		if ((clip.getState() == ClipState.RECORDING)
				&& (mClipStackManager.isFull()))
			endClip(true);
	}

	@Override
	public void onClipRemoved(Clip clip) {
		configureSwitchCameraButton();
//		configureLibraryButton();
		if (clip.getState() == ClipState.INVALID)
			showTapToRecordPopup();
		configureBackspaceButton();
	}

	@Override
	public void onClipStackFull() {
//		showTapToContinuePopup();
		gotoNext();
	}

	public void showTapToRecordPopup() {
		showPopup(Config.TAP_TO_RECORD, Gravity.BOTTOM | Gravity.CENTER, 0,
				(int) DpAndPxUtils.dip2px(104));
		mPreferences.edit().putBoolean("show_tap_to_record_nux", true).commit();
	}

	public void showTapToContinuePopup() {
		Config config = Config.TAP_TO_CONTINUE;
		if (mNuxPopup == null)
			mNuxPopup = new VideoPopup(getActivity(), config);
		int y = DpAndPxUtils.getScreenHeightPixels()
				- DpAndPxUtils.getScreenWidthPixels()
				+ DpAndPxUtils.dip2px(4);
		showPopup(config, Gravity.BOTTOM | Gravity.RIGHT, 0, y);
	}

	public void showMinVideoLengthPopup() {
		int x = (int) DpAndPxUtils.dip2px(35);
		int y = DpAndPxUtils.getScreenHeightPixels()
				- DpAndPxUtils.getScreenWidthPixels()
				+ DpAndPxUtils.dip2px(4);
		showPopup(Config.MIN_VIDEO_LENGTH, Gravity.BOTTOM | Gravity.LEFT,
				getMinVideoIndicatorXPos() - x - 1, y);
	}

	private void showPopup(Config config, int gravity, int x, int y) {
		if (mNuxPopup == null)
			mNuxPopup = new VideoPopup(getActivity(), config);
		dismissOtherPopups(config);
		if (!mNuxPopup.isShowing()) {
			View container = getView().findViewById(R.id.camcorder_root);
			mNuxPopup
					.setAnimationStyle(R.style.camcorder_popup_animation_style);
			mNuxPopup.showAtLocation(container, gravity, x, y);
			mHandler.sendEmptyMessageDelayed(MESSAGE_DISMISS_NUX, 2000L);
		}
	}

	private void dismissOtherPopups(Config config) {
		if (mNuxPopup.getConfig() != config) {
			mNuxPopup.dismiss();
			mHandler.removeMessages(MESSAGE_DISMISS_NUX);
			mNuxPopup = new VideoPopup(getActivity(), config);
		}
	}

	private void startNewSession() {
		Log.v(TAG, "Starting new session");
		if ((mCurrentSession != null) || areVideoDirectoriesReady()) {
			mClipStackManager.setHasImportedClips(false);
			mCurrentSession = PendingMedia.createVideo(String.valueOf(System
					.nanoTime()));
			mCurrentSession.setVideoSessionName(VideoFileUtil
					.generateRecordingSessionName(
							mCurrentSession.getVideoSessionName(),
							getCameraId(), getActivity()));
			PendingMediaStore.getInstance().put(
					mCurrentSession.getVideoSessionName(), mCurrentSession);
			// PendingMediaStoreSerializer.getInstance().serializeAsync();
		}
	}

	private boolean areVideoDirectoriesReady() {
		try {
			VideoFileUtil.createVideoDirs(getActivity());
			return true;
		} catch (final IllegalStateException exception) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					finishActivityWithError(exception);
				}
			});
			return false;
		}
	}

	private void finishActivityWithError(Throwable throwable) {
		finishActivityWithError(throwable, "无法启动录像机");
	}

	private void finishActivityWithError(Throwable throwable, String msg) {
		mPreferences.edit().putInt("media_mode", 1).commit();
		ActivityUtils.showAlertDialog(getActivity(), "应用关闭",
				throwable.getMessage());
		ActivityUtils.showToastWithIcon(getActivity(), msg, false);
		getActivity().onBackPressed();
	}

	private int getMinVideoIndicatorXPos() {
		return (int)Math.ceil(1f*DpAndPxUtils.getScreenWidthPixels()*EgmConstants.VIDEO_MIN_LENGH/EgmConstants.VIDEO_MAX_LENGH);
	}

//	public String getQueueId() {
//		return queueId;
//	}
//
//	public void setQueueId(String queueId) {
//		this.queueId = queueId;
//	}

	class ProcessMoviesTask extends AsyncTask<PendingMedia, Void, PendingMedia> {

		@Override
		protected PendingMedia doInBackground(PendingMedia... params) {
			PendingMedia pendingMedia = params[0];
			long startTime = System.currentTimeMillis();
			if (mClipStackManager.size() >= 1) {
				int frameWidth = mProfile.videoFrameWidth;
				int frameHeight = mProfile.videoFrameHeight;
				if (frameWidth > frameHeight) {
					int tmp = frameHeight;
					frameWidth = frameHeight;
					frameHeight = tmp;
				}
				pendingMedia.setSourceType(1);
				pendingMedia.setOriginalWidth(frameWidth);
				pendingMedia.setOriginalHeight(frameHeight);
				File[] files = new File[1 + mClipStackManager.size()];
				ArrayList<ClipInfo> list = new ArrayList<ClipInfo>();
				Iterator it = mClipStackManager.getClipStack().iterator();
				int m = 0;
				while (it.hasNext()) {
					Clip clip = (Clip) it.next();
					if ((clip.getState() == ClipState.RECORDED)
							&& (clip.getVideoPath() != null)) {
						String filePath = clip.getVideoPath();
						files[m] = new File(filePath);
						m++;
						ClipInfo info = new ClipInfo();
						info.setVideoFilePath(filePath);
						info.setCameraId(clip.getCameraId());
						info.setStartTime(0);
						info.setEndTime((int) clip.getDuration());
						info.setDimensions(frameWidth, frameHeight);
						list.add(info);
					}
				}
				pendingMedia.setClipInfoList(list);
				String modName = files[mClipStackManager.size() - 1].getAbsolutePath();
				String stitchedName = modName
						.substring(0, modName.length() - 4) + "-stitched.mp4";
				files[(files.length - 1)] = new File(stitchedName);
				Log.v(TAG, "Saving stitched file to: " + stitchedName);
				float durtime = VideoEditUtil.stitchMovies(files);
				ClipInfo clipInfo = new ClipInfo();
				clipInfo.setVideoFilePath(stitchedName);
				clipInfo.setCameraId(getCameraId());
				clipInfo.setStartTime(0);
				clipInfo.setEndTime((int) (1000.0F * durtime));
				clipInfo.setDimensions(frameWidth, frameHeight);
				pendingMedia.setStitchedClipInfo(clipInfo);
				Log.v(TAG,
						"Splitting and stitching took: "
								+ (System.currentTimeMillis() - startTime)
								+ "ms");

			}
			return pendingMedia;
		}

		@Override
		protected void onPostExecute(PendingMedia result) {
			super.onPostExecute(result);
			toNexting = false;
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.cancel();
				progressDialog = null;
			}
			Intent intent = new Intent(getActivity(),
					VideoPreviewActivity.class);
			intent.putExtra("path", result.getStitchedClipInfo()
					.getVideoFilePath());
//			intent.putExtra("queueId", CamcorderFragment.this.getQueueId());
			intent.putExtra("cameraId", getCameraId());
			// 参加话题参数
			intent.putExtras(getActivity().getIntent());
			getActivity().startActivity(intent);
			if(mClipStackManager != null){
				mClipStackManager.deleteLastClip();
			}	
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new LofterProgressDialog(getActivity(),
					R.style.lofter_progress_dialog_fullscreen);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}
	}

	private class LoadThumbnailTask extends AsyncTask<Void, Void, Thumbnail> {
		private boolean mLookAtCache;

		public LoadThumbnailTask(boolean lookAtCache) {
			mLookAtCache = lookAtCache;
		}

		@Override
		protected Thumbnail doInBackground(Void... params) {
			// Load the thumbnail from the file.
			ContentResolver resolver = getActivity().getContentResolver();
			Thumbnail t = null;
			if (mLookAtCache) {
				t = Thumbnail.getLastThumbnailFromFile(getActivity()
						.getFilesDir(), resolver);
			}

			if (isCancelled())
				return null;

			if (t == null) {
				Thumbnail result[] = new Thumbnail[1];
				// Load the thumbnail from the media provider.
				int code = Thumbnail.getLastVideoThumbnailFromContentResolver(
						resolver, result);
				switch (code) {
				case Thumbnail.THUMBNAIL_FOUND:
					return result[0];
				case Thumbnail.THUMBNAIL_NOT_FOUND:
					return null;
				case Thumbnail.THUMBNAIL_DELETED:
					cancel(true);
					return null;
				}
			}
			return t;
		}

		@Override
		protected void onPostExecute(Thumbnail thumbnail) {
			if (isCancelled())
				return;
			mThumbnail = thumbnail;
//			updateThumbnailView(true);
		}
	}

	protected void getLastThumbnail() {
		mThumbnail = ThumbnailHolder.getLastThumbnail(getActivity()
				.getContentResolver());
		// Suppose users tap the thumbnail view, go to the gallery, delete the
		// image, and coming back to the camera. Thumbnail file will be invalid.
		// Since the new thumbnail will be loaded in another thread later, the
		// view should be set to gone to prevent from opening the invalid image.
//		updateThumbnailView(false);
		if (mThumbnail == null) {
			mLoadThumbnailTask = new LoadThumbnailTask(true).execute();
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode != Activity.RESULT_OK){
    		return ;
    	}
		String filePath = null ;
		long duration = 0 ;
		switch(requestCode){
    		
    		case EgmConstants.REQUEST_SELECT_VIDEO:
    			/**
    			 * 选择相册中的视频
    			 */
    			if(data == null){
    				return ;
    			}
    			filePath = data.getStringExtra(EgmConstants.BUNDLE_KEY.CHAT_VIDEO_PATH);
    			duration = data.getLongExtra(EgmConstants.BUNDLE_KEY.CHAT_VIDEO_DURATION,0);
    			Intent intent = new Intent(getActivity(),
						VideoEditActivity.class);
				intent.putExtra("duration", String.valueOf(duration));
				intent.putExtra("path", filePath);
				getActivity().startActivity(intent);
    			break;
    		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
		public void onClick(View v) {
			 int id = v.getId();
		        switch (id) {
		            case R.id.fragment_camera_shutter_button:
		            	if (getClipRecorderState() == ClipRecorderState.STOPPED) {
							startClip();
							mVideoShutterButton.setBackgroundResource(R.drawable.btn_pgchat_video_record_stop);
						} else if(getClipRecorderState() == ClipRecorderState.RECORDING){
							if (mClipStackManager.getTotalClipLength() < (EgmConstants.VIDEO_MIN_LENGH*1000)) {
//								showMinVideoLengthPopup();
								showToast(R.string.video_minimum_tips);
							} else {
								endClip(false);
								mVideoShutterButton.setEnabled(false);
								mVideoShutterButton.setBackgroundResource(R.drawable.btn_pgchat_video_record);
								gotoNext();
								
							}
						}
		            	
		            	break;
		            	default:
		            		break;
		        }
	}
}
