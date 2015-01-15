package com.netease.common.http.httpclient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.text.TextUtils;

import com.netease.common.http.ApnReference;
import com.netease.common.http.THttpHeader;
import com.netease.common.http.THttpMethod;
import com.netease.common.http.THttpRequest;
import com.netease.common.http.THttpResponse;
import com.netease.common.http.ApnReference.ApnWrapper;
import com.netease.common.service.BaseService;
import com.netease.util.Util;

public class THttpClient implements THttp {

	static SchemeRegistry mSchReg;
	
	static { // 静态变量初始化
		mSchReg = new SchemeRegistry();
		mSchReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		mSchReg.register(new Scheme("https", TrustAllSSLSocketFactory.getDefault(), 443));
	}
	
//	Map<Integer, HttpClient> mClients;
//	THttpRequest mRequest;
	
	boolean mIsClosed;
	static CookieStore mCookieStore = new BasicCookieStore();
	
	private HttpClient buildHttpClient() {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, 20 * 1000);
		HttpConnectionParams.setSoTimeout(params, 60 * 1000);
		HttpConnectionParams.setSocketBufferSize(params, 8 * 1024);
		HttpProtocolParams.setUseExpectContinue(params, false);

		ClientConnectionManager conMgr = new ThreadSafeClientConnManager(
				params, mSchReg);
		DefaultHttpClient httpClient = new DefaultHttpClient(conMgr, params);

		httpClient.getParams()
				.setParameter(ClientPNames.HANDLE_REDIRECTS, false);
		
