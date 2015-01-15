package com.netease.engagement.image.explorer.adapter;

public class ImageExplorerBean {
	
	private long origId; //图片的id
	
	private String path; //图片的文件路径
	
	public ImageExplorerBean(int origId, String path) {
		this.origId = origId;
		this.path = path;
	}

	public long getOrigId() {
		return origId;
	}

	public void setOrigId(int origId) {
		this.origId = origId;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	
}
