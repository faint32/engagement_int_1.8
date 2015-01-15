
package com.netease.engagement.widget;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.netease.common.config.IConfig;
import com.netease.common.image.ImageAsyncCallback;
import com.netease.common.image.ImageManager;
import com.netease.common.image.ImageNetControl;
import com.netease.common.image.ImageType;
import com.netease.common.image.ImageViewAsyncCallback;
import com.netease.common.image.task.ImageTransaction;
import com.netease.util.ImageViewUtil;
import com.netease.util.ImageViewUtil.Scale;

/**
 * <p>
 * 异步加载图片ImageView
 * </p>
 * <p>
 * 使用setLoadingImage加载图片
 * </p>
 * <p>
 * 使用setServerClipSize设置图片服务器的裁剪宽高
 * </p>
 * @author echo_chen(add)
 * @since  2014-03-11
 * @version 1.0
 */
public class LoadingImageView extends ImageView implements ImageAsyncCallback, IConfig {
	
	private static final boolean DEBUG = true;
	private static final String TAG = "LoadingImageView";

    /******************************** IConfig 配置信息 **************************/
    /**
     * 缺省图片类型 可配置项
     *
     * @see IConfig
     */
    public static ImageType DefaultImageType = ImageType.MemCache;
    /************************** 以上 IConfig 配置信息 **************************/

    private WeakReference<ImageTransaction> mImageTransRef;
    private int mTid;
    private boolean mNeedLoadImageErrorCallBack;// 是否需要在下载出错的情况下回调UI线程的下载出错接口，默认情况关闭
    private int mServerClipWidth, mServerClipHeight;
    private ImageNetControl mImageNetControl = ImageNetControl.All;//缺省纯文字模式不下载
    private String mUrl;//记录请求url,用于个别界面需要取出比较
    /** 默认图片的res id，请求的图片如果为null就使用默认图片 */
    private int mDefaultImgResId;
    private boolean mScaleTop;
    private ScaleType mScaleType;
    private CircleProgress mCircleProgress;
    
    /**
     * ignore request layout
     */
    private boolean mIgnoreRequestLayout;

    public LoadingImageView(Context context) {
        this(context, null, 0);
    }

