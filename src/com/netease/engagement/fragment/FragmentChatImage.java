package com.netease.engagement.fragment;


import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.internal.ViewCompat;
import com.netease.common.image.ImageType;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.activity.ActivityWeb;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.image.ImageViewTouch;
import com.netease.engagement.view.PraisePhotoAnimation;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.engagement.widget.UnLockView;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.GetSigPriResult;
import com.netease.service.protocol.meta.PictureInfo;
import com.netease.service.protocol.meta.SendGiftResult;
import com.netease.util.MediaPlayerSystemTone;

/**
 * 聊天界面查看图片
 */
public class FragmentChatImage extends FragmentBase{
	
	public static FragmentChatImage newInstance(
			String imageUrl,
			boolean isPrivate,
			long userId,
			long picId,
			int isCameraPhoto){
		FragmentChatImage fragment = new FragmentChatImage();
		Bundle bundle = new Bundle();
		bundle.putString(EgmConstants.BUNDLE_KEY.CHAT_IMAGE_URL,imageUrl);
		bundle.putBoolean(EgmConstants.BUNDLE_KEY.CHAT_IMAGE_IS_PRIVATE,isPrivate);
		bundle.putLong(EgmConstants.BUNDLE_KEY.USER_ID,userId);
		bundle.putLong(EgmConstants.BUNDLE_KEY.CHAT_PRIVATE_IMAGE_ID,picId);
		bundle.putInt(EgmConstants.BUNDLE_KEY.CHAT_IMAGE_IS_CAMERA_PHOTO, isCameraPhoto);
		fragment.setArguments(bundle);
		return fragment ;
	}
	
	private String mImageUrl ;
	
	private UnLockView mUnLockView ;
	
	private UnLockView mVipView ;
	
	private boolean isPrivate ;
	private long userId ;
	private long picId ;
	private PictureInfo mPictureInfo ;
	private int mUnlockTranId ;
	
	private CustomActionBar mCustomActionBar ;
	private RelativeLayout mContainer ;
	private ProgressBar mProgressBar ;
	
	private LinearLayout mPraiseLayout ;
	private TextView mPraiseIcon ;
	private TextView mPraiseTxt ;
	
	private LinearLayout mUnlikeLayout;
	private TextView mUnlikeIcon;
	private TextView mUnlikeTxt;
	
	private int mScreenWidth ;
	private int mScreenHeight ;
	
	private UnLockView selectUnLockView;
	
	private ImageView mPraiseView;

	private boolean isUnlockSuccess = false;
	
	private boolean isPraised = false;
	private boolean isUnliked = false;

	private int mPraiseTranId;
	private int mUnLikeTranId;
	
