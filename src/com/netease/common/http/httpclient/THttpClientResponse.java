package com.netease.common.http.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;

import com.netease.common.http.THttpResponse;

/**
 * 
 * 在Response被close的时候，对HttpRequestBase进行abort
 */
public class THttpClientResponse implements THttpResponse {

	HttpRequestBase mHttpRequest;
	HttpResponse mHttpResponse;
	HttpClient mHttpClient;
	
	public THttpClientResponse(HttpClient httpClient,
			HttpRequestBase httpRequest, HttpResponse httpResponse) {
		mHttpClient = httpClient;
		mHttpRequest = httpRequest;
		mHttpResponse = httpResponse;
	}
	
	@Override
	public long getContentLength() {
		long contentLength = -1;
		Header header = mHttpResponse.getFirstHeader(THttp.CONTENT_LENGTH);
		if (header != null) {
			try {
				contentLength = Long.parseLong(header.getValue());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return contentLength; 
	}
	
	@Override
	public String getContentType() {
		return getFirstHeader("Content-Type");
	}

	@Override
	public int getResponseCode() throws IOException {
		return mHttpResponse.getStatusLine().getStatusCode();
	}
	
	@Override
	public String getFirstHeader(String key) {
		String value = null;
		Header header = mHttpResponse.getFirstHeader(key);
		if (header != null)
			value = header.getValue();
		
		return value;
	}

	@Override
	public InputStream getResponseStream() throws IOException {
		InputStream inputStream = mHttpResponse.getEntity().getContent();
		if (inputStream != null 
				&& "gzip".equalsIgnoreCase(getFirstHeader(THttp.CONTENT_ENCODING))) {
			inputStream = new GZIPInputStream(inputStream);
		}
		
		return inputStream;
	}

	@Override
	public void close() {
		mHttpResponse = null;
		try {
			if (mHttpRequest != null) {
				mHttpRequest.abort();
			}
			mHttpRequest = null;
			
			if (mHttpClient != null) {
				mHttpClient.getConnectionManager().shutdown();
				mHttpClient = null;
			}
		} catch (Exception e) {
		}
	}

}
