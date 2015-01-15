package com.netease.common.http.httpclient;

import java.io.IOException;

import com.netease.common.http.THttpRequest;
import com.netease.common.http.THttpResponse;

/**
 * 对HttpClient和HttpUrlConnection两种连接请求方式进行兼容，通过THttpFactory进行
 * 切换
 * 
 * @see THttpFactory
 * 
 * @author dingding
 *
 */
public interface THttp {

	public static final String LOCAL_PARAM_TAG = "%local";
	
	public static final String ACCEPT_ENCODING = "Accept-Encoding";
	public static final String CONTENT_ENCODING = "Content-Encoding";
	public static final String CONTENT_LENGTH = "Content-Length";
	public static final String GZIP_DEFLATE  = "gzip, deflate";
	
	/**
	 * 执行request请求
	 * 
	 * @param request
	 * @return
	 */
	public THttpResponse executeRequest(THttpRequest request) throws IOException;
	
	/**
	 * 关闭释放资源
	 */
	public void close();
	
}
