package com.netease.engagement.fragment;

import java.util.ArrayList;
import java.util.Arrays;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityComplain;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.activity.ActivityImageList;
import com.netease.engagement.activity.ActivityPageInfo;
import com.netease.engagement.activity.ActivityPrivateSession;
import com.netease.engagement.activity.ActivityProfileExplore;
import com.netease.engagement.adapter.UserGiftPagerAdapter;
import com.netease.engagement.adapter.YixinHelper;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.ConfigDataManager;
import com.netease.engagement.view.HeadView;
import com.netease.engagement.view.ImageFillLayout;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.engagement.widget.LoadingImageView;
import com.netease.engagement.widget.ProgerssImageView;
import com.netease.engagement.widget.UserInfoUtil;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmProtocolConstants.Block_Type;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.ChatItemUserInfo;
import com.netease.service.protocol.meta.PictureInfo;
import com.netease.service.protocol.meta.UserInfo;
import com.netease.service.protocol.meta.UserInfoConfig;
import com.netease.service.protocol.meta.UserInfoDetail;
import com.netease.service.stat.EgmStat;


/**
 * 男性会员资料页面
 * 传入参数：userId 
 */
public class FragmentUserPageMan extends FragmentBase{
	private static String FROM_SESSION = "FROM_SESSION";

	public static FragmentUserPageMan newInstance(String uid, boolean isFromSession){
		FragmentUserPageMan fragment = new FragmentUserPageMan();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		args.putString(EgmConstants.BUNDLE_KEY.USER_ID, uid);
		args.putBoolean(FROM_SESSION, isFromSession);
		return fragment ;
	}
	
	private boolean mIsFromSession = false;
	
	private CustomActionBar mCustomActionBar ;
	
	//顶部背景图
	private ImageView mBackImage ;
	
	//网络异常view
	private View mEmptyView;
	
	
	private ScrollView mScrollView;
	//头像
	private TextView mUserNick ;
	private HeadView mProfileView ;
	private TextView mLevel ;
	private TextView mRichIndex ;
	
	//易信
	private LinearLayout mAddYX ;
	private View mYixinIcon ;
	private TextView mYixinText ;
	private TextView mNotifyYixinTip ;
	//照片
	private TextView mTagImage ;
	private LinearLayout mScrollLayout ;
	
	
	// 聊天技
	private TextView mTagTaklSkill;
	private LinearLayout mTalkSkillLayout;
	
	//礼物
	private TextView mTagGift ;
	private ViewPager mGiftPager ;
	private UserGiftPagerAdapter mGiftAdapter ;
	private ImageFillLayout mAutoFillLayout ;
	//资料
	private TextView mTagData ;
	private TextView mDataAge ;
	private TextView mDataHeight ;
	private TextView mDataCol ;
	private TextView mDataCity ;
	private TextView mDataIncome ;
	//更多
	private TextView mTagMore ;
	//自我介绍
	private TextView mIntroduce ;
	private TextView mIntrContent ;
	private View mIntrHori ;
	//喜欢的约会
	private TextView mTagDate ;
	private TextView mFavorDateView ; 
	private View mDateHori ;
	//兴趣爱好
	private TextView mTagHobby;
	private TextView mHobbyView ;
	private View mHobbyHori ;
	//技能
	private TextView mTagSkill ;
	private TextView mSkillView ;
	private View mSkillHori ;
	
	//隐藏资料
	private LinearLayout mPageTail ;
	
	//聊天
	private LinearLayout mUserPageBottomLayout ;
	private RelativeLayout mChatLayout ;
	private TextView mChatIcon ;
	/** 双方已是好友。临时标记，因为添加好友成功后，mUserInfoDetail.IsYixinFriend没有马上刷新 */
    private boolean mIsYixinFriend = false;
	
	private int mScreenWidth ;
	private int mScreenHeight ;
	
	//用户信息
	private long mUid ;
	private long mMyUid ;
	private int mGender ;
	private UserInfoConfig mUserInfoConfig ;
	private UserInfoDetail mUserInfoDetail ;
	private UserInfo mUserInfo ;
	private static final float FRACTION_IMAGE = 480f/1280 ; 
	
	private boolean isBlock ;
	
	private YixinHelper mYixinHelper ;
	private int mTid;//请求id
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (this.getArguments() == null
				|| TextUtils.isEmpty(getArguments().getString(EgmConstants.BUNDLE_KEY.USER_ID))) {
			return;
		}
		
