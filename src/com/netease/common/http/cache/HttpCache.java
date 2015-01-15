package com.netease.common.http.cache;

import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.provider.BaseColumns;

import com.netease.common.cache.CacheManager;
import com.netease.common.cache.file.StoreFile;
import com.netease.common.http.cache.db.HttpCacheDBTables.CacheTable;

/**
 * Http Cache类，用于部分Http向事务回调的数据结构
 *
 */
public class HttpCache {
	
	public static final int TYPE_VALID = 1;
	public static final int TYPE_INVALID = 2;
	public static final int TYPE_MODIFY = 3;
	
	// SQLite数据库中的id值
	public long _ID = -1;
	
	// Cache Url, Http全路径 
	public String Url;
	
	// Http ETag属性内容，“ETag”，形如"3f9f640-318-cb9f8380"
	public String ETag;
	
	// 最近更新时间，“Last-Modified”，形如Fri, 26 Jan 2007 01:53:34 GMT
	public String LastModefy;
	
	// Http Content内容长度，chunk数据采用最后下载得到的数据长度，“Content-Length”
	public long ContentLength;
	
	// 存在时间，允许客户端在这个时间之前不去检查（发请求），“Expires”，
	// 形如Thu, 01 Dec 1994 16:00:00 GMT
	public String ExpiresString;

	// Expires 表示存在时间，允许客户端在这个时间之前不去检查（发请求），等同max-age的
	// 效果。但是如果同时存在，则被Cache-Control的max-age覆盖。
	public long Expires;
	
	// 缓存的本地文件
	public StoreFile LocalFile;
	
	// 字符的编码，utf-8，gbk等，“Content-Type”中截取，形如utf-8（text/html;charset=UTF-8）
	public String Charset;
	
	// 数据压缩格式，“Content-Encoding”，形如gzip
	public String ContentEncoding;
	
	// 数据类型信息，image/jpg，text/html等，“Content-Type”中截取，形如text/html（text/html;charset=UTF-8）
	public String MimeType;
	
	/**
	 * 是否是文件缓存
	 */
	private boolean mFileCache;
	
	private static final String JK_ETAG = "etag";
	private static final String JK_LASTMODEFY = "lm";
	private static final String JK_CONTENTLENGTH = "cl";
	private static final String JK_EXPIRESSTRING = "eps";
	private static final String JK_EXPIRES = "ep";
	private static final String JK_LOCALFILE = "path";
	private static final String JK_CHARSET = "cs";
	private static final String JK_CONTENTENCODING = "ce";
	private static final String JK_MIMETYPE = "mime";
	
	public HttpCache() {
		
	}
	
	public static String[] Projection = new String[] {
		BaseColumns._ID,
		CacheTable.C_LASTMODIFY,
		CacheTable.C_CONTENT_LENGTH,
		CacheTable.C_ETAG,
		CacheTable.C_EXPIRES_STR,
		CacheTable.C_EXPIRES,
		CacheTable.C_LOCAL_PATH,
		CacheTable.C_CONTENT_ENCODING,
		CacheTable.C_CHARSET,
		CacheTable.C_MIME,
	};
	
	private static final int DB_ID = 0;
	private static final int DB_LAST_MODIFY = 1;
	private static final int DB_CONTENT_LENGTH = 2;
	private static final int DB_ETAG = 3;
	private static final int DB_EXPIRES_STRING = 4;
	private static final int DB_EXPIRES = 5;
	private static final int DB_LOCAL_PATH = 6;
	private static final int DB_CONTENT_ENCODING = 7;
	private static final int DB_CHARSET = 8;
	private static final int DB_MIME = 9;
	
	public HttpCache(String url, Cursor cursor) {
		Url = url;
		
		_ID = cursor.getInt(0);
		
		LastModefy = cursor.getString(DB_LAST_MODIFY);
		ContentLength = cursor.getLong(DB_CONTENT_LENGTH);
		ETag = cursor.getString(DB_ETAG);
		ExpiresString = cursor.getString(DB_EXPIRES_STRING);
		Expires = cursor.getLong(DB_EXPIRES);
		LocalFile = CacheManager.parseStoreFile(cursor.getString(DB_LOCAL_PATH));
		ContentEncoding = cursor.getString(DB_CONTENT_ENCODING);
		Charset = cursor.getString(DB_CHARSET);
		MimeType = cursor.getString(DB_MIME);
	}
	
	public HttpCache(String url, StoreFile storeFile) {
		Url = url;
		LocalFile = storeFile;
		setFileCache();
	}
	
	public HttpCache(String url, JSONObject json) {
		Url = url;
		ETag = json.optString(JK_ETAG);
		LastModefy = json.optString(JK_LASTMODEFY);
		ContentLength = json.optLong(JK_CONTENTLENGTH);
		ExpiresString = json.optString(JK_EXPIRESSTRING);
		Expires = json.optLong(JK_EXPIRES);
		LocalFile = CacheManager.parseStoreFile(json.optString(JK_LOCALFILE));
		Charset = json.optString(JK_CHARSET);
		ContentEncoding = json.optString(JK_CONTENTENCODING);
		MimeType = json.optString(JK_MIMETYPE);
	}
	
	public void setFileCache() {
		mFileCache = true;
	}
	
	/**
	 * 
	 * @return the type
	 */
	public int getType() {
		int type = TYPE_INVALID;
		
		if (LocalFile == null || ! LocalFile.exists()) {
			type = TYPE_INVALID;
		} else if (mFileCache) {
			type = TYPE_VALID;
		} else if (Expires >= System.currentTimeMillis()) {
			type = TYPE_VALID;
		} else if (LastModefy != null && LastModefy.length() > 0){
			type = TYPE_MODIFY;
		}
		
		return type;
	}
	
	public String toJSONString() {
		JSONObject json = new JSONObject();
		
		try {
			if (ETag != null) {
				json.put(JK_ETAG, ETag);
			}
			if (LastModefy != null) {
				json.put(JK_LASTMODEFY, LastModefy);
			}
			if (ContentLength > 0) {
				json.put(JK_CONTENTLENGTH, ContentLength);
			}
			if (ExpiresString != null) {
				json.put(JK_EXPIRESSTRING, ExpiresString);
			}
			if (Expires > 0) {
				json.put(JK_EXPIRES, Expires);
			}
			if (LocalFile != null) {
				json.put(JK_LOCALFILE, LocalFile.getPath());
			}
			if (Charset != null) {
				json.put(JK_CHARSET, Charset);
			}
			if (ContentEncoding != null) {
				json.put(JK_CONTENTENCODING, ContentEncoding);
			}
			if (MimeType != null) {
				json.put(JK_MIMETYPE, MimeType);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json.toString();
	}

	public void setExpires(long expires) {
		Expires = expires;
	}
	
	
}
