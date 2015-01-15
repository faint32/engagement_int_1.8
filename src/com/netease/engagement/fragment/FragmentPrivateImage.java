package com.netease.engagement.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.adapter.ImagePagerAdapter;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.view.PraisePhotoAnimation;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.GetUnLockPicListResult;
import com.netease.service.protocol.meta.LoopBack;
import com.netease.service.protocol.meta.PictureInfo;

/**
 * 聊天界面发送私照；个人主页查看私照
 */
public class FragmentPrivateImage extends FragmentBase{

	public static FragmentPrivateImage newInstance(String toUserId , ArrayList<PictureInfo> unLockList, int pos){
		FragmentPrivateImage fragment = new FragmentPrivateImage();
		Bundle args = new Bundle();
		args.putString(EgmConstants.BUNDLE_KEY.USER_ID, toUserId);
		args.putParcelableArrayList(EgmConstants.BUNDLE_KEY.PICTURE_INFO_LIST, unLockList);
		args.putInt(EgmConstants.BUNDLE_KEY.POSITION, pos);
		fragment.setArguments(args);
		return fragment ;
	}
	
	private CustomActionBar mCustomActionBar;
	
	//赞一下
	private LinearLayout mPraiseLayout;
	private TextView mPraiseIcon;
	private TextView mPraiseTxt;
	
	private ImageView mPraiseView;
	
	//踩一下
	private LinearLayout mUnlikeLayout;
	private TextView mUnlikeIcon;
	private TextView mUnlikeTxt;
	//现拍照片
	private TextView mCameraPhotoTips;
	
	private ViewPager mViewPager;
	private ImagePagerAdapter mPagerAdapter;
	
	private String mToUserId;
	private ArrayList<PictureInfo> mUnLockList;
	private int mPos;
	
	private int mUnLockTranId;
	private int mUnLikeTranId;
	private int mPraiseTranId;
	
	private SparseIntArray mTransArr = new SparseIntArray();
	private boolean mLoading = false;
	private boolean mHasMore = true;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		EgmService.getInstance().addListener(mCallBack);
		
