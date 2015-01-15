package com.netease.engagement.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.WeakHashMap;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher.OnMatrixChangedListener;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;
import uk.co.senab.photoview.PhotoViewAttacher.OnViewTapListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.support.v4.view.PagerAdapter;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.netease.common.image.ImageType;
import com.netease.common.image.ImageViewAsyncCallback;
import com.netease.date.R;
import com.netease.engagement.widget.CircleProgress;
import com.netease.engagement.widget.LoadingImageView;

/**
 * 需要和ImageViewPager搭配使用，否则缩放时有异常
 * 
 * @author dingding
 *
 */
public class ImagePagerAdapter extends PagerAdapter {
	/**
	 * 图片url
	 */
	protected ArrayList<String> mImgUrls ;
	
	private RelativeLayout.LayoutParams lp ;
	protected int mScreenWidth ;
	protected int mScreenHeight ;
	
	private PhotoView mCurrentPhotoView;
	private float mCurrentScale = 1;
	private boolean mCurrentMatrixChangeed;
	
	private OnGestureListener mOnGestureListener;
	
	private WeakHashMap<View, PhotoView> mAttachers;
	private OnViewTapListener mViewTapListener;
	private OnPhotoTapListener mPhotoTapListener;
	private boolean mZoomable;
	private ScaleType mScaleType;
	private boolean mScaleTop;
	
	private boolean mInfinite;
	
	public ImagePagerAdapter(Context context ,
			ArrayList<String> imgUrls){
		this.mImgUrls = imgUrls ;
		mScreenWidth = context.getResources().getDisplayMetrics().widthPixels;
		mScreenHeight = context.getResources().getDisplayMetrics().heightPixels;
		lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 
				LayoutParams.MATCH_PARENT);
		
