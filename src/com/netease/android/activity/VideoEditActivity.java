package com.netease.android.activity;

import java.io.IOException;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.netease.android.util.ActivityUtils;
import com.netease.android.util.DpAndPxUtils;
import com.netease.android.util.ThreadUtil;
import com.netease.android.video.player.TextureVideoPlayer;
import com.netease.android.video.player.VideoPlayer;
import com.netease.android.video.player.VideoPlayer.OnCompletionListener;
import com.netease.android.video.player.VideoPlayer.OnErrorListener;
import com.netease.android.video.player.VideoPlayer.OnPreparedListener;
import com.netease.android.video.ui.VideoPopup;
import com.netease.android.video.ui.VideoPopup.Config;
import com.netease.android.video.util.VideoEditUtil;
import com.netease.android.widget.VideoEditAdapter;
import com.netease.android.widget.ui.HorizontalListView;
import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;

//import android.widget.VideoView;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class VideoEditActivity extends VideoBaseActivity {
//	private VideoView mVideoView;
	private TextureVideoPlayer mVideoView;
	private TextureView mTextureView;
	private ImageView mVideoIcon;
	private int mStartPosition;// 视频起始位置
	private int mCurrentPosition = 1;// 视频当前位置
	private int mEndPosition;// 视频结束位置
	private View videoLayoutView;
	private float mListviewX;// 时间轴x坐标
	// private Handler handler = new Handler();
	// private Runnable runnable;
	private float mTimeBarWidth;// seekbar物理长度
	private float mTimeListViewWidth;// 时间轴物理长度
	private long mTotalDuration;// 视频总时长
	private long mDuration;// 截取后视频时长
	public static final int mVideoeEditDuration = EgmConstants.VIDEO_MAX_LENGH * 1000;// 编辑后视频最大时长
	private static final int mVideoeEditMinDuration = EgmConstants.VIDEO_MIN_LENGH * 1000;// 编辑后视频最小时长
	private HorizontalListView mListView;
	private SeekBar mEndSeekBar;
	// private float mVideoHeight;
	// private float mVideoWidth;
	// 屏幕高宽
	// private float sWidth;
	// private float sHeight;
	private View mVideoProgressBegin;
	private View mVideoProgressEnd;

	private VideoEditAdapter mAdapter;
	// private int mItemCount;// 时间轴图片数量
	private int mItemHeight;// 时间轴元素高度
	private int mItemWidth;// 时间轴元素宽度
//	private View mVideoPlay;

	private View mListViewMask;// 时间轴半透明遮挡
	private View mVideoSeekbarBackground;// seekbar背景,随progress联动

	private VideoPopup mStartPopup;
	private VideoPopup mEndPopup;
	private VideoPopup mSeekbarMinPopup;
