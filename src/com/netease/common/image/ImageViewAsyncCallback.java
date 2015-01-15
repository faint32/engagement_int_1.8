package com.netease.common.image;

import java.lang.ref.WeakReference;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.netease.common.config.IConfig;
import com.netease.common.debug.CheckAssert;
import com.netease.common.image.task.ImageTransaction;
import com.netease.engagement.widget.CircleProgress;

/**
 *
 * ImageAsyncCallback 用于异步图片获取，使用ImageType标记图片类型，
 * 说明图片是否进行内存缓存
 * 
 * 例子：
 * ImageView imageView = (ImageView) findViewById(R.id.imageview);
 * imageView.setBitmapResource(R.drawable.image);
 * imageView.setTag(new ImageViewAsyncCallback(imageView, "http://test.com/test.jpg"));
 * 
 * @author dingding
 *
 */
public class ImageViewAsyncCallback implements ImageAsyncCallback, IConfig {

	/********************************IConfig 配置信息**************************/
	/**
	 * 缺省图片类型
	 * 可配置项
	 * @see IConfig
	 */
	public static ImageType DefaultImageType = ImageType.MemCache;
	/**************************以上 IConfig 配置信息**************************/
	
	private int mTid;
	private WeakReference<ImageTransaction> mImageTransRef;
	private WeakReference<ImageView> mImageViewRef;
	private WeakReference<CircleProgress> mCircleProgress;
	private boolean mIsCancel;
	
	/**
	 * 创建异步图片回调
	 * 
	 * @param imageView
	 * @param url 需要完整Http路径
	 */
	public ImageViewAsyncCallback(ImageView imageView, String url) {
		this(imageView, url, -1, -1);
	}

	/**
	 * 创建异步图片回调
	 * 
	 * @param imageView
	 * @param url 
	 * @param type
	 * @see ImageType
	 */
	public ImageViewAsyncCallback(ImageView imageView, String url, ImageType type) {
		this(imageView, url, -1, -1, type);
	}
	
	/**
	 * 创建异步图片回调
	 * 
	 * @param imageView
	 * @param url
	 * @param width
	 * @param height
	 */
	public ImageViewAsyncCallback(ImageView imageView, String url, 
			int width, int height) {
		this(imageView, url, -1, -1, DefaultImageType);
	}
	
	/**
	 * 创建异步图片回调
	 * 
	 * @param imageView
	 * @param url
	 * @param width
	 * @param height
	 * @param type
	 * @see ImageType
	 */
	public ImageViewAsyncCallback(ImageView imageView, String url, 
			int width, int height, ImageType type) {
		this (imageView, url, width, height, -1, type);
	}
	
	/**
	 * 创建异步图片回调
	 * 
	 * @param imageView
	 * @param url
	 * @param width 像素
	 * @param height 像素
	 * @param roundSize 像素
	 * @param type
	 */
	public ImageViewAsyncCallback(ImageView imageView, String url, 
			int width, int height, int roundSize, ImageType type) {
		this(imageView, null, url, width, height, roundSize, type);
	}
	
	public ImageViewAsyncCallback(ImageView imageView, CircleProgress progress,
			String url, int width, int height, int roundSize, ImageType type) {
		CheckAssert.checkNull(imageView);
		
		mImageViewRef = new WeakReference<ImageView>(imageView);
		if (progress != null) {
			mCircleProgress = new WeakReference<CircleProgress>(progress);
		}
		else {
			mCircleProgress = null;
		}
		
		if (TextUtils.isEmpty(url)) {
			imageView.setImageDrawable(null);
		} else {
			Object callback = imageView.getTag();
			if (callback != null && callback instanceof ImageViewAsyncCallback) {
				((ImageViewAsyncCallback) callback).doCancel();
			}
			imageView.setTag(this);
			
			ImageManager.getInstance().getImage(url, width, height, 
					roundSize, type, this);
		}
	}
	
	/**
	 * 创建异步图片回调，在资源包中异步 获取图片
	 * 
	 * @param imageView
	 * @param res
	 * @param resId
	 */
	public ImageViewAsyncCallback(ImageView imageView, Resources res, int resId) {
		this(imageView, res, resId, -1, -1, -1);
	}
	
	public void onLoadProgressChange(int tid, int progress) {
		
		if (mCircleProgress != null && isValid(tid)) {
			CircleProgress view = mCircleProgress.get();
			if (view != null) {
				view.setProgress(progress);
			}
		}
	}
	