		httpClient.setCookieStore(mCookieStore);

//		 TODO: Http 代理
		try {
			ApnWrapper aw = ApnReference.getInstance(
					BaseService.getServiceContext()).getCurrApn();

			if (aw != null && !Util.isStringEmpty(aw.proxy)) {
				// used to access cmwap
				HttpHost proxy = new HttpHost(aw.proxy, aw.port);
				httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
						proxy);
			}
		} catch (Exception e) {
		}
		
		return httpClient;
	}
	
	HttpParams createParams(THttpRequest request) {
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		return params;
	}
	
	
	HttpRequestBase createHttpRequest(String url, THttpRequest request) {
		HttpRequestBase requestBase = null;
		if (request.getMethod() == THttpMethod.GET) {
			requestBase = new HttpGet();
		} else if (request.getMethod() == THttpMethod.HEAD) {
			requestBase = new HttpHead();
		} else if (request.getMethod() == THttpMethod.POST) {
			HttpPost post = new HttpPost();
			
			HttpEntity entity = request.getHttpEntity();
			if (entity != null) {
				post.setEntity(entity);
			}
			requestBase = post;
		} else if (request.getMethod() == THttpMethod.PUT) {
			HttpPut put = new HttpPut();
			HttpEntity entity = request.getHttpEntity();
			
			if (entity != null) {
				put.setEntity(entity);
			}
			requestBase = put;
		} else if (request.getMethod() == THttpMethod.OPTIONS) {
			requestBase = new HttpOptions();
		} else if (request.getMethod() == THttpMethod.DELETE) {
			requestBase = new HttpDelete();
		} else {
			Assert.assertTrue(false);
		}

		
		URI uri = URI.create(url);
		requestBase.setURI(uri);

		//TODO  create new field to save param .not entity
		requestBase.setParams(createParams(request));
		
		List<THttpHeader> list = request.getRequestHeaders();
		if (list != null) {
			for (THttpHeader header : list) {
				requestBase.addHeader(header.getKey(), header.getValue());
			}
		}
		
		if (request.isDivided()) {
			requestBase.addHeader("Range", "bytes=" + request.getRangeStart() 
					+ "-" + request.getRangeEnd());
		}
		
//		if (request.isCookieConcerned()) {
//			String host = uri.getHost();
//			String cookie = mDomainCookies.get(host);
//
//			if (cookie != null) {
//				requestBase.addHeader("Cookie", cookie);
//			}
//		}
		
		return requestBase;
	}
	
	@Override
	public THttpResponse executeRequest(THttpRequest request) throws IOException {
		
		THttpClientResponse response = null;
		
		if (! mIsClosed) {
			HttpClient httpClient = buildHttpClient();
			
			try {
				HttpRequestBase httpRequest = createHttpRequest(
						request.getRequestUrl(), request);
				HttpResponse httpResponse = httpClient.execute(httpRequest);
				int responseCode = httpResponse.getStatusLine().getStatusCode();
				int maxCount = 3;

				while (responseCode == 302 && --maxCount >= 0) {
					Header header = httpResponse.getLastHeader("Location");
					String location = null;
					if (header != null) {
						location = header.getValue();
					}

					if (!TextUtils.isEmpty(location)) {
						location = request.onRedirectUrl(location, request);

						if (!TextUtils.isEmpty(location)) {
							if (!request.isCancel()) {
								httpRequest = createHttpRequest(location,
										request);
								httpResponse = httpClient.execute(httpRequest);

								responseCode = httpResponse.getStatusLine()
										.getStatusCode();
								continue;
							}
						}
					}

					break;
				}

				response = new THttpClientResponse(httpClient, httpRequest,
						httpResponse);
			} catch (Exception e) {
				closeHttpClient(httpClient);
				
				// e.printStackTrace();
				throw new IOException(e.fillInStackTrace());
			}
		}
		
		return response;
	}
	
	/**
	 * close http client
	 * 
	 * @param httpClient
	 */
	private void closeHttpClient(HttpClient httpClient) {
		try {
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void close() {
		mIsClosed = true;
	}
	
	/**
	 * 获取HttpCookie s
	 * 
	 * @param url
	 * @return
	 */
	public static List<Cookie> getHttpCookies(String url) {
		List<Cookie> list = null;
		
		if (!TextUtils.isEmpty(url)) {
			list = new LinkedList<Cookie>();
			
			try {
				URI uri = new URI(url);
				String domain = uri.getHost();
				domain = domain.startsWith("www.") ? domain.substring(4) : domain;
				    
				List<Cookie> cookies = mCookieStore.getCookies();
				if (cookies != null) {
					for (Cookie c : cookies) {
					    String cd = c.getDomain();
					    if (! TextUtils.isEmpty(cd)) {
					        if (cd.charAt(0) == '.') {
    					        if (domain.endsWith(cd)) {
    					        	list.add(c);
    					        }
					        }
					        else {
					            if (domain.equals(cd)) {
					            	list.add(c);
                                }
					        }
					    }
					}
				}
			} catch (URISyntaxException e) {
			}
		}
		
		return list;
	}
	
	/**
	 * 获取HttpCookie
	 * 
	 * @param url
	 * @return
	 */
	public static String getHttpCookie(String url) {
		String cookie = null;
		if (!TextUtils.isEmpty(url)) {
			try {
				URI uri = new URI(url);
				String domain = uri.getHost();
				domain = domain.startsWith("www.") ? domain.substring(4) : domain;
				    
				List<Cookie> cookies = mCookieStore.getCookies();
				if (cookies != null) {
					StringBuffer buffer = new StringBuffer();
					for (Cookie c : cookies) {
//						if (c.getVersion() > 0) {
//							buffer.append("$Version=").append(c.getVersion()).append("; ");
//						}
					    String cd = c.getDomain();
					    if (! TextUtils.isEmpty(cd)) {
					        if (cd.charAt(0) == '.') {
    					        if (domain.endsWith(cd)) {
    					            buffer.append(c.getName() + "=" + c.getValue() + "&");
    					        }
					        }
					        else {
					            if (domain.equals(cd)) {
                                    buffer.append(c.getName() + "=" + c.getValue() + "&");
                                }
					        }
					    }
//						if (! TextUtils.isEmpty(c.getPath())) {
//							buffer.append("$Path").append("=\"").append(c.getPath()).append("\"; ");
//						}
					}
					
					if (buffer.length() > 0) {
//						buffer.deleteCharAt(buffer.length() - 1);
//						buffer.deleteCharAt(buffer.length() - 1);
						cookie = buffer.toString();
					}
				}
			} catch (URISyntaxException e) {
			}
		}
		return cookie;
	}
}
