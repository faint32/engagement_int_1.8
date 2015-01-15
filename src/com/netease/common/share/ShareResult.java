package com.netease.common.share;

import java.util.List;


public class ShareResult {

	private ShareType mShareType; 
	private int mCode;
	private String mMessageCode;
	private String mMessage;
	private boolean mSuccess;
	
	private boolean mFollowingOfficial;
	
	private ShareBind mShareBind;
	
	private List<ShareBindFriend> mFriends;
	
	public ShareResult(ShareType shareType, boolean success) {
		mShareType = shareType;
		mSuccess = success;
		mFollowingOfficial = true;
	}
	
	public ShareBind getShareBind() {
		return mShareBind;
	}

	public void setShareBind(ShareBind shareBind) {
		this.mShareBind = shareBind;
	}
	
	public boolean isFollowingOfficial() {
		return mFollowingOfficial;
	}
	
	public void setFollowingOfficial(boolean value) {
		mFollowingOfficial = value;
	}

	public boolean isSuccess() {
		return mSuccess;
	}

	public int getCode() {
		return mCode;
	}

	public void setCode(int code) {
		this.mCode = code;
	}
	
	public String getMessageCode() {
		return mMessageCode;
	}

	public void setMessageCode(String code) {
		this.mMessageCode = code;
	}

	public String getMessage() {
		return mMessage;
	}

	public void setMessage(String message) {
		this.mMessage = message;
	}

	public ShareType getShareType() {
		return mShareType;
	}

	public List<ShareBindFriend> getFriends() {
		return mFriends;
	}

	public void setFriends(List<ShareBindFriend> friends) {
		this.mFriends = friends;
	}
	
}
