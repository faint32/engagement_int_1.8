package com.netease.engagement.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.internal.ViewCompat;
import com.netease.date.R;
import com.netease.engagement.fragment.FragmentEditPhoto;
import com.netease.engagement.widget.LoadingImageView;
import com.netease.service.protocol.meta.PictureInfo;

public class PhotoListAdapter extends BaseAdapter {
	private List<PictureInfo> mPicList;
	private Context context;
	private GridView.LayoutParams lp;
	private LayoutInflater mInflater ;
	private int itemWidth ;
	//私照、公开照标记
	private boolean isPrivate ;

	public PhotoListAdapter(Context context, List<PictureInfo> picList,int itemWidth,boolean isPrivate) {
		this.context = context;
		this.mPicList = picList;
		this.itemWidth  = itemWidth ;
		this.isPrivate = isPrivate ;
		
		lp = new GridView.LayoutParams(itemWidth, itemWidth);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		switch (FragmentEditPhoto.mMode) {
			case FragmentEditPhoto.BROWSE_MODE:
				return mPicList == null ? 1 : mPicList.size() + 1;
			case FragmentEditPhoto.EDIT_MODE:
				return mPicList == null ? 0 : mPicList.size();
		}
		return mPicList == null ? 0 : mPicList.size();
	}

	@Override
	public Object getItem(int position) {
		switch (FragmentEditPhoto.mMode) {
			case FragmentEditPhoto.BROWSE_MODE:
				if (position == 0) {
					return new RelativeLayout(context);
				}
				return mPicList == null ? null : mPicList.get(position - 1);
			case FragmentEditPhoto.EDIT_MODE:
				return mPicList == null ? null : mPicList.get(position);
		}
		return mPicList == null ? null : mPicList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null ;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_view_photo_list, null);
			holder = new ViewHolder();
			holder.imageView = (LoadingImageView) convertView.findViewById(R.id.photo);
			holder.imageView.setScaleType(ScaleType.CENTER_CROP);
			holder.imageView.setScaleTop(true);
			
			holder.imageView.setDefaultResId(R.drawable.icon_photo_loaded_fail);
			holder.unselected = (ImageView)convertView.findViewById(R.id.unselected);
			holder.selected = (ImageView)convertView.findViewById(R.id.selected);
			holder.unlockCountContainer = (LinearLayout) convertView.findViewById(R.id.unlock_count_container);
			holder.unlockCount = (TextView) convertView.findViewById(R.id.unlock_count);
			convertView.setLayoutParams(lp);
			convertView.setTag(holder);
		}
		holder = (ViewHolder) convertView.getTag();
		
		switch (FragmentEditPhoto.mMode) {
			case FragmentEditPhoto.BROWSE_MODE:
				holder.unselected.setVisibility(View.GONE);
				holder.selected.setVisibility(View.GONE);
				if(position == 0){
					holder.unlockCountContainer.setVisibility(View.GONE);
					if(isPrivate){
						ViewCompat.setBackground(holder.imageView, 
								context.getResources().getDrawable(R.drawable.bg_add_private_photo_transparent));
					}else{
						ViewCompat.setBackground(holder.imageView, 
								context.getResources().getDrawable(R.drawable.bg_add_public_photo_transparent));
					}
					holder.imageView.setImageBitmap(null);
				}else{
					ViewCompat.setBackground(holder.imageView,
							context.getResources().getDrawable(R.drawable.bg_photo_placeholder));
					holder.imageView.setImageBitmap(null);
					holder.imageView.setServerClipSize(itemWidth, itemWidth);
					PictureInfo picInfo = mPicList.get(position - 1);
					if(isPrivate){
						holder.unlockCount.setText(String.valueOf(picInfo.unlockCount));
						holder.unlockCountContainer.setVisibility(View.VISIBLE);
						holder.imageView.setLoadingImage(picInfo.smallPicUrl);
					}else{
						holder.unlockCountContainer.setVisibility(View.GONE);
						holder.imageView.setLoadingImage(picInfo.picUrl);
					}
				}
				break;
				
			case FragmentEditPhoto.EDIT_MODE:
				ViewCompat.setBackground(holder.imageView,context.getResources().getDrawable(R.drawable.bg_photo_placeholder));
				holder.imageView.setImageBitmap(null);
				holder.unselected.setVisibility(View.VISIBLE);
				holder.selected.setVisibility(View.GONE);
				holder.unlockCountContainer.setVisibility(View.GONE);
				holder.imageView.setServerClipSize(itemWidth, itemWidth);
				if(isPrivate){
					holder.imageView.setLoadingImage(mPicList.get(position).smallPicUrl);
				}else{
					holder.imageView.setLoadingImage(mPicList.get(position).picUrl);
				}
				break;
		}
		return convertView;
	}
	
	class ViewHolder{
		LoadingImageView imageView ;
		ImageView unselected ;
		ImageView selected ;
		LinearLayout unlockCountContainer;
		TextView unlockCount;
	}
}
