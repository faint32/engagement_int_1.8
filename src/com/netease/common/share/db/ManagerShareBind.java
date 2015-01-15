package com.netease.common.share.db;

import android.database.Cursor;

import com.netease.common.share.ShareBind;
import com.netease.common.share.ShareType;


public class ManagerShareBind {

	private static ShareBindManager mShareBindManager = new ShareBindDBManager();
	
	public static Cursor getAllShareBind(String key) {
		Cursor cursor = null;
		if (key != null) {
			cursor = mShareBindManager.getAllShareBind(key);
		}
		
		return cursor;
	}
	
	public static Cursor getAllValidShareBind(String key) {
		Cursor cursor = null;
		if (key != null) {
			cursor = mShareBindManager.getAllValidShareBind(key);
		}
		
		return cursor;
	}
	
	public static void addShareBind(String key, ShareBind shareBind) {
		if (key != null) {
			mShareBindManager.addShareBind(key, shareBind);
		}
	}
	
	public static void updateShareBind(String key, ShareBind shareBind) {
		if (key != null) {
			mShareBindManager.updateShareBind(key, shareBind);
		}
	}
	
	public static void removeShareBind(String key, ShareType shareType) {
		if (key != null) {
			mShareBindManager.removeShareBind(key, shareType);
		}
	}
	
	public static void removeShareBind(String key) {
		if (key != null) {
			mShareBindManager.removeShareBind(key);
		}
	}
	
	public static void copyShareBind(String keySource, String keyTarget) {
		if (keySource != null && keyTarget != null) {
			mShareBindManager.copyShareBind(keySource, keyTarget);
		}
	}
	
	public static ShareBind getShareBind(String key, ShareType shareType) {
		ShareBind shareBind = null;
		if (key != null) {
			shareBind = mShareBindManager.getShareBind(key, shareType);
		}
		
		return shareBind;
	}
	
}
