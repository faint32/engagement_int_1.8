package com.netease.common.cache.file;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import android.text.TextUtils;
import android.util.Log;

import com.netease.common.config.IConfig;
import com.netease.common.debug.CheckAssert;

/**
 * 
 * 对唯一标识的文件ID（如综合url）进行hash取值，对hash取值结果进行内存标记和子目录
 * 分类，使得文件个数能够突破FAT32文件系统对单个缓存目录的文件个数限制，同时提供缓存
 * 自动清理功能，当缓存大小达到一定值时进行清理
 * 
 * @author dingding
 *
 */
public class FileManager implements IConfig {
	/**
	 * FileManager 将提供一个二级目录缓存方式，主要为了解决以下两个问题：
	 * 1、FAT32文件系统下，单目录缓存文件对文件个数有限制
	 * 2、文件读写效率低，包括文件存在判断效率低下
	 * 
	 * 另外也将提供自动缓存清理策略:
	 * 1、对几天以前的缓存进行清理
	 */

	private static final boolean DEBUG = false;
	private static final String TAG = "FileManager";
	
	
	/************************以下 IConfig 配置项*******************************/
	
	/**
	 * 使用FileManager管理一个缓存目录时，通过内存缓存对文件存在性判断，
	 * 目前，对文件个数MemCacheFileSize取模情况下，对文件进行查询判断，
	 * 每个文件使用一个bit表示存在与否，这样，就会消耗MemCacheFileSize / 8的内存。
	 * 
	 * 建议MemCacheFileSize最大选择为32×512×8或者64×512×8，可按实际情况进行调整。
	 * 按单个文件8KB进行估算，MemCacheFileSize为32×512×8时，全缓存文件大小可以
	 * 达到1GB
	 */
	protected static final int MemCacheFileSize = 32 * 512 * 8;
	
	/**
	 * 单个子目录内存缓存大小，开辟多少空间进行对文件的存在性判断进行加速
	 * 256\512\1024\2048
	 */
	public static int MemCacheSize = 512;
	
	/**
	 * 文件缓存级别（0 ~ 2），级别为n时，子目录个数为2的n*5次个，建议n选择1
	 * 
	 * 用于控制子目录个数,为了尽可能减少子目录命名长度，采用自定义32进制表示，
	 * 字符选择范围[0-9a-u]，
	 */
	public static int FSCacheDirs = 32;
	
	/**
	 * 本地文件自动清除控制
	 * 
	 * 是否进行缓存的自动清理
	 */
	public static boolean FSAutoClear = true;
	
	/**
	 * 本地文件达到多少大小时进行自动清理
	 */
	public static long FSAutoClearSize = 512 * 1024 * 1024;
	
	/**
	 * 缓存自动清理
	 * 
	 * 缓存自动清理多长时间以前的数据
	 */
	public static long FSAutoClearTime = 7 * 24 * 3600;
	
	/************************以上 IConfig 配置项*******************************/
	
	private RootStoreDir mRoorDir;
	
	private static class RootStoreDir extends StoreDir {

		private ArrayList<StoreDir> mSubDirs = null;
		
		AtomicLong mLength; // 文件大小容量
		AtomicInteger mSize; // 文件个数
		List<SizeObserver> mList;
		
		private boolean mInit;
		
		private boolean mFSAutoClear;
		private long mFSAutoClearSize;
		private long mFSAutoClearTime;
		
		ClearFileTask mClearFileTask;
		StoreDeleteCallback mDeleteCallback;

//		// 一级为目录分类，二级为单目录下内存缓存加速
//		private byte[][] mMemCacheState = null;
		
		public RootStoreDir(String filepath, boolean autoClear, 
				long autoClearSize, long autoClearTime) {
			super(filepath);
			
			mFSAutoClear = autoClear;
			mFSAutoClearSize = autoClearSize;
			mFSAutoClearTime = autoClearTime;
			
			mLength = new AtomicLong();
			mSize = new AtomicInteger();
			
			mSubDirs = new ArrayList<StoreDir>();
			if (FSCacheDirs > 0) {
				for (int i = 0; i < FSCacheDirs; i++) {
					mSubDirs.add(new StoreDir(this, getSubDirName(i) + "/"));
				}
			} else {
				mSubDirs.add(new StoreDir(this, ""));
			}
			
//			if (MemCacheSize > 0) {
//				mMemCacheState = new byte[mSubDirs.size()][MemCacheSize];
//			}
			
			initSize();
		}
		
