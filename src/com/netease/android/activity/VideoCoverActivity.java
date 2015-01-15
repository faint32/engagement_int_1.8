package com.netease.android.activity;

import java.io.File;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore.Images;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.mixin.helper.media.VideoConverter;
import com.netease.android.util.ActivityUtils;
import com.netease.android.util.DpAndPxUtils;
import com.netease.android.util.PhotoPickUtils;
import com.netease.android.video.ui.VideoFileUtil;
import com.netease.android.video.ui.VideoPopup;
import com.netease.android.video.util.VideoEditUtil;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityMyShow;

@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
public class VideoCoverActivity extends VideoBaseActivity {
	// private VideoView mVideoView;
	private long mDuration;// 视频截取后时长
	private long mTotalDuration;// 视频总时长
	private SeekBar mSeekbar;
	private volatile long mCurrentPosition; 
	private volatile long mPrePosition = -1L;// 前一次截图的位置
	// private Bitmap mBitmap;
	// private String mPath;
	private String mCoverName;// 封面截图
	private int mThumbSize = 74;
	private ImageView mCoverImg;
	private View videoLayoutView;
	// private float mVideoWidth;// 缩放后宽
	// private float mVideoHeight;//缩放后高
//	private float mOrgVideoWidth;// 原始视频宽
//	private float mOrgVideoHeight;// 原始视频高
	// private float sWidth;
	// private float sHeight;
	private int mStartPosition;
	private int mEndPosition;

	private int mScrollX;
	private int mScrollY;
	
	private boolean mIsCamera;

	private int mDstRectX;
	private int mDstRectY;
	private int mDstRectWidth;
	private int mDstRectHeight;

	private int mSeekbarThumbSize;// seekbarthumb尺寸
	private int mThumbBorder;// seekbarthumb边框
	// private volatile long mCoverPosition = -1;// 当前封面位置
	private volatile boolean mHandleCover = true;
	// private volatile boolean mHandleCoverWait = false;
	private static final int mHorizontalCount = 8;// 时间轴图片数量
	private LinearLayout mHorizontalLayout;
//	private HandleCover mcoverHandler;
	private HandleHorizontal mHorizontalHandler;
	private int mCoverSleepDuration = 1;
	private BitmapDrawable mThumbDrawable;

	private VideoPopup mNuxPopup;
	
	private View mVideoProgressView;
	
	private volatile boolean mTrackingTouch = false;
	
	protected TextView mBackButton;
	
	private HandleCover mHandleCoverTask;
	private GeneratCoverVideo mNextTask;
	
	private AlertDialog mVideoProcessDialog;
	
//	private MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_cover);
		
		initParams();

		initSeekBar();

		initHorizontalLayout();

		initNavigation();
		
		initVideoView();
		
		
		mHandleCoverTask = new HandleCover();
		mHandleCoverTask.execute();

//		initTips();
		
		mProgress.setCancelable(false);
//		mProgress.show();
	}

	/**
	 * 初始化按钮
	 */
	private void initNavigation() {
		mBackButton.setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						VideoCoverActivity.this.onBackPressed();
					}
				});
		
		mNextButton.setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						if(mVideoProgressView != null && mVideoProgressView.getVisibility() == View.VISIBLE){
							VideoCoverActivity.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									mVideoProgressView.setVisibility(View.GONE);
								}
							});
						}
