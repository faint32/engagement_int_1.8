package com.netease.common.nio;

import java.io.IOException;
import java.io.OutputStream;

public class NioOutputStream extends OutputStream {

	private static Object[] SizeLock = new Object[0];
	private static int TotalSpeed = 0;
	private static int TmpTotalSpeed = 0;
	private static NioListener GlobalNioListener;
	private static long TotalSize = 0;
	
	private static final int STATISTICS_SECEND_BIT = 2;// 与NioSpeedListener一致
	
	private int mSpeed; // 过去几秒钟的平均值
	private int mTmpSpeed;// 用来存放过去几秒钟的数据
	private int mSize;
	private long mStartTime;
	private NioListener mNioListener;
	
	OutputStream mOutputStream;
	
	public NioOutputStream(OutputStream out) {
		if (out == null) {
			throw new IllegalArgumentException("out is null");
		}
		mOutputStream = out;
		mStartTime = System.currentTimeMillis();
	}
	
	public void setNioListener(NioListener listener) {
		mNioListener = listener;
	}
	
	public static void setGlobalNioListener(NioListener listener) {
		GlobalNioListener = listener;
	}

	public static void initTotalSize(long size) {
		TotalSize = size;
	}
	
	
	@Override
	public void write(int b) throws IOException {
		mOutputStream.write(b);
		increaseSize(1);
	}
	
	@Override
	public void write(byte[] buffer) throws IOException {
		write(buffer, 0, buffer.length);
	}
	
	@Override
	public void write(byte[] buffer, int offset, int count) throws IOException {
		mOutputStream.write(buffer, offset, count);
		increaseSize(count);
	}
	
	@Override
	public void close() {
		if (mOutputStream != null) {
			try {
				mOutputStream.close();
				super.close();
			} catch (IOException e) { }
			mOutputStream = null;
		}
	}

	private void increaseSize(int i) {
		if (i > 0) {
			synchronized (this) {
				mSpeed += i;
				mSize += i;
			}
			synchronized (SizeLock) {
				TotalSize += i;
				TmpTotalSpeed += i;
			}
			
			if (mNioListener != null) {
				mNioListener.onSizeIncrease(NioListener.TYPE_OUT_SIZE, i);
			}
			
			if (GlobalNioListener != null) {
				GlobalNioListener.onSizeIncrease(NioListener.TYPE_GLOBAL_OUT_SIZE, i);
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
					NioListener.TYPE_GLOBAL_OUT_SPEED, TotalSpeed);
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
			mNioListener.onSpeedChange(NioListener.TYPE_OUT_SPEED, mSpeed);
		}
	}
	
	/**
	 * 获取当前的瞬时速度
	 * 秒速 B/s
	 * @return
	 */
	public int getCurrentSpeed() {
		return mSpeed;
	}
	
	/**
	 * 获取当前的平均速度
	 * 秒速 B/s
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
	 * 秒速 B/s
	 * @return
	 */
	public static int getTotalSpeed() {
		return TotalSpeed;
	}
	
	/**
	 * 字节 B
	 * @return
	 */
	public static long getTotalSize() {
		return TotalSize;
	}
}
