package com.netease.engagement.widget;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityPrivateSession;
import com.netease.engagement.activity.ActivityUserPage;
import com.netease.engagement.adapter.GiftKeyBoardAdapter;
import com.netease.engagement.dataMgr.GiftInfoManager;
import com.netease.engagement.dataMgr.MemoryDataCenter;
import com.netease.engagement.fragment.FragmentUserPageGirl;
import com.netease.engagement.view.PagerIndicator;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.protocol.meta.GiftInfo;

/**
 * 礼物键盘
 */
public class GiftKeyboardView extends LinearLayout{
	
	private TextView mGiftTip ;
	private LinearLayout mMarginLayout ; 
	
	private ProgressBar mLoading ;
	private ViewPager mViewPager ;
	private PagerIndicator mIndicator ;
	private PagerIndicator mIndicatorForChat ;
	
	private RelativeLayout mBottomLayout ;
	private RadioGroup mRadioGroup ;
	private TextView mTxtSend ;
	
	private PagerAdapter mAdapter ;
	private String[] mGroupNames ;
	private RadioGroup.LayoutParams rlp ;
	
	private Context mContext ;
	private FragmentUserPageGirl fragment ;
	
	private boolean forChat = false ;
	
	public GiftKeyboardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public GiftKeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public GiftKeyboardView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context){
		mContext = context ;
		if(mContext instanceof ActivityUserPage){
			fragment = ((ActivityUserPage)mContext).getFragmentUserPageGirl();
		}
		
		if(mContext instanceof ActivityPrivateSession){
			forChat = true ;
		}
		
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_gift_keyboard_layout,this,true);
		
		mGiftTip = (TextView)this.findViewById(R.id.send_gift_tip);
		mMarginLayout = (LinearLayout)this.findViewById(R.id.margin_layout);
		mIndicator = (PagerIndicator)this.findViewById(R.id.gift_indicator);
		mIndicatorForChat = (PagerIndicator)this.findViewById(R.id.gift_indicator_chat);
		mBottomLayout = (RelativeLayout)this.findViewById(R.id.send_bottom_layout);
		mRadioGroup = (RadioGroup)this.findViewById(R.id.gift_group);
		mTxtSend = (TextView)this.findViewById(R.id.txt_send);
		mTxtSend.setOnClickListener(mOnClickListener);
		mLoading = (ProgressBar)this.findViewById(R.id.loading);
		
		mViewPager = (ViewPager) this.findViewById(R.id.gift_pager);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener(){
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			@Override
			public void onPageSelected(int arg0) {
				refreshIndicator(arg0);
			}
		});
		renderView();
	}
	
	public void renderView(){
		mGiftTip.setVisibility(View.INVISIBLE);
		mIndicator.setVisibility(View.GONE);
		mRadioGroup.setVisibility(View.INVISIBLE);
		mTxtSend.setVisibility(View.INVISIBLE);
		mLoading.setVisibility(View.VISIBLE);
		
//		SpecialGift[] specialGifts = (SpecialGift[]) MemoryDataCenter.getInstance().get(MemoryDataCenter.SPECIALGIFTS);
		
//		if(GiftInfoManager.getInstance().initData()){
			mGiftTip.setVisibility(View.VISIBLE);
			mRadioGroup.setVisibility(View.VISIBLE);
			mTxtSend.setVisibility(View.VISIBLE);
			mLoading.setVisibility(View.INVISIBLE);
			
			mGroupNames = GiftInfoManager.getGroupNames();
			
    		mAdapter = new GiftKeyBoardAdapter(mContext ,mGroupNames,forChat);
    		mViewPager.setAdapter(mAdapter);
			
			//初始化
			int firstPageCount = ((GiftKeyBoardAdapter)mAdapter).getFirstPageCount();
			int secondPageCount = ((GiftKeyBoardAdapter)mAdapter).getSecondPageCount();
			if(!forChat) {
				if(firstPageCount + secondPageCount > 1){
					mIndicator.setVisibility(View.VISIBLE);
					mIndicator.setCount(firstPageCount + secondPageCount);
					mIndicator.setCurrentItem(0);
				}
				mIndicatorForChat.setVisibility(View.GONE);
			} else {
				mIndicator.setVisibility(View.GONE);
				if(firstPageCount+secondPageCount > 1) {
					mIndicatorForChat.setVisibility(View.VISIBLE);
					mIndicatorForChat.setCount(firstPageCount+secondPageCount);
					mIndicatorForChat.setCurrentItem(0);
				}
			}
			
			
			if(mGroupNames.length >= 2){
				mRadioGroup.setVisibility(View.VISIBLE);
				mRadioGroup.removeAllViews();
				rlp = new RadioGroup.LayoutParams(0, LayoutParams.MATCH_PARENT);
				rlp.weight = 1.0f;
				for (int i = 0; i < mGroupNames.length; i++) {
					RadioButton rb = (RadioButton)((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_item_radio_button, null);
					if(i == 0) {
						rb.setChecked(true);
					}
					rb.setId(i);
					rb.setText(mGroupNames[i]);
					mRadioGroup.addView(rb, rlp);
					rb.setOnClickListener(listener);
				}
				mRadioGroup.check(0);
			}
//		}
		
		if(forChat){
			mGiftTip.setVisibility(View.GONE);
			mMarginLayout.setVisibility(View.GONE);
			mBottomLayout.setVisibility(View.GONE);
		}else{
			initUserPageSendGift(0);
		}
	}
	
	private void initUserPageSendGift(int index) {
		if (index == 0) {
			mGiftTip.setText(getContext().getString(R.string.send_gift_tip));
		} else {
			int crownId = (Integer) (MemoryDataCenter.getInstance()
					.get(MemoryDataCenter.CURRENT_COMPARE_CROWNID));
			
			GiftInfo expensive = GiftInfoManager.getMostExpensiveCrown();
			
			if (crownId == 0) {
				mGiftTip.setText(getContext().getString(R.string.to_send_crown));
			} else if (expensive == null) {
				mGiftTip.setText(getContext()
						.getString(R.string.send_huanguan_tip, ""));
			} else if (expensive.id == crownId) {
				mGiftTip.setText(getContext().getString(
						R.string.send_huanguan_most, expensive.name));
			} else {
				mGiftTip.setText(getContext()
						.getString(R.string.send_huanguan_tip, expensive.name));
			}
		}
	}

	/**
	 * 为了控制左右滑动切换和点击切换之间的矛盾，不使用OnCheckedChangeListener
	 */
	private OnClickListener listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			int firstPageCount = ((GiftKeyBoardAdapter)mAdapter).getFirstPageCount();
			switch(v.getId()){
				case 0:
					mRadioGroup.check(0);
					initUserPageSendGift(0);
					mViewPager.setCurrentItem(0);
					break;
				case 1:
					mRadioGroup.check(1);
					initUserPageSendGift(1);
					mViewPager.setCurrentItem(firstPageCount);
					break;
				}
		}
	};
	
	private void refreshIndicator(int pageNo){
		int firstPageCount = ((GiftKeyBoardAdapter)mAdapter).getFirstPageCount();
//		int secondPageCount = ((GiftKeyBoardAdapter)mAdapter).getSecondPageCount();
		
		if(!forChat) {
			int index = 0 ;
			if(pageNo < firstPageCount){
				index = 0 ;
			}else{
				index = 1 ;
			}
	
			if(index == 0){
				mRadioGroup.check(0);

			}else{
				mRadioGroup.check(1);
			}
			mIndicator.setCurrentItem(pageNo);
			
			initUserPageSendGift(index);
			
			mIndicatorForChat.setVisibility(View.GONE);
		} else {
			mIndicatorForChat.setCurrentItem(pageNo);
			mIndicator.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 获取到用户信息后刷新礼物键盘
	 */
	public void refreshViewPager(){
		mViewPager.getAdapter().notifyDataSetChanged();
	}
	
	/**
	 * 发送礼物后刷新礼物列表状态
	 */
	public void refreshGiftState(){
		((GiftKeyBoardAdapter)mAdapter).refreshPager(mViewPager, null);
	}
	
	/**
	 * 点击发送按钮
	 * 女性个人主页发送礼物
	 */
	private OnClickListener mOnClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			GiftInfo info = GiftInfoManager.getChoosedGiftInfo();
			if(info != null){
				if(fragment != null){
					fragment.doSendGift(String.valueOf(info.id));
					
					GiftInfoManager.reduceSpecialGift(info.id);
					refreshGiftState();
				}
			}else{
				ToastUtil.showToast(mContext,"请选择要赠送的礼物");
			}
		}
	};
	
	@Override
	public boolean onTouchEvent(android.view.MotionEvent event) {
		super.onTouchEvent(event);
		return true;
	}
}
