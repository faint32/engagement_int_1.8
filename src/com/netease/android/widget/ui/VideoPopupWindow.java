package com.netease.android.widget.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.android.util.ActivityUtils;
import com.netease.android.video.player.SurfaceViewVideoPlayer;
import com.netease.android.video.player.VideoPlayer;
import com.netease.date.R;

public class VideoPopupWindow extends Dialog {

    private static final String tag = "VideoPopupWindow";

    private View window;
    private FrameLayout video_wrapper;
    private ImageView image;
    private View video_play;
    private View video_action;

    private Context context;
    private String videoCover;
    private String videoUrl;
    private int screenWidth;

    private VideoPlayer mVideoPlayer;
    private View videoDownloadingView;
    private TextView videoDownloadPercentView;
    private Animation mCoverPhotoFadeInAnimation;
    private Animation mCoverPhotoFadeOutAnimation;

    public VideoPopupWindow(Context context, String videoCover, String videoUrl) {
        super(context, R.style.lofter_video_dialog);
        this.context = context;
        this.videoCover = videoCover;
        this.videoUrl = videoUrl;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        window = inflater.inflate(R.layout.video_popup_window, null);
        setContentView(window);

        videoDownloadingView = inflater.inflate(R.layout.video_ui_image_scan_wait, null);
        videoDownloadPercentView = (TextView) videoDownloadingView.findViewById(R.id.processNumber);

        mCoverPhotoFadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.video_image_fade_in);
        mCoverPhotoFadeOutAnimation = AnimationUtils.loadAnimation(context, R.anim.video_image_fade_out);

        window.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void initView() {
        video_wrapper = (FrameLayout) window.findViewById(R.id.video_wrapper);
        image = (ImageView) window.findViewById(R.id.image);
        video_play = window.findViewById(R.id.video_play_button);
        video_action = window.findViewById(R.id.video_action);

        screenWidth = context.getResources().getDisplayMetrics().widthPixels;

        ViewGroup.LayoutParams params = video_wrapper.getLayoutParams();
        params.height = screenWidth;
        params.width = screenWidth;
        video_wrapper.setLayoutParams(params);

        params = image.getLayoutParams();
        params.height = screenWidth;
        params.width = screenWidth;
        image.setLayoutParams(params);

        getVideoPlayer().bindView(video_wrapper, 0);

        video_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getVideoPlayer().isPlaying()) {
                    getVideoPlayer().pause();
                    video_play.setVisibility(View.VISIBLE);
                }   else if (getVideoPlayer().canPlay() && getVideoPlayer().isPaused()) {
                    getVideoPlayer().play();
                    video_play.setVisibility(View.INVISIBLE);
                } else if (getVideoPlayer().canPlay() && getVideoPlayer().isPlaybackCompleted()) {
                    getVideoPlayer().play();
                    video_play.setVisibility(View.INVISIBLE);
                    image.startAnimation(mCoverPhotoFadeOutAnimation);
                } else {
                     prepareVideo();
                }
            }
        });
    }

    private void prepareVideo() {
    	//TODO: 提前下载视频 videoUrl
//                @Override
//                public void onError() {
//                    stopVideo(true);
//                }
//
//                @Override
//                public void onDownloading(int percentage) {
//                    videoDownloadPercentView.setText(percentage + "%");
//                }
//
//                @Override
//                public void onDownloadStart() {
//                    videoDownloadPercentView.setText("0%");
//                    if (videoDownloadingView.getParent() != null) {
//                        ((FrameLayout)videoDownloadingView.getParent()).removeView(videoDownloadingView);
//                    }
//                    ((FrameLayout)image.getParent()).addView(videoDownloadingView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
//                    videoDownloadingView.requestLayout();
//                }
    }

    private VideoPlayer getVideoPlayer()
    {
        if(mVideoPlayer == null)
        {
            mVideoPlayer = new SurfaceViewVideoPlayer();
            mVideoPlayer.setOnErrorListener(new VideoPlayer.OnErrorListener(){

                @Override
                public boolean onError(VideoPlayer videoPlayer) {
                    stopVideo(true);
                    return true;
                }
            });
            mVideoPlayer.setOnPreparedListener(new VideoPlayer.OnPreparedListener(){

                @Override
                public void onPrepared(VideoPlayer videoPlayer) {
                    video_play.setVisibility(View.INVISIBLE);
                    image.startAnimation(mCoverPhotoFadeOutAnimation);
                    video_wrapper.requestLayout();
                    videoPlayer.play();
                }
            });
            mVideoPlayer.setOnCompletionListener(new VideoPlayer.OnCompletionListener(){

                @Override
                public void onCompletion(VideoPlayer videoPlayer) {
//                    image.startAnimation(mCoverPhotoFadeInAnimation);
//                    video_play.setVisibility(View.VISIBLE);
                    videoPlayer.play();
                }
            });
        }
        return mVideoPlayer;
    }

    private void stopVideo(boolean hasError) {
        //getVideoPlayer().removeView();
        //getVideoPlayer().reset();

        ((FrameLayout)image.getParent()).removeView(videoDownloadingView);
        videoDownloadingView.invalidate();
        image.startAnimation(mCoverPhotoFadeInAnimation);
        video_play.setVisibility(View.VISIBLE);

        if (hasError) {
            ActivityUtils.showToastWithIcon(context, "该手机暂不支持视频播放", false);
        }
    }

    public void releaseVideo()
    {
        if(mVideoPlayer != null)
        {
            stopVideo(false);
            mVideoPlayer.release();
            mVideoPlayer = null;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        initView();
    }

    @Override
    protected void onStop() {
        releaseVideo();
        super.onStop();
    }


    @Override
    public void show() {
        super.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                prepareVideo();
            }
        }, 500);
    }
}
