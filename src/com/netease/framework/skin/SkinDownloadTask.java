package com.netease.framework.skin;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.netease.common.log.NTLog;
import com.netease.service.Utils.StackTraceUtil;
import com.netease.util.Util;

public class SkinDownloadTask extends AsyncTask<String, Integer, Integer> {
	public static final String TAG = SkinDownloadTask.class.getSimpleName();
	
	public static final String  SUFFIX_MD5 = ".md5";
	public static final String  SUFFIX_TMP = "_tmp";
	
	public static final int DOWNLOAD_STAT_SUCCESS = 0;
	public static final int DOWNLOAD_STAT_CANCLE = 1;
	public static final int DOWNLOAD_STAT_ERROR = 2;
	
	static int mTid = 0;
	private long   mStart;
	private String mUrl;
	private Object mTag;
	private WeakReference<IPathConvert> mWrfUrlToPath;
	private WeakReference<IProcessListener> mWrfProcessListener;
	
	public SkinDownloadTask(Object tag, IPathConvert urlToPath, IProcessListener listener) throws IllegalArgumentException {
		if (null == urlToPath || null == listener) {
			throw new IllegalArgumentException("The parameter can not be null");
		}
		
		mStart = System.currentTimeMillis();
		getNextTid();
		mTag = tag;
		mWrfUrlToPath = new WeakReference<IPathConvert>(urlToPath);
		mWrfProcessListener = new WeakReference<IProcessListener>(listener);
	}
	
