package com.netease.android.camera;

import java.math.BigDecimal;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;

@TargetApi(9)
public class CameraSettings {
	private static final String TAG = "CameraSettings";
	public static final int CURRENT_LOCAL_VERSION = 0;
	public static final int CURRENT_VERSION = 0;
	public static final String EXPOSURE_DEFAULT_VALUE = "0";
	public static final String KEY_CAMERA_ID = "pref_camera_id_key";
	public static final String KEY_EXPOSURE = "pref_camera_exposure_key";
	public static final String KEY_FLASH_MODE = "pref_camera_flashmode_key";
	public static final String KEY_FOCUS_MODE = "pref_camera_focusmode_key";
	public static final String KEY_LOCAL_VERSION = "pref_local_version_key";
	public static final String KEY_PICTURE_SIZE = "pref_camera_picturesize_key_V4";
	public static final String KEY_SCENE_MODE = "pref_camera_scenemode_key";
	public static final String KEY_TAP_TO_FOCUS_PROMPT_SHOWN = "pref_tap_to_focus_prompt_shown_key";
	public static final String KEY_VERSION = "pref_version_key";
	public static final String KEY_WHITE_BALANCE = "pref_camera_whitebalance_key";
	private static final String KEY_RECORDER_STOP_DELAY_AVERAGE = "pref_stop_delay_average_key";
	public static final int MAX_SUPPORTED_WIDTH = 3000;
	private static final int NOT_FOUND = -1;

	@TargetApi(9)
	public static void initialCameraPictureSize(Context context,
			Parameters parameters,double expectedRatio) {
		List<Size> supported = parameters.getSupportedPictureSizes();
		if (supported == null)
			return;
		// Display display = ((Activity) context).getWindowManager()
		// .getDefaultDisplay();
		// int containerWidth = display.getWidth();
		// int containerHeight = display.getHeight();
		Camera.Size candidate = null;
		double minDiff = Double.MAX_VALUE;
		// BigDecimal b = new java.math.BigDecimal((double) containerHeight
		// / containerWidth);
		for (Size size : supported) {
			Log.v(TAG, "size.width:" + size.width + ",size.height:"
					+ size.height);
			// float heightDif = Math.abs(size.width / 4.0F * 3.0F -
			// size.height);
			// float widthDif = 0.1F * size.width;
			// if (heightDif < widthDif) {
			// if (candidate != null) {
			// if ((size.height <= candidate.height))
			// continue;
			// }
			// candidate = size;
			// }
			double s = new java.math.BigDecimal((double) size.width
					/ size.height).setScale(2, BigDecimal.ROUND_HALF_UP)
					.doubleValue();
			double ratioDif = Math.abs(s - expectedRatio);
			if (ratioDif < minDiff) {
				candidate = size;
				minDiff = ratioDif;
			} else if (ratioDif == minDiff && candidate != null) {
				if ((size.height <= candidate.height))
					continue;
				candidate = size;
			}
		}
		// candidate = Util.getOptimalPreviewSize((Activity) context, supported,
		// (double) DpAndPxUtils.getScreenHeightPixels() /
		// DpAndPxUtils.getScreenWidthPixels());
		if (candidate != null) {
			Log.v(TAG, "candidate.width:" + candidate.width
					+ ",candidate.height:" + candidate.height);
			parameters.setPictureSize(candidate.width, candidate.height);
			return;
		}
		Log.e(TAG, "No supported picture size found");
	}

	public static int readExposure(SharedPreferences comboPreferences) {
		String exposure = comboPreferences.getString(
				"pref_camera_exposure_key", "0");
		try {
			return Integer.parseInt(exposure);
		} catch (Exception exception) {
			Log.e(TAG, "Invalid exposure: " + exposure);
			return 0;
		}
	}

	public static int readPreferredCameraId(SharedPreferences pref) {
		return Integer.parseInt(pref.getString(KEY_CAMERA_ID, "0"));
	}

	public static boolean setCameraPictureSize(String candidate,
			List<Size> supported, Parameters parameters) {
		int index = candidate.indexOf('x');
		if (index == NOT_FOUND)
			return false;
		int width = Integer.parseInt(candidate.substring(0, index));
		int height = Integer.parseInt(candidate.substring(index + 1));
		for (Size size : supported) {
			if (size.width == width && size.height == height) {
				parameters.setPictureSize(width, height);
				return true;
			}
		}
		return false;
	}

	public static void upgradeGlobalPreferences(SharedPreferences pref) {
		try {
			pref.getInt(KEY_VERSION, 0);
		} catch (Exception exception) {
			Editor editor = pref.edit();
			editor.putInt(KEY_VERSION, 0);
			editor.apply();
		}
	}

	public static void upgradeLocalPreferences(SharedPreferences pref) {
		try {
			pref.getInt(KEY_LOCAL_VERSION, 0);
		} catch (Exception exception) {
			Editor editor = pref.edit();
			editor.putInt(KEY_LOCAL_VERSION, 0);
			editor.apply();
		}
	}

	public static void writePreferredCameraId(SharedPreferences pref,
			int cameraId) {
		Editor editor = pref.edit();
		editor.putString(KEY_CAMERA_ID, Integer.toString(cameraId));
		editor.apply();
	}

	public static int getStopDelaySampleCount(
			SharedPreferences sharedPreferences) {
		return sharedPreferences.getInt("pref_stop_delay_samples_key", 1);
	}

	public static int getStopRecordingDelayAverage(SharedPreferences preferences) {
		return preferences.getInt(KEY_RECORDER_STOP_DELAY_AVERAGE, 400);
	}

	public static void setStopDelayAverageAndSampleCount(
			SharedPreferences sharedPreferences, int average, int count) {
		Log.v(TAG, "Setting stop delay average to " + average);
		Editor editor = sharedPreferences.edit();
		editor.putInt(KEY_RECORDER_STOP_DELAY_AVERAGE, average);
		if (count + 1 <= 30) {
			++count;
		}
		editor.putInt("pref_stop_delay_samples_key", count);
		editor.commit();
	}
}