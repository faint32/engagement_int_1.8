package com.netease.common.http.httpclient;

import java.util.List;

import org.apache.http.cookie.Cookie;

public class THttpFactory {
	
	public static THttp createHttp() {
		return new THttpClient();
	}

	public static String getHttpCookie(String url) {
		return THttpClient.getHttpCookie(url);
	}
	
	public static List<Cookie> getHttpCookies(String url) {
		return THttpClient.getHttpCookies(url);
	}
	
}
