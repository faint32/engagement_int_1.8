package com.netease.common.http;

import java.io.ByteArrayInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.cookie.Cookie;

import android.graphics.Bitmap;
import android.util.Log;

import com.netease.common.cache.CacheManager;
import com.netease.common.cache.file.FileCreateException;
import com.netease.common.cache.file.StoreFile;
import com.netease.common.cache.file.StoreTmpFileInfo;
import com.netease.common.config.IConfig;
import com.netease.common.http.cache.HttpCache;
import com.netease.common.http.cache.HttpCacheManager;
import com.netease.common.http.httpclient.THttp;
import com.netease.common.http.httpclient.THttpFactory;
import com.netease.common.image.util.ImageUtil;
import com.netease.common.nio.NioInputStream;
import com.netease.common.nio.NioListener;
import com.netease.common.service.BaseService;
import com.netease.common.task.TransTypeCode;
import com.netease.util.ByteArrayPool;
import com.netease.util.PlatformUtil;
import com.netease.util.priority.Priority;

/**
 * 
 *
 */
public class HttpEngine implements IConfig {
	
	/************************以下 IConfig 配置项*******************************/
	
	// 多线程断点区块切割大小
	public static int DivideSize = 2 << 20; // 2MB
	
	// 多线程成立条件
	public static int DivideCondition = DivideSize << 1;
	
	// 最小断点续传大小，用来加速判断，一般建议1MB
	public static int MinDivideSize = 1 << 20; // 1MB
	
	// 是否判断AcceptRange来断点续传
	public static boolean CheckAcceptRange = true; 
	
	/************************以上 IConfig 配置项*******************************/

	private static final int MAX_TRY_TIME = 25 * 1000;
	
	private static final boolean DEBUG = false;
	private static final String TAG = "HttpEngine";
	
	private static final int BUFFER_SIZE = 1024 << 2;
	
	private static HttpEngine mInstance;
	
	/**
	 * 缺省3个线程方式的HttpEngine
	 * @return
	 */
	public static HttpEngine Instance() {
		if (mInstance == null) {
			mInstance = new HttpEngine(3, Thread.NORM_PRIORITY - 1);
		}
		
		return mInstance;
	}
	
	private static Pattern ContentPattern = Pattern.compile(
			"([^\\s;]+)\\s*(;\\s*(?i)charset=(\\S+))?");
	
	boolean mIsClosed;
	LinkedList<HttpThread> mHttpThreads;
	HttpThread mEmergencyThread;
	PriorityBlockingQueue<THttpRequest> mRequestQueue;

	Map<String, List<THttpRequest>> mRunningRequests; 
	
	THttp mHttp;
	
	ByteArrayPool mBytePool;
	
	private synchronized THttp getHttp() {
		if (mHttp == null) {
			mHttp = THttpFactory.createHttp();
		}
		return mHttp;
	}
	
	private synchronized void closeHttp() {
		if (mHttp != null) {
			mHttp.close();
		}
		mHttp = null;
	}
	
	/**
	 * 
	 * @param request
	 * @return
	 */
	private boolean addRunWaitingQueue(THttpRequest request) {
		boolean ret = false;
		
		if (request.isCacheFile()) {
			synchronized (mRunningRequests) {
				List<THttpRequest> list = mRunningRequests.get(request.getCacheUrl());
				if (list != null) {
					list.add(request);
					ret = true;
				} else {
					list = new LinkedList<THttpRequest>();
					list.add(request);
					mRunningRequests.put(request.getCacheUrl(), list);
				}
			}
		}
		
		return ret;
	}
	
	/**
	 * 
	 * @param request
	 * @return
	 */
	private List<THttpRequest> removeRunWaitingQueue(THttpRequest request) {
		List<THttpRequest> list = null; 
		if (request.isCacheFile()) {
			synchronized (mRunningRequests) {
				list = mRunningRequests.remove(request.getCacheUrl());
			}
		}
		
		return list;
	}
	
