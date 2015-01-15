package com.netease.engagement.fragment;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.handmark.pulltorefresh.compat.LoadingListView;
import com.handmark.pulltorefresh.library.LoadingAdapterViewBaseWrap.OnLoadingListener;
import com.handmark.pulltorefresh.library.internal.ViewCompat;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.activity.ActivityPrivateImage;
import com.netease.engagement.activity.ActivityWeb;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.app.EgmConstants.IsCameraPhotoFlag;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.engagement.widget.LoadingImageView;
import com.netease.engagement.widget.ProgerssImageView;
import com.netease.engagement.widget.UnLockView;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.LoopBack;
import com.netease.service.protocol.meta.PictureInfo;
import com.netease.service.protocol.meta.PictureInfos;
import com.netease.service.protocol.meta.SendGiftResult;
import com.netease.service.stat.EgmStat;
import com.netease.util.MediaPlayerSystemTone;

/**
 * 女性会员私照列表页面
 */
public class FragmentImageListGirl extends FragmentBase {

	public static FragmentImageListGirl newInstance(long userId, int position) {
		FragmentImageListGirl fragment = new FragmentImageListGirl();
		Bundle args = new Bundle();
		args.putLong(EgmConstants.BUNDLE_KEY.USER_ID, userId);
		args.putInt(EgmConstants.BUNDLE_KEY.POSITION, position);
		fragment.setArguments(args);
		return fragment;
	}

	private CustomActionBar mActionBar;
	private LoadingListView mLoadingListView;
	private ImageListAdapter mAdapter;

	private long toUserId;
	private int position;

	private ArrayList<PictureInfo> mPicList;
	private int mCurPos = -1;

	private int mScreenWidth;
	private int mScreenHeight;

	// 当前页码
	private int mPageNo = 1;
	// 每页的数量
	private int mPageNum;
	// 总数
	private int mTotalCount;
	private int mGetPicListTranId;

	private int mVipFreeTime;

	private int mTid;
	
	//赞操作的Tid
	private int mPraiseTid;
	//踩操作的Tid
	private int mUnlikeTid;

	private boolean touchEventState = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		toUserId = args.getLong(EgmConstants.BUNDLE_KEY.USER_ID);
		position = args.getInt(EgmConstants.BUNDLE_KEY.POSITION, 0);
		EgmService.getInstance().addListener(mCallBack);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mLoadingListView = (LoadingListView) inflater.inflate(R.layout.view_loading_list, container, false);
		
