package com.netease.common.http;

import java.io.IOException;
import java.util.List;

public interface HttpCallBack {

	/********************************************************************/

	public Object onPreError(THttpRequest request, int errCode, Object data) throws IOException;
	
	public Object onPreReceived(THttpRequest request, int code, THttpResponse response) throws IOException;
	
	/********************************************************************/
	
	/********************************************************************/
	
	public boolean onError(THttpRequest request, int errCode, Object data);

	public boolean onReceived(THttpRequest request, int code, Object data);

	public boolean onError(List<THttpRequest> requests, int errCode, Object data);

	public boolean onReceived(List<THttpRequest> requests, int code, Object data);
	
	/********************************************************************/
}
