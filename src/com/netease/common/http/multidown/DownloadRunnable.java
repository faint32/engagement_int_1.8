package com.netease.common.http.multidown;

import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.content.Context;
import android.os.PowerManager;
import android.os.StatFs;
import android.util.Log;

import com.netease.common.cache.file.FileCreateException;
import com.netease.common.http.httpclient.TrustAllSSLSocketFactory;

public class DownloadRunnable implements Runnable {
	
	private static final String TAG = "DownloadRunnable";
	
	private static final int BUFFER_SIZE = 4 * 1024;
	
    // Default connection and socket timeout of 60 seconds.  Tweak to taste.
    private static final int SOCKET_OPERATION_TIMEOUT = 60 * 1000;
    
    static SchemeRegistry mSchReg;
	
	static { // 静态变量初始化
		mSchReg = new SchemeRegistry();
		mSchReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		mSchReg.register(new Scheme("https", TrustAllSSLSocketFactory.getDefault(), 443));
	}

	private Context mContext;
	private DownloadState mDownloadState;
	private int mDownloadIndex;
	private ThreadPoolExecutor mExecutor;
	private DownloadListener mDownloadListener;
	
	public DownloadRunnable(Context context, ThreadPoolExecutor executor, 
			DownloadState downloadState, int index, DownloadListener listener) {
		mContext = context;
		mExecutor = executor;
		mDownloadState = downloadState;
		mDownloadIndex = index;
		mDownloadListener = listener;
	}
	