		init();
		return mLoadingListView;
	}

	private void init() {
		mScreenWidth = getResources().getDisplayMetrics().widthPixels;
		mScreenHeight = getResources().getDisplayMetrics().heightPixels;
		mPicList = new ArrayList<PictureInfo>();

		mLoadingListView.disablePullToRefresh();
		mLoadingListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				int pos = arg2 - mLoadingListView.getRefreshableView().getHeaderViewsCount();
				//交互修改：只有当该私照解锁了以后，点击才能进入大图
				if(mPicList == null || mPicList.size() < (pos+1))
					return;
				if(mPicList.get(pos).isViewed) {
					long userId = toUserId;
					long picId = mPicList.get(pos).id;
					ArrayList<PictureInfo> unLockList = getUnLockPicList();
					ActivityPrivateImage.startActivityForResult(FragmentImageListGirl.this, String.valueOf(toUserId), unLockList, unLockList.indexOf(mPicList.get(pos)));
					EgmStat.log(EgmStat.LOG_CLICK_PHOTO_DETAIL, EgmStat.SCENE_USER_DETAIL, userId, picId, EgmStat.TYPE_PAID);
				}
			}
		});
		mLoadingListView.setOnLoadingListener(new OnLoadingListener() {
			@Override
			public void onRefreshing() {
			}

			@Override
			public void onLoading() {
				mGetPicListTranId = EgmService.getInstance().doGetPictureList(String.valueOf(toUserId), EgmConstants.Photo_Type.TYPE_PRIVATE, mPageNo);
			}

			@Override
			public void onLoadingMore() {
				mGetPicListTranId = EgmService.getInstance().doGetPictureList(String.valueOf(toUserId), EgmConstants.Photo_Type.TYPE_PRIVATE, mPageNo);
			}
		});
	}
	
	private ArrayList<PictureInfo> getUnLockPicList() {
		ArrayList<PictureInfo> unLockList = new ArrayList<PictureInfo>();
		for (PictureInfo picInfo : mPicList) {
			if (picInfo.isViewed) {
				unLockList.add(picInfo);
			}
		}
		return unLockList;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mActionBar = ((ActivityEngagementBase) getActivity()).getCustomActionBar();
		mActionBar.setLeftBackgroundResource(R.drawable.titlebar_c_selector);
		mActionBar.setLeftAction(R.drawable.bar_btn_back_b, R.string.back);
		mActionBar.setLeftTitleColor(getResources().getColor(R.color.white));
		mActionBar.hideMiddleTitle();
		mActionBar.setRightVisibility(View.GONE);

		mAdapter = new ImageListAdapter(getActivity());
		mLoadingListView.setAdapter(mAdapter);
		mLoadingListView.load();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == EgmConstants.REQUEST_GET_PRI_PIC) {
			PictureInfo picInfo = data.getParcelableExtra(EgmConstants.BUNDLE_KEY.PICTURE_INFO);
			int pos = mPicList.indexOf(picInfo);
			if (picInfo != null && pos != -1) {
				mLoadingListView.getRefreshableView().setSelection(pos + 1);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private EgmCallBack mCallBack = new EgmCallBack() {
		@Override
		public void onSendGiftSucess(int transactionId, final SendGiftResult obj) {
			if (mTid != transactionId) {
				return;
			}
			if (obj != null && !TextUtils.isEmpty(obj.privacyUrl)) {
				if (selectUnLockView != null) {
					selectUnLockView.postDelayed(new Runnable() {
						@Override
						public void run() {
							if (selectProgressBar != null) {
								selectProgressBar.setVisibility(View.GONE);
							}
							// added by lishang 播放解锁音效
							if (EgmPrefHelper.getGiftsPicOn(getActivity(),
									ManagerAccount.getInstance().getCurrentId()))
								MediaPlayerSystemTone.instance(getActivity()).playWelecomTone("date_unlock.mp3");
							// 播放解锁动画
							selectUnLockView.startUnlockAnim();
							selectUnLockView.postDelayed(new Runnable() {
								@Override
								public void run() {
									PictureInfo info = mPicList.get(mCurPos);
									info.oldPrivateUrl = info.picUrl;
									info.picUrl = obj.privacyUrl;
									info.isViewed = true;

									mVipFreeTime = obj.freeTimes;

									mAdapter.notifyDataSetChanged();

									touchEventState = false;

									LoopBack lp = new LoopBack();
									lp.mType = EgmConstants.LOOPBACK_TYPE.pri_pic_unlocked;
									lp.mData = info;
									mTid = EgmService.getInstance().doLoopBack(lp);

									mCurPos = -1;
									selectUnLockView = null;
									selectProgressBar = null;
								}
							}, 1200);
						}
					}, 400);

				}
			}
		}

		@Override
		public void onSendGiftError(int transactionId, int errCode, String err) {
			if (mTid != transactionId) {
				return;
			}
			if (selectProgressBar != null) {
				selectProgressBar.setVisibility(View.GONE);
			}
			mCurPos = -1;
			touchEventState = false;
			selectUnLockView = null;
			selectProgressBar = null;
			ToastUtil.showToast(getActivity(), err);
			if (errCode == EgmServiceCode.TRANSACTION_COMMON_BALANCE_NOT_ENOUGHT) {
				showChargeDialog();
			}
		}

		@Override
		public void onGetPictureListSucess(int transactionId, PictureInfos obj) {
			if (mGetPicListTranId == transactionId && obj != null) {
				mPageNum = obj.count;
				mTotalCount = obj.totalCount;
				if (mPageNo * mPageNum >= mTotalCount) {
					mLoadingListView.onLoadingComplete(false);
				} else {
					mLoadingListView.onLoadingComplete(true);
				}
				mVipFreeTime = obj.freeTimes;
				mPicList.addAll(Arrays.asList(obj.pictureInfos));
				mAdapter.notifyDataSetChanged();
				if (mPageNo == 1) {
					mLoadingListView.postDelayed(new Runnable() {
						@Override
						public void run() {
							mLoadingListView.getRefreshableView().setSelection(position);
						}
					}, 100);
				}
				mPageNo++;
			}
		}

		@Override
		public void onGetPictureListError(int transactionId, int errCode, String err) {
			mLoadingListView.onLoadingComplete();
			ToastUtil.showToast(getActivity(), err);
		}
		
		@Override
		public void onPraisePicSucess(int transactionId, int code) {
			if(mPraiseTid == transactionId) {
				ToastUtil.showToast(getActivity(),R.string.praise_pic_suc);
				selectPraiseCount.setTextColor(getResources().getColor(R.color.content_text));
				selectPraiseCount.setText(String.valueOf(mPicList.get(selectPraisePosition).praiseCount + 1) + getResources().getString(R.string.praised));
				ViewCompat.setBackground(selectPraiseIcon,getResources().getDrawable(R.drawable.icon_photo_love_prs));
				mPicList.get(selectPraisePosition).praiseCount++; //为了防止重新getView的时候，会出现已经点过赞的图片，赞的数目不对
			}
		}
		
		@Override
		public void onPraisePicError(int transactionId, int errCode, String err) {
			if(mPraiseTid == transactionId) {
				if(errCode == EgmServiceCode.TRANSACTION_PICTURE_HAS_PRAISED){
					ToastUtil.showToast(getActivity(),R.string.has_praised);
				}else{
					ToastUtil.showToast(getActivity(),err);
				}
			}
		}
		
		@Override
		public void onUnlikePicSuccess(int transactionId, int code) {
			if(mUnlikeTid == transactionId) {
				ToastUtil.showToast(getActivity(),R.string.unlike_pic_suc);
				selectUnlikeCount.setTextColor(getResources().getColor(R.color.content_text));
				selectUnlikeCount.setText(String.valueOf(mPicList.get(selectUnlikePosition).stepCount + 1) + getResources().getString(R.string.unliked));
				ViewCompat.setBackground(selectUnlikeIcon,getResources().getDrawable(R.drawable.icon_photo_unlike_prs));
				mPicList.get(selectUnlikePosition).stepCount++; //为了防止重新getView的时候，会出现已经踩过的图片，踩的数目不对
			}
		}
		
		@Override
		public void onUnlikePicError(int transactionId, int errCode, String err) {
			if(mUnlikeTid == transactionId) {
				if(errCode == EgmServiceCode.TRANSACTION_PICTURE_HAS_UNLIKED){
					ToastUtil.showToast(getActivity(),R.string.has_unliked);
				}else{
					ToastUtil.showToast(getActivity(),err);
				}
			}
		}

		@Override
		public void onLoopBack(int transactionId, LoopBack obj) {// 私照解锁后个人主页私照列表改变
			if (mTid == transactionId) { // 是当前页面调用的loopBack，就不做处理
				return;
			}
			if (obj != null) {
				PictureInfo picInfo = (PictureInfo) obj.mData;
				switch (obj.mType) {
				case EgmConstants.LOOPBACK_TYPE.pri_pic_unlocked:
					for (PictureInfo info : mPicList) {
						if (picInfo.id == info.id) {
							info.isViewed = true;
							info.oldPrivateUrl = info.picUrl;
							info.picUrl = picInfo.picUrl;
							mVipFreeTime = picInfo.vipFreeTime;
							mAdapter.notifyDataSetChanged();
							break;
						}
					}
					break;
				case EgmConstants.LOOPBACK_TYPE.pri_pic_praise:
					for(PictureInfo info : mPicList) {
						if(picInfo.id == info.id) {
							info.praiseCount++;
							mAdapter.notifyDataSetChanged();
							break;
						}
					}
					break;
				case EgmConstants.LOOPBACK_TYPE.pri_pic_unlike:
					for(PictureInfo info : mPicList) {
						if(picInfo.id == info.id) {
							info.stepCount++;
							mAdapter.notifyDataSetChanged();
							break;
						}
					}
					break;
				}
			}
		}
	};

	private AlertDialog mChargeDialog;

	private void showChargeDialog() {
		if (mChargeDialog == null) {
			mChargeDialog = EgmUtil.createEgmMenuDialog(getActivity(), getActivity().getResources().getString(R.string.coins_not_enough_private_photo), new CharSequence[] { getActivity().getResources().getString(R.string.go_to_charge) }, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ActivityWeb.startCoinCharge(getActivity());
					mChargeDialog.dismiss();
				}
			});
		}
		mChargeDialog.show();
	}

	private UnLockView selectUnLockView;
	private ProgressBar selectProgressBar;
	
	private TextView selectPraiseCount;
	private TextView selectPraiseIcon;
	private int selectPraisePosition;
	
	private TextView selectUnlikeCount;
	private TextView selectUnlikeIcon;
	private int selectUnlikePosition;

	private int imageHeight;

	private class ImageListAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private FrameLayout.LayoutParams lp;

		public ImageListAdapter(Context context) {
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			imageHeight = (int) (575f * mScreenWidth / 640);
			lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, imageHeight);
		}

		@Override
		public int getCount() {
			return mPicList.size();
		}

		@Override
		public Object getItem(int position) {
			return mPicList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;

			boolean needInflate = false;
			if (convertView == null) {
				needInflate = true;
			} else {
				holder = (ViewHolder) convertView.getTag();
				if (holder.unlockView.alreadyStartAnimation) {
					needInflate = true;
				} else if (holder.vipView.alreadyStartAnimation) {
					needInflate = true;
				}
			}

			LoadingImageView imageView = null;
			
			if (needInflate) {
				convertView = mInflater.inflate(R.layout.view_image_list_item, null);
				
				holder = new ViewHolder();
				holder.mDivider = convertView.findViewById(R.id.divider);
				
				holder.unlockView = (UnLockView)convertView.findViewById(R.id.unlock_view);
				holder.unlockView.setLayoutParams(new LayoutParams(lp));
				holder.sendGiftLayoutInUnLockView = (LinearLayout)holder.unlockView.findViewById(R.id.txt_send_gift_layout);
				holder.sendGiftLayoutInUnLockView.setOnClickListener(unlockClickListener);
				holder.giftCoinLayoutInUnLockView = (LinearLayout) holder.unlockView.findViewById(R.id.txt_coin_layout);
				holder.giftCoinLayoutInUnLockView.setOnClickListener(unlockClickListener);
				holder.giftIconTvInUnLockView = (ImageView) holder.unlockView.findViewById(R.id.gift_icon);
				holder.giftIconTvInUnLockView.setOnClickListener(unlockClickListener);
				holder.vipView = (UnLockView)convertView.findViewById(R.id.vip_view);
				holder.vipView.setLayoutParams(new LayoutParams(lp));
				holder.sendGiftLayoutInVipView = (LinearLayout)holder.vipView.findViewById(R.id.txt_send_gift_layout);
				holder.sendGiftLayoutInVipView.setOnClickListener(unlockClickListener);
				holder.giftCoinLayoutInVipView = (LinearLayout) holder.vipView.findViewById(R.id.txt_coin_layout);
				holder.giftCoinLayoutInVipView.setOnClickListener(unlockClickListener);
				holder.giftIconTvInVipView = (ImageView) holder.vipView.findViewById(R.id.gift_icon);
				holder.giftIconTvInVipView.setOnClickListener(unlockClickListener);

				holder.mProgressIv = (ProgerssImageView) convertView.findViewById(R.id.image);
				holder.mProgressIv.setAutoInvisible(false);
				
				holder.mCameraPhotoTips = (TextView)convertView.findViewById(R.id.xian_pai_photo_tips);
				holder.mPraiseLayout = (LinearLayout)convertView.findViewById(R.id.praise_layout);
				holder.mPraiseLayout.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						int pos = (Integer) v.getTag();
						selectPraiseCount = (TextView)v.findViewById(R.id.praise_num);
						selectPraiseIcon = (TextView)v.findViewById(R.id.praise_icon);
						selectPraisePosition = pos;
						mPraiseTid = EgmService.getInstance().doPraisePriPic(Long.toString(toUserId), 
									String.valueOf(mPicList.get(pos).id));
					}
				});
				holder.mPraiseIcon = (TextView)convertView.findViewById(R.id.praise_icon);
				holder.mPraiseCount = (TextView)convertView.findViewById(R.id.praise_num);
				
				holder.mUnlikeLayout = (LinearLayout)convertView.findViewById(R.id.unlike_layout);
				holder.mUnlikeLayout.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						int pos = (Integer) v.getTag();
						selectUnlikeCount = (TextView)v.findViewById(R.id.unlike_num);
						selectUnlikeIcon = (TextView) v.findViewById(R.id.unlike_icon);
						selectUnlikePosition = pos;
						mUnlikeTid = EgmService.getInstance().doUnlikePriPic(Long.toString(toUserId),
								String.valueOf(mPicList.get(pos).id));
					}
				});
				holder.mUnlikeIcon = (TextView)convertView.findViewById(R.id.unlike_icon);
				holder.mUnlikeCount = (TextView)convertView.findViewById(R.id.unlike_num);
				
				imageView = holder.mProgressIv.mImageView;
				imageView.setDefaultResId(R.drawable.icon_photo_loaded_fail);
				imageView.setLayoutParams(lp);
				imageView.setScaleType(ScaleType.CENTER_CROP);
				imageView.setLoadingImage(null);
				imageView.setServerClipSize(mScreenWidth, mScreenHeight);

				holder.mProgressBar = (ProgressBar) convertView.findViewById(R.id.progressbar);

				convertView.setTag(holder);
			}
			else {
				imageView = holder.mProgressIv.mImageView;
//				imageView.setIgnoreRequestLayout(true); // ignore default image
				imageView.setLoadingImage(null);
//				imageView.setIgnoreRequestLayout(true); // ignore remote image
			}

			holder = (ViewHolder) convertView.getTag();
			holder.mPraiseLayout.setTag(position);
			holder.mUnlikeLayout.setTag(position);
			
			holder.mProgressIv.showProgressBar();
			
			if (position == getCount() - 1) {
				holder.mDivider.setVisibility(View.GONE);
			}
			else {
				holder.mDivider.setVisibility(View.VISIBLE);
			}
			
			String url = mPicList.get(position).oldPrivateUrl;
			if (! TextUtils.isEmpty(url)) { // 切换动画中检查模糊图是否在内存缓存
				if (imageView.checkCacheImage(url)) {
					holder.mProgressIv.showImageViewWithProgess();
				}
			}
			
			PictureInfo picInfo = mPicList.get(position);
			imageView.setLoadingImage(picInfo.picUrl);
			if (picInfo.isViewed) {
				holder.mPraiseLayout.setVisibility(View.VISIBLE);
				holder.mUnlikeLayout.setVisibility(View.VISIBLE);
				holder.unlockView.setVisibility(View.INVISIBLE);
				holder.vipView.setVisibility(View.INVISIBLE);
				holder.mCameraPhotoTips.setVisibility(picInfo.isCamera() ? View.VISIBLE : View.INVISIBLE);
				
				holder.mPraiseCount.setText(String.valueOf(picInfo.praiseCount));
				holder.mUnlikeCount.setText(String.valueOf(picInfo.stepCount));
				ViewCompat.setBackground(holder.mPraiseIcon,getResources().getDrawable(R.drawable.icon_photo_love));
				ViewCompat.setBackground(holder.mUnlikeIcon,getResources().getDrawable(R.drawable.icon_photo_unlike));
				holder.mPraiseCount.setTextColor(getResources().getColor(R.color.white));
				holder.mUnlikeCount.setTextColor(getResources().getColor(R.color.white));
				
			} else {
				holder.mPraiseLayout.setVisibility(View.GONE);
				holder.mUnlikeLayout.setVisibility(View.GONE);
				holder.mCameraPhotoTips.setVisibility(View.INVISIBLE);
				if (mVipFreeTime > 0) {
					holder.unlockView.setVisibility(View.INVISIBLE);
					holder.vipView.setVisibility(View.VISIBLE);
					holder.vipView.initLogos(UnLockView.STATE_VIP, mVipFreeTime, -1);
					holder.vipView.initPraisePhoto(picInfo.praiseCount, picInfo.stepCount);
				} else {
					holder.vipView.setVisibility(View.INVISIBLE);
					holder.unlockView.setVisibility(View.VISIBLE);
					holder.unlockView.initCoins(picInfo.needCoins);
					holder.unlockView.initLogos(UnLockView.STATE_COMMON, -1, picInfo.giftId);
					holder.unlockView.initPraisePhoto(picInfo.praiseCount, picInfo.stepCount);
				}
			}
			holder.sendGiftLayoutInUnLockView.setTag(Integer.valueOf(position));
			holder.giftCoinLayoutInUnLockView.setTag(Integer.valueOf(position));
			holder.giftIconTvInUnLockView.setTag(Integer.valueOf(position));

			holder.sendGiftLayoutInVipView.setTag(Integer.valueOf(position));
			holder.giftCoinLayoutInVipView.setTag(Integer.valueOf(position));
			holder.giftIconTvInVipView.setTag(Integer.valueOf(position));

			return convertView;
		}

		OnClickListener unlockClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				touchEventState = true;
				selectUnLockView = (UnLockView) v.getParent().getParent().getParent();
				RelativeLayout rl = (RelativeLayout) selectUnLockView.getParent();
				selectProgressBar = (ProgressBar) rl.findViewById(R.id.progressbar);
				int position = (Integer) v.getTag();
				mCurPos = position;
				selectProgressBar.setVisibility(View.VISIBLE);
				
				long userId = toUserId;
				long picId = mPicList.get(position).id;
				
				mTid = EgmService.getInstance().doSendGift(
						String.valueOf(userId), null, String.valueOf(picId), 
						EgmConstants.Send_Gift_From.TYPE_UNLOCK_PRIVATE_IMAGE);
				
				EgmStat.log(EgmStat.LOG_CLICK_PHOTO_DETAIL, 
						EgmStat.SCENE_USER_DETAIL, userId, picId, 
						mVipFreeTime > 0 ? EgmStat.TYPE_VIP_FREE : EgmStat.TYPE_PAY);
			}
		};

		private class ViewHolder {
			UnLockView unlockView;
			LinearLayout sendGiftLayoutInUnLockView;
			LinearLayout giftCoinLayoutInUnLockView;
			ImageView giftIconTvInUnLockView;
			UnLockView vipView;
			LinearLayout sendGiftLayoutInVipView;
			LinearLayout giftCoinLayoutInVipView;
			ImageView giftIconTvInVipView;
			ProgerssImageView mProgressIv;;
			ProgressBar mProgressBar;
			TextView mCameraPhotoTips;
			LinearLayout mPraiseLayout;
			TextView mPraiseIcon;
			TextView mPraiseCount;
			LinearLayout mUnlikeLayout;
			TextView mUnlikeIcon;
			TextView mUnlikeCount;
			View mDivider;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		EgmService.getInstance().removeListener(mCallBack);
	}

	public boolean dispatchTouchEvent(MotionEvent ev) {

		if (ev.getAction() == MotionEvent.ACTION_MOVE) {
			return touchEventState;
		}
		return touchEventState;
	}
}