		mZoomable = true;
		mScaleType = ScaleType.FIT_CENTER;
		mAttachers = new WeakHashMap<View, PhotoView>();
	}
	
	public void setInfinite(boolean value) {
		mInfinite = value;
	}
	
	public void setScaleTop(boolean top) {
		mScaleTop = top;
	}
	
	public boolean isZoomable() {
		return mZoomable;
	}
	
	public void setZoomEnable(boolean value) {
		mZoomable = value;
		
		Collection<PhotoView> values = mAttachers.values();
		for (PhotoView attacher : values) {
			attacher.setZoomable(value);
		}
	}
	
	public void setOnGestureListener(OnGestureListener listener) {
		mOnGestureListener = listener;
	}
	
	public void setOnViewTapListener(OnViewTapListener listener) {
		mViewTapListener = listener;
		
		Collection<PhotoView> values = mAttachers.values();
		for (PhotoView attacher : values) {
			attacher.setOnViewTapListener(listener);
		}
	}
	
	public void setOnPhotoTapListener(OnPhotoTapListener listener) {
		mPhotoTapListener = listener;
		
		Collection<PhotoView> values = mAttachers.values();
		for (PhotoView attacher : values) {
			attacher.setOnPhotoTapListener(listener);
		}
	}
	
	public void addImageUrls(ArrayList<String> newImgUrls) {
		if (mImgUrls == null) {
			mImgUrls = newImgUrls;
		} else {
			mImgUrls.addAll(newImgUrls);
		}
		this.notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		if (mInfinite) {
			return mImgUrls == null ? 0 : Integer.MAX_VALUE;
		}
		else {
			return mImgUrls == null ? 0 : mImgUrls.size();
		}
	}
	
	public int getRealCount() {
		return mImgUrls == null ? 0 : mImgUrls.size();
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		FrameLayout root = (FrameLayout) View.inflate(container.getContext(), 
				R.layout.item_view_image_browser, null);
		
		position = position % getRealCount();
		
		RelativeLayout viewContainer = (RelativeLayout) root.findViewById(R.id.container);
		CircleProgress progressbar = (CircleProgress) root.findViewById(R.id.progressbar);
		
		renderView(root, viewContainer, progressbar, position);
		
		container.addView(root, 0);
		return root;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		View root = (View)object;
		mAttachers.remove(root);
		
		container.removeView(root);
	}
	
	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1 ? true : false ;
	}
	
	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		if (mAttachers != null) {
			mCurrentPhotoView = mAttachers.get(object);
		}
	}
	
	public boolean isCurrentScaling() {
		boolean ret = false;
		
//		if (mZoomable) {
			PhotoView view = mCurrentPhotoView;
			
			if (view != null) {
				if (mCurrentMatrixChangeed) {
					mCurrentScale = view.getScale();
					mCurrentMatrixChangeed = false;
				}
				
				ret = mCurrentScale != 1;
			}
//		}
		
		return ret;
	}
	
	public boolean setScale(float scale) {
		PhotoView view = mCurrentPhotoView;

		if (view != null) {
			if (view.getScale() != scale) {
				view.setScale(scale, true);
			}
		}
		
		return false;
	}
	
	public boolean setScaleZoom(float scale) {
		boolean ret = false;
		
		PhotoView view = mCurrentPhotoView;

		if (view != null) {
			if (scale > 1) {
				if (! view.canZoom() || view.getScale() != scale) {
					view.setZoomable(true);
					view.setScale(scale, true);
				}
			}
			else {
				if (view.canZoom() || view.getScale() != scale) {
					view.setZoomable(false);
					view.setScale(scale, true);
				}
			}
		}
		
		return ret;
	}
	
	protected void showImage(String url, final PhotoView image, 
			final CircleProgress progressbar) {
		image.setScaleHeight(mScreenHeight);
		image.setScaleType(mScaleType);
		image.setOnPhotoTapListener(mPhotoTapListener);
		image.setOnViewTapListener(mViewTapListener);
		image.setOnClickListener(null);
		
		url = LoadingImageView.makeServerClipUrl(null, url, mScreenWidth, 0);
		
		ImageViewAsyncCallback tag = new ImageViewAsyncCallback(image, progressbar,
				url, -1, -1, 0, ImageType.MemCache) {
			@Override
			public boolean onPreUiGetImage(int tid, Bitmap bitmap) {
				return true;
			}
			
			@Override
			public void onUiGetImageNull(int tid, ImageView imageView) {
				progressbar.setVisibility(View.GONE);
				
				image.setScaleType(ScaleType.CENTER);
                image.setImageResource(R.drawable.icon_photo_loaded_fail);
			}
			
			@Override
			public void onUiGetImage(int tid, Bitmap bitmap) {
				progressbar.setVisibility(View.GONE);
				image.setScaleType(mScaleType);
				
				super.onUiGetImage(tid, bitmap);
			}
			
			@Override
			public String onRedirectUrl(String originalUrl, String redirectUrl) {
				return LoadingImageView.makeServerClipUrl(originalUrl, 
						redirectUrl, mScreenWidth, 0);
			}
		};
		
		image.setTag(tag);
	}
	
	protected void renderView(View root, RelativeLayout container, 
			CircleProgress progressbar, int position) {
		final PhotoView image = new PhotoView(root.getContext());
		
		mAttachers.put(root, image);
		
		image.setZoomable(mZoomable);
		image.setMediumScale(2.0f);
		image.setMaximumScale(3.0f);
		image.setOnGestureListener(mOnGestureListener);
		image.setOnMatrixChangeListener(mMatrixChangedListener);
		
		String url = mImgUrls.get(position);
		
		showImage(url, image, progressbar);
		
		container.addView(image, lp);
	}
	
	private LayoutInflater mInflater;
	
	protected LayoutInflater getLayoutInflater(View view) {
		if (mInflater == null) {
			Context context = view.getContext();
			mInflater = (LayoutInflater) context.getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
		}
		
		return mInflater;
	}
	
	private OnMatrixChangedListener mMatrixChangedListener 
			= new OnMatrixChangedListener() {
		
		@Override
		public void onMatrixChanged(RectF rect) {
			mCurrentMatrixChangeed = true;
		}
	};

}