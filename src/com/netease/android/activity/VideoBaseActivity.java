package com.netease.android.activity;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.netease.android.util.ActivityUtils;
import com.netease.android.video.util.VideoEditUtil;
import com.netease.android.widget.dialog.LofterProgressDialog;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;

public class VideoBaseActivity extends ActivityEngagementBase {
	protected float mOrgVideoWidth;// 原始视频宽
	protected float mOrgVideoHeight;// 原始视频高
	protected float mVideoHeight;
	protected float mVideoWidth;
	// 屏幕高宽
	protected float sWidth;
	protected float sHeight;
	protected LofterProgressDialog mProgress;
	protected boolean mInit;// 是否初始化过
	protected TextView mNextButton;
	
	protected String mPath = null;
//	protected long mClickTime = 0L;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setOveride(false);
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.video_slide_from_right, R.anim.video_hold);
		
		mPath = getIntent().getStringExtra("path");
		
		mProgress = new LofterProgressDialog(this,R.style.lofter_progress_dialog_fullscreen);

	}
	@Override
	protected void requestFeature() {
		 // 设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
              WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
	@Override
	protected void onPause() {
		super.onPause();
		if(mProgress != null && mProgress.isShowing()){
			mProgress.cancel();
		}
		if(mNextButton != null){
			mNextButton.setClickable(true);
			mNextButton.setEnabled(true);
		}

	}
	
	protected void showProgress() {
		if(mNextButton != null){
			mNextButton.setClickable(false);
			mNextButton.setEnabled(false);
		}
		if(mProgress != null && !mProgress.isShowing()){
			mProgress.show();
		}
	}
	
	protected void hideProgess() {
		if(mProgress != null){
			mProgress.dismiss();
		}
	}
	
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.video_hold, R.anim.video_slide_from_left);
	}
	
	/**
	 * 计算视频显示大小
	 */
	protected void handleVideoSize() {
		// 计算视频view大小
//		Bitmap bitmap = VideoEditUtil.getVideoFrame(0, mPath);
		Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(mPath, Images.Thumbnails.FULL_SCREEN_KIND);
		if(bitmap == null){
			bitmap = VideoEditUtil.getVideoFrame(1, mPath);
		}
		if(bitmap == null){
			return;
		}
		mOrgVideoHeight = bitmap.getHeight();
		mOrgVideoWidth = bitmap.getWidth();
		sWidth = (float) ActivityUtils.getSnapshotWidth(this);
		sHeight = (float) ActivityUtils.getSnapshotHeight(this);
		if (mOrgVideoHeight > mOrgVideoWidth) {
			mVideoHeight = mOrgVideoHeight / mOrgVideoWidth * sWidth;
			mVideoWidth = sWidth;
		} else {
			mVideoWidth = mOrgVideoWidth / mOrgVideoHeight * sWidth;
			mVideoHeight = sWidth;
		}
	}
	
	protected boolean isSupportGesture() {
		return false;
	}
	
	protected boolean isSupportSnapshot() {
		return false;
	}

}
