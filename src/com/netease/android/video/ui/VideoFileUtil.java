package com.netease.android.video.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.netease.android.activity.LofterApplication;
import com.netease.android.util.ActivityUtils;
import com.netease.android.video.model.pendingmedia.PendingMedia;

public class VideoFileUtil {
	private static final String TAG = "VideoFileUtil";
	
	private static File getRootDir(Context context, String name) {
		File root = context.getExternalFilesDir(null);
		if (root == null) {
			root = new File(Environment.getExternalStorageDirectory(), 
					"Android/data/" + context.getPackageName());
		}
		
		File file = null;
		
		root = new File(root, "video");
		
		if (TextUtils.isEmpty(name)) {
			file = root;
		}
		else {
			file = new File(root, name);
			if (! file.exists()) {
				file.mkdirs();
			}
		}
		
		return file;
	}

	public static void createVideoDirs(Context context) {
//		if (context.getExternalFilesDir(null) == null) {
//			throw new IllegalStateException("file is not available");
//		}
	}
	
	public static void deleteOldFiles() {
		File file = getRootDir(LofterApplication.getInstance(), null);
		
		String[] names = file.list();
		if (names != null) {
			for (String name : names) {
				File tmp = new File(file, name);
				deleteFiles(tmp.getPath(), false);
			}
		}
	}

	public static void cleanupEmptyFileAsync(final String fileName) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (fileName != null) {
					File file = new File(fileName);
					if ((file.length() == 0L) && (file.delete())) {
						StringBuilder sb = new StringBuilder().append(
								"Empty video file deleted: ").append(fileName);
						Log.v(TAG, sb.toString());
					}
				}
			}
		}).start();
	}


	private static String createName(long curTimestamp, Context context) {
		Date date = new Date(curTimestamp);
		return new SimpleDateFormat("'VID'_yyyyMMdd_HHmmss").format(date);
	}

	public static void deleteFileAsync(final String filePath) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				boolean isDelete = new File(filePath).delete();
				StringBuilder sb = new StringBuilder("Delete result: ")
						.append(isDelete).append(" for file: ")
						.append(filePath);
				Log.d("VideoFileUtil", sb.toString());
			}
		}).start();
	}

	public static void deleteFiles(String filepath, boolean removeDir) {
		File file = new File(filepath);
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				deleteFiles(files[i].toString(), true);
			}
		}
		if (removeDir) {
			boolean bool = file.delete();
			StringBuilder sb = new StringBuilder().append("Delete result: ")
					.append(bool).append(" for file: " + filepath);
			Log.d("VideoFileUtil", sb.toString());
		}
	}

	public static String generateRecordingSessionName(String sessionName,
			int cameraId, Activity activity) {
		long l = System.currentTimeMillis();
		if (sessionName == null) {
			sessionName = createName(l, activity) + "_session_" + cameraId;
			String dirPath = getClipsPath(activity);
			boolean bool = new File(dirPath, sessionName).mkdirs();
			StringBuilder sb = new StringBuilder(
					"Recording session started in folder ").append(sessionName)
					.append(" ").append(bool);
			Log.d("VideoFileUtil", sb.toString());
		}
		return sessionName;
	}

	public static String generateVideoFilename(String fileName,
			Activity activity) {
		String name = createName(System.currentTimeMillis(), activity) + ".mp4";
		String dirPath = getClipsPath(activity);
		File file = new File(dirPath, fileName);
		file.mkdirs();
		return new File(file, name).getPath();
	}

	public static String getClipsPath(Context context) {
		File file = getRootDir(context, "videos");
		
		return file.getPath();
	}

	/**
	 * 获取视频裁剪的路径
	 * 
	 * @param context
	 * @return
	 */
	public static String getConverterdPath() {
		File file = getRootDir(LofterApplication.getInstance(), "converter");
		
		return file.getPath();
	}

	public static String getCoversPath(Context context) {
		File file = getRootDir(context, "covers");
		return file.getPath();
	}

	public static boolean isFileSystemAvailable(Context context) {
//		if (context.getExternalFilesDir(null) == null) {
//			ActivityUtils.showAlertDialog((Activity) context,
//					"getExternalFilesDir", "getExternalFilesDir null");
//			return false;
//		}
		return true;
	}

	public static List<File> getPreviousRecordingForRestoringSession(
			PendingMedia pendingMedia, Context Context) {
		List<File> list = new ArrayList<File>();
		if ((pendingMedia != null)
				&& (pendingMedia.getVideoSessionName() != null)) {
			list = getClipFilesFromVideoFolder(
					pendingMedia.getVideoSessionName(), Context);
		}
		return list;
	}

	public static List<File> getClipFilesFromVideoFolder(String sessionName,
			Context context) {
		List<File> files = new ArrayList<File>();
		File clipsDir = new File(getClipsPath(context), sessionName);
		if (clipsDir.exists()) {
			for (File file : clipsDir.listFiles()) {
				if ((file.getName().endsWith(".mp4"))
						&& (!isStitchedVideoFile(file))) {
					files.add(file);
				}
			}
		}
		return files;
	}

	public static boolean isStitchedVideoFile(File file) {
		return file.getName().contains("-stitched.mp4");
	}

	@TargetApi(10)
	public static long getClipDurationMillis(MediaMetadataRetriever retriever) {
		long duration = 0l;
		String str = retriever.extractMetadata(9);
		if (str != null) {
			duration = Long.valueOf(str).longValue();
		}
		return duration;
	}

	@TargetApi(10)
	public static long getClipDurationMillis(File file) {
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		try {
			retriever.setDataSource(file.getAbsolutePath());
			return getClipDurationMillis(retriever);
		} catch (Exception exception) {
			Log.e("VideoFileUtil",
					"Cannot setDataSource. File is corrupted or incomplete.",
					exception);
		}
		return 0;
	}

	@TargetApi(10)
	public static long getClipDurationMillis(String filePath) {
		return getClipDurationMillis(new File(filePath));
	}
}