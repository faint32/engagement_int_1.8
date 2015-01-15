package com.netease.engagement.image.explorer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.netease.date.R;
import com.netease.engagement.app.EngagementApp;
import com.netease.engagement.image.explorer.ExplorerPhotoType;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.image.explorer.IOnSelectPictureListener;
import com.netease.engagement.image.explorer.PhotoUploadActivity;
import com.netease.engagement.image.explorer.utils.BitmapUtil;
import com.netease.engagement.image.explorer.utils.ILoadBitmapCallback;


public class GalleryPhotoUploadAdapter extends BaseAdapter implements ILoadBitmapCallback{
	
	private LayoutInflater inflater;
	
	private IOnSelectPictureListener mListener;
	
	private int ownposition;
	
	private int mPicType ;
	
	public GalleryPhotoUploadAdapter(Context context) {
		inflater = LayoutInflater.from(context);
		mListener = (IOnSelectPictureListener) context;
	}
	
	public GalleryPhotoUploadAdapter(Context context,int picType) {
		inflater = LayoutInflater.from(context);
		mListener = (IOnSelectPictureListener) context;
		this.mPicType = picType ;
	}

	public int getOwnposition() {
		return ownposition;
	}

	public void setOwnposition(int ownposition) {
		this.ownposition = ownposition;
	}
	
	@Override
	public int getCount() {
	    return PhotoUploadActivity.getPicList().size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ownposition = position; 
		mListener.onSlidePictureSelected(ownposition);
		final ViewHolder viewHolder; 
		if(convertView == null) {
			convertView = inflater.inflate(R.layout.gallery_photo_item, null);
			viewHolder = new ViewHolder(); 
			viewHolder.mIv = (ImageView) convertView.findViewById(R.id.mIv);
			
			viewHolder.mCb = (CheckBox) convertView.findViewById(R.id.mCb);
			
			if(mPicType == EgmConstants.Photo_Type.TYPE_CHAT_PUBLIC_PIC){
				viewHolder.mCb.setVisibility(View.GONE);
			}
			
			viewHolder.mCb.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int pos = (Integer) v.getTag();
					if(viewHolder.mCb.isChecked() == true) {
						if(PhotoUploadActivity.getCheckedCount() < PhotoUploadActivity.getMaxCount()) {
						    boolean isSelect = mListener.onPictureSelected(pos);
                            if(!isSelect){  // 取消选中
                                viewHolder.mCb.toggle();   
                            }
						} 
						else {
							viewHolder.mCb.toggle();
							mListener.onOverMaxPictureSelected();
						}
					} else {
						mListener.onPictureDisseletced(pos);
						if(PhotoUploadActivity.getCheckedCount() == 0) {
							mListener.onNonePictureSelected();
						}
					}
				}
			});
			
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		viewHolder.mIv.setTag(position);
		viewHolder.mCb.setTag(position);
		
		long origId = PhotoUploadActivity.getPicList().get(position).getOrigId();
        String path = PhotoUploadActivity.getPicList().get(position).getPath();
        int max = EngagementApp.getAppInstance().getResources().getDisplayMetrics().widthPixels;
//        Log.e("TEST", "width" + max);
        
        BitmapUtil.getInstance().loadBitmap(origId, path, viewHolder.mIv, max, ExplorerPhotoType.GALLERY, this);
        
        viewHolder.mCb.setChecked(PhotoUploadActivity.getCheckSatus().get(position));
		
		return convertView;
	}
	
	static class ViewHolder {
		private ImageView mIv;
		private CheckBox mCb;
	}

	@Override
	public void dealBitmap(ImageView imageView, Bitmap bitmap) {
		imageView.setImageBitmap(bitmap);
	}
	
}
