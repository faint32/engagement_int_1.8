package com.netease.common.image.task;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build.VERSION;
import android.webkit.URLUtil;

import com.netease.common.cache.CacheManager;
import com.netease.common.cache.file.StoreFile;
import com.netease.common.http.THttpRequest;
import com.netease.common.http.cache.HttpCache;
import com.netease.common.http.httpclient.THttp;
import com.netease.common.image.ImageAsyncCallback;
import com.netease.common.image.ImageManager;
import com.netease.common.image.ImageNetControl;
import com.netease.common.image.ImageResult;
import com.netease.common.image.ImageType;
import com.netease.common.image.ImageViewAsyncCallback;
import com.netease.common.image.util.ImageUtil;
import com.netease.common.nio.NioListener;
import com.netease.common.service.BaseService;
import com.netease.common.task.AsyncTransaction;
import com.netease.common.task.NotifyTransaction;
import com.netease.common.task.TransTypeCode;
import com.netease.util.PlatformUtil;
import com.tencent.mm.sdk.Build;

/**
 * 
 * 
 * @author dingding
 *
 */
public class ImageTransaction extends AsyncTransaction implements NioListener {

	String mUrl;
	Resources mResources;
	int mResId;
	
	String mCacheUrl;
	
	ImageType mImageType;
	ImageNetControl mImageNetControl;
	WeakReference<ImageAsyncCallback> mImageCallback;
	
	boolean mNeedDecode;
	boolean mIsTransport;
	
	int mWidth;
	int mHeight;
	int mCornerSize;
	
	THttpRequest mTHttpRequest;
	
	public ImageTransaction(String url, int width, int height, int cornerSize, 
			ImageType type, ImageNetControl net, ImageAsyncCallback callback) {
		super(TransTypeCode.TYPE_IMAGE);
		
		mUrl = url;
		mWidth = width;
		mHeight = height;
		mCornerSize = cornerSize;
		mImageType = type;
		mImageNetControl = net;
		mImageCallback = new WeakReference<ImageAsyncCallback>(callback);
		mNeedDecode = true;
		
		mCacheUrl = callback.getCacheUrl(this);
		mIsTransport = callback.isTransport(this);
	}
	
	public ImageTransaction(Resources res, int resId, int width, int height,
			int cornerSize, ImageType type, ImageAsyncCallback callback) {
		super(TransTypeCode.TYPE_IMAGE);
		
		mResources = res;
		mResId = resId;
		mWidth = width;
		mHeight = height;
		mCornerSize = cornerSize;
		mNeedDecode = true;
		mImageCallback = new WeakReference<ImageAsyncCallback>(callback);
		
		mCacheUrl = callback.getCacheUrl(this);
	}
	
	public ImageTransaction(String url, int width, int height, 
			ImageType type, boolean needDecode) {
		super(TransTypeCode.TYPE_IMAGE);
		
		mUrl = url;
		mWidth = width;
		mHeight = height;
		mImageType = type;
		mNeedDecode = needDecode;
	}
	
	public String getUrl() {
		return mUrl;
	}

	@Override
	protected void onTransactionError(int errCode, Object obj) {
		notifyError(errCode, getNotifyResult(null, null));
	}

	@Override
	protected void onTransactionSuccess(int code, Object obj) {
		if (obj != null && obj instanceof ImageResult) {
			ImageResult result = (ImageResult) obj;
			result = new ImageResult(result);
			if (mImageCallback != null) {
				result.mImageCallback = mImageCallback.get();
			}
			notifyMessage(0, result);
		} else {
			notifyError(0, getNotifyResult(null, null));
		}
	}
	
