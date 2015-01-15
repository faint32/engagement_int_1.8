package com.netease.common.http.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.zip.GZIPInputStream;

import com.netease.common.http.THttpResponse;

public class THttpConnectionResponse implements THttpResponse {

	HttpURLConnection mHttpConnection;
	
	public THttpConnectionResponse(HttpURLConnection connection) {
		mHttpConnection = connection;
	}
	
	@Override
	public long getContentLength() {
		return mHttpConnection.getContentLength();
	}
	
	@Override
	public String getContentType() {
		return mHttpConnection.getContentType();
	}

	@Override
	public int getResponseCode() throws IOException {
		return mHttpConnection.getResponseCode();
	}
	
	@Override
	public String getFirstHeader(String key) {
		return mHttpConnection.getHeaderField(key);
	}

	@Override
	public InputStream getResponseStream() throws IOException {
		InputStream inputStream = null;
		if (getResponseCode() == 200) {
			inputStream = mHttpConnection.getInputStream();
		} else {
			inputStream = mHttpConnection.getErrorStream();
		}

		if (inputStream != null
				&& "gzip".equalsIgnoreCase(getFirstHeader(THttp.CONTENT_ENCODING))) {
			inputStream = new GZIPInputStream(inputStream);
		}

		return inputStream;
	}

	@Override
	public void close() {
		if (mHttpConnection != null) {
			mHttpConnection.disconnect();
		}
		
		mHttpConnection = null;
	}

}
