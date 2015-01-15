package com.netease.engagement.adapter;

import java.util.List;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.netease.engagement.activity.ActivityUserPage;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentUserPageGirl;
import com.netease.engagement.fragment.FragmentUserPageMan;
import com.netease.engagement.view.ImageFillLayout;
import com.netease.engagement.view.ImageFillLayout.IOnGiftListItemClickListener;
import com.netease.service.protocol.meta.GiftRecord;

public class UserGiftPagerAdapter extends PagerAdapter{

	private List<GiftRecord> mGiftRecordList ;
	private Context mContext ;
	private static final int PAGE_NUM = 8 ;
	private FragmentUserPageGirl mFragmentGirl;
	private FragmentUserPageMan mFragmentMan;
	private int sex;
	private boolean mEnable;
	
	public UserGiftPagerAdapter(Context context ,List<GiftRecord> mGiftRecordList, int sex){
		mContext = context ;
		this.mGiftRecordList = mGiftRecordList ;
		
		if(mContext instanceof ActivityUserPage) {
			if(sex == EgmConstants.SexType.Female) {
				mFragmentGirl = ((ActivityUserPage)mContext).getFragmentUserPageGirl();
			} else {
				mFragmentMan = ((ActivityUserPage)mContext).getFragmentUserPageMan();
			}
		}
		this.sex = sex;
	}
	
	public void setItemEnable(boolean value) {
		mEnable = value;
	}
	
	@Override
	public int getCount() {
		if(mGiftRecordList == null){
			return 0 ;
		}
		return mGiftRecordList.size()%PAGE_NUM == 0 ? mGiftRecordList.size()/PAGE_NUM : mGiftRecordList.size()/PAGE_NUM + 1;
	}
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		
		ImageFillLayout layout = new ImageFillLayout(mContext);
		layout.setItemEnable(mEnable);
		layout.fillLayout(mGiftRecordList, position);
		layout.setOnGiftListItemClickListener(new IOnGiftListItemClickListener() {
			@Override
			public void onClick(int giftId) {
				if(sex == EgmConstants.SexType.Female) {
					if (mFragmentGirl != null) {
						mFragmentGirl.showGiftExploreView(giftId);
					}
				} else {
					if (mFragmentMan != null) {
						mFragmentMan.showGiftExploreView(giftId);
					}
				}
			}
		});
		container.addView(layout);
		return layout ;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1 ? true : false ;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View)object);
	}
}
