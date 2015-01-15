package com.netease.engagement.fragment;

import java.util.ArrayList;
import java.util.Arrays;

import uk.co.senab.photoview.PhotoViewAttacher.OnViewTapListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.internal.ViewCompat;
import com.netease.date.R;
import com.netease.date.R.id;
import com.netease.engagement.activity.ActivityComplain;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.activity.ActivityImageList;
import com.netease.engagement.activity.ActivityPageInfo;
import com.netease.engagement.activity.ActivityPrivateSession;
import com.netease.engagement.activity.ActivityProfileExplore;
import com.netease.engagement.activity.ActivityWeb;
import com.netease.engagement.adapter.UserGiftPagerAdapter;
import com.netease.engagement.adapter.VideoImagePagerAdapter;
import com.netease.engagement.adapter.YixinHelper;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.ConfigDataManager;
import com.netease.engagement.dataMgr.GiftInfoManager;
import com.netease.engagement.dataMgr.MemoryDataCenter;
import com.netease.engagement.itemview.RenderEnkeItem;
import com.netease.engagement.util.LevelChangeStatusBean;
import com.netease.engagement.util.LevelChangeStatusBean.LevelChangeType;
import com.netease.engagement.view.HeadView;
import com.netease.engagement.view.ImageFillLayout;
import com.netease.engagement.view.ImageViewPager;
import com.netease.engagement.view.ShareDialog;
import com.netease.engagement.view.ShareDialogInterface;
import com.netease.engagement.view.SnowView;
import com.netease.engagement.view.UserPageTabView;
import com.netease.engagement.view.UserScrollView;
import com.netease.engagement.view.UserScrollView.UserScrollListener;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.engagement.widget.GiftExploreView;
import com.netease.engagement.widget.GiftKeyboardView;
import com.netease.engagement.widget.LoadingImageView;
import com.netease.engagement.widget.ProgerssImageView;
import com.netease.engagement.widget.UserInfoUtil;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.media.MediaPlayerWrapper;
import com.netease.service.media.MediaPlayerWrapper.MediaListener;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmProtocolConstants.Block_Type;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.ChatItemUserInfo;
import com.netease.service.protocol.meta.GiftConfigResult;
import com.netease.service.protocol.meta.GiftInfo;
import com.netease.service.protocol.meta.LoopBack;
import com.netease.service.protocol.meta.PictureInfo;
import com.netease.service.protocol.meta.SendGiftResult;
import com.netease.service.protocol.meta.SpecialGift;
import com.netease.service.protocol.meta.UserInfo;
import com.netease.service.protocol.meta.UserInfoConfig;
import com.netease.service.protocol.meta.UserInfoDetail;
import com.netease.service.stat.EgmStat;
import com.netease.util.AnimationUtil;
import com.netease.util.MediaPlayerSystemTone;

public class FragmentUserPageGirl extends FragmentBase implements ShareDialogInterface {
    private static String EXTRA_IS_SHORTCUT = "extra_is_shortcut";
    private static String FROM_SESSION = "FROM_SESSION";
    
    private static final int PUBLIC_SLIDE_DURATION = 5000;
    
    private static final int AUDIO_DURATION = 1000;
 
    /**
     * msg
     */
    private static final int MSG_PUBLIC_SLIDE = 0x01;
	private static final int MSG_AUDIO_PLAY = 0x02;
	private static final int MSG_AUDIO_INIT = 0x03;
	private static final int MSG_CLICK_VIDEO_PLAY = 0x04;
    
    private static final int PUBLIC_PIC_SHADOW_ALPHA = 0x33;
    private static final int PUBLIC_PIC_SHADOW = 0x000000;

	public static FragmentUserPageGirl newInstance(String uid, boolean isFromeShortcut, boolean isFromSession) {
		FragmentUserPageGirl fragment = new FragmentUserPageGirl();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		args.putString(EgmConstants.BUNDLE_KEY.USER_ID, uid);
		args.putBoolean(EXTRA_IS_SHORTCUT, isFromeShortcut);
		args.putBoolean(FROM_SESSION, isFromSession);
		return fragment;
	}
	
	private CustomActionBar mCustomActionBar ;
	private View mActionBarView ;

	private long mUid;
	private long mMyUid ;
	//自己的性别
	private int mGender ;
	
	private UserInfo mUserInfo;
	private UserInfoDetail mUserInfoDetail;
	private UserInfoConfig mUserInfoConfig;
	
	// 雪花动效View
	private SnowView snowView;
	
	private LinearLayout mBottomLayout ;
	private UserScrollView mScrollView ;
	
	// 顶部图片pager
	private RelativeLayout mTopLayout ;
	private ImageViewPager mViewPager;
	private TextView mTxtNextPage ;
	private TextView mPageTip ;
	
	private LinearLayout mInfoLayout ;
	private TextView mTxtNick ;
	private TextView mLevelName ;
	
	private VideoImagePagerAdapter mAdapter;
	//头像和资料区
	
	private LinearLayout mContainer;
	
	//头像Layout
	private FrameLayout mHeadLayout;
	private RelativeLayout mProfileAudioLayout;
	private FrameLayout mProfileLayout ;
	private ImageView mImgCrown ;
	private ImageView mCrwonLayer ;
	private HeadView mProfileView;
	private FrameLayout mAudioLayout;
	private ProgressBar mAudioProgress;
	private TextView mAudioIntr;
	private TextView mAudioIcon;
	private TextView mTxtCert;
	//tab
	private UserPageTabView mTabs;
	private static final int TAB_MAX = 99999999 ;
	//加为易信好友
	private LinearLayout mAddYX;
	private View mYixinIcon ;
	private TextView mYixinText ;
	private TextView mNotifyYixinTip;
	// 私照
	private TextView mTagImage;
	private LinearLayout mScrollLayout;
	// 聊天技
	private TextView mTagTaklSkill;
	private LinearLayout mTalkSkillLayout;
	// 礼物
	private TextView mTagGift;
	private ViewPager mGiftPager ;
	private UserGiftPagerAdapter mGiftPagerAdapter ;
	private TextView mGiftTip ;
	private ImageFillLayout mAutoFillLayout ;
	//恩客
	private TextView mTagEnke ;
	private LinearLayout mEnkeLayout ;
	// 资料
	private TextView mTagData;
	private TextView mDataAge ;
	private TextView mDataHeight ;
	private TextView mDataWeight ;
	private TextView mDataFigure ;
	private TextView mDataFovorPart ;
	private TextView mDataCol ;
	private TextView mDataCity ;
	// 更多
	private TextView mTagMore;
	// 自我介绍
	private TextView mIntroduce;
	private View mIntrHori ;
	private TextView mIntrContent;
	// 喜欢的约会
	private TextView mTagDate;
	private View mDateHori ;
	private TextView mFavorDateView;
	// 兴趣爱好
	private TextView mTagHobby;
	private View mHobbyHori ;
	private TextView mHobbyView;
	// 技能
	private TextView mTagSkill;
	private View mSkillHori ;
	private TextView mSkillView;
	
	// 隐藏资料
	private LinearLayout mPageTail;

	// 底部，聊天，送礼物
	private RelativeLayout mChatLayout ;
	private RelativeLayout mGiftLayout ;
	private TextView mChatIcon ;
	
	private View mEmptyView;
	
	/** 双方已是好友。临时标记，因为添加好友成功后，mUserInfoDetail.IsYixinFriend没有马上刷新 */
	private boolean mIsYixinFriend = false;
	
	//图片比例系数
	private static final float FRACTION_IMAGE = 560f/1280 ; 
	//图片高度
	private int mIniHeight ;
	
	private int mScreenWidth;
	private int mScreenHeight;
	private int mDisplayHeight;
	
	private int mInfoMinMargin;
	
	//送礼物相关
	private LinearLayout mGiftMatchLayoutBackground;
	private LinearLayout mGiftMatchLayout ;
	private View mTopMarginLayout ;
	private GiftKeyboardView mGiftKeyboardView ;
	private GiftExploreView mGiftExploreView;
	private SendGiftResult mSendResult ;
	private SpecialGift[] mSpecialGifts ;
	private int mSendGiftTranId ;
	
	private YixinHelper mYixinHelper;
	
	private int mTid;
	
	/** 标记是否是从快捷方式进入的，如果是的话activity的进入和退出方式是从下到上和从上到下，与其它的不一样 */
    private boolean mIsFromeShortcut = false; 
    
    private boolean mIsFromSession = false;
    
    // 是否公开照大图模式
    private boolean mIsPublicMode;
    
	//是否已经加黑
	private boolean isBlock ;
	
	// Handler (公开照轮播使用)
	private Handler mHandler;
	
	// 下载audio 资源任务id
	private int mGetAudioTransId;
	
	private int mAudioPlayTime;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (this.getArguments() == null
				|| TextUtils.isEmpty(getArguments().getString(EgmConstants.BUNDLE_KEY.USER_ID))) {
			return;
		}
		
		mUid = Long.parseLong(getArguments().getString(EgmConstants.BUNDLE_KEY.USER_ID));
		mMyUid = ManagerAccount.getInstance().getCurrentId();
		mGender = ManagerAccount.getInstance().getCurrentGender() ;
		mIsFromeShortcut = getArguments().getBoolean(EXTRA_IS_SHORTCUT, false);
		mIsFromSession = getArguments().getBoolean(FROM_SESSION, false);
		
		mHandler = new InterHandler();
		
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
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
		RelativeLayout root = (RelativeLayout) inflater.inflate(
				R.layout.fragment_user_page_girl, container, false);
		init(root);
		
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
		
		return root;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		startPublicSlide();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		cancelPublicSlide();
		
