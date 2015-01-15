package com.netease.android.video.camera;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VideoRecorderDelegateListener implements
		VideoRecorderStateChangeListener {
	private List<VideoRecorderStateChangeListener> mListeners;

	public VideoRecorderDelegateListener() {
		mListeners = new ArrayList();
	}

	public void addListener(VideoRecorderStateChangeListener changeListener) {
		mListeners.add(changeListener);
	}

	public void onVideoRecorderStateChange(String fileName,
			VideoRecorderStateChangeListener.VideoState videoState, long time) {
		Iterator localIterator = this.mListeners.iterator();
		while (localIterator.hasNext())
			((VideoRecorderStateChangeListener) localIterator.next())
					.onVideoRecorderStateChange(fileName, videoState, time);
	}

	public void removeListener(VideoRecorderStateChangeListener changeListener) {
		mListeners.remove(changeListener);
	}
}