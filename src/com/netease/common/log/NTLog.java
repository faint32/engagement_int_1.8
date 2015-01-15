package com.netease.common.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.util.Log;

import com.netease.common.config.IConfig;

/**
 * 日志工具 1.添加日志输出选项.控制日志输出位置 2.添加文件日志功能.(因进程问题.现UI与Service只能打到不同的文件中)
 * 3.控制单个日志文件最大限制.由LOG_MAXSIZE常量控制,保留两个最新日志文件 4.文件日志输出目标
 * /data/data/%packetname%/files/
 * 
 * @author wangrongqiang@163.com
 * @date 2010-5-26
 * 
 * 
 * @date 2012-5-24
 */

public final class NTLog implements IConfig {

	/*************************以下IConfig 配置信息*****************************/
	
	/**
	 * 将NTLog日志输出到控制台
	 */
	public static final int TO_CONSOLE = 0x01;

	/**
	 * 将NTLog日志输出到文件
	 */
	public static final int TO_FILE = 0x02;

	/**
	 * 将系统warn以上的日志输出到文件
	 */
	public static final int FROM_LOGCAT = 0x04;
	
	public static int DEBUG_ALL = TO_CONSOLE | TO_FILE | FROM_LOGCAT;
	
	/*************************以上IConfig 配置信息*****************************/
	
	private static final String LOG_TEMP_FILE = "NTLog_log.txt";
	private static final String LOG_LAST_FILE = "NTLog_log_last.txt";
	private static final String LOG_NOW_FILE = "NTLog_log_now.txt";

	private static final int LOG_MAXSIZE = 1024 * 1024; // double the size

    /**
     * Priority constant for the println method; use Log.v.
     */
	private static final int VERBOSE = 2; // Log.VERBOSE

    /**
     * Priority constant for the println method; use Log.d.
     */
	private static final int DEBUG = 3; // Log.DEBUG

    /**
     * Priority constant for the println method; use Log.i.
     */
	private static final int INFO = 4; // Log.INFO

    /**
     * Priority constant for the println method; use Log.w.
     */
	private static final int WARN = 5; // Log.WARN

    /**
     * Priority constant for the println method; use Log.e.
     */
	private static final int ERROR = 6; // Log.ERROR

    /**
     * Priority constant for the println method.
     */
	private static final int ASSERT = 7; // Log.ASSERT
	
    
    private static final int LOG_LEVEL = VERBOSE;
    
	/**
	 * log文件路径
	 */
	private static final String LOG_PATH = "/data/data/%s/files/";

	private static Object[] Lock = new Object[0];

	private static String mAppPath;
	private static OutputStream mLogStream;
	private static long mFileSize;

	private static PaintLogThread mPaintLogThread;

	private static ExecutorService mExecutorService = Executors
			.newFixedThreadPool(1);

	/**
     * 外部传入包名，存储在data目录下
     * @param packageName 包名
     */
	public static void init(String packageName) {
		synchronized (Lock)
		{
			mAppPath = String.format(LOG_PATH, packageName);
			File dir = new File(mAppPath);
			if(!dir.exists()){
				dir.mkdir();
			}
		}
	
		if ((DEBUG_ALL & FROM_LOGCAT) != 0) {
			if (mPaintLogThread == null) {
				mPaintLogThread = new PaintLogThread();
				mPaintLogThread.start();
			}
		}
	}
	/**
	 * 外部直接传入全路径 
	 * @param path 路径
	 */
	public static void initPath(String path) {
        synchronized (Lock)
        {
            mAppPath = path;
            File dir = new File(mAppPath);
            if(!dir.exists()){
                dir.mkdir();
            }
        }
    
        if ((DEBUG_ALL & FROM_LOGCAT) != 0) {
            if (mPaintLogThread == null) {
                mPaintLogThread = new PaintLogThread();
                mPaintLogThread.start();
            }
        }
    }

	public static void d(String tag, String msg) {
		log(tag, msg, DEBUG);
	}

	public static void v(String tag, String msg) {
		log(tag, msg, VERBOSE);
	}

	public static void e(String tag, String msg) {
		log(tag, msg, ERROR);
	}

	public static void i(String tag, String msg) {
		log(tag, msg, INFO);
	}

	public static void w(String tag, String msg) {
		log(tag, msg, WARN);
	}

