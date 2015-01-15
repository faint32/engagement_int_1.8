package com.netease.android.video.util;

import android.content.Context;
import android.os.Build;

import com.netease.android.util.NetworkUtil;

public class VideoFeatureUtil {
	private static boolean isBlacklistedDevice() {
		return Build.MODEL.startsWith("LG-E61");
	}

	public static boolean meetsVideoCaptureRequirements() {
		if ((Build.VERSION.SDK_INT >= 14) && (!isBlacklistedDevice())) {
			return true;
		}
		return false;
	}

	public static boolean needsLegacyRendering() {
		if (Build.VERSION.SDK_INT < 16) {
			return true;
		}
		return false;
	}

	public static boolean needsLegacyVideoPlayer() {
		if (Build.VERSION.SDK_INT < 14) {
			return true;
		}
		return false;
	}

	public static boolean shouldAutoplay() {
		if (!needsLegacyVideoPlayer()) {
			return true;
		}
		return false;
	}

	public static boolean shouldPrefetch(Context context) {
		if (NetworkUtil.isWifi(context)) {
			return true;
		}
		return false;
	}

	public static boolean supportsStabilization() {
		return Build.MODEL.equals("Nexus 4");
	}
}