package com.netease.util;

import org.apache.http.NameValuePair;

/**
 * 一般的key value
 * 
 * @author dingding
 *
 */
public class KeyValuePair implements NameValuePair, Comparable<KeyValuePair> {
	// 控制是否检查Compare过程中对象null值或者key的null值
	private static final boolean CHECK_NULL = false;
	
	private String mKey;
	private String mValue;
	
	public KeyValuePair(String key) {
		mKey = key;
	}
	
	public KeyValuePair(String key, String value) {
		mKey = key;
		mValue = value;
	}
	
	public void setValue(String value) {
		mValue = value;
	}
	
	public String getKey() {
		return mKey;
	}
	
	@Override
	public String getName() {
		return mKey;
	}
	
	@Override
	public String getValue() {
		return mValue;
	}
	
	@Override
	public int compareTo(KeyValuePair another) {
		int ret = 0;
		
		if (CHECK_NULL && mKey == null) {
			ret = 1;
		}
		else if (CHECK_NULL && (another == null || another.mKey == null)) {
			ret = -1;
		}
		else {
			ret = mKey.compareTo(another.mKey);
		}
		return ret;
	}

}
