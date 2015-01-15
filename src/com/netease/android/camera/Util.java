package com.netease.android.camera;

import java.io.Closeable;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import com.netease.date.R;

/***
 * Collection of utility functions used in this package.
 */
public class Util {
	private static final String TAG = "Util";
	public static final int DIRECTION_LEFT = 0;
	public static final int DIRECTION_RIGHT = 1;
	public static final int DIRECTION_UP = 2;
	public static final int DIRECTION_DOWN = 3;
	public static final int ORIENTATION_HYSTERESIS = 5;
	public static final String REVIEW_ACTION = "com.cooliris.media.action.REVIEW";
	private static ImageFileNamer sImageFileNamer = new ImageFileNamer(
			"'IMG'_yyyyMMdd_HHmmss");

	private Util() {

	}

	// Rotates the bitmap by the specified degree.
	// If a new bitmap is created, the original bitmap is recycled.
	public static Bitmap rotate(Bitmap b, int degrees) {
		if (degrees != 0 && b != null) {
			Matrix m = new Matrix();
			m.setRotate(degrees, (float) b.getWidth() / 2,
					(float) b.getHeight() / 2);
			try {
				Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
						b.getHeight(), m, true);
				if (b != b2) {
					b.recycle();
					b = b2;
				}
			} catch (OutOfMemoryError ex) {
				// We have no memory to rotate. Return the original bitmap.
			}
		}
		return b;
	}

	public static <T> int indexOf(T[] array, T s) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(s)) {
				return i;
			}
		}
		return -1;
	}

	public static void closeSilently(Closeable c) {
		if (c == null)
			return;
		try {
			c.close();
		} catch (Throwable t) {
			// do nothing
		}
	}

	public static void Assert(boolean cond) {
		if (!cond) {
			throw new AssertionError();
		}
	}

	public static Animation slideOut(View view, int to) {
		view.setVisibility(View.INVISIBLE);
		Animation anim;
		switch (to) {
		case DIRECTION_LEFT:
			anim = new TranslateAnimation(0, -view.getWidth(), 0, 0);
			break;
		case DIRECTION_RIGHT:
			anim = new TranslateAnimation(0, view.getWidth(), 0, 0);
			break;
		case DIRECTION_UP:
			anim = new TranslateAnimation(0, 0, 0, -view.getHeight());
			break;
		case DIRECTION_DOWN:
			anim = new TranslateAnimation(0, 0, 0, view.getHeight());
			break;
		default:
			throw new IllegalArgumentException(Integer.toString(to));
		}
		anim.setDuration(500);
		view.startAnimation(anim);
		return anim;
	}

	public static Animation slideIn(View view, int from) {
		view.setVisibility(View.VISIBLE);
		Animation anim;
		switch (from) {
		case DIRECTION_LEFT:
			anim = new TranslateAnimation(-view.getWidth(), 0, 0, 0);
			break;
		case DIRECTION_RIGHT:
			anim = new TranslateAnimation(view.getWidth(), 0, 0, 0);
			break;
		case DIRECTION_UP:
			anim = new TranslateAnimation(0, 0, -view.getHeight(), 0);
			break;
		case DIRECTION_DOWN:
			anim = new TranslateAnimation(0, 0, view.getHeight(), 0);
			break;
		default:
			throw new IllegalArgumentException(Integer.toString(from));
		}
		anim.setDuration(500);
		view.startAnimation(anim);
		return anim;
	}

	public static <T> T checkNotNull(T object) {
		if (object == null)
			throw new NullPointerException();
		return object;
	}

	public static boolean equals(Object a, Object b) {
		return (a == b) || (a == null ? false : a.equals(b));
	}

	public static boolean isPowerOf2(int n) {
		return (n & -n) == n;
	}

	public static int nextPowerOf2(int n) {
		n -= 1;
		n |= n >>> 16;
		n |= n >>> 8;
		n |= n >>> 4;
		n |= n >>> 2;
		n |= n >>> 1;
		return n + 1;
	}

	public static float distance(float x, float y, float sx, float sy) {
		float dx = x - sx;
		float dy = y - sy;
		return (float) Math.sqrt(dx * dx + dy * dy);
	}

	public static int clamp(int x, int min, int max) {
		if (x > max)
			return max;
		if (x < min)
			return min;
		return x;
	}

	public static boolean isUriValid(Uri uri, ContentResolver resolver) {
		if (uri == null)
			return false;

		try {
			ParcelFileDescriptor pfd = resolver.openFileDescriptor(uri, "r");
			if (pfd == null) {
				Log.e(TAG, "Fail to open URI. URI=" + uri);
				return false;
			}
			pfd.close();
		} catch (IOException ex) {
			return false;
		}
		return true;
	}

	public static String createJpegName(long dateTaken) {
		synchronized (sImageFileNamer) {
			return sImageFileNamer.generateName(dateTaken);
		}
	}

	public static void broadcastNewPicture(Context context, Uri uri) {
		context.sendBroadcast(new Intent(
				android.hardware.Camera.ACTION_NEW_PICTURE, uri));
		// Keep compatibility
		context.sendBroadcast(new Intent("com.android.camera.NEW_PICTURE", uri));
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public static Camera openCamera(Activity activity, int cameraId)
			throws CameraHardwareException, CameraDisabledException {
		// Check if device policy has disabled the camera.
		if (Build.VERSION.SDK_INT >= 14) {
			DevicePolicyManager dpm = (DevicePolicyManager) activity
					.getSystemService(Context.DEVICE_POLICY_SERVICE);
			if (dpm.getCameraDisabled(null)) {
				throw new CameraDisabledException();
			}
		}
		try {
			return CameraHolder.instance().open(cameraId);
		} catch (CameraHardwareException e) {
			// In eng build, we throw the exception so that test tool
			// can detect it and report it
			// if ("eng".equals(Build.TYPE)) {
			// throw new RuntimeException("openCamera failed", e);
			// } else {
			throw e;
			// }
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void showErrorAndFinish(final Activity activity, int msgId) {
		DialogInterface.OnClickListener buttonListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				activity.finish();
			}
		};
		new AlertDialog.Builder(activity).setCancelable(false)
				.setIconAttribute(android.R.attr.alertDialogIcon)
				.setTitle(R.string.video_camera_error_title).setMessage(msgId)
				.setNeutralButton(R.string.video_dialog_ok, buttonListener).show();
	}

	public static int roundOrientation(int orientation, int orientationHistory) {
		boolean changeOrientation = false;
		if (orientationHistory == OrientationEventListener.ORIENTATION_UNKNOWN) {
			changeOrientation = true;
		} else {
			int dist = Math.abs(orientation - orientationHistory);
			dist = Math.min(dist, 360 - dist);
			changeOrientation = (dist >= 45 + ORIENTATION_HYSTERESIS);
		}
		if (changeOrientation) {
			return ((orientation + 45) / 90 * 90) % 360;
		}
		return orientationHistory;
	}

	public static Size getOptimalPreviewSize(Activity currentActivity,
			List<Camera.Size> sizes, double targetRatio) {
		// Use a very small tolerance because we want an exact match.
		final double ASPECT_TOLERANCE = 0.1;
		if (sizes == null)
			return null;
		Camera.Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;
		// Because of bugs of overlay and layout, we sometimes will try to
		// layout the viewfinder in the portrait orientation and thus get the
		// wrong size of preview surface. When we change the preview size, the
		// new overlay will be created before the old one closed, which causes
		// an exception. For now, just get the screen size.
		Display display = currentActivity.getWindowManager()
				.getDefaultDisplay();
		int targetHeight = Math.min(display.getHeight(), display.getWidth());
		if (targetHeight <= 0)
			targetHeight = ((WindowManager) currentActivity
					.getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay().getHeight();
		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			Log.v(TAG, "size.width:" + size.width + ",size.height:"
					+ size.height);
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}
		// Cannot find the one match the aspect ratio. This should not happen.
		// Ignore the requirement.
		if (optimalSize == null) {
			Log.w(TAG, "No preview size match the aspect ratio");
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		Log.v(TAG, "optimalSize.width:" + optimalSize.width
				+ ",optimalSize.height:" + optimalSize.height);
		return optimalSize;
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	public static int getDisplayRotation(Activity activity) {
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		switch (rotation) {
		case Surface.ROTATION_0:
			return 0;
		case Surface.ROTATION_90:
			return 90;
		case Surface.ROTATION_180:
			return 180;
		case Surface.ROTATION_270:
			return 270;
		}
		return 0;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static void setCameraDisplayOrientation(Activity activity,
			int cameraId, Camera camera) {
		// See android.hardware.Camera.setCameraDisplayOrientation for
		// documentation.
		CameraInfo info = new CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		int degrees = getDisplayRotation(activity);
		int result;
		if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static int getJpegRotation(int cameraId, int orientation) {
		// See android.hardware.Camera.Parameters.setRotation for
		// documentation.
		int rotation = 0;
		if (orientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
			CameraInfo info = CameraHolder.instance().getCameraInfo()[cameraId];
			if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
				rotation = (info.orientation - orientation + 360) % 360;
			} else { // back-facing camera
				rotation = (info.orientation + orientation) % 360;
			}
		}
		return rotation;
	}

	public static void rectFToRect(RectF rectF, Rect rect) {
		rect.left = Math.round(rectF.left);
		rect.top = Math.round(rectF.top);
		rect.right = Math.round(rectF.right);
		rect.bottom = Math.round(rectF.bottom);
	}

	public static void prepareMatrix(Matrix matrix, boolean mirror,
			int displayOrientation, int viewWidth, int viewHeight) {
		// Need mirror for front camera.
		matrix.setScale(mirror ? -1 : 1, 1);
		// This is the value for android.hardware.Camera.setDisplayOrientation.
		matrix.postRotate(displayOrientation);
		// Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
		// UI coordinates range from (0, 0) to (width, height).
		matrix.postScale(viewWidth / 2000f, viewHeight / 2000f);
		matrix.postTranslate(viewWidth / 2f, viewHeight / 2f);
	}

	private static class ImageFileNamer {
		private SimpleDateFormat mFormat;

		// The date (in milliseconds) used to generate the last name.
		private long mLastDate;

		// Number of names generated for the same second.
		private int mSameSecondCount;

		public ImageFileNamer(String format) {
			mFormat = new SimpleDateFormat(format);
		}

		public String generateName(long dateTaken) {
			Date date = new Date(dateTaken);
			String result = mFormat.format(date);

			// If the last name was generated for the same second,
			// we append _1, _2, etc to the name.
			if (dateTaken / 1000 == mLastDate / 1000) {
				mSameSecondCount++;
				result += "_" + mSameSecondCount;
			} else {
				mLastDate = dateTaken;
				mSameSecondCount = 0;
			}

			return result;
		}
	}

	public static boolean isS2Model() {
		if (("GT-I9100".equals(Build.MODEL))
				|| ("GT-I9100G".equals(Build.MODEL))
				|| ("SAMSUNG-SGH-T989".equals(Build.MODEL))
				|| ("SPH-D710".equals(Build.MODEL))
				|| ("SAMSUNG-SGH-I727".equals(Build.MODEL))
				|| ("SGH-I727R".equals(Build.MODEL))
				|| ("SGH-T989".equals(Build.MODEL))
				|| ("SGH-I777".equals(Build.MODEL))) {
			return true;
		}
		return false;
	}

	public static boolean isS4Model() {
		if (("SAMSUNG-SGH-I337".equals(Build.MODEL))
				|| ("SGH-M919".equals(Build.MODEL))
				|| ("SCH-I545".equals(Build.MODEL))
				|| ("SPH-L720".equals(Build.MODEL))
				|| ("SGH-I337M".equals(Build.MODEL))
				|| ("SCH-R970".equals(Build.MODEL))
				|| ("SC-04E".equals(Build.MODEL))
				|| ("GT-I9500".equals(Build.MODEL))
				|| ("GT-I9505".equals(Build.MODEL))
				|| ("SCH-I545".equals(Build.MODEL))) {
			return true;
		}
		return false;
	}
}
