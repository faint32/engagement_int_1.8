package com.netease.engagement.view;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.app.EngagementApp;
import com.netease.engagement.dataMgr.GiftInfoManager;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.protocol.meta.GiftInfo;
import com.netease.service.protocol.meta.GiftRecord;

public class ImageFillLayout extends AutoFillLayout{
	
	private IOnGiftListItemClickListener mOnGiftItemClickListener;
	
	public ImageFillLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public ImageFillLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ImageFillLayout(Context context) {
		super(context);
		init(context);
	}
	
	
	private int mItemHeight ;
	private ViewGroup.LayoutParams lp ;
	private int mPadding = 10 ;
	private float mDensity;
	
	private boolean mEnable;
	
	private void init(Context context){
		mPadding = EgmUtil.dip2px(context,mPadding);
		mItemHeight = context.getResources().getDisplayMetrics().widthPixels/4 ;
		lp = new ViewGroup.LayoutParams(mItemHeight, mItemHeight);
		mDensity = EngagementApp.getAppInstance().getResources().getDisplayMetrics().density;
		super.setColums(4);
	}
	
	public void setItemEnable(boolean enable) {
		mEnable = enable;
	}

	@Override
	public int getItemHeight() {
		return mItemHeight ;
	}
	
	private static final int PAGE_NUM = 8 ;
	
	public void fillLayout(List<GiftRecord> list , int pageNo){
		removeAllViews();
		int startIndex = pageNo * PAGE_NUM ;
		for(int i = startIndex ; i < startIndex + PAGE_NUM ; i++){
			if(i < list.size()){
				final int giftId = list.get(i).giftInfo.id;
				GiftListItemView item = getItemView(list.get(i).giftInfo);
				item.setEnabled(mEnable);
				item.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if(mOnGiftItemClickListener != null) {
							mOnGiftItemClickListener.onClick(giftId);
						}
					}
				});
				
				addView(item);
			}else{
				break ;
			}
		}
	}
	
	private GiftListItemView getItemView(GiftInfo info) {
		GiftListItemView view = new GiftListItemView(getContext());
		view.setLayoutParams(lp);
		
		GiftInfoManager.setGiftInfo(info.id, info, view.giftIv);
		
		view.setText((info != null) ? String.valueOf(info.usercp) : "");
		return view;
	}
	
	private class GiftListItemView extends RelativeLayout {
		public ImageView giftIv;
		public TextView intimacyTv;
		
		public GiftListItemView(Context context) {
			super(context);
			init();
		}
		
		public GiftListItemView(Context context, AttributeSet attrs) {
			super(context, attrs);
			init();
		}
		
		private void init() {
			giftIv = new ImageView(getContext());
			intimacyTv = new TextView(getContext());
			
			LayoutParams imageViewLP = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			imageViewLP.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			imageViewLP.addRule(RelativeLayout.CENTER_HORIZONTAL);
			imageViewLP.setMargins(0, 10, 0, 50);
			addView(giftIv, imageViewLP);
			
			LayoutParams textViewLP = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			textViewLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			textViewLP.addRule(RelativeLayout.CENTER_HORIZONTAL);
			addView(intimacyTv, textViewLP);
			intimacyTv.setGravity(Gravity.CENTER);
			intimacyTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, 12*mDensity);
			intimacyTv.setTextColor(getResources().getColor(R.color.purple_dark));
			intimacyTv.setBackgroundResource(R.drawable.btn_user_page_gift_intimacy_selector);
		}
		
		public void setText(String intimacy) {
			intimacyTv.setText("亲密度" + intimacy);
		}
	}
	
	public interface IOnGiftListItemClickListener {
		public void onClick(int giftId);
	}
	
	public void setOnGiftListItemClickListener(IOnGiftListItemClickListener l) {
		mOnGiftItemClickListener = l;
	}
}