		if (mAdapter != null) {
			mAdapter.stopPlay();
		}
	}
	
	//举报和加黑
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
								ActivityComplain.startActivity(FragmentUserPageGirl.this,mUserInfo.uid);
								break;
							case 1:
								/**
								 * 加黑
								 */
								if(isBlock){
									//取消加黑
									showCancelBlock();
								}else{
									//加黑
									showAddBlock();
								}
								break;
						}
						mShowMoreDialog.dismiss();
					}
				});
		mShowMoreDialog.show();
	}

	//加黑
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
	
	//取消加黑
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

	private void init(View root) {
		if (root == null) {
			return;
		}
		
		mUserInfoConfig = ConfigDataManager.getInstance().getUConfigFromData();
		if (mUserInfoConfig == null) {
			return;
		}
		
		root.findViewById(R.id.data_girl_layout).setVisibility(View.VISIBLE);
		
		// 获取显示高度等
		mScreenWidth = getResources().getDisplayMetrics().widthPixels;
		mScreenHeight = getResources().getDisplayMetrics().heightPixels;
		mIniHeight = (int) (mScreenHeight * FRACTION_IMAGE);
		
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		snowView = (SnowView) root.findViewById(R.id.snowView);
		snowView.SetView(dm.heightPixels, dm.widthPixels);

		mBottomLayout = (LinearLayout) root.findViewById(R.id.chat_layout_all);
		mScrollView = (UserScrollView)root.findViewById(R.id.top_layout);
		mTopLayout = (RelativeLayout)root.findViewById(R.id.top_layout_pager);
		mPageTip = (TextView)root.findViewById(R.id.page_tip);
		
		mTxtNextPage = (TextView)root.findViewById(R.id.next_page);
		mTxtNextPage.setOnClickListener(mOnClickListener);
		
		mInfoLayout = (LinearLayout)root.findViewById(R.id.info_layout);
		mInfoMinMargin = getLayoutTopMargin(mInfoLayout);
		
		mTxtNick = (TextView)root.findViewById(R.id.user_nick);
		
		mViewPager = (ImageViewPager) root.findViewById(R.id.viewpager);
		mViewPager.setOnPageChangeListener(mOnPageChangeListener);

		mContainer = (LinearLayout) root.findViewById(R.id.list_container);
		mHeadLayout = (FrameLayout) root.findViewById(R.id.head_layout);
		
		mCrwonLayer = (ImageView)root.findViewById(R.id.crown_layer);
		
		mProfileAudioLayout = (RelativeLayout)root.findViewById(R.id.profile_audio_container);
		mProfileLayout = (FrameLayout)root.findViewById(R.id.profile_layout);
		mImgCrown = (ImageView)root.findViewById(R.id.img_crown);
		mLevelName = (TextView)root.findViewById(R.id.level);
		
		mProfileView = (HeadView) root.findViewById(R.id.profile);
		mProfileView.setOnClickListener(mOnClickListener);
		mProfileView.setImageUrl(false,HeadView.PROFILE_SIZE_LARGE,null,
				EgmConstants.SexType.Female,R.drawable.bg_portrait_circle_200x200,getResources().getDimensionPixelSize(R.dimen.userpage_avatar_border_dp));
		
		mAudioLayout = (FrameLayout)root.findViewById(R.id.audio_layout);
		mAudioIntr = (TextView)root.findViewById(R.id.audio_intr);
		mAudioIcon = (TextView)root.findViewById(R.id.audio_icon);
		mAudioLayout.setOnClickListener(mOnClickListener);
		mAudioProgress = (ProgressBar) root.findViewById(R.id.audio_progress_bar);
		mTxtCert = (TextView)root.findViewById(R.id.cert);
		
		mTabs = (UserPageTabView) root.findViewById(R.id.userpage_tab);

		// 添加易信好友
		mAddYX = (LinearLayout) root.findViewById(R.id.add_yx);
		mAddYX.setOnClickListener(mOnClickListener);
		mYixinIcon = root.findViewById(R.id.yixin_icon);
		mYixinText = (TextView)root.findViewById(R.id.yixin_txt);
		mNotifyYixinTip = (TextView)root.findViewById(R.id.notify_yixin_tip);
		
		// 照片tag
		mTagImage = (TextView) root.findViewById(R.id.tag_image);
		mTagImage.setEnabled(false);
		mTagImage.setOnClickListener(mOnClickListener);
		mTagImage.setText(String.format(getString(R.string.private_image_num), 0));
		mScrollLayout = (LinearLayout) root.findViewById(R.id.scroll_layout);
		// 聊天技tag
		mTagTaklSkill = (TextView) root.findViewById(R.id.tag_talkskill);
		mTagTaklSkill.setEnabled(false);
		mTagTaklSkill.setText(getString(R.string.rec_chatskill_title));
		mTalkSkillLayout = (LinearLayout) root.findViewById(R.id.talkskill_layout);
		// 礼物tag
		mTagGift = (TextView) root.findViewById(R.id.tag_gift);
		mTagGift.setText(String.format(getString(R.string.receive_gift_num), 0));
		mGiftPager = (ViewPager)root.findViewById(R.id.gift_list);
		mGiftTip = (TextView)root.findViewById(R.id.gift_tip);
		mAutoFillLayout = (ImageFillLayout)root.findViewById(R.id.auto_fill_layout);
		//恩客
		mTagEnke = (TextView)root.findViewById(R.id.tag_enke);
		mTagEnke.setText(String.format(getString(R.string.enke_num), 0));
		mEnkeLayout = (LinearLayout)root.findViewById(R.id.enke_layout);
		// 资料tag
		mTagData = (TextView) root.findViewById(R.id.tag_data);
		mTagData.setText(getString(R.string.pri_user_data));
		mDataAge = (TextView)root.findViewById(R.id.data_age_girl);
		mDataHeight = (TextView)root.findViewById(R.id.data_height_girl);
		mDataCol = (TextView)root.findViewById(R.id.data_co_girl);
		mDataCity = (TextView)root.findViewById(R.id.data_city_girl);
		mDataWeight = (TextView)root.findViewById(R.id.data_weight_girl);
		mDataFigure = (TextView)root.findViewById(R.id.data_figure_girl);
		mDataFovorPart = (TextView)root.findViewById(R.id.data_favor_part_girl);
		// 自我介绍
		mIntroduce = (TextView) root.findViewById(R.id.introduce_tag);
		mIntrHori = root.findViewById(R.id.intr_hori);
		mIntrContent = (TextView) root.findViewById(R.id.introduce_content);
		// 喜欢的约会
		mTagDate = (TextView) root.findViewById(R.id.date_tag);
		mDateHori = root.findViewById(R.id.date_hori);
		mFavorDateView = (TextView) root.findViewById(R.id.favor_date_content);
		// 兴趣爱好
		mTagHobby = (TextView) root.findViewById(R.id.hobby_tag);
		mHobbyHori = root.findViewById(R.id.hobby_hori);
		mHobbyView = (TextView) root.findViewById(R.id.hobby_content);
		// 想学的技能
		mTagSkill = (TextView) root.findViewById(R.id.tag_skill);
		mTagSkill.setText(R.string.wanna_skill);
		mSkillHori = root.findViewById(R.id.skill_hori);
		mSkillView = (TextView) root.findViewById(R.id.skill_content);
		// 更多
		mTagMore = (TextView) root.findViewById(R.id.more_data);
		mTagMore.setText(getString(R.string.see_more_data));
		mTagMore.setOnClickListener(mOnClickListener);

		mPageTail = (LinearLayout) root.findViewById(R.id.user_pager_tail);
		
		//数据没有获取到之前将聊天和送礼物设置为不可点击
		mChatLayout = (RelativeLayout)root.findViewById(R.id.chat_layout);
		mChatLayout.setEnabled(false);
		mGiftLayout = (RelativeLayout)root.findViewById(R.id.gift_layout);
		mGiftLayout.setEnabled(false);
		
		LinearLayout gift_area = (LinearLayout)root.findViewById(R.id.gift_area);
		mChatLayout.setOnClickListener(mOnClickListener);
		mGiftLayout.setOnClickListener(mOnClickListener);
		mChatIcon = (TextView)root.findViewById(R.id.chat_icon);
		
		//送礼物相关
		mGiftMatchLayoutBackground = (LinearLayout)root.findViewById(R.id.gift_match_layout_background);
		mGiftMatchLayout = (LinearLayout)root.findViewById(R.id.gift_match_layout);
		mTopMarginLayout = root.findViewById(R.id.top_margin_layout);
		mTopMarginLayout.setOnClickListener(mOnClickListener);
		mGiftKeyboardView = (GiftKeyboardView)root.findViewById(R.id.gift_view);
		mGiftExploreView = (GiftExploreView)root.findViewById(R.id.gift_explore_view);
		
		//女性看女性
		if(mGender == EgmConstants.SexType.Female){
		    gift_area.setVisibility(View.GONE);
		    mBottomLayout.setVisibility(View.GONE);
		}
	}
	
	class UserGirlScrollListener implements UserScrollListener {
		
		private float mDistanceX;
		private float mDistanceY;
		
		private float mLastX;
		private float mLastY;
		
		private int mTouchSlop = 0;
		
		private float mMoveY;
		private int mDeltaY;
		private int mMarginTop;
		
		public UserGirlScrollListener(Context context) {
			mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		}
		
		@Override
		public boolean onInterceptTouchEvent(MotionEvent ev) {
			final float x = ev.getRawX();
			final float y = ev.getRawY();

			if (isAsynMoveing) {
				return true;
			}
			
			if (mAdapter.isCurrentScaling()) {
				return false;
			}
			
			switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mDistanceX = mDistanceY = 0f;
				mLastX = x;
				mLastY = y;
				
				mDeltaY = 0;
				mMarginTop = getLayoutTopMargin(mContainer);
				break;
			case MotionEvent.ACTION_MOVE:
				mDistanceX += x - mLastX;
				mDistanceY += y - mLastY;
				mLastX = x;
				mLastY = y;

				float absDx = Math.abs(mDistanceX);
				float absDy = Math.abs(mDistanceY);
				
				if (absDx < absDy && absDy > mTouchSlop) {
					return true;
				}
				break;
			}
			
			return false;
		}

		@Override
		public boolean onTouchEvent(MotionEvent ev) {
			final float x = ev.getRawX();
			final float y = ev.getRawY();
			
			if (isAsynMoveing) {
				return true;
			}
			
			if (mAdapter.isCurrentScaling()) {
				return true;
			}
			
			int maxMargin = mDisplayHeight;
			int minMargin = mIniHeight - (getHeadHeight() >> 1);// maxMargin - minMargin

			switch (ev.getAction()) {
			case MotionEvent.ACTION_MOVE:
				mMoveY += y - mLastY;
				
				mLastX = x;
				mLastY = y;
				
				if (mScrollView.getScrollY() == 0) {
					int delta = layoutInfoLayout((int) (mMarginTop + mMoveY), ev);
					mDeltaY += delta;
					
					return delta != 0 || mMarginTop > minMargin;
				}
				break;
				
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				int minDistance = getHeadHeight() / 3;
				int margin = getLayoutTopMargin(mContainer);
				
				if (mDeltaY != 0) {
					if (mMoveY > minDistance) {
						if (maxMargin - margin != 0) {
							mAdapter.enterFullActivity();
							
							new AsynMove(margin, maxMargin - margin).execute();
							
							statClickPublicPic();
						}
					}
					else if (mMoveY < - minDistance) {
						if (minMargin - margin != 0) {
							mAdapter.existFullActivity();
							
							new AsynMove(margin, minMargin - margin).execute();
						}
					}
					else {
						new AsynMove(margin, - mDeltaY).execute();
					}
				}
				
				mMoveY = 0;
				mDeltaY = 0;
				break;
			}
			
			return false;
		}

		@Override
		public void onScrollChanged(ScrollView view, int l, int t, int oldl,
				int oldt) {
			int margin = getLayoutTopMargin(mContainer);
			int minMargin = mIniHeight - (getHeadHeight() >> 1);// maxMargin - minMargin
			
			if (t > 0 && margin > minMargin) {
				mScrollView.scrollTo(0, 0);
			}
		}
		
	};
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mCustomActionBar = ((ActivityEngagementBase)getActivity()).getCustomActionBar();
        mActionBarView = mCustomActionBar.getCustomView();
        
        mCustomActionBar.setLeftBackgroundResource(R.drawable.titlebar_c_selector);
        mCustomActionBar.setLeftAction(R.drawable.bar_btn_back_b, R.string.back);
        mCustomActionBar.setLeftTitleColor(getResources().getColor(R.color.white));
        mCustomActionBar.setLeftClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
            	if(mActionBarView.getVisibility() != View.GONE) {
            		clickBack();
            	}
            }
        });
        mCustomActionBar.setMiddleTitleColor(getResources().getColor(R.color.white));
        mCustomActionBar.setMiddleTitle("");
        
        mCustomActionBar.setRightBackgroundResource(R.drawable.titlebar_c_selector);
        mCustomActionBar.setRightTitleColor(getResources().getColor(R.color.white));
        mCustomActionBar.setRightAction(-1, R.string.more);
        mCustomActionBar.setRightClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
            	if(mActionBarView.getVisibility() != View.GONE)
            		showMoreDialog();
            }
        });
        if(mUid == mMyUid || mGender == EgmConstants.SexType.Female){
            mCustomActionBar.hideRightTitle();
        }
		
		try {
			layoutInfoLayout(-1);
			
			startLayoutAnimation();
		} catch (Exception e) {
		}
	}
	
	private int oldPosition = -1;
	
	/**
	 * 顶部ViewPager滑动
	 */
	private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener(){
		
		
		@Override
		public void onPageScrollStateChanged(int arg0) {
			switch (arg0) {
			case ViewPager.SCROLL_STATE_DRAGGING:
				cancelPublicSlide();
				break;
			case ViewPager.SCROLL_STATE_IDLE:
			case ViewPager.SCROLL_STATE_SETTLING:
				startPublicSlide();
				break;
			}
		}
		
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
//			if (arg2 == 0 && mAdapter != null) {
//				mAdapter.setScaleZoom(1);
//			}
		}
		
		@Override
		public void onPageSelected(int arg0) {
			int size = getPublicPicSize();
			arg0 = arg0 % size;
			
			if (oldPosition != arg0) {
				if (mAdapter != null) {
					mAdapter.setScaleZoom(1);
				}
			}
			oldPosition = arg0;
			
			updatePageInfo(arg0);
		}
	};
	
	
	private void updatePageInfo(int arg0) {
		int size = getPublicPicSize();
		String page = "" + (arg0 + 1) + "/" + size;
		mTxtNextPage.setText(page);
		mPageTip.setText(page);
		
		statClickPublicPic();
	}
	
	private void statClickPublicPic() {
		int pos = oldPosition;
		if (mAdapter.hasVideo()) {
			pos--;
		}
		
		if (pos >= 0 && mUserInfoDetail.publicPicList != null 
				&& pos < mUserInfoDetail.publicPicList.length) {
			
			if (! isPublicSlide()) {
				PictureInfo info = mUserInfoDetail.publicPicList[pos];
				
				EgmStat.log(EgmStat.LOG_CLICK_PHOTO_DETAIL, 
						EgmStat.SCENE_USER_DETAIL, mUid, 
						info.id, EgmStat.TYPE_PUB_FREE);
			}
		}
	}
	
	/**
	 * 是否处于自动轮播状态，不统计公开照片点击
	 * @return
	 */
	private boolean isPublicSlide() {
		boolean ret = false;
		if (mHandler != null) {
			ret = mHandler.hasMessages(MSG_PUBLIC_SLIDE);
		}
		
		return ret;
	}

	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
				case R.id.chat_layout:
					/**
					 * 聊天
					 */
					if(isBlock){
						ToastUtil.showToast(getActivity(),R.string.block_cannot_chat_tip_girl);
						return ;
					}
					
