package com.netease.android.camera;

import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;

public class CameraErrorCallback implements Camera.ErrorCallback {
	private static final String TAG = "CameraErrorCallback";
	private Activity mActivity;

	public void onError(int error, Camera camera) {
		Log.e(TAG, "Got camera error callback. error=" + error);
		if (error == 100) {
			if (this.mActivity != null)
				this.mActivity.finish();
			throw new RuntimeException("Media server died.");
		}

	}

	public void setActivity(Activity activity) {
		this.mActivity = activity;
	}
}