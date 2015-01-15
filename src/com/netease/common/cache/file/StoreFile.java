package com.netease.common.cache.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class StoreFile implements Comparable<StoreFile> {
	
	private static final boolean DEBUG = false;
	private static final String TAG = "StoreFile";
	
	StoreDir mParent;
	String mFileName;
	File mFile;
	File mTmpFile;
	
	long mLastModified = -1;
	
	InputStream mInputStream;
	OutputStream mOutputStream;
	
	boolean mGzip;
	
	public StoreFile(String filePath) {
		mFileName = filePath;
	}
	
	public StoreFile(StoreDir dir, String filename) {
		mParent = dir;
		mFileName = filename;
	}
	
	public String getName(){
		if (mParent != null) {
			return mFileName;
		}
		
		return getFile().getName();
	}
	
	public Uri getUri() {
		return Uri.fromFile(getFile());
	}
	
	public String getPath() {
	    String path = "";
	    if(mParent != null){
	        path = mParent.getPath();
	        if(!TextUtils.isEmpty(path) && !path.endsWith("/")){
	            path +="/";
	        }       
	    }
		return  path + mFileName;
	}
	
	protected File getFile() {
		File file = null;
		if (mFile != null) {
			file = mFile;
		} else {
			file = new File(getPath());
		}
		
		return file;
	}
	
	/**
	 * 下载过程的中的临时文件
	 * 
	 * @return
	 */
	public File getTmpFile() {
		if (mTmpFile == null) {
			mTmpFile = new File(getPath() + "_tmp");
		}
		
		return mTmpFile;
	}
	
	/**
	 * 用于断点续传的
	 * 
	 * @return
	 */
	public File getTmpInfoFile() {
		return new File(getPath() + "_tmp_info");
	}
	
	public RandomAccessFile getRandomAccessFile() throws IOException {
		checkParent();
		
		File file = getTmpFile();
		if (! file.exists()) {
			file.createNewFile();
		}
		
		return new RandomAccessFile(file, "rwd");
	}
	
	
	public boolean isDirectory() {
		return getFile().isDirectory();
	}
	
	public boolean isInterDirectory() {
		boolean ret = false;
		if (getFile().isDirectory()) {
			ret = getFile().getName().length() == 2;
		}
		
		return ret;
	}
	
	public void setGzip() {
		mGzip = true;
	}

	public long lastModified() {
		if (mLastModified < 0) {
			mLastModified = getFile().lastModified();
		}
		return mLastModified;
	}
	
	public void setLastModified(long time) {
		mLastModified = time;
		getFile().setLastModified(time);
	}
	
	public boolean exists() {
		long time = System.currentTimeMillis();
		boolean ret = false;
		
		if (getParent() == null || getParent().childExist(this)) {
			ret = getFile().exists();
			
			if (DEBUG) {
				Log.w("store file", this + " exist " + (System.currentTimeMillis() - time));
			}
		}
		
		return ret;
	}
	
	public long length() {
		return getFile().length();
	}
	
	public StoreDir getParent() {
		return mParent;
	}
	
	protected void lengthChange(long length) {
		if (mParent != null && length != 0) {
			mParent.lengthChange(length);
		}
	}
	
	protected void sizeChange(int size) {
		if (mParent != null && size != 0) {
			mParent.sizeChange(size);
		}
	}
	
	public boolean createNewFile() throws FileCreateException {
		boolean ret = false;
		try {
			if (getParent() == null) {
				throw new IOException("parent null");
			}
			File file = getParent().getFile();
			if (! file.exists()) {
				if (file.mkdirs()) {
					getParent().sizeChange(1);
				}
			}
			ret = getTmpFile().createNewFile();
			
			if (ret) {
				getParent().createNewFile(getName());
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new FileCreateException();
		}
		
		if (ret) {
			sizeChange(1);
		}
		
		return ret;
	}
	
	public void open() {
		if (mFile == null) {
			mFile = new File(getPath());
		}
	}
	
	public boolean renameTo(StoreFile storefile) {
		boolean ret = false;
		File file = getFile();
		if (file.exists()) {
			long length = file.length();

			// 使用 rename 返回值进行判断，减少一个目录存在判断
//			if (! storefile.getParent().exists()) {
//				storefile.getParent().mkdirs();
//			}
			
			ret = file.renameTo(storefile.getFile());

			if (ret) {
				if (getParent() == null) {
					return true;
				}
				
				storefile.getParent().createNewFile(
						storefile.getFile().getName());
				
				sizeChange(-1);
				lengthChange(- length);

				storefile.sizeChange(1);
				storefile.lengthChange(length);
			}
		}
		
		return ret;
	}
	
	public boolean renameTo(File target) {
		boolean ret = false;
		File file = getFile();
		if (file.exists()) {
			long length = file.length();
			
			ret = file.renameTo(target);
			
			if (ret) {
				sizeChange(-1);
				lengthChange(- length);
			}
		}
		
		return ret;
	}
	
	public void delete() {
		File file = getFile();
		if (file.exists()) {
			sizeChange(- 1);
			lengthChange(- file.length());
			
			if (file.isDirectory()) {
				deleteDir(file);
			} else {
				file.delete();
			}
		}
	}
	
	public InputStream openInputStream() throws IOException {
		if (DEBUG) Log.d(TAG, "openInputStream" + getPath());
		
		if (getFile().exists()) {
			mInputStream = new FileInputStream(getPath());
		} else {
			throw new IOException("not exist");
		}
		return mInputStream;
	}
	
	private void checkParent() throws IOException {
		if (getParent() == null) {
			if (DEBUG) Log.e(TAG, "openOutputStream parent null");
			throw new IOException("parent null");
		}
		
		File file = getParent().getFile();
		if (! file.exists()) {
			if (DEBUG) Log.d(TAG, "openOutputStream Parent not exist " + getPath());
			if (file.mkdirs()) {
				if (DEBUG) Log.d(TAG, "openOutputStream Parent create " + getPath());
				getParent().sizeChange(1);
			}
		}
	}
	
	public OutputStream openOutputStream() throws IOException {
		return openOutputStream(false);
	}
	
	public OutputStream openOutputStream(boolean append) throws IOException {
		if (DEBUG) Log.e(TAG, "openOutputStream" + getPath());
		
		checkParent();
		
		if (getTmpFile().exists()) {
			mOutputStream = new FileOutputStream(getTmpFile());
		} else {
			createNewFile();
			mOutputStream = new FileOutputStream(getTmpFile());
		}
		return mOutputStream;
	}
	
	public void closeInputStream() {
		if (DEBUG) Log.d(TAG, "closeInputStream" + getPath());
		
		if (mInputStream != null) {
			try {
				mInputStream.close();
			} catch (IOException e) { }
			mInputStream = null;
		}
	}
	
	public void closeOutputStream() {
		if (DEBUG) Log.d(TAG, "closeOutputStream" + getPath());
		
		if (mOutputStream != null) {
			try {
				mOutputStream.close();
				
				if (getFile().exists()) {
					lengthChange(- getFile().length());
					
					getFile().delete();
				}
				
				getTmpFile().renameTo(getFile());
				
				lengthChange(getFile().length());
			} catch (IOException e) { }
			
			mOutputStream = null;
		}
	}
	
	public void update(StoreTmpFileInfo fileInfo) {
		if (DEBUG) Log.d(TAG, "update StoreTmpFileInfo: " + fileInfo.isSuccess() + " path: "+ getPath());
		
		if (fileInfo.isSuccess() && getTmpFile().exists()) {
//				&& fileInfo.getLength() == getTmpFile().length()) {
			if (getFile().exists()) {
				lengthChange(- getFile().length());
				getFile().delete();
			}
			else {
				sizeChange(1);
			}
			
			if (DEBUG) Log.d(TAG, "update StoreTmpFileInfo rename: " + getPath());
			
			if (getTmpFile().renameTo(getFile())) {
				if (DEBUG) Log.d(TAG, "update StoreTmpFileInfo renameTo: " + true);
				
				lengthChange(fileInfo.getSizeAppend());
				
				getTmpInfoFile().delete();
			}
			else {
				if (DEBUG) Log.d(TAG, "update StoreTmpFileInfo renameTo: " + false);
			}
			
			mFile = null;
			mTmpFile = null;
		}
	}
	
	public void close() {
		closeInputStream();
		closeOutputStream();
		mFile = null;
		mTmpFile = null;
	}
	
	@Override
	public int compareTo(StoreFile another) {
		int ret = 0;
		if (lastModified() < another.lastModified()) {
			ret = -1;
		} else if (lastModified() > another.lastModified()) {
			ret = 1;
		}
		
		return ret;
	}
	
	
	protected static void deleteDir(File file) {
		String[] names = file.list();
		if (names != null) {
			int size = names.length;
			for (int i = 0; i < size; i++) {
				File tmp = new File(file, names[i]);
				if (tmp.isDirectory()) {
					deleteDir(tmp);
				} else {
					tmp.delete();
				}
				
				names[i] = null;
			}
		}
		file.delete();
	}
	
	@Override
	public int hashCode() {
		return getPath().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		boolean ret = false;
		if (o != null && o instanceof StoreFile) {
			ret = getPath().equals(((StoreFile) o).getPath()); 
		}
		
		return ret;
	}
	
	@Override
	public String toString() {
		return getPath();
	}
}
