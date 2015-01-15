package com.netease.engagement.util;

public class LevelChangeStatusBean {
	
	public enum LevelChangeType {
		None,
		Male_Level_Up,
		Male_Level_Up_1,
		Male_Level_Down,
		Female_Level_Up
	}
	
	private static LevelChangeStatusBean instance;
	
	private LevelChangeStatusBean() {
	}

	public static LevelChangeStatusBean getInstance() {
		if (instance == null) {
			instance = new LevelChangeStatusBean();
			instance.clear();
		}
		return instance;
	}
	
	private long uid;
	private LevelChangeType type;
	private int oldLevel;
	private int newLevel;
	
	public void set(long uid, LevelChangeType type, int oldLevel, int newLevel) {
		this.uid = uid;
		this.type = type;
		this.oldLevel = oldLevel;
		this.newLevel = newLevel;
	}

	public long getUid() {
		return uid;
	}

	public LevelChangeType getType() {
		return type;
	}

	public int getOldLevel() {
		return oldLevel;
	}

	public int getNewLevel() {
		return newLevel;
	}
	
	public void clear() {
		uid = -1;
		type = LevelChangeType.None;
		oldLevel = 0;
		newLevel = 0;
	}
}
