package com.netease.engagement.util;


public class DialogInfo  {
	
	private int type;
	private Object info;
	
	public DialogInfo(int type, Object info) {
		this.type = type;
		this.info = info;
	}
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public Object getInfo() {
		return info;
	}
	public void setInfo(Object info) {
		this.info = info;
	}
	
}
