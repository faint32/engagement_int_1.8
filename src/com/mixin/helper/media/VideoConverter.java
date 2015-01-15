package com.mixin.helper.media;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.os.Parcel;
import android.os.Parcelable;

import com.netease.android.video.ui.VideoFileUtil;

public class VideoConverter {
	
	static {
		System.loadLibrary("VideoConverter");
	}

	public static final int NO = -1;// 未开始

	public static final int BEGIN = 0;// 处理中

	public static final int FINISH = 1;

	public static final int ERROR = 2;

	public static final String VIDEO_CONVER_PARAMS = "video_conver_params";

	private static Map<String, Integer> converterStatusMap = new ConcurrentHashMap<String, Integer>();
	
//	private static Executor singleThreadExecutor = Executors.newSingleThreadExecutor();

	/**
	 * 获取视频处理的状态
	 * 
	 * @param videoFilePath
	 * @return
	 */
	public static int getStatus(String videoFilePath, Params params) {
		File file = new File(getConvertedFilePath(videoFilePath, params));
		if (file.exists()) {
			return FINISH;
		}

		Integer status = converterStatusMap.get(videoFilePath + getParamsStr(params));
		if (status == null) {
			status = NO;
		}
		return status;
	}

	/**
	 * 视频视频处理的状态
	 * 
	 * @param videoFilePath
	 * @param status
	 */
	public static void setStatus(String videoFilePath, int status) {
		converterStatusMap.put(videoFilePath, status);
	}

	public static native int convertFile(String inFile,// 需要编辑的视频文件路径
			String outFile, // 编辑后的视频文件路径名字
			int fps, // 输出视频的帧率
			int outW, // 输出视频的宽度
			int outH, // 输出视频的高度
			int angle, // 输出视频的角度，目前只能是0度
			int rcX, // 原始视频需要截取区域的左上角x坐标（注意截取区域越界，将会返回错误信息）
			int rcY, // 原始视频需要截取区域的左上角y坐标
			int rcW, // 原始视频需要截取区域的宽度
			int rcH, // 原始视频需要截取区域的高度
			double beginTime, // 原始视频需要截取的开始时间
			double endTime, // 原始视频需要截取的结束时间（必须满足beginTime<endTime，0<beginTime、endTime<100）
			VideoConverterListener listener// 视频编辑内部监督器，主要是报告错误，进度等所使用
	);

	/**
	 * 获取视频拍摄的旋转角度
	 * @param inFile 视频文件路径
	 * @return
	 */
	public static native int getVideoRotate(String inFile);

//	/**
//	 * 
//	 * 异步方式处理
//	 * 
//	 * @param videoFilePath
//	 * @param params
//	 */
//	public static void convertFileAsync(final String videoFilePath,
//			final Params params) {
//		int status = getStatus(videoFilePath, params);
//		if (status == BEGIN || status == FINISH) {
//			return;
//		}
//
//		String key = videoFilePath+ getParamsStr(params);
//		VideoConverter.setStatus(key, VideoConverter.BEGIN);
//		singleThreadExecutor.execute(new Runnable() {
//			@Override
//			public void run() {
//				convertFile(videoFilePath, params);
//			}
//		});
//	}
	
	/**
	 * 视频裁剪处理所在的目录，并返回videoFile对应的文件名
	 * 
	 * @param videoFile
	 * @return
	 */
	private static String getMoviesFilePath(File videoFile) {
		File moviesDir = new File(VideoFileUtil.getConverterdPath());
		if (!moviesDir.exists()) {
			moviesDir.mkdir();
		}
		return moviesDir.getAbsolutePath() + "/" + videoFile.getName();
	}

	/**
	 * 返回压缩裁剪处理过程中临时文件路径
	 * 
	 * @param videoFilePath
	 * @return
	 */
	private static String getConvertingFilePath(String videoFilePath, Params params) {
		return getMoviesFilePath(new File(videoFilePath)) + getParamsStr(params) + "_converting.mp4";
	}

	/**
	 * 返回压缩裁剪的文件路径
	 * 
	 * @param videoFilePath
	 * @return
	 */
	public static String getConvertedFilePath(String videoFilePath, Params params) {
		return getMoviesFilePath(new File(videoFilePath)) + getParamsStr(params) + "_converted.mp4";
	}

	/**
	 * params的字符串表示
	 * @param params
	 * @return
	 */
	public static String getParamsStr(Params params) {
		return "-" + params.dstRectX + "-" + params.dstRectY + "-"
				+ params.dstRectWidth + "-" + params.dstRectHeight
				+ "-" + params.startTimePercent + "-" + params.endTimePercent;
	}