	private int isCameraPhoto=-1;
	private TextView mCameraPhotoTips;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle args = this.getArguments();
		mImageUrl = args.getString(EgmConstants.BUNDLE_KEY.CHAT_IMAGE_URL);
		isPrivate = args.getBoolean(EgmConstants.BUNDLE_KEY.CHAT_IMAGE_IS_PRIVATE,false);
		userId = args.getLong(EgmConstants.BUNDLE_KEY.USER_ID,0);
		picId = args.getLong(EgmConstants.BUNDLE_KEY.CHAT_PRIVATE_IMAGE_ID,0);
		isCameraPhoto=args.getInt(EgmConstants.BUNDLE_KEY.CHAT_IMAGE_IS_CAMERA_PHOTO);
		EgmService.getInstance().addListener(mCallBack);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
	    initTitle();
	}
	
	private void initTitle(){
		mCustomActionBar = ((ActivityEngagementBase)getActivity()).getCustomActionBar();
		mCustomActionBar.getCustomView().setBackgroundColor(getResources().getColor(R.color.black));
		mCustomActionBar.setLeftBackgroundResource(R.drawable.titlebar_c_selector);
		mCustomActionBar.setLeftAction(R.drawable.bar_btn_back_b, R.string.back);
		mCustomActionBar.setLeftTitleColor(getResources().getColor(R.color.white));
		mCustomActionBar.hideMiddleTitle();
		mCustomActionBar.hideRightTitle();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.fragment_chat_image, container,false);
		init(root);
		return root ;
	}
	
	private void init(View root){
		mScreenWidth = getActivity().getResources().getDisplayMetrics().widthPixels;
		mScreenHeight = getActivity().getResources().getDisplayMetrics().heightPixels;
		
		mUnLockView = (UnLockView)root.findViewById(R.id.unlock_view);
		LinearLayout sendGiftLayoutInUnLockView = (LinearLayout)mUnLockView.findViewById(R.id.txt_send_gift_layout);
		sendGiftLayoutInUnLockView.setOnClickListener(unlockClickListener);
		LinearLayout giftCoinLayoutInUnLockView = (LinearLayout)mUnLockView.findViewById(R.id.txt_coin_layout);
		giftCoinLayoutInUnLockView.setOnClickListener(unlockClickListener);
		ImageView giftIconTvInUnLockView = (ImageView)mUnLockView.findViewById(R.id.gift_icon);
		giftIconTvInUnLockView.setOnClickListener(unlockClickListener);
		
		mVipView = (UnLockView)root.findViewById(R.id.vip_view);
		LinearLayout sendGiftLayoutInVipView = (LinearLayout)mVipView.findViewById(R.id.txt_send_gift_layout);
		sendGiftLayoutInVipView.setOnClickListener(unlockClickListener);
		LinearLayout giftCoinLayoutInVipView = (LinearLayout)mVipView.findViewById(R.id.txt_coin_layout);
		giftCoinLayoutInVipView.setOnClickListener(unlockClickListener);
		ImageView giftIconTvInVipView = (ImageView)mVipView.findViewById(R.id.gift_icon);
		giftIconTvInVipView.setOnClickListener(unlockClickListener);
		
		mPraiseView = (ImageView)root.findViewById(R.id.praise_anim);
		
		mContainer = (RelativeLayout)root.findViewById(R.id.container);
		mProgressBar = (ProgressBar)root.findViewById(R.id.progressbar);
		
		mPraiseLayout = (LinearLayout)root.findViewById(R.id.praise_layout);
		mPraiseLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(!isPraised) {
					mPraiseTranId = EgmService.getInstance().doPraisePriPic(Long.toString(userId),String.valueOf(mPictureInfo.id));
				} else {
					ToastUtil.showToast(getActivity(),R.string.has_praised);
				}
			}
		});
		mPraiseLayout.setEnabled(false);
		mPraiseIcon = (TextView)root.findViewById(R.id.praise_icon);
		mPraiseTxt = (TextView)root.findViewById(R.id.praise_num);
		
		mUnlikeLayout = (LinearLayout) root.findViewById(R.id.unlike_layout);
		mUnlikeLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(!isUnliked) {
					mUnLikeTranId = EgmService.getInstance().doUnlikePriPic(Long.toString(userId),String.valueOf(mPictureInfo.id));
				} else {
					ToastUtil.showToast(getActivity(),R.string.has_unliked);
				}
			}
		});
		mUnlikeLayout.setEnabled(false);
		mUnlikeIcon = (TextView)root.findViewById(R.id.unlike_icon);
		mUnlikeTxt = (TextView)root.findViewById(R.id.unlike_num);
		
		if(userId != ManagerAccount.getInstance().getCurrentId() && isPrivate) {
			mPraiseLayout.setVisibility(View.VISIBLE);
			mUnlikeLayout.setVisibility(View.VISIBLE);
		}
		if (isCameraPhoto == EgmConstants.IsCameraPhotoFlag.CameraPhoto) {
			mCameraPhotoTips = (TextView) root.findViewById(R.id.xian_pai_photo_tips);
			mCameraPhotoTips.setVisibility(View.VISIBLE);
		}
		renderView();
	}
	
	OnClickListener unlockClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (!isUnlockSuccess) {
				v.setClickable(false);
				selectUnLockView = (UnLockView) v.getParent().getParent().getParent();

				mProgressBar.setVisibility(View.VISIBLE);
				mUnlockTranId = EgmService.getInstance().doSendGift(
					String.valueOf(userId),
					null,
					String.valueOf(picId),
					EgmConstants.Send_Gift_From.TYPE_UNLOCK_PRIVATE_IMAGE);	
			}
		}
	};
	
	private void renderView(){
		if(isPrivate){
			EgmService.getInstance().doGetPrivateImage(userId,picId);
			return ;
		}
		renderView(mImageUrl);
	}
	
	private void renderView(String url){
		ImageViewTouch image = new ImageViewTouch(getActivity()){
			@Override
			public void onUiGetImage(int tid, Bitmap bitmap) {
				mProgressBar.setVisibility(View.GONE);
				setScaleType(ScaleType.MATRIX);
				super.onUiGetImage(tid, bitmap);
			}
			@Override
			public void onUiGetImageNull(int tid){
				mProgressBar.setVisibility(View.GONE);
				setScaleType(ScaleType.FIT_XY);
				Drawable d = getActivity().getResources().getDrawable(R.drawable.icon_photo_loaded_fail_with_bg);
                setImageDrawable(d);
			}
		};
		
		mContainer.addView(image,new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		image.allowTouch(true);
		
		image.setNeedLoadImageErrorCallBack(true);
		if (URLUtil.isFileUrl(url)) {
		    int displayWidth = (int)Math.min(mScreenWidth * 1.5, 1024);
			image.setLoadingImage(url,displayWidth,-1, ImageType.NoCache);
			//image.setLoadingImage(url);
		}
		else {
			image.setServerClipSize(mScreenWidth, mScreenHeight);
			image.setLoadingImage(url,ImageType.NoCache);
		}
	}
	
	private EgmCallBack mCallBack = new EgmCallBack(){
		@Override
		public void onGetSinPriImageSucess(int transactionId, GetSigPriResult obj) {
			if(obj != null){
				mPictureInfo = obj.privacyInfo ;
				renderView(mPictureInfo.picUrl);
				
				mPraiseTxt.setText("" + mPictureInfo.praiseCount);
				mUnlikeTxt.setText("" + mPictureInfo.stepCount);
				
				if(mPictureInfo.isViewed){
					mUnLockView.setVisibility(View.GONE);
					mVipView.setVisibility(View.GONE);
					
					mPraiseLayout.setEnabled(true);
					mUnlikeLayout.setEnabled(true);
				}else{
					if(obj.freeTimes > 0){
						mUnLockView.setVisibility(View.GONE);
						mVipView.setVisibility(View.VISIBLE);
						mVipView.initLogos(UnLockView.STATE_VIP,obj.freeTimes, -1);
						mVipView.initPraisePhoto(mPictureInfo.praiseCount, mPictureInfo.stepCount);
					}else{
						mUnLockView.setVisibility(View.VISIBLE);
						mUnLockView.initCoins(mPictureInfo.needCoins);
						mUnLockView.initLogos(UnLockView.STATE_COMMON, -1, mPictureInfo.giftId);
						mUnLockView.initPraisePhoto(mPictureInfo.praiseCount, mPictureInfo.stepCount);
					}
				}
			}
		}

		@Override
		public void onGetSinPriImageError(int transactionId, int errCode,String err) {
			ToastUtil.showToast(getActivity(),err);
		}
		
		@Override
		public void onSendGiftSucess(int transactionId, final SendGiftResult obj) {
			if(mUnlockTranId == transactionId){
				if(obj != null && !TextUtils.isEmpty(obj.privacyUrl)){
					isUnlockSuccess = true;
					mPraiseLayout.setEnabled(true);
					mUnlikeLayout.setEnabled(true);
					if(selectUnLockView != null) {
						selectUnLockView.postDelayed(new Runnable() {
							@Override
							public void run() {
								mProgressBar.setVisibility(View.GONE);
								
								if (EgmPrefHelper.getGiftsPicOn(getActivity(), ManagerAccount.getInstance().getCurrentId()))
									MediaPlayerSystemTone.instance(getActivity()).playWelecomTone("date_unlock.mp3");

								selectUnLockView.startUnlockAnim();
								selectUnLockView.postDelayed(new Runnable() {
									@Override
									public void run() {
									    if(getActivity() != null && !getActivity().isFinishing()){
    										if (selectUnLockView != null) {
    											TextView tv = (TextView)selectUnLockView.findViewById(R.id.txt_send_gift);
    											tv.setClickable(true);
    											selectUnLockView = null;
    										}
    										
    										mPictureInfo.picUrl = obj.privacyUrl ;
    										renderView(mPictureInfo.picUrl);
									    }
									}
								}, 1200);
							}
						}, 400);
					}
				}
			}
		}

		@Override
		public void onSendGiftError(int transactionId, int errCode, String err) {
			if(mUnlockTranId == transactionId){
				isUnlockSuccess = false;
				mProgressBar.setVisibility(View.GONE);
				if (selectUnLockView != null) {
					TextView tv = (TextView)selectUnLockView.findViewById(R.id.txt_send_gift);
					tv.setClickable(true);
					selectUnLockView = null;
				}
				
				ToastUtil.showToast(getActivity(),err);
				if(errCode == EgmServiceCode.TRANSACTION_COMMON_BALANCE_NOT_ENOUGHT){
					showChargeDialog();
				}
			}
		}
		
		@Override
		public void onPraisePicSucess(int transactionId, int code) {
			if (mPraiseTranId == transactionId) {
				PraisePhotoAnimation mPraiseAnim = new PraisePhotoAnimation();
				mPraiseAnim.setPraiseView(mPraiseView);
				mPraiseAnim.startAnimation();
				
				ToastUtil.showToast(getActivity(),R.string.praise_pic_suc);
				mPraiseTxt.setTextColor(getResources().getColor(R.color.content_text));
				mPraiseTxt.setText(String.valueOf(mPictureInfo.praiseCount + 1) + getResources().getString(R.string.praised));
				ViewCompat.setBackground(mPraiseIcon,getResources().getDrawable(R.drawable.icon_photo_love_prs));
				isPraised = true;
			}
		}

		@Override
		public void onPraisePicError(int transactionId, int errCode, String err) {
			if (mPraiseTranId == transactionId) {
				if(errCode == EgmServiceCode.TRANSACTION_PICTURE_HAS_PRAISED){
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
				mUnlikeTxt.setTextColor(getResources().getColor(R.color.content_text));
				mUnlikeTxt.setText(String.valueOf(mPictureInfo.stepCount + 1) + getResources().getString(R.string.unliked));
				ViewCompat.setBackground(mUnlikeIcon, getResources().getDrawable(R.drawable.icon_photo_unlike_prs));
				isUnliked = true;
			}
		}
		
		@Override
		public void onUnlikePicError(int transactionId, int errCode, String err) {
			if (mUnLikeTranId == transactionId) {
				if(errCode == EgmServiceCode.TRANSACTION_PICTURE_HAS_UNLIKED){
					ToastUtil.showToast(getActivity(),R.string.has_unliked);
				}else{
					ToastUtil.showToast(getActivity(),err);
				}
			}
		}
	};
	
	/**
	 * 充值
	 */
	private AlertDialog mChargeDialog ;
	private void showChargeDialog(){
		if(mChargeDialog == null){
			mChargeDialog = EgmUtil.createEgmMenuDialog(
					getActivity(), 
					getActivity().getResources().getString(R.string.coins_not_enough_private_photo), 
					new CharSequence[]{getActivity().getResources().getString(R.string.go_to_charge)}, 
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							ActivityWeb.startCoinCharge(getActivity());
							mChargeDialog.dismiss();
						}
					});
		}
		mChargeDialog.show();
	}
}