//	private NPreferences preferences;
	private int edge = 0;
	private int itemCount = 5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_edit);

		mNextButton = (TextView) this.findViewById(R.id.video_next);
		mNextButton.setText(R.string.video_edit);

		initParams();

		initVideoView();

		initSeekbar();

		initNavigation();

		// initListView();

	}

	private void initParams() {
//		preferences = new NPreferences(this);
	}

	private void initSeekbar() {
		mListViewMask = this.findViewById(R.id.video_listview_mask);
		mVideoSeekbarBackground = this
				.findViewById(R.id.video_seekbar_background);
		// final View thumbImg =
		// this.findViewById(R.id.video_seekbar_thumb_img);
		mEndSeekBar = (SeekBar) this.findViewById(R.id.video_edit_end_seekbar);
		// endSeekBar.setThumb(getResources().getDrawable(R.drawable.ic_video_edit_end_thumb));
		// 横向listview宽度
		// final float listViewWidth = ActivityUtils.getSnapshotWidth(this)
		// - DpAndPxUtils.dip2px(this, 8 * 2);
		mEndSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				dismissTips();
				videoPause();
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (!fromUser) {
					return;
				}

				handleSeekBarProgress(progress, false);

				handleEndPosition();
				videoSeekto(mEndPosition);
			}
		});
		handleSeekBarProgress(mEndSeekBar.getProgress(), true);
		ImageView miniClip = (ImageView)findViewById(R.id.minimum_clip_length_image);
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) miniClip
				.getLayoutParams();
		layoutParams.setMargins(getMinVideoIndicatorXPos(), 0, 0, 0);
		miniClip.setLayoutParams(layoutParams);
		miniClip.setVisibility(View.VISIBLE);

	}
	private int getMinVideoIndicatorXPos() {
		return (int)Math.ceil(1f*DpAndPxUtils.getScreenWidthPixels()*EgmConstants.VIDEO_MIN_LENGH/EgmConstants.VIDEO_MAX_LENGH);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (mInit) {
			return;
		}
		mInit = true;
		initListView();
	}

	/**
	 * 处理seekbar和时间轴进度
	 * 
	 * @param progress
	 * @param init
	 */
	private void handleSeekBarProgress(int progress, boolean init) {

		// 不能移动超出最大时间
		if (mDuration < mVideoeEditDuration) {
			int newProgress = (int) ((((float) mDuration) / mVideoeEditDuration) * mEndSeekBar
					.getMax());
			if (progress > newProgress) {
				progress = newProgress;
			}
		}
		if (!init) {
			// 小于3s，不能移动
			if (mDuration < mVideoeEditMinDuration) {
				int newProgress = (int) ((((float) mDuration) / mVideoeEditDuration) * mEndSeekBar
						.getMax());
				if (progress < newProgress) {
					progress = newProgress;
				}
			} else {
				// 大于3s时候，不能移动到小于3s的位置
				if (progress < EgmConstants.VIDEO_MIN_LENGH*100) {
					progress = EgmConstants.VIDEO_MIN_LENGH*100;
				}
			}

			if (progress <= EgmConstants.VIDEO_MIN_LENGH*100 && mDuration >= mVideoeEditMinDuration) {
				showSeekbarMinTips(EgmConstants.VIDEO_MIN_LENGH*100);
			}

		}

		mEndSeekBar.setProgress(progress);

		// seekbar 背景，seekbar样式无法满足，用view来模拟
		android.view.ViewGroup.LayoutParams paramsSeekbar = mVideoSeekbarBackground
				.getLayoutParams();
		int widthSeekbar = (int) ((mEndSeekBar.getMax() - progress) * (mTimeBarWidth / mEndSeekBar
				.getMax()));
		// widthSeekbar += DpAndPxUtils.dip2px(VideoEditActivity.this, 8);//
		// 加8dp，边界空的宽度
		paramsSeekbar.width = widthSeekbar;
		mVideoSeekbarBackground.setLayoutParams(paramsSeekbar);

		android.view.ViewGroup.LayoutParams paramsMark = mListViewMask
				.getLayoutParams();
//		int listWidth = (int) ((mEndSeekBar.getMax() - progress) * (mTimeBarWidth / mEndSeekBar
//				.getMax()));
//		listWidth = widthSeekbar
//				+ DpAndPxUtils.dip2px(VideoEditActivity.this, 8);// 加8dp，边界空的宽度
		paramsMark.width = widthSeekbar;
		mListViewMask.setLayoutParams(paramsMark);

		if (init) {
			View listViewBlackMark = this
					.findViewById(R.id.video_listview_black_mask);
			android.view.ViewGroup.LayoutParams paramsBlackMark = listViewBlackMark
					.getLayoutParams();
			paramsBlackMark.width = widthSeekbar;
			listViewBlackMark.setLayoutParams(paramsBlackMark);
		}
		// LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
		// LinearLayout.LayoutParams.WRAP_CONTENT,
		// LinearLayout.LayoutParams.WRAP_CONTENT);
		// // 减掉图标一般的宽度
		// lp.setMargins(
		// listWidth - DpAndPxUtils.dip2px(VideoEditActivity.this, 7), 0,
		// 0, 0);
		// ((ImageView)thumbImg).setLayoutParams(lp);

	}

	private void initListView() {
		mListView = (HorizontalListView) this
				.findViewById(R.id.video_horizontallistview);

//		mItemHeight = getResources().getDimensionPixelSize(R.dimen.video_edit_cover_height);//DpAndPxUtils.dip2px(50);
		mItemWidth = (int) ((ActivityUtils.getSnapshotWidth(this) - getResources().getDimensionPixelSize(R.dimen.video_edit_hlist_right_margin)) /itemCount*1.0f);
		mItemHeight = mItemWidth;

		mAdapter = new VideoEditAdapter(this, mItemHeight, mItemWidth, mPath,
				mDuration * VideoEditUtil.THOUSAND_INT);

		mListView.setAdapter(mAdapter);
		// for(int i = 0;i<mItemCount;i++){
		// new VideoFrameTask().execute(i);
		// }
		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {

				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// Log.e("=====", mListView.getCurrentX() + "onScroll");
				mListviewX = mListView.getCurrentX();
				if(mListviewX > 0){
					dismissTips();
				}
				videoPause();
				handleStartPosition();
				// mVideoView.seekTo(mStartPosition);
				videoSeekto(mStartPosition);
			}
		});
		View hListviewLay = findViewById(R.id.video_edit_horizontallistview);
		hListviewLay.getLayoutParams().height = mItemHeight;
		hListviewLay.requestLayout();
	}

	private void initNavigation() {
		
		this.findViewById(R.id.video_back).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						VideoEditActivity.this.onBackPressed();
					}
				});
		mNextButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// LOFTER-14689
				videoPause();
				
				handleStartPosition();
				handleEndPosition();
				if (mEndPosition - mStartPosition < mVideoeEditMinDuration) {
					ActivityUtils.showToastWithIcon(
							VideoEditActivity.this,
							getResources().getString(
									R.string.video_edit_min_tips), false);
					return;
				}
				showProgress();
				ThreadUtil.execute(new Runnable() {

					@Override
					public void run() {
						go2Next();
					}
				});
			}
		});

	}

	private void go2Next() {
		Intent intent = new Intent(VideoEditActivity.this,
				VideoCoverActivity.class);
		intent.putExtra("queueId", getIntent().getStringExtra("queueId"));
		intent.putExtra("path", mPath);
		
//		 参加话题参数
		intent.putExtras(getIntent());
		//保证seekbar在终端时截满10s
		if(mEndPosition == mTotalDuration && mTotalDuration > mVideoeEditDuration){
			mStartPosition = (int)(mTotalDuration - mVideoeEditDuration);
		}
		
		intent.putExtra("duration", mTotalDuration);
		intent.putExtra("startPosition", mStartPosition);
		intent.putExtra("endPosition", mEndPosition == 0 ? mStartPosition
				+ mVideoeEditDuration : mEndPosition);
		if (videoLayoutView instanceof HorizontalScrollView) {
			intent.putExtra("scrollX",
					((HorizontalScrollView) videoLayoutView).getScrollX());
			intent.putExtra("scrollY",
					((HorizontalScrollView) videoLayoutView).getScrollY());
		}
		if (videoLayoutView instanceof ScrollView) {
			intent.putExtra("scrollX",
					((ScrollView) videoLayoutView).getScrollX());
			intent.putExtra("scrollY",
					((ScrollView) videoLayoutView).getScrollY());
		}

		intent.putExtra("videoheight", mOrgVideoHeight);
		intent.putExtra("videowidth", mOrgVideoWidth);

//		dismissPopup();
//		dismissTips();
		
		VideoEditActivity.this.startActivity(intent);

	}

	/**
	 * 初始化视频view
	 */
	private void initVideoView() {

		mTimeListViewWidth = ActivityUtils.getSnapshotWidth(this)
				- getResources().getDimensionPixelSize(R.dimen.video_edit_hlist_right_margin);
		mTimeBarWidth = ActivityUtils.getSnapshotWidth(this)
				- getResources().getDimensionPixelSize(R.dimen.video_edit_hlist_right_margin);
		// mPath = TextUtils.isEmpty(getIntent().getStringExtra("path")) ? mPath
		// : getIntent().getStringExtra("path");
		// mPath = getIntent().getStringExtra("path");

		mTotalDuration = TextUtils.isEmpty(getIntent().getStringExtra(
				"duration")) ? 0L : Long.valueOf(getIntent().getStringExtra(
				"duration"));
		if(mTotalDuration == 0L){
			 mTotalDuration = VideoEditUtil.getDuration(mPath);
		}
		mDuration = mTotalDuration;
		// 最多一分钟
		if (mDuration > 60 * 1000) {
			mDuration = 60 * 1000;
		}

		handleVideoSize();

//		Uri uri = Uri.parse(mPath);

		if (mVideoHeight > mVideoWidth) {
			videoLayoutView = this.findViewById(R.id.video_scrollview);
			mTextureView = (TextureView) this
					.findViewById(R.id.video_preview_view_s);
			View view = this.findViewById(R.id.video_scrollview_padding);
			android.view.ViewGroup.LayoutParams layoutParams = view
					.getLayoutParams();
			layoutParams.height = (int) (sHeight - sWidth - getResources().getDimensionPixelSize(R.dimen.video_record_top_bar));
			view.setLayoutParams(layoutParams);
//			mVideoPlay = this.findViewById(R.id.video_play_h);
		} else {
			videoLayoutView = this
					.findViewById(R.id.video_horizontalscrollview);
			mTextureView = (TextureView) this
					.findViewById(R.id.video_preview_view_h);
			
//			mVideoPlay = this.findViewById(R.id.video_play_h);
		}

		
		mVideoView = new TextureVideoPlayer(mTextureView);
		
		videoLayoutView.setVisibility(View.VISIBLE);

		initVideoPlayer();
		
		mVideoIcon = (ImageView) this.findViewById(R.id.video_icon);
		mVideoProgressBegin = this.findViewById(R.id.video_progress_begin);
		mVideoProgressEnd = this.findViewById(R.id.video_progress_end);

		
		View operationLayout = this.findViewById(R.id.video_operation_layout);
		// 设置遮挡view的高度，保证视频正方形
		operationLayout.getLayoutParams().height = ActivityUtils
				.getSnapshotHeight(this) - ActivityUtils.getSnapshotWidth(this)
				-getResources().getDimensionPixelSize(R.dimen.video_record_top_bar);
				/*+ getResources().getDimensionPixelSize(R.dimen.video_edit_seekbar_progress_height)
				+(getResources().getDimensionPixelSize(R.dimen.video_edit_seekbar_thumb_height)
						-getResources().getDimensionPixelSize(R.dimen.video_edit_seekbar_progress_height)/2);*/

		operationLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismissTips();
			}
		});
		View play_icon = findViewById(R.id.video_icon);