		mUid = Long.parseLong(getArguments().getString(EgmConstants.BUNDLE_KEY.USER_ID));
		mMyUid = ManagerAccount.getInstance().getCurrentId() ;
		mGender = ManagerAccount.getInstance().getCurrentGender() ;
		mIsFromSession = getArguments().getBoolean(FROM_SESSION, false);
		
		EgmService.getInstance().addListener(mCallBack);
		
		mYixinHelper = new YixinHelper(this, new YixinHelper.IYixinCallback() {
            @Override
            public void onAlreadyFriend() {
                mIsYixinFriend = true;
                
                if(mAddYX != null){
                    mAddYX.setVisibility(View.GONE);
                    mNotifyYixinTip.setVisibility(View.GONE);
                }
                
//                if(mChatIcon != null){
//                    ViewCompat.setBackground(mChatIcon, getResources().getDrawable(R.drawable.button_yixin_selector));
//                }
            }
        }, YixinHelper.TYPE_TO_ADD_FRIEND);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.fragment_user_page_man, container,false);
		init(root);
		
		return root;
	}
	
	private void initTitle(){
		mCustomActionBar = ((ActivityEngagementBase)getActivity()).getCustomActionBar();
		mCustomActionBar.setMiddleTitleColor(getResources().getColor(R.color.white));
		mCustomActionBar.setMiddleTitle("");
		
		mCustomActionBar.setLeftBackgroundResource(R.drawable.titlebar_c_selector);
		mCustomActionBar.setLeftAction(R.drawable.bar_btn_back_b, R.string.back);
		mCustomActionBar.setLeftTitleColor(getResources().getColor(R.color.white));
		
		mCustomActionBar.setRightBackgroundResource(R.drawable.titlebar_c_selector);
		mCustomActionBar.setRightAction(-1, R.string.more);
		mCustomActionBar.setRightTitleColor(getResources().getColor(R.color.white));
		mCustomActionBar.setRightClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showMoreDialog();
			}
		});
		  if(mUid == mMyUid || mGender == EgmConstants.SexType.Male){
	            mCustomActionBar.hideRightTitle();
	        }
	}
	
	private void init(View root){
		mUserInfoConfig = ConfigDataManager.getInstance().getUConfigFromData();
		if(mUserInfoConfig == null){
			return ;
		}
		
		mScrollView = (ScrollView) root.findViewById(R.id.top_layout);
		mEmptyView = root.findViewById(R.id.empty_tip);
		mEmptyView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(mTipType == TYPE_NET_ERROR) {
					mTid = EgmService.getInstance().doGetUserInfoDetail(mUid);
					showWatting("加载中");
				}
			}
		});
		
		root.findViewById(R.id.data_man_layout).setVisibility(View.VISIBLE);
		mScreenWidth = this.getResources().getDisplayMetrics().widthPixels ;
		mScreenHeight = this.getResources().getDisplayMetrics().heightPixels ;
		
		//背景图片，开始即加载
		mBackImage = (ImageView)root.findViewById(R.id.background);
		
		//通过固定的高宽比例来压缩背景图片
		mBackImage.getLayoutParams().height = (int) (mScreenHeight * FRACTION_IMAGE);
		mBackImage.setImageResource(R.drawable.bg_pgregister_head);
		
		mProfileView = (HeadView) root.findViewById(R.id.profile);
		mProfileView.setVisibility(View.INVISIBLE);
		
		mProfileView.setOnClickListener(mOnClickListener);
		mProfileView.setImageUrl(false,HeadView.PROFILE_SIZE_LARGE,null,
				EgmConstants.SexType.Male,R.drawable.bg_portrait_circle_200x200,getResources().getDimensionPixelSize(R.dimen.userpage_avatar_border_dp));
		
		mUserNick = (TextView)root.findViewById(R.id.user_nick);
		//会员等级
		mLevel = (TextView)root.findViewById(R.id.man_level);
		//豪气值
		mRichIndex = (TextView)root.findViewById(R.id.rich_index);
		mRichIndex.setVisibility(View.INVISIBLE);
		
		//添加易信好友
		mAddYX = (LinearLayout)root.findViewById(R.id.add_yx);
		mAddYX.setOnClickListener(mOnClickListener);
		mYixinIcon = root.findViewById(R.id.yixin_icon);
		mYixinText = (TextView)root.findViewById(R.id.yixin_txt);
		mNotifyYixinTip = (TextView)root.findViewById(R.id.notify_yixin_tip);
		
		//照片tag
		mTagImage = (TextView)root.findViewById(R.id.tag_image);
		mTagImage.setText(String.format(getString(R.string.image_num), 0));
		
		if(mUid != mMyUid && mGender != EgmConstants.SexType.Male){
			mTagImage.setOnClickListener(mOnClickListener);
		}
		
		mScrollLayout = (LinearLayout)root.findViewById(R.id.scroll_layout);
		//礼物tag
		mTagGift = (TextView)root.findViewById(R.id.tag_gift);
		mTagGift.setText(String.format(getString(R.string.send_gift_num), 0));
		mGiftPager = (ViewPager)root.findViewById(R.id.gift_list);
		mAutoFillLayout = (ImageFillLayout)root.findViewById(R.id.auto_fill_layout);
		
		// 聊天技tag
		mTagTaklSkill = (TextView) root.findViewById(R.id.tag_talkskill);
		mTagTaklSkill.setVisibility(View.GONE);
		mTalkSkillLayout = (LinearLayout) root.findViewById(R.id.talkskill_layout);
		mTalkSkillLayout.setVisibility(View.GONE);
		
		//资料tag
		mTagData = (TextView)root.findViewById(R.id.tag_data);
		mTagData.setText(getString(R.string.pri_user_data));
		mDataAge = (TextView)root.findViewById(R.id.data_age_man);
		mDataHeight = (TextView)root.findViewById(R.id.data_height_man);
		mDataCol = (TextView)root.findViewById(R.id.data_co_man);
		mDataCity = (TextView)root.findViewById(R.id.data_city_man);
		mDataIncome = (TextView)root.findViewById(R.id.data_income_man);
		//自我介绍
		mIntroduce = (TextView)root.findViewById(R.id.introduce_tag);
		mIntrContent = (TextView)root.findViewById(R.id.introduce_content);
		mIntrHori = root.findViewById(R.id.intr_hori);
		//喜欢的约会
		mTagDate = (TextView)root.findViewById(R.id.date_tag);
		mDateHori = root.findViewById(R.id.date_hori);
		mFavorDateView = (TextView)root.findViewById(R.id.favor_date_content);
		//兴趣爱好
		mTagHobby = (TextView)root.findViewById(R.id.hobby_tag);
		mHobbyHori = root.findViewById(R.id.hobby_hori);
		mHobbyView = (TextView)root.findViewById(R.id.hobby_content);
		//擅长的技能
		mTagSkill = (TextView)root.findViewById(R.id.tag_skill);
		mSkillHori = root.findViewById(R.id.skill_hori);
		mSkillView = (TextView)root.findViewById(R.id.skill_content);
		//更多
		mTagMore = (TextView)root.findViewById(R.id.more_data);
		mTagMore.setText(getString(R.string.see_more_data));
		mTagMore.setOnClickListener(mOnClickListener);
		
		mTagMore.setVisibility(View.GONE);
		
		mPageTail = (LinearLayout)root.findViewById(R.id.user_pager_tail);
		
		mUserPageBottomLayout = (LinearLayout)root.findViewById(R.id.user_page_bottom_layout);
		
		mChatLayout = (RelativeLayout)root.findViewById(R.id.chat_layout);
		mChatLayout.setOnClickListener(mOnClickListener);
		mChatIcon = (TextView)root.findViewById(R.id.chat_icon);
		
		if(mGender == EgmConstants.SexType.Male){
			mUserPageBottomLayout.setVisibility(View.GONE);
		}
		
