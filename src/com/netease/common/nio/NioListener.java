package com.netease.common.nio;

public interface NioListener {

	public static final byte TYPE_IN_SPEED = 0x01;
	public static final byte TYPE_OUT_SPEED = 0x02;
	public static final byte TYPE_GLOBAL_IN_SPEED = 0x03;
	public static final byte TYPE_GLOBAL_OUT_SPEED = 0x04;
	
	public static final byte TYPE_IN_SIZE = 0x05;
	public static final byte TYPE_OUT_SIZE = 0x06;
	public static final byte TYPE_GLOBAL_IN_SIZE = 0x07;
	public static final byte TYPE_GLOBAL_OUT_SIZE = 0x08;
	
	public static final int STATISTICS_SECEND = 4;// 2的次数
	
	public void onSpeedChange(byte type, int speed);
	
	public void onContentLength(byte type, long length);
	
	public void onSizeIncrease(byte type, long size);
}