	/**
	 * 创建异步图片回调，在资源包中异步 获取图片
	 * 
	 * @param imageView
	 * @param res
	 * @param resId
	 * @param width 像素
	 * @param height 像素
	 * @param roundCornerSize 像素
	 */
	public ImageViewAsyncCallback(ImageView imageView, Resources res, int resId,
			int width, int height, int roundCornerSize) {
		CheckAssert.checkNull(imageView);
		CheckAssert.checkNull(res);
		
		mImageViewRef = new WeakReference<ImageView>(imageView);
		mCircleProgress = null;

		Object callback = imageView.getTag();
		if (callback != null && callback instanceof ImageViewAsyncCallback) {
			((ImageViewAsyncCallback) callback).doCancel();
		}
		imageView.setTag(this);
		
		ImageManager.getInstance().getImage(res, resId, width, height, 
				roundCornerSize, ImageType.NoCache, this);
	}
	
	/**
	 * 开始图片事务
	 * 
	 * @param imageTrans
	 */
	@Override
    public void startImageTransacion(ImageTransaction imageTrans) {
		if (imageTrans != null) {
			mImageTransRef = new WeakReference<ImageTransaction>(imageTrans);
			mTid = imageTrans.getId();
		}
	}
	
	/**
	 * 在非UI线程检查图片有效性(在isValid()方法之后调用)
	 * 
	 * @param bitmap
	 * @return 是否需要转到UI线程进行处理
	 */
	@Override
    public boolean onPreUiGetImage(int tid, Bitmap bitmap) {
		return bitmap != null;
	}
	
	/**
	 * 在判断完onPreUiGetImage后，对获取得到的图片Null时调用
	 * 
	 * @param imageView 所属ImageView
	 */
	public void onUiGetImageNull(int tid, ImageView imageView) {
		
	}
	
	/**
	 * 
	 * @param bitmap
	 */
	@Override
    public void onUiGetImage(int tid, Bitmap bitmap) {
		WeakReference<ImageView> imageViewRef = mImageViewRef;
		if (isValid(tid) && imageViewRef != null) {
			ImageView imageView = imageViewRef.get();
			if (imageView != null) {
				if (bitmap == null) {
					onUiGetImageNull(tid, imageView);
				} else {
					if (tid < 0) {
						imageView.setImageBitmap(bitmap);
					}
					else {
						TransitionDrawable td = new TransitionDrawable(
								new Drawable[] { new ColorDrawable(0x0106000D),
										new BitmapDrawable(imageView.getResources(), bitmap) });

						imageView.setImageDrawable(td);
						td.startTransition(300);
					}
				}
			}
		}
	}
	
	/**
	 * 验证当前ImageAsyncCallback所关联的ImageView是否还关心当前图片下载
	 * @return
	 */
	@Override
    public boolean isValid(int tid) {
		boolean ret = false;
		
//		WeakReference<ImageView> imageViewRef = mImageViewRef;
		
		if (!mIsCancel) {
//				&& imageViewRef != null) {
//			ImageView imageView = imageViewRef.get();
//			ret = (imageView != null && imageView.getTag() == this);
			ret = tid < 0 || mTid == tid;
		}
		
		return ret;
	}
	
	/**
	 * 被回收后进行cancel处理
	 */
	public void doCancel() {
		mIsCancel = true;
		mTid = -1;
		mImageViewRef = null;
		
		WeakReference<ImageTransaction> imageTransRef = mImageTransRef;
		mImageTransRef = null;
		
		if (imageTransRef != null) {
			ImageTransaction trans = imageTransRef.get();
			if (trans != null) {
				trans.doCancel();
			}
		}
	}

	/**
	 * 图片链接重定向处理 
	 */
	@Override
	public String onRedirectUrl(String originalUrl, String redirectUrl) {
		return redirectUrl;
	}
	
	/**
     * 计算自有的缓存方式
     */
//    @Override
//    public String getCacheUrl(ImageTransaction imageTrans) {
//        return null;
//    }
	
    @Override
    public String getCacheUrl(ImageTransaction trans) {
        String url = trans.getUrl();
        if (url.toLowerCase().contains("NOSAccessKeyId".toLowerCase())) {
            int index = url.indexOf("?NOSAccessKeyId");
            if (index >= 0) {
                return url.substring(0, index);
            }
        }
        return null;
    };
    
    @Override
    public boolean isTransport(ImageTransaction trans) {
    	return false;
    }
}
