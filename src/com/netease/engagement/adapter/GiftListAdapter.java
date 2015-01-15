package com.netease.engagement.adapter;

import java.util.List;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.dataMgr.GiftDownLoadManager;
import com.netease.engagement.dataMgr.GiftInfoManager;
import com.netease.service.protocol.meta.GiftInfo;
import com.netease.service.protocol.meta.GiftRecord;

/**
 * 礼物列表adapter
 */
public class GiftListAdapter extends BaseAdapter{
	
    private LayoutInflater mInflater ;
    private LinearLayout.LayoutParams lp ;
    private List<GiftRecord> mGiftList ;
    
	public GiftListAdapter(Context context,int columnWidth,List<GiftRecord> records){
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		lp = new LinearLayout.LayoutParams(columnWidth,columnWidth);
		mGiftList = records ;
	}
	@Override
	public int getCount() {
		return mGiftList == null ? 0 : mGiftList.size();
	}
	@Override
	public Object getItem(int position) {
		return mGiftList == null ? null : mGiftList.get(position);
	}
	@Override
	public long getItemId(int position) {
		return position; 
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null ;
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.view_item_gift_list,null);
			holder = new ViewHolder();
			holder.mImage = (ImageView) convertView.findViewById(R.id.gift_image);
			holder.mImage.setLayoutParams(lp);
			holder.mText = (TextView) convertView.findViewById(R.id.from_user_name);
			convertView.setTag(holder);
		}
		
		holder = (ViewHolder) convertView.getTag();
		
		GiftInfo info = mGiftList.get(position).giftInfo;
		GiftInfoManager.setGiftInfo(info.id, info, holder.mImage);
		
		holder.mText.setText(mGiftList.get(position).fromUserName);
		return convertView;
	}
	
	class ViewHolder{
		ImageView mImage;
		TextView mText ;
	}
}
