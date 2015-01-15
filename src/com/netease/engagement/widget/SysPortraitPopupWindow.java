package com.netease.engagement.widget;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.view.PagerIndicator;
import com.netease.framework.widget.PopupWindowBase;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.SysPortraitInfo;
import com.netease.service.protocol.meta.SysPortraitListResult;

/**
 * 系统头像PopupWindow实现类
 * @author gordondu
 *
 */
public class SysPortraitPopupWindow extends PopupWindowBase {
	
	private ViewPager mViewPager;
	private PagerIndicator mIndicator;
	private SysPortraitPagerAdapter mAdapter;
	private TextView mConfirmButton;
	private LinearLayout mPanel;
	
	private SysPortraitInfo[] mSysPortraitInfoList;
	private int mGetSysPortraitTransId = -1, mUpdateSysPortraitTransId = -1;
	
	public SysPortraitPopupWindow(Activity context, SysPortraitInfo[] sysPortraitInfoList) {
		super(context);
		mSysPortraitInfoList = sysPortraitInfoList;
		if (mSysPortraitInfoList == null) {
			EgmService.getInstance().addListener(mCallBack);
			mGetSysPortraitTransId = EgmService.getInstance().doGetSysPortraitList();
		} else {
			resetChoosedStatus();
		}
	}

	@Override
	protected void initViews(RelativeLayout root) {
		ViewGroup layout = (ViewGroup)mInflater.inflate(R.layout.view_set_sysportrait_layout, root, true);
		mPanel = (LinearLayout)layout.findViewById(R.id.set_sysportrait_panel);
		mPanel.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_in_from_bottom));
		mPanel.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		mConfirmButton = (TextView) layout.findViewById(R.id.set_portrait);
		mConfirmButton.setOnClickListener(mOnClickListener);
		mViewPager = (ViewPager) layout.findViewById(R.id.sysportrait_pager);
		mIndicator = (PagerIndicator) layout.findViewById(R.id.sysportrait_indicator);
		mAdapter = new SysPortraitPagerAdapter(mContext, mIndicator);
		mViewPager.setAdapter(mAdapter);
		mViewPager.setOnPageChangeListener(mOnPageChangeListener);
	}
	
	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			SysPortraitInfo sysPortrait = getChoosedSysPortrait();
			if (sysPortrait != null) {
				EgmService.getInstance().addListener(mCallBack);
				mUpdateSysPortraitTransId = EgmService.getInstance().doUpdateSysPortrait(sysPortrait.id);
				showWatting(mContext.getString(R.string.setting_doing));
			} else {
				ToastUtil.showToast(mContext, R.string.pri_setportrait_is_empty);
			}
		}
	};
	
	private void resetChoosedStatus(){
		if (mSysPortraitInfoList != null) {
			for (int i=0; i<mSysPortraitInfoList.length; i++) {
				mSysPortraitInfoList[i].choosed = false;
			}
		}
	}
	
	private SysPortraitInfo getChoosedSysPortrait(){
		if (mSysPortraitInfoList != null) {
			for (int i=0; i<mSysPortraitInfoList.length; i++) {
				if (mSysPortraitInfoList[i].choosed) {
					return mSysPortraitInfoList[i];
				}
			}
		}
		return null;
	}
	
	
	private EgmCallBack mCallBack = new EgmCallBack(){
		@Override
		public void onGetSysPortraitListSucess(int transactionId, SysPortraitListResult obj) {
			if (mGetSysPortraitTransId == transactionId) {
				EgmService.getInstance().removeListener(mCallBack);
				if (obj != null) {
					mSysPortraitInfoList = obj.sysPortraitList;
					if (mAdapter != null) {
						mAdapter.notifyDataSetChanged();
					}
				} else {
					closeWindow();
					ToastUtil.showToast(mContext, R.string.pri_setportrait_getlist_is_empty);
				}
			}
		}
		
		@Override
		public void onGetSysPortraitListError(int transactionId, int errCode, String err) {
			if (mGetSysPortraitTransId == transactionId) {
				EgmService.getInstance().removeListener(mCallBack);
				closeWindow();
				ToastUtil.showToast(mContext, err);
			}
		}
		
		@Override
		public void onUpdateSysPortraitSucess(int transactionId, int code) {
			if (mUpdateSysPortraitTransId == transactionId) {
				EgmService.getInstance().removeListener(mCallBack);
				EgmService.getInstance().doGetPrivateData();
				closeWindow();
				ToastUtil.showToast(mContext, R.string.modify_success);
			}
		}
		
		@Override
		public void onUpdateSysPortraitError(int transactionId, int errCode, String err) {
			if (mUpdateSysPortraitTransId == transactionId) {
				EgmService.getInstance().removeListener(mCallBack);
				closeWindow();
				ToastUtil.showToast(mContext, err);
			}
		}
	};
	
	private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
		@Override
		public void onPageScrollStateChanged(int arg0) {
			
		}
		
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			
		}
		
		@Override
		public void onPageSelected(int arg0) {
			mIndicator.setCurrentItem(arg0);
		}
	};
	
	public class SysPortraitPagerAdapter extends PagerAdapter {

		private Context mContext;
		private PagerIndicator mIndecator;
		private int mPageCount = 0;

		public SysPortraitPagerAdapter(Context context, PagerIndicator indicator) {
			this.mContext = context;
			this.mIndecator = indicator;
			init();
		}
		
		private void init() {
			if (mSysPortraitInfoList != null) {
				mPageCount = Math.round(Float.valueOf(mSysPortraitInfoList.length) / SysPortraitGridView.PAGE_SIZE);
			}
			mIndecator.setCount(mPageCount);
			mIndecator.setCurrentItem(0);
		}

		@Override
		public void notifyDataSetChanged() {
			init();
			super.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mPageCount;
		}

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			return view == obj ? true : false;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			SysPortraitGridView gridView = new SysPortraitGridView(mContext, mSysPortraitInfoList);
			gridView.setPageNum(position);
			container.addView(gridView);
			return gridView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}
	}
}