		protected void initSize() {
			Thread thread = new Thread(SizeComputeRunnable);
			thread.setPriority(Thread.NORM_PRIORITY - 1);
			thread.start();
		}
		
		@Override
		public int size() {
			return mSize.get();
		}
		
		@Override
		public long length() {
			return mLength.get();
		}
		
		public void lengthReset() {
			mLength.set(0);
		}
		
		@Override
		protected void resetSizeLength() {
			mSize.set(0);
			mLength.set(0);
		}
		
		@Override
		protected void sizeChange(int size) {
			mSize.addAndGet(size);
		}
		
		@Override
		protected void lengthChange(long length) {
			if (mLength.addAndGet(length) >= mFSAutoClearSize 
					&& ! (length < 0)) { // length >= 0
				checkLengthClear();
			}
		}
		
		private void checkLengthClear() {
			if (mFSAutoClear && mClearFileTask == null) {
				mDeleteCallback = new StoreDeleteCallback();
				mClearFileTask = new ClearFileTask(mDeleteCallback, mFSAutoClearTime, true);
				
				Thread thread = new Thread(mClearFileTask);
				thread.setPriority(Thread.MIN_PRIORITY);
				thread.start();
			}
		}
		
		public void startClearFileTask(StoreDeleteCallback callback, 
				long timeline, boolean force) {
			if (DEBUG) Log.d(TAG, "startClearFileTask " + callback + " time: " + timeline + " force: " + force);
			
			if (mClearFileTask == null || force) {
				mClearFileTask = new ClearFileTask(callback, timeline, false);
				
				if (mDeleteCallback != null) {
					mDeleteCallback.doCancel();
				}
				
				mDeleteCallback = callback;
				
				Thread thread = new Thread(mClearFileTask);
				thread.setPriority(Thread.MIN_PRIORITY + 1);
				thread.start();
			}
		}
		
		@Override
		public void delete(StoreDeleteCallback callback) {
			if (DEBUG) Log.d(TAG, getPath() + " delete " + callback);
			
			callback.setTotalSize(size(), length());
			
			for (StoreDir dir : mSubDirs) {
				if (callback.isCancel()) {
					break;
				}
				callback.setTotalSize(size(), length());
				
				dir.delete(callback);
			}
			
			if (callback.isCancel()) {
				return ;
			}
			
			callback.setTotalSize(size(), length());
			
			StoreFile[] files = listFiles();
			if (files != null) {
				deletFiles(files, 0, files.length, callback, false, -1);
			}
			
			if (! callback.isCancel()) {
				mChanged = false;
//				Util.delAllFiles(getPath());
				resetSizeLength();
			}
		}
		
		@Override
		public void delete(StoreDeleteCallback callback, long time) {
			if (DEBUG) Log.d(TAG, getPath() + " delete t< " + time + " " + callback);
			
			for (StoreDir dir : mSubDirs) {
				if (callback.isCancel()) {
					break;
				}
				
				callback.setTotalSize(size(), length());
				dir.delete(callback, time);
			}
			
			if (callback.isCancel()) {
				return ;
			}
			callback.setTotalSize(size(), length());
			
			StoreFile[] files = listFiles();
			if (files != null) {
				deletFiles(files, 0, files.length, callback, false, time);
			}
		}
		
		public StoreFile getStoreFile(String url) {
			StoreFile storeFile = null;
			
			if (! TextUtils.isEmpty(url)) {
				String filename = getCacheName(url) + getSuffix(url);
				int index = getSubDirIndex(filename.substring(0, FSCacheDirs / 32));
				
				if (DEBUG) Log.e(TAG, "getStoreFile index " + index + " dir: " + mSubDirs.get(index));
				if (index >= 0) {
					storeFile = new StoreFile(mSubDirs.get(index), filename);
				}
				
				if (DEBUG) Log.e(TAG, "getStoreFile index " + index + " filename: " + filename);
			}
			
			return storeFile;
		}
		
		protected StoreFile parseStoreFile(String path) {
			StoreFile storeFile = null;
			
			String rootPath = getPath();
			if (path.startsWith(rootPath)) {
				String subPath = path.substring(rootPath.length());
				int index = subPath.indexOf('/');
				if (index > 0) {
					int dirIndex = getSubDirIndex(subPath.substring(0, index));
					if (dirIndex >= 0 && dirIndex < mSubDirs.size()) {
						storeFile = new StoreFile(mSubDirs.get(dirIndex), 
								subPath.substring(index + 1));
					} else {
						if (DEBUG) Log.e("parseStoreFile", "path: " + path);
						
						storeFile = new StoreFile(this, subPath);
					}
				} else if (mSubDirs.size() == 1) {
					storeFile = new StoreFile(mSubDirs.get(0), subPath);
				} else {
					storeFile = new StoreFile(this, subPath);
				}
			}
			
			return storeFile;
		}
		
