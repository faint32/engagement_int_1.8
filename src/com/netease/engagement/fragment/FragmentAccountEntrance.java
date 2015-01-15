package com.netease.engagement.fragment;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.ScrollView;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.activity.ActivityLogin;
import com.netease.engagement.activity.ActivityRegisterEntrance;
import com.netease.engagement.activity.ActivityWelcome;
import com.netease.engagement.adapter.AccountRecommendListAdapter;
import com.netease.engagement.view.UserScrollView;
import com.netease.engagement.view.UserScrollView.UserScrollListener;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.LoginUserInfo;
import com.netease.service.protocol.meta.RecommendListInfo;
import com.netease.service.protocol.meta.RecommendUserInfo;

/**
 * 登录注册入口界面
 * 
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class FragmentAccountEntrance extends FragmentBase {

	private ActivityEngagementBase mActivity;
	private int mSexType = -1;
	private AccountRecommendListAdapter mAdapter;
	private ListView mListView;
	/** 用来把列表往上顶一定距离的 */
	private View mFootView;

	private UserScrollView mBlurBackground;
	private boolean isReverse = false;
	private int mScrollFactor = 5;

	private int mTid;

	public static FragmentAccountEntrance newInstance(int sexType) {
		FragmentAccountEntrance fragment = new FragmentAccountEntrance();
		fragment.setSexType(sexType);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mActivity = (ActivityEngagementBase) this.getActivity();
		EgmService.getInstance().addListener(mEgmCallback);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_account_entrance_layout,
				container, false);

		mBlurBackground = (UserScrollView) view.findViewById(R.id.blur_background);
		mBlurBackground.setUserScrollListener(new MyScrollListener());

		view.findViewById(R.id.entrance_register).setOnClickListener(
				mClickRegister);
		view.findViewById(R.id.entrance_login).setOnClickListener(mClickLogin);

		mAdapter = new AccountRecommendListAdapter(mActivity);

		mListView = (ListView) view.findViewById(R.id.account_rec_list);
		mFootView = new View(mActivity);
		mFootView.setLayoutParams(new AbsListView.LayoutParams(
				LayoutParams.MATCH_PARENT, getResources()
						.getDimensionPixelSize(
								R.dimen.reg_account_recommend_footer_height)));
		mListView.addFooterView(mFootView);
		mListView.setAdapter(mAdapter);
		mListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView arg0, int state) {

			}

			@Override
			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
				if(isReverse) {
					mBlurBackground.scrollBy(0, -mScrollFactor);
				} else {
					mBlurBackground.scrollBy(0, mScrollFactor);
				}
				
			}
		});

		getRecommendData();

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		CustomActionBar actionBar = ((ActivityEngagementBase) getActivity())
				.getCustomActionBar();
		actionBar.setMiddleTitle(R.string.app_name);
		actionBar.setLeftVisibility(View.INVISIBLE);
		actionBar.setRightVisibility(View.INVISIBLE);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		EgmService.getInstance().removeListener(mEgmCallback);
	}

	/** 点击登录 */
	private View.OnClickListener mClickLogin = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			ActivityLogin.startActivity(mActivity,
					EgmProtocolConstants.AccountType.Mobile);
		}
	};

	/** 点击注册 */
	private View.OnClickListener mClickRegister = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mSexType < 0) { // 没有性别，进入选择性别界面
				ActivityWelcome.startActivity(mActivity,
						ActivityWelcome.TYPE_REGISTER);
			} else { // 有性别，进入注册界面
				ActivityRegisterEntrance.startActivity(mActivity, mSexType);
			}
		}
	};

	private void setSexType(int type) {
		mSexType = type;
	}

	private void getRecommendData() {
		mTid = EgmService.getInstance().doGetRecommend(false);
	}

	private EgmCallBack mEgmCallback = new EgmCallBack() {
		/** 登录前的推荐列表 */
		@Override
		public void onGetRecommend(int transactionId,
				RecommendListInfo recommendListInfo) {
			if (mTid != transactionId)
				return;
			ArrayList<RecommendUserInfo> list = recommendListInfo.list;
			mAdapter.setDataList(list);
		}

		@Override
		public void onGetRecommendError(int transactionId, int errCode,
				String err) {
			if (mTid != transactionId)
				return;

			showToast(err);
			mListView.removeFooterView(mFootView);
		}

		@Override
		public void onLoginGetUserInfo(int transactionId,
				LoginUserInfo loginUserInfo) {
			// 登录或者注册成功了，那就结束帐号入口界面
			mActivity.finish();
		}
	};
	
	private class MyScrollListener implements UserScrollListener {

		@Override
		public boolean onInterceptTouchEvent(MotionEvent ev) {
			return false;
		}

		@Override
		public boolean onTouchEvent(MotionEvent ev) {
			return false;
		}

		@Override
		public void onScrollChanged(ScrollView view, int l, int t, int oldl,
				int oldt) {
			
			int scrollY = mBlurBackground.getScrollY();
			int height = mBlurBackground.getHeight();
			int scrollViewMeasureHeight = mBlurBackground.getChildAt(0).getMeasuredHeight();
			
			if(scrollY == 0) {
				isReverse = false;
			}
			if((scrollY + height) == scrollViewMeasureHeight) {
				isReverse = true;
			}	
		}
	}

}
