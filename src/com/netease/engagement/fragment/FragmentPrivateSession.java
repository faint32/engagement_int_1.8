package com.netease.engagement.fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher.ViewFactory;

import com.handmark.pulltorefresh.compat.LoadingListView;
import com.handmark.pulltorefresh.library.LoadingAdapterViewBaseWrap.OnLoadingListener;
import com.netease.common.image.util.ImageUtil;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.activity.ActivityLevelTable;
import com.netease.engagement.activity.ActivityPrivateSession;
import com.netease.engagement.activity.ActivityWeb;
import com.netease.engagement.adapter.HomeComeinTipHelper;
import com.netease.engagement.adapter.MsgListCursorAdapter;
import com.netease.engagement.adapter.SelectAvatarHelper;
import com.netease.engagement.adapter.UploadPictureHelper;
import com.netease.engagement.adapter.UploadPictureHelper.IUploadPicture;
import com.netease.engagement.adapter.YixinHelper;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.app.EngagementApp;
import com.netease.engagement.dataMgr.GiftInfoManager;
import com.netease.engagement.dataMgr.MemoryDataCenter;
import com.netease.engagement.dataMgr.MsgAttach;
import com.netease.engagement.dataMgr.MsgDataManager;
import com.netease.engagement.dataMgr.TopicDataManager;
import com.netease.engagement.dataMgr.cursorloader.MsgListLoader;
import com.netease.engagement.itemview.RenderEmotView;
import com.netease.engagement.pushMsg.NotificationBarMgr;
import com.netease.engagement.util.DialogComparator;
import com.netease.engagement.util.DialogInfo;
import com.netease.engagement.util.LevelChangeStatusBean;
import com.netease.engagement.util.LevelChangeStatusBean.LevelChangeType;
import com.netease.engagement.view.RecordTipView;
import com.netease.engagement.view.ShareDialog;
import com.netease.engagement.view.ShareDialogInterface;
import com.netease.engagement.view.SnowView;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.engagement.widget.DoubleClickRecordDialog;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.db.manager.LastMsgDBManager;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.db.manager.MsgDBManager;
import com.netease.service.media.MediaPlayerWrapper;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.ChatItemInfo;
import com.netease.service.protocol.meta.ChatItemUserInfo;
import com.netease.service.protocol.meta.ChatSkillInfo;
import com.netease.service.protocol.meta.GiftInfo;
import com.netease.service.protocol.meta.LoopBack;
import com.netease.service.protocol.meta.MessageInfo;
import com.netease.service.protocol.meta.MessageList;
import com.netease.service.protocol.meta.MsgExtra;
import com.netease.service.protocol.meta.PictureInfo;
import com.netease.service.protocol.meta.PortraitInfo;
import com.netease.service.protocol.meta.SendMsgResult;
import com.netease.service.protocol.meta.UserInfo;
import com.netease.service.stat.EgmStat;
import com.netease.util.MediaPlayerSystemTone;


/**
 * 聊天界面
 */
public class FragmentPrivateSession extends FragmentBase 
		implements LoaderCallbacks<Cursor>, OnClickListener, ShareDialogInterface, ViewFactory {
	
	private ActivityPrivateSession activityPrivateSession;
	
	private static String FROM_USERINFO = "FROM_USERINFO";
	private static String SKILL_ID = "SKILL_ID";

	private FragmentManager mFragmentManager;
	
	private CustomActionBar mCustomActionBar ;
	
	private ImageSwitcher bgIS;
	
	private TextView mYixinIcon ;
	private int intimacy ;
	
	public LoadingListView mLoadingListView;
	private MsgListCursorAdapter mAdapter ;
	
	private SnowView snowView;

	private LinearLayout mBottomLayout ;
	private RenderEmotView mRenderEmotView ;
	private RecordTipView mRecordTipView ;
	
	/*
	//话题部分
	private LinearLayout mTopicLayout ;
	private LinearLayout mLeftMargin ;
	//话题tab
	private RadioGroup mRadioGroup ;
	//话题pager
	private ViewPager mTopicViewPager ;
	private TopicPagerAdapter mTopicPagerAdapter ;
	
	private Animation mInAnim, mOutAnim;
	*/
	
	// 聊天技部分
	private RelativeLayout mTalkSkillRl;
	private RelativeLayout mTalkSkillLayout;
	private RelativeLayout mTalkSkillBg;
	private LinearLayout mTalkSkillContiner;
	private boolean mIsTalkSkillAnimating;
	
	private ChatItemUserInfo mChatUserInfo ;
	
	private boolean isFromUserInfo;
	private String skillId;
	
	//自身的id
	private long mMyId ;
	//聊天对象的id
	private long mAnotherId ;
	//自己的头像
	private String mAvatar ;
	//自身的性别
	private int mSex ;
	
	private int mLoaderId = FragmentPrivateSession.class.getSimpleName().hashCode();
	
	private int pageNo ;
	
	private UploadPictureHelper mUploadPictureHelper;
	
	//小爱助手底部按钮栏
	private LinearLayout mXiaoAiBottomLayout;
	private TextView mWhoInviteMe;
	private View mDivider;
	private TextView mFeedback;
	private TextView mHelpCenter;
	private Context context;
	private PopupWindow mPopupWindow ;//易信好友提示窗口
	
	// 标示从其他页面回来，是否需要刷新数据   
	// ture:不需要  如从看公开照、看私照、看视频、阅后即焚回来，不需要刷新数据，因为没产生新数据
	// false:需要   如从拍照、选照片、拍摄、选视频回来，需要刷死你数据，因为产生了一条新的聊天数据
	public static boolean notNeedRefresh;  
	private GestureDetector mGestureDetector;

	protected DoubleClickRecordDialog audioRecordDialog;

	/**
	 * 双击录音提醒指示
	 */
	private LinearLayout doubleClickGuideLayout;
	private boolean doubleClickIsShowing=false;
	/**
	 * 空白处双击标志位，防止点击太快，出现重叠对话框
	 */
	private Boolean hasDialogShowing=false;
	private YixinHelper mYixinHelper;//用于已是易信好友跳去易信聊天
	private  boolean isTopLevelGuideShwoing = false;
	
	private RelativeLayout bgRL;
	private ImageView fireSwitchIV;
	private boolean isOpenFire;
	
	private boolean isShowingDialog = false;
	private PriorityQueue<DialogInfo> priorityQueue =  new PriorityQueue<DialogInfo>(11, new DialogComparator());  
	enum DialogType { // 按优先级降序排列
		First_Gift,
		Special_Gift,
		First_Receive_Fire_Message,
		First_Open_Fire,
		Fire_Screen_Shot,
		Snow,
	}
	
	public static FragmentPrivateSession newInstance(ChatItemUserInfo userInfo, 
			boolean isFromUserInfo, boolean isFromScreenShotNotificatoin, String skillId) {
		FragmentPrivateSession fragment = new FragmentPrivateSession();
		Bundle args = new Bundle();
		args.putParcelable(EgmConstants.BUNDLE_KEY.CHAT_ITEM_USER_INFO, userInfo);
		args.putBoolean(FROM_USERINFO, isFromUserInfo);
		args.putBoolean(EgmConstants.BUNDLE_KEY.CHAT_FROM_SCREEN_SHOT_NOTIFICATION, isFromScreenShotNotificatoin);
		args.putString(SKILL_ID, skillId);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context=getActivity();	
		if(this.getArguments() == null ||
				this.getArguments().getParcelable(EgmConstants.BUNDLE_KEY.CHAT_ITEM_USER_INFO) == null){
			getActivity().finish();
			return ;
		}
		mFragmentManager = getFragmentManager();
		
		EgmService.getInstance().addListener(mCallBack);
		
		mChatUserInfo = this.getArguments().getParcelable(EgmConstants.BUNDLE_KEY.CHAT_ITEM_USER_INFO);
		isFromUserInfo = this.getArguments().getBoolean(FROM_USERINFO, false);
		skillId = (this.getArguments().getString(SKILL_ID));
		
		boolean isFromScreenShotNotificatoin = this.getArguments().getBoolean(EgmConstants.BUNDLE_KEY.CHAT_FROM_SCREEN_SHOT_NOTIFICATION, false);
		if (isFromScreenShotNotificatoin) {
			// 只添加到队列，但不调用队列显示。等getMessageList成功后，自动再显示
			priorityQueue.offer(new DialogInfo(DialogType.Fire_Screen_Shot.ordinal(), null));
		}
		
//		MemoryDataCenter.getInstance().put(MemoryDataCenter.CURRENT_CHAT_UID, mChatUserInfo.uid);
		MemoryDataCenter.getInstance().put(MemoryDataCenter.CURRENT_COMPARE_CROWNID,mChatUserInfo.crownId);
		MemoryDataCenter.getInstance().put(MemoryDataCenter.CURRENT_CHAT_OTHER_PROFILE,mChatUserInfo.portraitUrl192);
		MemoryDataCenter.getInstance().put(MemoryDataCenter.CURRENT_CAHT_OTHER_NICK,mChatUserInfo.nick);
		
		mMyId = ManagerAccount.getInstance().getCurrentId();
		mSex = ManagerAccount.getInstance().getCurrentGender();
		
		mAnotherId = mChatUserInfo.uid ;
		
//		MediaPlayerWrapper.getInstance().doBindService(EngagementApp.getAppInstance());
		
		mUploadPictureHelper = new UploadPictureHelper(this, new IUploadPicture(){
	        @Override
	        public void onStartUpload() {
	            showWatting(getString(R.string.common_tip_is_updating));
	        }
	        
	        @Override
	        public void onFinishUpload() {
	            stopWaiting();
	        }
	    });
		mUploadPictureHelper.registerCallback();
		
		notNeedRefresh = false;
	}
	
	@Override
	public void onDestroyView() {
		if (mRenderEmotView != null) {
			if (mRenderEmotView.mAudio) { // 没在显示录音layout
				EgmPrefHelper.putIsShownRecordingInlastPrivateSession(getActivity(), mMyId, false);
			} else { // 正在显示录音layout
				EgmPrefHelper.putIsShownRecordingInlastPrivateSession(getActivity(), mMyId, true);
			}
		}
		
		super.onDestroyView();
	}
	
	private void initTitle(){
		mCustomActionBar = ((ActivityEngagementBase)getActivity()).getCustomActionBar();
//		mCustomActionBar.getCustomView().setBackgroundColor(getResources().getColor(R.color.pri_header_back));
		mCustomActionBar.setCustomBarBackground(R.drawable.bg_message_topbar);
		mCustomActionBar.setLeftBackgroundResource(R.drawable.titlebar_b_selector);
		mCustomActionBar.setLeftAction(R.drawable.bar_btn_back_b, R.string.back);
		mCustomActionBar.setLeftTitleColor(getResources().getColor(R.color.white));
		mCustomActionBar.setLeftClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mIsTalkSkillAnimating) {
					return;
				}
				if (mTalkSkillLayout!=null && mTalkSkillLayout.getVisibility()==View.VISIBLE) {
					hideTalkSkillLayout();
				} else {
					FragmentPrivateSession.this.getActivity().finish();
				}
			}
		});
		
		mCustomActionBar.setMiddleTitleColor(getResources().getColor(R.color.black));
		mCustomActionBar.setMiddleTitle(mChatUserInfo.nick);
		mCustomActionBar.setMiddleTitleSize(20);
		
		mCustomActionBar.setRightBackgroundResource(R.drawable.titlebar_b_selector);
		mCustomActionBar.setRightAction(-1,R.string.rec_nvshen_skill);
		mCustomActionBar.setRightTitleColor(getResources().getColor(R.color.white));
		mCustomActionBar.setRightTitleSize(17);
		mCustomActionBar.setRightClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mTalkSkillLayout != null) {
					if(mIsTalkSkillAnimating) {
						return;
					}
				    if(mTalkSkillLayout.getVisibility() == View.VISIBLE){
				    	hideTalkSkillLayout();
					} else {
						showTalkSkillLayout();
					}
				}
			}
		});
		mCustomActionBar.setRightVisibility(View.INVISIBLE);
		
		if(isSystemUser()){
			mCustomActionBar.hideRightTitle();
		} else {
		    mCustomActionBar.showSubTitle();
		    mCustomActionBar.setMiddleTitleSize(17);
		    mCustomActionBar.setSubTitle(String.format(getResources().getString(R.string.inm_num),0));
		}
		mCustomActionBar.setRightIcon(R.drawable.icon_pgchat_ear_black);
		if(!isSystemUser()){
			if(EgmPrefHelper.getReceiverModeOn(getActivity())){
				changeTitle(true);
			}
		}
	}
	
	private void changeTitle(boolean bLeft){
		if(mCustomActionBar == null)
			return;
		
		if(bLeft){
			mCustomActionBar.setTitleAlignLeft(12);
			mCustomActionBar.setRightIconShow(true);
		} else {
			mCustomActionBar.setTitleAlignCenter();
			mCustomActionBar.setRightIconShow(false);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.fragment_private_session,container, false);
		init(root);
		
		if (savedInstanceState != null) {
			if (mRenderEmotView != null) {
				mRenderEmotView.onRestoreId(savedInstanceState);
			}
			if (mUploadPictureHelper != null) {
				mUploadPictureHelper.onSaveInstanceState(savedInstanceState);
			}
		}
		
		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initTitle();
		mLoadingListView.load();
		getLoaderManager().restartLoader(mLoaderId,null,FragmentPrivateSession.this);
		
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    if (mRenderEmotView != null) {
            mRenderEmotView.onSaveInstanceState(outState);
        }
	    
	    if(mUploadPictureHelper != null) {
	    	mUploadPictureHelper.onSaveInstanceState(outState);
	    }
	}
	
	@Override
	public void onResume() {
		super.onResume();
		MemoryDataCenter.getInstance().put(MemoryDataCenter.CURRENT_CHAT_UID, mChatUserInfo.uid);
		NotificationBarMgr.getInstance(EngagementApp.getAppInstance()).cancelPushChat();
		
		if (mAdapter != null) {
			mAdapter.onResume();
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		if (mAdapter != null) {
			mAdapter.onPause();
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		MemoryDataCenter.getInstance().remove(MemoryDataCenter.CURRENT_CHAT_UID);
	}

	private void init(View root) {
		
		bgRL = (RelativeLayout) root.findViewById(R.id.bg);
		
		Animation inAnimation = AnimationUtils.loadAnimation(this.getActivity(), android.R.anim.fade_in);
		inAnimation.setDuration(300);
		Animation outAnimation = AnimationUtils.loadAnimation(this.getActivity(), android.R.anim.fade_out);
		outAnimation.setDuration(300);
		bgIS = (ImageSwitcher) root.findViewById(R.id.bgIS);
		bgIS.setFactory(this);
		bgIS.setInAnimation(inAnimation);
		bgIS.setOutAnimation(outAnimation);
		
		fireSwitchIV = (ImageView) root.findViewById(R.id.fire_switch);
		fireSwitchIV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				isOpenFire = !isOpenFire;
				fireSwitch(true);
			}
		});
		
		mAvatar = ManagerAccount.getInstance().getCurrentAvatar();
		//亲密度
		mYixinIcon = (TextView)root.findViewById(R.id.yixin_tip);
		mYixinIcon.setOnClickListener(this);
		