	private static void log(String tag, String msg, int level) {
		if (tag == null)
			tag = "TAG_NULL";
		if (msg == null)
			msg = "MSG_NULL";

		if (level >= LOG_LEVEL) {
			if ((DEBUG_ALL & TO_CONSOLE) != 0) {
				LogToConsole(tag, msg, level);
			}

			if ((DEBUG_ALL & TO_FILE) != 0) {
				final String Tag = tag;
				final String Msg = msg;
				final int Level = level;
				mExecutorService.submit(new Runnable() {
					@Override
					public void run() {
						LogToFile(Tag, Msg, Level);
					}
				});
			}
		}
	}

	/**
	 * 将log打到文件日志
	 * 
	 * @param tag
	 * @param msg
	 * @param level
	 */
	private static void LogToFile(String tag, String msg, int level) {
		synchronized (Lock) {
			OutputStream outStream = openLogFileOutStream();

			if (outStream != null) {
				try {
					byte[] d = getLogStr(tag, msg).getBytes("utf-8");
					// byte[] d = msg.getBytes("utf-8");
					if (mFileSize < LOG_MAXSIZE) {
						outStream.write(d);
						outStream.write("\r\n".getBytes());
						outStream.flush();
						mFileSize += d.length;
					} else {
						closeLogFileOutStream();
						renameLogFile();
//						LogToFile(tag, msg, level);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}

	private static Calendar mDate = Calendar.getInstance();
	private static StringBuffer mBuffer = new StringBuffer();

	/**
	 * 组成Log字符串.添加时间信息.
	 * 
	 * @param tag
	 * @param msg
	 * @return
	 */
	private static String getLogStr(String tag, String msg) {

		mDate.setTimeInMillis(System.currentTimeMillis());

		mBuffer.setLength(0);
		mBuffer.append("[");
		mBuffer.append(tag);
		mBuffer.append(" : ");
		mBuffer.append(mDate.get(Calendar.MONTH) + 1);
		mBuffer.append("-");
		mBuffer.append(mDate.get(Calendar.DATE));
		mBuffer.append(" ");
		mBuffer.append(mDate.get(Calendar.HOUR_OF_DAY));
		mBuffer.append(":");
		mBuffer.append(mDate.get(Calendar.MINUTE));
		mBuffer.append(":");
		mBuffer.append(mDate.get(Calendar.SECOND));
		mBuffer.append(":");
		mBuffer.append(mDate.get(Calendar.MILLISECOND));
		mBuffer.append("] ");
		mBuffer.append(msg);

		return mBuffer.toString();
	}

	/**
	 * 获取日志临时文件输入流
	 * 
	 * @return
	 */
	private static OutputStream openLogFileOutStream() {
		if (mLogStream == null) {
			try {
				if (mAppPath == null || mAppPath.length() == 0) {
					return null;
					// mAppPath="/data/data/com.netease.rpmms/files/";
				}
				File file = openAbsoluteFile(LOG_TEMP_FILE);

				if (file == null) {
					return null;
				}

				if (file.exists()) {
					mLogStream = new FileOutputStream(file, true);
					mFileSize = file.length();
				} else {
					// file.createNewFile();
					mLogStream = new FileOutputStream(file);
					mFileSize = 0;
				}
			} catch (Exception e) {
//				e.printStackTrace();
			}
		}

		return mLogStream;
	}

	public static File openAbsoluteFile(String name) {
		if (mAppPath == null || mAppPath.length() == 0) {
			return null;
		} else {
			File file = new File(mAppPath + name);
			return file;
		}
	}

	/**
	 * 关闭日志输出流
	 */
	private static void closeLogFileOutStream() {
		try {
			if (mLogStream != null) {
				mLogStream.close();
				mLogStream = null;
				mFileSize = 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * rename log file
	 */
	private static void renameLogFile() {
		synchronized (Lock) {
			File file = openAbsoluteFile(LOG_TEMP_FILE);
			File destFile = openAbsoluteFile(LOG_LAST_FILE);

			if (destFile.exists()) {
				destFile.delete();
			}
			file.renameTo(destFile);
		}
	}

	/**
	 * 将log打到控制台
	 * 
	 * @param tag
	 * @param msg
	 * @param level
	 */
	private static void LogToConsole(String tag, String msg, int level) {
		switch (level) {
		case Log.DEBUG:
			Log.d(tag, msg);
			break;
		case Log.ERROR:
			Log.e(tag, msg);
			break;
		case Log.INFO:
			Log.i(tag, msg);
			break;
		case Log.VERBOSE:
			Log.v(tag, msg);
			break;
		case Log.WARN:
			Log.w(tag, msg);
			break;
		}
	}

	static class PaintLogThread extends Thread {

		int mEmptyMsg;
		Process mProcess;
		boolean mStop = false;

		public void shutdown() {
			mStop = true;
			if (mProcess != null) {
				mProcess.destroy();
				mProcess = null;
			}
		}

		@Override
        public void run() {
			// TODO Auto-generated method stub
			try {
				ArrayList<String> commandLine = new ArrayList<String>();
				commandLine.add("logcat");
				commandLine.add( "-d"); 
//				commandLine.add("-v");
				commandLine.add("time");
				commandLine.add("-s");
				commandLine.add("tag:W");
				// commandLine.add( "-f");
				// commandLine.add("/sdcard/log.txt");

				mProcess = Runtime.getRuntime().exec(
						commandLine.toArray(new String[commandLine.size()]));

				BufferedReader bufferedReader = new BufferedReader
				// ( new InputStreamReader(mProcess.getInputStream()), 1024);
				(new InputStreamReader(mProcess.getInputStream()));
				String line = null;
				while (!mStop) {
					line = bufferedReader.readLine();
					if (line != null) {
						LogToFile("SysLog", line, VERBOSE);
						line = null;
					} else {
						break;
					}
				}
				bufferedReader.close();
				if (mProcess != null)
					mProcess.destroy();

				mProcess = null;
				mPaintLogThread = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * back now log file
	 */
	public static void backLogFile() {
		synchronized (Lock) {
			try {
				closeLogFileOutStream();

				File destFile = openAbsoluteFile(LOG_NOW_FILE);

				if (destFile.exists()) {
					destFile.delete();
				}

				try {
					destFile.createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
					return;
				}

				File srcFile1 = openAbsoluteFile(LOG_LAST_FILE);
				File srcFile2 = openAbsoluteFile(LOG_TEMP_FILE);

				copyFile(srcFile1, srcFile2, destFile, true);
				openLogFileOutStream();

			} catch (IOException e) {
				// TODO: handle exception
				e.printStackTrace();
				Log.w("NTLog", "backLogFile fail:" + e.toString());
			}
		}
	}

	public static boolean zipLogFile(String zipFileName) {
		// backup ui log file
		backLogFile();

		File destFile = openAbsoluteFile(zipFileName);
		if (destFile.exists()) {
			destFile.delete();
		}
		try {
			destFile.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}
		File srcFile = openAbsoluteFile(LOG_NOW_FILE);
		boolean ret = zip(srcFile, destFile);
		destFile = null;
		srcFile = null;
		return ret;
	}

	private static void copyFile(File src1, File src2, File dest,
			boolean destAppend) throws IOException {
		if (dest.exists()) {
			dest.delete();
		}
		long total = 0;
		long count = 0;
		FileInputStream in = null;
		FileOutputStream out = new FileOutputStream(dest);
		byte[] temp = new byte[1024 * 10];

		if (src1.exists()) {
			total = src1.length();
			in = new FileInputStream(src1);
			while (count < total) {
				int size = in.read(temp);
				out.write(temp, 0, size);
				count += size;
			}
			in.close();
		}

		if (src2.exists()) {
			count = 0;
			total = src2.length();
			in = new FileInputStream(src2);
			while (count < total) {
				int size = in.read(temp);
				out.write(temp, 0, size);
				count += size;
			}
			in.close();
		}

		in = null;
		out.close();
		out = null;

	}

	private static boolean zip(File unZip, File zip) {
		if (!unZip.exists())
			return false;
		if (!zip.getParentFile().exists())
			zip.getParentFile().mkdir();

		try {
			FileInputStream in = new FileInputStream(unZip);
			FileOutputStream out = new FileOutputStream(zip);

			ZipOutputStream zipOut = new ZipOutputStream(out);

			// for buffer
			byte[] buf = new byte[1024];

			int readCnt = 0;

			zipOut.putNextEntry(new ZipEntry(unZip.getName()));
			while ((readCnt = in.read(buf)) > 0) {
				zipOut.write(buf, 0, readCnt);
			}
			zipOut.closeEntry();

			in.close();
			zipOut.close();

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
}