		private static int getSubDirIndex(String subDirName) {
			int index = 0;
			for (int i = 0; i < subDirName.length(); i++) {
				index <<= 5;
				char ch = subDirName.charAt(i);
				if (ch >= '0' && ch <= '9') {
					index += ch - '0';
				} else if (ch >= 'a' && ch <= 'v') {
					index += ch - 'a' + 10;
				} else {
					return -1;
				}
			}
			
			return index;
		}
		
		/**
		 * 通过索引计算子目录名
		 * 
		 * @param i
		 * @return
		 */
		private static String getSubDirName(int i) {
			String name = null;
			switch (FSCacheDirs) {
			case 32:
				name = String.valueOf(get32Char(i));
				break;
			case 32 * 32:
				if ((i >> 5) > 0) {
					name = String.valueOf(get32Char(i >> 5)) 
						+ String.valueOf(get32Char(i & 0x1F));
				} else {
					name = String.valueOf(get32Char(i & 0x1F));
				}
				break;
			}
			
			return name;
		}
		
		private static String getSuffix(String input) {
			String suffix = ".n";
			
			CheckAssert.checkValue(input.length(), '<', 9);
			
			int index = input.lastIndexOf('.');
			if (index > 0) {
				input = input.substring(index).toLowerCase();
				
				int i = 1;
				for (; i < input.length(); i++) {
					char ch = input.charAt(i);
					if (! ((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'z'))){
						break;
					}
				}
				
				if (DEBUG) Log.e("getSuffix", "i: " + i + " suffix: " + input.substring(0, i));
				if (i > 1) {
					i = Math.min(6, i);
					suffix = input.substring(0, i);
				}
			}
			
			if (DEBUG) Log.e("getSuffix", " suffix: " + suffix);
			return suffix;
		}