//					if (mUserInfoDetail.IsYixinFriend || mIsYixinFriend) {    // 易信好友，用易信聊天
//					    if(!mYixinHelper.startYixin()){
//					        ToastUtil.showToast(getActivity(), R.string.yixin_no_install);
//					    }
//					}
//					else{   // 不是易信好友，在内部聊天
						if (mIsFromSession) {
	                		Context context = view.getContext();
	    			        if(context instanceof Activity){
	    			        	Activity activity = (Activity) context;
	    			        	activity.finish();
	    			        }
	                	} else {
	                		if(mUserInfo == null){
			                    ToastUtil.showToast(getActivity(),"等待获取用户数据");
			                    return ;
			                }
			                ChatItemUserInfo info = new ChatItemUserInfo();
			                info.uid = mUserInfo.uid ;
			                info.crownId = mUserInfo.crownId ;
			                info.nick = mUserInfo.nick ;
			                info.portraitUrl192 = mUserInfo.portraitUrl192 ;
			                info.isNew = mUserInfo.isNew;
			                ActivityPrivateSession.startActivityFromUserinfo(getActivity(), info);
	                	}
//					}
					
					break;
				case R.id.gift_layout:
					/**
					 * 送礼物
					 */
					if(mUserInfo == null){
						ToastUtil.showToast(getActivity(),"等待获取用户数据");
						return ;
					}
					if(isBlock){
						ToastUtil.showToast(getActivity(),R.string.block_cannot_send_gift);
						return ;
					}
					mGiftMatchLayout.setVisibility(View.VISIBLE);
					Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_from_bottom);
					mGiftMatchLayout.startAnimation(anim);
					mGiftKeyboardView.setVisibility(View.VISIBLE);
					
					mGiftMatchLayoutBackground.setVisibility(View.VISIBLE);
					Animation anim2 = AnimationUtils.loadAnimation(getActivity(), R.anim.alpha_0_to_1);
					mGiftMatchLayoutBackground.startAnimation(anim2);
					
					break;
				case R.id.top_margin_layout:
					/**
					 * 隐藏礼物layout
					 */
					Animation animHide = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_to_bottom);
					mGiftMatchLayout.startAnimation(animHide);
					mGiftMatchLayout.setVisibility(View.GONE);
					mGiftKeyboardView.setVisibility(View.GONE);
					mGiftExploreView.setVisibility(View.GONE);

					Animation animHide2 = AnimationUtils.loadAnimation(getActivity(), R.anim.alpha_1_to_0);
					mGiftMatchLayoutBackground.startAnimation(animHide2);
					mGiftMatchLayoutBackground.setVisibility(View.GONE);
					
					break;
				case R.id.more_data:
					/**
					 * 详细资料
					 */
					view.setVisibility(View.GONE);
					mPageTail.setVisibility(View.VISIBLE);
					
					EgmStat.log(EgmStat.LOG_CLICK_INTRODUCTION_DETAIL, 
							EgmStat.SCENE_USER_DETAIL, mUid);
					break;
				case R.id.add_yx:
					/**
					 * 加易信好友
					 */
					if(mUid == ManagerAccount.getInstance().getCurrentId()
							&& mUserInfo != null){
						ActivityPageInfo.startActivityForResult(
								FragmentUserPageGirl.this,
								String.valueOf(ActivityPageInfo.DETAIL_INFO), 
								mUserInfo);
					}else{
						//添加易信好友
					    mYixinHelper.addFriend(mUid);
					}
					break;
				case R.id.audio_layout:
					/**
					 *播放语音自我介绍
					 */
					switch(MediaPlayerWrapper.getInstance().getPlayStatus()){
						case IDLE:
							MediaPlayerWrapper.getInstance().registerMediaListener(mMediaPlayListener);
							MediaPlayerWrapper.getInstance().play(mUserInfo.voiceIntroduce);
							break;
						case PLAYING:
							MediaPlayerWrapper.getInstance().stop();
							break;
					}
					break;
				case R.id.more_enke:
					/**
					 * 点击查看更多恩客
					 */
					enkeLayout.setVisibility(View.GONE);
					for(int i = 4 ;i < mEnkeLayout.getChildCount();i++){
						mEnkeLayout.getChildAt(i).setVisibility(View.VISIBLE);
					}
					LinearLayout l = new LinearLayout(getActivity());
					l.setBackgroundColor(getResources().getColor(R.color.white));
					mEnkeLayout.addView(l, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, EgmUtil.dip2px(getActivity(), 24)));
					break;
				case R.id.title_right:
					/**
					 * 加黑操作
					 */
					break;
				case R.id.tag_image:
					/**
					 * 进入私照列表
					 */
					ActivityImageList.startActivity(getActivity(), mUserInfo.uid, null, 0);
					break;
				case R.id.next_page:
					/**
					 * 翻页
					 */
					mViewPager.setCurrentItem(mViewPager.getCurrentItem()+1);
					break;
				case R.id.profile:
					EgmStat.log(EgmStat.LOG_CLICK_PHOTO_DETAIL, 
							EgmStat.SCENE_USER_DETAIL, mUid, 0, EgmStat.TYPE_HEAD);
					
					/**
					 * 头像
					 */
					mScrollView.fullScroll(ScrollView.FOCUS_UP);
					mScrollView.postDelayed(new Runnable() {
						@Override
						public void run() {
							if(mUserInfo != null) {
								ActivityProfileExplore.startActivity(
										getActivity(), mUserInfo.portraitUrl,
										EgmConstants.SexType.Female);
							}
						}
					}, 200);
					break;
				}
		}
	};
	
	/**
	 * 点击礼物列表
	 */
	private ImageFillLayout.IOnGiftListItemClickListener mOnGiftListItemClickListener = new ImageFillLayout.IOnGiftListItemClickListener() {
		@Override
		public void onClick(int giftId) {
			showGiftExploreView(giftId);
		}
	};
	
	public void showGiftExploreView(int giftId) {
		if(isBlock) {
			ToastUtil.showToast(getActivity(),R.string.block_cannot_send_gift);
			return ;
		}
		
		if(mUid == mMyUid  || mGender == EgmConstants.SexType.Female) {
			return;
		}
		
		if(giftId <= 0) {
			return;
		}
		
		mGiftMatchLayout.setVisibility(View.VISIBLE);
		Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_from_bottom);
		mGiftMatchLayout.startAnimation(anim);
		mGiftExploreView.setVisibility(View.VISIBLE);
		
		mGiftMatchLayoutBackground.setVisibility(View.VISIBLE);
		Animation anim2 = AnimationUtils.loadAnimation(getActivity(), R.anim.alpha_0_to_1);
		mGiftMatchLayoutBackground.startAnimation(anim2);
		
		int crownId = (Integer)(MemoryDataCenter.getInstance().get(
				MemoryDataCenter.CURRENT_COMPARE_CROWNID));
		
		mGiftExploreView.renderView(giftId, crownId);
	}
	
	/**
	 * 赠送礼物
	 */
	private int mGiftId;
	public void doSendGift(String giftId){
		mSendGiftTranId = EgmService.getInstance().doSendGift(String.valueOf(mUserInfo.uid), 
				giftId,
				null,
				EgmConstants.Send_Gift_From.TYPE_USERPAGE_SEND_GIFT);
		mGiftMatchLayout.setVisibility(View.GONE);
		mGiftMatchLayoutBackground.setVisibility(View.GONE);
		mGiftKeyboardView.setVisibility(View.GONE);
		mGiftExploreView.setVisibility(View.GONE);
		
		showWatting("赠送中...");
		mGiftId = Integer.parseInt(giftId) ;
		
		EgmStat.log(EgmStat.LOG_GIVE_GIFT_DETAIL, EgmStat.SCENE_USER_DETAIL, 
				mUserInfo.uid, mGiftId);
	}
	
	private MediaListener mMediaPlayListener = new MediaListener(){
		@Override
		public void onMediaPrePare() {
			setAudioIcon(true);
		}
		@Override
		public void onMediaPlay() {
			setAudioIcon(true);
			
			mAudioPlayTime = 0;
			
			mHandler.sendEmptyMessageDelayed(MSG_AUDIO_INIT, 200);
			mHandler.sendEmptyMessageDelayed(MSG_AUDIO_PLAY, AUDIO_DURATION - 100); // 动画偏移部分
		}
		@Override
		public void onMediaPause() {
			setAudioIcon(false);
		}
		@Override
		public void onMediaRelease() {
			MediaPlayerWrapper.getInstance().removeMediaListener(mMediaPlayListener);
			setAudioIcon(false);
		}
		@Override
		public void onMediaCompletion() {
			MediaPlayerWrapper.getInstance().stop();
			MediaPlayerWrapper.getInstance().removeMediaListener(mMediaPlayListener);
			setAudioIcon(false);
		}
	};
	
	private void setAudioIcon(boolean playing) {
		if (! playing) {
			mAudioPlayTime = 0;
			mAudioIntr.setVisibility(View.VISIBLE);
			mAudioIntr.setText(String.format(getString(R.string.second_unit),mUserInfo.duration));
			mHandler.removeMessages(MSG_AUDIO_INIT);
			mHandler.removeMessages(MSG_AUDIO_PLAY);
		}
		
		mAudioIcon.setBackgroundResource(playing ? R.drawable.icon_pginfo_voice_pause : R.drawable.icon_pginfo_voice_play);
	}
	
    private static final int TYPE_CONTENT = 0;
    private static final int TYPE_NET_ERROR = 1;
    private int mTipType;
    
    private void showTip(int type) {
    	mTipType = type;
    	switch (type) {
		case TYPE_CONTENT:
			mScrollView.setVisibility(View.VISIBLE);
			if(mGender == EgmConstants.SexType.Female) {
				mBottomLayout.setVisibility(View.GONE);
			} else {
				mBottomLayout.setVisibility(View.VISIBLE);
			}
			if(mUid != mMyUid && mGender != EgmConstants.SexType.Female) {
				mCustomActionBar.setRightVisibility(View.VISIBLE);
			}
			mEmptyView.setVisibility(View.GONE);
			break;

		case TYPE_NET_ERROR:
			mScrollView.setVisibility(View.GONE);
			mBottomLayout.setVisibility(View.GONE);
			mCustomActionBar.setRightVisibility(View.GONE);
			mEmptyView.setVisibility(View.VISIBLE);
			((TextView)mEmptyView.findViewById(R.id.empty_text)).setText(R.string.common_reload_tip);
			break;
		}
    }
    

	private void refreshViews() {
		if(mUserInfo == null || getActivity() == null){
			return ;
		}
		isBlock = mUserInfoDetail.isBlack ;
		
		if(mUid != mMyUid && mGender != EgmConstants.SexType.Female){
			mTagImage.setEnabled(true);
		}
		mChatLayout.setEnabled(true);
		mGiftLayout.setEnabled(true);
		
		MemoryDataCenter.getInstance().put(MemoryDataCenter.CURRENT_COMPARE_CROWNID,mUserInfo.crownId);
		
		fillPublicPager();
		fillHeader();
		fillImageData();
		fillTalkSkill();
		fillData();
		
		boolean enableClick = mUid != mMyUid && mGender != EgmConstants.SexType.Female;
		mAutoFillLayout.setItemEnable(enableClick);
		
		// 礼物列表
		if (mUserInfoDetail.giftList == null || mUserInfoDetail.giftList.length == 0) {
			mTagGift.setText(String.format(getString(R.string.receive_gift_num),0));
			mGiftTip.setVisibility(View.VISIBLE);
			mGiftPager.setVisibility(View.GONE);
			mAutoFillLayout.setVisibility(View.GONE);
		} else {
			mTagGift.setText(String.format(getString(R.string.receive_gift_num),mUserInfoDetail.userInfo.giftCount));
			if(mUserInfoDetail.giftList.length >= 5){
				mGiftPager.setVisibility(View.VISIBLE);
				mGiftTip.setVisibility(View.GONE);
				mAutoFillLayout.setVisibility(View.GONE);
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,mScreenWidth/2);
				mGiftPager.setLayoutParams(lp);
				mGiftPagerAdapter = new UserGiftPagerAdapter(getActivity(),
						Arrays.asList(mUserInfoDetail.giftList), EgmConstants.SexType.Female);
				mGiftPagerAdapter.setItemEnable(enableClick);
				mGiftPager.setAdapter(mGiftPagerAdapter);
			}else{
				mAutoFillLayout.setVisibility(View.VISIBLE);
				mAutoFillLayout.setOnGiftListItemClickListener(mOnGiftListItemClickListener);
				mGiftTip.setVisibility(View.GONE);
				mGiftPager.setVisibility(View.GONE);
				mAutoFillLayout.fillLayout(Arrays.asList(mUserInfoDetail.giftList),0);
			}
		}
		
		//恩客
		if(mUserInfoDetail.loveList == null || mUserInfoDetail.loveList.length == 0){
			mTagEnke.setVisibility(View.GONE);
			mEnkeLayout.setVisibility(View.GONE);
		}else{
			mTagEnke.setVisibility(View.VISIBLE);
			mEnkeLayout.setVisibility(View.VISIBLE);
			mTagEnke.setText(String.format(getString(R.string.enke_num), mUserInfoDetail.userInfo.adorerCount));
			mEnkeLayout.removeAllViews();
			fillEnkeList();
		}
		
		mChatLayout.setClickable(true);
		mGiftLayout.setClickable(true);
		
		AlphaAnimation aam = new AlphaAnimation(0, 1);
		aam.setDuration(200);
		
		mInfoLayout.startAnimation(aam);
		mTabs.startAnimation(aam);
	}
	
	/**
	 * 填充皇冠布局
	 */
	private void fillCrownImage(long crownId){
		if(crownId == 0){
			return ;
		}
		Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.alpha_0_to_1);
		mImgCrown.setAnimation(animation);
		mCrwonLayer.setAnimation(animation);
		mImgCrown.setVisibility(View.VISIBLE);
		mCrwonLayer.setVisibility(View.VISIBLE);
		
		GiftInfoManager.setCrownInfo(crownId, true, mImgCrown);
		
		int profileWidth = mProfileView.getWidth();
		int layerWidth = mCrwonLayer.getWidth();
		
		int width = View.MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED);
        int height = View.MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED);
        mAudioLayout.measure(width, height);
		
		int left = mProfileAudioLayout.getLeft();
		int leftMargin = left - (layerWidth - profileWidth)/2 ;
		LinearLayout.LayoutParams lp = (android.widget.LinearLayout.LayoutParams) mCrwonLayer.getLayoutParams();
		lp.leftMargin = leftMargin ;
		mCrwonLayer.setLayoutParams(lp);
	}
	
	/**
	 * 填充顶部公开照列表
	 */
	private void fillPublicPager(){
		//顶部公开照片
		PictureInfo[] pics = mUserInfoDetail.publicPicList;
		UserInfo user = mUserInfoDetail.userInfo;
		
//		user.videoIntroduce = "http://101.71.72.12/vweishi.tc.qq.com/1008_47a90d8491f44834a81dd05ebdc3cef8.f30.mp4?vkey=FE29B171CC4216CE1FB2861C23CA9D7845D1DD09C1B66D102AA93013C452E6173B798F3B54DBEF67&sha=db8edae61923d2e1cf18d0ca8a1406e806d7b3cb&platform=1&br=128&fmt=flv&sp=0";
//		user.videoCover = "http://yimg.nos.netease.com/98/3/9a67ffd2ad80bfd7a8064a68797d6578/200398/p1417704383427";
		
		if((pics == null || pics.length == 0)
				&& TextUtils.isEmpty(user.videoIntroduce)) {
			return ;
		}
		
		ViewCompat.setBackground(mViewPager, null);
		mViewPager.setBackgroundColor(getResources().getColor(R.color.black));
		
		ArrayList<String> pubImageList = new ArrayList<String>();
		
		if (! TextUtils.isEmpty(user.videoIntroduce)) {
			pubImageList.add(user.videoCover);
		}
		
		mTxtNextPage.setVisibility(View.VISIBLE);
		
		if (pics != null) {
			int length = Math.min(10, pics.length);
			
			for (int i = 0; i < length; i++) {
				pubImageList.add(pics[i].picUrl);
			}
		}
		
		mAdapter = new VideoImagePagerAdapter(getActivity(), pubImageList);
		mAdapter.setScaleTop(true);
		mAdapter.setZoomEnable(false);
		mAdapter.setVideoUrl(user.videoIntroduce);
		
		mAdapter.setOnViewTapListener(mOnViewTapListener);
		mAdapter.setOnGestureListener(mImageGestureListener);
		mAdapter.setOnClickListener(mVideoClickListener);
		
		if (pubImageList.size() > 1) {
			mAdapter.setInfinite(true);
		}
		
		mViewPager.setAdapter(mAdapter);
		mViewPager.setFrontColor((PUBLIC_PIC_SHADOW_ALPHA << 24) | PUBLIC_PIC_SHADOW);
		
		String page = "1/" + getPublicPicSize();
		mTxtNextPage.setText(page);
		mPageTip.setText(page);
		
		mScrollView.setUserScrollListener(
				new UserGirlScrollListener(mScrollView.getContext()));
		mScrollView.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
		
		oldPosition = 0;
		statClickPublicPic();
		
		if (pubImageList.size() > 1) {
			mViewPager.setCurrentItem(Short.MAX_VALUE - (Short.MAX_VALUE % pubImageList.size()));
			startPublicSlide();
		}
	}
	
	private OnClickListener mVideoClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.video_pub_root:
			case R.id.video_view:
			case R.id.video_cover_img:
				clickPublicPicture();
				break;
				
			case R.id.video_cover_play:
				clickPublicPicture();
				
				mHandler.removeMessages(MSG_PUBLIC_SLIDE);
				mHandler.sendEmptyMessageDelayed(MSG_CLICK_VIDEO_PLAY, 200);
				break;
			case R.id.video_progress:
				if (mAdapter.isPlaying()) {
					mAdapter.stopPlay();
				}
				else {
					mAdapter.startPlay();
				}
				break;
			}
		}
	};
	
	private OnViewTapListener mOnViewTapListener = new OnViewTapListener() {
		
		@Override
		public void onViewTap(View view, float x, float y) {
			int margin = getLayoutTopMargin(mContainer);
			int distance = mDisplayHeight - mIniHeight
					+ (mHeadLayout.getHeight() >> 1);
			
			if (margin >= mDisplayHeight) {
				mAdapter.existFullActivity();
				
				new AsynMove(margin, -distance).execute();
			}

		}
	};
	
	private SimpleOnGestureListener mImageGestureListener 
			= new SimpleOnGestureListener() {
		
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			clickPublicPicture();
			
			return false;
		}
		
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			int margin = getLayoutTopMargin(mContainer);
			
			if (margin >= mDisplayHeight) {
				if (! mAdapter.isCurrentScaling()) {
					mAdapter.setScaleZoom(2);
				}
			}
			
			return true;
		}
		
	};
	
	private void clickPublicPicture() {
		int margin = getLayoutTopMargin(mContainer);
		int distance = mDisplayHeight - mIniHeight
				+ (getHeadHeight() >> 1);
		
		if (margin < mDisplayHeight) {
			mAdapter.enterFullActivity();
			
			mScrollView.scrollTo(0, 0);
			
			new AsynMove(margin, distance).execute();
			
			statClickPublicPic();
		}
		else {
			mAdapter.existFullActivity();
			
			new AsynMove(margin, -distance).execute();
		}
	}
	
	/**
	 * 填充顶部信息
	 */
	private void fillHeader(){
		// 头像
		if(!TextUtils.isEmpty(mUserInfo.portraitUrl192)) {
			mProfileView.setImageUrl(mUserInfo.isVip,HeadView.PROFILE_SIZE_LARGE, mUserInfo.portraitUrl192,
					EgmConstants.SexType.Female,R.drawable.bg_portrait_circle_200x200,getResources().getDimensionPixelSize(R.dimen.userpage_avatar_border_dp));
		}
		//语音介绍
		if(!TextUtils.isEmpty(mUserInfo.voiceIntroduce) && mUserInfo.duration > 0){
			mAudioLayout.setVisibility(View.INVISIBLE);
			Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_center_to_left);
			animation.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					fillCrownImage(mUserInfo.crownId);
					mAudioLayout.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_left_in));
					mAudioLayout.setVisibility(View.VISIBLE);
					mAudioIntr.setText(String.format(getString(R.string.second_unit),mUserInfo.duration));
					mAudioLayout.postDelayed(new Runnable() {
						@Override
						public void run() {
							if (!TextUtils.isEmpty(mUserInfo.voiceIntroduce)) {
								mGetAudioTransId = EgmService.getInstance().doDownloadRes(mUserInfo.voiceIntroduce);
							}
						}
					}, 200);
				}
			});
			mProfileLayout.setAnimation(animation);
			mProfileLayout.setVisibility(View.VISIBLE);
		} else {
			mProfileLayout.setVisibility(View.VISIBLE);
			mProfileLayout.post(new Runnable() {
				@Override
				public void run() {
					fillCrownImage(mUserInfo.crownId);
				}
			});
		}
		mTxtCert.setVisibility(mUserInfo.hasVideoAuth ? View.VISIBLE : View.INVISIBLE);
		
		// 填充tabs
		mTabs.setVisibility(View.VISIBLE);
		mTabs.clear();
		mTabs.createTabs(Math.min(TAB_MAX, mUserInfo.usercp),
				Math.min(TAB_MAX, mUserInfo.praise),
				Math.min(TAB_MAX, mUserInfo.visitTimes));
		
		// 加易信好友