	/**
	 * 同步方式处理
	 * 
	 * @param videoFilePath
	 * @param params
	 */
	public static void convertFile(final String videoFilePath, final Params params) {
//		int status = getStatus(videoFilePath, params);
//		if (status == BEGIN || status == FINISH) {
//			return;
//		}

		final String key = videoFilePath + getParamsStr(params);
		VideoConverter.setStatus(key, VideoConverter.BEGIN);
		
		File videoFile = new File(videoFilePath);
		if(!videoFile.exists()) {
			VideoConverter.setStatus(key, VideoConverter.ERROR);
			return;
		}
		
		final String moveToFilePath = getMoviesFilePath(videoFile);

		VideoConverterListener listener = new VideoConverterListener() {

			@Override
			public void onStart() {
				VideoConverter.setStatus(key, VideoConverter.BEGIN);
			}

			@Override
			public void onStep(int step) {
			}

			@Override
			public void onFinish() {
				File f = new File(getConvertingFilePath(moveToFilePath, params));
				f.renameTo(new File(getConvertedFilePath(moveToFilePath, params)));
				VideoConverter.setStatus(key, VideoConverter.FINISH);
			}

			@Override
			public void onError(int errorCode, String errorMsg) {
				VideoConverter.setStatus(key, VideoConverter.ERROR);
			}

			@Override
			public void onFault(String faultMsg) {
				VideoConverter.setStatus(key, VideoConverter.ERROR);
			}
		};

		int outWidth = 480; // 输出视频的宽度
//		if (ActivityUtils.isWifi() && params.dstRectWidth > outWidth) {
//			outWidth = 640;
//		}

		int outHeight = outWidth;

		int digree = getVideoRotate(videoFilePath);
		
		File f = new File(videoFilePath);
		if (! f.getParentFile().exists()) {
			f.getParentFile().mkdirs();
		}
		
		VideoConverter.convertFile(videoFilePath,
				getConvertingFilePath(moveToFilePath, params), 20, outWidth, outHeight,
				digree, params.dstRectX, params.dstRectY, params.dstRectWidth,
				params.dstRectHeight, params.startTimePercent,
				params.endTimePercent, listener);
	}
	
	/**
	 * 转换参数
	 * 
	 * @author lvyusheng
	 * 
	 */
	public static class Params implements Parcelable {
		private static final long serialVersionUID = 1L;
		private int dstRectX; // 原始视频需要截取区域的左上角x坐标（注意截取区域越界，将会返回错误信息）
		private int dstRectY; // 原始视频需要截取区域的左上角y坐标
		private int dstRectWidth; // 原始视频需要截取区域的宽度
		private int dstRectHeight; // 原始视频需要截取区域的高度
		private double startTimePercent; // 截取视频的起始时间点百分比
		private double endTimePercent; // 截取视频的结束时间点百分比

		public Params(int dstRectX, int dstRectY, int dstRectWidth,
				int dstRectHeight, double startTimePercent,
				double endTimePercent) {
			this.dstRectX = dstRectX;
			this.dstRectY = dstRectY;
			this.dstRectWidth = dstRectWidth;
			this.dstRectHeight = dstRectHeight;
			this.startTimePercent = startTimePercent;
			this.endTimePercent = endTimePercent;
		}
		
		public Params(Parcel parcel) {
			this.dstRectX = parcel.readInt();
			this.dstRectY = parcel.readInt();
			this.dstRectWidth = parcel.readInt();
			this.dstRectHeight = parcel.readInt();
			this.startTimePercent = parcel.readDouble();
			this.endTimePercent = parcel.readDouble();
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(dstRectX); // 原始视频需要截取区域的左上角x坐标（注意截取区域越界，将会返回错误信息）
			dest.writeInt(dstRectY); // 原始视频需要截取区域的左上角y坐标
			dest.writeInt(dstRectWidth); // 原始视频需要截取区域的宽度
			dest.writeInt(dstRectHeight); // 原始视频需要截取区域的高度
			dest.writeDouble(startTimePercent); // 截取视频的起始时间点百分比
			dest.writeDouble(endTimePercent); // 截取视频的结束时间点百分比
		}
		
		public static final Parcelable.Creator<Params> CREATOR = new Parcelable.Creator<Params>() {

			@Override
			public Params createFromParcel(Parcel source) {
				return new Params(source);
			}

			@Override
			public Params[] newArray(int size) {
				return new Params[size];
			}

		};
	}

}
