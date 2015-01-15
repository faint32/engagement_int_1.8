package com.netease.android.video.model.pendingmedia;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.netease.android.video.model.mediatype.MediaType;
import com.netease.android.video.model.mediatype.ShareableMedia;

public class PendingMedia implements ShareableMedia {
	private String mCaption;
	private List<ClipInfo> mClipInfoList = new ArrayList<ClipInfo>();
	private ArrayList<FaceBox> mFaceRectangles;
	private int mFilterType;
	private String mImageFilePath;
	private volatile boolean mInProgress;
	private String mKey;
	private String mMediaId;
	private MediaType mMediaType;
	private int mOriginalHeight;
	private int mOriginalWidth;
	private volatile int mProgress;
	private volatile PendingMedia.ProgressListener mProgressListener;
	private String mRenderedVideoFilePath;
	private volatile PendingMedia.Status mServerStatus = PendingMedia.Status.NOT_UPLOADED;
	private int mSourceType;
	private ClipInfo mStitchedClipInfo;
	private volatile PendingMedia.Status mTargetStatus = PendingMedia.Status.NOT_UPLOADED;
	private String mTimestamp;
	private String mVideoResult;
	private String mVideoSessionName = null;
	private List<VideoUploadUrl> mVideoUploadUrls;

	private PendingMedia(String name) {
		this.mKey = name;
		this.mTimestamp = name;
	}

	public static PendingMedia createImage(String paramString) {
		PendingMedia mMedia = new PendingMedia(paramString);
		mMedia.mMediaType = MediaType.PHOTO;
		return mMedia;
	}

	public static PendingMedia createImage(String paramString1,
			String paramString2) {
		PendingMedia media = createImage(paramString1);
		if ((paramString2 != null) && (!new File(paramString2).exists())) {
			media = null;
		} else {
			media.setImageFilePath(paramString2);
		}
		return media;
	}

	public static PendingMedia createVideo(String name) {
		PendingMedia pendingMedia = new PendingMedia(name);
		pendingMedia.mMediaType = MediaType.VIDEO;
		return pendingMedia;
	}

	private void notifyListener() {
		if (this.mProgressListener != null)
			this.mProgressListener.onProgressChange(this);
	}

	public String getCaption() {
		return this.mCaption;
	}

	public List<ClipInfo> getClipInfoList() {
		return this.mClipInfoList;
	}

	public String getDeviceTimestamp() {
		return this.mTimestamp;
	}

	public ArrayList<FaceBox> getFaceRectangles() {
		return this.mFaceRectangles;
	}

	public int getFilterType() {
		return this.mFilterType;
	}

	public String getImageFilePath() {
		return this.mImageFilePath;
	}

	public boolean getInProgress() {
		return this.mInProgress;
	}

	public String getKey() {
		return this.mKey;
	}

	public String getMediaId() {
		return this.mMediaId;
	}

	public MediaType getMediaType() {
		return this.mMediaType;
	}

	public int getOriginalHeight() {
		return this.mOriginalHeight;
	}

	public int getOriginalWidth() {
		return this.mOriginalWidth;
	}

	public int getProgress() {
		return this.mProgress;
	}

	public PendingMedia.ProgressListener getProgressListener() {
		return this.mProgressListener;
	}

	public String getRenderedVideoFilePath() {
		return this.mRenderedVideoFilePath;
	}

	public PendingMedia.Status getServerStatus() {
		return this.mServerStatus;
	}

	public int getShortestOriginalSide() {
		return Math.min(this.mOriginalHeight, this.mOriginalWidth);
	}

	public int getSourceType() {
		return this.mSourceType;
	}

	public ClipInfo getStitchedClipInfo() {
		return this.mStitchedClipInfo;
	}

	public PendingMedia.Status getTargetStatus() {
		return this.mTargetStatus;
	}

	public String getVideoResult() {
		return this.mVideoResult;
	}

	public String getVideoSessionName() {
		return this.mVideoSessionName;
	}

	public List<VideoUploadUrl> getVideoUploadUrls() {
		return this.mVideoUploadUrls;
	}

	public boolean hasOriginalSize() {
		if ((this.mOriginalWidth != 0) && (this.mOriginalHeight != 0)) {
			return true;
		}
		return false;
	}

	public void setCaption(String paramString) {
		this.mCaption = paramString;
	}

	public void setClipInfoList(List<ClipInfo> paramList) {
		this.mClipInfoList = paramList;
	}

	public void setDeviceTimestamp(String paramString) {
		this.mTimestamp = paramString;
	}

	public void setFaceRectangles(ArrayList<FaceBox> paramArrayList) {
		this.mFaceRectangles = paramArrayList;
	}

	public void setFilterType(int paramInt) {
		this.mFilterType = paramInt;
	}

	public void setImageFilePath(String paramString) {
		this.mImageFilePath = paramString;
	}

	public void setInProgress(boolean paramBoolean) {
		this.mInProgress = paramBoolean;
		notifyListener();
	}

	public void setMediaId(String paramString) {
		this.mMediaId = paramString;
	}

	public void setOriginalHeight(int paramInt) {
		this.mOriginalHeight = paramInt;
	}

	public void setOriginalWidth(int originalWidth) {
		this.mOriginalWidth = originalWidth;
	}

	public void setProgress(int progress) {
		this.mProgress = progress;
		notifyListener();
	}

	public void setProgressListener(PendingMedia.ProgressListener istener) {
		this.mProgressListener = istener;
	}

	public void setRenderedVideoFilePath(String paramString) {
		this.mRenderedVideoFilePath = paramString;
	}

	public void setServerStatus(PendingMedia.Status paramStatus) {
		this.mServerStatus = paramStatus;
		notifyListener();
	}

	public void setSourceType(int sourceType) {
		this.mSourceType = sourceType;
	}

	public void setStitchedClipInfo(ClipInfo clipInfo) {
		this.mStitchedClipInfo = clipInfo;
	}

	public void setTargetStatus(PendingMedia.Status status) {
		this.mTargetStatus = status;
	}

	public void setVideoResult(String videoResult) {
		this.mVideoResult = videoResult;
	}

	public void setVideoSessionName(String sessionName) {
		this.mVideoSessionName = sessionName;
	}

	public void setVideoUploadUrls(List<VideoUploadUrl> list) {
		this.mVideoUploadUrls = list;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("Media type:");
		if (this.mMediaType == MediaType.PHOTO) {
			sb.append("Photo").append("\nID: ").append(this.mMediaId)
					.append("\n").append("\nServer Status: ")
					.append(this.mServerStatus.toString())
					.append("\nTarget Status: ")
					.append(this.mTargetStatus.toString());
		} else if (this.mMediaType == MediaType.VIDEO) {
			sb.append("\nSession name: ").append(this.mVideoSessionName);
			sb.append("\nRendered Video Path: ").append(
					this.mRenderedVideoFilePath);
		}
		return sb.toString();
	}

	public enum Status {
		NOT_UPLOADED, UPLOADED, CONFIGURED, CREATED_MEDIA, UPLOADED_VIDEO;
	}

	public abstract interface ProgressListener {
		public abstract void onProgressChange(PendingMedia paramPendingMedia);
	}
}