//		if (mGender == EgmConstants.SexType.Male 
//				&& mUserInfoDetail.IsYixinFriend && mUid != mMyUid) {
//			mAddYX.setVisibility(View.GONE);
//			mNotifyYixinTip.setVisibility(View.GONE);
//		}
		
		switch(mGender){
			case EgmConstants.SexType.Male:
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
			case EgmConstants.SexType.Female:
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
	}
	
	/**
	 * 获取实际公开照个数，剔除循环使用的两张图
	 */
	private int getPublicPicSize() {
		int size = 0;
		
		if (mAdapter != null) {
			size = mAdapter.getRealCount();
		}
		
		return size;
	}
	
	/**
	 * 单独提出，方便用户编辑详细资料后进行刷新
	 * @param userInfo
	 */
	private void fillData(){
		//昵称和等级
		mTxtNick.setText(mUserInfo.nick);
		if(!TextUtils.isEmpty(mUserInfo.levelName)){
			mLevelName.setVisibility(View.VISIBLE);
			String levelStr = String.format(getResources().getString(R.string.level_num),mUserInfo.level);
			mLevelName.setText(levelStr+" "+ mUserInfo.levelName);
		}
		
		// 如果“自我介绍”、“喜欢的约会”、“兴趣爱好”、“我擅长的技能”、“社交网络”全部都为空，则不显示“查看完整资料”操作
		if (TextUtils.isEmpty(mUserInfo.introduce)
				&& (mUserInfo.favorDate == null || mUserInfo.favorDate.length == 0)
				&& (mUserInfo.hobby == null || mUserInfo.hobby.length == 0)
				&& (mUserInfo.skill == null || mUserInfo.skill.length == 0)
				&& TextUtils.isEmpty(mUserInfo.socialUrl)) {
			mTagMore.setVisibility(View.GONE);
		}else{
			mTagMore.setVisibility(View.VISIBLE);
			
			/**
			 * 详细资料
			 */
			mTagMore.setVisibility(View.GONE);
			mPageTail.setVisibility(View.VISIBLE);
		}
		
		// 资料
		if(mUserInfo.age != 0){
			mDataAge.setVisibility(View.VISIBLE);
			mDataAge.setText(String.format(getString(R.string.age_unit), mUserInfo.age));
		}else{
			mDataAge.setVisibility(View.GONE);
		}
		
		//体重
		if(mUserInfo.weight != 0){
			mDataWeight.setVisibility(View.VISIBLE);
			mDataWeight.setText(String.format(getString(R.string.weight_unit_big), mUserInfo.weight));
		}else{
			mDataWeight.setVisibility(View.GONE);
		}
		
		//身高
		if(mUserInfo.height != 0){
			mDataHeight.setVisibility(View.VISIBLE);
			mDataHeight.setText(String.format(getString(R.string.height_unit_big), mUserInfo.height));
		}else{
			mDataHeight.setVisibility(View.GONE);
		}
		
		//身材
		if(!TextUtils.isEmpty(UserInfoUtil.getFigureSimple(mUserInfo,mUserInfoConfig))){
			mDataFigure.setVisibility(View.VISIBLE);
			mDataFigure.setText(UserInfoUtil.getFigureSimple(mUserInfo, mUserInfoConfig));
		}else{
			mDataFigure.setVisibility(View.GONE);
		}
		
		//星座
		if(mUserInfo.constellation != 0){
			mDataCol.setVisibility(View.VISIBLE);
			mDataCol.setText(UserInfoUtil.getConstellation(mUserInfo,mUserInfoConfig));
		}else{
			mDataCol.setVisibility(View.GONE);
		}
		
		//最满意的部位
		if(mUserInfo.satisfiedPart !=0){
			mDataFovorPart.setVisibility(View.VISIBLE);
			String str = getString(R.string.favar_part_a) ;
			mDataFovorPart.setText(
					getSSb(str + UserInfoUtil.getFavorPart(mUserInfo.satisfiedPart,mUserInfoConfig),
					0,
					str.length()));
		}else{
			mDataFovorPart.setVisibility(View.GONE);
		}
		
		//地区
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
		
		// 自我介绍
		if (TextUtils.isEmpty(mUserInfo.introduce)) {
			mIntroduce.setVisibility(View.GONE);
			mIntrHori.setVisibility(View.GONE);
			mIntrContent.setVisibility(View.GONE);
		} else {
			mIntroduce.setVisibility(View.VISIBLE);
			mIntrHori.setVisibility(View.VISIBLE);
			mIntrContent.setVisibility(View.VISIBLE);
			mIntrContent.setText(mUserInfo.introduce);
		}
		
		// 喜欢的约会
		if (mUserInfo.favorDate == null || mUserInfo.favorDate.length == 0
				|| TextUtils.isEmpty(UserInfoUtil.getDateText(
						mUserInfo, mUserInfoConfig))) {
			mTagDate.setVisibility(View.GONE);
			mDateHori.setVisibility(View.GONE);
			mFavorDateView.setVisibility(View.GONE);
		} else {
			mTagDate.setVisibility(View.VISIBLE);
			mDateHori.setVisibility(View.VISIBLE);
			mFavorDateView.setVisibility(View.VISIBLE);
			mFavorDateView.setText(UserInfoUtil.getDateText(
					mUserInfo, mUserInfoConfig));
		}
		
		// 兴趣爱好
		if (mUserInfo.hobby == null || mUserInfo.hobby.length == 0
				|| TextUtils.isEmpty(UserInfoUtil.getFemaleHobbyText(
					mUserInfo, mUserInfoConfig))) {
			mTagHobby.setVisibility(View.GONE);
			mHobbyHori.setVisibility(View.GONE);
			mHobbyView.setVisibility(View.GONE);
		} else {
			mTagHobby.setVisibility(View.VISIBLE);
			mHobbyHori.setVisibility(View.VISIBLE);
			mHobbyView.setVisibility(View.VISIBLE);
			mHobbyView.setText(UserInfoUtil.getFemaleHobbyText(
					mUserInfo, mUserInfoConfig));
		}
		
		// 擅长的技能
		if (mUserInfo.skill == null || mUserInfo.skill.length == 0
				|| TextUtils.isEmpty(UserInfoUtil.getSkillText(
					mUserInfo, mUserInfoConfig))) {
			mTagSkill.setVisibility(View.GONE);
			mSkillHori.setVisibility(View.GONE);
			mSkillView.setVisibility(View.GONE);
		} else {
			mTagSkill.setVisibility(View.VISIBLE);
			mSkillHori.setVisibility(View.VISIBLE);
			mSkillView.setVisibility(View.VISIBLE);
			mSkillView.setText(UserInfoUtil.getSkillText(
					mUserInfo, mUserInfoConfig));
		}
		
		// 聊天按钮换成易信logo
//		if(mUserInfoDetail.IsYixinFriend){
//		    ViewCompat.setBackground(mChatIcon, 
//		    		getResources().getDrawable(R.drawable.button_yixin_selector));
//		}
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
	 * 填充私照列表
	 */
	private void fillImageData() {
		
		if (mUserInfoDetail.privatePicList == null || mUserInfoDetail.privatePicList.length == 0) {
			mTagImage.setVisibility(View.GONE);
			mScrollLayout.setVisibility(View.GONE);
			return ;
		}
		
		mScrollLayout.removeAllViews();
		
		mTagImage.setText(String.format(getString(R.string.private_image_num),mUserInfo.privatePhotoCount));
		
		int paddingLeft = EgmUtil.dip2px(getActivity(),16);
		int paddingRight = EgmUtil.dip2px(getActivity(), 56);
		int imageListMargin = EgmUtil.dip2px(getActivity(), 4);
		
		PictureInfo[] pictures = mUserInfoDetail.privatePicList;
		int imageWidth = (mScreenWidth - paddingLeft - 3*imageListMargin - paddingRight)/3 ;
		
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(imageWidth, imageWidth);
		lp.rightMargin = imageListMargin ;
		
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
		
		int length = pictures.length > 10 ? 10 : pictures.length ;
		for (int i = 0; i < length; i++) {
			RelativeLayout item = new RelativeLayout(getActivity());
			
//added by lishang 改用带有进度条的ProgerssImageView代替LoadingImageView
			ProgerssImageView progerssImageView=new ProgerssImageView(getActivity());
			progerssImageView.setBackgroundColor(getResources().getColor(R.color.rec_no_picture_bg));
			LoadingImageView imageView=progerssImageView.mImageView;
			

			imageView.setDefaultResId(R.drawable.icon_photo_loaded_fail);
			imageView.setScaleTop(true);
			
			if(pictures[i].stateChanged){
				imageView.setServerClipSize(imageWidth, imageWidth);
				imageView.setLoadingImage(pictures[i].picUrl);
			}else{
				imageView.setLoadingImage(pictures[i].smallPicUrl);
			}
			imageView.setScaleType(ScaleType.CENTER_CROP);
			
			item.addView(progerssImageView,new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
					RelativeLayout.LayoutParams.MATCH_PARENT));
		
			
			if(!pictures[i].isViewed){
				TextView textView = new TextView(getActivity());
				ViewCompat.setBackground(textView,this.getResources().getDrawable(R.drawable.icon_photo_lock));
				item.addView(textView,rlp);
			}
			
//			final PictureInfo info = pictures[i];
			item.setTag(i);
			//女性用户，不可点击
			if(mGender != EgmConstants.SexType.Female){
				item.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						int i = (Integer)v.getTag();
						ActivityImageList.startActivity(getActivity(), mUserInfo.uid, null, i+1);
					}
				});
			}
			mScrollLayout.addView(item,lp);
		}
		
		//私照大于十张的时候在后面添加查看更多接口
		if(mUserInfo.privatePhotoCount > 10){
			RelativeLayout item = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.item_view_see_more_private, null);
			LoadingImageView imageView = (LoadingImageView) item.findViewById(R.id.image);
			imageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
					RelativeLayout.LayoutParams.MATCH_PARENT));
			imageView.setScaleType(ScaleType.CENTER_CROP);
			if(mUid != mMyUid && mGender != EgmConstants.SexType.Female){	
				item.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						ActivityImageList.startActivity(getActivity(), mUserInfo.uid, null, 11);
					}
				});
			}
			mScrollLayout.addView(item,lp);
		}
	}
	
	/**
	 * 填充聊天技
	 */
	private void fillTalkSkill() {
		if (mUserInfo.chatSkills == null || mUserInfo.chatSkills.length == 0) {
			mTagTaklSkill.setVisibility(View.GONE);
			mTalkSkillLayout.setVisibility(View.GONE);
			return ;
		}
		
		LayoutInflater inflater = this.getActivity().getLayoutInflater();
		LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		
		int row = (mUserInfo.chatSkills.length % 3 == 0) ? mUserInfo.chatSkills.length / 3 : mUserInfo.chatSkills.length / 3 + 1;
		for (int i=0; i<row; i++) {
			LinearLayout ll = new LinearLayout(this.getActivity());
			ll.setLayoutParams(lp1);
			
			int column = (mUserInfo.chatSkills.length > (i + 1) * 3) ? 3 : (mUserInfo.chatSkills.length - i * 3);
			for (int j=0; j<column; j++) {
				LinearLayout item = (LinearLayout) inflater.inflate(R.layout.view_chatskill_item_layout, null);
				TextView btn = (TextView) item.findViewById(R.id.chat_skill_item_btn);
				btn.setLayoutParams(lp2);
				
				int index = i * 3 + j;
				btn.setTag(mUserInfo.chatSkills[index].id);
				btn.setText(mUserInfo.chatSkills[index].name);
				if(mGender != EgmConstants.SexType.Female){
					btn.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							String skillId = ((Integer) v.getTag()).toString();
							ChatItemUserInfo info = new ChatItemUserInfo();
			                info.uid = mUserInfo.uid ;
			                info.crownId = mUserInfo.crownId ;
			                info.nick = mUserInfo.nick ;
			                info.portraitUrl192 = mUserInfo.portraitUrl192 ;
			                info.isNew = mUserInfo.isNew;
							ActivityPrivateSession.startActivityFromUserinfo(getActivity(), info, skillId);
						}
					});
				}
				
				ll.addView(item);
			}
			
			mTalkSkillLayout.addView(ll);
		}
	}
	
	/**
	 * 填充恩客榜
	 */
	private LinearLayout enkeLayout ;
	private void fillEnkeList(){
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View itemView = null ;
		int length = mUserInfoDetail.loveList.length ;
		if(length <= 3){
			for(int i = 0 ;i<length ;i++){
				addEnkeItem(i);
			}
		}else{
			for(int i = 0 ;i < 3;i++){
				itemView = inflater.inflate(R.layout.view_item_enke_list,null);
				RenderEnkeItem itemRender = new RenderEnkeItem(itemView,mUserInfoDetail.loveList[i],i+1);
				itemRender.renderView();
				mEnkeLayout.addView(itemView);
			}
			
			enkeLayout = (LinearLayout) inflater.inflate(R.layout.view_item_more_data,null);
			TextView moreEnke = (TextView)enkeLayout.findViewById(R.id.more_enke);
			moreEnke.setText(getString(R.string.enke_rank_ten));
			moreEnke.setOnClickListener(mOnClickListener);
			mEnkeLayout.addView(enkeLayout,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT));
			for(int i = 3 ;i<length ;i++){
				addEnkeItem(i);
			}
		}
	}
	
	private void addEnkeItem(int index){
		View itemView = getActivity().getLayoutInflater().inflate(R.layout.view_item_enke_list,null);
		RenderEnkeItem itemRender = new RenderEnkeItem(itemView,mUserInfoDetail.loveList[index],index+1);
		itemRender.renderView();
		if(index > 2){
			itemView.setVisibility(View.GONE);
		}
		mEnkeLayout.addView(itemView);
	}

	private EgmCallBack mCallBack = new EgmCallBack() {
		@Override
		public void onGetUserInfoDetailSucess(int transactionId,UserInfoDetail obj) {
			if (obj == null || mTid != transactionId) {
				return;
			}
			stopWaiting();
			showTip(TYPE_CONTENT);
			
			mUserInfoDetail = obj;
			mUserInfo = mUserInfoDetail.userInfo ;
			
			mSpecialGifts = mUserInfoDetail.specialGifts ;
//			MemoryDataCenter.getInstance().put(MemoryDataCenter.SPECIALGIFTS,mSpecialGifts);
			GiftInfoManager.updateSpecialGifts(mSpecialGifts);
			
			mGiftKeyboardView.renderView();
			
			refreshViews();
			
			if (!TextUtils.isEmpty(mUserInfoDetail.animat)) {
				animat = mUserInfoDetail.animat;
				checkShowSnow();
			}
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
		public void onGetGiftConfigSucess(int transactionId, GiftConfigResult obj) {
		}

		@Override
		public void onGetGiftConfigError(int transactionId, int errCode,String err) {
            if (errCode != EgmServiceCode.TRANSACTION_RES_UNCHANGED) {
                ToastUtil.showToast(getActivity(), err);
            }
		}

		@Override
		public void onSendGiftSucess(int transactionId, SendGiftResult obj) {
			stopWaiting();
			if(obj != null && mSendGiftTranId == transactionId){
				mSendResult = obj ;
				if(!TextUtils.isEmpty(mSendResult.thanks)){
					
					if (mSendResult.chatItemInfo!=null && mSendResult.chatItemInfo.message!=null) {
						if (!TextUtils.isEmpty(mSendResult.chatItemInfo.message.animat)) {
							animat = mSendResult.chatItemInfo.message.animat;
						}
					}
					
					showThanks(obj.thanks,obj.usercp,obj.intimacy);
					
					// added by lishang 播放发送成功音效
					if (EgmPrefHelper.getGiftsPicOn(getActivity(),
							ManagerAccount.getInstance().getCurrentId()))
						MediaPlayerSystemTone.instance(getActivity()).playWelecomTone("date_gift.mp3");
				}
			}
			
			if(GiftInfoManager.isCrown(mGiftId)){
				fillCrownImage(mGiftId);
			}
			
			mTid = EgmService.getInstance().doGetUserInfoDetail(mUid);
		}

		@Override
		public void onSendGiftError(int transactionId, int errCode, String err) {
			stopWaiting();
			if(mSendGiftTranId == transactionId){
				if(errCode == EgmServiceCode.TRANSACTION_COMMON_BALANCE_NOT_ENOUGHT){
					showChargeDialog();
				}else{
			            ToastUtil.showToast(getActivity(),err);
				}
			}
		}

		@Override
		public void onBlockSucess(int transactionId, int code,long uid) {
			isBlock = !isBlock ;
			if(isBlock){
				ToastUtil.showToast(getActivity(),R.string.block_suc_girl);
			}else{
				ToastUtil.showToast(getActivity(),R.string.cancel_block_suc);
			}
		}

		@Override
		public void onBlockError(int transactionId, int errCode, String err) {
			ToastUtil.showToast(getActivity(),err);
		}

		@Override
		public void onModifyDetailInfoSucess(int transactionId, UserInfo obj) {//从预览中修改资料后刷新界面
			if(obj != null){
				mUserInfo = obj ;
				fillData();
			}
		}

		@Override
		public void onLoopBack(int transactionId, LoopBack obj) {//私照解锁后个人主页私照列表改变
			if(obj != null){
				switch(obj.mType){
					case EgmConstants.LOOPBACK_TYPE.pri_pic_unlocked :
						PictureInfo picInfo = (PictureInfo)obj.mData ;
						for(PictureInfo info : mUserInfoDetail.privatePicList){
							if(picInfo.id == info.id){
								info.isViewed = true ;
								info.picUrl = picInfo.picUrl ;
								info.stateChanged = true ;
								break; 
							}
						}
						fillImageData();
						break;
				}
			}
				
		}
		
		@Override
		public void onDownloadResSucess(int transactionId, int code) {
			if (mGetAudioTransId == transactionId) {
				mAudioProgress.setBackgroundResource(R.drawable.audiointro_progress_bg);
				mAudioProgress.setIndeterminateDrawable(null);
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
		
		mShowCloseDialog.setOnKeyListener(new DialogInterface.OnKeyListener(){
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
	
	/**
	 * 发送礼物显示答谢语句
	 */
	private void showThanks(String thanks ,int userCp,int intimacy){
		mThanksDialog = showThanksDialog(thanks,userCp,intimacy);
		new Handler().postDelayed(new Runnable(){
			@Override
			public void run() {
			    if(mThanksDialog != null){   // 页面被关闭后再显示对话框，可能已经为空，需保护
			        mThanksDialog.dismiss();
			    }
				mThanksDialog = null ;
			    checkShowShareDialog();
			}
		},3000);
	}
	
	private AlertDialog mThanksDialog ;
	private LinearLayout layout;
	private TextView txt_thanks;
	private TextView txt_usercp;
	private TextView txt_intimacy;
	private AlertDialog showThanksDialog(String thanks ,int userCp,int intimacy){
		if(mThanksDialog == null){
			layout = (LinearLayout) getActivity().getLayoutInflater().inflate(
					R.layout.view_send_gift_thanks,null);
			txt_thanks = (TextView)layout.findViewById(R.id.thanks);
			txt_usercp = (TextView)layout.findViewById(R.id.usercp);
			txt_intimacy = (TextView)layout.findViewById(R.id.intimacy);
			mThanksDialog = new AlertDialog.Builder(getActivity()).setView(layout).create();
		}
		txt_thanks.setText(thanks);
		StringBuilder sb = new StringBuilder();
		sb.append(getActivity().getResources().getString(R.string.rich_index_a)).append(" +").append(userCp);
		txt_usercp.setText(sb.toString());
		txt_intimacy.setText(getActivity().getResources().getString(R.string.intimacy_index_a)+" +"+intimacy);
		mThanksDialog.show();
		return mThanksDialog ;
	}
	
	private AlertDialog mChargeDialog ;
	private void showChargeDialog(){
		if(mChargeDialog == null){
			mChargeDialog = EgmUtil.createEgmMenuDialog(
					getActivity(), 
					getActivity().getResources().getString(R.string.coins_not_enough), 
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
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		EgmService.getInstance().removeListener(mCallBack);
		if(MediaPlayerWrapper.getInstance() != null){
    		MediaPlayerWrapper.getInstance().removeMediaListener(mMediaPlayListener);
//    		MediaPlayerWrapper.getInstance().stop();
		}
		
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
			mHandler = null;
		}
		
		if (mAdapter != null) {
			mAdapter.existFullActivity();
		}
		
		//MemoryDataCenter.getInstance().remove(MemoryDataCenter.SPECIALGIFTS);
		
//		mUserInfo = null;
//		mUserInfoDetail = null;
//		mUserInfoConfig = null;
		mShowCancelBlock = null ;
		mThanksDialog = null ;
		mChargeDialog = null ;
		mShowCloseDialog = null ;
		
		mYixinHelper.removeCallback();
	}
	
	/**
	 * 开始公开照自动轮播
	 */
	private void startPublicSlide() {
		if (! mIsPublicMode && getPublicPicSize() > 1 && mHandler != null) {
			if (mHandler.hasMessages(MSG_PUBLIC_SLIDE)) {
				mHandler.removeMessages(MSG_PUBLIC_SLIDE);
			}
			
			mHandler.sendEmptyMessageDelayed(MSG_PUBLIC_SLIDE, PUBLIC_SLIDE_DURATION);
		}
	}
	
	/**
	 * 取消公开照自动轮播
	 */
	private void cancelPublicSlide() {
		if (getPublicPicSize() > 1 && mHandler != null) {
			mHandler.removeMessages(MSG_PUBLIC_SLIDE);
		}
	}
	
	private void startLayoutAnimation() {
		final int offset = getActivity().getResources().getDimensionPixelSize(
				R.dimen.userpage_animation_data_off);
		
		if (mBottomLayout.getVisibility() == View.VISIBLE) {
			mBottomLayout.setTranslationY(offset);
			
			new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					mBottomLayout.setTranslationY(0);
					
					TranslateAnimation ta = new TranslateAnimation(0, 0, offset, 0);
					ta.setDuration(250);
					
					mBottomLayout.startAnimation(ta);
				}
			}, 100);
		}
		
		TranslateAnimation ta = new TranslateAnimation(0, 0, offset >> 1, 0);
		ta.setDuration(250);
		ta.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation arg0) { }
			
			@Override
			public void onAnimationRepeat(Animation arg0) { }
			
			@Override
			public void onAnimationEnd(Animation arg0) {
				mTid = EgmService.getInstance().doGetUserInfoDetail(mUid);
				showWatting("加载中");
			}
		});
		mViewPager.startAnimation(ta);
		
		ta = new TranslateAnimation(0, 0, offset, 0);
		ta.setDuration(250);
		mContainer.startAnimation(ta);
	}

	//以下界面手势控制相关-------------------------------------------------------------------------------------------------

	public static int getLayoutTopMargin(View view) {
		int marginTop = 0;
		LayoutParams lp = view.getLayoutParams();
		if (lp instanceof RelativeLayout.LayoutParams) {
			marginTop = ((RelativeLayout.LayoutParams) lp).topMargin;
		}
		else if (lp instanceof LinearLayout.LayoutParams) {
			marginTop = ((LinearLayout.LayoutParams) lp).topMargin;
		}
		else if (lp instanceof FrameLayout.LayoutParams) {
			marginTop = ((FrameLayout.LayoutParams) lp).topMargin;
		}
		
		return marginTop;
	}
	
	private boolean isPublicPictureFullScreen() {
		return getLayoutTopMargin(mContainer) >= mDisplayHeight;
	}
	
	public boolean resetPublicPicture() {
		boolean ret = false;
		
		if (mAdapter != null && isPublicPictureFullScreen()) {
			mScrollView.setScrollY(0);

			mAdapter.existFullActivity();

			int distance = mDisplayHeight - mIniHeight + (getHeadHeight() >> 1);
			int margin = getLayoutTopMargin(mContainer);

			new AsynMove(margin, -distance).execute();

			ret = true;
		}
		
		return ret;
	}
	
	public int layoutInfoLayout(int height) {
		return layoutInfoLayout(height, null);
	}
	
	private int mHeadHeight;
	private int getHeadHeight() {
		if (mHeadHeight == 0) {
			mHeadHeight = getActivity().getResources().getDimensionPixelSize(
					R.dimen.user_page_head_size);
		}
		
		return mHeadHeight;
	}
	
	private int layoutInfoLayout(int height, MotionEvent ev) {
		
		final int minMargin = mIniHeight - (getHeadHeight() >> 1);
		final int maxMargin = mDisplayHeight;
		
		if (height < 0) {
			height = minMargin;
			
			Rect frame = new Rect();
			getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
			mDisplayHeight = mScreenHeight - frame.top;
		}

		RelativeLayout.LayoutParams tlp = (RelativeLayout.LayoutParams) 
				mInfoLayout.getLayoutParams();
		
		if (height - minMargin >= mInfoMinMargin * 3) {
			int margin = mInfoMinMargin * 3;
			
			if (margin != tlp.topMargin) {
				tlp.topMargin = margin;
				mInfoLayout.requestLayout();
			}
			
			if (height - minMargin >= mInfoMinMargin * 4 
					&& mInfoLayout.getVisibility() == View.VISIBLE) {
				AnimationUtil.alphaHide(mInfoLayout, 200);
				mAdapter.hideCoverPlay();
			}
		}
		else {
			int margin = Math.max(height - minMargin, mInfoMinMargin);
			
			if (margin != tlp.topMargin) {
				tlp.topMargin = margin;
				mInfoLayout.requestLayout();
			}
			
			if (height - minMargin < mInfoMinMargin * 2
					&& mInfoLayout.getVisibility() != View.VISIBLE) {
				AnimationUtil.alphaShow(mInfoLayout, 200);
				mAdapter.showCoverPlay();
			}
		}
		
		int delta = 0;
		
		int topHeight = Math.max(Math.min(height + (getHeadHeight() >> 1),
				mDisplayHeight), mIniHeight);
		
		if (mViewPager.getLayoutParams().height != topHeight) {
			int alpha = PUBLIC_PIC_SHADOW_ALPHA * (mDisplayHeight - topHeight) / (mDisplayHeight - mIniHeight);
			if (mAdapter != null) {
				mViewPager.setFrontColor( (alpha << 24) | PUBLIC_PIC_SHADOW);
			}
			
			mViewPager.getLayoutParams().height = topHeight;
			mTopLayout.getLayoutParams().height = topHeight;
			mTopLayout.requestLayout();
		}
		
		int containerHeight = Math.max(minMargin, Math.min(maxMargin, height));
		
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) 
				mContainer.getLayoutParams();
		
		if (lp.topMargin != containerHeight) {
			if (lp.topMargin < containerHeight) { // 往下拉
				
				if (mActionBarView.getVisibility() == View.VISIBLE
						&& containerHeight >= mIniHeight) {
					hideTopBottom();
				}
			}
			else { // 往上推
				if (mActionBarView.getVisibility() == View.GONE
						&& containerHeight <= mIniHeight) {
					showTopBottom();
				}
			}
			
			delta = containerHeight - lp.topMargin;
			lp.topMargin = containerHeight;
			mContainer.requestLayout();
			
			if (mIniHeight - (getHeadHeight() >> 1) == containerHeight) {
				if (ev != null) {
					ev.setAction(MotionEvent.ACTION_DOWN);
					mScrollView.onTouchEvent(ev);
				}
			}
		}
		
		return delta;
	}
	
	
	private boolean isAsynMoveing = false;
	class AsynMove extends AsyncTask<Void, Integer, Void> {
		
		int mStart; 
		int mDistance;
		int DELTA;
		
		public AsynMove(int start, int distance) {
			DELTA = 30;//Math.max(mHeadLayout.getHeight() >> 3, 30);
			this.mStart = start;
			
			this.mDistance = distance; 
			isAsynMoveing = true;
		}
		
		
		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(5); 
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			int distance = mDistance;
			
			if (distance < 0) {
				for (int d = 0; d > distance; d -= DELTA) {
					publishProgress(d);
					
					try {
						Thread.sleep(5); 
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			else {
				for (int d = 0; d < distance; d+= DELTA) {
					publishProgress(d);
					
					try {
						Thread.sleep(5); 
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			publishProgress(distance);
			
			return null ;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			if (values != null && values.length > 0) {
				layoutInfoLayout(mStart + values[0].intValue());
			}
		}
		
		@Override
		protected void onPostExecute(Void result) {
			isAsynMoveing = false;
			
			if (mDistance < 0) { // 往上滚动
				if (mIsPublicMode) {
					mIsPublicMode = false;
					startPublicSlide();
				}
			}
			else { // 往下滚动进入公开照大图模式
				if (! mIsPublicMode) {
					mIsPublicMode = true;
					cancelPublicSlide();
				}
			}
		}
	}
	
	private void hideTopBottom(){
		mCustomActionBar.hideWithAnim();
		mActionBarView.setVisibility(View.GONE);
		mBottomLayout.setVisibility(View.GONE);
		mTxtNextPage.setVisibility(View.GONE);
		mInfoLayout.setVisibility(View.GONE);
		mPageTip.setVisibility(View.VISIBLE);
	}
	
	private void showTopBottom(){
		mPageTip.setVisibility(View.GONE);
		
		mActionBarView.setVisibility(View.VISIBLE);
		mCustomActionBar.showWithAnim();
		if(mUid != mMyUid && mGender != EgmConstants.SexType.Female){
			mBottomLayout.setVisibility(View.VISIBLE);
			TranslateAnimation animation = new TranslateAnimation(0,0,mBottomLayout.getHeight(),0);
			animation.setDuration(300);
			mBottomLayout.startAnimation(animation);
		}
		
		mTxtNextPage.setVisibility(View.VISIBLE);
		mInfoLayout.setVisibility(View.VISIBLE);
	}
	
	private boolean isFirst = true;
    @Override
    public void onStart() {
    	super.onStart();
    	if(isFirst) {
    		isFirst = false;
    	} else {
    		checkShowShareDialog();
    	}
    }
	
	private void checkShowShareDialog() {
		LevelChangeStatusBean status = LevelChangeStatusBean.getInstance();
		long uid = ManagerAccount.getInstance().getCurrentId();
		if (status.getUid()==uid && status.getType()==LevelChangeType.Male_Level_Up) {
			new ShareDialog().showLevel(this, status.getType(), status.getOldLevel(), status.getNewLevel());
			status.clear();
		} else {
			checkShowSnow();
		}
	}
	
	public void shareDialogDismiss() {
		checkShowSnow();
	}
	
	private String animat;
	public void checkShowSnow() {
		if (!TextUtils.isEmpty(animat)) {
			snowView.snow(animat);
		}
	}
	
	private class InterHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			switch (msg.what) {
			case MSG_CLICK_VIDEO_PLAY:
				mAdapter.startPlay();
				break;
				
			case MSG_PUBLIC_SLIDE:
//				startPublicSlide();
				
				if (mViewPager != null) {
					mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
				}
				break;
				
			case MSG_AUDIO_PLAY:
				++mAudioPlayTime;
				
				mAudioIntr.setText(String.format(getString(R.string.second_unit), mAudioPlayTime));
				
				if (mAudioIntr.getVisibility() == View.INVISIBLE) {
					AlphaAnimation a = new AlphaAnimation(0, 1);
					a.setDuration(200);
					mAudioIntr.startAnimation(a);
					
					mAudioIntr.setVisibility(View.VISIBLE);
				}
				
				if (mAudioPlayTime < mUserInfo.duration) {
					mHandler.sendEmptyMessageDelayed(MSG_AUDIO_PLAY, AUDIO_DURATION);
				}
				else {
					mAudioPlayTime = 0;
				}
				break;
				
			case MSG_AUDIO_INIT:
				AlphaAnimation a = new AlphaAnimation(1, 0);
				a.setDuration(300);
				mAudioIntr.startAnimation(a);
				
				mAudioIntr.setVisibility(View.INVISIBLE);
				break;
			}
		}
	}
}