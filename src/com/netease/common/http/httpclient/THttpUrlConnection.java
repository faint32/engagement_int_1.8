package com.netease.common.http.httpclient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

import android.os.Build;

import com.netease.common.http.THttpHeader;
import com.netease.common.http.THttpRequest;
import com.netease.common.http.THttpResponse;

/**
 * 
 * 
 * 
 * http://developer.android.com/reference/java/net/HttpURLConnection.html
 * 
 */
public class THttpUrlConnection implements THttp {

	static {
		disableConnectionReuseIfNecessary();
	}
	
	private boolean mIsClosed;
	
	@Override
	public THttpResponse executeRequest(THttpRequest request) throws IOException {
		if (mIsClosed) {
			return null;
		}
		
		URL url = null;  
        try {  
            url = new URL(request.getRequestUrl());  
        } catch (MalformedURLException e) {  
            e.printStackTrace();  
        }
        
		HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
		httpConnection.setRequestMethod(request.getMethodName());
		
		List<THttpHeader> headers = request.getRequestHeaders();
		for (THttpHeader header : headers) {
			httpConnection.setRequestProperty(header.getKey(), 
					header.getValue());
		}
		
		if (request.isDivided()) {
			httpConnection.setRequestProperty("Range", "bytes=" + request.getRangeStart() 
					+ "-" + request.getRangeEnd());
		}
		
		switch (request.getMethod()) {
		case DELETE:
		case POST:
		case PUT:
		case OPTIONS:
		case HEAD:
			HttpEntity entity = request.getHttpEntity();
			if (entity != null) {
				httpConnection.setDoOutput(true);
				httpConnection.setRequestProperty("Content-Length", 
						String.valueOf(entity.getContentLength()));
				Header header = entity.getContentType();
				if (header != null) {
					httpConnection.setRequestProperty(header.getName(), 
							header.getValue());
				}
				
				OutputStream out = httpConnection.getOutputStream();
				entity.writeTo(out);
				out.flush();
				out.close();
			} else {
				httpConnection.setRequestProperty("Content-Length", "0");
			}
			break;
		}
		
		return new THttpConnectionResponse(httpConnection);
	}
	
	/**
	 * Avoiding Bugs In Earlier Releases
	 * 
	 */
	private static void disableConnectionReuseIfNecessary() {
		try {
			if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
				System.setProperty("http.keepAlive", "false");
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		mIsClosed = true;
	}
	
}