//		initTopicLayout(root);
		mTalkSkillRl = (RelativeLayout)root.findViewById(R.id.talk_skill_rl);
		
		if (!isSystemUser()) { // 对于系统用户（小爱、易信），不初始化mRenderEmotView，也不进行进入聊天页次数计数操作
		
			mRenderEmotView = new RenderEmotView(this,root);
			if (!TextUtils.isEmpty(skillId)) {
				setSendTalkSkill(Integer.valueOf(skillId));
			}
			
			doubleClickGuideLayout = (LinearLayout) root.findViewById(R.id.double_click_record_guide);	
			
			if(ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Male
					&& !EgmPrefHelper.getHasShownTipInPrivateSession(getActivity(), mMyId)) {
				if (!isSystemUser()) {
					mRenderEmotView.showGiftTipForMan(); // 男性第一次进入聊天界面弹出送礼物入口
					EgmPrefHelper.putHasShownTipInPrivateSession(getActivity(), mMyId);
					mRenderEmotView.mGiftTipShowing = true;
				}
			} else  if(!EgmPrefHelper.getHasShownTipInPrivateSession(getActivity(), mMyId)) {
				if (!isSystemUser()) {
					mRenderEmotView.showRecordingLayout(true); // 女性第一次进入聊天界面弹出录音layout
					EgmPrefHelper.putHasShownTipInPrivateSession(getActivity(), mMyId);
					isTopLevelGuideShwoing = true;
				}
			} else if (!EgmPrefHelper.getHasShownDoubleClickGuide(getActivity(), mMyId)) {
				if (!isSystemUser()) {
					EgmPrefHelper.putHasShownDoubleClickGuide(getActivity(), mMyId);// 第二次进入聊天界面现实双击录音指引
					doubleClickGuideLayout.setVisibility(View.VISIBLE);
					if (doubleClickGuideLayout.getBackground() != null) {
						doubleClickGuideLayout.getBackground().setAlpha(245);
					}
					doubleClickIsShowing = true;
				}
			} else {//扩展

				// 每次进入聊天页，都需要判断上一次离开聊天页时，是否显示录音layout
				if (!isSystemUser()) {
					boolean b = EgmPrefHelper.getIsShownRecordingInlastPrivateSession(getActivity(), mMyId);
					if (b) {
						mRenderEmotView.showRecordingLayout(false);
					}
				}
			}
			// 利用次数标记是否需要弹出索礼物引导
			if (!isSystemUser() && mSex == EgmConstants.SexType.Female) {
				int count = EgmPrefHelper.getPrivateSessionCount(getActivity(), mMyId);
				if (count >= 4) {
					EgmPrefHelper.putShouldGirlGiftsTipOn(getActivity(), mMyId);
				} else {
					if (count < 0)
						count = 1;
					else
						count++;
					EgmPrefHelper.putPrivateSessionCount(getActivity(), mMyId, count);
				}
			}
		
		}
		
		mAdapter = new MsgListCursorAdapter(getActivity(),null);
		mAdapter.setUploadPictureHelper(mUploadPictureHelper);
		mAdapter.setChatUser(mChatUserInfo);
		mAdapter.setFromUserInfo(isFromUserInfo);
		mAdapter.setMyId(mMyId);
		
		mLoadingListView = (LoadingListView) root.findViewById(R.id.msg_list);
		mLoadingListView.disableLoadingMore();
		mLoadingListView.getRefreshableView().setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		mLoadingListView.setAdapter(mAdapter);
		mLoadingListView.setShowIndicator(false);
		
		mLoadingListView.setOnLoadingListener(new OnLoadingListener() {
			@Override
			public void onRefreshing() {
				pageNo ++ ;
				FragmentPrivateSession.this.getLoaderManager().restartLoader(mLoaderId,null,FragmentPrivateSession.this);
			}
			@Override
			public void onLoading() {
				EgmService.getInstance().doGetMsgList(mChatUserInfo.uid);
			}
			@Override	
			public void onLoadingMore() {
			}
		});
		
		mLoadingListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) { // 停止滑动
					if (isOpenFire) {
						fireSwitchIV.setBackgroundResource(R.drawable.btn_pgchat_fire_open);
					} else {
						fireSwitchIV.setBackgroundResource(R.drawable.btn_pgchat_fire_close);
					}
				} else { // 滑动中
					if (isOpenFire) {
						fireSwitchIV.setBackgroundResource(R.drawable.icon_pgchat_fire_l_prs_alpha30);
					} else {
						fireSwitchIV.setBackgroundResource(R.drawable.icon_pgchat_fire_l_alpha30);
					}
				}
			}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
		
		mLoadingListView.setVisibility(View.INVISIBLE);
		mLoadingListView.postDelayed(new Runnable() {
			@Override
			public void run() {
				mLoadingListView.setVisibility(View.VISIBLE);
			}
		}, 1000);
		
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		snowView = (SnowView) root.findViewById(R.id.snowView);
		snowView.SetView(dm.heightPixels, dm.widthPixels);
		
		mRecordTipView = (RecordTipView)root.findViewById(R.id.record_tip);
		mBottomLayout = (LinearLayout)root.findViewById(R.id.private_session_bottom);
		
		mXiaoAiBottomLayout = (LinearLayout)root.findViewById(R.id.xiaoai_bottom_layout);
		mWhoInviteMe = (TextView)root.findViewById(R.id.who_invite_me);
		mWhoInviteMe.setOnClickListener(this);
		mDivider = root.findViewById(R.id.view);
		
		
		mFeedback = (TextView)root.findViewById(R.id.chat_feedback);
		mFeedback.setOnClickListener(this);
		
		mHelpCenter = (TextView)root.findViewById(R.id.chat_help_center);
		mHelpCenter.setOnClickListener(this);
		
		if(isSystemUser()){
			mBottomLayout.setVisibility(View.GONE);
		}
		
		if(isXiaoAiUser()) {
			mXiaoAiBottomLayout.setVisibility(View.VISIBLE);
			RelativeLayout.LayoutParams lp = new 
					RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			lp.addRule(RelativeLayout.ABOVE, R.id.xiaoai_bottom_layout);
			mLoadingListView.setLayoutParams(lp);
			
			if(ManagerAccount.getInstance().getCurrentAccount()!= null && 
					ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Male) {
				mWhoInviteMe.setVisibility(View.VISIBLE);
				mDivider.setVisibility(View.VISIBLE);
			}
		}
		if (!isSystemUser()) {
			mLoadingListView.getRefreshableView().setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (mGestureDetector != null) {
						mGestureDetector.onTouchEvent(event);
					}
					return false;
				}
			});

			mGestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {

				@Override
				public boolean onDoubleTap(MotionEvent e) {
					showDoubleClickRecordDialog();
					return true;
				}
			});
		} else {
			fireSwitchIV.setVisibility(View.GONE);
		}
		
		isOpenFire = EgmPrefHelper.getSessionFireFlag(getActivity(), mMyId, mAnotherId);
		fireSwitch(false);
		
		mAdapter.setOpenFire(isOpenFire);
	}
	
	/*
	// 找话题相关
	private void initTopicLayout(View root){
		mTopicLayout = (LinearLayout) root.findViewById(R.id.topic_layout);
		mInAnim = AnimationUtils.loadAnimation(root.getContext(), R.anim.push_right_in);
		mOutAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.push_right_out);
	    mOutAnim.setAnimationListener(new AnimationListener() {
	            @Override
	            public void onAnimationStart(Animation animation) {
	            }
	            @Override
	            public void onAnimationRepeat(Animation animation) {
	            }
	            @Override
	            public void onAnimationEnd(Animation animation) {
	                mTopicLayout.setVisibility(View.GONE);
	            }
	        });
		
		mLeftMargin = (LinearLayout)mTopicLayout.findViewById(R.id.left_margin);
		mLeftMargin.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(mTopicLayout.getVisibility() == View.VISIBLE){
					hideTopicLayout();
					return true;
				}
				return false ;
			}
		});
		
		mRadioGroup = (RadioGroup) mTopicLayout.findViewById(R.id.topic_tabs);
		RadioButton youmo = (RadioButton) mRadioGroup.findViewById(R.id.youmo);
		RadioButton hello = (RadioButton) mRadioGroup.findViewById(R.id.say_hello);
		if(ManagerAccount.getInstance().getCurrentAccount().mSex == EgmConstants.SexType.Female) {
		    hello.setText(R.string.say_hello);
			youmo.setText(R.string.ask_gift);
		} else{
		    hello.setText(R.string.str_gouda);
            youmo.setText(R.string.str_tiaoxi);
		}
		 
		mTopicViewPager = (ViewPager)mTopicLayout.findViewById(R.id.topic_pager);
		mTopicViewPager.setOnPageChangeListener(new OnPageChangeListener(){
			@Override
			public void onPageScrollStateChanged(int arg0) {}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {}

			@Override
			public void onPageSelected(int arg0) {
				mRadioGroup.check(mRadioGroup.getChildAt(arg0).getId());
			}
		});
		
		mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch(checkedId){
					case R.id.say_hello:
						mTopicViewPager.setCurrentItem(0);
						break;
					case R.id.youmo:
						mTopicViewPager.setCurrentItem(1);
						break;
				}
			}
		});
		((RadioButton)mRadioGroup.getChildAt(0)).setChecked(true);
		
		mTopicPagerAdapter = new TopicPagerAdapter(FragmentPrivateSession.this);
		mTopicViewPager.setAdapter(mTopicPagerAdapter);
		
	}
	*/
	
	/**
	 * 判断是不是系统用户
	 * @return
	 */
	private boolean isSystemUser(){
		if(mAnotherId == EgmConstants.System_Sender_Id.TYPE_XIAOAI
				|| mAnotherId == EgmConstants.System_Sender_Id.TYPE_YIXIN){
			return true ;
		}
		return false ;
	}
	
	private boolean isXiaoAiUser() {
		if(mAnotherId == EgmConstants.System_Sender_Id.TYPE_XIAOAI) {
			return true;
		}
		return false;
	}
	
	/*
	// 隐藏找话题
	public void hideTopicLayout(){
		mTopicLayout.startAnimation(mOutAnim);
	}

	// 显示找话题
    public void showTopicLayout(){
        mTopicLayout.setVisibility(View.VISIBLE);
        mTopicLayout.startAnimation(mInAnim);
    }
    
	// 发送话题
	public void setSendTopic(String topic){
		if(mRenderEmotView != null){
			mRenderEmotView.setSendTopic(topic);
		}
	}
	*/
	
	/**
	 * 发送礼物
	 * @param giftName
	 * @param giftId
	 */
	public void setSendGift(String giftName ,int giftId){
		if(mRenderEmotView != null){
			mRenderEmotView.setSendGift(giftName ,giftId);
		}
	}
	
	/**
	 * 显示录音提示
	 */
	public void showRecordTip(){
		if(mRecordTipView.getVisibility() == View.GONE){
			mRecordTipView.setVisibility(View.VISIBLE);
			mRecordTipView.startTiming();
		}
	}
	
	/**
	 * 隐藏录音提示
	 */
	public void hideRecordTip(){
		if(mRecordTipView.getVisibility() == View.VISIBLE){
			mRecordTipView.setVisibility(View.GONE);
			mRecordTipView.clear();
		}
	}
	
	/**
	 * 设置为取消状态
	 */
	public void setCancelState(boolean cancel){
		if(mRecordTipView.getVisibility() == View.VISIBLE){
			mRecordTipView.setCancelState(cancel);
		}
	}
	
	/**
	 * 获取ChatItemInfo
	 * @param info
	 * @return
	 */
	private ChatItemInfo getChatItemInfo(MessageInfo info){
		ChatItemInfo chatItemInfo = new ChatItemInfo();
		chatItemInfo.message = info ;
		chatItemInfo.notReadCount = 0 ;
		chatItemInfo.anotherUserInfo = mChatUserInfo ;
		return chatItemInfo ;
	}
	
	/**
	 * 发送一条消息
	 */
	public void sendMsg(
			MessageInfo data,
			int msgType ,
			long toUserId ,
			String filePath ,
			String text,
			String privacyId ,
			String giftId ,
			String duration ,
			String faceId){
		EgmService.getInstance().doSendMsg(data, filePath);
	}
	
	/**
	 * 发送表情消息
	 */
	public void sendFaceMsg(String faceId, String faceDesc) {
		mLoadingListView.getRefreshableView().setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		MessageInfo info = new MessageInfo();
		info.setMsgId(-1 * System.currentTimeMillis());
		info.setSender(mMyId);
		info.setReceiver(mAnotherId);
		info.setType(EgmProtocolConstants.MSG_TYPE.MSG_TYPE_FACE);
		info.setTime(System.currentTimeMillis());
		info.setMsgContent(faceDesc);
		info.setProfileUrl(mAvatar);
		info.setStatus(EgmConstants.Sending_State.SENDING);
		info.setSendType(EgmProtocolConstants.MSG_SENDTYPE.MSG_SENDTYPE_COMMON);
		info.setFaceId(faceId);
		
		MsgDBManager.insertMsgInfo(info);
		LastMsgDBManager.handelNewMsg(getChatItemInfo(info));
		getLoaderManager().restartLoader(mLoaderId,null,FragmentPrivateSession.this);
		
		sendMsg(
				info,
				EgmProtocolConstants.MSG_TYPE.MSG_TYPE_FACE, 
				mChatUserInfo.uid, 
				null, 
				null, 
				null, 
				null, 
				null,
				faceId);
	}
	
	/**
	 * 发送文本消息
	 */
	public void sendTextMsg(String text){
		mLoadingListView.getRefreshableView().setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		MessageInfo info = new MessageInfo();
		info.setMsgId(-1 * System.currentTimeMillis());
		info.setSender(mMyId);
		info.setReceiver(mAnotherId);
		info.setType(EgmProtocolConstants.MSG_TYPE.MSG_TYPE_TEXT);
		info.setTime(System.currentTimeMillis());
		info.setMsgContent(text);
		info.setProfileUrl(mAvatar);
		info.setStatus(EgmConstants.Sending_State.SENDING);
		if (isOpenFire) {
			info.setSendType(EgmProtocolConstants.MSG_SENDTYPE.MSG_SENDTYPE_FIRE);
		} else {
			info.setSendType(EgmProtocolConstants.MSG_SENDTYPE.MSG_SENDTYPE_COMMON);
		}
		
		MsgDBManager.insertMsgInfo(info);
		LastMsgDBManager.handelNewMsg(getChatItemInfo(info));
		getLoaderManager().restartLoader(mLoaderId,null,FragmentPrivateSession.this);
		
		sendMsg(
				info,
				EgmProtocolConstants.MSG_TYPE.MSG_TYPE_TEXT, 
				mChatUserInfo.uid, 
				null, 
				text, 
				null, 
				null, 
				null,
				null);
	}
	
	/**
	 * 发送本地照片
	 */
	public void sendLocalImage(String filePath ,int isCameraPhoto){
		mLoadingListView.getRefreshableView().setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		Bitmap smallBmp = null ;
		try {
			smallBmp = ImageUtil.getBmpSizeLimit(getActivity(),filePath, 
					EgmConstants.Chat_Image_Width.MAX_WIDTH, 
					EgmConstants.Chat_Image_Width.MIN_WIDTH);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		MessageInfo info = new MessageInfo();
		info.setMsgId(-1 * System.currentTimeMillis());
		info.setSender(mMyId);
		info.setReceiver(mAnotherId);
		info.setType(EgmProtocolConstants.MSG_TYPE.MSG_TYPE_LOCAL_PIC);
		long time = System.currentTimeMillis() ;
		info.setTime(time);
		info.setProfileUrl(mAvatar);
		info.setStatus(EgmConstants.Sending_State.SENDING);
		info.setMediaUrl(filePath);
		info.setIsCameraPhoto(isCameraPhoto);
		
		MsgAttach attach = new MsgAttach();
		String path = MsgDataManager.getInstance().convertPath(EgmProtocolConstants.MSG_TYPE.MSG_TYPE_LOCAL_PIC,String.valueOf(time));
		if(ImageUtil.saveBitMaptoFile(smallBmp, path)){
			attach.setSmallImagePath(path);
		}
		info.setAttach(MsgAttach.toJsonString(attach));
		
		if (isOpenFire) {
			info.setSendType(EgmProtocolConstants.MSG_SENDTYPE.MSG_SENDTYPE_FIRE);
		} else {
			info.setSendType(EgmProtocolConstants.MSG_SENDTYPE.MSG_SENDTYPE_COMMON);
		}
		
		MsgDBManager.insertMsgInfo(info);
		LastMsgDBManager.handelNewMsg(getChatItemInfo(info));
		getLoaderManager().restartLoader(mLoaderId,null,FragmentPrivateSession.this);
		
		sendMsg(
				info ,
				EgmProtocolConstants.MSG_TYPE.MSG_TYPE_LOCAL_PIC, 
				mChatUserInfo.uid, 
				filePath, 
				null, 
				null, 
				null, 
				null,
				null);
	}
	
	/**
	 * 获取视频文件缩略图
	 * @param filePath
	 * @return
	 */
	private Bitmap getVideoThumb(String filePath){
		Bitmap thumb = null ;
		if(!TextUtils.isEmpty(filePath)){
			thumb = ThumbnailUtils.createVideoThumbnail(filePath,
					MediaStore.Video.Thumbnails.MINI_KIND);
		}
		return thumb ;
	}
	
	/**
	 * 发送音频
	 */
	public void sendAudio(String filePath,String duration){
		mLoadingListView.getRefreshableView().setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		MessageInfo info = new MessageInfo();
		info.setMsgId(-1 * System.currentTimeMillis());
		info.setSender(mMyId);
		info.setReceiver(mAnotherId);
		info.setType(EgmProtocolConstants.MSG_TYPE.MSG_TYPE_AUDIO);
		info.setTime(System.currentTimeMillis());
		info.setProfileUrl(mAvatar);
		info.setStatus(EgmConstants.Sending_State.SENDING);
		info.setDuration(Integer.parseInt(duration));
		
		MsgAttach attach = new MsgAttach();
		attach.setAudioPath(filePath);
		info.setAttach(MsgAttach.toJsonString(attach));
		
		if (isOpenFire) {
			info.setSendType(EgmProtocolConstants.MSG_SENDTYPE.MSG_SENDTYPE_FIRE);
		} else {
			info.setSendType(EgmProtocolConstants.MSG_SENDTYPE.MSG_SENDTYPE_COMMON);
		}
		
		MsgDBManager.insertMsgInfo(info);
		LastMsgDBManager.handelNewMsg(getChatItemInfo(info));
		getLoaderManager().restartLoader(mLoaderId,null,FragmentPrivateSession.this);
		
		sendMsg(
				info ,
				EgmProtocolConstants.MSG_TYPE.MSG_TYPE_AUDIO, 
				mChatUserInfo.uid, 
				filePath, 
				null, 
				null, 
				null, 
				String.valueOf(Long.parseLong(duration)),
				null);
	}
	
	/**
	 * 发送视频
	 * @param filePath
	 * @param duration
	 */
	public void sendVideo(String filePath ,String duration){
		mLoadingListView.getRefreshableView().setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		Bitmap thumb = getVideoThumb(filePath);
		MessageInfo info = new MessageInfo();
		info.setMsgId(-1 * System.currentTimeMillis());
		info.setSender(mMyId);
		info.setReceiver(mAnotherId);
		info.setType(EgmProtocolConstants.MSG_TYPE.MSG_TYPE_VIDEO);
		long time = System.currentTimeMillis();
		info.setTime(time);
		info.setProfileUrl(mAvatar);
		info.setStatus(EgmConstants.Sending_State.SENDING);
		info.setDuration(Integer.parseInt(duration));
		
		MsgAttach attach = new MsgAttach();
		String path = MsgDataManager.getInstance().convertPath(EgmProtocolConstants.MSG_TYPE.MSG_TYPE_LOCAL_PIC,String.valueOf(time));
		if(ImageUtil.saveBitMaptoFile(thumb,path)){
			attach.setSmallImagePath(path);
		}
		attach.setVideoPath(filePath);
		info.setAttach(MsgAttach.toJsonString(attach));
		
		if (isOpenFire) {
			info.setSendType(EgmProtocolConstants.MSG_SENDTYPE.MSG_SENDTYPE_FIRE);
		} else {
			info.setSendType(EgmProtocolConstants.MSG_SENDTYPE.MSG_SENDTYPE_COMMON);
		}
		
		MsgDBManager.insertMsgInfo(info);
		LastMsgDBManager.handelNewMsg(getChatItemInfo(info));
		getLoaderManager().restartLoader(mLoaderId,null,FragmentPrivateSession.this);
		
		sendMsg(
				info, 
				EgmProtocolConstants.MSG_TYPE.MSG_TYPE_VIDEO, 
				mChatUserInfo.uid, 
				filePath, 
				null, 
				null, 
				null, 
				duration,
				null);
	}
	
	/**
	 * 发送私照
	 */
	public void sendPrivateImage(String privacyId , String smallUrl ,String picUrl){
		mLoadingListView.getRefreshableView().setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		MessageInfo info = new MessageInfo();
		info.setMsgId(-1 * System.currentTimeMillis());
		info.setSender(mMyId);
		info.setReceiver(mAnotherId);
		info.setType(EgmProtocolConstants.MSG_TYPE.MSG_TYPE_PRIVATE_PIC);
		info.setTime(System.currentTimeMillis());
		info.setProfileUrl(mAvatar);
		info.setStatus(EgmConstants.Sending_State.SENDING);
		info.setExtraId(Long.parseLong(privacyId));
		info.setMediaUrl(picUrl);
		
		MsgAttach attach = new MsgAttach();
		attach.setSmallImagePath(smallUrl);
		info.setAttach(MsgAttach.toJsonString(attach));
		
		MsgDBManager.insertMsgInfo(info);
		LastMsgDBManager.handelNewMsg(getChatItemInfo(info));
		getLoaderManager().restartLoader(mLoaderId,null,FragmentPrivateSession.this);
		
		sendMsg(
				info ,
				EgmProtocolConstants.MSG_TYPE.MSG_TYPE_PRIVATE_PIC, 
				mChatUserInfo.uid, 
				null, 
				null, 
				privacyId, 
				null, 
				null,
				null);
	}
	
	/**
	 * 发送礼物
	 */
	public void sendGift(String giftId){
		mLoadingListView.getRefreshableView().setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		MessageInfo info = new MessageInfo();
		info.setMsgId(-1 * System.currentTimeMillis());
		info.setSender(mMyId);
		info.setReceiver(mAnotherId);
		info.setType(EgmProtocolConstants.MSG_TYPE.MSG_TYPE_GIFT);
		info.setTime(System.currentTimeMillis());
		info.setProfileUrl(mAvatar);
		info.setStatus(EgmConstants.Sending_State.SENDING);
		info.setExtraId(Long.parseLong(giftId));
		info.setUsercp(0);
		
		MsgDBManager.insertMsgInfo(info);
		LastMsgDBManager.handelNewMsg(getChatItemInfo(info));
		getLoaderManager().restartLoader(mLoaderId,null,FragmentPrivateSession.this);
		EgmStat.log(EgmStat.LOG_GIVE_GIFT_DETAIL, EgmStat.SCENE_USER_DETAIL, 
				mChatUserInfo.uid, Integer.parseInt(giftId));
		sendMsg(
				info ,
				EgmProtocolConstants.MSG_TYPE.MSG_TYPE_GIFT, 
				mChatUserInfo.uid, 
				null, 
				null, 
				null, 
				giftId, 
				null,
				null);
	}
	
	

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode != Activity.RESULT_OK){
    		return ;
    	}
		if(mHomeComeinTipHelper != null){//如果有上传头像引导，优先传给上传头像引导
			mHomeComeinTipHelper.onActivityResult(requestCode, resultCode, data);
			return;
		}
		if(isSystemUser()) {
			if(mUploadPictureHelper != null) {
				mUploadPictureHelper.onActivityResult(requestCode, resultCode, data);  
			}
		} else {
			if (mRenderEmotView != null) {
				mRenderEmotView.onActivityResult(requestCode, resultCode, data);
	        }
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private EgmCallBack mCallBack = new EgmCallBack(){
		@Override
		public void onGetMsgListSucess(int transactionId, MessageList obj) {
//			mLoadingListView.onLoadingComplete();
			
			GiftInfoManager.updateSpecialGifts(obj.specialGifts);
			
			if (mAdapter != null) {
				mAdapter.setFireStart(obj.fireMsgList);
			}
			
			if (mRenderEmotView != null) {
				mRenderEmotView.renderGiftKeyBoard();
			}
			
			// 亲密度
			intimacy = obj.intimacy ;
			if(mCustomActionBar != null){
			    mCustomActionBar.setSubTitle(String.format(getString(R.string.inm_num),intimacy));
			}
			
			// 聊天技
			if (obj.chatSkills!=null && obj.chatSkills.length>0) {
				initTaklSkillLayout(obj.chatSkills);
			}
			
			getLoaderManager().restartLoader(mLoaderId,null,FragmentPrivateSession.this);
			
			EgmService.getInstance().doSetMsgRead(mAnotherId,null);
			
			processDialogPopup(obj.msgList, MessageList_From.From_Pull);
			
//			if(!isSystemUser()){
//	            handleYixinTip(obj.isYixinFriend);//处理易信好友提醒
//			}
		}

		@Override
		public void onGetMsgListError(int transactionId, int errCode, String err) {
//			errCode = EgmServiceCode.TRANSACTION_CHAT_AVATAR_CHECKING;
//			err = "他要看到头像再聊天，等头像审核通过后再与他聊天吧";
//			errCode = EgmServiceCode.TRANSACTION_CHAT_LOW_LEVEL;
//			err = "他只跟达到2级的女神聊，努力升级吧，据说多传照片可以帮助快速升级";
//			errCode = EgmServiceCode.TRANSACTION_CHAT_NO_AVATAR;
//			err = "他要看头像再聊天，快上传头像吧";
			mLoadingListView.onLoadingComplete();
			if(errCode == EgmServiceCode.TRANSACTION_CHAT_ANOTHER_IS_BLOCKED){//对方被加黑
				showIsBlockedDialog(EgmConstants.Cannot_Chat_Type.BLOCK);
				return ;
			}else if(errCode == EgmServiceCode.TRANSACTION_CHAT_RECEIVE_SIDE_FROZEN){//对方被冻结
				showIsBlockedDialog(EgmConstants.Cannot_Chat_Type.DONGJIE);
				return ;
			}else if(errCode == EgmServiceCode.TRANSACTION_CHAT_NO_AVATAR || 
					errCode == EgmServiceCode.TRANSACTION_CHAT_AVATAR_CHECKING ||
					errCode == EgmServiceCode.TRANSACTION_CHAT_LOW_LEVEL) {
				showForbidenChatDialog(errCode,err);
				return;
			}
			ToastUtil.showToast(getActivity(), err);
			getLoaderManager().restartLoader(mLoaderId,null,FragmentPrivateSession.this);
		}

		@Override
		public void onDelMsgSucess(int transactionId, int code) {
//			ToastUtil.showToast(getActivity(),R.string.delete_suc);
//			delMsg(transactionId,mMsgMap.get(transactionId));
			getLoaderManager().restartLoader(mLoaderId,null,FragmentPrivateSession.this);
		}

		@Override
		public void onDelMsgError(int transactionId, int errCode, String err) {
			ToastUtil.showToast(getActivity(),err);
		}
		
		@Override
		public void onDeletePicSucess(int transactionId, int code) {
			
		}
		
		@Override
		public void onDelFireMsgError(int transactionId, int errCode, String err) {
			
		}

		@Override
		public void onSetMsgReadSucess(int transactionId, int code) {
//			LastMsgDBManager.setUnReadNumZero(mAnotherId);
		}

		@Override
		public void onSetMsgReadError(int transactionId, int errCode, String err) {
		}

		@Override
		public void onPushMsgArrived(int transactionId, List<ChatItemInfo> obj) {
			if(obj == null || obj.size() <= 0){
				return ;
			}
			
			List<MessageInfo> messageInfoList = new ArrayList<MessageInfo>();
			for(ChatItemInfo info : obj){
				if(info.message.type == EgmProtocolConstants.MSG_TYPE.MSG_TYPE_SYS) { // 系统消息不入库
					messageInfoList.add(info.message);
					continue;
				}
				if(info.anotherUserInfo.uid == mAnotherId){
					EgmService.getInstance().doSetMsgRead(mAnotherId,String.valueOf(info.message.msgId));
					messageInfoList.add(info.message);
				}
				if(info.message.usercp != 0){
					intimacy = intimacy + info.message.usercp ;
//					mTxtInm.setText(String.format(getString(R.string.inm_num),intimacy));
					if(mCustomActionBar != null){
		                mCustomActionBar.setSubTitle(String.format(getString(R.string.inm_num),intimacy));
		            }
				}
			}
			
			FragmentPrivateSession.this.getLoaderManager().restartLoader(mLoaderId,null,FragmentPrivateSession.this);
			
			processDialogPopup(messageInfoList, MessageList_From.From_Pull);
		}
		
		@Override
		public void onSendMsgSucess(int transactionId, SendMsgResult obj) {
			if(obj != null){
				if(!TextUtils.isEmpty(obj.thanks)){
					intimacy = intimacy + obj.intimacy ;
//					mTxtInm.setText(String.format(getString(R.string.inm_num),intimacy));
					if(mCustomActionBar != null){
                        mCustomActionBar.setSubTitle(String.format(getString(R.string.inm_num),intimacy));
                    }
					showThanks(obj.thanks,obj.usercp,obj.intimacy);
					
					if (EgmPrefHelper.getGiftsPicOn(getActivity(),
							ManagerAccount.getInstance().getCurrentId()))
						MediaPlayerSystemTone.instance(getActivity()).playWelecomTone("date_gift.mp3");

					//豪气值改变
					LoopBack lp = new LoopBack();
					lp.mType = EgmConstants.LOOPBACK_TYPE.usercp_change;
					lp.mData = obj.usercp ;
					EgmService.getInstance().doLoopBack(lp);
					
					if (obj.messageInfo!=null && !TextUtils.isEmpty(obj.messageInfo.animat)) {
						priorityQueue.offer(new DialogInfo(DialogType.Snow.ordinal(), obj.messageInfo.animat));
					}
				}
			}
		}

		@Override
		public void onSendMsgError(int transactionId, int errCode, String err) {
			if(errCode == EgmServiceCode.TRANSACTION_COMMON_BALANCE_NOT_ENOUGHT){
				showChargeDialog();
			}else{
				if(errCode != EgmServiceCode.TRANSACTION_CHAT_KEYWORDS_BLOCKED){
					ToastUtil.showToast(getActivity(),err);
				}
			}
		}

		@Override
		public void onLoopBack(int transactionId, LoopBack obj) {
			if(obj != null){
				switch(obj.mType){
					case EgmConstants.LOOPBACK_TYPE.chat_send_pri_pic:
						//发送图片
						PictureInfo pictureInfo = (PictureInfo)(obj.mData) ;
						sendPrivateImage(
								String.valueOf(pictureInfo.id),
								pictureInfo.smallPicUrl,
								pictureInfo.picUrl);
						break;
					case EgmConstants.LOOPBACK_TYPE.send_gift:
						//从聊天界面礼物键盘发送礼物
						GiftInfo giftInfo = (GiftInfo)obj.mData;
						setSendGift(giftInfo.name,giftInfo.id);
						break;
					case EgmConstants.LOOPBACK_TYPE.msg_delete:
						//删除消息
						MessageInfo info = (MessageInfo)obj.mData ;
						switch(info.status){
							case EgmConstants.Sending_State.SEND_FAIL:
//								delMsg(-1,info.msgId);
								EgmService.getInstance().doDelMsg(info,mChatUserInfo);
//								ToastUtil.showToast(getActivity(),R.string.delete_suc);
								break;
							case EgmConstants.Sending_State.SEND_SUCCESS:
//								mMsgMap.put(EgmService.getInstance().doDelMsg(info,mChatUserInfo),info.msgId);
								EgmService.getInstance().doDelMsg(info,mChatUserInfo);
								break;
						}
						break;
					case EgmConstants.LOOPBACK_TYPE.msg_fire_delete:
						MessageInfo info2 = (MessageInfo) obj.mData ;
						EgmService.getInstance().doDelFireMsg(info2);
						break;
					case EgmConstants.LOOPBACK_TYPE.msg_resend:
						//重新发送消息
						MessageInfo msgInfo = (MessageInfo)obj.mData ;
						msgInfo.status = EgmConstants.Sending_State.SENDING;
						int type = msgInfo.type ;
						long toUserId = msgInfo.receiver;
						String filePath = null ;
						String msgContent = null ;
						String privacyId = null ;
						String giftId = null ;
						String duration = null ;
						MsgAttach attach = null ;
						String faceId = null;
						switch(msgInfo.type){
							case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_TEXT:
								msgContent = msgInfo.msgContent;
								break;
							case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_AUDIO:
								attach = MsgAttach.toMsgAttach(msgInfo.attach);
								filePath = attach.audioPath;
								duration = String.valueOf(msgInfo.duration) ;
								break;
							case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_VIDEO:
								attach = MsgAttach.toMsgAttach(msgInfo.attach);
								filePath = attach.videoPath;
								duration = String.valueOf(msgInfo.duration);
								break;
							case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_LOCAL_PIC:
								filePath = msgInfo.mediaUrl ;
								break;
							case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_PRIVATE_PIC:
								privacyId = String.valueOf(msgInfo.extraId);
								break;
							case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_GIFT:
								giftId = String.valueOf(msgInfo.extraId);
								break;
							case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_FACE:
								faceId = msgInfo.getFaceId();
								break;
						}
						
						MsgDBManager.updataMsgState(msgInfo);
						LastMsgDBManager.handleNewMsg(msgInfo);
						FragmentPrivateSession.this.getLoaderManager().restartLoader(mLoaderId,null,FragmentPrivateSession.this);
	
						sendMsg(msgInfo, 
								type, 
								toUserId, 
								filePath, 
								msgContent, 
								privacyId, 
								giftId, 
								duration,
								faceId);
						break;
					case EgmConstants.LOOPBACK_TYPE.change_audiostrem:
						if(!isSystemUser()){
							if(EgmPrefHelper.getReceiverModeOn(getActivity())){
								changeTitle(true);
							} else{
								changeTitle(false);
							}
						}
						break;
				}
			
			}
		}
		//2014-07-21 by echo_chen 处理黑名单，当前聊天人被加入黑名单后关闭聊天窗口 
		@Override
        public void onBlockSucess(int transactionId, int code,long uid) {
		    if(mAnotherId == uid){
		        getActivity().finish();
		    }
        }
		//2014-12-01 by echo_chen 当因为等级低禁止聊天时，成功上传私照后关闭窗口 
		@Override
		public void onUploadPicSucess(int transactionId, PictureInfo obj) {
			if(mForbidenDialog != null && mForbidenDialog.isShowing()){
				mForbidenDialog.dismiss();
			}
			clickBack();
		}
		//2014-12-01 by echo_chen 当因为没有头像禁止聊天时，成功上传头像后弹框提示
		@Override
		public void onModifyProfileSucess(int transactionId, PortraitInfo obj) {
            if(mHomeComeinTipHelper != null){
				if(mUploadAvatarDialog != null && mUploadAvatarDialog.isShowing()){
					mUploadAvatarDialog.dismiss();
				}
				mHomeComeinTipHelper = null;
				showForbidenChatDialog(-1, null);
            }
		}
	};
	
	// bug fix #141128  by gzlichangjie 金币不足时，增加充值入口
	/**
	 * 充值
	 */
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
	
	private AlertDialog mShowIsBlocked ;
	private void showIsBlockedDialog(int type){
		if(mShowIsBlocked == null){
			String title = null ;
			switch(ManagerAccount.getInstance().getCurrentGender()){
				case EgmConstants.SexType.Female:
					if(type == EgmConstants.Cannot_Chat_Type.BLOCK){
						title = getActivity().getResources().getString(R.string.block_cannot_chat_tip_man);
					}else if(type == EgmConstants.Cannot_Chat_Type.DONGJIE){
						title = getActivity().getResources().getString(R.string.dongjie_cannot_chat_tip_man);
					}
					break;
				case EgmConstants.SexType.Male:
					if(type == EgmConstants.Cannot_Chat_Type.BLOCK){
						title = getActivity().getResources().getString(R.string.block_cannot_chat_tip_girl);
					}else if(type == EgmConstants.Cannot_Chat_Type.DONGJIE){
						title = getActivity().getResources().getString(R.string.dongjie_cannot_chat_tip_girl);
					}
					break;
			}
			mShowIsBlocked = EgmUtil.createEgmMenuDialog(
					getActivity(), 
					title, 
					new CharSequence[]{getActivity().getResources().getString(R.string.confirm)}, 
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mShowIsBlocked.dismiss();
							getActivity().finish();
						}
					});
		}
		mShowIsBlocked.setCanceledOnTouchOutside(false);
		
		//点击back键返回，两种实现方式
		//方式一：
		/*mShowIsBlocked.setOnCancelListener(new OnCancelListener(){
			@Override
			public void onCancel(DialogInterface dialog) {
				getActivity().finish();
			}
		});*/
		//方式二：
		mShowIsBlocked.setOnKeyListener(new OnKeyListener(){
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_BACK){
					mShowIsBlocked.dismiss();
					getActivity().finish();
				}
				return false;
			}
		});
		mShowIsBlocked.show();
	}
	/**
	 * 女性因为男性设置限制没头像及低级别不能与他聊天
	 */
	private AlertDialog mForbidenDialog;
	private Dialog mUploadAvatarDialog;
	private SelectAvatarHelper mAvatarHelper;
	private HomeComeinTipHelper mHomeComeinTipHelper;
	private void showForbidenChatDialog(int code,String error){
		switch(code){
		case EgmServiceCode.TRANSACTION_CHAT_NO_AVATAR:{
			if(mAvatarHelper == null){
				int min;
		        if(mSex == EgmConstants.SexType.Female){
		            min = EgmConstants.SIZE_MIN_AVATAR_FEMALE;
		        }
		        else{
		            min = EgmConstants.SIZE_MIN_AVATAR_MALE;
		        }
			  mAvatarHelper = new SelectAvatarHelper(FragmentPrivateSession.this, EgmConstants.SIZE_MAX_AVATAR, min, true);
			}
			if(mHomeComeinTipHelper == null){
				mHomeComeinTipHelper = new HomeComeinTipHelper(FragmentPrivateSession.this, null, null, mAvatarHelper);
			}
			mUploadAvatarDialog = mHomeComeinTipHelper.showUploadAvatarTipChat(getString(R.string.rec_tip_reselect_avatar), error, new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(mUploadAvatarDialog != null && mUploadAvatarDialog.isShowing()){
						mUploadAvatarDialog.dismiss();
					}
					clickBack();
				}
			});
		}
			break;
		case EgmServiceCode.TRANSACTION_CHAT_LOW_LEVEL:{
			String[] items = { getString(R.string.chat_upload_pic_menu),getString(R.string.back) };
			mForbidenDialog = EgmUtil.createEgmContentMenuDialog(getActivity(), null, error,items,new OnClickListener() {
				@Override
				public void onClick(View v) {
					int tag = (Integer) v.getTag();
					switch (tag) {
					case 0:
						UserInfo user = new UserInfo();
						user.uid = ManagerAccount.getInstance().getCurrentId();//取私照只需要id
						ActivityLevelTable.startActivityForResult(FragmentPrivateSession.this,
								ActivityLevelTable.FRAGMENT_PRIVATE_PHOTO,UserInfo.toJsonString(user));
					
						break;
					case 1:
					if(mForbidenDialog != null && mForbidenDialog.isShowing()){
						mForbidenDialog.dismiss();
					}
					clickBack();
						break;
					default:
						break;
					}
				}
			},false);
			mForbidenDialog.setCancelable(false);
			mForbidenDialog.setCanceledOnTouchOutside(false);
			mForbidenDialog.show();
		}
			break;
		case EgmServiceCode.TRANSACTION_CHAT_AVATAR_CHECKING:{
			
			View.OnClickListener listener = new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					int which = (Integer) v.getTag();
					
					if (which == DialogInterface.BUTTON_POSITIVE) {
						clickBack();
					}
				}
			};
	    	
	    	AlertDialog dialog = EgmUtil.createEgmBtnDialog(context, null, 
	    			error, null,  
	    			context.getString(R.string.back), listener, true);
	    	dialog.setCancelable(false);
	    	dialog.show();
		}
			
			break;
		case -1:{//上传头像成功后的本地提示
			String title = getString(R.string.rec_upload_picture_result2);
			String content = getString(R.string.chat_upload_avatar_sucess);
	    	String[] items = {getString(R.string.back) };
			mForbidenDialog = EgmUtil.createEgmContentMenuDialog(getActivity(), title, content,items,new OnClickListener() {
				@Override
				public void onClick(View v) {
					int tag = (Integer) v.getTag();
					switch (tag) {
					case 0:
					if(mForbidenDialog != null && mForbidenDialog.isShowing()){
						mForbidenDialog.dismiss();
					}
					clickBack();
						break;
					default:
						break;
					}
				}
			},false);
			mForbidenDialog.setCancelable(false);
			mForbidenDialog.setCanceledOnTouchOutside(false);
			mForbidenDialog.show();
			break;
		}
		}
		
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
				if(mThanksDialog != null){
					mThanksDialog = null ;
				}
				showShareDialog();
			}
		},3000);
	}
	
	private boolean isFirst = true;
    @Override
    public void onStart() {
    	super.onStart();
    	if(isFirst) {
    		isFirst = false;
    	} else {
    		showShareDialog();
    	}
    }
	
	private void showShareDialog() {
		LevelChangeStatusBean status = LevelChangeStatusBean.getInstance();
		long uid = ManagerAccount.getInstance().getCurrentId();
		if(status.getUid()!=uid && status.getType() == LevelChangeType.Male_Level_Up) {
			new ShareDialog().showLevel(FragmentPrivateSession.this, status.getType(), status.getOldLevel(), status.getNewLevel());
			status.clear();
		} else {
			showDialogInPriorityQueue();
		}
	}
	
	private AlertDialog mThanksDialog ;
	private AlertDialog showThanksDialog(String thanks ,int userCp,int intimacy){
		if(mThanksDialog == null){
			LinearLayout layout = (LinearLayout) getActivity().getLayoutInflater().inflate(
					R.layout.view_send_gift_thanks,null);
			TextView txt_thanks = (TextView)layout.findViewById(R.id.thanks);
			TextView txt_usercp = (TextView)layout.findViewById(R.id.usercp);
			TextView txt_intimacy = (TextView)layout.findViewById(R.id.intimacy);
			txt_thanks.setText(thanks);
			
			StringBuilder sb = new StringBuilder();
			sb.append(getActivity().getResources().getString(R.string.rich_index_a)).append(" +").append(userCp);
			txt_usercp.setText(sb.toString());
			txt_intimacy.setText(getActivity().getResources().getString(R.string.intimacy_index_a)+" +"+intimacy);
			mThanksDialog = new AlertDialog.Builder(getActivity()).setView(layout).create();
		}
		mThanksDialog.show();
		return mThanksDialog ;
	}
	
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new MsgListLoader(getActivity(),0,pageNo,mAnotherId,mMyId);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		if (notNeedRefresh) {
			notNeedRefresh = false;
			return;
		}
		mLoadingListView.onRefreshComplete();
		mLoadingListView.onLoadingComplete();
		try {
            mAdapter.swapCursor(cursor);
            if(pageNo != 0){
            	mLoadingListView.getRefreshableView().setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
            	mLoadingListView.getRefreshableView().setSelection(EgmConstants.CHAT_LIST_PAGE_NUM);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		try {
            mAdapter.swapCursor(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public boolean dispatchTouchEvent(MotionEvent ev){
		if(!isEventWithinView(ev,mBottomLayout)){
			if (mRenderEmotView != null) {
				if(mRenderEmotView.mRecordingView.mIsRecording) {
					// 正在录音的时候，不隐藏
				} else if(mRenderEmotView.mGiftTipShowing) {
					//如果送礼物提示层showing，用户点击屏幕后，渐隐该提示层
					mRenderEmotView.hideManGiftTip();
				}else {
					if(!isEventWithinView(ev, mCustomActionBar.getmLeftTv())) {
						mRenderEmotView.hideAll();
					} else {
						mRenderEmotView.immControl(false);
					}
				}
			}
		}
		if (doubleClickIsShowing) {
			// 如果现实了双击录音指引，触摸隐藏
			doubleClickGuideLayout.setVisibility(View.INVISIBLE);
			doubleClickIsShowing = false;
			
			boolean b = EgmPrefHelper.getIsShownRecordingInlastPrivateSession(getActivity(), mMyId);
			if (b) {
				doubleClickGuideLayout.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (mRenderEmotView != null) {
							mRenderEmotView.showRecordingLayout(false);
						}
					}
				}, 500);
			}
		}

		return true;
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
//		MemoryDataCenter.getInstance().remove(MemoryDataCenter.CURRENT_CHAT_UID);
		MemoryDataCenter.getInstance().remove(MemoryDataCenter.CURRENT_CHAT_OTHER_PROFILE);
		MemoryDataCenter.getInstance().remove(MemoryDataCenter.CURRENT_CAHT_OTHER_NICK);
		
		EgmService.getInstance().removeListener(mCallBack);
		MediaPlayerWrapper.getInstance().removeAllMediaListener();
//		MediaPlayerWrapper.getInstance().doUnbindService(EngagementApp.getAppInstance());
		
		if (mAdapter != null) {
			mAdapter.onDestroy();
		}
		
		if(mShowIsBlocked != null){
			mShowIsBlocked = null ;
		}
		
		if(mThanksDialog != null){
			mThanksDialog = null ;
		}
		
		if(mChatUserInfo != null){
			mChatUserInfo = null ;
		}
		
		 if(mPopupWindow != null && mPopupWindow.isShowing()){
             mPopupWindow.dismiss();
         }
         mPopupWindow = null;
		
		mUploadPictureHelper.removeCallback();
//		if(MediaPlayerWrapper.getInstance() != null){
//	    		MediaPlayerWrapper.getInstance().stop();
//		}
	}
	
	
	// 从Activity中转发过来的Back键处理。
	public boolean onBackKeyDown() {
		if(mIsTalkSkillAnimating) {
			return true;
		} else if (mTalkSkillLayout!=null && mTalkSkillLayout.getVisibility()==View.VISIBLE) {
			hideTalkSkillLayout();
			return true;
		}
		return false;
	}

	
	private FragmentPrivateSession getFragment() {
		return this;
	}

	/**
	 * 显示录音Dialog界面
	 */
	private void showDoubleClickRecordDialog() {

		if (!hasDialogShowing) { // 弹出录音对话框,为了防止点击太快出现多个，必须等到当前Dialog消失才能出现下一个
			hasDialogShowing = true;
			audioRecordDialog = new DoubleClickRecordDialog(context);
			audioRecordDialog.setFragmentContext(getFragment());
			audioRecordDialog.setCanceledOnTouchOutside(false);
			audioRecordDialog.show();
			audioRecordDialog.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					// TODO Auto-generated method stub
					hasDialogShowing = false;
				}
			});
		}
	}

	public boolean isFromUserInfo() {
		return isFromUserInfo;
	}

	public void setFromUserInfo(boolean isFromUserInfo) {
		this.isFromUserInfo = isFromUserInfo;
	}
	//处理易信提醒
	private void handleYixinTip(boolean isYixinFriend){
        if(isYixinFriend){
            if(EgmPrefHelper.getShouldYixinFriendTip(getActivity(),mMyId)){
                if(mCustomActionBar != null){
                    showYixinFriendTipsPopupWindow(mCustomActionBar.getCustomView());
                }
                showYixinIcon(false);
            } else {
                showYixinIcon(true);
            }
            
        } else {
            showYixinIcon(false);
        }
	    
	}
	//显示易信好友提醒
	private void showYixinFriendTipsPopupWindow(final View anchor) {
        final View tipsLayout = View.inflate(getActivity(), R.layout.view_chat_yixin_friend_tip, null);

        TextView tipText = (TextView) tipsLayout.findViewById(R.id.yixin_friend_tips);
        // 添加监听，提示条的文字被点击后，会跳去易信
        tipText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startYixin();
            }
        });
        // 添加监听，如果点击的是提示条关闭（最小化）操作，则提示条消失，聊天界面右上角出现“易信”logo
        TextView tipClose = (TextView) tipsLayout.findViewById(R.id.tips_close);
        tipClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPopupWindow != null && mPopupWindow.isShowing()){
                    mPopupWindow.dismiss();
                }
                showYixinIcon(true);
                EgmPrefHelper.putShouldYixinFriendTip(getActivity(),mMyId);
            }
        });
        
        // 创建PopupWindow
        mPopupWindow = new PopupWindow(tipsLayout, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable());
