package com.netease.common.cache.file;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class StoreTmpFileInfo {

	private static final boolean DEBUG = false;
	
	private static final int MAX_TMP_INFO_SIZE = 2 << 10;
	
	private String eTag;
	private String lastModify;
	private long length;
	private long currentSize;
	private LinkedList<Long> failedSeeks;
	private LinkedList<Long> runningSeeks;
	private StoreFile mStoreFile;
	
	private long sizeAppend;
	
	private Object[] lock;
	
	public StoreTmpFileInfo(StoreFile storeFile) {
		lock = new Object[0];
		
		failedSeeks = new LinkedList<Long>();
		runningSeeks = new LinkedList<Long>();
		mStoreFile = storeFile;
	}
	
	public long getSizeAppend() {
		return sizeAppend;
	}
	
	public void reset(String etag, String lastModify, long length) {
		this.eTag = etag;
		this.lastModify = lastModify;
		this.length = length;
		this.currentSize = 0;
		
		synchronized (lock) {
			failedSeeks.clear();
			runningSeeks.clear();
		}
	}
	
	public StoreFile getStoreFile() {
		return mStoreFile;
	}
	
	public long getLength() {
		return length;
	}
	
	public long getCurrentSize() {
		return currentSize;
	}
	
	public long getFailedSize() {
		long size = 0;
		synchronized (lock) {
			int tmp = failedSeeks.size() - 1;
			for (int i = 0; i < tmp; i++, i++) {
				long end = failedSeeks.get(i + 1).longValue() + 1;
				long start = failedSeeks.get(i).longValue();
				
				size += end - start;
			}
		}
		
		return size;
	}

	public String getETag() {
		return eTag;
	}

	public String getLastModify() {
		return lastModify;
	}
	
	public long[] requestNextFragement(long defaultSize) {
		long[] seeks = new long[2];
		synchronized (lock) {
			if (failedSeeks.size() > 1) {
				seeks[0] = failedSeeks.remove(0).longValue();
				seeks[1] = failedSeeks.remove(0).longValue() + 1;
			}
			else if (currentSize < length) {
				seeks[0] = currentSize;
//				seeks[1] = currentSize + Math.min(length - currentSize, defaultSize);
				
				long tmp = length - currentSize;
				if (tmp > defaultSize) {
					// 缺省值最小为总长度的8分之一
					defaultSize = Math.max(defaultSize, ((length >> 10) / 6) << 10);
					
					tmp = (tmp >> 12) << 10;
					if (tmp > defaultSize) { // 4分之一对比缺省值，4分之一大于缺省值，则用4分之一，减少请求数
						seeks[1] = currentSize + tmp;
					}
					else {
						seeks[1] = Math.min(currentSize + defaultSize, length);
					}
				}
				else {
					seeks[1] = length;
				}
				
				currentSize = seeks[1];
			}
			else {
				return null;
			}
			
			runningSeeks.add(Long.valueOf(seeks[0]));
			runningSeeks.add(Long.valueOf(seeks[1] - 1));
		}
		
		return seeks;
	}
	
	public void addFailFragement(long start, long end) {
		synchronized (lock) {
			int index = runningSeeks.indexOf(Long.valueOf(end - 1));
			
			if (index > 0) {
				// remove end
				runningSeeks.remove(index);
				// remove start
				Long oldStart = runningSeeks.remove(index - 1);
				sizeAppend += start - oldStart.longValue();
				
				failedSeeks.add(Long.valueOf(start));
				failedSeeks.add(Long.valueOf(end - 1));
				
				if (start - oldStart.longValue() > 0) {
					save();
				}
			}
		}
	}
	
	public void removeSuccessFragement(long start, long end) {
		synchronized (lock) {
			int index = runningSeeks.indexOf(Long.valueOf(end - 1));

			if (DEBUG) System.out.println(" >>> start: " + "end: " + end);
			
			if (index > 0) {
				runningSeeks.remove(index);
				// remove start
				runningSeeks.remove(index - 1);
				
				sizeAppend += end - start;
			}
		}
	}
	
	public boolean isSuccess() {
		boolean ret = false;
		
		synchronized (lock) {
			if (runningSeeks.size() == 0
					&& failedSeeks.size() == 0
					&& length <= currentSize) {
				ret = true;
			}
		}
		
		return ret; 
	}
	
	public void readInfo() {
		File file = mStoreFile.getTmpInfoFile();
		
		if (file.exists()) {
			if (file.length() < (MAX_TMP_INFO_SIZE)) {
				byte[] data = new byte[(int) file.length()];
				FileInputStream fis = null;
				try {
					fis = new FileInputStream(file);
					int length = fis.read(data);
					
					if (length == data.length) {
						parseData(data);
					}
					fis.close();
				} catch (IOException e) {
					if (fis != null) {
						try {
							fis.close();
						} catch (IOException e1) {
						}
					}
				}
			}
		}
	}
	
	private void parseData(byte[] data) {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		DataInput dit = new DataInputStream(bais);
		
		// version
		try {
			if (dit.readByte() == 1) {
				length = dit.readLong();
				eTag = dit.readUTF();
				lastModify = dit.readUTF();
				
				int size = dit.readInt();
				if ((size & 0x1) == 0) {
					currentSize = dit.readLong();
					
					for (int i = 0; i < size; i++) {
						failedSeeks.add(Long.valueOf(dit.readLong()));
					}
					
				}
			}
			
			bais.close();
		} catch (IOException e) {
		}
		
		if ((failedSeeks.size() & 0x1) != 0 || currentSize == 0) {
			currentSize = 0;
			failedSeeks.clear();
		}
	}

	private byte[] getData() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutput dot = new DataOutputStream(baos);
		
		long current = 0;
		List<Long> failed;
		List<Long> running;
		
		synchronized (lock) {
			current = currentSize;
			running = new LinkedList<Long>(runningSeeks);
			failed = new LinkedList<Long>(failedSeeks);
		}
		
		failed.addAll(running);
		
		try {
			// version
			dot.write(1);
			dot.writeLong(length);
			dot.writeUTF(eTag == null ? "" : eTag);
			dot.writeUTF(lastModify == null ? "" : lastModify);
			
			int size = failed.size();
			dot.writeInt(size);
			dot.writeLong(current);
			
			for (int i = 0; i < size; i++) {
				dot.writeLong(failed.get(i).longValue());
			}
			
			baos.close();
		} catch (Exception e) {
			
		}
		
		return baos.toByteArray();
	}
	
	public void save() {
		if (sizeAppend > 0) {
			File file = mStoreFile.getTmpInfoFile();
			
			if (file.getParentFile().exists()) {
				BufferedOutputStream bos = null;
				try {
					byte[] data = getData();
					if (data != null && data.length > 0) {
						bos = new BufferedOutputStream(new FileOutputStream(file));
						bos.write(data);
					}
				} catch (Exception e) {
				} finally {
					if (bos != null) {
						try {
							bos.close();
						} catch (IOException e) { }
					}
				}
			}
		}
	}
	
	public void delete() {
		File file = mStoreFile.getTmpInfoFile();
		file.delete();
	}
}