	@Override
	public void run() {
		Log.e(TAG, "run: " + mDownloadIndex);
		
		HttpClient client = null;
		PowerManager.WakeLock wakeLock = null;

		final PowerManager pm = (PowerManager) mContext
				.getSystemService(Context.POWER_SERVICE);
		try {
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Constants.TAG);
            wakeLock.acquire();
            
            client = buildHttpClient(mContext, Constants.DEFAULT_USER_AGENT);
            
            boolean finished = false;
            while (!finished && !mDownloadState.isCancel()) {
	            HttpGet request = new HttpGet(mDownloadState.mUrl);
	            try {
	            	checkDownloadCancel();
	            	
	                executeDownload(mDownloadState, client, request);
	                finished = true;
	            } catch (RetryDownload exc) {
	            	exc.printStackTrace();
	            } finally {
	                request.abort();
	                request = null;
	            }
            }
            
            notifyDownloadState();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (wakeLock != null) {
                wakeLock.release();
                wakeLock = null;
            }
		}
	}
	
	private void increaseDownloadSize(int size) {
		if (mDownloadState.increaseDownloadSize(mDownloadIndex, size)) {
			// need notify
			notifyDownloadState();
			
			DownloadPreference.saveDownloadState(mContext, mDownloadState);
		}
	}
	
	private void notifyDownloadState() {
		if (mDownloadListener != null) {
			mDownloadListener.notifyDownloadState(mDownloadState);
		}
	}

	private void checkDownloadCancel() throws RetryDownload {
		if (mDownloadState.isCancel()) {
			throw new RetryDownload();
		}
	}
	
	private void readWriteHttpContent(HttpEntity entity, 
			DataOutput output, int preferLength) 
					throws IllegalStateException, IOException, RetryDownload {
		if (preferLength > 0) {
			InputStream input = entity.getContent();
			byte[] data = new byte[4 * 1024];
			if (preferLength > 0) {
				int numread = 0;
				int count = 0;
				int offset = 0;
				
				while (count < preferLength && ! mDownloadState.isCancel()) {
					numread = input.read(data, offset, Math.min(
							data.length - offset, preferLength
							- count));
					offset += numread;
					
					if (numread == -1) { // 数据长度 与size 大小不匹配.
						throw new IOException(
								"http readData from stream num mismatch");
					} else if (offset == BUFFER_SIZE) {
						output.write(data, 0, BUFFER_SIZE);
						increaseDownloadSize(BUFFER_SIZE);
						offset = 0;
						count += numread;
					} else {
						count += numread;
					}
				}
				
				if (offset > 0) {
					output.write(data, 0, offset);
					increaseDownloadSize(offset);
				}
				
				checkDownloadCancel();
			}
		} else {
			
		}
	}

	private void executeDownload(DownloadState state,
			HttpClient client, HttpGet request) throws RetryDownload {
//		throw new RetryDownload();
		if (state.mInit) {
			if (mDownloadIndex < 0) {
				for (int i = 0; i < state.getPreferThreadSize(); i++) {
					mExecutor.execute(new DownloadRunnable(mContext, 
							mExecutor, state, i, mDownloadListener));
				}
			} else {
				long start = mDownloadState.getRangeStart(mDownloadIndex);
				long end = mDownloadState.getRangeEnd(mDownloadIndex);
				String range = String.format("bytes=%d-%d", start, end);
				Log.e("Range", "" + range);
				request.addHeader("Range", range);
				
				try {
					HttpResponse response = client.execute(request);
					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_PARTIAL_CONTENT) {
						HttpEntity entity = response.getEntity();
						if (entity != null) {
							int preferLength = (int) entity.getContentLength();
							checkDownloadCancel();
							
							Log.e("response", "Length" + entity.getContentLength() + " Type:" + entity.getContentType());
							
							RandomAccessFile file = new RandomAccessFile(
									mDownloadState.mTargetPath + "_tmp", "rwd");
							file.seek(start);
							
							try {
								readWriteHttpContent(entity, file, preferLength);
							} catch (IllegalStateException e) {
								e.printStackTrace();
								throw e;
							} catch (IOException e) {
								// TODO: handle exception
								throw e;
							} finally {
								file.close();
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					throw new RetryDownload();
				} finally {
					request.abort();
				}
			}
		} else {
			try {
				HttpResponse response = client.execute(request);
				
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					checkDownloadCancel();
					long contentLength = response.getEntity().getContentLength();
					if (contentLength >= DownloadService.MinMultiDownloadSize) {
						setupFile(state.mTargetPath, contentLength);
						Header contentType = response.getEntity().getContentType();
						state.init(contentLength, 
								contentType != null ? contentType.getValue() : null);
						
						notifyDownloadState();
						
						checkDownloadCancel();
						for (int i = 0; i < state.getPreferThreadSize(); i++) {
							mExecutor.execute(new DownloadRunnable(mContext, 
									mExecutor, state, i, mDownloadListener));
						}
					} else if (contentLength > 0) {
						setupFile(state.mTargetPath, contentLength);
					} else {
						
					}
				} else {
					
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new RetryDownload();
			} finally {
				request.abort();
			}
		}
	}
	
	private static byte[] tmp = new byte[512]; 
	
	/**
	 * 创建一个空大小的文件
	 * 
	 * @param path
	 * @param length
	 */
	public static synchronized void setupFile(String path, long length)
			throws IOException {
		File file = new File(path + "_tmp");
		StatFs statFS = new StatFs(file.getParent());
		if ((long)statFS.getAvailableBlocks()*statFS.getBlockSize() > 
				length + (10 << 10)) { // 10MB
			RandomAccessFile randomFile = new RandomAccessFile(file, "rwd");
			
			if (length > tmp.length) {
				randomFile.seek(length - tmp.length);
				randomFile.write(tmp);
			} else {
				randomFile.write(tmp, 0, (int)length);
			}
			
			randomFile.close();
		} else {
			throw new FileCreateException();
		}
	}

	private HttpClient buildHttpClient(Context context, String userAgent) {
		HttpParams params = new BasicHttpParams();

        // Turn off stale checking.  Our connections break all the time anyway,
        // and it's not worth it to pay the penalty of checking every time.
        HttpConnectionParams.setStaleCheckingEnabled(params, false);

        HttpConnectionParams.setConnectionTimeout(params, SOCKET_OPERATION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, SOCKET_OPERATION_TIMEOUT);
        HttpConnectionParams.setSocketBufferSize(params, 8192);

        // Set the specified user agent and register standard protocols.
        HttpProtocolParams.setUserAgent(params, userAgent);
		HttpClientParams.setRedirecting(params, true);

		ClientConnectionManager conMgr = new ThreadSafeClientConnManager(
				params, mSchReg);
		
		return new DefaultHttpClient(conMgr, params);
	}
	
}
