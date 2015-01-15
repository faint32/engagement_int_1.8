package com.netease.engagement.image.explorer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.image.explorer.ExplorerPhotoType;
import com.netease.engagement.image.explorer.IOnSelectPictureListener;
import com.netease.engagement.image.explorer.PhotoUploadActivity;
import com.netease.engagement.image.explorer.utils.BitmapUtil;
import com.netease.engagement.image.explorer.utils.ILoadBitmapCallback;

public class GridPhotoUploadAdapter extends BaseAdapter implements ILoadBitmapCallback {
	
	private LayoutInflater inflater;
	
	private IOnSelectPictureListener mListener;
	
	private Bitmap placeHolder;
	
	private OnClickListener onGridPhotoClickListener;
	
	private int mPictureType;
	
	public GridPhotoUploadAdapter(Context context, OnClickListener onGridPhotoClickListener, int type) {
        this.mListener = (IOnSelectPictureListener) context;
        inflater = LayoutInflater.from(context);
        placeHolder = BitmapFactory.decodeResource(context.getResources(), R.drawable.bg_photo_placeholder_170x170);
        this.onGridPhotoClickListener = onGridPhotoClickListener;
        mPictureType = type;
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
        final ViewHolder viewHolder; 
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.grid_photo_upload_item, null);
            viewHolder = new ViewHolder(); 
            viewHolder.mIv = (ImageView) convertView.findViewById(R.id.mIv);
            viewHolder.mIv.setOnClickListener(onGridPhotoClickListener);
            
            viewHolder.mCb = (CheckBox) convertView.findViewById(R.id.mCb);
            
            if(mPictureType == EgmConstants.Photo_Type.TYPE_AVATAR){
                viewHolder.mCb.setVisibility(View.INVISIBLE);
            }
            else{
                viewHolder.mCb.setVisibility(View.VISIBLE);
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
            }
            convertView.setTag(viewHolder);
        } 
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        
        viewHolder.mIv.setTag(position);
        viewHolder.mCb.setTag(position);
        
        long origId = PhotoUploadActivity.getPicList().get(position).getOrigId();
        String path = PhotoUploadActivity.getPicList().get(position).getPath();
        int max = placeHolder.getWidth();
        
        if(viewHolder.mIv.getContentDescription()!=null && !(path.equalsIgnoreCase(viewHolder.mIv.getContentDescription().toString()))) {
        	viewHolder.mIv.setImageBitmap(placeHolder);
        	BitmapUtil.getInstance().loadBitmap(origId, path, viewHolder.mIv, max, ExplorerPhotoType.GRID, this);
        }
        
        
        viewHolder.mCb.setChecked(PhotoUploadActivity.getCheckSatus().get(position));
        
        return convertView;
    }

    static class ViewHolder {
        private ImageView mIv;
        private CheckBox mCb;
    }

    @Override
    public void dealBitmap(ImageView imageView, Bitmap bitmap) {
        if(bitmap != null) {
        	imageView.setImageBitmap(bitmap);
//            Bitmap cropBitmap = BitmapUtil.getInstance().cropBitmapFix(bitmap, placeHolder.getWidth(), placeHolder.getHeight());
//            if(cropBitmap != null) {
//                imageView.setImageBitmap(cropBitmap);
//            }
        }
        
    }
}
