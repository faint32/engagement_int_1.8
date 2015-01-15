package com.netease.common.http.cache.db;

import android.net.Uri;

public class HttpCacheDBTables {

	private static String AUTHORITY = HttpCacheDatabase.Authority;
	
	public static final String[] TableNames = new String[]{
		CacheTable.TABLE_NAME
	};
	
	public static interface CacheTable {
		// 需要创建url索引
		public static final String TABLE_NAME = "hcache";
		
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
		
		public static final String C_URL = "url"; // String
		public static final String C_LASTMODIFY = "lastmodify"; // String
		public static final String C_CONTENT_LENGTH = "c_length"; // long 
		public static final String C_CONTENT_TYPE = "c_type"; // String
		public static final String C_ETAG = "etag"; // String
		public static final String C_EXPIRES_STR = "expires_str"; // String
		public static final String C_EXPIRES = "expires"; // long
		public static final String C_LOCAL_PATH = "path"; // String
		public static final String C_CONTENT_ENCODING = "encoding"; // String
		public static final String C_CHARSET = "charset"; // String
		public static final String C_MIME = "mime"; // String 
		public static final String C_TYPE = "type"; // int 
		public static final String C_DATA = "data"; // String 保留字段
	}
	
}
