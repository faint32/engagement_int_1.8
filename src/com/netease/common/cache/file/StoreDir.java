package com.netease.common.cache.file;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.util.Log;


public class StoreDir extends StoreFile {

	private static final boolean DEBUG = false;
	private static final String TAG = "StoreDir";
	
	private static LinkedBlockingQueue<Runnable> mSizeFlushQueue = 
					new LinkedBlockingQueue<Runnable>();  
	private static ThreadPoolExecutor mSizeFlushers = new ThreadPoolExecutor(2, 
			5, 10, TimeUnit.SECONDS, mSizeFlushQueue);
	
	protected static char getInt2Char(int value) {
		return (char)(value < 10 ? '0' + value : 'a' + value - 10);
	}
	
	protected static int getChar2Int(char ch) {
		int value = 0;
		// '0' < 'a'
		if (ch >= 'a') {
			value = ch - 'a' + 10;
		} else {
			value = ch - '0';
		}
		return value;
	}
	
	long mLength;
	int mSize;
	int mLastFlushSize;
	boolean mChanged;
	
	byte[] mExistStatus; 
	
	public StoreDir(String filepath) {
		this(null, filepath);
	}
	
	public StoreDir(StoreDir dir, String filename) {
		super(dir, filename);
	}
	
	public long length() {
		return mLength;
	}
	
	public int size() {
		return mSize;
	}
	
	public boolean isChanged() {
		return mChanged;
	}
	
	public StoreFile[] listFiles() {
		StoreFile[] files = null;
		
		File file = getFile();
		if (file.exists()) {
			String[] names = file.list();
			if (names != null) {
				int size = names.length;
				files = new StoreFile[size];
				for (int i = 0; i < size; i++) {
					// 更加确切的情况下应该判断文件还是目录
					files[i] = new StoreFile(this, names[i]);
				}
			}
		}
		
		return files;
	}
	
	@Override
	public boolean renameTo(StoreFile file) {
		return false;
	}
	
	private static int getNameValue(String name) {
		int value = -1;
		
		if (name.length() > 16) {
			// 32 进制表示
			value = getChar2Int(name.charAt(3));
			value <<= 5;
			value += getChar2Int(name.charAt(4));
			value <<= 5;
			value += getChar2Int(name.charAt(5));
		}
		
		return value;
	}
	
	/**
	 * 
	 * @param storeFile
	 * @return exist or unknown;
	 */
	protected boolean childExist(StoreFile storeFile) {
		boolean ret = true;
		if (mExistStatus != null) {
			String name = storeFile.getName();
			int value = getNameValue(name);
			
			if (value >= 0) {
				int status = 0x01 << (value & 0x07);
				value >>= 3;
				
				if (value >= 0 && value < FileManager.MemCacheSize) {
					ret = (mExistStatus[value] & status) != 0;
					if (DEBUG) Log.d(TAG, "ret: " + ret + " value: " + value + " status: " + status);
				}
			}
		}
		return ret;
	}
	
	protected static void addFileExist(byte[] existStatus, String name) {
		int value = getNameValue(name);
		if (value >= 0) {
			int status = 0x01 << (value & 0x07);
			value >>= 3;
			
			if (value >= 0 && value < FileManager.MemCacheSize) {
				existStatus[value] |= status;
			}
		}
	}
	
	/**
	 * 初始化
	 * @param childNames
	 */
	protected void initChildExist(String[] childNames) {
		byte[] tmp = new byte[FileManager.MemCacheSize];
		if (childNames != null) {
			int length = childNames.length;
			for (int i = 0; i < length; i++) {
				addFileExist(tmp, childNames[i]);
			}
		}
		
		mExistStatus = tmp;
	}

	protected void createNewFile(String name) {
		if (mExistStatus != null) {
			addFileExist(mExistStatus, name);
		}
	}
	
	@Override
	public void delete() {
	}
	
	
	protected void deletFiles(StoreFile[] files, int start, int end, 
			StoreDeleteCallback callback, boolean allDir, long timeline) {
		long length = 0;
		int deleteSize = 0;
		
		for (int i = start; i < end; i++) {
			if (files[i] == null) {
				continue;
			}
			files[i].open();
			if (allDir || ! files[i].isInterDirectory()) {
				if (timeline <= 0 || files[i].lastModified() < timeline){
					length += files[i].length();
					deleteSize++;
					files[i].delete();
				}
			}
			files[i] = null;
			
			if ((i & 0x07) == 0x07) {
				// 每8个文件更新一次
				callback.addDelete(0x08, length);
				length = 0;
				deleteSize = 0;
				
				if (callback.isCancel()) {
					break;
				}
			}
		}
		
		if ((deleteSize & 0x07) > 0) {
			callback.addDelete(deleteSize & 0x07, length);
		}
	}
	
