package com.netease.engagement.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityUserPage;
import com.netease.engagement.dataMgr.GiftInfoManager;
import com.netease.engagement.fragment.FragmentUserPageGirl;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.protocol.meta.GiftInfo;

public class GiftExploreView extends LinearLayout {
	
	private FragmentUserPageGirl fragment;
	
	private Context mContext;
	
	private ImageView giftIv;
	private TextView giftNameTv;
	private TextView giftPriceTv;
	
	private TextView giftTipsTv;
	private TextView sendGiftTv;
	
	private int mCurrentGiftId = 0;
	
	public GiftExploreView(Context context) {
		super(context);
		init(context);
	}
	
	public GiftExploreView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public GiftExploreView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context) {
		mContext = context;
		if(context instanceof ActivityUserPage){
			fragment = ((ActivityUserPage)context).getFragmentUserPageGirl();
		}
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_gift_single_explore_layout, this, true);
		
		giftIv = (ImageView) this.findViewById(R.id.gift_image);
		giftNameTv = (TextView) this.findViewById(R.id.gift_name);
		giftPriceTv = (TextView) this.findViewById(R.id.gift_price);
		
		giftTipsTv = (TextView) this.findViewById(R.id.gift_tip);
		sendGiftTv = (TextView) this.findViewById(R.id.send_gift);
		sendGiftTv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(fragment != null && fragment instanceof FragmentUserPageGirl) {
//					ToastUtil.showToast(mContext, "赠送礼物 id + " + mCurrentGiftId);
					if(mCurrentGiftId > 0) {
						fragment.doSendGift(String.valueOf(mCurrentGiftId));
						GiftInfoManager.reduceSpecialGift(mCurrentGiftId);
					}
				}
			}
		});

	}
	
	public void renderView(int giftId, int currentCrownId) {
		boolean mIsVip = ManagerAccount.getInstance().isVip();
		
		GiftInfo info = GiftInfoManager.getGiftInfoById(giftId);
		
		if (info == null) {
			giftNameTv.setText("");
			giftPriceTv.setText("金币");
			giftTipsTv.setText("");
		}
		else {
			String tip = null;
			
			if (! info.isVisible() && info.backupId > 0) {
				tip = mContext.getString(
						R.string.you_have_no_gift, info.name);
				info = GiftInfoManager.getGiftInfoById(info.backupId);
			}
			
			if (info.isCrown()) {
				GiftInfo curInfo = GiftInfoManager.getGiftInfoById(currentCrownId);
				if (curInfo != null && info.price < curInfo.price) {
					info = curInfo;
				}
			}
			else if (info.specialGift > 0 && ! info.isVisible()) {
				tip = mContext.getString(
						R.string.you_have_no_more_special_gift, info.name);
				info = GiftInfoManager.getGiftInfoById(105); // 没有那个特殊礼物的时候使用30的毛绒狗狗礼物代替
			}
			
			//设置gift名称
			String giftName = info.name;
			giftNameTv.setText(giftName);
			
			//设置gift金币额
			String giftPrice = String.valueOf(mIsVip ? info.vipPrice : info.price);
			
			giftPriceTv.setText(giftPrice + "金币");
			
			//设置tips
			if (! TextUtils.isEmpty(tip)) {
				giftTipsTv.setText(tip);
			}
			else if (info.isCrown()) {
				giftTipsTv.setText(R.string.single_gift_explore_crown_tip);
			}
			else {
				giftTipsTv.setText(mContext.getString(
						R.string.single_gift_explore_common_tip, 
						giftName, String.valueOf(info.usercp)));
			}
		}
		
		if (info != null) {
			giftId = info.id;
		}
		
		mCurrentGiftId = giftId;
		
		GiftInfoManager.setGiftInfo(giftId, info, giftIv);
	}
	
	@Override
	public boolean onTouchEvent(android.view.MotionEvent event) {
		super.onTouchEvent(event);
		return true;
	}
}
