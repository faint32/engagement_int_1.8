package com.netease.engagement.adapter;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.WeakHashMap;

import uk.co.senab.photoview.PhotoView;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.netease.common.cache.CacheManager;
import com.netease.common.cache.file.StoreFile;
import com.netease.common.http.filedownload.FileDownloadListener;
import com.netease.common.http.filedownload.FileDownloadManager;
import com.netease.date.R;
import com.netease.engagement.widget.CircleProgress;
import com.netease.framework.widget.CircleProgressShaderView;
import com.netease.framework.widget.ToastUtil;
import com.netease.util.AnimationUtil;
import com.netease.util.AudioUtil;
import com.netease.util.PlatformUtil;

public class VideoImagePagerAdapter extends ImagePagerAdapter 
		implements OnCompletionListener, OnErrorListener {
	
	private static final boolean DEBUG = false;
	private static final String TAG = "VideoImagePagerAdapter";
	
	private static final long NETWORK_TOAST_INTERVAL = 24 * 3600 * 1000;
	
	private static long LastNetworkToastTime = 0;

	private OnClickListener mClickListener;
	
	private String mVideoUrl;
	
	private String mVideoPath;
	
	private boolean mIsFullActivity;
	
	private VideoHolder mVideoHolder;
	
	private VideoDownloadListener mDownloadListener;
	
	private WeakHashMap<View, View> mPlayBtns;
	
	private Context mContext;
	
	public VideoImagePagerAdapter(Context context, ArrayList<String> imgUrls) {
		super(context, imgUrls);
		
		mContext = context;
		
		mDownloadListener = new VideoDownloadListener();
		mPlayBtns = new WeakHashMap<View, View>();
	}
	
	public void setVideoUrl(String url) {
		mVideoUrl = url;
		
		StoreFile file = CacheManager.getStoreFile(url);
		if (file != null && file.exists()) {
			mVideoPath = file.getPath();
		}
	}
	
	public void setOnClickListener(OnClickListener listener) {
		mClickListener = listener;
	}
	
	@Override
	protected void renderView(View root, RelativeLayout container,
			CircleProgress progressbar, int position) {
		if (position == 0 && ! TextUtils.isEmpty(mVideoUrl)) {
			renderVideoView(root, container, progressbar);
		}
		else {
			super.renderView(root, container, progressbar, position);
		}
	}
	
	private void renderVideoView(View root, RelativeLayout container,
			CircleProgress progressbar) {
		VideoHolder holder = new VideoHolder();
		holder.mAdapter = this;
		
		LayoutInflater inflater = getLayoutInflater(root);
		View view = inflater.inflate(R.layout.view_video_in_head_layout, 
				container, false);
		view.setOnClickListener(mClickListener);
		
		PhotoView imageView = (PhotoView) view.findViewById(R.id.video_cover_img);
		imageView.setZoomable(false);
		
		holder.mVideoCover = imageView;
		holder.mVideoCoverPlay = view.findViewById(R.id.video_cover_play);
		holder.mVideoProgress = (CircleProgressShaderView) view.findViewById(
				R.id.video_progress);
		holder.mVideoProgress.setBitmap(R.drawable.icon_video_play);
		holder.mVideoProgress.setProgressShaderColor(0xFF666666);
		
		mPlayBtns.put(root, holder.mVideoCoverPlay);
		
		holder.mVideoView = (VideoView) view.findViewById(R.id.video_view);
		holder.mVideoView.setOnCompletionListener(this);
		holder.mVideoView.setOnErrorListener(this);
		holder.mVideoWifiToast = view.findViewById(R.id.video_wifi_toast);
		
		View videoContainer = view.findViewById(R.id.video_container);
		videoContainer.getLayoutParams().height = mScreenWidth;
		
		showImage(mImgUrls.get(0), imageView, progressbar);
		
		if (! TextUtils.isEmpty(mVideoPath)) {
			holder.mVideoProgress.setProgress(100);
		}
		
		if (mIsFullActivity) {
			holder.mVideoCoverPlay.setVisibility(View.INVISIBLE);
		}
		
		holder.mVideoView.setOnClickListener(mClickListener);
		holder.mVideoCover.setOnClickListener(mClickListener);
		holder.mVideoCoverPlay.setOnClickListener(mClickListener);
		holder.mVideoProgress.setOnClickListener(mClickListener);
		
		container.addView(view);
		
		root.setTag(holder);
	}
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		super.destroyItem(container, position, object);
		
		mPlayBtns.remove(object);
	}
	
	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		super.setPrimaryItem(container, position, object);
		
		if (hasVideo() && object != null && object instanceof View) {
			Object tag = ((View) object).getTag();
			
			if (tag != null && tag instanceof VideoHolder) {
				mVideoHolder = (VideoHolder) tag;
				mDownloadListener.setVideoHolder(mVideoHolder);
			}
			else {
				if (mVideoHolder != null) {
					stopPlay();
				}
				
				mDownloadListener.setVideoHolder(null);
				mVideoHolder = null;
			}
		}
	}
	
	public boolean hasVideo() {
		return ! TextUtils.isEmpty(mVideoUrl);
	}
	
	public boolean isFullActivity() {
		return mIsFullActivity;
	}
	
	public void enterFullActivity() {
		mIsFullActivity = true;

		if (mVideoHolder != null) {
			mVideoHolder.mVideoCoverPlay.setVisibility(View.INVISIBLE);
		}
		
		Collection<View> views = mPlayBtns.values();
		if (views != null) {
			for (View view : views) {
				if (view != null) {
					view.setVisibility(View.INVISIBLE);
				}
			}
		}
		
		
	}
	
	public void existFullActivity() {
		mIsFullActivity = false;
		
		setZoomEnable(false);
		
		stopPlay();
		
		if (mVideoHolder != null) {
			mVideoHolder.mVideoCoverPlay.setVisibility(View.VISIBLE);
		}
		
		Collection<View> views = mPlayBtns.values();
		if (views != null) {
			for (View view : views) {
				if (view != null) {
					view.setVisibility(View.VISIBLE);
				}
			}
		}
	}
	
	public boolean isPlaying() {
		boolean ret = false;
		
		if (mVideoHolder != null) {
			ret = mVideoHolder.mPlaying;
		}
		
		return ret;
	}
	
	public void startPlay() {
		VideoHolder holder = mVideoHolder;
		
		if (mIsFullActivity && holder != null) {
			if (holder.mVideoPath == null) {
				StoreFile file = CacheManager.getStoreFile(mVideoUrl);
				if (file != null && file.exists()) {
					String path = file.getPath();
					
					startPlay(holder, path);
					return ;
				}
			}
			else {
				if (new File(holder.mVideoPath).exists()) {
					startPlay(holder, null);
					return ;
				}
			}
			
			if (PlatformUtil.isMobileNetWork(holder.mVideoWifiToast.getContext())) {
				long time = System.currentTimeMillis();
				
				// 判断是否超过一定的时间
				if (time - LastNetworkToastTime > NETWORK_TOAST_INTERVAL) {
					LastNetworkToastTime = time;
					holder.mVideoWifiToast.setVisibility(View.VISIBLE);
				}
			}
			
			FileDownloadManager.getInstance().downloadFile(mVideoUrl, 
					null, null, mDownloadListener);
		}
	}
	
	public void hideCoverPlay() {
		if (mVideoHolder != null) {
			AnimationUtil.alphaHide(mVideoHolder.mVideoCoverPlay, 200);
		}
	}
	
	public void showCoverPlay() {
		if (mVideoHolder != null) {
			AnimationUtil.alphaShow(mVideoHolder.mVideoCoverPlay, 200);
		}
	}
	
	private static void startPlay(VideoHolder holder, String path) {
		AudioUtil.setMuteAll(holder.mVideoCover.getContext(), true, 0);
		
		if (path != null) {
			holder.mAdapter.mVideoPath = path;
		}
		
		if (! holder.mAdapter.isFullActivity()) {
			return ;
		}
		
		
		if (holder.mVideoPath == null) {
			holder.mVideoPath = path;
			holder.mVideoView.setVideoPath(path);
		}
		
		holder.mVideoProgress.setProgress(100);
		holder.mVideoProgress.setBitmap(R.drawable.icon_video_stop);
		holder.mVideoCover.setVisibility(View.INVISIBLE);
		holder.mVideoView.setVisibility(View.VISIBLE);
		holder.mVideoView.start();
		holder.mPlaying = true;
	}
	
	public void stopPlay() {
		VideoHolder holder = mVideoHolder;
		
		if (holder != null) {
			if (holder.mPlaying) {
				holder.mVideoView.stopPlayback();
				holder.mVideoProgress.setBitmap(R.drawable.icon_video_play);
				holder.mVideoView.setVisibility(View.INVISIBLE);
				
				AnimationUtil.alphaShow(holder.mVideoCover, 500);
				holder.mPlaying = false;
			}
			
			if (holder.mVideoProgress.getProgress() < 100) {
				holder.mVideoProgress.setProgress(1);
			}
			
			holder.mVideoWifiToast.setVisibility(View.INVISIBLE);
			
			AudioUtil.setMuteAll(holder.mVideoCover.getContext(), false, 0);
		}
	}
	
	public void onDestroy() {
		if (hasVideo()) {
			FileDownloadManager.getInstance().cancelDownload(mVideoUrl);
		}
	}
	
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		stopPlay();
		return false;
	}
	
	@Override
	public void onCompletion(MediaPlayer mp) {
		stopPlay();
	}
	
	public static class VideoHolder {
		VideoImagePagerAdapter mAdapter;
		
		View mVideoWifiToast;
		CircleProgressShaderView mVideoProgress;
		PhotoView mVideoCover;
		VideoView mVideoView;
		View mVideoCoverPlay;
		
		String mVideoPath;
		boolean mPlaying;
	}
	
	public static class VideoDownloadListener implements FileDownloadListener {

		WeakReference<VideoHolder> mVideoHolder;
		
		public void setVideoHolder(VideoHolder holder) {
			mVideoHolder = new WeakReference<VideoHolder>(holder);
		}
		
		private VideoHolder getVideoHolder() {
			VideoHolder holder = null;
			if (mVideoHolder != null) {
				holder = mVideoHolder.get();
			}
			
			return holder;
		}
		
		private CircleProgressShaderView getProgressView() {
			CircleProgressShaderView progress = null;
			
			VideoHolder holder = getVideoHolder();
			
			if (holder != null) {
				progress = holder.mVideoProgress;
			}
			
			return progress;
		}
		
		private View getVideoWifiToast() {
			View view = null;
			VideoHolder holder = getVideoHolder();
			
			if (holder != null) {
				view = holder.mVideoProgress;
			}
			
			return view;
		}
		
		@Override
		public void onSuccess(String path) {
			if (DEBUG)
				Log.e(TAG, "onSuccess: " + " path: " + path);
			
			
			VideoHolder holder = getVideoHolder();
			
			if (holder != null) {
				startPlay(holder, path);
				
				holder.mVideoWifiToast.setVisibility(View.INVISIBLE);
			}
		}

		@Override
		public void onFailed(String err, int errCode) {
			if (DEBUG)
				Log.e(TAG, "onFailed: " + "err: " + err + " errCode: " + errCode);
			
			
			CircleProgressShaderView progress = getProgressView();
			
			if (progress != null) {
				ToastUtil.showToast(progress.getContext(), 
						R.string.download_failed);
			}
			
			View view = getVideoWifiToast();
			if (view != null) {
				view.setVisibility(View.INVISIBLE);
			}
		}

		@Override
		public void onProgress(long current, long total, int percent, int speed) {
			if (DEBUG)
				Log.e(TAG, "onProgress: " + "percent: " + percent);
			
			CircleProgressShaderView progress = getProgressView();
			
			if (progress != null) {
				progress.setProgress(percent);	
			}
		}
		
	}
}