		mToUserId = this.getArguments().getString(EgmConstants.BUNDLE_KEY.USER_ID);
		mUnLockList = this.getArguments().getParcelableArrayList(EgmConstants.BUNDLE_KEY.PICTURE_INFO_LIST);
		mPos = this.getArguments().getInt(EgmConstants.BUNDLE_KEY.POSITION);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.fragment_private_image_layout, container,false);
		init(root);
		root.setFocusable(true);
		root.setFocusableInTouchMode(true);
		root.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
					processBackKey();
				}
				return false;
			}
		});
		return root ;
	}
	
	private void processBackKey() {
		if (mUnLockList != null && mUnLockList.get(mPos) != null) {
			Intent data = new Intent();
			data.putExtra(EgmConstants.BUNDLE_KEY.PICTURE_INFO, mUnLockList.get(mPos));
			getActivity().setResult(Activity.RESULT_OK, data);
		}
	}
	
	private void init(View root){
		if(root == null){
			return ;
		}
		mPraiseView = (ImageView)root.findViewById(R.id.praise_anim);
		mPraiseLayout = (LinearLayout)root.findViewById(R.id.praise_layout);
		mPraiseLayout.setEnabled(false);
		mPraiseLayout.setVisibility(View.VISIBLE);
		mPraiseLayout.setOnClickListener(mOnClickListener);
		mPraiseIcon = (TextView)root.findViewById(R.id.praise_icon);
		mPraiseTxt = (TextView)root.findViewById(R.id.praise_num);
		
		mUnlikeLayout = (LinearLayout)root.findViewById(R.id.unlike_layout);
		mUnlikeLayout.setEnabled(false);
		mUnlikeLayout.setOnClickListener(mOnClickListener);
		mUnlikeIcon = (TextView)root.findViewById(R.id.unlike_icon);
		mUnlikeTxt = (TextView)root.findViewById(R.id.unlike_num);
		mCameraPhotoTips = (TextView)root.findViewById(R.id.xian_pai_photo_tips);
		
		mViewPager = (ViewPager)root.findViewById(R.id.viewpager);
		mPagerAdapter = new ImagePagerAdapter(getActivity(), getPhotoUrls(mUnLockList));
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOnPageChangeListener(mOnPageChangeListener);
		mViewPager.setCurrentItem(mPos);
		renderView(mPos);
	}
	
	private ArrayList<String> getPhotoUrls(List<PictureInfo> picList){
		ArrayList<String> imgUrls  = new ArrayList<String>();
		for(PictureInfo item : picList){
			imgUrls.add(item.picUrl);
		}
		return imgUrls;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mCustomActionBar = ((ActivityEngagementBase) getActivity()).getCustomActionBar();
		mCustomActionBar.setLeftBackgroundResource(R.drawable.titlebar_c_selector);
		mCustomActionBar.setLeftAction(R.drawable.bar_btn_back_b, R.string.back);
		mCustomActionBar.setLeftTitleColor(getResources().getColor(R.color.white));
		mCustomActionBar.setMiddleTitle("");
		mCustomActionBar.hideRightTitle();
		
		mCustomActionBar.setLeftClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				processBackKey();
				clickBack();
			}
		});
		loadMoreUnLockList();
	}
	
	private void loadMoreUnLockList() {
		if (!mLoading && mHasMore && mPos + 1 >= mUnLockList.size()) {
			mLoading = true;
			PictureInfo lastPicInfo = mUnLockList.get(mUnLockList.size() - 1);
			mUnLockTranId = EgmService.getInstance().doGetUnLockPicList(mToUserId, String.valueOf(lastPicInfo.id));
		}
	}
	
	private void renderView(int pos){
		mPos = pos;
		PictureInfo picInfo = mUnLockList.get(mPos);
		if (picInfo.praised) {
			mPraiseTxt.setText(picInfo.praiseCount + getResources().getString(R.string.praised));
			mPraiseTxt.setTextColor(getResources().getColor(R.color.content_text));
			mPraiseIcon.setBackgroundResource(R.drawable.icon_photo_love_prs);
			mPraiseLayout.setEnabled(false);
		} else {
			mPraiseTxt.setText("" + picInfo.praiseCount);
			mPraiseTxt.setTextColor(getResources().getColor(R.color.white));
			mPraiseIcon.setBackgroundResource(R.drawable.icon_photo_love);
			mPraiseLayout.setEnabled(true);
		}
		if (picInfo.unliked) {
			mUnlikeTxt.setText(picInfo.stepCount + getResources().getString(R.string.unliked));
			mUnlikeTxt.setTextColor(getResources().getColor(R.color.content_text));
			mUnlikeIcon.setBackgroundResource(R.drawable.icon_photo_unlike_prs);
			mUnlikeLayout.setEnabled(false);
		} else {
			mUnlikeTxt.setText("" + picInfo.stepCount);
			mUnlikeTxt.setTextColor(getResources().getColor(R.color.white));
			mUnlikeIcon.setBackgroundResource(R.drawable.icon_photo_unlike);
			mUnlikeLayout.setEnabled(true);
		}
		mCameraPhotoTips.setVisibility(picInfo.isCamera() ? View.VISIBLE : View.INVISIBLE);
	}
	
	private OnClickListener mOnClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			switch(v.getId()){
				case R.id.praise_layout:
					if(!mUnLockList.get(mPos).praised){
						mPraiseTranId = EgmService.getInstance().doPraisePriPic(mToUserId,String.valueOf(mUnLockList.get(mPos).id));
						mTransArr.append(mPraiseTranId, mPos);
					}else{
						ToastUtil.showToast(getActivity(),R.string.has_praised);
					}
					break;
				case R.id.unlike_layout:
					if(!mUnLockList.get(mPos).unliked) {
						mUnLikeTranId = EgmService.getInstance().doUnlikePriPic(mToUserId, String.valueOf(mUnLockList.get(mPos).id));
						mTransArr.append(mUnLikeTranId, mPos);
					} else {
						ToastUtil.showToast(getActivity(),R.string.has_unliked);
					}
					break;
			}
		}
	};
	
	private EgmCallBack mCallBack = new EgmCallBack(){
		@Override
		public void onPraisePicSucess(int transactionId, int code) {
			if (mPraiseTranId == transactionId) {
				PraisePhotoAnimation mPraiseAnim = new PraisePhotoAnimation();
				mPraiseAnim.setPraiseView(mPraiseView);
				mPraiseAnim.startAnimation();
				ToastUtil.showToast(getActivity(),R.string.praise_pic_suc);
				int pos = mTransArr.get(transactionId, -1);
				if (pos != -1) {
					mTransArr.delete(transactionId);
					PictureInfo picInfo = mUnLockList.get(pos);
					picInfo.praised = true;
					picInfo.praiseCount++;
					LoopBack lp = new LoopBack();
					lp.mType = EgmConstants.LOOPBACK_TYPE.pri_pic_praise ;
					lp.mData = picInfo;
					EgmService.getInstance().doLoopBack(lp);
				}
				if (pos == mPos) {
					renderView(mPos);
				}
			}
		}

		@Override
		public void onPraisePicError(int transactionId, int errCode, String err) {
			if (mPraiseTranId == transactionId) {
				int pos = mTransArr.get(transactionId, -1);
				if (pos != -1) {
					mTransArr.delete(transactionId);
					PictureInfo picInfo = mUnLockList.get(pos);
					picInfo.praised = true;
				}
				if(errCode == EgmServiceCode.TRANSACTION_PICTURE_HAS_PRAISED){
					if (pos == mPos) {
						renderView(mPos);
					}
					ToastUtil.showToast(getActivity(),R.string.has_praised);
				}else{
					ToastUtil.showToast(getActivity(),err);
				}
			}
		}
		
		@Override
		public void onUnlikePicSuccess(int transactionId, int code) {
			if (mUnLikeTranId == transactionId) {
				ToastUtil.showToast(getActivity(),R.string.unlike_pic_suc);
				int pos = mTransArr.get(transactionId, -1);
				if (pos != -1) {
					mTransArr.delete(transactionId);
					PictureInfo picInfo = mUnLockList.get(pos);
					picInfo.unliked = true;
					picInfo.stepCount++;
					LoopBack lp = new LoopBack();
					lp.mType = EgmConstants.LOOPBACK_TYPE.pri_pic_unlike ;
					lp.mData = picInfo;
					EgmService.getInstance().doLoopBack(lp);
				}
				if (pos == mPos) {
					renderView(mPos);
				}
			}
		}
		
		@Override
		public void onUnlikePicError(int transactionId, int errCode, String err) {
			if (mUnLikeTranId == transactionId) {
				int pos = mTransArr.get(transactionId, -1);
				if (pos != -1) {
					mTransArr.delete(transactionId);
					PictureInfo picInfo = mUnLockList.get(pos);
					picInfo.unliked = true;
				}
				if(errCode == EgmServiceCode.TRANSACTION_PICTURE_HAS_UNLIKED) {
					if (pos == mPos) {
						renderView(mPos);
					}
					ToastUtil.showToast(getActivity(),R.string.has_unliked);
				} else {
					ToastUtil.showToast(getActivity(),err);
				}
			}
		}
		
		@Override
		public void onGetUnLockPictureListSucess(int transactionId, GetUnLockPicListResult obj) {
			if (mUnLockTranId == transactionId && obj != null) {
				mLoading = false;
				if (obj.pictureInfos.length > 0) {
					List<PictureInfo> picInfos = Arrays.asList(obj.pictureInfos);
					mUnLockList.addAll(picInfos);
					mPagerAdapter.addImageUrls(getPhotoUrls(picInfos));
				}
				if (obj.pictureInfos.length < 20) {
					mHasMore = false;
				}
			}
		}
		
		@Override
		public void onGetUnLockPictureListError(int transactionId, int errCode, String err) {
			if (mUnLockTranId == transactionId) {
				mLoading = false;
				ToastUtil.showToast(getActivity(), err);
			}
		}
	};
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		EgmService.getInstance().removeListener(mCallBack);
	}
	
	private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
		@Override
		public void onPageScrollStateChanged(int arg0) {
			if (arg0 == ViewPager.SCROLL_STATE_DRAGGING && mViewPager.getCurrentItem() == mUnLockList.size() - 1) {
				loadMoreUnLockList();
			}
		}
		
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			
		}
		
		@Override
		public void onPageSelected(int pos) {
			mPagerAdapter.setScale(1.0f);
			renderView(pos);
			loadMoreUnLockList();
		}
	};
}