	@Override
	protected Integer doInBackground(String... params) {
		int iRes = DOWNLOAD_STAT_ERROR;
		long range = -1;
		mUrl = params[0];
		
		
		HttpURLConnection conn = null;
		FileOutputStream os = null;
		
		try {
			if (isCancelled()) {
				iRes = DOWNLOAD_STAT_CANCLE;
				return iRes;
			}
			
			String path = getPath(mUrl);
			if (TextUtils.isEmpty(path)) {
				return iRes;
			}
			
			String path_tmp = path + SUFFIX_TMP;
			String path_md5 = path + SUFFIX_MD5;
			
			// 判断是否需要断点续传
			if (isRange(path_tmp, path_md5)) {
				range = getRange(mUrl);
			}
			
			
			File file_tmp = new File(path_tmp);
			if (isCancelled()) {
				iRes = DOWNLOAD_STAT_CANCLE;
				return iRes;
			}
			conn = (HttpURLConnection)new URL(mUrl).openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(30*000);  
			conn.setReadTimeout(30*000);
			// 如果需要断点续传，需要在请求头部加入RANGE字段
			if (range > 0) {
				String newValue = "bytes=" + range + "-";
				conn.setRequestProperty("RANGE", newValue);
			}
			
			int status = conn.getResponseCode();
			int len = conn.getContentLength();
			if (isCancelled()) {
				iRes = DOWNLOAD_STAT_CANCLE;
				return iRes;
			}
			
			if (status == 200) {
				os = new FileOutputStream(file_tmp, false);
				iRes = saveToFile(conn.getInputStream(), os, len);
			} else if (status == 206) {
				os = new FileOutputStream(file_tmp, true);
				iRes = saveToFile(conn.getInputStream(), os, len);
			}
			
			if (iRes == DOWNLOAD_STAT_SUCCESS) {
				// 文件保存正确，把临时文件改为正式文件
				File file = new File(path);
				file.delete();
				file_tmp.renameTo(file);
			} else {
				// 文件保存失败，保存md5文件，用来验证完整性
				saveToMD5File(path_tmp, path_md5);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			os = null;
			if (null != conn) {
				conn.disconnect();
			}
			conn = null;
		}
		
		return iRes;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		IProcessListener listener = null;
		listener = mWrfProcessListener.get();
		if (result == DOWNLOAD_STAT_SUCCESS) {
			listener.onResult(mTag, true, 0, mUrl);
		} else {
			listener.onResult(mTag, false, 0, mUrl);
		}
		
		mWrfUrlToPath = null;
		mWrfProcessListener = null;
		if (SkinConfig.DEBUG) {
			NTLog.d(TAG, StackTraceUtil.getMethodName() + " spend time=" + (System.currentTimeMillis() - mStart));
		}
	}
	
	private int saveToFile(InputStream in, OutputStream os, int len) {
		int iRes = DOWNLOAD_STAT_ERROR;
		
		byte[] buffer = new byte[1024 * 8];
		BufferedInputStream bin = new BufferedInputStream(in);
		BufferedOutputStream bout = new BufferedOutputStream(os);
		
		
		if (len <= 0) {
			len = Integer.MAX_VALUE;
		}
		
		int total = 0;
		int size = 0;
		IProcessListener listener = null;
		listener = mWrfProcessListener.get();
		
		try {
			while ((size = bin.read(buffer)) != -1 && (total < len)) {
				if (size > 0) {
					if (null != listener) {
						if (total > len) {
							len = total;
						}
						
						listener.onProcess(mTag, total, len);
					}
					bout.write(buffer, 0, size);
					total += size;
				}
				if (isCancelled()) {
					iRes = DOWNLOAD_STAT_CANCLE;
					break;
				}
			}
			
			if (iRes != DOWNLOAD_STAT_CANCLE) {
				iRes = DOWNLOAD_STAT_SUCCESS;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != bin) {
					bin.close();
				}
				bin = null;
				
				if (null != in) {
					in.close();
				}
				in = null;
				
				if (null != bout) {
					bout.close();
				}
				bout = null;
				
				if (null != os) {
					os.close();
				}
				os = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return iRes;
	}
	
	/**
	 * 判断是否需要断点续传
	 * @param url
	 * @return
	 */
	private boolean isRange(String filePath, String filePathMd5) {
		boolean bRes = false;
		
		do {
			File file = new File(filePath);
			File fileMd5 = new File(filePathMd5);
			// 如果临时文件不存在，删除临时md5文件
			if (!file.exists()) {
				if (fileMd5.exists()) {
					fileMd5.delete();
				}
				break;
			}
			
			// 如果临时文件存在，而md5文件不存在，删除临时文件
			if (!fileMd5.exists()) {
				file.delete();
				break;
			}
			
			// 验证文件完整性
			BufferedInputStream bufferInput = null;
			BufferedReader reader = null;
			String md5 = null;
			try {
				bufferInput = new BufferedInputStream(new FileInputStream(file));
				reader = new BufferedReader(new FileReader(fileMd5));
				md5 = reader.readLine();
				if (Util.checkMD5(bufferInput, md5)) {
					bRes = true;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (null != bufferInput) {
						bufferInput.close();
					}
					bufferInput = null;
					
					if (null != reader) {
						reader.close();
					}
					reader = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				if (!bRes) {
					// md5不一致，删除文件
					file.delete();
					fileMd5.delete();
				}
			}
		} while (false);
		
		return bRes;
	}
	
	/**
	 * 获取断点续传文件大小
	 * @param url
	 * @return
	 */
	private long getRange(String path) {
		long size = -1;
		
		File file = new File(path);
		if (file.exists()) {
			size = file.length();
		}
		
		return size;
	}
	
	private String getPath(String url) {
		String path = null;
		
		do {
			
			IPathConvert  urlToPath = mWrfUrlToPath.get();
			if (null == urlToPath ) {
				break;
			}
			
			path = urlToPath.convert(url);
			
		} while (false);

		return path;
	}
	
	private void saveToMD5File(String filePath, String filePathMd5) {
		File file = new File(filePath);
		File fileMd5 = new File(filePathMd5);
		
		String md5 = null;
		BufferedInputStream bIn = null;
		BufferedWriter bWriter = null;
		
		try {
			bIn = new BufferedInputStream(new FileInputStream(file));
			md5 = getMD5(bIn);
			if (!TextUtils.isEmpty(md5)) {
				if (fileMd5.exists()) {
					fileMd5.delete();
				}
				fileMd5.createNewFile();
				
				bWriter = new BufferedWriter(new FileWriter(fileMd5));
				bWriter.write(md5);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != bIn) {
					bIn.close();
				}
				bIn = null;
				
				if (null != bWriter) {
					bWriter.close();
				}
				bWriter = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static String getMD5(InputStream is) {
		String md5 = null;
		
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			
			byte[] data = new byte[1024 * 4];
			int length = 0;
			while ((length = is.read(data)) > 0) {
				md.update(data, 0, length);
			}
			
			is.close();
			
			BigInteger number = new BigInteger(1, md.digest());
			md5 = number.toString(16);

			while (md5.length() < 32)
				md5 = "0" + md5;

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return md5;
	}
	
	public int getId() {
		return mTid;
	}
	
	private synchronized static int getNextTid() {
		if (mTid >= Short.MAX_VALUE) {
			mTid = 0;
		}
		return ++mTid;
	}
}
