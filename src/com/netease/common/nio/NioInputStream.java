package com.netease.common.nio;

import java.io.IOException;
import java.io.InputStream;

public class NioInputStream extends InputStream {

	private static Object[] SizeLock = new Object[0];
	private static int TotalSpeed = 0; // 过去几秒钟的平均值
	private static int TmpTotalSpeed = 0; // 用来存放过去几秒钟的数据
	private static NioListener GlobalNioListener;
	private static long TotalSize = 0;
	
	private static final int STATISTICS_SECEND_BIT = 2; // 与NioSpeedListener一致
	
	private int mSpeed; // 过去几秒钟的平均值
	private int mTmpSpeed;// 用来存放过去几秒钟的数据
	private int mSize;
	private long mStartTime;
	private NioListener mNioListener;
	
	protected InputStream mInputStream;
	
	public NioInputStream(InputStream in) {
		if (in == null) {
			throw new IllegalArgumentException("in is null");
		}
		mInputStream = in;
		mStartTime = System.currentTimeMillis();
	}
	
	public void setNioListener(NioListener listener) {
		mNioListener = listener;
	}
	
	public static void setGlobalNioListener(NioListener listener) {
		GlobalNioListener = listener;
	}

	/**
	 * 初始化总的大小
	 * @param size
	 */
	public static void initTotalSize(long size) {
		TotalSize = size;
	}
	
	@Override
	public int read() throws IOException {
		int b = mInputStream.read();
		if (b > -1) {
			increaseSize(1);
		}
		
		return b;
	}
	
	@Override
	public int available() throws IOException {
		return mInputStream.available();
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}
	
	@Override
	public int read(byte[] b, int offset, int length) throws IOException {
		int size = mInputStream.read(b, offset, length);
		increaseSize(size);
		
		return size;
	}
	
	@Override
	public void close() {
		if (mInputStream != null) {
			try {
				mInputStream.close();
				super.close();
			} catch (IOException e) { }
			mInputStream = null;
		}
	}
	
	private void increaseSize(int i) {
		if (i > 0) {
			synchronized (this) {
				mSize += i;
				mTmpSpeed += i;
			}
			synchronized (SizeLock) {
				TotalSize += i;
				TmpTotalSpeed += i;
			}
			
			if (mNioListener != null) {
				mNioListener.onSizeIncrease(NioListener.TYPE_IN_SIZE, i);
			}
			
			if (GlobalNioListener != null) {
				GlobalNioListener.onSizeIncrease(NioListener.TYPE_GLOBAL_IN_SIZE, i);
			}
		}
	}
	
	/**
	 * 重设临时速度
	 */
	public static void resetTotalSpeed() {
		synchronized (SizeLock) {
			TotalSpeed = TmpTotalSpeed >> STATISTICS_SECEND_BIT;
			TmpTotalSpeed = 0;
		}
		
		if (GlobalNioListener != null) {
			GlobalNioListener.onSpeedChange(
					NioListener.TYPE_GLOBAL_IN_SPEED, TotalSpeed);
		}
	}
	
	/**
	 * 重设临时速度
	 */
	public void resetSpeed() {
		synchronized (this) {
			mSpeed = mTmpSpeed >> STATISTICS_SECEND_BIT;
			mTmpSpeed = 0;
		}
		
		if (mNioListener != null) {
			mNioListener.onSpeedChange(NioListener.TYPE_IN_SPEED, mSpeed);
		}
	}
	
	/**
	 * 获取当前的瞬时速度
	 * @return
	 */
	public int getCurrentSpeed() {
		return mSpeed;
	}
	
	/**
	 * 获取当前的平均速度
	 * @return
	 */
	public int getAverageSpeed() {
		long time = (System.currentTimeMillis() - mStartTime) / 1000;
		if (time <= 0) {
			time = 1;
		}
		return (int) (mSize / time);
	}

	/**
	 * 获取总的流量速度
	 * @return
	 */
	public static int getTotalSpeed() {
		return TotalSpeed;
	}
	
	/**
	 * 
	 * @return
	 */
	public static long getTotalSize() {
		return TotalSize;
	}
	
}