	public HttpEngine(int httpThread, int threadPriority) {
		mRunningRequests = new HashMap<String, List<THttpRequest>>();
		mRequestQueue = new PriorityBlockingQueue<THttpRequest>();
		mHttpThreads = new LinkedList<HttpThread>();
		
		mBytePool = new ByteArrayPool(8 * 1024);
		
		for (int i = 0; i < httpThread; i++) {
			HttpThread thread = new HttpThread();
			thread.setPriority(threadPriority);
			thread.start();
			
			mHttpThreads.add(thread);
		}
	}
	
	/**
	 * 获取http Cookie
	 * @param url
	 * @return
	 */
	public static String getHttpCookie(String url) {
		return THttpFactory.getHttpCookie(url);
	}
	
	/**
	 * 获取http Cookie
	 * @param url
	 * @return
	 */
	public static List<Cookie> getHttpCookies(String url) {
		return THttpFactory.getHttpCookies(url);
	}
	
	private class HttpThread extends Thread {
		
		@Override
		public void run() {
			while (! mIsClosed) {
				try {
					THttpRequest request = mRequestQueue.take();
					execute(request);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		private void storeCacheTmpFragement(THttpRequest request,
				THttpResponse response, long start, long end) throws IOException {
			if (request.isCancel()) {
				throw new CancelException();
			}
			
			StoreTmpFileInfo fileInfo = request.getStoreTmpFileInfo();
			StoreFile storeFile = fileInfo.getStoreFile();
			
			RandomAccessFile file = null;
			try {
				file = storeFile.getRandomAccessFile();
				file.seek(start);
				
				InputStream is = response.getResponseStream();
				
				saveDataOutput(request, file, is, (int) (end - start));
				response.close();
				
				file.close();
				
				fileInfo.removeSuccessFragement(start, end);
				
				THttpRequest next = request.requestNextRequest(DivideSize);
				if (next == null) {
					if (fileInfo.isSuccess() && request.shouldNotify()) {
						if (DEBUG) Log.e(TAG, "shouldNotify: " + true);
							
						storeFile.update(fileInfo);
						
						if (DEBUG) Log.e(TAG, "add request start ");
						
						HttpCache httpCache = request.getHttpCache();
						if (request.isDivided()) {
							HttpCacheManager.addHttpCache(request.getParentRequest(), httpCache);
							
							notifyReceived(request.getParentRequest(), 0, httpCache);
						}
						else {
							HttpCacheManager.addHttpCache(request, httpCache);
							
							notifyReceived(request, 0, httpCache);
						}
					}
					else {
						if (DEBUG) Log.e(TAG, "shouldNotify: " + false);
					}
				}
				else {
					if (DEBUG) Log.e(TAG, "next: " + false);
					
					addRequest(next);
				}
			} catch (IOException e) {
				fileInfo.addFailFragement(request.getRangeStart(), end);
				
				try {
					if (file != null) {
						file.close();
					}
				} catch (Exception e1) { }
				
				throw e;
			}
		}
		
		/**
		 * 检查是否允许使用临时文件进行断点续传
		 * 
		 * @param request
		 * @param response
		 * @param httpCache
		 * @return
		 */
		private boolean checkCacheTmp(THttpRequest request, THttpResponse response,
				HttpCache httpCache) throws IOException {
			boolean ret = false;
			
			long contentLength = response.getContentLength();
			// 最小1MB以上
			if ((request.isCacheFile() || request.isCacheDatabase())
					&& ! request.isDivided() && contentLength > MinDivideSize 
					&& (! CheckAcceptRange || "bytes".equals(response.getFirstHeader("Accept-Ranges")))) {
				
			    StoreTmpFileInfo tmpInfo = null;
			    
			    String lastModify = response.getFirstHeader("Last-Modified");
			    String etag = response.getFirstHeader("ETag");
			    
			    if (httpCache != null && httpCache.LocalFile != null) {
    				lastModify = lastModify == null ? "" : lastModify;
    				etag = etag == null ? "" : etag;
    				
    				tmpInfo = new StoreTmpFileInfo(httpCache.LocalFile);
    				
    				File file = httpCache.LocalFile.getTmpFile();
    				if (file.exists()) {
    					tmpInfo.readInfo();
    					
    					// 既有临时文件
    					String oldLastModify = tmpInfo.getLastModify();
    					String oldEtag = tmpInfo.getETag();
    					
    					oldLastModify = oldLastModify == null ? "" : oldLastModify;
    					oldEtag = oldEtag == null ? "" : oldEtag;
    					
    					// 对比是否有变更
    					if (contentLength == tmpInfo.getLength() 
    							&& etag.equals(oldEtag)
    							&& lastModify.equals(oldLastModify)) {
    						// 追加数据
    						
    					}
    					else {
    						// 新数据
    						tmpInfo.delete();
    						file.delete();
    						
    						tmpInfo.reset(etag, lastModify, contentLength);
    					}
    				}
    				else {
    					tmpInfo.reset(etag, lastModify, contentLength);
    				}
			    }
			    else {
			        tmpInfo = new StoreTmpFileInfo(CacheManager.getStoreFile(
			                request.getCacheUrl()));
			        tmpInfo.reset(etag, lastModify, contentLength);
			    }
				
				if (request.isCacheDatabase()) {
					httpCache = parseHttpCache(request.getCacheUrl(), 
							response, tmpInfo.getStoreFile());
				}
				else if (request.isCacheFile()) {
                    httpCache = new HttpCache(request.getCacheUrl(), 
                            tmpInfo.getStoreFile());
                }
				
				request.setStoreTmpFileInfo(tmpInfo);
				request.setHttpCache(httpCache);
				
				NioListener listener = request.getNioListener();
				if (listener != null) {
					listener.onContentLength(NioListener.TYPE_IN_SIZE, contentLength);
					listener.onSizeIncrease(NioListener.TYPE_IN_SIZE, 
							tmpInfo.getCurrentSize() - tmpInfo.getFailedSize());
				}
				
				// 非移动网络下且contentLength满足切割条件
				if (contentLength > DivideCondition) {
//						&& ! PlatformUtil.isMobileNetWork(BaseService.getServiceContext())) {
					// 进行分段切割
					int size = mHttpThreads.size();
					
					long[] seeks = null;
					// 初始请求的那种直接读取第一段数据
					if (tmpInfo.getCurrentSize() <= 0) {
						seeks = tmpInfo.requestNextFragement(DivideSize);
						size -= 1;
					}
					
					// 同时请求线程个数的request
					for (int i = 0; i < size; i++) {
						addRequest(request.requestNextRequest(DivideSize));
					}
					
					// 初始seek不为null
					if (seeks != null) {
						storeCacheTmpFragement(request, response, seeks[0], seeks[1]);
					}
				}
				else {
					// 在原有基础上进行获取
					if (tmpInfo.getCurrentSize() <= 0) {
						long[] seeks = tmpInfo.requestNextFragement(DivideCondition);
						storeCacheTmpFragement(request, response, seeks[0], seeks[1]);
					}
					else {
						addRequest(request.requestNextRequest(DivideCondition));
					}
				}
				
				ret = true;
			}
			
			return ret;
		}
		
		public void execute(THttpRequest request) {
			if (DEBUG) Log.e(TAG, "execute Request " + request);
			
			if (request == null) {
				return ;
			}

			if (request.isCancel()) {
				notifyError(request, TransTypeCode.ERR_CODE_CANCEL, null);
				return ;
			}
			
			request.doBefore();

			HttpCache httpCache = null;
			
			if (! request.isDivided()) {
				if (addRunWaitingQueue(request)) {
					return;
				}
	
				httpCache = HttpCacheManager.getHttpCache(request);
				
				if (! PlatformUtil.hasConnected(BaseService.getServiceContext())) {
					if (httpCache != null && httpCache.LocalFile != null
							&& httpCache.LocalFile.exists()) {
						notifyReceived(request, TransTypeCode.ERR_CODE_NO_NETWORK, httpCache);
					}
					else {
						notifyError(request, TransTypeCode.ERR_CODE_NO_NETWORK, null);
					}
					return;
				}
				
				if (httpCache != null) {
					switch (httpCache.getType()) {
					case HttpCache.TYPE_VALID:
						notifyReceived(request, 0, httpCache);
						return;
					case HttpCache.TYPE_MODIFY:
						request.addHeader("If-Modified-Since", httpCache.LastModefy);
						break;
					}
				}
			}

			THttpResponse response = null;
			THttp http = null;

			long time = System.currentTimeMillis();
			
			for (int i = request.getTryTimes(); ; i--) {
				try {
					http = getHttp();
					response = http.executeRequest(request);

					Object ret = null;

					if (response != null) {
						int respCode = response.getResponseCode();
						if (respCode == 304) {
							notifyReceived(request, respCode, httpCache);
							break;
						} 
						else if (respCode == 206) {
							storeCacheTmpFragement(request, response, 
									request.getRangeStart(), request.getRangeEnd());
							break;
						}
						else if (respCode == 200) {
							if (checkCacheTmp(request, response, httpCache)) {
								break;
							}
							else if (request.isDivided()) {
								notifyError(request, 200, null);
								break;
							}
							
							HttpCallBack callback = request.getHttpCallBack();
							if (callback != null) {
								ret = callback.onPreReceived(request, respCode,
										response);
								if (ret != null) {
									notifyReceived(request, respCode, ret);
									httpCache = storeHttpCache(request,
											response, httpCache, ret);
								} else {
									httpCache = storeHttpCache(request,
											response, httpCache, ret);
									notifyReceived(request, respCode, httpCache);
								}
							} else {
								httpCache = storeHttpCache(request, response,
										httpCache, ret);
								notifyReceived(request, respCode, httpCache);
							}
							break;
						} else if (respCode == 401) {
							THttpHeader header = request.getAuthHeader(response);
							if (header != null) {
								request.addHeader(header.getKey(), header.getValue());
								continue ;
							}
						}

						HttpCallBack callback = request.getHttpCallBack();
						if (callback != null) {
							ret = callback.onPreError(request, 
									getNotifyErrCode(respCode), response);
						}
						notifyError(request, getNotifyErrCode(respCode), ret);
						break;
					} else {
						notifyError(request,
								TransTypeCode.ERR_CODE_NETWORK_EXCEPTION, null);
						break;
					}
				} catch (FileCreateException e) {
					if (DEBUG) e.printStackTrace();
					notifyError(request,
							TransTypeCode.ERR_CODE_FILE_CREATE_EXCEPTION,
							e.getMessage());
					break;
				} catch (CancelException e) {
					notifyError(request,
							TransTypeCode.ERR_CODE_CANCEL,
							null);
					break;
				} catch (IOException e) {
					if (DEBUG) e.printStackTrace();
					closeHttp();
					
					if (i < 0) {
						notifyError(request,
								TransTypeCode.ERR_CODE_NETWORK_IOEXCEPTION,
								e.getMessage());
						break;
					}
				} catch (Exception e) {
					if (DEBUG) e.printStackTrace();
					if (i < 0) {
						notifyError(request,
								TransTypeCode.ERR_CODE_NETWORK_EXCEPTION,
								e.getMessage());
						break;
					}
				} finally {
					if (response != null) {
						response.close();
					}
				}

				request.incTryFailed();
				
				if (System.currentTimeMillis() - time > MAX_TRY_TIME) {
					notifyError(request,
							TransTypeCode.ERR_CODE_NETWORK_EXCEPTION,
							null);
					break;
				}
			}
		}
	}
	
	/**
	 * 有需要紧急处理事务的情况下，创建紧急HttpThread进行处理HttpRequest 
	 */
	private class EmergencyHttpThread extends HttpThread {
		
		@Override
		public void run() {
			if (! mIsClosed) {
				THttpRequest request = mRequestQueue.poll();
				execute(request);
			}
			
			mEmergencyThread = null;
		}
	}
	

	private void checkStartEmergencyHttpThread(int priority) {
		if ((priority & 0xFF) == Priority.EMERGENCY
				&& mEmergencyThread == null) {
			if (DEBUG) Log.d(TAG, "start emergency http thread");
			
			mEmergencyThread = new EmergencyHttpThread();
			mEmergencyThread.start();
		}
	}
	
	public void addRequest(THttpRequest httpRequest) {
		if (DEBUG) Log.e(TAG, "addRequest " + httpRequest);
		
		if (! mIsClosed && httpRequest != null) {
			mRequestQueue.offer(httpRequest);
			
			checkStartEmergencyHttpThread(httpRequest.getPriority());
		}
	}

	public void adjustPriorityByGID(int gid, int priority) {
		if (! mIsClosed) {
			boolean find = false;
			Iterator<THttpRequest> requests = mRequestQueue.iterator();
			while (requests.hasNext()) {
				THttpRequest request = requests.next();
				if (request.getGroupID() == gid) {
					request.setPriority(priority);
					find = true;
				}
			}
			
			if (find) {
				checkStartEmergencyHttpThread(priority);
			}
		}
	}

	public void shutdown() {
		mIsClosed = true;
		closeHttp();
		
		if (mEmergencyThread != null) {
			mEmergencyThread.interrupt();
			mEmergencyThread = null;
		}
		
		if (mHttpThreads != null) {
			for (HttpThread httpThread : mHttpThreads) {
				httpThread.interrupt();
			}
			mHttpThreads.clear();
			mHttpThreads = null;
		}
	}
	
	private void writeDataOutputCheckException(DataOutput output, 
			byte[] data, int off, int length) throws FileCreateException {
		try {
			output.write(data, off, length);
		} catch (Exception e) {
			throw new FileCreateException();
		}
	}
	
	private void saveDataOutput(THttpRequest request, DataOutput output,
			InputStream input, int preferLength) throws IOException {
		NioListener listener = request.getNioListener();
		byte[] data = mBytePool.getBuf(BUFFER_SIZE);
		
		if (listener != null) {
			NioInputStream nis = new NioInputStream(input);
			nis.setNioListener(listener);
			input = nis; 
		}
		
		int numread = 0;
		int count = 0;
		int offset = 0;
		int writeCount = 0;
		
		try {
			if (preferLength > 0) {
				while (count < preferLength) {
					numread = input.read(data, offset, Math.min(data.length - offset, preferLength - count));
					offset += numread;
					
					if (numread == -1)
					{ // 数据长度 与size 大小不匹配.
						throw new IOException("http readData from stream num mismatch: prefer: " + preferLength + " count: " + count);
					} else if (offset == BUFFER_SIZE) {
						writeDataOutputCheckException(output, data, 0, BUFFER_SIZE);
						
						offset = 0;
						count += numread;
						
						writeCount += BUFFER_SIZE;
					} else {
						count += numread;
					}
					
					if (request.isCancel()) {
						throw new CancelException();
					}
				}
				
				if (offset > 0) {
					writeDataOutputCheckException(output, data, 0, offset);
					writeCount += offset;
				}
			} else {
				long timeout = System.currentTimeMillis();
				
				while (true) {
					numread = input.read(data, offset, data.length - offset);
	
					long t = System.currentTimeMillis();
					if (numread < 0) {
						break;
					} else if (numread == 0) {
						if (t - timeout > 5000) {
							break;
						}
					} else {
						count += numread;
						offset += numread;
						
						if (offset == BUFFER_SIZE) {
							writeDataOutputCheckException(output, data, 0, BUFFER_SIZE);
							offset = 0;
							
							writeCount += BUFFER_SIZE;
						}
						timeout = t;
					}
					
					if (request.isCancel()) {
						throw new CancelException();
					}
				}
				
				if (offset > 0) {
					writeDataOutputCheckException(output, data, 0, offset);
					writeCount += offset;
				}
			}
		}
		finally {
			request.addRangeStart(writeCount);
			
			mBytePool.returnBuf(data);
		}
	}
	
	private void saveStoreFile(THttpRequest request, StoreFile storeFile, 
			InputStream input, int preferLength) throws IOException {
		OutputStream output = storeFile.openOutputStream();
		DataOutput out = new DataOutputStream(output);
		
		NioListener listener = request.getNioListener();
		if (preferLength > 0 && listener != null) {
			listener.onContentLength(NioListener.TYPE_IN_SIZE, preferLength);
		}
		
		try {
			saveDataOutput(request, out, input, preferLength);
			
			output.close();
			storeFile.closeOutputStream();
		} catch (IOException e) {
			if (DEBUG) e.printStackTrace();
			
			if (e instanceof CancelException) {
				output.close();
			}
			else {
				storeFile.closeOutputStream();
				storeFile.delete();
			}
			
			throw e;
		}
	}
	
	private static HttpCache parseHttpCache(String url, THttpResponse response,
			StoreFile storeFile) {
		HttpCache httpCache = new HttpCache();
		httpCache.Url = url;
		httpCache.LastModefy = response.getFirstHeader("Last-Modified");
		httpCache.ETag = response.getFirstHeader("ETag");
		httpCache.ExpiresString = response.getFirstHeader("Expires");
		httpCache.ContentLength = storeFile.length();
		httpCache.ContentEncoding = response.getFirstHeader("Content-Encoding");
		httpCache.LocalFile = storeFile;
		
		String cacheControl = response.getFirstHeader("Cache-Control");
		if (cacheControl != null) {
			String[] controls = cacheControl.toLowerCase().split("[ ,;]");
			for (int i = 0; i < controls.length; i++) {
				if ("no-store".equals(controls[i])) {
					storeFile.delete();
					return null;
				}
				if ("no-cache".equals(controls[i])) {
					httpCache.setExpires(0);
				} 
				else if (controls[i].startsWith("max-age")) {
					int separator = controls[i].indexOf('=');
					if (separator < 0) {
						separator = controls[i].indexOf(':');
					}
					if (separator > 0) {
						String s = controls[i].substring(separator + 1);
						try {
							long sec = Long.parseLong(s);
							if (sec >= 0) {
								httpCache.setExpires(System.currentTimeMillis() 
										+ 1000 * sec);
							}
						} catch (NumberFormatException e) {
							if (DEBUG) e.printStackTrace();
							
							if ("1d".equals(s)) {
								// Take care of the special "1d" case
								httpCache.setExpires(System.currentTimeMillis() 
										+ 86400000); // 24*60*60*1000
							} else {
								httpCache.setExpires(0);
							}
						}
					}
				}
			}
		}
		
		if (httpCache.Expires <= 0 && httpCache.ExpiresString != null) {
			try {
				long time = HttpDateTime.parse(httpCache.ExpiresString);
				httpCache.setExpires(time);
			} catch (IllegalArgumentException e) {
				if (DEBUG) e.printStackTrace();
			}
		}
		
		String contentType = response.getContentType();
		if (contentType != null) {
			Matcher matcher = ContentPattern.matcher(contentType);
			if (matcher.find()) {
				httpCache.MimeType = matcher.group(1);
				httpCache.Charset = matcher.group(3);
			}
		}
		
		return httpCache;
	}
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @param httpCache
	 * @param preObject 预处理对象
	 * @throws IOException 
	 */
	private HttpCache storeHttpCache(THttpRequest request, THttpResponse response,
			HttpCache httpCache, Object preObject) throws IOException {
		if (request.isCacheDatabase() || request.isCacheFile()) {
			long length = response.getContentLength();
			CacheManager.checkCacheSizeAvaiable(length);
			
			StoreFile storeFile = null;
			if (httpCache != null && httpCache.LocalFile != null) {
				storeFile = httpCache.LocalFile;
			} else {
				storeFile = CacheManager.getStoreFile(request.getCacheUrl());
			}
			
			if (storeFile != null) {
				OutputStream output = null;
				if (preObject != null) {
					if (preObject instanceof String) {
						output = storeFile.openOutputStream();
						output.write(((String) preObject).getBytes());
						output.close();
						
						storeFile.close();
					} else if (preObject instanceof Bitmap) {
						ImageUtil.saveBitmap2File((Bitmap) preObject, 75, storeFile);
					} else if (preObject instanceof byte[]) {
						ByteArrayInputStream in = new ByteArrayInputStream(
								(byte[])preObject);
						saveStoreFile(request, storeFile, in, in.available());
					} else {
						httpCache = null;
						return httpCache;
					}
				} else {
					InputStream input = response.getResponseStream();
					if (input != null) {
						if ("gzip".equalsIgnoreCase(response.getFirstHeader(
								THttp.CONTENT_ENCODING))) {
							length = -1;
						}
						saveStoreFile(request, storeFile, input, 
								(int) length);
					} else {
						httpCache = null;
						return httpCache;
					}
				}
			}

			if (request.isCacheDatabase()) {
                httpCache = parseHttpCache(request.getCacheUrl(), 
                        response, storeFile);
            }
			else if (request.isCacheFile()) {
				httpCache = new HttpCache(request.getCacheUrl(), storeFile);
			}
			
			HttpCacheManager.addHttpCache(request, httpCache);
		}
		
		return httpCache;
	}
	
	private int getNotifyErrCode(int responseCode) {
		if (responseCode < 0) {
			responseCode = 0;
		} else if (responseCode > 1000) {
			responseCode = 1000;
		}
		return TransTypeCode.ERR_CODE_HTTP - responseCode;
	}
	
	/**
	 * http请求出错. 请出错消息回调给请求者
	 * 
	 * @param request
	 * @param err
	 */
	private void notifyError(THttpRequest request, int errCode,
			Object data) {
		if (mIsClosed) {
			return ;
		}
		
		if (request.isDivided()) {
			if (! request.shouldNotify()) {
				return ;
			}
			else {
				request = request.getParentRequest();
			}
		}
		
		List<THttpRequest> list = removeRunWaitingQueue(request);
		
		if (list != null && list.size() > 1) {
			if (errCode == TransTypeCode.ERR_CODE_CANCEL) {
				request.getHttpCallBack().onError(request, errCode, data);
				
				list.remove(request);
				
				for (THttpRequest req : list) {
					addRequest(req);
				}
			}
			else {
				if (request.getHttpCallBack() != null) {
					request.getHttpCallBack().onError(list, errCode, data);
				}
			}
		}
		else {
			if (request.getHttpCallBack() != null) {
				request.getHttpCallBack().onError(request, errCode, data);
			}
		}
		
	}
	
	/**
	 * http请求.流式数据接收.
	 * 
	 * @param request
	 * @param data
	 * @param size
	 */
	private void notifyReceived(THttpRequest request, int code, Object data) {
		if (mIsClosed) {
			return ;
		}
		
		List<THttpRequest> list = removeRunWaitingQueue(request);
		
		if (list != null && list.size() > 1) {
			if (request.getHttpCallBack() != null) {
				request.getHttpCallBack().onReceived(list, code, data);
			}
		}
		else {
			if (request.getHttpCallBack() != null) {
				request.getHttpCallBack().onReceived(request, code, data);
			}
		}
	}
	
	
}
