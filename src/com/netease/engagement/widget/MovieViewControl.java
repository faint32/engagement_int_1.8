package com.netease.engagement.widget;
/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import com.netease.date.R;

public class MovieViewControl implements MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener,MediaPlayer.OnPreparedListener{

    private VideoView mVideoView;
    private View mProgressView;
    private Uri mUri;
    
    private int mPositionWhenPaused = -1;
    private boolean mWasPlayingWhenPaused = false;
    private MediaController mMediaController;

    public MovieViewControl(View rootView, Context context, String videoUri) {
    	if(TextUtils.isEmpty(videoUri)){
    		return ;
    	}
        
        mVideoView = (VideoView) rootView.findViewById(R.id.video_view);
        mProgressView = rootView.findViewById(R.id.progress_indicator);

        mUri = Uri.parse(videoUri);

        mVideoView.setOnErrorListener(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnPreparedListener(this);

        mMediaController = new MediaController(context);
        mVideoView.setMediaController(mMediaController);

        // make the video view handle keys for seeking and pausing
        mVideoView.requestFocus();
        mVideoView.setVideoURI(mUri);
        mVideoView.start();
    }

    public void onPause() {
    	if (null == mVideoView) {
    		return;
    	}
        mPositionWhenPaused = mVideoView.getCurrentPosition();
        mWasPlayingWhenPaused = mVideoView.isPlaying();
        mVideoView.pause();
    }

    public void onResume() {
    	if (null == mVideoView) {
    		return;
    	}
        if (mPositionWhenPaused >= 0) {
            if (mWasPlayingWhenPaused) {
                mVideoView.start();
                mVideoView.seekTo(mPositionWhenPaused);
                mMediaController.show(0);
            }
            mPositionWhenPaused = -1;
        }
    }

    @Override
    public boolean onError(MediaPlayer player, int arg1, int arg2) {
        mProgressView.setVisibility(View.GONE);
        clear();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
    	clear();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mProgressView.setVisibility(View.GONE);
    }

	public void clear() {
		if (null != mVideoView) {
			mVideoView.stopPlayback();
			mVideoView.setOnErrorListener(null);
	        mVideoView.setOnCompletionListener(null);
	        mVideoView.setOnPreparedListener(null);
	        mVideoView.setMediaController(null);
		}
		mMediaController = null;
		mVideoView = null;
		if (null != mProgressView) {
			mProgressView.setVisibility(View.GONE);
		}
		mProgressView = null;
		mUri = null;
	}
}
