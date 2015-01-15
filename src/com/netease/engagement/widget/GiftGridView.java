package com.netease.engagement.widget;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.dataMgr.GiftInfoManager;
import com.netease.engagement.dataMgr.MemoryDataCenter;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.protocol.meta.GiftInfo;

public class GiftGridView extends GridView{
	//列数
	private static final int COLUMN_NUM = 4 ;
	private int mScreenWidth ;
	private int mHoriSpacing = 5 ;
	private int mVertSpacing = 5 ;
	//每页个数
	private static final int PAGE_NUM = 8 ;
	//当前在哪一页，从0开始
	private int mPageNum ;
	private ArrayList<GiftInfo> mGiftInfoList ;
	private String mGroupName ;
	
	private GiftGridAdapter mAdapter ;
	private int mItemWidth ;
	private RelativeLayout.LayoutParams lp ;
	
	private int mCrownPrice ;
	
	public GiftGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public GiftGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public GiftGridView(Context context) {
		super(context);
		init();
	}
	
	public GiftGridView(Context context ,String groupName){
		super(context);
		this.mGroupName = groupName ;
		init();
	}
	
	private void init(){
		this.setGravity(Gravity.CENTER);
		this.setNumColumns(COLUMN_NUM);
		this.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
		
		mScreenWidth = getContext().getResources().getDisplayMetrics().widthPixels ;
		mHoriSpacing = EgmUtil.dip2px(getContext(),mHoriSpacing);
		mVertSpacing = EgmUtil.dip2px(getContext(),mVertSpacing);
		
		mItemWidth = mScreenWidth/COLUMN_NUM ;
		lp = new RelativeLayout.LayoutParams(mItemWidth , mItemWidth);
		
		this.setHorizontalSpacing(mHoriSpacing);
		this.setVerticalSpacing(mVertSpacing);
		
		int crownId = (Integer)(MemoryDataCenter.getInstance().get(MemoryDataCenter.CURRENT_COMPARE_CROWNID));
		mCrownPrice = GiftInfoManager.getCrownPriceById(crownId);
			
		mAdapter = new GiftGridAdapter();
		this.setAdapter(mAdapter);
	}
	
	public void setPageNum(int pageNum){
		mPageNum = pageNum ;
	}
	
	public void setGiftInfoList(ArrayList<GiftInfo> list){
		mGiftInfoList = list ;
	}
	
	public GiftGridAdapter getGiftAdapter(){
		if(mAdapter != null){
			return mAdapter ;
		}
		return null ;
	}
	
	public class GiftGridAdapter extends BaseAdapter{
		
		private LayoutInflater inflater ;
		
		private boolean mIsVip;
		
		private GiftInfo mChoosed;
		
		public GiftGridAdapter(){
			inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mIsVip = ManagerAccount.getInstance().isVip();
		}
		
		public void setChoosed(GiftInfo info) {
			mChoosed = info;
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
			return PAGE_NUM;
		}

		@Override
		public Object getItem(int position) {
			//获取当前文件名
			int index = mPageNum * PAGE_NUM + position ;
			if(mGiftInfoList != null){
				if(index >= 0 && index < mGiftInfoList.size()){
					return mGiftInfoList.get(index);
				}
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null ;
			if(convertView == null){
				convertView = inflater.inflate(R.layout.view_item_gift_grid,null);
				holder = new ViewHolder();
				holder.gift_image = (ImageView)convertView.findViewById(R.id.gift_image);
				holder.gift_image.setLayoutParams(lp);
				holder.gift_tag = (TextView)convertView.findViewById(R.id.choosed_tag);
				holder.gift_name = (TextView)convertView.findViewById(R.id.gift_name);
				holder.gift_price = (TextView)convertView.findViewById(R.id.gift_price);
				convertView.setTag(holder);
			}
			holder = (ViewHolder) convertView.getTag();
			holder.gift_tag.setVisibility(View.GONE);
			
			int listPos = mPageNum * PAGE_NUM + position ;
			if(listPos >= 0 && listPos < mGiftInfoList.size()){
			    GiftInfo info = mGiftInfoList.get(listPos);
			    
			    GiftInfoManager.setGiftInfo(info.id, info, holder.gift_image);
				
				if(info.isCrown() && info.price < mCrownPrice){
					ColorMatrix colorMatrix = new ColorMatrix();
					colorMatrix.setSaturation(0); 
					ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(colorMatrix); 
					holder.gift_image.setColorFilter(colorMatrixFilter);
				}
				else {
					holder.gift_image.setColorFilter(null);
				}
				
			    holder.gift_name.setText(info.name);
                if(info.specialGift > 0){
                    holder.gift_price.setText("免费, 剩" + info.times + "个");
                }else{
                	int price = 0;
                	if(mIsVip) {
                		price = info.vipPrice;
                	} else {
                		price = info.price;
                	}
                	
                	holder.gift_price.setText(String.format(
                			getContext().getResources().getString(R.string.coin_unit), 
                			price));
                }
                
                if (mChoosed != null && info.id == mChoosed.id) {
                    holder.gift_tag.setVisibility(View.VISIBLE);
                }
			}
			else{
				holder.gift_image.setImageBitmap(null);
				holder.gift_tag.setVisibility(View.GONE);
				holder.gift_name.setText("");
				holder.gift_price.setVisibility(View.GONE);
			}
			return convertView;
		}
		
		class ViewHolder{
			ImageView gift_image ;
			TextView gift_tag ;
			TextView gift_name ;
			TextView gift_price ;
		}
	}
}