	   /*
     * An InputStream that skips the exact number of bytes provided, unless it reaches EOF.
     */
    static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0) {
                        break;  // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }
	
//	@Override
//	public Object onDataChannelPreNotify(Object data, int notifyType, int code) throws IOException {
//		Bitmap bitmap = null;
//		if (needDecoded() && notifyType == NotifyTransaction.NOTIFY_TYPE_SUCCESS) {
//			if (data != null && data instanceof THttpResponse) {
//				try {
//					bitmap = BitmapFactory.decodeStream(new FlushedInputStream(
//							((THttpResponse)data).getResponseStream()));
//				} catch (OutOfMemoryError e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		return bitmap;
//	}
	
	@Override
	public void onTransact() {
		Bitmap bitmap = null;
		
		if (mUrl != null) {
			if (URLUtil.isFileUrl(mUrl)) {
		        URI uri = URI.create(mUrl);
                File file = new File(uri);
				
				bitmap = getImage(file, mWidth, mHeight);
				
				notify(bitmap, null);
			}
			else if (URLUtil.isContentUrl(mUrl)) {
				Uri imgUri = Uri.parse(mUrl);
				
				bitmap = getImage(imgUri, mWidth, mHeight);
				
				notify(bitmap, null);
			}
			else {
				THttpRequest request = getHttpRequest();
				
				StoreFile storeFile = CacheManager.getStoreFile(request.getCacheUrl());
				
				if (needDecoded()) {
					bitmap = getImage(storeFile, mWidth, mHeight);
					
					if (bitmap != null) {
						notify(bitmap, storeFile);
					} else {
//						if (mWidth > 0 || mHeight > 0) {
							//TODO: 检查图片是否存在时，判断是否原始图片存在
//							StoreFile oldFile = CacheManager.getStoreFile(mUrl);
//							bitmap = getImage(oldFile, mWidth, mHeight);
//						
//							if (bitmap != null) {
//								notify(bitmap, oldFile);
//							
//								ImageUtil.saveBitmap2File(bitmap, 75, storeFile, 
//										CompressFormat.JPEG);
//							} else {
								sendRequest(request);
//							}
//						}
					}
				} else {
					if (storeFile != null && storeFile.exists()) {
						notify(null, storeFile);
					} else {
						sendRequest(request);
					}
				}
			}
		}
		else if (mResources != null) {
			bitmap = ImageUtil.getBitmap(mResources, mResId, mWidth, mHeight);
			notify(bitmap, null);
		}
		else {
			doEnd();
		}
	}
	
	@Override
	protected void sendRequest(Object obj) {
		mTHttpRequest = (THttpRequest) obj;
		
		mTHttpRequest.setNioListener(this);
		
		Context context = BaseService.getServiceContext();
		if (mImageNetControl == null) {
			mImageNetControl = ImageNetControl.Default;
		}
		
		switch (mImageNetControl) {
		case Default:
			switch (ImageManager.DownloadType) {
			case ImageManager.NO_DOWNLOAD:
				errorNotify();
				return;

			case ImageManager.WIFI_DOWNLOAD:
				if (!PlatformUtil.isWifiNetWork(context)) {
					errorNotify();
					return;
				}
				break;
			}
			break;
		case CacheOnly:
			errorNotify();
			return;
		case WifiOnly:
			if (!PlatformUtil.isWifiNetWork(context)) {
				errorNotify();
				return;
			}
			break;
		default:
			break;
		}
		
		super.sendRequest(obj);
	}
	
	private THttpRequest getHttpRequest() {
		THttpRequest request = new THttpRequest(mUrl) {
			@Override
			public String onRedirectUrl(String url, THttpRequest request) {
				if (mImageCallback != null) {
					ImageAsyncCallback callback = mImageCallback.get();
					
					if (callback != null) {
						return callback.onRedirectUrl(mUrl, url);
					}
				}
				
				return super.onRedirectUrl(url, request);
			}
			
			@Override
			public String getRequestUrl() {
				return makeServerQualityUrl(super.getRequestUrl(), mIsTransport);
			}
		};
		if (mWidth > 0 || mHeight > 0) {
			StringBuffer buffer = new StringBuffer();
			appendImageHttpLocalParam(buffer, mWidth, mHeight);
			request.setUrlLocalParam(buffer.toString());
		}
		
		request.setCacheUrl(mCacheUrl);
		request.setCacheFile();
		
		return request;
	}
	
	/**
     * 添加服务器图片质量参数
     * @param url
     * @return
     */
    public static String makeServerQualityUrl(String url, boolean transport) {
    	if(url != null && url.startsWith("http://yimg.nos.netease.com")) {
    		StringBuilder sb = new StringBuilder(url);
    		int quality = 70;
    		try {
    			Context context = BaseService.getServiceContext();
    			
    			if(transport || PlatformUtil.isWifiNetWork(context)) {
    				quality = 70; //wifi环境下quality为70
    			} else {
    				quality = 30; //mobile环境下quality为30
    			}
    		} catch(Exception e) {
    			
    		}
    		
    		if(url.contains("?imageView")) {
    			sb.append("&quality=").append(quality);
    		} else {
    			sb.append("?imageView&quality=").append(quality);
    		}
    		
    		if (! transport || VERSION.SDK_INT > 17) {
    			sb.append("&type=").append("webp");
    		}
    		
    		return sb.toString();
    	}
    	
    	return url;
    }
	
	private Bitmap getImage(Uri contentUri, int width, int height) {
		Bitmap bitmap = null;
		if (! isCancel() && contentUri != null) {
			try {
				ContentResolver cr = BaseService.getServiceContext().getContentResolver();
				InputStream input = cr.openInputStream(contentUri);
				
				bitmap = ImageUtil.getBitmap(input, width, height);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return bitmap;
	}
	
    private Bitmap getImage(File file, int width, int height) {
        Bitmap bitmap = null;
        if (!isCancel() && file != null && file.exists()) {
            bitmap = ImageUtil.getBitmap(file, width, height);
        }

        return bitmap;
    }
	
    private Bitmap getImage(StoreFile file, int width, int height) {
        Bitmap bitmap = null;
        if (!isCancel() && file != null && file.exists()) {
        	File img = new File(file.getPath());
            bitmap = ImageUtil.getBitmap(img, width, height);
            
            if (bitmap == null) {
                file.delete();
            }
        }

        return bitmap;
    }
	
	private ImageResult getNotifyResult(Bitmap bitmap, StoreFile file) {
		if (bitmap != null) {
			switch (mImageType) {
			case CircleNoCache:
			case CircleMemCache:
				bitmap = ImageUtil.getCircleBitmap(bitmap);
				break;
			case RoundMemCache:
			case RoundNoCache:
				bitmap = ImageUtil.getRoundedCornerBitmap(bitmap, mCornerSize);
				break;
			}
		}
		
		ImageResult result = new ImageResult();
		result.mUrl = mUrl != null ? mUrl : String.valueOf(mResId);
		result.mBitmap = bitmap;
		result.mRoundCornerSize = mCornerSize;
		result.mWidth = mWidth;
		result.mHeight = mHeight;
		if (mImageCallback != null) {
			result.mImageCallback = mImageCallback.get();
		}
		result.mImageType = mImageType;
		result.mStoreFile = file;
		
		return result;
	}
	
	private void errorNotify() {
		notifyError(0, getNotifyResult(null, null));
		doEnd();
	}
	
	private void notify(Bitmap bitmap, StoreFile file) {
		ImageResult result = getNotifyResult(bitmap, file);
		
		notifyMessage(0, result);
		doEnd();
	}
	
	private boolean needDecoded() {
		return mNeedDecode;
	}
	
	private static void appendImageHttpLocalParam(StringBuffer buffer, 
			int width, int height) {
		if (width > 0 || height > 0) {
			buffer.append(THttp.LOCAL_PARAM_TAG);
			if (width > 0) {
				buffer.append("&w=").append(width);
			}
			if (height > 0) {
				buffer.append("&h=").append(height);
			}
		}
	}
	
	private class ImageNotifyTransaction extends NotifyTransaction {
		
		boolean mNeedImageDecode;

		public ImageNotifyTransaction(AsyncTransaction tran, Object data,
				int type, int code) {
			super(tran, data, type, code);
			
			mNeedImageDecode = ((ImageTransaction) tran).needDecoded();
		}

		public ImageNotifyTransaction(List<AsyncTransaction> trans,
				Object data, int type, int code) {
			super(trans, data, type, code);
			
			for (AsyncTransaction t : trans) {
				if (((ImageTransaction) t).needDecoded()) {
					mNeedImageDecode = true;
					break;
				}
			}
		}
		
		@Override
		public void doBeforeTransact() {
			if (isSuccessNotify()) {
				ImageResult result = null;
				
				Object data = getData();
				if (data != null) {
					if (data instanceof Bitmap) {
						StoreFile storeFile = CacheManager.getStoreFile(
								mTHttpRequest.getCacheUrl());
						
						result = getNotifyResult((Bitmap) data, storeFile);
					} else if (data instanceof HttpCache) {
						StoreFile storeFile = ((HttpCache) data).LocalFile;
						if (mNeedImageDecode) {
							Bitmap bitmap = getImage(storeFile, mWidth, mHeight);
							if (bitmap == null) {
								result = getNotifyResult(null, null);
								setNotifyTypeAndCode(NOTIFY_TYPE_ERROR, 0);
							} else {
								result = getNotifyResult(bitmap, storeFile);
							}
						} else {
							result = getNotifyResult(null, storeFile);
						}
					} else {
						result = getNotifyResult(null, null);
						setNotifyTypeAndCode(NOTIFY_TYPE_ERROR, 0);
					}
				} else {
					result = getNotifyResult(null, null);
					setNotifyTypeAndCode(NOTIFY_TYPE_ERROR, 0);
				}
				
				resetData(result);
			}
		}
	}

	@Override
	public NotifyTransaction createNotifyTransaction(Object data,
			int notifyType, int code) {
		return new ImageNotifyTransaction(this, data, notifyType, code);
	}
	
	@Override
	public NotifyTransaction createNotifyTransaction(
			List<AsyncTransaction> trans, Object data, int notifyType,
			int code) {
		return new ImageNotifyTransaction(trans, data, notifyType, code);
	}

	@Override
	public void onSpeedChange(byte type, int speed) {
		
	}

	private long mContnetLength;
	private long mDownloadSize;
	private long mLastNotifyTime;
	private long mLastNotifySize;
	
	@Override
	public void onContentLength(byte type, long length) {
		mContnetLength = length;
	}

	@Override
	public void onSizeIncrease(byte type, long size) {
		ImageAsyncCallback callback = null;
		if (mImageCallback != null) {
			callback = mImageCallback.get();
		}

		if (callback != null && callback instanceof ImageViewAsyncCallback) {
		
			int progress = (int) (((float) (mDownloadSize)) / mContnetLength * 100);
			((ImageViewAsyncCallback) callback).onLoadProgressChange(getId(), progress);
		}
		mDownloadSize += size;
	}
}