		private static String getCacheName(String input) {
			try {
				// 对有道图片裁剪服务器进行特殊处理
				if (input.length() > 40 && input.charAt(15) == '.' 
					&& ".ydstatic.com/image?".equals(input.substring(15, 35))) {
					input = input.substring(39);
				}
				
				MessageDigest md = MessageDigest.getInstance("MD5");
				byte[] messageDigest = md.digest(input.getBytes());
				
				// 128 bit
				StringBuffer buffer = new StringBuffer();
				int bitSize = 0, value = 0; 
				for (int i = 0; i < messageDigest.length; i++) {
					value <<= 8;
					value |= 0xFF & messageDigest[i];
					bitSize += 8;
					
					while (bitSize >= 5) {
						buffer.append(get32Char((value >> (bitSize - 5)) & 0x1F));
						bitSize -= 5;
					}
				}
				
				if (bitSize > 0) {
					value &= 0x1F >> (5 - bitSize);
					buffer.append(get32Char(value));
				}
				
				return buffer.toString();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		protected class ClearFileTask implements Runnable {
			
			StoreDeleteCallback mCallback;
			long mTimeline;
			boolean mAuto;
			
			public ClearFileTask(StoreDeleteCallback callback, long timeline, boolean auto) {
				mCallback = callback;
				mTimeline = timeline;
				mAuto = auto;
			}
			
			@Override
			public void run() {
				mCallback.startDelete();
				
				try {
					if (mTimeline > 0) {
						delete(mCallback, System.currentTimeMillis() - mTimeline);
						
						if (mAuto && length() >= (mFSAutoClearSize * 9 / 10) 
								&& ! mCallback.isCancel()) {
							deleteHalf(mCallback);
						}
					} else {
						delete(mCallback);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				mCallback.endDetele();
				
				if (! mAuto) {
					notifyStoreSizeResult();
				}
				
				mClearFileTask = null;
			}
		};
		
		Runnable SizeComputeRunnable = new Runnable() {
			
			@Override
			public void run() {
				mInit = false;
				
				File file = getFile();
				if (file.exists()) {
					for (StoreDir dir : mSubDirs) {
						if (dir.exists()) {
							File tmp = dir.getFile();
							String[] names = tmp.list();
							
							dir.initChildExist(names);

							long[] sizes = StoreDir.readSizeLength(dir);
							
							if (names != null) {
								if (Math.abs(names.length - sizes[0]) > 3
										|| sizes[1] <= 0) {
									dir.resetSizeLength();
									
									int size = names.length;
									String path = dir.getPath();
									sizes[1] = 0;
									for (int j = 0; j < size; j++) {
										sizes[1] += new File(path, names[j]).length();
										names[j] = null;
									}
									
									sizes[0] = size;
								}
							} else {
								dir.resetSizeLength();
								dir.initSizeLength(0, 0);
							}
						} else {
							dir.initChildExist(null);
						}
					}
				}
				
				mInit = true;
				
				notifyStoreSizeResult();
			}
		};
		
		public void registerSizeObserver(SizeObserver observer) {
			if (DEBUG) Log.d(TAG, "registerSizeObserver " + observer);
			
			if (mList == null) {
				mList = new LinkedList<SizeObserver>();
			}
			
			if (! mList.contains(observer)) {
				mList.add(observer);
			}
			
			if (mInit) {
				observer.onGetStoreSizeResult(getStoreSizeResult());
			}
		}
		
		public void unregisterSizeObserver(SizeObserver observer) {
			if (DEBUG) Log.d(TAG, "unregisterSizeObserver " + observer);
			
			if (mList != null) {
				mList.remove(observer);
			}
		}
		
		protected void notifyStoreSizeResult() {
			if (mList != null) {
				List<SizeObserver> observers = new LinkedList<SizeObserver>();
				observers.addAll(mList);
				
				StoreSizeResult result = getStoreSizeResult();
				if (DEBUG) Log.d(TAG, "notifyStoreSizeResult: " + result.toString());
				
				for (SizeObserver observer : observers) {
					observer.onGetStoreSizeResult(result);
				}
			}
		}
		
		private StoreSizeResult getStoreSizeResult() {
			StoreSizeResult result = new StoreSizeResult();
			result.mSize = size();
			result.mLength = length();
			result.mSize = result.mSize > 0 ? result.mSize : 0;
			result.mLength = result.mLength > 0 ? result.mLength : 0;
			
			result.mPath = getFile().getPath();
			
			return result;
		}
	}
	
	private static char get32Char(int value) {
//		value = value & 0x1F;
		char ch;
		if (value < 10) {
			ch = (char) ('0' + value);
		} else {
			ch = (char) ('a' + (value - 10));
		}
		
		return ch;
	}
	
	/************************以下外部方法*******************************/
	
	public FileManager(String root) {
		this(root, FSAutoClear, FSAutoClearSize, FSAutoClearTime);
	}
	
	public FileManager(String root, boolean autoClear, 
			long autoClearSize, long autoClearTime) {
		if (! root.endsWith("/")) {
			root += "/";
		}
		mRoorDir = new RootStoreDir(root, autoClear, autoClearSize, autoClearTime);
	}
	
	/**
	 * 获取FileManager的Path
	 * 
	 * @return
	 */
	public String getPath() {
		return mRoorDir.getPath();
	}
	
	/**
	 * 获取FileManager所管理的文件个数
	 * 
	 * @return
	 */
	public int size() {
		return mRoorDir.size();
	}
	
	/**
	 * 获取FileManager所管理的文件大小
	 * 
	 * @return
	 */
	public long length() {
		return mRoorDir.length();
	}
	
	/**
	 * 重设文件夹大小
	 */
	public void lengthReset() {
		mRoorDir.lengthReset();
	}
	
	/**
	 * 移除文件大小注册
	 * 
	 * @param observer
	 */
	public void unregisterSizeObserver(SizeObserver observer) {
		mRoorDir.unregisterSizeObserver(observer);
	}
	
	/**
	 * 添加文件大小注册
	 * 
	 * @param observer
	 */
	public void registerSizeObserver(SizeObserver observer) {
		mRoorDir.registerSizeObserver(observer);
	}
	
	/**
	 * 删除
	 * 
	 * @param callback
	 * @param timeline 多长时间以前
	 * @param force 是否强制执行
	 */
	public void delete(StoreDeleteCallback callback, 
			long timeline, boolean force) {
		mRoorDir.startClearFileTask(callback, timeline, force);
	}
	
	/**
	 * 
	 * @param url
	 * @return
	 */
	public StoreFile getStoreFile(String url) {
		StoreFile file = mRoorDir.getStoreFile(url);
		return file;
	}
	
	/**
	 * 
	 * @param localPath
	 * @return
	 */
	public StoreFile parseStoreFile(String localPath) {
		StoreFile file = null;
		if (! TextUtils.isEmpty(localPath)) {
			file = mRoorDir.parseStoreFile(localPath);
		}
		
		return file;
	}
	
	/************************以上外部方法*******************************/

	
}
