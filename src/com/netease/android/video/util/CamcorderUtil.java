package com.netease.android.video.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;
import android.os.Build;
import android.util.Log;

import com.netease.android.camera.CameraSettings;
import com.netease.android.camera.Util;

@TargetApi(9)
public class CamcorderUtil {
	public static final String TAG = "CamcorderUtil";
	private static final List<String> aacExceptionList = Arrays
			.asList(new String[] { "GT-N7000", "GT-N7000B", "GT-N7005",
					"SHV-E160K", "SHV-E160L", "SHV-E160S", "SGH-I717",
					"SC-05D", "SGH-T879", "GT-I9220", "GT-I9228", "SCH-I889" });
	private static List<Integer> supportedProfilesInHighestOrder;
	private static List<Integer> supportedProfilesInLowestOrder;

	static {
		supportedProfilesInHighestOrder = Arrays.asList(new Integer[] { 1, 6,
				5, 4 });
		supportedProfilesInLowestOrder = new ArrayList(
				supportedProfilesInHighestOrder);
		Collections.reverse(supportedProfilesInLowestOrder);
	}

	public static boolean enforceLowQualityVideo() {
		if ((VideoFeatureUtil.needsLegacyRendering()) || (Util.isS2Model())
				|| ("EK-GC100".equals(Build.MODEL))
				|| ("Nexus S".equals(Build.MODEL))
				|| ("Nexus S 4G".equals(Build.MODEL))) {
			return true;
		}
		return false;
	}

	public static boolean isMiSeria() {
		if (Build.BRAND.equalsIgnoreCase("xiaomi")) {
			return true;
		}
		return false;
	}

	@TargetApi(11)
	public static Camera.Size getCorrectedPreferredPreviewSizeForVideo(
			Camera.Parameters parameters, List<Camera.Size> list) {
		if ((Util.isS4Model()) || (Util.isS2Model())) {
			return (Camera.Size) list.get(0);
		}
		return parameters.getPreferredPreviewSizeForVideo();
	}

	@TargetApi(13)
	private static Point getDefaultDisplaySize(Activity activity, Point point) {
		activity.getWindowManager().getDefaultDisplay().getSize(point);
		return point;
	}

	public static int getDisplayOrientation(int mDisplayRotation, int cameraId) {
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, cameraInfo);
		if (cameraInfo.facing == 1) {
			return (360 - (mDisplayRotation + cameraInfo.orientation) % 360) % 360;
		} else {
			return (360 + (cameraInfo.orientation - mDisplayRotation)) % 360;
		}
	}

	public static boolean isExceptionForAAC() {
		if (Build.BOARD.startsWith("MSM8660")
				|| aacExceptionList.contains(Build.MODEL)) {
			return true;
		}
		return false;
	}

	public static boolean isSupported(String feature, List<String> features) {
		if ((features != null) && (features.indexOf(feature) >= 0)) {
			return true;
		}
		return false;
	}

	public static CamcorderProfile selectBestSupportedCamcorderProfile(
			int cameraId) {
		List<Integer> profiles = supportedProfilesInLowestOrder;
		CamcorderProfile camcorderProfile = CamcorderProfile.get(cameraId,
				CamcorderProfile.QUALITY_LOW);
		// if (enforceLowQualityVideo()) {
		//
		// } else {
		// profiles = supportedProfilesInHighestOrder;
		// }
		if (isMiSeria()) {
			profiles = Arrays.asList(new Integer[] { 5, 4 });
		}
		for (int quality : profiles) {
			try {
				camcorderProfile = CamcorderProfile.get(cameraId, quality);
				Log.v(TAG, "grabbed profile " + quality);
				return camcorderProfile;
			} catch (RuntimeException exception) {
				Log.e(TAG, "error trying to grab profile " + quality
						+ " trying another profile");
			}
		}
		Log.v(TAG, "Phone had to use low quality instead of 480p");
		return camcorderProfile;
	}

	public static void setFocusModeForCamera(Camera.Parameters paramParameters) {
		if (Util.isS4Model()) {
			paramParameters.setFocusMode("auto");
		} else {
			if (isSupported("continuous-video",
					paramParameters.getSupportedFocusModes()))
				paramParameters.setFocusMode("continuous-video");
			else
				Log.e("CamcorderUtil", "No auto focus mode found!");
		}
	}

	public static void setWhiteBalance(Camera.Parameters paramParameters) {
		if (isSupported("auto", paramParameters.getSupportedWhiteBalance()))
			paramParameters.setWhiteBalance("auto");
	}

	public static boolean supportsAutoFocus() {
		if ("Galaxy Nexus".equals(Build.MODEL)) {
			return false;
		}
		return true;
	}

	public static void updateStopRecordingDelay(int deley,
			SharedPreferences preferences) {
		int count = CameraSettings.getStopDelaySampleCount(preferences);
		CameraSettings.setStopDelayAverageAndSampleCount(
				preferences,
				(deley + count
						* CameraSettings
								.getStopRecordingDelayAverage(preferences))
						/ (count + 1), count);
	}

	@TargetApi(9)
	public static Camera.CameraInfo getCameraInfo(int cameraId) {
		CameraInfo info = new CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		return info;
	}
}