//						showProgress();
						
						go2Next();
					}
				});
	}
	
	class GeneratCoverVideo implements Runnable {

		String mCoverPath;
		String mVideoPath;
		
		@Override
		public void run() {
			try {
				mCoverPath = VideoFileUtil
						.getCoversPath(VideoCoverActivity.this)
						+ File.separator + mCoverName;
				
				Bitmap bitmap = getVideoFrame(mCurrentPosition);

				PhotoPickUtils.savePhoto(bitmap, VideoFileUtil
						.getCoversPath(VideoCoverActivity.this),
						mCoverName);
				
				double startTimePercent = mStartPosition == 0 ? 0D
						: (mStartPosition / (double) mTotalDuration); // 截取视频的起始时间点百分比
				double endTimePercent = mEndPosition == 0 ? 1D
						: (mEndPosition / (double) mTotalDuration); // 截取视频的结束时间点百分比

				int rotate = VideoConverter.getVideoRotate(mPath);
				
				VideoConverter.Params tmpParams = null;
				
				// 视频实际是横向的,封面中是纵向的,需要计算
				if(rotate == 270){
					// 前置摄像头拍摄，需要特殊处理参数
					tmpParams = new VideoConverter.Params((int)(mOrgVideoHeight 
										- (mDstRectWidth + mDstRectY)), mDstRectX,
									mDstRectWidth, mDstRectHeight,
									startTimePercent, endTimePercent);
				}else if (rotate == 90) {
					// 后置摄像头
					tmpParams = new VideoConverter.Params(mDstRectY, mDstRectX,
									mDstRectWidth, mDstRectHeight,
									startTimePercent, endTimePercent);
				}else{
					// 0 度 ？ 
					tmpParams = new VideoConverter.Params(mDstRectX, mDstRectY,
									mDstRectWidth, mDstRectHeight,
									startTimePercent, endTimePercent);
				}
				
				VideoConverter.convertFile(mPath, tmpParams);
				
				mVideoPath = VideoConverter.getConvertedFilePath(mPath, tmpParams);
			} catch (Exception e) {
			}
			
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (mVideoProcessDialog != null) {
						mVideoProcessDialog.dismiss();
					}
					
					finish();
					
					ActivityMyShow.startActivity(VideoCoverActivity.this, mVideoPath, 
							mCoverPath, mDuration, mIsCamera, null);
				}
			});
		}
		
		
	}

	private void go2Next(){
		mHandleCover = false;
		
		if (mHandleCoverTask != null) {
			mHandleCoverTask.cancel(true);
			mHandleCoverTask = null;
		}
		
		if (mNextTask == null) {
			mNextTask = new GeneratCoverVideo();
			Thread thread = new Thread(mNextTask);
			thread.setPriority(Thread.NORM_PRIORITY + 1);
			thread.start();
			
			if (mVideoProcessDialog == null) {
				mVideoProcessDialog = ProgressDialog.show(this, 
						null, getString(R.string.video_processing), true, false);
			}
			
			mVideoProcessDialog.show();
		}
	}
	
	/**
	 * 初始化缩略图
	 */
	private void initHorizontalLayout() {
		mHorizontalLayout = (LinearLayout) findViewById(R.id.horizontal_layout);
		
		// 需求调整，不需要展示8张图片
		LayoutInflater inflater = LayoutInflater.from(this);
		for (int i = 0; i < mHorizontalCount; i++) {
			// View view = inflater.inflate(R.layout.video_frame_item, null);
			mHorizontalLayout.addView(inflater.inflate(
					R.layout.video_frame_item, null));
		}
		mHorizontalHandler = new HandleHorizontal();
		mHorizontalHandler.execute();
		
//		handleHorizontal();
	}

	/**
	 * 初始化时间轴
	 */
	private void initSeekBar() {
		mSeekbar = (SeekBar) this.findViewById(R.id.video_cover_seekbar);
		mSeekbar.setMax(VideoEditUtil.THOUSAND_INT);
//		mCurrentPosition = VideoEditUtil.THOUSAND_INT / 2;
		mSeekbar.setProgress(VideoEditUtil.THOUSAND_INT / 2);
		
//		// 初始化seekbar的thumb
//		initThumb((mDuration / 2 + mStartPosition) * VideoEditUtil.THOUSAND_INT);
		// 初始化seekbar的thumb
		initThumb(mCurrentPosition);
		mSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
//				mCoverSleepDuration = 500;
//				VideoCoverActivity.this.runOnUiThread(new  Runnable() {
//					public void run() {
//						mVideoProgressView.setVisibility(View.GONE);
//					}
//				});
				mTrackingTouch = false;
				
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
//				handleButton(false);
//				mCoverSleepDuration = 500;
				if(mNuxPopup != null && mNuxPopup.isShowing()){
					mNuxPopup.dismiss();
				}
				VideoCoverActivity.this.runOnUiThread(new  Runnable() {
					@Override
					public void run() {
						mVideoProgressView.setVisibility(View.VISIBLE);
					}
				});
				mTrackingTouch = true;
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (!fromUser) {
					return;
				}
				mCurrentPosition = mStartPosition * VideoEditUtil.THOUSAND_INT
						+ mDuration * progress;
				mCurrentPosition = mCurrentPosition == 0 ? 1 : mCurrentPosition;
//				Bitmap thumbBitmap = getScaleVideoFrame(mCurrentPosition,
//						mSeekbarThumbSize, mSeekbarThumbSize);
//				PhotoPickUtils.drawBorder(thumbBitmap, mThumbBorder,
//						Color.WHITE);
//				Bitmap coverBitmap = VideoEditUtil.getVideoFrame(
//						mCurrentPosition, mPath);
//				
//				setThumb(thumbBitmap);
//				mCoverImg.setImageBitmap(coverBitmap);
			}
		});
	}

	private void initParams() {
		mOrgVideoHeight = getIntent().getFloatExtra("videoheight", 0F);
		mOrgVideoWidth = getIntent().getFloatExtra("videowidth", 0F);
		
		mStartPosition = getIntent().getIntExtra("startPosition", 0);
//		mStartPosition = getIntent().getIntExtra("startPosition", 0);
		mEndPosition = getIntent().getIntExtra("endPosition", 0);
		mTotalDuration = getIntent().getLongExtra("duration", 0L);

		mScrollX = getIntent().getIntExtra("scrollX", 0);
		mScrollY = getIntent().getIntExtra("scrollY", 0);
		
		mIsCamera = getIntent().getBooleanExtra("isCamera", false);

		mCoverName = "cover_" + System.currentTimeMillis() + ".jpg";

		if (mEndPosition == 0) {
			// mDuration = VideoEditUtil.getDuration(mPath);
			// 魅族2videoview.getDuration() == -1,这里取一次
			if(mTotalDuration <= 0){
				mTotalDuration = VideoEditUtil.getDuration(mPath);
			}
			mDuration = mTotalDuration;
			// 从预览页面跳转，此参数为0
			mEndPosition = (int) mDuration;
			// 超过8s，只显示8s--新拍摄视频不准确，可能超过8s
			if (mEndPosition > VideoEditActivity.mVideoeEditDuration) {
				mEndPosition = VideoEditActivity.mVideoeEditDuration;
			}
		} else {
			mDuration = mEndPosition - mStartPosition;
		}

		mSeekbarThumbSize = DpAndPxUtils.dip2px(
				DpAndPxUtils.dip2px(mThumbSize + 6));
		mThumbBorder = DpAndPxUtils.dip2px(
				DpAndPxUtils.dip2px(3));
		
		
		mCurrentPosition = (mDuration / 2 + mStartPosition) * VideoEditUtil.THOUSAND_INT;
		
		mNextButton = (TextView)this.findViewById(R.id.video_next);
		mBackButton = (TextView)this.findViewById(R.id.video_back);
		mNextButton.setText(R.string.done);
		
		handleButton(false);
		mVideoProgressView = this.findViewById(R.id.video_cover_progressbar_layout);
		
		this.findViewById(R.id.video_cover_mask).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mNuxPopup != null && mNuxPopup.isShowing()){
					mNuxPopup.dismiss();
				}
			}
		});
		
	}

	/**
	 * 初始化视频view
	 */
	private void initVideoView() {

		// 计算视频view大小
		handleVideoSize();

		if (mVideoHeight > mVideoWidth) {
			videoLayoutView = this.findViewById(R.id.video_scrollview);
			mCoverImg = (ImageView) this.findViewById(R.id.video_cover_img_s);
			View view = this.findViewById(R.id.video_scrollview_padding);
			android.view.ViewGroup.LayoutParams layoutParams = view
					.getLayoutParams();
			layoutParams.height = (int) (sHeight - sWidth);
			view.setLayoutParams(layoutParams);
		} else {
			videoLayoutView = this
					.findViewById(R.id.video_horizontalscrollview);
			mCoverImg = (ImageView) this.findViewById(R.id.video_cover_img_h);
		}
		videoLayoutView.setVisibility(View.VISIBLE);

		android.view.ViewGroup.LayoutParams params = mCoverImg
				.getLayoutParams();
		params.height = (int) mVideoHeight;
		params.width = (int) mVideoWidth;

		// 中间位置
//		mCoverImg.setImageBitmap(VideoEditUtil.getVideoFrame(mCurrentPosition, mPath));

		// 初始化seekbar的thumb
//		initThumb(mCurrentPosition);
		
		// scrollto 正确位置
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (videoLayoutView instanceof HorizontalScrollView) {
					((HorizontalScrollView) videoLayoutView).scrollTo(mScrollX,
							mScrollY);
				}
				if (videoLayoutView instanceof ScrollView) {
					((ScrollView) videoLayoutView).scrollTo(mScrollX, mScrollY);
				}
				// 跳转后背景透明，部分机器抖动
				VideoCoverActivity.this.findViewById(R.id.video_play).setBackgroundResource(R.color.trans);
			}
		}, 100);
		
		handleButton(true);
		
