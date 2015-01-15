package com.netease.android.camera;

import android.content.pm.PackageManager;

public class CameraDetectionUtil {
	public static final String FEATURE_CAMERA_FRONT = "android.hardware.camera.front";

	public static boolean hasAnyCamera(PackageManager packageManager) {
		if ((packageManager.hasSystemFeature("android.hardware.camera"))
				|| (packageManager.hasSystemFeature(FEATURE_CAMERA_FRONT))) {
			return true;
		}
		return false;
	}
}