//		mTid = EgmService.getInstance().doGetUserInfoDetail(mUid);
//		showWatting("加载中");
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initTitle();
		
		try {
			startLayoutAnimation();
		} catch (Exception e) {
		}
	}
	
	private AlertDialog mShowMoreDialog ;
	private void showMoreDialog(){
		String[] operations = new String[2];
		if(isBlock){
			operations = getActivity().getResources().getStringArray(R.array.show_more_operation_other);
		}else{
			operations = getActivity().getResources().getStringArray(R.array.show_more_operation);
		}
		mShowMoreDialog = EgmUtil.createEgmMenuDialog(
				getActivity(),
				getActivity().getResources().getString(R.string.more_operation), 
				operations, 
				new OnClickListener(){
					@Override
					public void onClick(View v) {
						int which = (Integer) v.getTag();
						switch(which){
							case 0:
								/**
								 * 举报
								 */
								ActivityComplain.startActivity(FragmentUserPageMan.this,mUserInfo.uid);
								break;
							case 1:
								/**
								 * 加黑
								 */
								if(isBlock){
									showCancelBlock();
								}else{
									showAddBlock();
								}
								break;
						}
						mShowMoreDialog.dismiss();
					}
				});
		mShowMoreDialog.show();
	}
	
	private AlertDialog mShowAddBlock ;
	private void showAddBlock(){
		if(mShowAddBlock == null){
			mShowAddBlock = EgmUtil.createEgmMenuDialog(
					getActivity(), 
					getActivity().getResources().getString(R.string.block_tip), 
					new CharSequence[]{getActivity().getResources().getString(R.string.block_confirm)}, 
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							EgmService.getInstance().doAddBlack(Block_Type.BLOCK_CONFIRM, mUserInfo.uid);
							mShowAddBlock.dismiss();
						}
					});
		}
		mShowAddBlock.show();
	}
	
	private AlertDialog mShowCancelBlock ;
	private void showCancelBlock(){
		if(mShowCancelBlock == null){
			mShowCancelBlock = EgmUtil.createEgmMenuDialog(
					getActivity(), 
					getActivity().getResources().getString(R.string.block_cancel_confirm), 
					new CharSequence[]{"确定"}, 
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							EgmService.getInstance().doAddBlack(Block_Type.BLOCK_CANCEL, mUserInfo.uid);
							mShowCancelBlock.dismiss();
						}
					});
		}
		mShowCancelBlock.show();
	}
	
	private EgmCallBack mCallBack = new EgmCallBack(){
		@Override
		public void onGetUserInfoDetailSucess(int transactionId,UserInfoDetail obj) {
			if(obj == null){
				return ;
			}
			stopWaiting();
			showTip(TYPE_CONTENT);
			mUserInfoDetail = obj ;
			mUserInfo = mUserInfoDetail.userInfo ;
			refreshViews();
		}

		@Override
		public void onGetUserInfoDetailError(int transactionId, int errCode,String err) {
			if(mTid != transactionId){
				return;
			}
			if(errCode == EgmServiceCode.NETWORK_ERR_COMMON) {
				stopWaiting();
				showTip(TYPE_NET_ERROR);
			} else if(errCode == EgmServiceCode.TRANSACTION_CHAT_RECEIVE_SIDE_FROZEN){
				stopWaiting();
				showCloseDialog();
				return ;
			}
			ToastUtil.showToast(getActivity(), err);
		}

		@Override
		public void onBlockSucess(int transactionId, int code,long uid) {
			isBlock = !isBlock ;
			if(isBlock){
				ToastUtil.showToast(getActivity(),R.string.block_suc_man);
			}else{
				ToastUtil.showToast(getActivity(),R.string.cancel_block_suc);
			}
		}

		@Override
		public void onBlockError(int transactionId, int errCode, String err) {
			ToastUtil.showToast(getActivity(),err);
		}

		@Override
		public void onModifyDetailInfoSucess(int transactionId, UserInfo obj) {
			if(obj != null){
				mUserInfo = obj ;
				fillData();
			}
		}
	};
	
	private AlertDialog mShowCloseDialog ;
	private void showCloseDialog(){
		if(mShowCloseDialog == null){
			String title = getActivity().getResources().getString(R.string.dongjie_cannot_get_userInfo) ;
			mShowCloseDialog = EgmUtil.createEgmMenuDialog(
					getActivity(), 
					title, 
					new CharSequence[]{getActivity().getResources().getString(R.string.confirm)}, 
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mShowCloseDialog.dismiss();
							getActivity().finish();
						}
					});
		}
		mShowCloseDialog.setCanceledOnTouchOutside(false);
		
		//点击back键返回，两种实现方式
		//方式一：
		/*mShowIsBlocked.setOnCancelListener(new OnCancelListener(){
			@Override
			public void onCancel(DialogInterface dialog) {
				getActivity().finish();
			}
		});*/
		//方式二：
		mShowCloseDialog.setOnKeyListener(new OnKeyListener(){
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_BACK){
					mShowCloseDialog.dismiss();
					getActivity().finish();
				}
				return false;
			}
		});
		mShowCloseDialog.show();
	}
	
	
	private OnClickListener mOnClickListener = new OnClickListener(){
		@Override
		public void onClick(View view) {
			switch(view.getId()){
				case R.id.more_data:
					view.setVisibility(View.GONE);
					mPageTail.setVisibility(View.VISIBLE);
					
					EgmStat.log(EgmStat.LOG_CLICK_INTRODUCTION_DETAIL, 
							EgmStat.SCENE_USER_DETAIL, mUid);
					break;
				case R.id.add_yx:
					/**
					 * 添加易信好友或者查看我的资料
					 */
					if(mUid == mMyUid && mUserInfo != null){
						ActivityPageInfo.startActivityForResult(
								FragmentUserPageMan.this,
								String.valueOf(ActivityPageInfo.DETAIL_INFO), 
								mUserInfo);
					}else{
						//添加易信好友
					    mYixinHelper.addFriend(mUid);
					}
					break;
				case R.id.tag_image:
					/**
					 * 进入图集查看公开照片
					 */
					ArrayList<PictureInfo> infoList = new ArrayList<PictureInfo>();
					for(PictureInfo item : mUserInfoDetail.publicPicList){
						infoList.add(item);
					}
					ActivityImageList.startActivity(getActivity(), mUserInfo.uid ,infoList, 0);
					break;
				case R.id.chat_layout:
					/**
					 * 聊天
					 */
					if(mUserInfo == null){
                        ToastUtil.showToast(getActivity(),"等待获取用户数据");
                        return ;
                    }
					
					if(mUserInfo == null){
                        ToastUtil.showToast(getActivity(),"等待获取用户数据");
                        return ;
                    }
					if(isBlock){
						ToastUtil.showToast(getActivity(),R.string.block_cannot_chat_tip_man);
						return ;
					}
//	changed by echo-chen 2014-10-30	改成无论易信好友都是去聊天页面			
//					if (mUserInfoDetail.IsYixinFriend  || mIsYixinFriend) {    // 易信好友，用易信聊天
//	                    if(!mYixinHelper.startYixin()){
//	                        ToastUtil.showToast(getActivity(), R.string.yixin_no_install);
////	                    }
////	                }
//	                else{   // 不是易信好友，在内部聊天
					
	                	if (mIsFromSession) {
	                		Context context = view.getContext();
	    			        if(context instanceof Activity){
	    			        	Activity activity = (Activity) context;
	    			        	activity.finish();
	    			        }
	                	} else {
	                		ChatItemUserInfo info = new ChatItemUserInfo();
		                    info.uid = mUserInfo.uid ;
		                    info.crownId = mUserInfo.crownId ;
		                    info.nick = mUserInfo.nick ;
		                    info.portraitUrl192 = mUserInfo.portraitUrl192 ;
		                    info.isNew = mUserInfo.isNew;
		                    ActivityPrivateSession.startActivityFromUserinfo(getActivity(), info);
	                	}
//	                }
					
					break;
				case R.id.profile:
					mScrollView.fullScroll(ScrollView.FOCUS_UP);
					mScrollView.postDelayed(new Runnable() {
						@Override
						public void run() {
							if(mUserInfo != null) {
								ActivityProfileExplore.startActivity(getActivity(), mUserInfo.portraitUrl, EgmConstants.SexType.Male);
						
							}
						}
					}, 200);
					break;
			}
		}
	};
	
	private final static int TYPE_CONTENT = 0;
	private final static int TYPE_NET_ERROR = 1;
	private int mTipType;
	
	private void showTip(int type) {
		mTipType = type;
		switch (type) {
		case TYPE_CONTENT:
			mScrollView.setVisibility(View.VISIBLE);
			if(mGender == EgmConstants.SexType.Male) {
				mUserPageBottomLayout.setVisibility(View.GONE);
			} else {
				mUserPageBottomLayout.setVisibility(View.VISIBLE);
			}
			if(mUid != mMyUid && mGender != EgmConstants.SexType.Male){
				mCustomActionBar.setRightVisibility(View.VISIBLE);
			}
			mEmptyView.setVisibility(View.GONE);
			break;

		case TYPE_NET_ERROR:
			mScrollView.setVisibility(View.GONE);
			mUserPageBottomLayout.setVisibility(View.GONE);
			mEmptyView.setVisibility(View.VISIBLE);
			mCustomActionBar.setRightVisibility(View.GONE);
			((TextView)mEmptyView.findViewById(R.id.empty_text)).setText(R.string.common_reload_tip);
			break;
		}
	}
	
	private void refreshViews(){
		if(mUserInfo == null || getActivity() == null){
			return ;
		}
		
		isBlock = mUserInfoDetail.isBlack ;
		
		//小头像
		if(!TextUtils.isEmpty(mUserInfo.portraitUrl192)){
			mProfileView.setImageUrl(mUserInfo.isVip,HeadView.PROFILE_SIZE_LARGE,mUserInfo.portraitUrl192,
					EgmConstants.SexType.Male,R.drawable.bg_portrait_circle_200x200,getResources().getDimensionPixelSize(R.dimen.userpage_avatar_border_dp));
		}
		
		mProfileView.setVisibility(View.VISIBLE);
		
		fillData();
		fillImageData();
		
		switch(mGender){
			case EgmConstants.SexType.Female:
//				if(mUserInfoDetail.IsYixinFriend){
//					mAddYX.setVisibility(View.GONE);
//					mNotifyYixinTip.setVisibility(View.GONE);
//				}else{
//					mAddYX.setVisibility(View.VISIBLE);
//					mNotifyYixinTip.setVisibility(View.VISIBLE);
//				}
			    mAddYX.setVisibility(View.GONE);
                mNotifyYixinTip.setVisibility(View.GONE);
				break;
			case EgmConstants.SexType.Male:
				if(mUid == mMyUid){
					mAddYX.setVisibility(View.VISIBLE);
					mYixinIcon.setVisibility(View.GONE);
					mNotifyYixinTip.setVisibility(View.GONE);
					mAddYX.setGravity(Gravity.CENTER);
					mYixinText.setText(R.string.edit_my_info);
				}else{
					mAddYX.setVisibility(View.GONE);
					mNotifyYixinTip.setVisibility(View.GONE);
				}
				break;
		}
		
		//礼物列表
		if(mUserInfoDetail.giftList == null || mUserInfoDetail.giftList.length == 0){
			mTagGift.setVisibility(View.GONE);
		}else{
			mTagGift.setText(String.format(getString(R.string.send_gift_num), mUserInfoDetail.userInfo.giftCount));
			
			if(mUserInfoDetail.giftList.length >= 5){
				mGiftPager.setVisibility(View.VISIBLE);
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,mScreenWidth/2);
				mGiftPager.setLayoutParams(lp);
				mGiftAdapter = new UserGiftPagerAdapter(getActivity(),
						Arrays.asList(mUserInfoDetail.giftList), EgmConstants.SexType.Male);
				mGiftPager.setAdapter(mGiftAdapter);
			}else{
				mAutoFillLayout.setVisibility(View.VISIBLE);
				mAutoFillLayout.fillLayout(Arrays.asList(mUserInfoDetail.giftList),0);
			}
		}
		
		AlphaAnimation aam = new AlphaAnimation(0, 1);
		aam.setDuration(200);
		
		mProfileView.startAnimation(aam);
		mRichIndex.startAnimation(aam);
		
		if (mScrollLayout.getVisibility() == View.VISIBLE) {
			mScrollLayout.startAnimation(aam);
		}
	}
	
	private void fillData(){
		mUserNick.setText(mUserInfo.nick);
		//等级
		if(!TextUtils.isEmpty(mUserInfo.levelName) && mUserInfo.level != 0){
			mLevel.setVisibility(View.VISIBLE);
			String levelStr = String.format(getResources().getString(R.string.level_num),
					mUserInfo.level);
			//显示等级
			mLevel.setText(levelStr+" "+ mUserInfo.levelName);
		}
		
		mRichIndex.setVisibility(View.VISIBLE);
		//男性豪气值
		if(mUserInfo.usercp > 999999999){
			mRichIndex.setText(getString(R.string.rich_index_a)+ 999999999);
		}else{
			mRichIndex.setText(getString(R.string.rich_index_a)+ String.valueOf(mUserInfo.usercp));
		}
		
		//如果“自我介绍”、“喜欢的约会”、“兴趣爱好”、“我擅长的技能”、“社交网络”全部都为空，则不显示“查看完整资料”操作
		if(TextUtils.isEmpty(mUserInfo.introduce) 
				&& (mUserInfo.favorDate == null || mUserInfo.favorDate.length == 0)
				&& (mUserInfo.hobby == null || mUserInfo.hobby.length == 0)
				&& (mUserInfo.skill == null || mUserInfo.skill.length == 0)
				&& TextUtils.isEmpty(mUserInfo.socialUrl)){
			mTagMore.setVisibility(View.GONE);
		}
		else {
			mTagMore.setVisibility(View.GONE);
			mPageTail.setVisibility(View.VISIBLE);
		}
		
		//资料
		if(mUserInfo.age != 0){
			mDataAge.setVisibility(View.VISIBLE);
			mDataAge.setText(String.format(getString(R.string.age_unit),mUserInfo.age));
		}else{
			mDataAge.setVisibility(View.GONE);
		}
		
		if(mUserInfo.height != 0){
			mDataHeight.setVisibility(View.VISIBLE);
			mDataHeight.setText(String.format(getString(R.string.height_unit_big),mUserInfo.height));
		}else{
			mDataHeight.setVisibility(View.GONE);
		}
		
		if(mUserInfo.constellation != 0){
			mDataCol.setVisibility(View.VISIBLE);
			mDataCol.setText(UserInfoUtil.getConstellation(mUserInfo,mUserInfoConfig));
		}else{
			mDataCol.setVisibility(View.GONE);
		}
		
		if(mUserInfo.province != 0){
			mDataCity.setVisibility(View.VISIBLE);
			String str = getString(R.string.city);
			mDataCity.setText(
					getSSb(str+UserInfoUtil.getLocation(getActivity(), mUserInfo),
					0,
					str.length()));
		}else{
			mDataCity.setVisibility(View.GONE);
		}
		
		if(mUserInfo.income != 0){
			mDataIncome.setVisibility(View.VISIBLE);
			String str = getString(R.string.income_unit);
			mDataIncome.setText(
					getSSb(str + UserInfoUtil.getIncome(mUserInfo.income,mUserInfoConfig),
					0,
					str.length()));
		}else{
			mDataIncome.setVisibility(View.GONE);
		}
		
		//自我介绍
		if(TextUtils.isEmpty(mUserInfo.introduce)){
			mIntrHori.setVisibility(View.GONE);
			mIntroduce.setVisibility(View.GONE);
			mIntrContent.setVisibility(View.GONE);
		}else{
			mIntrHori.setVisibility(View.VISIBLE);
			mIntroduce.setVisibility(View.VISIBLE);
			mIntrContent.setVisibility(View.VISIBLE);
			mIntrContent.setText(mUserInfo.introduce);
		}
		
		//喜欢的约会
		if(mUserInfo.favorDate == null || mUserInfo.favorDate.length == 0
				|| TextUtils.isEmpty(UserInfoUtil.getDateText(mUserInfo, mUserInfoConfig))){
			mTagDate.setVisibility(View.GONE);
			mDateHori.setVisibility(View.GONE);
			mFavorDateView.setVisibility(View.GONE);
		}else{
			mTagDate.setVisibility(View.VISIBLE);
			mDateHori.setVisibility(View.VISIBLE);
			mFavorDateView.setVisibility(View.VISIBLE);
			mFavorDateView.setText(UserInfoUtil.getDateText(mUserInfo, mUserInfoConfig));
		}
		
		//兴趣爱好
		if(mUserInfo.hobby == null || mUserInfo.hobby.length == 0
				|| TextUtils.isEmpty(UserInfoUtil.getMaleHobbyText(mUserInfo, mUserInfoConfig))){
			mTagHobby.setVisibility(View.GONE);
			mHobbyHori.setVisibility(View.GONE);
			mHobbyView.setVisibility(View.GONE);
		}else{
			mTagHobby.setVisibility(View.VISIBLE);
			mHobbyHori.setVisibility(View.VISIBLE);
			mHobbyView.setVisibility(View.VISIBLE);
			mHobbyView.setText(UserInfoUtil.getMaleHobbyText(mUserInfo, mUserInfoConfig));
		}
		
		//擅长的技能
		if(mUserInfo.skill == null || mUserInfo.skill.length == 0
				|| TextUtils.isEmpty(UserInfoUtil.getSkillText(mUserInfo, mUserInfoConfig))){
			mTagSkill.setVisibility(View.GONE);
			mSkillHori.setVisibility(View.GONE);
			mSkillView.setVisibility(View.GONE);
		}else{
			mTagSkill.setVisibility(View.VISIBLE);
			mSkillHori.setVisibility(View.VISIBLE);
			mSkillView.setVisibility(View.VISIBLE);
			mSkillView.setText(UserInfoUtil.getSkillText(mUserInfo, mUserInfoConfig));
		}
		
		// 聊天按钮换成易信logo
//        if(mUserInfoDetail.IsYixinFriend){
//            ViewCompat.setBackground(mChatIcon, getResources().getDrawable(R.drawable.button_yixin_selector));
//        }
	}
	
	private SpannableStringBuilder getSSb(String str , int start ,int end){
		SpannableStringBuilder ssb = new SpannableStringBuilder(str);
		ssb.setSpan(
				new ForegroundColorSpan(getResources().getColor(R.color.info_audio_txt_color)), 
				start,
				end, 
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return ssb ;
	}
	
	/**
	 * 填充照片列表
	 */
	private void fillImageData(){
		
		//照片列表
		if(mUserInfoDetail.publicPicList == null || mUserInfoDetail.publicPicList.length == 0){
			mTagImage.setVisibility(View.GONE);
			mScrollLayout.setVisibility(View.GONE);
			return ;
		}
		
		mScrollLayout.removeAllViews();
		mTagImage.setText(String.format(getString(R.string.image_num), mUserInfoDetail.publicPicList.length));
		
		int paddingLeft = EgmUtil.dip2px(getActivity(),16);
		int imageListMargin = EgmUtil.dip2px(getActivity(), 4);
		int paddingRight = EgmUtil.dip2px(getActivity(), 56);
		
		//获取公开照片列表
		PictureInfo[] pictures = mUserInfoDetail.publicPicList ;
		int imageWidth = (mScreenWidth - paddingLeft - 3*imageListMargin - paddingRight)/3 ;
		
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(imageWidth,imageWidth);
		lp.bottomMargin = imageListMargin ;
		lp.rightMargin = imageListMargin ;
		
		for(int i = 0;i < pictures.length;i++){
			LinearLayout item = new LinearLayout(getActivity());
						
//			LoadingImageView imageView = new LoadingImageView(getActivity());

//added by lishang 改用带有进度条的ProgerssImageView代替LoadingImageView
			ProgerssImageView progerssImageView=new ProgerssImageView(getActivity());
			progerssImageView.setBackgroundColor(getResources().getColor(R.color.rec_no_picture_bg));
			LoadingImageView imageView=progerssImageView.mImageView;

			imageView.setServerClipSize(imageWidth, imageWidth);
			imageView.setLoadingImage(pictures[i].picUrl);
			imageView.setScaleType(ScaleType.CENTER_CROP);
			imageView.setScaleTop(true);

			
//			//添加image
//			item.addView(imageView,new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
//					LayoutParams.MATCH_PARENT));
			
			item.addView(progerssImageView,new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
					RelativeLayout.LayoutParams.MATCH_PARENT));
			final int index = i ;
			if(mGender != EgmConstants.SexType.Male){
				item.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						/**
						 * 进入图集查看公开照片
						 */
						ArrayList<PictureInfo> picInfos = new ArrayList<PictureInfo>();
						for(PictureInfo item : mUserInfoDetail.publicPicList){
							picInfos.add(item);
						}
						//显示第i张图片
//						ActivityImageBrowser.startActivity(getActivity(),picInfos,index,false);
						int ii = index;
						if (ii != 0 && ii != mUserInfoDetail.publicPicList.length-1) {
							ii = ii + 1;
						}
						ActivityImageList.startActivity(getActivity(), mUserInfo.uid, picInfos, ii);
					}
				});
			}
			mScrollLayout.addView(item,lp);
		}
	}
	
	private void startLayoutAnimation() {
		final int offset = getActivity().getResources().getDimensionPixelSize(
				R.dimen.userpage_animation_data_off);
		
		final int height = mBackImage.getLayoutParams().height;
		ValueAnimator sa = ValueAnimator.ofInt(height + offset, height);
		
		sa.setDuration(350);
		sa.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator va) {
				int value = (Integer) va.getAnimatedValue();
				mBackImage.getLayoutParams().height = value;
				mBackImage.requestLayout();
			}
		});
		
		sa.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				mTid = EgmService.getInstance().doGetUserInfoDetail(mUid);
				showWatting("加载中");
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				
			}
		});
		sa.start();
		
		if (mUserPageBottomLayout.getVisibility() == View.VISIBLE) {
			mUserPageBottomLayout.setTranslationY(offset);
			
			new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					mUserPageBottomLayout.setTranslationY(0);
					
					TranslateAnimation ta = new TranslateAnimation(0, 0, offset, 0);
					ta.setDuration(250);
					
					mUserPageBottomLayout.startAnimation(ta);
				}
			}, 100);
		}
	}
	
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(isEventWithinView(ev, mUserPageBottomLayout) && !isEventWithinView(ev, mChatLayout)) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isEventWithinView(MotionEvent e, View child) {
		if(e == null || child == null) {
			return false;
		}
        Rect viewRect = new Rect();
        int[] childPosition = new int[2];
        child.getLocationOnScreen(childPosition);
        int left = childPosition[0];
        int right = left + child.getWidth();
        int top = childPosition[1];
        int bottom = top + child.getHeight();
        viewRect.set(left, top, right, bottom);
        return isEventWithinRect(e,viewRect);
    }
	
	private boolean isEventWithinRect(MotionEvent e , Rect rect){
		//TODO验证rect是否合法
		if(e == null || rect == null){
			return false ;
		}
		return rect.contains((int) e.getRawX(), (int) e.getRawY());
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		EgmService.getInstance().removeListener(mCallBack);
		
		mShowCancelBlock = null;
		mShowAddBlock = null;
		mShowMoreDialog = null;
		mShowCloseDialog = null;
		
		mYixinHelper.removeCallback();
	}
	
	public void showGiftExploreView(int giftId) {
		//do nothing;
	}
}