//		mVideoProgressView = this.findViewById(R.id.video_cover_progressbar_layout);
	}

	
	/**
	 * 初始化videoview
	 * 
	 */
	private class HandleVideoview extends AsyncTask<Object, Object, Object> {
		@Override
		protected Object doInBackground(Object... params) {
			handleVideoSize();
			return null;
		}

		
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			if (mVideoHeight > mVideoWidth) {
				videoLayoutView = VideoCoverActivity.this.findViewById(R.id.video_scrollview);
				mCoverImg = (ImageView) VideoCoverActivity.this.findViewById(R.id.video_cover_img_s);
				View view = VideoCoverActivity.this.findViewById(R.id.video_scrollview_padding);
				android.view.ViewGroup.LayoutParams layoutParams = view
						.getLayoutParams();
				layoutParams.height = (int) (sHeight - sWidth);
				view.setLayoutParams(layoutParams);
			} else {
				videoLayoutView = VideoCoverActivity.this
						.findViewById(R.id.video_horizontalscrollview);
				mCoverImg = (ImageView) VideoCoverActivity.this.findViewById(R.id.video_cover_img_h);
			}
			videoLayoutView.setVisibility(View.VISIBLE);

			android.view.ViewGroup.LayoutParams params = mCoverImg
					.getLayoutParams();
			params.height = (int) mVideoHeight;
			params.width = (int) mVideoWidth;

			// 中间位置
//			mCoverImg.setImageBitmap(VideoEditUtil.getVideoFrame(mCurrentPosition, mPath));

			// 初始化seekbar的thumb
//			initThumb(mCurrentPosition);
			
//			videoLayoutView.invalidate();
			
			// scrollto 正确位置
//			new Handler().postDelayed(new Runnable() {
//				@Override
//				public void run() {
					if (videoLayoutView instanceof HorizontalScrollView) {
						((HorizontalScrollView) videoLayoutView).scrollTo(mScrollX,
								mScrollY);
					}
					if (videoLayoutView instanceof ScrollView) {
						((ScrollView) videoLayoutView).scrollTo(mScrollX, mScrollY);
					}
					// 跳转后背景透明，部分机器抖动
//					VideoCoverActivity.this.findViewById(R.id.video_play).setBackgroundResource(R.color.trans);
//				}
//			}, 100);
			handleButton(true);

		}

	}
	
	private void handleButton(boolean flag){
		try {
			mNextButton.setClickable(flag);
			mNextButton.setEnabled(flag);
			mBackButton.setClickable(flag);
			mBackButton.setEnabled(flag);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initTips() {
		//TODO: initTip
//		NPreferences preferences = new NPreferences(this);
//		String videoCoverTipsShown = preferences.getSettingItem(
//				PreferencesConst.VIDEO_COVER_TIPS_SHOWN_KEY, "");
//
//		if ("covershown".equalsIgnoreCase(videoCoverTipsShown)) {
//			return;
//		}
//		int[] location = new int[2];
//		mHorizontalLayout.getLocationInWindow(location); // 获取在当前窗口内的绝对坐标
//		// view.getLocationOnScreen(location);//获取在整个屏幕内的绝对坐标
//		int x = ActivityUtils.getSnapshotWidth(this) / 2;
//		int y = location[1];
//		if (mNuxPopup == null)
//			mNuxPopup = new VideoPopup(this, Config.VIDEO_COVER_TIPS);
//		if (!mNuxPopup.isShowing()) {
//			// View container = this.findViewById(R.id.video_cover_tips);
//			mNuxPopup
//					.setAnimationStyle(R.style.camcorder_popup_animation_style);
//
//			mNuxPopup.showAtLocation(mHorizontalLayout, Gravity.NO_GRAVITY, x
//					- DpAndPxUtils.dip2px(45),
//					y - DpAndPxUtils.dip2px(55));
////			new Handler().postDelayed(new Runnable() {
////				@Override
////				public void run() {
////					mNuxPopup.dismiss();
////				}
////			}, 2000L);
//			preferences.putSettingItem(
//					PreferencesConst.VIDEO_COVER_TIPS_SHOWN_KEY, "covershown");
//		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(mInit){
			return;
		}
		mInit = true;
//		initVideoView();
//		initSeekBar();
//		new HandleVideoview().execute();
//		mcoverHandler = new HandleCover();
//		mcoverHandler.execute();
		
//		new HandleCover().execute();
//
		initTips();
		
		
//		if (videoLayoutView instanceof HorizontalScrollView) {
//			((HorizontalScrollView) videoLayoutView).scrollTo(mScrollX,
//					mScrollY);
//		}
//		if (videoLayoutView instanceof ScrollView) {
//			((ScrollView) videoLayoutView).scrollTo(mScrollX, mScrollY);
//		}
	}

	/**
	 * 获取视频截图
	 * 
	 * @param position
	 * @param border
	 * @return
	 */
	private Bitmap getScaleVideoFrame(long position, int height, int width) {
		Bitmap bitmap = getVideoFrame(position);
		bitmap = PhotoPickUtils.scaleCrop(bitmap, height, width, false);
		return bitmap;
	}

	private Bitmap getVideoFrame(long position) {
		Bitmap bitmap = VideoEditUtil.getVideoFrame(position, mPath);
		try {
			bitmap = Bitmap.createBitmap(bitmap, mDstRectX, mDstRectY,
					mDstRectWidth, mDstRectHeight);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;

	}

	/**
	 * 设置seekbar 图标
	 * 
	 * @param seekBar
	 * @param position
	 */
	private void initThumb(long position) {
//		Bitmap bitmap = getScaleVideoFrame(
//				position,
//				DpAndPxUtils.dip2px(this,
//						DpAndPxUtils.dip2px(this, mThumbSize + 6)),
//				DpAndPxUtils.dip2px(this,
//						DpAndPxUtils.dip2px(this, mThumbSize + 6)));
//		if(bitmap == null){

//		}
		
//		BitmapDrawable drawable = (BitmapDrawable)getResources().getDrawable(R.drawable.video_cover_thumb);
		Bitmap bitmap = null;
//		if(drawable != null){
//			Bitmap dbitmap = drawable.getBitmap();
//			if(dbitmap != null){
//				bitmap = dbitmap.copy(dbitmap.getConfig(), false);
//				dbitmap.recycle();
//			}
//		}
		if(bitmap == null){
			bitmap = Bitmap.createBitmap(DpAndPxUtils.dip2px(
					DpAndPxUtils.dip2px(mThumbSize + 6)), DpAndPxUtils.dip2px(
							DpAndPxUtils.dip2px(mThumbSize + 6)), android.graphics.Bitmap.Config.ARGB_8888);
		}
		PhotoPickUtils.drawBorder(bitmap,
				DpAndPxUtils.dip2px(DpAndPxUtils.dip2px(3)),
				Color.WHITE);
		if (bitmap == null) {
			return;
		}
		// drawable 会根据density转换回来，所以这里需要DpAndPxUtils.dip2px两次
		mThumbDrawable = new BitmapDrawable(bitmap);

		Rect bounds = mThumbDrawable.getBounds();
		// fix 3.0以下seekbar setthumb bug
		if (VERSION.SDK_INT <= VERSION_CODES.GINGERBREAD_MR1) {
			float seekbarWidth = ActivityUtils.getSnapshotWidth(this)
					- DpAndPxUtils.dip2px(5 * 2);
			// bitmap大小需从drawable转回一次
			int thumbBitmapWidth = DpAndPxUtils.px2dip(mSeekbarThumbSize);
			int left = (int) (seekbarWidth / 2 - thumbBitmapWidth / 2);
			// int left = 0;
			bounds.set(left, 0, left + thumbBitmapWidth, thumbBitmapWidth);
		}

		setThumb(bitmap);
	}

	private void setThumb(Bitmap bitmap) {
		BitmapDrawable drawable = new BitmapDrawable(bitmap);
		// fix 3.0以下seekbar setthumb bug
		if (VERSION.SDK_INT <= VERSION_CODES.GINGERBREAD_MR1) {
			Rect oldbound = mThumbDrawable.getBounds();
			int width = drawable.getIntrinsicWidth();
			int height = drawable.getIntrinsicHeight();
			int left = (oldbound.left + oldbound.right - width) / 2;
			int top = (oldbound.top + oldbound.bottom - height) / 2;
			Rect newbound = new Rect(left, top, left + width, top + height);
			// Rect newbound = new Rect(0, 0, 0, 0);
			drawable.setBounds(newbound);
		}
		mThumbDrawable = drawable;
		mSeekbar.setThumb(mThumbDrawable);
		mSeekbar.invalidate();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHandleCover = false;
		if (mNuxPopup != null && mNuxPopup.isShowing()) {
			mNuxPopup.dismiss();
		}
	}

	/**
	 * 获取封面
	 * 
	 */
	private class HandleCover extends AsyncTask<Object, Bitmap, Object> {

		@Override
		protected Bitmap[] doInBackground(Object... params) {
			while (mHandleCover) {
				long position = mCurrentPosition;
				if (position != 0 && mVideoProgressView != null 
						&& mVideoProgressView.getVisibility() == View.VISIBLE) {
					if (isCancelled()) {
						return null;
					}
					
					Bitmap thumbBitmap = getScaleVideoFrame(position,
							mSeekbarThumbSize, mSeekbarThumbSize);
					
					if (isCancelled()) {
						return null;
					}
					
					PhotoPickUtils.drawBorder(thumbBitmap, mThumbBorder,
							Color.WHITE);
					
					if (isCancelled()) {
						return null;
					}
					
					Bitmap coverBitmap = VideoEditUtil.getVideoFrame(
							position, mPath);
					
					if (isCancelled()) {
						return null;
					}
					
					publishProgress(thumbBitmap, coverBitmap);
					
//					Log.e("=======", "position = "+position);
					mPrePosition = position;
				}
				
				try {
					Thread.sleep(mCoverSleepDuration);
				} catch (InterruptedException e) {
				}
				
				// 初始化可能取不到图，sleep 1ms，后面正常500ms
				if(mCoverSleepDuration != 500){
					mCoverSleepDuration = 500;
				}
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Bitmap... values) {
			super.onProgressUpdate(values);
			// mSeekbar.setThumb(new BitmapDrawable(values[0]));
			if(values[0] != null){
				setThumb(values[0]);
				mCoverImg.setImageBitmap(values[1]);
			}
			if(!mTrackingTouch && mPrePosition == mCurrentPosition){
				mVideoProgressView.setVisibility(View.GONE);
//				handleButton(true);
			}
		}

	}
	
	/**
	 * 初始化时间轴图片
	 * 
	 */
	private class HandleHorizontal extends AsyncTask<Object, Object, Object> {
		@Override
		protected Object doInBackground(Object... params) {
			int height = DpAndPxUtils.dip2px(mThumbSize);

			long frameDuration = mDuration * VideoEditUtil.THOUSAND_INT
					/ mHorizontalCount - 1;
			float screenWidth = ActivityUtils
					.getSnapshotWidth(VideoCoverActivity.this);
			// 计算view宽度
			float viewWidth = (screenWidth - DpAndPxUtils.dip2px(
					 5 * 2));
			// 计算一张图宽度
			float frameWidth = viewWidth / mHorizontalCount;
			for (int i = 0; i < mHorizontalCount; i++) {
				long framePosition = i * frameDuration + mStartPosition
						* VideoEditUtil.THOUSAND_INT;
				// 第一张截图
				if (i == 0) {
					framePosition = mStartPosition * VideoEditUtil.THOUSAND_INT
							+ 1;
				}
				// 最后一张截图
				if (i == mHorizontalCount - 1) {
					framePosition = mEndPosition * VideoEditUtil.THOUSAND_INT;
				}
				Bitmap bitmap = getScaleVideoFrame(framePosition, height,
						(int) frameWidth);
				if (bitmap != null) {
					publishProgress(i, bitmap);
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Object... values) {
			super.onProgressUpdate(values);

			Bitmap bitmap = (Bitmap) values[1];
			if (bitmap == null) {
				return;
			}
			// oncreate中可能拿不到截图，第一次拿到截图的时候，设置页面大小
			if (bitmap != null && mVideoHeight == 0) {
//				initVideoView();
//				initSeekBar();
			}

			View view = mHorizontalLayout.getChildAt((Integer) values[0]);
			ImageView imageView = (ImageView) view
					.findViewById(R.id.video_frame);
			imageView.setImageBitmap((Bitmap) values[1]);
		}
		
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
//			retrieverRelease();
			if(mProgress != null && mProgress.isShowing()){
				mProgress.cancel();
				mProgress.setCancelable(true);
			}
		}

	}

	/**
	 * 计算视频显示大小
	 */
	@Override
	protected void handleVideoSize() {
		// 计算视频view大小
		// Bitmap bitmap = VideoEditUtil.getVideoFrame(0, mPath);
		
		if(mOrgVideoHeight == 0F){
			Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(mPath,
					Images.Thumbnails.FULL_SCREEN_KIND);
			if (bitmap == null) {
				bitmap = VideoEditUtil.getVideoFrame(1, mPath);
			}
			if (bitmap == null) {
				return;
			}
			mOrgVideoHeight = bitmap.getHeight();
			mOrgVideoWidth = bitmap.getWidth();
		}

		sWidth = (float) ActivityUtils.getSnapshotWidth(this);
		sHeight = (float) ActivityUtils.getSnapshotHeight(this);
		if (mOrgVideoHeight > mOrgVideoWidth) {
			mVideoWidth = sWidth;
			mVideoHeight = mOrgVideoHeight / mOrgVideoWidth * mVideoWidth;

			// 宽度被缩放，重新计算宽度
			// mDstRectWidth = (int)(mOrgVideoWidth / mVideoWidth * sWidth);//
			// 原始视频需要截取区域的宽度
			mDstRectWidth = (int) mOrgVideoWidth;// 原始视频需要截取区域的宽度
			mDstRectHeight = mDstRectWidth;// 原始视频需要截取区域的高度
		} else {
			mVideoHeight = sWidth;
			mVideoWidth = mOrgVideoWidth / mOrgVideoHeight * mVideoHeight;

			// mDstRectHeight = (int)(mOrgVideoHeight / mVideoHeight *
			// sHeight);// 原始视频需要截取区域的宽度
			mDstRectHeight = (int) mOrgVideoHeight;// 原始视频需要截取区域的宽度
			mDstRectWidth = mDstRectHeight;// 原始视频需要截取区域的高度
		}
		mDstRectX = (int) (mOrgVideoWidth / mVideoWidth * mScrollX); // 原始视频需要截取区域的左上角x坐标（注意截取区域越界，将会返回错误信息）
		mDstRectY = (int) (mOrgVideoHeight / mVideoHeight * mScrollY);// 原始视频需要截取区域的左上角y坐标

		// 防止精度损失造成的裁剪图片异常，这里做兼容
		if (mDstRectX + mDstRectWidth > mOrgVideoWidth) {
			mDstRectWidth -= (mDstRectX + mDstRectWidth - mOrgVideoWidth);
		}
		if (mDstRectY + mDstRectHeight > mOrgVideoHeight) {
			mDstRectHeight -= (mDstRectY + mDstRectHeight - mOrgVideoHeight);
		}

	}
}