//        int measureSpec=MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED);
//		play_icon.measure(measureSpec, measureSpec);
		RelativeLayout.LayoutParams param = (LayoutParams) play_icon.getLayoutParams();
//		param.setMargins(0, ((int)sWidth-param.height)/2, 0, 0);
		param.setMargins(0, ((int)sWidth-DpAndPxUtils.dip2px(72))/2, 0, 0);//播放按钮位置，144*144，后续优化
		play_icon.requestLayout();
	}
	
	
	private void initVideoPlayer(){
		videoLayoutView.setVisibility(View.VISIBLE);

		android.view.ViewGroup.LayoutParams params = mTextureView.getLayoutParams();
		params.height = (int) mVideoHeight;
		params.width = (int) mVideoWidth;
		
		
		
		mVideoView = new TextureVideoPlayer(mTextureView);
//		mVideoView.setVideoURI(uri);
		try {
			mVideoView.setUrl(mPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// mVideoView.start();
		// mVideoView.requestFocus();
		// videoSeekto(1);
		
		
		mVideoView.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(VideoPlayer mp) {
				mTextureView.requestFocus();
				videoSeekto(mCurrentPosition);
				new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					VideoEditActivity.this.findViewById(R.id.video_scroll_mask).setVisibility(View.GONE);
				}
			}, 100);
			}
		});

		mVideoView.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(VideoPlayer vp) {
				// ActivityUtils.showToastWithIcon(VideoEditActivity.this,
				// getResources().getString(R.string.video_play_error), false);
				mVideoProgressBegin.clearAnimation();
				return true;
			}
		});

		mVideoView.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(VideoPlayer mp) {
				if (mVideoIcon != null) {
					mVideoIcon.setVisibility(View.VISIBLE);
				}
				if (mVideoView != null) {
					handleStartPosition();
					// mVideoView.seekTo(mStartPosition);
					videoSeekto(mStartPosition);
				}
			}
		});
		
		mTextureView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mVideoView.isPlaying()) {
					videoPause();
				} else {
					videoStart();
				}
			}
		});
	}

	// @Override
	// public void onWindowFocusChanged(boolean hasFocus) {
	// super.onWindowFocusChanged(hasFocus);
	// mVideoView.requestFocus();
	// }

	@Override
	protected void onPause() {
		super.onPause();
		videoPause();
		mCurrentPosition = mVideoView.getCurrentPosition();
		
//		mVideoView.release();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mVideoView.resume();
//		initVideoPlayer();
		if (mCurrentPosition > 0) {
			videoSeekto(mCurrentPosition);
		}
	}

	private void videoPause() {
		// handler.removeCallbacks(runnable);
		// int position = mVideoView.getCurrentPosition();
		// mVideoView.stopPlayback();
		// mVideoView.seekTo(position);
		if (mVideoView.canPause()) {
			mVideoView.pause();
			mVideoIcon.setVisibility(View.VISIBLE);
			mVideoProgressBegin.clearAnimation();
		}

	}

	private void videoSeekto(int msec) {
		// mVideoView.start();
		try {
			mVideoView.seekTo(msec);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// mVideoView.pause();
	}

	private void videoStart() {
		dismissTips();
		if (!mVideoView.isInPlaybackState()) {
			return;
		}
		handleStartPosition();
		handleEndPosition();
		// Log.e("=====",
		// "mStartPosition = "+mStartPosition+" , mEndPosition = "+mEndPosition);
		videoSeekto(mStartPosition);

		mVideoIcon.setVisibility(View.GONE);
		// final int oldX = mVideoProgressBegin.getLeft();

		TranslateAnimation translateAnimation = new TranslateAnimation(
				mVideoProgressBegin.getLeft(), mVideoProgressEnd.getLeft(), 0,
				0);
		// translateAnimation.setFillEnabled(true);
		// translateAnimation.setFillAfter(true);
		// 退出
		translateAnimation.setInterpolator(new LinearInterpolator());
		handleStartPosition();
		handleEndPosition();
		translateAnimation.setDuration(mEndPosition - mStartPosition);
		translateAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				mVideoProgressBegin.setVisibility(View.VISIBLE);
				mVideoProgressEnd.setVisibility(View.INVISIBLE);
//				mVideoView.start();
				mVideoView.play();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mVideoProgressBegin.setVisibility(View.INVISIBLE);
				mVideoProgressEnd.setVisibility(View.VISIBLE);
				videoPause();
				mVideoProgressBegin.clearAnimation();
			}
		});
		mVideoProgressBegin.startAnimation(translateAnimation);
		// handler.postDelayed(runnable, mEndPosition - mStartPosition);
		// mVideoView.start();

		// 3.0以下，主动刷新以下scrollview，否则无法显示
		if (VERSION.SDK_INT <= VERSION_CODES.GINGERBREAD_MR1) {
			videoLayoutView.invalidate();
		}

	}

	/**
	 * 计算视频截取后起始位置
	 */
	private void handleStartPosition() {
		mStartPosition = (int) ((mListView.getCurrentX() / mTimeListViewWidth) * mVideoeEditDuration);
	}

	/**
	 * 计算视频截取后终止位置
	 */
	private void handleEndPosition() {
		mEndPosition = (int) (mStartPosition + ((float) mEndSeekBar
				.getProgress() / mEndSeekBar.getMax()) * mVideoeEditDuration);
		mEndPosition = mEndPosition > (int) mDuration ? (int) mDuration
				: mEndPosition;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// if (receiver != null) {
		// this.unregisterReceiver(receiver);
		// }
		if (mVideoView != null) {
//			mVideoView.stopPlayback();
			mVideoView.release();
		}

		dismissPopup();
		dismissTips();
	}

	@Override
	protected void showProgress() {
		super.showProgress();
		if (mVideoView != null) {
			mVideoView.stopPlayback();
//			mVideoView.release();
		}
	}

	private void showSeekbarMinTips(int progress) {
		if (mSeekbarMinPopup == null) {
			mSeekbarMinPopup = new VideoPopup(this, Config.VIDEO_EDIT_MIN_TIPS);
		}
		showSeekbarTips(progress, mSeekbarMinPopup);
		new Handler().postDelayed(new Runnable() {
		@Override
		public void run() {
			if (mSeekbarMinPopup != null && mSeekbarMinPopup.isShowing()) {
				mSeekbarMinPopup.dismiss();
			}
		}
	}, 2000L);
	}

	private void showSeekbarTips(int progress, final VideoPopup popup) {
		if (popup == null || popup.isShowing()) {
			return;
		}
		int[] location = new int[2];
		mEndSeekBar.getLocationInWindow(location); // 获取在当前窗口内的绝对坐标
		// view.getLocationOnScreen(location);//获取在整个屏幕内的绝对坐标
		// thumb位置+左边距8dip- 二分之一popupwindow宽
		// int progress = mEndSeekBar.getProgress();
		// int progress = 650;
		int x = (int) (progress * (mTimeBarWidth / mEndSeekBar.getMax()))
				+ DpAndPxUtils.dip2px(edge);
		// y - popupwindow高度
		int y = location[1] - DpAndPxUtils.dip2px(34);

		// if (mStartPopup == null){
		if (progress > 650) {
			x -= DpAndPxUtils.dip2px(92);
			// popup = new VideoPopup(this, Config.VIDEO_EDIT_START_RIGHT_TIPS);
		} else {
			x -= DpAndPxUtils.dip2px(46);
			// if(progress <= 200){
			// popup = new VideoPopup(this, Config.VIDEO_EDIT_MIN_TIPS);
			// }else{
			// mStartPopup = new VideoPopup(this, Config.VIDEO_EDIT_START_TIPS);
			// }
		}
		// }
		// dismissOtherPopups(config);
		if (!popup.isShowing()) {
			// View container = getView().findViewById(R.id.camcorder_root);
			popup.setAnimationStyle(R.style.camcorder_popup_animation_style);
			popup.showAtLocation(videoLayoutView, Gravity.NO_GRAVITY, x, y);
			// mHandler.sendEmptyMessageDelayed(MESSAGE_DISMISS_NUX, 2000L);

		}

//		new Handler().postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				if (popup != null && popup.isShowing()) {
//					popup.dismiss();
//				}
//			}
//		}, 2000L);
	}

	/**
	 * 初始化选取终点提示
	 */
	private void showEndTips() {
		int progress = mEndSeekBar.getProgress();
		// if(mStartPopup == null){
		// if(progress > 650){
		// mStartPopup = new VideoPopup(this,
		// Config.VIDEO_EDIT_START_RIGHT_TIPS);
		// }else{
		// if(progress <= 200){
		// mStartPopup = new VideoPopup(this, Config.VIDEO_EDIT_MIN_TIPS);
		// }else{
		// mStartPopup = new VideoPopup(this, Config.VIDEO_EDIT_START_TIPS);
		// }
		// }
		// }
		if (mEndPopup == null) {
			if (progress > 650) {
				mEndPopup = new VideoPopup(this,
						Config.VIDEO_EDIT_END_RIGHT_TIPS);
			} else {
				mEndPopup = new VideoPopup(this, Config.VIDEO_EDIT_END_TIPS);
			}
		}
		showSeekbarTips(progress, mEndPopup);
	}

	/**
	 * 初始化选取起点提示
	 */
	private void showStartTips() {
		int[] location = new int[2];
		mListView.getLocationInWindow(location); // 获取在当前窗口内的绝对坐标
		// view.getLocationOnScreen(location);//获取在整个屏幕内的绝对坐标
		// 两张图片宽+左边距8dip- 二分之一popupwindow宽
		int x = mItemWidth * 2 + DpAndPxUtils.dip2px(edge)
				- DpAndPxUtils.dip2px(46);
		// y - popupwindow高度
		int y = location[1] - DpAndPxUtils.dip2px(37);
		if (mStartPopup == null)
			mStartPopup = new VideoPopup(this, Config.VIDEO_EDIT_START_TIPS);
		// dismissOtherPopups(config);
		if (!mStartPopup.isShowing()) {
			// View container = getView().findViewById(R.id.camcorder_root);
			mStartPopup
					.setAnimationStyle(R.style.camcorder_popup_animation_style);
			mStartPopup.showAtLocation(videoLayoutView, Gravity.NO_GRAVITY, x, y);
			// mHandler.sendEmptyMessageDelayed(MESSAGE_DISMISS_NUX, 2000L);

		}
//		new Handler().postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				if (mEndPopup != null && mEndPopup.isShowing()) {
//					mEndPopup.dismiss();
//				}
//			}
//		}, 2000L);

	}

	private void dismissPopup() {
		dismissTips();
		if (mSeekbarMinPopup != null && mSeekbarMinPopup.isShowing()) {
			mSeekbarMinPopup.dismiss();
		}
	}

	private void dismissTips(){
		if (mStartPopup != null && mStartPopup.isShowing()) {
			mStartPopup.dismiss();
		}
		if (mEndPopup != null && mEndPopup.isShowing()) {
			mEndPopup.dismiss();
		}
	}
	
	// private void dismissOtherPopups(Config config) {
	// if (mNuxPopup.getConfig() != config) {
	// mNuxPopup.dismiss();
	// mHandler.removeMessages(MESSAGE_DISMISS_NUX);
	// mNuxPopup = new VideoPopup(getActivity(), config);
	// }
	// }
}