//        mPopupWindow.setTouchable(false);
        mPopupWindow.setOutsideTouchable(false);
        mPopupWindow.setFocusable(false);
        mPopupWindow.setClippingEnabled(false);

        
        // 设置PopupWindow的位置
        mPopupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
        final int anchorLocation[] = new int[2];
        anchor.getLocationInWindow(anchorLocation);
        mPopupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, anchorLocation[0], anchorLocation[1] + anchor.getHeight() + getResources().getDimensionPixelSize(R.dimen.info_margin_8dp));
    }
	
	public void setActivityPrivateSession(
			ActivityPrivateSession activityPrivateSession) {
		this.activityPrivateSession = activityPrivateSession;
	}

	private void startYixin(){
	    if(mYixinHelper == null){
            mYixinHelper = new YixinHelper(getActivity(), YixinHelper.TYPE_FROM_CHAT_ADD_FRIEND);
        }
        mYixinHelper.onIsFriend();
	}

	private void showYixinIcon(boolean show){
	    if(mYixinIcon != null){
	        if(show){
	            mYixinIcon.setVisibility(View.VISIBLE);
	        } else {
	            mYixinIcon.setVisibility(View.GONE); 
	        }
        }
	}

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.yixin_tip:
                startYixin();
                break;
            case R.id.chat_feedback:
                Fragment fragFeedback = new FragmentFeedBack();
                mFragmentManager.beginTransaction().replace(R.id.activity_private_session_id, fragFeedback)
                        .addToBackStack(null).commit();
                break;
            case R.id.chat_help_center:
                Fragment fragHelp = new FragmentHelpCenter();
                mFragmentManager.beginTransaction().replace(R.id.activity_private_session_id, fragHelp)
                        .addToBackStack(null).commit();
                break;
            case R.id.who_invite_me:
                Fragment fragWhoInviteMe = new FragmentWhoInviteMe();
                mFragmentManager.beginTransaction().replace(R.id.activity_private_session_id, fragWhoInviteMe)
                .addToBackStack(null).commit();
                break;
            default:
                break;
        }
    }

    
    
    /******************  优先级弹框 开始  ****************************/
    
    enum MessageList_From {
		From_Pull,
		From_Push,
	}
    
	private void processDialogPopup(final List<MessageInfo> messageInfoList, final MessageList_From from) { 
		new Thread(new Runnable() {
			@Override
			public void run() {
				
				boolean hasReceiveFireMsg = EgmPrefHelper.getSessionHasReceiveFireFlag(getActivity(), mMyId);
				boolean hasFireScreenShotMsg = false;
				
				long lastAnimatGiftId = 0;
				String animat = null;
				
				List<Long> giftIdList = new ArrayList<Long>();
				for (MessageInfo info : messageInfoList) {
					if(info.type != EgmProtocolConstants.MSG_TYPE.MSG_TYPE_SYS){ // 收到的非系统消息
						
						if (info.type == EgmProtocolConstants.MSG_TYPE.MSG_TYPE_GIFT) {
							giftIdList.add(info.extraId);
							
							if (!TextUtils.isEmpty(info.animat)) {
								if (lastAnimatGiftId == 0) {
									animat = info.animat;
									lastAnimatGiftId = info.extraId;
								} else {
									GiftInfo lastGiftInfo = GiftInfoManager.getGiftInfoById((int)lastAnimatGiftId);
									GiftInfo currentGiftInfo = GiftInfoManager.getGiftInfoById((int)info.extraId);
									if (currentGiftInfo.price > lastGiftInfo.price) {
										animat = info.animat;
										lastAnimatGiftId = info.extraId;
									}
								}
							}
						}
						
						// 判断是否第一次收到阅后即焚消息
						if (!hasReceiveFireMsg) {
							if (info.isFireMsg()) {
								hasReceiveFireMsg = true;
								priorityQueue.offer(new DialogInfo(DialogType.First_Receive_Fire_Message.ordinal(), null));
							}
						}
					} else { // 收到的系统消息
						
						// 判断是否阅后即焚截屏系统消息
						if (!hasFireScreenShotMsg) {
							MsgExtra extra = MsgExtra.toMsgExtra(info.extra.toString());
							if (extra != null &&
									extra.sysMsgType == EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_FIRE_SCREEN_SHOT_NOTICE) {
								hasFireScreenShotMsg = true;
								priorityQueue.offer(new DialogInfo(DialogType.Fire_Screen_Shot.ordinal(), null));
							}
						}
						
						
					}
				}
				
				// 判断是否第一个非新手礼物 First Gift
				boolean needCheckFirstGift = EgmPrefHelper.getNeedCheckFirstGift(getActivity(), mMyId);
				if (needCheckFirstGift) {
					if (giftIdList!=null && giftIdList.size() > 0) {
						if (mSex == EgmConstants.SexType.Female) {
	                        for (long giftId : giftIdList) {
		                        if (!GiftInfoManager.isSpecialGift((int)giftId)) {
		                            priorityQueue.offer(new DialogInfo(DialogType.First_Gift.ordinal(), null));
		                            break;
		                        }
	                        }
	                    }
					}
				}
				
				// 判断是否有特殊礼物 Special Gift
				List<Long> specialGiftIdList = null;
				if (from == MessageList_From.From_Pull) { // from pull
					specialGiftIdList = MsgDBManager.getGiftListBySenderAndReceiver(mAnotherId, mMyId);
				} else { // from push
					specialGiftIdList = giftIdList;
				}
				if (specialGiftIdList!=null && specialGiftIdList.size()>0) {
					GiftInfo showGiftInfo = null;
					for(long giftId : specialGiftIdList) {
						if (showGiftInfo != null) {
							if (showGiftInfo.id == giftId) {
								continue;
							}
						}
						GiftInfo giftInfo = GiftInfoManager.getGiftInfoById((int) giftId);
						if (giftInfo == null) {
							continue;
						}
						if (giftInfo.share == 1) {
							boolean hasShowAlready = EgmPrefHelper.getUserGiftShareFlag(getActivity(), mMyId, giftId);
							if (!hasShowAlready) {
								if (showGiftInfo == null) {
									showGiftInfo = giftInfo;
								} else {
									if (showGiftInfo.price < giftInfo.price) {
										showGiftInfo = giftInfo;
									}
								}
							}
						}
					}
					if (showGiftInfo != null) {
						priorityQueue.offer(new DialogInfo(DialogType.Special_Gift.ordinal(), showGiftInfo));
					}
				}
				
				if (!TextUtils.isEmpty(animat)) {
					priorityQueue.offer(new DialogInfo(DialogType.Snow.ordinal(), animat));
				}
				
				if (isShowingDialog == false) {
					if (priorityQueue.size() > 0) {
						showDialogInPriorityQueue();
					}
				}
			}
		}).start();
	}
	
	private void showDialogInPriorityQueue() {
		if (priorityQueue.size() == 0) {
			return;
		}
		if(isTopLevelGuideShwoing){//当女性首次语音引导在显示时等弹层消息再显示礼物提示
			mLoadingListView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isTopLevelGuideShwoing = false;
                    showDialogInPriorityQueue();
                }
            }, EgmConstants.AUDIO_GUIDE_SHOW_DURATION);
    	    return;
	    }
		mLoadingListView.post(new Runnable() {
            @Override
            public void run() {
				DialogInfo dialogInfo = priorityQueue.poll();
				if (dialogInfo.getType() == DialogType.First_Gift.ordinal()) {
					if(showFirstGiftDialog()){
						EgmPrefHelper.putNeedCheckFirstGift(getActivity(), mMyId);
						 isShowingDialog = true;
					}
				} else if (dialogInfo.getType() == DialogType.Special_Gift.ordinal()) {
					GiftInfo giftInfo = (GiftInfo)dialogInfo.getInfo();
					ShareDialog shareDialog = new ShareDialog();
					shareDialog.setDelegate(FragmentPrivateSession.this);
					boolean b = shareDialog.showGift(FragmentPrivateSession.this, giftInfo);
					if (b) {
						isShowingDialog = true;
						EgmPrefHelper.putUserGiftShareFlag(FragmentPrivateSession.this.getActivity().getApplicationContext(), mMyId, giftInfo.id);
					}
				} else if (dialogInfo.getType() == DialogType.First_Receive_Fire_Message.ordinal()) {
					EgmPrefHelper.putSessionHasReceiveFireFlag(getActivity(), mMyId);
					showFirstReceiveFireMsgDialog();
					isShowingDialog = true;
				} else if (dialogInfo.getType() == DialogType.First_Open_Fire.ordinal()) {
					EgmPrefHelper.putSessionHasOpenFireFlag(getActivity(), mMyId);
					showFirstOpenFireDialog();
					isShowingDialog = true;
				} else if (dialogInfo.getType() == DialogType.Fire_Screen_Shot.ordinal()) {
					showReceiveScreenShotDialog();
					isShowingDialog = true;
				} else if (dialogInfo.getType() == DialogType.Snow.ordinal()) {
					String animat = (String)dialogInfo.getInfo();
					showSnow(animat);
					isShowingDialog = true;
				}
            }
		});
	}
	
	private AlertDialog mFirstGiftDialog ;
    private boolean showFirstGiftDialog(){
    	    if(getActivity() == null)
    	     	return false;
        LinearLayout layout = (LinearLayout)getActivity().getLayoutInflater().inflate(
                R.layout.view_firstgift_dialog, null);
        TextView tx_close = (TextView)layout.findViewById(R.id.btn_close);
        tx_close.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mFirstGiftDialog != null){   // 页面被关闭后再显示对话框，可能已经为空，需保护
                    mFirstGiftDialog.dismiss();
                    mFirstGiftDialog = null;
                }
                isShowingDialog = false;
                showDialogInPriorityQueue();
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        mFirstGiftDialog = builder.show();
        mFirstGiftDialog.setCanceledOnTouchOutside(false);
        mFirstGiftDialog.getWindow().setLayout( 
                getResources().getDimensionPixelSize(R.dimen.chat_firstgift_dialog_width), 
                android.view.WindowManager.LayoutParams.WRAP_CONTENT); 
        mFirstGiftDialog.getWindow().setContentView(layout);
        return true;
    }
    
    private AlertDialog screenShotDialog;
	private void showReceiveScreenShotDialog() {
		screenShotDialog = EgmUtil.createEgmBtnDialog(getActivity(), 
				null, 
				getResources().getString(R.string.receive_screen_shot_message, mChatUserInfo.nick),
        		null, 
                getResources().getString(R.string.I_know),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (screenShotDialog !=null && screenShotDialog.isShowing()) {
                        	screenShotDialog.dismiss();
                        	screenShotDialog = null;
                        }
                        isShowingDialog = false;
                        showDialogInPriorityQueue();
                    }
                });
		screenShotDialog.setCancelable(true);
		screenShotDialog.setCanceledOnTouchOutside(true);
		screenShotDialog.show();
	}
	
	@Override
    public void shareDialogDismiss() {
		isShowingDialog = false;
    	showDialogInPriorityQueue();
    }
	
	
	public void showSnow(String animat) {
		if (!TextUtils.isEmpty(animat)) {
			snowView.snow(animat);
		}
	}
	
    
	/******************  优先级弹框 结束  ****************************/
    
    
    
    
	/******************  阅后即焚 开始  ****************************/
	private void fireSwitch(boolean isOperateByUser) {
		if (isOpenFire) {
			fireSwitchIV.setTag(Integer.valueOf(1));
			fireSwitchIV.setImageResource(R.drawable.btn_pgchat_fire_open);
			EgmPrefHelper.putSessionFireFlag(getActivity(), mMyId, mAnotherId, true);
			
			bgIS.setImageResource(R.drawable.bg_pgchat_fire);
			
			boolean b = EgmPrefHelper.getSessionHasOpenFireFlag(getActivity(), mMyId);
			if (!b) {
				priorityQueue.offer(new DialogInfo(DialogType.First_Open_Fire.ordinal(), null));
				if (!isShowingDialog) {
					showDialogInPriorityQueue();
				}
			} else {
				if (isOperateByUser) {
					Toast.makeText(getActivity(), R.string.open_fire, Toast.LENGTH_SHORT).show();
				}
			}
		} else {
			fireSwitchIV.setTag(Integer.valueOf(0));
			fireSwitchIV.setImageResource(R.drawable.btn_pgchat_fire_close);
			EgmPrefHelper.putSessionFireFlag(getActivity(), mMyId, mAnotherId, false);
			
			bgIS.setImageResource(R.drawable.bg_message_all);
			
			if (isOperateByUser) {
				Toast.makeText(getActivity(), R.string.close_fire, Toast.LENGTH_SHORT).show();
			}
		} 
		
		if (isOperateByUser) {
			mAdapter.setOpenFire(isOpenFire);
			mAdapter.notifyDataSetChanged();
		}
	}
	
	private AlertDialog firstReceiveFireMsgDialog;
	private void showFirstReceiveFireMsgDialog() {
		firstReceiveFireMsgDialog = EgmUtil.createEgmBtnWithImageDialog(getActivity(), 
				getResources().getString(R.string.receive_fire), 
				getResources().getString(R.string.receive_fire_message), 
        		R.drawable.icon_layer_fire,
        		null, 
                getResources().getString(R.string.I_know),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (firstReceiveFireMsgDialog !=null && firstReceiveFireMsgDialog.isShowing()) {
                        	firstReceiveFireMsgDialog.dismiss();
                        	firstReceiveFireMsgDialog = null;
                        }
                        isShowingDialog = false;
                        showDialogInPriorityQueue();
                    }
                });
		firstReceiveFireMsgDialog.setCancelable(true);
		firstReceiveFireMsgDialog.setCanceledOnTouchOutside(true);
		firstReceiveFireMsgDialog.show();
	}
	
	private AlertDialog firstOpenFireDialog;
	private void showFirstOpenFireDialog() {
		firstOpenFireDialog = EgmUtil.createEgmBtnWithImageDialog(getActivity(), 
				getResources().getString(R.string.open_fire), 
				getResources().getString(R.string.open_fire_message), 
        		R.drawable.icon_layer_fire,
        		null, 
                getResources().getString(R.string.I_know),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (firstOpenFireDialog !=null && firstOpenFireDialog.isShowing()) {
                        	firstOpenFireDialog.dismiss();
                        	firstOpenFireDialog = null;
                        }
                        isShowingDialog = false;
                        showDialogInPriorityQueue();
                    }
                });
		firstOpenFireDialog.setCancelable(true);
		firstOpenFireDialog.setCanceledOnTouchOutside(true);
		firstOpenFireDialog.show();
	}
	
	public void returnFromFireAudioOrVideo() {
		mAdapter.returnFromFireAudioOrVideo();
	}
	
	@Override
	public View makeView() {
		ImageView iv = new ImageView(this.getActivity());
		iv.setScaleType(ImageView.ScaleType.FIT_XY);
        iv.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        return iv;
    }
	
	/******************  阅后即焚 结束  ****************************/
	
	
	
	/******************  聊天技 开始  ****************************/
	
	// 初始化聊天技
	public void initTaklSkillLayout(final ChatSkillInfo[] chatSkills) {
		if (chatSkills==null || chatSkills.length==0) {
			return;
		}
		mCustomActionBar.setRightVisibility(View.VISIBLE);
		
		LayoutInflater inflater = this.getActivity().getLayoutInflater();
		mTalkSkillLayout = (RelativeLayout) inflater.inflate(R.layout.view_chatskill_layout, null, false);
		mTalkSkillRl.addView(mTalkSkillLayout);
		
		mTalkSkillBg = (RelativeLayout) mTalkSkillLayout.findViewById(R.id.talk_skill_bg);
		mTalkSkillContiner = (LinearLayout) mTalkSkillLayout.findViewById(R.id.talk_skill_container);
		LinearLayout itemContainer = (LinearLayout) mTalkSkillLayout.findViewById(R.id.talk_skill_inner_container);
		
		mTalkSkillBg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hideTalkSkillLayout();
			}
		});
		
		mTalkSkillContiner.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
		
		LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		
		int row = (chatSkills.length % 3 == 0) ? chatSkills.length / 3 : chatSkills.length / 3 + 1;
		for (int i=0; i<row; i++) {
			LinearLayout ll = new LinearLayout(this.getActivity());
			ll.setLayoutParams(lp1);
			
			int column = (chatSkills.length > (i + 1) * 3) ? 3 : (chatSkills.length - i * 3);
			for (int j=0; j<column; j++) {
				LinearLayout item = (LinearLayout) inflater.inflate(R.layout.view_chatskill_item_layout, null);
				TextView btn = (TextView) item.findViewById(R.id.chat_skill_item_btn);
				btn.setLayoutParams(lp2);
				
				int index = i * 3 + j;
				btn.setTag(chatSkills[index].id);
				btn.setText(chatSkills[index].name);
				btn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						hideTalkSkillLayout();
						int skillid = (Integer) v.getTag();
						setSendTalkSkill(skillid);
					}
				});
				
				ll.addView(item);
			}
			
			itemContainer.addView(ll);
		}
		
	}
	
	// 隐藏聊天技
	public void hideTalkSkillLayout() {
		if (mIsTalkSkillAnimating) {
			return;
		}
		if (mTalkSkillLayout != null) {
			mIsTalkSkillAnimating = true;
			
			Animation slideOutToTop = AnimationUtils.loadAnimation(this.getActivity(), R.anim.slide_out_to_top);
	  		slideOutToTop.setDuration(500);
	  		slideOutToTop.setFillBefore(true);
	  		mTalkSkillContiner.startAnimation(slideOutToTop);
			
			Animation tvAlpha1to0 = AnimationUtils.loadAnimation(this.getActivity(), R.anim.alpha_1_to_0);
	        tvAlpha1to0.setDuration(300);
	        tvAlpha1to0.setStartOffset(500);
	        tvAlpha1to0.setFillBefore(true);
	        tvAlpha1to0.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}
				@Override
				public void onAnimationRepeat(Animation animation) {
				}
				@Override
				public void onAnimationEnd(Animation animation) {
					mTalkSkillLayout.setVisibility(View.GONE);
					
					mIsTalkSkillAnimating = false;
				}
			});
	        mTalkSkillBg.startAnimation(tvAlpha1to0);
		}
	}

	// 显示聊天技
	public void showTalkSkillLayout() {
		if (mIsTalkSkillAnimating) {
			return;
		}
		if (mTalkSkillLayout != null) {
			mIsTalkSkillAnimating = true;
			
			mTalkSkillLayout.setVisibility(View.VISIBLE);

			final Animation alpha0tp1 = AnimationUtils.loadAnimation(this.getActivity(), R.anim.alpha_0_to_1);
	    	alpha0tp1.setDuration(300);
	    	mTalkSkillBg.startAnimation(alpha0tp1);
	    	
	    	Animation slideInFromTop = AnimationUtils.loadAnimation(this.getActivity(), R.anim.slide_in_from_top);
			slideInFromTop.setInterpolator(new OvershootInterpolator());
	    	slideInFromTop.setDuration(500);
	    	slideInFromTop.setStartOffset(300);
	    	slideInFromTop.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}
				@Override
				public void onAnimationRepeat(Animation animation) {
				}
				@Override
				public void onAnimationEnd(Animation animation) {
					mIsTalkSkillAnimating = false;
				}
			});
	    	mTalkSkillContiner.startAnimation(slideInFromTop);
		}
	}

	// 发送聊天技
	public void setSendTalkSkill(int skillid) {
		if (mRenderEmotView != null) {
			String txt = TopicDataManager.getInstance().getTaklSkillById(skillid);
			if (TextUtils.isEmpty(txt)) {
				String array[] = getActivity().getResources().getStringArray(R.array.rec_chatskill_default_array);
				if (array!=null && array.length>0) {
					Random random = new Random(System.currentTimeMillis());
					int index = random.nextInt(array.length);
					if (index>=0 && index<array.length) {
						txt = array[index];
					}
				}
			}
			mRenderEmotView.setSendTalkSkill(txt);
		}
	}
	
	
		
	/******************  聊天技 结束  ****************************/
}
