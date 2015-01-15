package com.netease.engagement.image.explorer.adapter;

public class FileExplorerBean {
	
	private String displayName;
	private long origID;
	private int picCount;
	private String data;
	
	public FileExplorerBean(String displayName, long origID, int picCount, String data) {
		this.displayName = displayName;
		this.origID = origID;
		this.picCount = picCount;
		this.data = data;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public long getOrigID() {
		return origID;
	}
	public void setOrigID(long origID) {
		this.origID = origID;
	}
	public int getPicCount() {
		return picCount;
	}
	public void setPicCount(int picCount) {
		this.picCount = picCount;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
	
	
}
