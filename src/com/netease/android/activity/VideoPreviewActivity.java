package com.netease.android.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.netease.android.util.ActivityUtils;
import com.netease.android.video.util.VideoEditUtil;
import com.netease.android.video.util.VideoFeatureUtil;
import com.netease.android.widget.ui.VideoView;
import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.Utils.EgmUtil;

public class VideoPreviewActivity extends VideoBaseActivity {
    private VideoView mVideoView;
    private View progress_wrapper;
    private ProgressBar videoProgreass;
    private Handler handler;
    private ProgressThread progressThread;
    private ImageView video_cover;
    private View video_play_icon;

    private boolean isFrontCamera;
    private boolean paused = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFrontCamera = getIntent().getIntExtra("cameraId", 0) != 0;
        setContentView(R.layout.video_preview);


        mNextButton = (TextView)this.findViewById(R.id.video_next);

        handler = new Handler();
        progressThread = new ProgressThread();

        initVideoView();
        initOperation();
        
    }

    private void initOperation() {

        progress_wrapper.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        findViewById(R.id.bottom_bar).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        video_play_icon.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mVideoView.isPlaying()) {
                            videoPause();
                            progressThread.pause();
                        } else {
                            progressThread.start();
                            handler.post(progressThread);
                            videoStart();
                        }
                    }
                }
        );
        this.findViewById(R.id.video_back).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        VideoPreviewActivity.this.onBackPressed();
                    }
                }
        );
        mNextButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    	showProgress();
						go2Next();
                    }
                }
        );

    }

    private void go2Next(){
    		videoStop();
        Intent intent = new Intent(VideoPreviewActivity.this,
                VideoCoverActivity.class);
        intent.putExtra("path", mPath);
        intent.putExtra("queueId", getIntent().getStringExtra("queueId"));
        intent.putExtra("duration", Long.valueOf(mVideoView.getDuration()));
        intent.putExtra("isCamera", true);
        intent.putExtra("scrollY",getResources().getDimensionPixelSize(R.dimen.video_record_top_bar));
        
//		intent.putExtra("videoheight", mOrgVideoHeight);
//		intent.putExtra("videowidth", mOrgVideoWidth);
		// 参加话题参数
		intent.putExtras(getIntent());
        VideoPreviewActivity.this.startActivity(intent);
    }
    
    /**
     * 初始化视频view
     */
    private void initVideoView() {

        videoProgreass = (ProgressBar) findViewById(R.id.video_progress);
        mVideoView = (VideoView) this.findViewById(R.id.video_preview_view);
        progress_wrapper = findViewById(R.id.progress_wrapper);
        video_cover = (ImageView) findViewById(R.id.video_cover);
        video_play_icon = findViewById(R.id.video_play_icon);

        if (VideoFeatureUtil.needsLegacyVideoPlayer()) {
            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(mPath, MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
            if(bitmap == null){
                bitmap = VideoEditUtil.getVideoFrame(1, mPath);
            }
            if (bitmap != null) {
                if (bitmap.getWidth() > bitmap.getHeight()) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(isFrontCamera ? 270 : 90);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                }
                video_cover.setImageBitmap(bitmap);
                video_cover.setVisibility(View.VISIBLE);
            }
        }

        ViewGroup.LayoutParams param = progress_wrapper.getLayoutParams();
        param.height = ActivityUtils.getSnapshotHeight(this) -  ActivityUtils.getSnapshotWidth(this) 
            		- getResources().getDimensionPixelSize(R.dimen.video_record_top_bar)
            		- getResources().getDimensionPixelSize(R.dimen.video_record_bottom_bar);


        mVideoView.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mVideoView.seekTo(1);
                    }
                }, 210);
            }
        });
        mVideoView.setOnErrorListener(new OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
            	ToastUtil.showToast(VideoPreviewActivity.this,R.string.video_play_error);
            	progressThread.stop(false);
                return true;
            }
        });
        mVideoView.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                progressThread.stop(true);
                video_play_icon.setBackgroundResource(R.drawable.video_play_icon_selector);
            }
        });


    }

    private class ProgressThread implements Runnable {
        int lastPosition;
        int max;

        private static final int STARTED  = 1;
        private static final int PAUSED  = 2;
        private static final int STOPED  = 3;

        private int state = STOPED;

        private int step = 0;

        @Override
        public void run() {
            if (state == STARTED) {
                if (mVideoView.isPlaying()) {
                    int currentPosition= mVideoView.getCurrentPosition();


                    if (currentPosition < lastPosition) {
                        currentPosition = lastPosition;
                    }
                    lastPosition = currentPosition;


                    int progress = currentPosition;
                    if (progress >= max) {
                        progress = max;
                    }
                    videoProgreass.setProgress(progress);

                }
                handler.postDelayed(this, 15);

            }
            else if (state == STOPED) {
                videoProgreass.setProgress(0);
            }

        }

        public void start() {
            max = mVideoView.getDuration();
            if (max > (EgmConstants.VIDEO_MAX_LENGH *1000)) {
                max = (EgmConstants.VIDEO_MAX_LENGH *1000);
            }
            videoProgreass.setMax(max);
            this.state = STARTED;
        }

        public void stop(boolean completed) {
            this.state = STOPED;
            lastPosition = 0;
            if (completed) {
                videoProgreass.setProgress(max);
            }
            handler.post(this);
        }

        public void pause() {
            state = PAUSED;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        //videoPause();
        progressThread.stop(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mVideoView.stopPlayback();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mVideoView.setVideoURI(Uri.parse(mPath));
    }

    @Override
    protected void onResume() {
        super.onResume();
        //mVideoView.resume();
        video_cover.setVisibility(View.VISIBLE);
    }

    private void videoPause(){
        paused = true;
        mVideoView.pause();
        video_play_icon.setBackgroundResource(R.drawable.video_play_icon_selector);
    }

    private void videoStart(){
        if (!paused) {
            mVideoView.seekTo(1);
        }
        mVideoView.start();
        paused = false;
        video_cover.setVisibility(View.GONE);
        video_play_icon.setBackgroundResource(R.drawable.video_stop_icon_selector);
    }
    private void videoStop(){
    	    if(mVideoView.isPlaying()){
	        mVideoView.stopPlayback();
	        video_cover.setVisibility(View.GONE);
	        video_play_icon.setBackgroundResource(R.drawable.video_play_icon_selector);
    	    }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoView.stopPlayback();
    }
    
	@Override
	protected void showProgress(){
		super.showProgress();
		if(mVideoView != null){
			mVideoView.stopPlayback();
		}
	}

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    		if (mVideoView.isPlaying()) {
            videoPause();
            progressThread.pause();
        } 
		String content = getString(R.string.video_abandon);
		View.OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				int which = (Integer) v.getTag();
				if (which == DialogInterface.BUTTON_POSITIVE) {
                     finish();
				}
			}
		};

		AlertDialog dialog = EgmUtil.createEgmBtnDialog(this, null, content,
				 getString(R.string.cancel),getString(R.string.confirm),
				listener, true);
		dialog.show();

	}
}
