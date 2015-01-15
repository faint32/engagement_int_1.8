package com.netease.android.widget;

import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.netease.android.video.util.VideoEditUtil;
import com.netease.date.R;

public class VideoEditAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private long mDuration;// 总时长
	private Context mContext;
	private long mFrameDuration;// 每个截图间隔时间
	private float mFrameWidth;// 截图宽度
	private int mFrameHeight;
	private int mCount;// 数量
	private String mPath;
//	private int mStartPosition;// 视频起始位置
//	private int mEndPosition;// 视频结束位置
	private static final long  SEC_DURATION = 1000*1000;//1s
	private ConcurrentHashMap<Integer, Bitmap> dataMap = new ConcurrentHashMap<Integer, Bitmap>();
	
	private Bitmap defaultBitmap;
	
	public VideoEditAdapter(Context context, int frameHeight,int frameWidth,String path,long duration) {
		mFrameDuration = 2*SEC_DURATION;//2s
		this.mFrameHeight = frameHeight;
		this.mFrameWidth = frameWidth;
		this.mDuration = duration;
		// mDuration单位毫秒
		this.mCount = mDuration % mFrameDuration > 0 ? 1 : 0;
		this.mCount += mDuration / mFrameDuration ;
		this.mContext = context;
//		this.mStartPosition=  startPosition;
//		this.mEndPosition=  endPosition;
		this.mPath = path;
//		this.mFrameHeight = frameHeight;
		mInflater = LayoutInflater.from(this.mContext);
//		mFrameDuration = mDuration / mCount - 1;
		
//		float screenWidth = ActivityUtils.getSnapshotWidth((Activity) mContext);
		// 计算listview宽度
//		float listViewWidth = (screenWidth - DpAndPxUtils.dip2px(
//				(Activity) mContext, 5 * 2));
//		mFrameWidth = listViewWidth / mCount;
//		mFrameWidth = frameWidth;
		BitmapDrawable drawable = (BitmapDrawable)mContext.getResources().getDrawable(R.drawable.video_listview_item);
		defaultBitmap = drawable.getBitmap();
		defaultBitmap = ThumbnailUtils.extractThumbnail(defaultBitmap, frameWidth, frameHeight);
	}

	public void addData(int index,Bitmap bitmap){
		dataMap.put(index, bitmap);
		this.notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return mCount;
	}

	@Override
	public Object getItem(int position) {
		return dataMap.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.video_listview_item, null);
			viewHolder.image = (ImageView) convertView
					.findViewById(R.id.video_listview_item);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

//		if (mContext instanceof VideoCoverActivity) {
//			viewHolder.image.setImageBitmap(((VideoCoverActivity) mContext)
//					.getScaleCenterVideoFrame(framePosition, mFrameHeight,
//							(int) mFrameWidth));
//		}
//		viewHolder.image.setImageResource(R.id.video_listview_item);
		viewHolder.image.setImageBitmap(defaultBitmap);
		
		Bitmap bitmap = dataMap.get(position);
		if(bitmap != null){
			viewHolder.image.setImageBitmap(bitmap);
		}
		else{
			new VideoFrameTask().execute(viewHolder.image,position,mFrameHeight,(int) mFrameWidth,position);
		}
//		viewHolder.image.setImageBitmap(VideoEditUtil.getScaleVideoFrame(mPath,framePosition,mFrameHeight,(int) mFrameWidth));
		return convertView;
	}

	class ViewHolder {
		private ImageView image;
	}
	
	
	private class VideoFrameTask extends AsyncTask<Object, Object, Bitmap> {
		private ImageView imageView;
		private Integer viewPosition;
		@Override
		protected Bitmap doInBackground(Object... params) {
			imageView = (ImageView)params[0];
			viewPosition = (Integer)params[1];
			
			long framePosition = viewPosition * mFrameDuration;
//			 第一张截图
			if (viewPosition == 0) {
				framePosition = 1;
			}
			
			// fix 3.0以下取到最后一张是黑色图,3.0以下不取最后一张
			if(VERSION.SDK_INT > VERSION_CODES.GINGERBREAD_MR1){
				// 最后一张截图
				if (viewPosition == mCount - 1) {
					framePosition = mDuration;
				}
			}
			
			Bitmap bitmap = dataMap.get(viewPosition);
			if(bitmap == null){
				bitmap = VideoEditUtil.getScaleVideoFrame(mPath,framePosition,mFrameHeight,(int) mFrameWidth);
				if(bitmap != null){
					dataMap.put(viewPosition, bitmap);
				}
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			super.onPostExecute(bitmap);
			imageView.setImageBitmap(bitmap);
//			VideoCoverAdapter.this.notifyDataSetChanged();
		}
	}
}
