package com.netease.common.image;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.netease.common.cache.CacheMap;
import com.netease.common.config.IConfig;
import com.netease.common.http.HttpDataChannel;
import com.netease.common.http.HttpEngine;
import com.netease.common.image.task.ImageTransaction;
import com.netease.common.service.BaseService;
import com.netease.common.task.TransTypeCode;
import com.netease.common.task.TransactionEngine;
import com.netease.common.task.TransactionListener;
import com.netease.util.PlatformUtil;

/**
 * 使用ImageManager作为图片管理类型，由外部注册事务引擎方式后可使用
 * 
 * @author dingding
 *
 */
public final class ImageManager extends BaseService implements TransactionListener, IConfig {
	
	public static final int ALL_DOWNLOAD = 0x00; // 所有情况下全下
	public static final int NO_DOWNLOAD = 0x01; // 所有情况下都不下载
	public static final int WIFI_DOWNLOAD = 0x02; // WIFI 情况下下载
	
	/************************以下 IConfig 配置项*******************************/
	
	/**
	 * 图片下载Http线程个数
	 */
	public static int ImageHttpThread = 5;
	
	/**
	 * 图片下载类型
	 * 
	 * ALL_DOWNLOAD = 0x00; // 所有情况下全下
	 * NO_DOWNLOAD = 0x01; // 所有情况下都不下载
	 * WIFI_DOWNLOAD = 0x02; // WIFI 情况下下载
	 */
	public static int DownloadType = ALL_DOWNLOAD;
	
	/**
	 * 图片下载Http线程优先级
	 */
	public static int ImageHttpPriority = Thread.NORM_PRIORITY - 2;
	
	/************************以上 IConfig 配置项*******************************/

	/****************************以下私有属性********************************/
	private static ImageManager mInstance;
	
	private CacheMap<Integer, Bitmap> mImageCache;
	
	private int mDeviceWidth;
	private int mDeviceHeight;
	private int mDeviceHeight_4; // 1/4 device height
	
	Handler mHandler;
	
	/****************************以上私有属性********************************/

	/**************************************************************************
	 * 
	 * ImageManager 开放外部调用接口
	 * 
	 *************************************************************************/
	
	/**
	 * 获取单实例
	 */
	public static ImageManager getInstance() {
		if (mInstance == null) {
			mInstance = new ImageManager();
		}
		
		return mInstance;
	}
	
	/**
	 * add image to memory cache
	 * @param key
	 * @param bitmap
	 */
	public void addImageCache(String key, Bitmap bitmap) {
		if (TextUtils.isEmpty(key) || bitmap == null) {
			return ;
		}
		
		mImageCache.put(key.hashCode(), bitmap);
	}
	
	/**
	 * 
	 * @param key
	 * @deprecated
	 */
	public Bitmap getImageCache(String key) {
		Bitmap bitmap = null;
		
		if (! TextUtils.isEmpty(key)) {
			bitmap = mImageCache.get(key.hashCode());
			
			if (bitmap != null && bitmap.isRecycled()) {
				bitmap = null;
			}
		}
		
		return bitmap;
	}
	
	/**
	 * 获取图片
	 * 
	 * @param url
	 * @param width
	 * @param height
	 * @param type
	 * @param callback
	 */
	public void getImage(String url, int width, int height, int roundCornerSize, 
			ImageType type, ImageAsyncCallback callback) {
		getImage(url, width, height, roundCornerSize, type, 
				ImageNetControl.Default, callback);
	}
	
	/**
	 * 获取图片
	 * 
	 * @param url
	 * @param width
	 * @param height
	 * @param type
	 * @param callback
	 */
	public void getImage(String url, int width, int height, int roundCornerSize, 
			ImageType type, ImageNetControl net, ImageAsyncCallback callback) {
		Bitmap bitmap = getImageCache(url, width, height, roundCornerSize, type);
		if (bitmap != null) {
			callback.onUiGetImage(-1, bitmap);
		} else {
			ImageTransaction imageTrans = new ImageTransaction(url, width, height, 
					roundCornerSize, type, net, callback);
			startImageTransaction(imageTrans, callback);
		}
	}
	
	
	/**
	 * 获取图片
	 * 
	 * @param resources
	 * @param resId
	 * @param width
	 * @param height
	 * @param type
	 * @param callback
	 */
	public void getImage(Resources resources, int resId, int width, int height,
			int roundCornerSize, ImageType type, ImageAsyncCallback callback) {
		Bitmap bitmap = getImageCache(String.valueOf(resId), width, height,
				roundCornerSize, type);
		if (bitmap != null) {
			callback.onUiGetImage(-1, bitmap);
		} else {
			ImageTransaction imageTrans = new ImageTransaction(resources, resId,
					width, height, roundCornerSize, type, callback);
			startImageTransaction(imageTrans, callback);
		}
	}
	