	public void delete(StoreDeleteCallback callback) {
		StoreFile[] files = listFiles();
		if (files != null) {
			deletFiles(files, 0, files.length, callback, true, -1);
		}

		if (! callback.isCancel()) {
			mChanged = false;
//			Util.delAllFiles(getPath());
			resetSizeLength();
			
			if (mExistStatus != null) {
				Arrays.fill(mExistStatus, 0, FileManager.MemCacheSize, (byte)0);
			}
		}
	}
	
	/**
	 * 按照对时间以前
	 * @param time
	 */
	public void delete(StoreDeleteCallback callback, long time) {
		StoreFile[] files = listFiles();
		if (files != null) {
			deletFiles(files, 0, files.length, callback, true, time);
		}
	}
	
	/**
	 * 按照时间中段删除一半
	 */
	public void deleteHalf(StoreDeleteCallback callback) {
		StoreFile[] files = listFiles();
		
		if (files != null && ! callback.isCancel()) {
			Arrays.sort(files);
			
			int size = files.length;
			// 新的置空，不删除
			for (int i = size >> 1; i < size; i++) {
				files[i] = null;
			}
			
			size = size >> 1;
			if (! callback.isCancel()) {
				deletFiles(files, 0, size, callback, true, -1);
			}
		}
	}
	
	/**
	 * 再初始化统计过程中调用
	 */
	protected void resetSizeLength() {
		if (mSize > 0) {
			sizeChange(- mSize);
		}
		if (mLength > 0) {
			lengthChange(- mLength);
		}
	}
	
	/**
	 * 再初始化统计过程中调用
	 */
	protected void initSizeLength(int size, long length) {
		mLastFlushSize = size;
		if (size > 0) {
			sizeChange(size);
		}
		if (length > 0) {
			lengthChange(length);
		}
	}
	
	/**
	 * 
	 */
	protected void initExistStatus(byte[] status) {
		mExistStatus = status;
	}
	
	@Override
	public boolean createNewFile() {
		return false;
	}
	
	public boolean mkdirs() {
		boolean ret = getFile().mkdirs();
		if (ret) {
			sizeChange(1);
		}
		
		return ret;
	}
	
	public StoreFile getStoreFile(String name) {
		return new StoreFile(this, name);
	}
	
	@Override
	protected void sizeChange(int size) {
		if (size != 0) {
			mChanged = true;
			mSize += size;
			super.sizeChange(size);
		}
	}
	
	@Override
	protected void lengthChange(long length) {
		if (length != 0) {
			mChanged = true;
			mLength += length;
			super.lengthChange(length);
			
			if (Math.abs(mSize - mLastFlushSize) >= 3) {
				mLastFlushSize = size();
				try {
					mSizeFlushers.submit(SizeWriteRunnable);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	protected void flushSize() {
		if (! mSizeFlushQueue.contains(SizeWriteRunnable)) {
			try {
				mSizeFlushers.submit(SizeWriteRunnable);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	Runnable SizeWriteRunnable = new Runnable() {
		
		@Override
		public void run() {
			mLastFlushSize = size();
			writeSizeLength(StoreDir.this);
		}
	};

	protected static long[] readSizeLength(StoreDir dir) {
		long[] sizes = new long[2];
		
		String dirPath = dir.getPath();
		File file = new File(dirPath, ".size");
		if (file.exists()) {
			DataInputStream inStream = null;
			try {
				inStream = new DataInputStream(
						new FileInputStream(file));
				sizes[0] = inStream.readInt();
				sizes[1] = inStream.readLong();
				
				inStream.close();
			} catch (IOException e) {
//				e.printStackTrace();
			} finally {
				if (inStream != null) {
					try {
						inStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		if (sizes[0] <= 0 || sizes[1] <= 0) {
			sizes[0] = 0;
			sizes[1] = 0;
		}
		
		dir.resetSizeLength();
		dir.sizeChange((int) sizes[0]);
		dir.lengthChange(sizes[1]);
		
		return sizes;
	}
	
	protected static void writeSizeLength(StoreDir dir) {
		if (dir.exists() && dir.isChanged()) {
			dir.mChanged = false;
			String dirPath = dir.getPath();
			File file = new File(dirPath, ".size");
			if (! file.exists()) {
				dir.sizeChange(1);
			}
			DataOutputStream outStream = null;
			try {
				outStream = new DataOutputStream(
						new FileOutputStream(file));
				outStream.writeInt(dir.size());
				outStream.writeLong(dir.length());
			} catch (IOException e) {
//				e.printStackTrace();
			} finally {
				if (outStream != null) {
					try {
						outStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		}
	}
}
