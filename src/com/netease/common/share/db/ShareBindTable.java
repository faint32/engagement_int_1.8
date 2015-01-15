package com.netease.common.share.db;

import android.net.Uri;

public interface ShareBindTable {

	public static final String TABLE_NAME = "share_bind"; 
	
	public static final Uri CONTENT_URI = Uri.parse("content://shareb/" + TABLE_NAME);
	
	public static final String C_SHARE_KEY = "skey"; // string
	public static final String C_SHARE_TYPE = "type"; // int
	public static final String C_ACCESS_TOKEN = "access_t"; // string
	public static final String C_REFRESH_TOKEN = "refresh_t"; // string
	public static final String C_EXPIRES = "expires"; // long
	public static final String C_BIND_TIME = "bind_t"; // long
	public static final String C_NAME = "name"; // string
	public static final String C_USERID = "userid"; // string
	public static final String C_PROFILE = "profile"; // string
	public static final String C_DOMAIN = "domain"; // string
	public static final String C_STATE = "state"; // int
	public static final String C_JSON = "json"; // string
	public static final String C_DATA1 = "data1"; // string
	public static final String C_DATA2 = "data2"; // string
	
}