	/**
	 * 预下载图片
	 * 
	 * @param url
	 * @param width
	 * @param height
	 * @param decode
	 * @param listener
	 */
	public void downloadImage(String url, int width, int height, 
			boolean decode, TransactionListener listener) {
		ImageTransaction imageTrans = new ImageTransaction(url, 
				width, height, ImageType.NoCache, decode);
		imageTrans.setListener(listener);
		beginTransaction(imageTrans);
	}
	
	/**
	 * 清除图片内存缓存
	 */
	public void clearCache() {
		mImageCache.clear();
	}
	
	/*************************************************************************/
	
	private ImageManager() {
		super(new HttpDataChannel(TransactionEngine.Instance(), 
				new HttpEngine(ImageHttpThread, ImageHttpPriority)));
		
		mImageCache = new CacheMap<Integer, Bitmap>();
		mHandler = new InternalHandler(Looper.getMainLooper());
	}
	
	protected static class InternalHandler extends Handler {
		public InternalHandler(Looper looper) {
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			ImageResult result = (ImageResult) msg.obj;
			result.mImageCallback.onUiGetImage(msg.arg2, result.mBitmap);
		}
	}
	
	
	private static boolean checkImageCache(ImageType type) {
		boolean ret = false;
		switch (type) {
		case MemCache:
		case RoundMemCache:
		case CircleMemCache:
			ret = true;
			break;
		}
		
		return ret;
	}
	
	private int getImageCode(String url, int width, int height, 
			int roundCornerSize, ImageType type) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(url).append("&w=").append(width).append("&h=")
			.append(height).append("&rc=").append(roundCornerSize)
			.append("&t=").append(type.ordinal());
		
		return buffer.toString().hashCode();
	}
	
	private void addImageCache(ImageResult result) {
		if (checkImageCache(result.mImageType) && result.mBitmap != null) {
			if (mDeviceWidth <= 0) {
				int[] deviceSize = PlatformUtil.getDisplayMetrics(
						BaseService.getServiceContext());
				mDeviceWidth = deviceSize[0];
				mDeviceHeight = deviceSize[1];
				
				mDeviceHeight_4 = mDeviceHeight;
			}
			
			if (result.mBitmap.getWidth() <= mDeviceWidth 
					&& result.mBitmap.getHeight() <= mDeviceHeight_4) {
				mImageCache.put(getImageCode(result.mUrl, result.mWidth, 
						result.mHeight, result.mRoundCornerSize, result.mImageType),
						result.mBitmap);
			}
		}
	}
	
	public Bitmap getImageCache(String url, int width, int height, 
			int roundCornerSize, ImageType type) {
		Bitmap bitmap = null;
		if (checkImageCache(type)) {
			bitmap = mImageCache.get(getImageCode(url, width, height, 
					roundCornerSize, type));
			if (bitmap != null && bitmap.isRecycled()) {
				bitmap = null;
			}
		}
		return bitmap;
	}
	
	private void startImageTransaction(ImageTransaction trans, 
			ImageAsyncCallback callback) {
		if (trans != null) {
			if (callback != null) {
				callback.startImageTransacion(trans);
			}
			trans.setListener(this);
			beginTransaction(trans);
		}
	}
	
	/******************************图片下载回调处理****************************/
	
	@Override
	public void onTransactionMessage(int code, int type, int tid, Object arg3) {
		if (type == TransTypeCode.TYPE_IMAGE 
				&& arg3 != null && arg3 instanceof ImageResult) {
			ImageResult result = (ImageResult) arg3;
			if (result.mUrl != null && result.mBitmap != null) {
				addImageCache(result);
			}
			
			if (result.mImageCallback != null 
					&& result.mImageCallback.isValid(tid)
					&& result.mImageCallback.onPreUiGetImage(tid, result.mBitmap)) {
				mHandler.obtainMessage(code, type, tid, result).sendToTarget();
			}
		}
	}

	@Override
	public void onTransactionError(int errCode, int type, int tid, Object arg3) {
		onTransactionMessage(errCode, type, tid, arg3);
	}

}
