package com.netease.common.share.db;

import android.database.Cursor;

import com.netease.common.share.ShareBind;
import com.netease.common.share.ShareType;

public interface ShareBindManager {

	public Cursor getAllShareBind(String key);
	
	public Cursor getAllValidShareBind(String key);
	
	public void addShareBind(String key, ShareBind shareBind);
	
	public void updateShareBind(String key, ShareBind shareBind);
	
	public void removeShareBind(String key, ShareType shareType);
	
	public void removeShareBind(String key);
	
	public void copyShareBind(String keySource, String keyTarget);
	
	public ShareBind getShareBind(String key, ShareType shareType);
	
}
