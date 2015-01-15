package com.netease.engagement.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityLevelTable;
import com.netease.engagement.adapter.GiftListAdapter;
import com.netease.engagement.app.EgmConstants;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.GiftRecord;
import com.netease.service.protocol.meta.GiftRecords;
import com.netease.service.protocol.meta.UserInfo;
import com.netease.service.protocol.meta.UserPrivateData;

/**
 * 个人中心，查看礼物
 */
public class FragmentGiftList extends FragmentBase{
	
	public static FragmentGiftList newInstance(String userInfo){
		FragmentGiftList fragment = new FragmentGiftList();
		Bundle args = new Bundle();
		args.putString(EgmConstants.BUNDLE_KEY.SELF_PAGE_USERINFO, userInfo);
		fragment.setArguments(args);
		return fragment ;
	}
	
	private UserInfo mUserInfo;
	
	private RelativeLayout mGiftListContainer;
	
	private TextView mImageBack ;
	private TextView mMiddleTitle ;
	
	private PullToRefreshGridView mPullToRefreshGridView ;
	private GridView mGridView ;
	private GiftListAdapter mGiftListAdapter ;
	
	private List<GiftRecord> mGiftRecordList;
	private int mPageNum = 1 ;
	private int mPageCount ;
	
	//列表相关
	private static final int COLOUMN_NUM = 4 ;
	private int columnWidth;
	
	private LinearLayout mTipLayout;
	private TextView mPrvPhotoTip;
	private TextView mBtnSubmitPrvPhoto;
	private TextView mLoadingText;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Gson gson = new Gson();
		mUserInfo = gson.fromJson(this.getArguments().getString(EgmConstants.BUNDLE_KEY.SELF_PAGE_USERINFO), UserInfo.class);
		EgmService.getInstance().addListener(mCallBack);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		mGiftListContainer = (RelativeLayout)inflater.inflate(R.layout.view_pullrefresh_grid, container,false);
		init(mGiftListContainer);
		return mGiftListContainer;
	}
	
	private void init(View root){
		mImageBack = (TextView)root.findViewById(R.id.back);
		mImageBack.setOnClickListener(mOnClickListener);
		
		mMiddleTitle = (TextView)root.findViewById(R.id.middle_title);
		
		mPullToRefreshGridView = (PullToRefreshGridView)root.findViewById(R.id.pull_refresh_grid);
		mGridView = mPullToRefreshGridView.getRefreshableView();
		mGridView.setNumColumns(COLOUMN_NUM);
		
		columnWidth = getActivity().getResources().getDisplayMetrics().widthPixels /COLOUMN_NUM ;
		mGridView.setColumnWidth(columnWidth);
		
		mGiftRecordList = new ArrayList<GiftRecord>();
		
		mPullToRefreshGridView.setOnRefreshListener(new OnRefreshListener2<GridView>(){
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
			}
			@Override
			public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
				//上拉加载更多
				EgmService.getInstance().doGetGiftList(mPageNum);
			}
		});
		mPullToRefreshGridView.setMode(Mode.PULL_FROM_END);
		
		mTipLayout = (LinearLayout)root.findViewById(R.id.no_gift_tip_layout);
		mPrvPhotoTip = (TextView)root.findViewById(R.id.tip_txt_prv_photo);
		mBtnSubmitPrvPhoto = (TextView)root.findViewById(R.id.btn_submit_prv_photo);
		if (mUserInfo.privatePhotoCount == 0) {
			mPrvPhotoTip.setVisibility(View.VISIBLE);
			mBtnSubmitPrvPhoto.setVisibility(View.VISIBLE);
			mBtnSubmitPrvPhoto.setOnClickListener(mOnClickListener);
		} else {
			mPrvPhotoTip.setVisibility(View.INVISIBLE);
			mBtnSubmitPrvPhoto.setVisibility(View.INVISIBLE);
		}
		mLoadingText = (TextView)root.findViewById(R.id.loading);
		
		mGiftListAdapter = new GiftListAdapter(getActivity(),columnWidth,mGiftRecordList);
		mGridView.setAdapter(mGiftListAdapter);
	}
	
	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
				case R.id.back:
					getActivity().finish();
					break;
				case R.id.btn_submit_prv_photo:
					ActivityLevelTable.startActivityForResult(FragmentGiftList.this, ActivityLevelTable.FRAGMENT_PRIVATE_PHOTO, UserInfo.toJsonString(mUserInfo));
					break;
			}
		}
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		EgmService.getInstance().doGetGiftList(mPageNum);
	}
	
	private EgmCallBack mCallBack = new EgmCallBack(){
		@Override
		public void onGetGiftListSucess(int transactionId, GiftRecords obj) {
			mLoadingText.setVisibility(View.GONE);
			mPullToRefreshGridView.onRefreshComplete();
			if(obj == null){
				return ;
			}
			
			//没有收到礼物
			if(obj.gifts.length == 0){
				mTipLayout.setVisibility(View.VISIBLE);
				mPullToRefreshGridView.setMode(Mode.DISABLED);
				return ;
			}
			
			//每页的返回个数
			mPageCount = obj.count ;
			
			//当前能容纳的数量 >= 总数量
			if(mPageNum * mPageCount >= obj.totalCount){
				mPullToRefreshGridView.setMode(Mode.DISABLED);
			}
			
			mPageNum ++ ;
			
			mMiddleTitle.setText(String.format(getResources().getString(R.string.my_gift_num), obj.totalCount));
			mGiftRecordList.addAll(Arrays.asList(obj.gifts));
			mGiftListAdapter.notifyDataSetChanged();
		}

		@Override
		public void onGetGiftListError(int transactionId, int errCode,String err) {
			mLoadingText.setVisibility(View.GONE);
			mPullToRefreshGridView.setMode(Mode.DISABLED);
			ToastUtil.showToast(getActivity(), err);
		}

		@Override
		public void onGetPrivateDataSucess(int transactionId, UserPrivateData obj) {
			if (obj != null) {
				mUserInfo = obj.userInfo;
				init(mGiftListContainer);
			}
		}
	};
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		EgmService.getInstance().removeListener(mCallBack);
	}
}