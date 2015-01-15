package com.netease.android.video;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.netease.android.activity.LofterApplication;
import com.netease.android.camera.CameraSettings;
import com.netease.android.video.collections.ObservedStack;
import com.netease.android.video.model.Clip;
import com.netease.android.video.model.ClipStack;
import com.netease.android.video.model.Clip.ClipListener;
import com.netease.android.video.model.Clip.ClipState;
import com.netease.android.video.ui.ClipView;
import com.netease.android.video.ui.VideoFileUtil;

@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
public class ClipStackManager implements ClipListener {
	public static final String BUNDLE_KEY_HAS_IMPORTED_CLIPS = "hasImportedClips";
	private static final String TAG = "ClipStackManager";
	private ClipStack mClipStack = new ClipStack();
	private List<ClipStackManagerChangeListener> mClipStackObserverList = new ArrayList<ClipStackManagerChangeListener>();
	private Clip mCurrentClip;
	private boolean mHasImportedClips = false;

	public ClipStackManager() {
		mClipStack.setStackObserver(new ObservedStack.StackObserver<Clip>() {

			@Override
			public void onItemAdded(Clip clip) {
				for (ClipStackManagerChangeListener listener : mClipStackObserverList)
					listener.onClipAdded(clip);
			}

			@Override
			public void onItemRemoved(Clip clip) {
				if ((mClipStack.size() == 0) && mHasImportedClips) {
					setHasImportedClips(false);
				}
				for (ClipStackManagerChangeListener listener : mClipStackObserverList)
					listener.onClipRemoved(clip);
			}

		});
	}

	public void addClipStackListener(ClipStackManagerChangeListener listener) {
		mClipStackObserverList.add(listener);
	}

	public void addExistingClip(Clip clip) {
		Log.v(TAG, "Adding an existing clip " + clip.getVideoPath());
		mClipStack.add(clip);
		mCurrentClip = clip;
		mCurrentClip.addListener(this);
	}

	public Clip addNewClip(int duration) {
		mCurrentClip = new Clip(System.currentTimeMillis() + duration);
		mCurrentClip.setState(ClipState.RECORDING);
		mCurrentClip.setCameraId(CameraSettings
				.readPreferredCameraId(LofterApplication.getInstance()
						.getSharedPreferences("CAMERA_SETTINGS", 0)));
		mClipStack.add(mCurrentClip);
		mCurrentClip.addListener(this);
		return mCurrentClip;
	}

	public void cancelSoftDelete() {
		if (mClipStack.getLastClip() != null)
			mClipStack.getLastClip().setState(Clip.ClipState.RECORDED);
	}

	public void checkLastClip() {
		Clip clip = mClipStack.getLastClip();
		if ((clip != null)) {
			if (clip.getVideoPath() != null) {
				try {
					MediaMetadataRetriever retriever = new MediaMetadataRetriever();
					retriever.setDataSource(clip.getVideoPath());
					String str = retriever
							.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
					if (str != null) {
						Log.v(TAG, "duration: " + Long.valueOf(str));
						return;
					}
				} catch (Exception e) {
					Log.e(TAG, "Exception when retrieving metadata: " + e);
				}
			}
			Log.e(TAG, "No video file or too short; deleting");
			clip.setState(ClipState.INVALID);
			deleteLastClip();
		} else {
			Log.v(TAG, "No video file found");
		}

	}

	public void deleteLastClip() {
		Clip clip = mClipStack.getLastClip();
		if (clip != null) {
			if (clip.getVideoPath() != null) {
				VideoFileUtil.deleteFileAsync(clip.getVideoPath());
			} else {
				Log.v(TAG, "Deleted clip did not have a video file");
			}
			mClipStack.remove(clip);
		} else {
			Log.v(TAG, "Attempted to delete a non-existent clip");
		}
	}

	public void finishClip() {
		if (mCurrentClip != null) {
			mCurrentClip.setState(ClipState.RECORDED);
			mCurrentClip.setSystemStopTime(System.currentTimeMillis());
		}
	}

	public ClipStack getClipStack() {
		return mClipStack;
	}

	public Clip getCurrentClip() {
		return mCurrentClip;
	}

	public boolean getHasImportedClips() {
		return mHasImportedClips;
	}

	public int getRemainingDuration() {
		return ClipView.MAX_RECORD_DURATION - getTotalClipLength();
	}

	public int getTotalClipLength() {
		return mClipStack.getTotalClipLength();
	}

	public boolean hasRecordedClips() {
		boolean hasRecorded = false;
		for (Clip it : mClipStack) {
			if (it.getState() != ClipState.RECORDING) {
				hasRecorded = true;
				break;
			}
		}
		return hasRecorded;
	}

	public boolean isAlmostFull() {
		if (getRemainingDuration() < 125) {
			return true;
		}
		return false;
	}

	public boolean isFull() {
		if (getRemainingDuration() <= 0) {
			return true;
		}
		return false;
	}

	public boolean isInSoftDelete() {
		boolean isSoftdelete = false;
		if ((mClipStack.getLastClip() != null)
				&& (mClipStack.getLastClip().getState() == ClipState.SOFT_DELETED)) {
			isSoftdelete = true;
		}
		return isSoftdelete;
	}

	@Override
	public void onClipDurationChanged(Clip clip, long mDuration) {
		for (ClipStackManagerChangeListener it : mClipStackObserverList)
			it.onClipDurationChanged(clip);
		if (isAlmostFull()) {
			for (ClipStackManagerChangeListener it : mClipStackObserverList)
				it.onClipStackFull();
		}
	}

	@Override
	public void onClipStateChange(Clip clip, ClipState clipState) {
		for (ClipStackManagerChangeListener listener : mClipStackObserverList)
			listener.onClipChanged(clip, clipState);
	}

	public void removeClipStackListener(ClipStackManagerChangeListener listener) {
		mClipStackObserverList.remove(listener);
	}

	public void restoreClips(List<Clip> list) {
		Log.d(TAG, "Restoring clips:");
		for (Clip clip : list)
			addExistingClip(clip);
	}

	public void restoreInstanceState(Bundle bundle) {
		setHasImportedClips(bundle.getBoolean(BUNDLE_KEY_HAS_IMPORTED_CLIPS));
	}

	public void saveInstanceState(Bundle bundle) {
		bundle.putBoolean(BUNDLE_KEY_HAS_IMPORTED_CLIPS, mHasImportedClips);
	}

	public void setHasImportedClips(boolean hasImportedClips) {
		mHasImportedClips = hasImportedClips;
	}

	public void setVideoFile(String filePath) {
		if (new File(filePath).exists())
			mCurrentClip.setVideoFile(filePath);
	}

	public int size() {
		return mClipStack.size();
	}

	public void softDeleteClip() {
		if (mClipStack.getLastClip() != null)
			mClipStack.getLastClip().setState(Clip.ClipState.SOFT_DELETED);
	}

	public void updateClip() {
		mCurrentClip.setSystemStopTime(System.currentTimeMillis());
	}

	public abstract interface ClipStackManagerChangeListener {
		public abstract void onClipAdded(Clip clip);

		public abstract void onClipChanged(Clip clip, ClipState clipState);

		public abstract void onClipDurationChanged(Clip clip);

		public abstract void onClipRemoved(Clip clip);

		public abstract void onClipStackFull();
	}
}