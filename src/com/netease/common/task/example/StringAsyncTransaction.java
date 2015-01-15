package com.netease.common.task.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.netease.common.http.CancelException;
import com.netease.common.http.THttpRequest;
import com.netease.common.http.THttpResponse;
import com.netease.common.http.cache.HttpCache;
import com.netease.common.service.BaseService;
import com.netease.common.task.AsyncTransaction;
import com.netease.common.task.NotifyTransaction;

/**
 * 
 * @author dingding
 *
 */
public abstract class StringAsyncTransaction extends AsyncTransaction {

	protected StringAsyncTransaction(int type) {
		super(type);
	}
	
	protected static String readString(Object request, InputStream in, 
			String charset) throws IOException{
		BufferedReader reader = null;
		char[] data = null;
		
		try {
			THttpRequest tRequest = null;
			if (request != null && request instanceof THttpRequest) {
				tRequest = (THttpRequest) request;
			} else {
				tRequest = new THttpRequest("http");
			}
			
			if (charset != null) {
				charset = charset.toLowerCase();
                if ("utf-8".equals(charset)) {
                	charset = null;
                }
			}
			
			if (charset != null) {
				reader = new BufferedReader(new InputStreamReader(in, charset));
			} else {
				reader = new BufferedReader(new InputStreamReader(in));
			}
			
			StringBuffer buffer = new StringBuffer();
			int length = 0;
			
			data = BaseService.getCharBuf(2048);
			
			while (( length = reader.read(data)) != -1) {
				buffer.append(data, 0, length);
				
				if (tRequest.isCancel()) {
					throw new CancelException();
				}
			}
			
			return buffer.toString();
		} finally {
			BaseService.returnCharBuf(data);
			
			if (reader != null) {
				reader.close();
			}
		}
	}
	
	@Override
	public Object onDataChannelPreNotify(Object request, Object data, 
			int notifyType, int code) throws IOException {
//		if (notifyType == NotifyTransaction.NOTIFY_TYPE_SUCCESS) {
		if (data != null) {
			boolean readString = true;
			if (request != null && request instanceof THttpRequest) {
				THttpRequest tRequest = (THttpRequest) request;
				readString = ! (tRequest.isCacheDatabase() || tRequest.isCacheFile());
			}
			
			if (readString && data instanceof THttpResponse) {
				THttpResponse response = (THttpResponse) data;
				String charset = null;
				
				String ContentType = response.getFirstHeader("Content-Type");
				if (ContentType != null) {
					int i = ContentType.indexOf("=");
		            if (i > 0) {
		                charset = ContentType.substring(i + 1);
		            }
				}
				
				return readString(request, ((THttpResponse) data).getResponseStream(), charset);
				
			} else if (data instanceof HttpCache) {
//				String charset = ((HttpCache) data).Charset;
//				HttpCache cache = (HttpCache) data;
//				String charset = cache.Charset;
//				
//				InputStream in = cache.LocalFile.openInputStream();
//				return readString(request, in, null);
			}
		}
//		}
			
		return super.onDataChannelPreNotify(request, data, notifyType, code);
	}
	
	@Override
	public NotifyTransaction createNotifyTransaction(
			List<AsyncTransaction> trans, Object data, int notifyType, int code) {
		return new StringNotifyTransaction(trans, data, notifyType, code);
	}
	
	@Override
	public NotifyTransaction createNotifyTransaction(Object data,
			int notifyType, int code) {
		return new StringNotifyTransaction(this, data, notifyType, code);
	}
}
