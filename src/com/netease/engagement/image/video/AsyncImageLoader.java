package com.netease.engagement.image.video;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import com.netease.engagement.app.EngagementApp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.TextUtils;

/**
 * 视频缩略图异步加载
 */
public class AsyncImageLoader {
	
	private static AsyncImageLoader mLoader ;
	private AsyncImageLoader(){}
	
	public static AsyncImageLoader getInstance(){
		if(mLoader == null){
			mLoader = new AsyncImageLoader();
		}
		return mLoader ;
	}
	
	//图片内存缓存
	private static HashMap<String,SoftReference<Bitmap>> mImageCache
		= new HashMap<String,SoftReference<Bitmap>> ();
	
	/**
	 * 加载图片
	 * @param filePath
	 * @param callback
	 * @return
	 */
	public Bitmap loadBitmap(final String filePath ,final String videoId ,final ImageLoadCallBack callback){
		Bitmap result = null ;
		
		final Handler handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				Bitmap bitmap = (Bitmap) msg.obj ;
				if(bitmap != null && callback != null){
					callback.onImageLoaded(bitmap);
				}
				super.handleMessage(msg);
			}
		};
		
		if(mImageCache.containsKey(filePath)){
			SoftReference<Bitmap> reference = mImageCache.get(filePath);
			if(reference != null && reference.get() != null){
				result = reference.get();
			}
		}else{
			//TODO 改为线程池实现
			new Thread(new Runnable(){
				@Override
				public void run() {
					Bitmap bmp = getVideoThumb(filePath,videoId);
					mImageCache.put(filePath, new SoftReference<Bitmap>(bmp));
					Message msg = handler.obtainMessage();
					msg.obj = bmp ;
					handler.sendMessage(msg);
				}
			}).start();
		}
		return result ;
	}
	
	/**
	 * 获取视频文件缩略图
	 * @param filePath
	 * @return
	 */
	private Bitmap getVideoThumb(String filePath,String videoId){
		Bitmap thumb = null ;
		if(!TextUtils.isEmpty(filePath)){
			/*thumb = ThumbnailUtils.createVideoThumbnail(filePath,
					MediaStore.Video.Thumbnails.MICRO_KIND);*/
			thumb = ThumbnailUtils.createVideoThumbnail(filePath,
					MediaStore.Video.Thumbnails.MINI_KIND);
			if(thumb == null){
				BitmapFactory.Options options = new BitmapFactory.Options();    
		        options.inDither = false;    
		        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
				thumb = MediaStore.Video.Thumbnails.getThumbnail(
						EngagementApp.getAppInstance().getContentResolver(), 
						Long.parseLong(videoId),
						Images.Thumbnails.MINI_KIND, 
						options);
			}
		}
		return thumb ;
	}
	
	/**
	 * 清除数据
	 */
	public void clear(){
		if(mImageCache != null){
			mImageCache.clear();
		}
		if(mLoader != null){
			mLoader = null ;
		}
	}
}