    public LoadingImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        mScaleType = getScaleType();
    }
    
    public void setCircleProgress(CircleProgress progress) {
    	mCircleProgress = progress;
    }
    
    public void setScaleType(ScaleType scaleType) {
    	super.setScaleType(scaleType);
    	
    	if (scaleType != ScaleType.MATRIX) {
    		this.mScaleType = scaleType;
    	}
	}
    
    public void setScaleTop(boolean value) {
    	mScaleTop = value;
    }
    
    public void setIgnoreRequestLayout(boolean value) {
    	mIgnoreRequestLayout = value;
    }
    
    @Override
    public void requestLayout() {
//    	if (mIgnoreRequestLayout) {
    		super.requestLayout();
//    	}
//    	else {
//    		mIgnoreRequestLayout = false;
//    	}
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
    		int bottom) {
    	super.onLayout(changed, left, top, right, bottom);
    	
    	if (mScaleTop && getScaleType() == ScaleType.MATRIX) {
        	ImageViewUtil.setScaleMetrix(this, Scale.FIT_HEAD);
        }
    }
    
    @Override
    public void setImageResource(int resId) {
    	super.setImageResource(resId);
    	
    	if (mScaleTop && mScaleType != null && mScaleType != getScaleType()) {
    		super.setScaleType(mScaleType);
    	}
    }
    
    @Override
    public void setImageDrawable(Drawable drawable) {
    	super.setImageDrawable(drawable);
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
//        doCancel();
    }
    /**
     * 获取当前加载图片的url
     *
     * @return mUrl
     */
    public String getUrl(){
        return mUrl;
    }
    
    /** 设置默认图片的res id，请求的图片如果为null就使用默认图片 */
    public void setDefaultResId(int resId){
        mDefaultImgResId = resId;
    }
    
    /**
     * 创建异步图片回调
     *
     * @param url 需要完整Http路径
     */
    public void setNetControl(ImageNetControl netControl) {
        mImageNetControl = netControl;
    }

    /**
     * 创建异步图片回调
     *
     * @param url 需要完整Http路径
     */
    public void setLoadingImage(String url) {
        setLoadingImage(url, -1, -1);
    }

    /**
     * 创建异步图片回调
     *
     * @param url
     * @param type
     * @see ImageType
     */
    public void setLoadingImage(String url, ImageType type) {
        setLoadingImage(url, -1, -1, type);
    }

    /**
     * 创建异步图片回调
     *
     * @param url
     * @param type
     * @param roundSize
     */
    public void setLoadingImage(String url, ImageType type, int roundSize) {
        setLoadingImage(url, -1, -1, roundSize, type);
    }

    /**
     * 创建异步图片回调
     *
     * @param url
     * @param width
     * @param height
     */
    public void setLoadingImage(String url, int width, int height) {
        setLoadingImage(url, width, height, DefaultImageType);
    }

    /**
     * 创建异步图片回调
     *
     * @param url
     * @param width
     * @param height
     * @param type
     * @see ImageType
     */
    public void setLoadingImage(String url, int width, int height, ImageType type) {
        setLoadingImage(url, width, height, -1, type);
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
    public void setLoadingImage(String url, int width, int height, int roundSize, ImageType type) {
        doCancel();
        if (TextUtils.isEmpty(url)) {
            // setImageDrawable(null);
            if(mDefaultImgResId > 0){
                setImageResource(mDefaultImgResId);
                
                if(mUiGetImageListener != null){
                    mUiGetImageListener.onLoadImageError();
                }
            }
            
            setTag(null);
        } 
        else {
            if(mUiGetImageListener != null){
                mUiGetImageListener.onLoadImageStar(url);
            }
            
            mUrl = url;
            url = makeServerClipUrl(null, url, mServerClipWidth, mServerClipHeight);// 添加裁剪参数
//           Log.v(TAG, "mProgressBar==null "+(mCircleProgress==null)+" type "+type) ;
            ImageCallback callback = new ImageCallback(this, mCircleProgress, url, width, height, roundSize, type);
            setTag(callback);
        }
    }
    
    class ImageCallback extends ImageViewAsyncCallback {
    	
	    	public ImageCallback(ImageView imageView, CircleProgress circleProgress, 
	    			String url, int width, int height, int roundSize, ImageType type) {
	    		super(imageView, circleProgress, url, width, height, roundSize, type);
	    	}
	    	
	    	@Override
	    	public void onUiGetImage(int tid, Bitmap bitmap) {
	    	// TODO Auto-generated method stub
			LoadingImageView.this.onUiGetImage(tid, bitmap);
			if (mCircleProgress != null)
				mCircleProgress.setVisibility(View.INVISIBLE);
	    	}


			@Override
			public void startImageTransacion(ImageTransaction imageTrans) {
				// TODO Auto-generated method stub
				LoadingImageView.this.startImageTransacion(imageTrans);
			}

			@Override
			public boolean onPreUiGetImage(int tid, Bitmap bitmap) {
				// TODO Auto-generated method stub
				return LoadingImageView.this.onPreUiGetImage(tid, bitmap);
			}

			@Override
			public void onUiGetImageNull(int tid, ImageView imageView) {
				// TODO Auto-generated method stub
				LoadingImageView.this.onUiGetImageNull(tid);
				if (mCircleProgress != null)
					mCircleProgress.setVisibility(View.INVISIBLE);
			}

			@Override
			public boolean isValid(int tid) {
				// TODO Auto-generated method stub
				return LoadingImageView.this.isValid(tid);
			}

			@Override
			public void doCancel() {
				// TODO Auto-generated method stub
				LoadingImageView.this.doCancel();
				if (mCircleProgress != null)
					mCircleProgress.setVisibility(View.INVISIBLE);
			}

			@Override
			public String onRedirectUrl(String originalUrl, String redirectUrl) {
				// TODO Auto-generated method stub
				return LoadingImageView.this.onRedirectUrl(originalUrl, redirectUrl);
			}

			@Override
			public String getCacheUrl(ImageTransaction imageTrans) {
				// TODO Auto-generated method stub
				return LoadingImageView.this.getCacheUrl(imageTrans);
			}
	    	
    }
    

	/**
     * check bitmap in memory cache
     * 
     * @param url
     * @return
     */
    public boolean checkCacheImage(String url) {
    	return checkCacheImage(url, -1, -1, -1, DefaultImageType);
    }
    
    /**
     * check bitmap in memory cache
     * 
     * @param url
     * @param width
     * @param height
     * @param roundSize
     * @param type
     * @return 
     */
    public boolean checkCacheImage(String url, int width, int height, 
    		int roundSize, ImageType type) {
    	doCancel();
    	
        url = makeServerClipUrl(null, url, mServerClipWidth, mServerClipHeight);// 添加裁剪参数
        
        Bitmap bitmap = ImageManager.getInstance().getImageCache(url, 
        		width, height, roundSize, type);
        
        boolean ret = false;
        if (bitmap != null) {
        	setImageBitmap(bitmap);
        	ret = true;
        }
        
        return ret;
    }

    /**
     * 设置裁剪服务器裁剪图片宽高， 服务器会保持原图宽高比，并匹配设置宽高的最大值进行裁剪， 若不确定宽高中的某一项可以设为0，
     * 至少指定宽高中一个值不为0才会进行裁剪。
     *
     * @param width
     * @param height
     */
    public void setServerClipSize(int width, int height) {
        mServerClipWidth = width;
        mServerClipHeight = height;
    }

    /**
     * 添加图片服务器裁剪参数
     *
     * @param url
     * @return
     */
//    public static String makeServerClipUrl(String orgUrl, String url, int clipWidth, int clipHeight) {
//        // 有设置任意裁剪参数时
////        Log.e("LoadingImageView","makeServerClipUrl 01 mServerClipWidth=" + mServerClipWidth + " mServerClipHeight=" + mServerClipHeight + " orgUrl=" + orgUrl + " url=" + url);
//        if (clipWidth > 0 || clipHeight > 0) {
//            // 存储在改服务器的图片才能进行裁剪
//            if ((orgUrl != null && orgUrl.indexOf("nos=1") > 0
//                    || url.startsWith("http://pic.x1.126.net/") || url
//                        .startsWith("http://nos.netease.com") || url.startsWith("http://yimg.nos.netease.com")) ||url.startsWith("http://163.fm/") && !url.contains("?resize=")) {
//                // 添加裁剪参数?resize=100x100
//                StringBuilder sb = new StringBuilder(url);
//                sb.append("?imageView&thumbnail=").append(clipWidth).append("x")
//                        .append(clipHeight);
//                sb.append("&quality=10");
//                // 返回添加参数的地址
////                Log.e("LoadingImageView","makeServerClipUrl 02  resendUrl = " + sb.toString());
//                return sb.toString();
//            }
//        }
//        // 返回原地址
//        return url;
//    }
    
    public static String makeServerClipUrl(String orgUrl, String url, int clipWidth, int clipHeight) {
    	if(clipWidth > 0 || clipHeight > 0) {
    		if(url != null && url.startsWith("http://yimg.nos.netease.com")) {
    			StringBuilder sb = new StringBuilder(url);
    			if(url.contains("?imageView")) {
    				sb.append("&thumbnail=").append(clipWidth).append("x").append(clipHeight);
    			} else {
    				sb.append("?imageView&thumbnail=").append(clipWidth).append("x").append(clipHeight);
    			}
    			return sb.toString();
    		}
    	}
    	return url;
    }
    
    /**
     * 设置是否需要加载图片出错的回调
     *
     * @param isNeed
     */
    public void setNeedLoadImageErrorCallBack(boolean isNeed) {
        mNeedLoadImageErrorCallBack = isNeed;
    }

    @Override
    public void startImageTransacion(ImageTransaction trans) {
        if (trans != null) {
            mImageTransRef = new WeakReference<ImageTransaction>(trans);
            mTid = trans.getId();
        }
    }

    /**
     * 判断是否需要调用下载出错时，即bitmap != null时的处理，默认不处理
     */
    @Override
    public boolean onPreUiGetImage(int tid, Bitmap bitmap) {
        return tid == mTid && ((mNeedLoadImageErrorCallBack || bitmap != null));
    }

    /**
     * 下载图片出错时的UI线程回调，需要打开mNeedLoadImageErrorCallBaack，用于处理某些情况下下载图片出错时载入出错图片
     *
     * @param tid
     */
    public void onUiGetImageNull(int tid) {
        if(mUiGetImageListener != null){
            mUiGetImageListener.onLoadImageError();
        }
    }
    
    private IUiGetImage mUiGetImageListener;
    
    public void setUiGetImageListener(IUiGetImage l){
        mUiGetImageListener = l;
    }

    @Override
    public void onUiGetImage(int tid, Bitmap bitmap) {
        boolean hasImage = false;
        
        // tid = -1时表示直接从缓存拿到了图片没有tid
        if (tid < 0) {
            if (bitmap == null){
                onUiGetImageNull(tid);
                if(mDefaultImgResId > 0){
                    setImageResource(mDefaultImgResId);
                }
                
                hasImage = false;
            }
            else{
                setImageBitmap(bitmap);
                
                if (mScaleTop) {
                	ImageViewUtil.setScaleMetrix(this, Scale.FIT_HEAD);
                }
                
                hasImage = true;
            }
        } 
        else if (mTid == tid) {
            if (bitmap == null){
                onUiGetImageNull(tid);
                if(mDefaultImgResId > 0){
                    setImageResource(mDefaultImgResId);
                }
                
                hasImage = false;
            }
            else{
				TransitionDrawable td = new TransitionDrawable(new Drawable[] {
						new ColorDrawable(0x0106000D),
						new BitmapDrawable(getResources(), bitmap)});

				setImageDrawable(td);
				td.startTransition(300);
//                setImageBitmap(bitmap);
                
				if (mScaleTop) {
                	ImageViewUtil.setScaleMetrix(this, Scale.FIT_HEAD);
                }
				
                hasImage = true;
            }
        }
        
        if(mUiGetImageListener != null){
            if(hasImage){
                mUiGetImageListener.onLoadImageFinish();
            }
            else{
                mUiGetImageListener.onLoadImageError();
            }
            
        }
    }

    @Override
    public boolean isValid(int tid) {
        return mTid == tid;
    }

    /**
     * 被回收后进行cancel处理
     */
    public void doCancel() {
        WeakReference<ImageTransaction> imageTransRef = mImageTransRef;
        mImageTransRef = null;
        mTid = 0;
        if (imageTransRef != null) {
            ImageTransaction trans = imageTransRef.get();
            if (trans != null) {
                trans.doCancel();
            }
        }
    }

    @Override
    public String onRedirectUrl(String originalUrl, String redirectUrl) {
//        Log.e("LoadingImageView","onRedirectUrl originalUrl = " + originalUrl + " redirectUrl " + redirectUrl + "mServerClipWidth" + mServerClipWidth);
        return makeServerClipUrl(originalUrl, redirectUrl, mServerClipWidth, mServerClipHeight);
    }
    
    public interface IUiGetImage {
        /** 开始加载图片 */
        public void onLoadImageStar(String url);
        /** 图片加载完成 */
        public void onLoadImageFinish();
        /** 图片加载出错 */
        public void onLoadImageError();
    }

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
    
    /**
     * 通过url的域名来判断是否公照
     * @param url
     * @return
     */
    private boolean isPublicPicture(String url) { 
    	if(url != null && url.startsWith("http://yimg.nos.netease.com")) {
    		return true;
    	}
    	return false;
    }
    
    @Override
    public boolean isTransport(ImageTransaction trans) {
    	return false;
    }
}
