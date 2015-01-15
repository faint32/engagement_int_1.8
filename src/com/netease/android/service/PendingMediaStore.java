package com.netease.android.service;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.util.Log;

import com.netease.android.activity.LofterApplication;
import com.netease.android.video.model.pendingmedia.PendingMedia;
import com.netease.android.video.ui.VideoFileUtil;

public class PendingMediaStore {
	private static final String TAG = "PendingMediaStore";
	private static PendingMediaStore sInstance;
	final Map<String, PendingMedia> mPendingMediaMap = new ConcurrentHashMap<String, PendingMedia>();

	public static PendingMediaStore getInstance() {
		if (sInstance == null)
			sInstance = new PendingMediaStore();
		return sInstance;
	}

	public void clearUnusedSessions() {
		// HashSet<String> hashSet = new HashSet<String>();
		// for (PendingMedia media : mPendingMediaMap.values())
		// hashSet.add(media.getVideoSessionName());
		// File vidoesDir = new File(VideoFileUtil.getClipsPath(mContext));
		// File[] sessionDirs = vidoesDir.listFiles();
		// if ((vidoesDir.exists()) && (sessionDirs != null)) {
		// for (File dir : sessionDirs) {
		// if (!hashSet.contains(dir.getName())) {
		// Log.v(TAG, "Deleting session " + dir.getName());
		// VideoFileUtil.deleteFiles(dir.getPath(), true);
		// }
		// }
		// }
	}

	public PendingMedia getAbandonedSession() {
		PendingMedia media = null;
		File vidoesDir = new File(VideoFileUtil.getClipsPath(LofterApplication.getInstance()));
		File[] sessionDirs = vidoesDir.listFiles();
		if ((vidoesDir.exists()) && (sessionDirs != null)) {
			for (File dir : sessionDirs) {
				boolean recovery = true;
				File[] videos = dir.listFiles();
				if (videos != null && videos.length > 0) {
					for (File file : videos) {
						if (VideoFileUtil.isStitchedVideoFile(file)) {
							recovery = false;
							break;
						}
					}
				} else {
					dir.delete();
					recovery = false;
				}
				if (recovery) {
					String sessionName = dir.getName();
					media = PendingMedia.createVideo(sessionName);
					media.setVideoSessionName(sessionName);
					// mPendingMediaMap.put(dir.getName(), media);
					break;
				}
			}
		}
		return media;
	}

	public void put(String name, PendingMedia pendingMedia) {
		// mPendingMediaMap.put(name, pendingMedia);
	}

	public static void remove(String filePath) {
		String videosDir = VideoFileUtil.getClipsPath(LofterApplication.getInstance());
		if (filePath.startsWith(videosDir)) {
			File sessionDir = new File(filePath).getParentFile();
			if (sessionDir != null) {
				Log.v(TAG,
						"Finished configure, render is done, deleting clips & stitched file: "
								+ sessionDir.getName());
				VideoFileUtil.deleteFiles(sessionDir.getPath(), true);
			}
		}
	}

	public void removeUnconfiguredMedia() {
	}
}