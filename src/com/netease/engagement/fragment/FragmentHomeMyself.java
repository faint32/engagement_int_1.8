package com.netease.engagement.fragment;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.compat.LoadingListView;
import com.handmark.pulltorefresh.library.LoadingAdapterViewBaseWrap.OnLoadingListener;
import com.handmark.pulltorefresh.library.internal.ViewCompat;
import com.netease.common.image.util.ImageUtil;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityChatSkill;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.activity.ActivityHome;
import com.netease.engagement.activity.ActivityImageBrowser;
import com.netease.engagement.activity.ActivityInvite;
import com.netease.engagement.activity.ActivityLevelTable;
import com.netease.engagement.activity.ActivityMoneyAccount;
import com.netease.engagement.activity.ActivityMyShow;
import com.netease.engagement.activity.ActivityPageInfo;
import com.netease.engagement.activity.ActivitySetting;
import com.netease.engagement.activity.ActivityUserPage;
import com.netease.engagement.activity.ActivityUtil;
import com.netease.engagement.activity.ActivityWeb;
import com.netease.engagement.activity.ImageCropActivity;
import com.netease.engagement.adapter.PageListAdapter;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.app.EgmConstants.IsCameraPhotoFlag;
import com.netease.engagement.app.EngagementApp;
import com.netease.engagement.dataMgr.ConfigDataManager;
import com.netease.engagement.dataMgr.GiftInfoManager;
import com.netease.engagement.image.cropimage.ActivityCropImage;
import com.netease.engagement.image.explorer.FileExplorerActivity;
import com.netease.engagement.view.AbsTabView.OnTabSelectListener;
import com.netease.engagement.view.HeadView;
import com.netease.engagement.view.HomeTabView;
import com.netease.engagement.view.IFragment;
import com.netease.engagement.view.MyPageTabView;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.engagement.widget.LoadingImageView;
import com.netease.engagement.widget.SysPortraitPopupWindow;
import com.netease.engagement.widget.UserInfoUtil;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.Utils.TimeFormatUtil;
import com.netease.service.db.EgmDBProviderExport;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.media.MediaPlayerWrapper;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.AudioIntroduce;
import com.netease.service.protocol.meta.ChatItemInfo;
import com.netease.service.protocol.meta.ChatSkillInfo;
import com.netease.service.protocol.meta.LoopBack;
import com.netease.service.protocol.meta.PictureInfo;
import com.netease.service.protocol.meta.PortraitInfo;
import com.netease.service.protocol.meta.SendGiftResult;
import com.netease.service.protocol.meta.SysPortraitInfo;
import com.netease.service.protocol.meta.SysPortraitListResult;
import com.netease.service.protocol.meta.UserInfo;
import com.netease.service.protocol.meta.UserInfoConfig;
import com.netease.service.protocol.meta.UserPrivateData;
import com.netease.service.protocol.meta.VideoIntroduce;


/**
 * 男女个人中心首页
 */
public class FragmentHomeMyself extends FragmentBase implements IFragment{

	private LoadingListView mLoadingListView;
	// 顶部内容
	private LinearLayout mHeaderLayout;
	private View mProfileLayout ;
	private HeadView mProfileView;
	//昵称
	private TextView mTxtNick ;
	/** 皇冠 */
	private ImageView mCrownIv;
	
	//魅力值
	private TextView mTxtCharm ;
	//真人认证
	private TextView mTxtCert;
	//等级
	private LinearLayout mLevelLayout ;
	private TextView mTxtLevel ;
	// 女性语音介绍
	private LinearLayout mAudioLayout ;
	
	   // 女性语音/视频
    private View mAudioVedioLayout ;
    private TextView newTips;
    
	private TextView mAudioAnim;
	private TextView mAudio;
	// 女性语音介绍动画
	private RotateAnimation mRotateAnim;
	
	// 女性tab
	private MyPageTabView mMyPageTabView;
	
	private LinearLayout mScrollLayout ;
	
	private UserPrivateData mUserPrivateData ;
	private UserInfo mUserInfo;
	private UserInfoConfig mUserInfoConfig ;
	
	//page内容
	private PageListAdapter mPageListAdapter;
	private List<String> mTagNames;
	private List<String> mTagContents;
	
	//性别
	private int gender ;
	
	private boolean isUserConfiged = false ;
	private boolean isUserInfoGet = false ;
	
	private int mScreenWidth ;
	//图片列表padding
	private int padding = 2 ;
	
	private CustomActionBar mCustomActionBar ;
	
	
	private int upLoadPicTransactionId ;
	private int mChangeAvatarTid;

	public static FragmentHomeMyself newInstance() {
		FragmentHomeMyself fragment = new FragmentHomeMyself();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EgmService.getInstance().addListener(mCallback);
		MediaPlayerWrapper.getInstance().doBindService(EngagementApp.getAppInstance());
	}
	
	@Override
    public void onResume(){
	    super.onResume();
	    
	    // 从升级vip界面或者充值界面回来，要刷新一下数据。升级vip刷新是为了通知别的界面vip升级，充值刷新是刷新本界面的金币值。
	    if(mIsIntoVip || mIsIntoCharge){    
	        mLoadingListView.reLoad();
	    }
	    
	    // 恢复
	    mIsIntoVip = false;    
	    mIsIntoCharge = false;
	}
	
	private void initTitle(){
		mCustomActionBar = ((ActivityEngagementBase)getActivity()).getCustomActionBar();
		mCustomActionBar.setCustomBarBackground(R.drawable.bg_pgcenter_top);
		
		mCustomActionBar.setLeftVisibility(View.VISIBLE);
		mCustomActionBar.setLeftBackgroundResource(R.drawable.titlebar_c_selector);
		mCustomActionBar.setLeftAction(0,R.string.preview);
		mCustomActionBar.setLeftTitleCenter();
		mCustomActionBar.setLeftTitleColor(getResources().getColor(R.color.white));
		mCustomActionBar.setLeftTitleSize(17);
		mCustomActionBar.setLeftClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				//预览
				if(ManagerAccount.getInstance().getCurrentAccount() != null){
					ActivityUserPage.startActivity(FragmentHomeMyself.this, 
							ManagerAccount.getInstance().getCurrentIdString(), 
							String.valueOf(ManagerAccount.getInstance().getCurrentGender()));
					return ;
				}
			}
		});
		
		mCustomActionBar.setMiddleTitle(R.string.pri_center);
		mCustomActionBar.setMiddleTitleColor(getResources().getColor(R.color.black));
		mCustomActionBar.setMiddleTitleSize(20);
		
		mCustomActionBar.setRightBackgroundResource(R.drawable.titlebar_c_selector);
		mCustomActionBar.setRightAction(-1, R.string.setting);
		mCustomActionBar.setRightTitleColor(getResources().getColor(R.color.white));
		mCustomActionBar.setRightClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				//进入设置界面
			    ActivitySetting.launch(getActivity());
			}
		});
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		mLoadingListView = (LoadingListView) inflater.inflate(R.layout.view_loading_list, null);
		mLoadingListView.customerCover.setBackgroundResource(R.color.my_pull_bg);
		
		//获取账户信息
		if(ManagerAccount.getInstance().getCurrentAccount() != null){
		    gender = ManagerAccount.getInstance().getCurrentGender() ;
		}
		switch(gender){
			case EgmConstants.SexType.Female:
				mHeaderLayout = (LinearLayout) inflater.inflate(R.layout.fragment_pager_header, null);
				break;
			case EgmConstants.SexType.Male:
				mHeaderLayout = (LinearLayout) inflater.inflate(R.layout.fragment_pager_header_man, null);
				break;
		}
		
		//初始化顶部布局
		init(mHeaderLayout);

		mLoadingListView.getRefreshableView().setHeaderDividersEnabled(true);
		mLoadingListView.getRefreshableView().addHeaderView(mHeaderLayout);
		mLoadingListView.setOnItemClickListener(mOnItemClickListener);
		mLoadingListView.setShowIndicator(false);
		
		mLoadingListView.setOnLoadingListener(new OnLoadingListener(){
			@Override
			public void onRefreshing() {
				EgmService.getInstance().doGetPrivateData();
			}
			@Override
			public void onLoading() {
				mUserInfoConfig = ConfigDataManager.getInstance().getUConfigFromData();
				//用户配置信息不存在，重新获取配置信息
				if(mUserInfoConfig == null){
					EgmService.getInstance().doGetUserInfoConfig(null);
				}else{
					isUserConfiged = true ;
				}
				//获取用户数据
				EgmService.getInstance().doGetPrivateData();
			}
			@Override
			public void onLoadingMore() {
			}
		});
		return mLoadingListView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mLoadingListView.load();
		EgmService.getInstance().doGetSysPortraitList();
	}
	
	private void init(View root) {
		mScreenWidth = this.getResources().getDisplayMetrics().widthPixels ;
		padding = EgmUtil.dip2px(getActivity(),padding);
		
		//头像
		mProfileLayout = root.findViewById(R.id.profile_layout);
		mProfileLayout.setOnClickListener(mOnClickListener);
		mProfileLayout.setEnabled(false);
		
		mProfileView = (HeadView) root.findViewById(R.id.profile);
		//192*192
//		mProfileView.setIsUserCenter(true);
		mProfileView.setImageUrl(false,HeadView.PROFILE_SIZE_BIG,null,
				ManagerAccount.getInstance().getCurrentGender(),getResources().getColor(R.color.pri_my_head_border));
		//列表标签
		mTagNames = new ArrayList<String>();
		//列表内容
		mTagContents = new ArrayList<String>();
		// 昵称
		mTxtNick = ((TextView) root.findViewById(R.id.nickname));
		// 魅力值
		mTxtCharm = ((TextView) root.findViewById(R.id.charm));
		mTxtCert = (TextView) root.findViewById(R.id.cert);
		// 当前等级
		mLevelLayout = (LinearLayout)root.findViewById(R.id.level_layout);
		mTxtLevel = ((TextView) root.findViewById(R.id.level));
		mLevelLayout.setOnClickListener(mOnClickListener);
		mLevelLayout.setEnabled(false);
		
		mCrownIv = (ImageView)root.findViewById(R.id.user_page_crown);
		
		mPageListAdapter = new PageListAdapter(getActivity(), mTagNames,mTagContents);
		mLoadingListView.setAdapter(mPageListAdapter);
		
        mAudioVedioLayout = mHeaderLayout.findViewById(R.id.audio_video_introduce_layout);
        
		refreshNewFunctionVisible();
	}
	
	/**
	 * 刷新底部新功能标示
	 */
    public void refreshNewFunctionVisible(){
    	HomeTabView tabView = ((ActivityHome)getActivity()).getMyselfTab() ;
    	if (tabView != null) {
	    	if (ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Female) {
	    		long uid = ManagerAccount.getInstance().getCurrentId();
	        	boolean liaotianji = EgmPrefHelper.getNewFunctionLiaoTianJiFlag(this.getActivity(), uid);
	        	boolean personalPresentation = EgmPrefHelper.getNewFunctionPersonalPresentationFlag(this.getActivity(), uid);
	        	
	        	int visibility = View.GONE;
	        	if (liaotianji || personalPresentation) {
	        		visibility = View.VISIBLE;  
	        	}
	        	
	        	tabView.setNewFunctionLayoutVisibility(visibility);
	    	} else {
	    		tabView.setNewFunctionLayoutVisibility(View.GONE);
	    	}
    	}
	}
	
	/**
	 * 取到数据后刷新界面
	 */
	private void refreshViews(){
		mTagNames.clear();
		mTagContents.clear();
		
		if(mScrollLayout != null){
			mScrollLayout.removeAllViews();
		}
		
 		mUserInfo = mUserPrivateData.userInfo ;
 		
 		mProfileLayout.setEnabled(true);
 		mLevelLayout.setEnabled(true);
 		
		mProfileView.setImageUrl(mUserInfo.isVip,HeadView.PROFILE_SIZE_LARGE,mUserInfo.portraitUrl192,
				mUserInfo.sex,getResources().getColor(R.color.pri_my_head_border));
		
		mTxtNick.setText(mUserInfo.nick);
		
		if(mUserInfo.crownId > 0){    // 有皇冠
			GiftInfoManager.setCrownInfo(mUserInfo.crownId, true, mCrownIv);
			mCrownIv.setVisibility(View.VISIBLE);
		}
		else {
			mCrownIv.setVisibility(View.GONE);
		}
		
		switch (gender) {
			case EgmConstants.SexType.Female:
				// 魅力值
				mTxtCharm.setText(getCharmText());
				mTxtCert.setVisibility(mUserInfo.hasVideoAuth ? View.VISIBLE : View.GONE);
				//显示等级
				mTxtLevel.setText(getLevelStr());
				mTagNames.addAll(Arrays.asList(getActivity().getResources().getStringArray(R.array.main_page_tag_girl)));
				mTagContents.add(String.format(getActivity().getString(R.string.current_income),mUserInfo.balance));
				
				// 聊天技
				if (mUserInfo.chatSkills!=null && mUserInfo.chatSkills.length>0) {
					StringBuilder sb = new StringBuilder();
					for(int i=0; i<mUserInfo.chatSkills.length; i++) {
						ChatSkillInfo info = mUserInfo.chatSkills[i];
						sb.append(info.name).append("、");
					}
					if (sb.length() > 0) {
						sb.deleteCharAt(sb.length()-1);
					}
					mTagContents.add(sb.toString()); 
				} else {
					mTagContents.add("");
				}
				
				if(TextUtils.isEmpty(mUserInfo.introduce)){
					mTagContents.add(getString(R.string.say_sth_about_you));
				}else{
					mTagContents.add(mUserInfo.introduce);
				}
				mTagContents.add(getDetailInfoText());
				
				// tab
				mMyPageTabView = (MyPageTabView) mHeaderLayout.findViewById(R.id.girl_tab);
				mMyPageTabView.clear();
				mMyPageTabView.createTabs(mUserInfo.giftCount,mUserInfo.privatePhotoCount, mUserInfo.photoCount);
				mMyPageTabView.setOnTabSelectListener(mOnTabSelectedListener);
                
				// 女性语音/视频
//                mAudioVedioLayout = mHeaderLayout.findViewById(R.id.audio_video_introduce_layout);
                mAudioVedioLayout.setOnClickListener(mOnClickListener);
                TextView modeTips=(TextView)mAudioVedioLayout.findViewById(R.id.mode_tips);
                TextView modePic=(TextView)mAudioVedioLayout.findViewById(R.id.mode_pic);
                
                int paddingLeft = mAudioVedioLayout.getPaddingLeft();
                int paddingTop = mAudioVedioLayout.getPaddingTop();
                int paddingRight = mAudioVedioLayout.getPaddingRight();
                int paddingBottom = mAudioVedioLayout.getPaddingBottom();
                
                switch (mUserPrivateData.userInfo.introduceType) {
                    case 0://未录制
                        modeTips.setText(getActivity().getResources().getString(R.string.record_my_show));
                        modePic.setVisibility(View.GONE);
                        
                        mAudioVedioLayout.setPadding(paddingRight, paddingTop,
                        		paddingRight, paddingBottom);
                        break;
                    case 1:// 视频
                        modeTips.setText(getActivity().getResources().getString(R.string.video_voice_using_video));
                        modePic.setVisibility(View.VISIBLE);
                        modePic.setBackgroundResource(R.drawable.icon_pgcenter_video);
                        
                        mAudioVedioLayout.setPadding(0, paddingTop,
                        		paddingRight, paddingBottom);
                        break;
                    case 2:// 语音
                        modeTips.setText(getActivity().getResources().getString(R.string.video_voice_using_voice));
                        modePic.setVisibility(View.VISIBLE);
                        modePic.setBackgroundResource(R.drawable.icon_pgcenter_voice);
                        
                        mAudioVedioLayout.setPadding(0, paddingTop,
                        		paddingRight, paddingBottom);
                        break;
                    default:
                        break;
                }
                
                newTips=(TextView)mHeaderLayout.findViewById(R.id.new_tips);
                if (EgmPrefHelper.getNewFunctionPersonalPresentationFlag(getActivity(), mUserInfo.uid)) {
                	newTips.setVisibility(View.VISIBLE);
                }
                
				break;
				
			case EgmConstants.SexType.Male:
				//豪气值
				mTxtCharm.setText(getCharmText());
				//显示等级
				mTxtLevel.setText(getLevelStr());
				//vip
				TextView vipText = (TextView)mHeaderLayout.findViewById(R.id.vip_state);
				if(mUserInfo.isVip && mUserInfo.vipEndTime != 0){
					vipText.setText(String.format(getActivity().getResources().getString(R.string.vip_end), 
							TimeFormatUtil.forYMDDotFormat(mUserInfo.vipEndTime)));
				}else{
					vipText.setText(getString(R.string.vip_service_unused));
				}
				mHeaderLayout.findViewById(R.id.vip_layout).setOnClickListener(mClickUpgradeVip);
				
				TextView chargeTv = (TextView)mHeaderLayout.findViewById(R.id.charge);
				//显示金币余额
				chargeTv.setText(String.format(getActivity().getResources().getString(R.string.coin_balance),
						mUserInfo.coinBalance));
				mHeaderLayout.findViewById(R.id.account_layout).setOnClickListener(mClickCharge);
				
				mTagNames.addAll(Arrays.asList(getActivity().getResources().getStringArray(R.array.main_page_tag_man)));
				
				if(TextUtils.isEmpty(mUserInfo.introduce)){
					mTagContents.add(getString(R.string.say_sth_about_you));
				}else{
					mTagContents.add(mUserInfo.introduce);
				}
				
				mTagContents.add(getDetailInfoText());
				
				mScrollLayout = (LinearLayout)mHeaderLayout.findViewById(R.id.scroll_layout);
				fillImageData(mScrollLayout);
				break;
		}
		mPageListAdapter.notifyDataSetChanged();
	}
	
	/** 标记是否进入了升级vip流程 */
	private boolean mIsIntoVip = false;
	/** 点击升级vip */
	private View.OnClickListener mClickUpgradeVip = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ActivityWeb.startUpgradeVip(getActivity());
//            避免VIP升级回调两次
//            mIsIntoVip = true;
        }
    };
    
    /** 标记是否进入了充值流程 */
    private boolean mIsIntoCharge = false;
    /** 点击充值 */
    private View.OnClickListener mClickCharge = new View.OnClickListener() {
    	@Override
        public void onClick(View v) {
            ActivityWeb.startCoinCharge(getActivity());
            mIsIntoCharge = true;
        }
    };
    
    private String getLevelStr(){
    	String levelStrMan = String.format(getResources().getString(R.string.level_num),mUserInfo.level);
    	return levelStrMan+" "+ mUserInfo.levelName ;
    }
	
	/**
	 * 填充男性公开照片列表
	 */
	private int imageWidth ;
	private LinearLayout.LayoutParams lp ;
	private PictureInfo[] pictures ;
	private void fillImageData(View container){
		imageWidth = (mScreenWidth - EgmUtil.dip2px(getActivity(),45) - EgmUtil.dip2px(getActivity(), 14))/3 ; 
		
		pictures = mUserPrivateData.publicPicList ;
		
		lp = new LinearLayout.LayoutParams(imageWidth,imageWidth);
		for(int i = 0; i< pictures.length + 1; i++){
			if(i == 0){
				final View item = getItemView(null,R.drawable.bg_add_photo_gray);
				item.setId(R.id.button_image_post);
				item.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						upLoadProfile = false ;
						showUploadPubPicDialog();
					}
				});
				((LinearLayout)container).addView(item);
			}else{
				View item = getItemView(pictures[i-1].picUrl,-1);
				((LinearLayout)container).addView(item);
				final int index = i - 1 ;
				item.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						/**
						 * 进入图集查看公开照片
						 */
						ArrayList<PictureInfo> picInfos = new ArrayList<PictureInfo>();
						for(PictureInfo item : pictures){
							picInfos.add(item);
						}
						//显示第i张图片
						ActivityImageBrowser.startActivity(FragmentHomeMyself.this,picInfos,index,false,true);
					}
				});
			}
		}
	}
	
	/**
	 * 获取公开照列表ItemView
	 * @param picUrl
	 * @param drawableId
	 * @return
	 */
	private View getItemView(String picUrl , int drawableId){
		LinearLayout item = new LinearLayout(getActivity());
		item.setPadding(padding, 0, padding, 0);
		LoadingImageView imageview = new LoadingImageView(getActivity());
		imageview.setDefaultResId(R.drawable.icon_photo_loaded_fail);
		imageview.setServerClipSize(imageWidth - padding, imageWidth - padding);
		imageview.setScaleType(ScaleType.CENTER_CROP);
		imageview.setScaleTop(true);
		
		item.setLayoutParams(lp);
		if(picUrl != null){
			imageview.setLoadingImage(picUrl);
		}else if(drawableId > 0){
			ViewCompat.setBackground(imageview,getResources().getDrawable(drawableId));
		}
		item.addView(imageview,new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		return item ;
	}
	
	/**
	 * 女性魅力值/男性豪气值
	 */
	private String getCharmText(){
		switch(gender){
			case EgmConstants.SexType.Female:
				return String.format(getResources().getString(R.string.charm_index), mUserInfo.usercp);
			case EgmConstants.SexType.Male:
				return String.format(getResources().getString(R.string.rich_index), mUserInfo.usercp);
		}
		return String.valueOf(0);
	}
	
	/**
	 * 获取详细信息
	 */
	private String getDetailInfoText(){
		StringBuilder sb = new StringBuilder();
		if(!TextUtils.isEmpty(UserInfoUtil.getHeightText(mUserInfo))){
			sb.append(" , ").append(UserInfoUtil.getHeightText(mUserInfo));
		}
		switch(gender){
			case EgmConstants.SexType.Female:
				if(!TextUtils.isEmpty(UserInfoUtil.getWeightText(mUserInfo))){
					sb.append(" , ").append(UserInfoUtil.getWeightText(mUserInfo));
				}
				if(!TextUtils.isEmpty(UserInfoUtil.getFigureText(mUserInfo,mUserInfoConfig))){
					sb.append(" , ").append(UserInfoUtil.getFigureText(mUserInfo,mUserInfoConfig));
				}
				if(!TextUtils.isEmpty(UserInfoUtil.getFemaleHobbyText(mUserInfo,mUserInfoConfig))){
					sb.append(" , ").append(UserInfoUtil.getFemaleHobbyText(mUserInfo,mUserInfoConfig));
				}
				break;
			case EgmConstants.SexType.Male:
				if(!TextUtils.isEmpty(UserInfoUtil.getIncomeStr(mUserInfo.income,mUserInfoConfig))){
					sb.append(" , ").append(UserInfoUtil.getIncomeStr(mUserInfo.income,mUserInfoConfig));
				}
				if(!TextUtils.isEmpty(UserInfoUtil.getLocation(getActivity(),mUserInfo))){
					sb.append(" , ").append(UserInfoUtil.getLocation(getActivity(),mUserInfo));
				}
				if(!TextUtils.isEmpty(UserInfoUtil.getConstellation(mUserInfo,mUserInfoConfig))){
					sb.append(" , ").append(UserInfoUtil.getConstellation(mUserInfo,mUserInfoConfig));
				}
				if(!TextUtils.isEmpty(UserInfoUtil.getMaleHobbyText(mUserInfo,mUserInfoConfig))){
					sb.append(" , ").append(UserInfoUtil.getMaleHobbyText(mUserInfo,mUserInfoConfig));
				}
				break;
		}
		return sb.length() > 3 ? sb.substring(3,sb.length()) : "";
	}
	
	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
				case R.id.profile_layout:
					/**
					 * 修改头像
					 */
					upLoadProfile = true ;
					showModifyProfileDialog();
					break;
				case R.id.audio_video_introduce_layout:
                    /**
                     * 跳转录音/录视频界面
                     * mUserPrivateData.userInfo.introduceType
                     */
					if(mUserInfo != null && mUserInfo.introduceType == EgmProtocolConstants.Introduce_Type.Introduce_Type_Audio){
						ActivityMyShow.startActivity(getActivity(),false);
					} else{
						ActivityMyShow.startActivity(getActivity(),true);
					}
				    if (newTips!=null && newTips.getVisibility()==View.VISIBLE) {
				    	newTips.setVisibility(View.GONE);
				    	EgmPrefHelper.putNewFunctionPersonalPresentationFlag(getActivity(), mUserInfo.uid);
				    	refreshNewFunctionVisible();
				    }
					break;
				case R.id.level_layout:
					/**
					 * 等级查看表
					 */
					ActivityLevelTable.startActivity(
							FragmentHomeMyself.this,
							ActivityLevelTable.FRAGMENT_LEVEL,
							UserInfo.toJsonString(mUserInfo));
					break;
			}
		}
	};
	
	/**
	 * 修改头像
	 */
	private AlertDialog mModifyProDialog ;
	private String mId = null;
	private SysPortraitInfo[] mSysPortraitInfoList;
	
	private void showModifyProfileDialog(){
		if(mModifyProDialog == null){
			mModifyProDialog = EgmUtil.createEgmMenuDialog(
					getActivity(), 
					getResources().getString(R.string.modify_pic), 
					getResources().getStringArray(gender == EgmConstants.SexType.Male ? R.array.send_pub_pic_male_array : R.array.send_pub_pic_array), 
					new OnClickListener(){
						@Override
						public void onClick(View v) {
							int which = (Integer) v.getTag();
							switch(which){
								case 0:
									/**
									 * 拍照
									 */
							        mId = String.valueOf(System.currentTimeMillis());
							        ActivityUtil.capturePhotoForResult(FragmentHomeMyself.this, 
							        		 EgmDBProviderExport.getUri(EgmDBProviderExport.TYPE_CAMERA, mId), 
											 EgmConstants.REQUEST_CAPTURE_PHOTO);
									break;
								case 1:
									/**
									 * 从相册选择
									 */
									int size = 0 ;
									switch(gender){
										case EgmConstants.SexType.Female:
											size = EgmConstants.SIZE_MIN_PICTURE ;
											break;
										case EgmConstants.SexType.Male:
											size = EgmConstants.SIZE_MIN_AVATAR_MALE ;
											break;
									}
									FileExplorerActivity.startForSelectPicture(
											FragmentHomeMyself.this,
						                    EgmConstants.Photo_Type.TYPE_AVATAR, 
						                    EgmConstants.REQUEST_SELECT_PICTURE, 
						                    EgmConstants.SIZE_MAX_PICTURE,size);
									break;
								case 2:
									SysPortraitPopupWindow mSysPortraitPopupWindow = new SysPortraitPopupWindow(FragmentHomeMyself.this.getActivity(), mSysPortraitInfoList);
									mSysPortraitPopupWindow.showWindow();
									break;
							}
							mModifyProDialog.dismiss();
						}
					});
		}
		mModifyProDialog.show();
	}
	
	private AlertDialog mUploadPubPic ;
	private void showUploadPubPicDialog(){
		if(mUploadPubPic == null){
			String title = getResources().getString(R.string.public_image);
			if (gender == EgmConstants.SexType.Male) {
				title = getResources().getString(R.string.pic);
			}
			
			mUploadPubPic = EgmUtil.createEgmMenuDialog(
					getActivity(), 
					title, 
					getResources().getStringArray(R.array.send_pub_pic_array), 
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							int which = (Integer) v.getTag();
							switch(which){
								case 0:
									/**
									 * 拍照
									 */
							        mId = String.valueOf(System.currentTimeMillis());
							        ActivityUtil.capturePhotoForResult(FragmentHomeMyself.this, 
							        		 EgmDBProviderExport.getUri(EgmDBProviderExport.TYPE_CAMERA, mId), 
											 EgmConstants.REQUEST_CAPTURE_PHOTO);
									break;
								case 1:
									/**
									 * 从相册选择
									 */
									FileExplorerActivity.startForSelectPicture(
											FragmentHomeMyself.this,
						                    EgmConstants.Photo_Type.TYPE_AVATAR, 
						                    EgmConstants.REQUEST_SELECT_PICTURE, 
						                    EgmConstants.SIZE_MAX_PICTURE,
						                    EgmConstants.SIZE_MIN_PICTURE);
									break;
							}
							mUploadPubPic.dismiss();
						}
					});
		}
		mUploadPubPic.show();
	}
	

	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
			if(mLoadingListView.getLoadingState() == LoadingListView.STATE_LOADING){
				return ;
			}
			if(mUserInfo == null){
				return ;
			}
			
			TextView tagNameTxt = (TextView) arg1.findViewById(R.id.tag_name);
			String tagName = tagNameTxt.getText().toString();
			if(TextUtils.isEmpty(tagName)){
				return ;
			}
			if(tagName.equals(getString(R.string.account))){
				/**
				 * 账户
				 */
				ActivityMoneyAccount.startActivity(getActivity());
			}else if(tagName.equals(getString(R.string.rec_chatskill_title))){
				/**
				 * 聊天技
				 */
				ActivityChatSkill.startActivity(FragmentHomeMyself.this);
				if (EgmPrefHelper.getNewFunctionLiaoTianJiFlag(getActivity(), mUserInfo.uid)) {
					EgmPrefHelper.putNewFunctionLiaoTianJiFlag(getActivity(), mUserInfo.uid);
					FragmentHomeMyself.this.mPageListAdapter.notifyDataSetChanged();
					FragmentHomeMyself.this.refreshNewFunctionVisible();
				}
			}else if(tagName.equals(getString(R.string.self_introduce))){
				/**
				 * 自我介绍 
				 */
				ActivityPageInfo.startActivityForResult(
						FragmentHomeMyself.this,
						String.valueOf(ActivityPageInfo.INTRODUCE), 
						mUserInfo);
			}else if(tagName.equals(getString(R.string.detail_info))){
				/**
				 * 详细资料
				 */
				ActivityPageInfo.startActivityForResult(
						FragmentHomeMyself.this,
						String.valueOf(ActivityPageInfo.DETAIL_INFO), 
						mUserInfo);
			}else if(tagName.equals(getString(R.string.charm_strategy))){
				/**
				 * 魅力秘籍
				 */
				ActivityPageInfo.startActivity(FragmentHomeMyself.this,String.valueOf(ActivityPageInfo.CHARM_STRATEGY), mUserInfo);
			}else if(tagName.equals(getString(R.string.invite)) || tagName.equals(getString(R.string.invite_user_female))){
				/**
				 * 邀请朋友 
				 */
			    ActivityInvite.launch(getActivity());
			}
		}
	};

	private OnTabSelectListener mOnTabSelectedListener = new OnTabSelectListener() {
		@Override
		public void onTabSelected(View tabView, int index) {
			switch (index) {
			case 0:
				/**
				 * 礼物
				 */
				ActivityLevelTable.startActivityForResult(FragmentHomeMyself.this,
						ActivityLevelTable.FRAGMENT_GIFT,UserInfo.toJsonString(mUserInfo));
				break;
			case 1:
				/**
				 * 私密照片
				 */
				ActivityLevelTable.startActivityForResult(FragmentHomeMyself.this,
						ActivityLevelTable.FRAGMENT_PRIVATE_PHOTO,UserInfo.toJsonString(mUserInfo));
				break;
			case 2:
				/**
				 * 公开照片 
				 */
				ActivityLevelTable.startActivityForResult(FragmentHomeMyself.this,
						ActivityLevelTable.FRAGMENT_PUBLIC_PHOTO,UserInfo.toJsonString(mUserInfo));
				break;
			}
		}

		@Override
		public void onTabReSelected(View tabView, int index) { 
			switch (index) {
			case 0:
				ActivityLevelTable.startActivityForResult(FragmentHomeMyself.this,
						ActivityLevelTable.FRAGMENT_GIFT,UserInfo.toJsonString(mUserInfo));
				break;
			case 1:
				ActivityLevelTable.startActivityForResult(FragmentHomeMyself.this,
						ActivityLevelTable.FRAGMENT_PRIVATE_PHOTO,UserInfo.toJsonString(mUserInfo));
				break;
			case 2:
				ActivityLevelTable.startActivityForResult(FragmentHomeMyself.this,
						ActivityLevelTable.FRAGMENT_PUBLIC_PHOTO,UserInfo.toJsonString(mUserInfo));
				break;
			}
		}
	};

	private boolean upLoadProfile = false ;
	private String mAvatarPath ;
	private String mCropImagePath ;
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case EgmConstants.REQUEST_SELECT_PICTURE:
				/**
				 * 修改头像
				 */
				if(data == null){
					return ;
				}
				if(upLoadProfile){
					Uri uri = data.getData();        //获得图片的uri  
					int degree = ImageUtil.getRotateDegree(getActivity(), uri);
	                // 获取图片
                    Bitmap bitmap = ImageUtil.getBitmapFromUriLimitSize(getActivity(), uri, EgmConstants.SIZE_MAX_AVATAR);
                    if(bitmap == null){
                        showToast(R.string.reg_tip_avatar_get_error);
                    }
                    else{
                        // 判断图片是否符合尺寸要求，过大的图片进行缩小
                        int min;
                        if(gender == EgmConstants.SexType.Female){
                            min = EgmConstants.SIZE_MIN_AVATAR_FEMALE;
                        }
                        else{
                            min = EgmConstants.SIZE_MIN_AVATAR_MALE;
                        }
                        if(degree > 0){
                            bitmap = ImageUtil.rotateBitmap(bitmap,degree);
                        }
                        Bitmap result = ImageUtil.legitimateImageSize(bitmap, EgmConstants.SIZE_MAX_AVATAR, (min*5/4));
                        if(result == null){ // 过小
                        	showToast(R.string.reg_tip_avatar_too_small);
                        }
                        else{
                        	// 要先存到本地，否则图片过大会失败
                        	mAvatarPath = ImageUtil.getBitmapFilePath(result, EgmConstants.TEMP_PROFILE_NAME);
                        	if(!TextUtils.isEmpty(mAvatarPath)){
                        		Intent intent = ImageCropActivity.actionImageCrop(getActivity(), mAvatarPath, mAvatarPath, EgmConstants.SIZE_MIN_AVATAR_CROPED);
                        		startActivityForResult(intent, EgmConstants.REQUEST_CROP_IMAGE);
//                        		ActivityCropImage.actionGetCropImage(this,
//                        				EgmConstants.REQUEST_CROP_IMAGE, mAvatarPath, false, min);// 图片裁剪	
                        	}
                        }
                    }
				} else{
					String filePath = getAlbumImagePath(data.getData());
					if(!TextUtils.isEmpty(filePath)){
						upLoadPicTransactionId = EgmService.getInstance().upLoadPicture(filePath,EgmConstants.Photo_Type.TYPE_PUBLIC, IsCameraPhotoFlag.OtherPhoto);
						showWatting(null, "上传中...", false);
					}
				}
				break;
			case EgmConstants.REQUEST_CROP_IMAGE:
				/**
				 * 获取裁剪后的头像以及坐标
				 */
				if(data == null){
					return ;
				}
				
				WindowManager.LayoutParams attrs = getActivity().getWindow().getAttributes();
				attrs.flags = WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN;
				getActivity().getWindow().setAttributes(attrs);
				
				Bundle bundle = data.getExtras();
				mCropImagePath = bundle.getString(ActivityCropImage.EXTRA_DATA);
				String coordination = bundle.getString(ActivityCropImage.CROP_COORDINATE);
				
				String[] coor = coordination.split("&");
				if(coor == null || coor.length != 4 || TextUtils.isEmpty(mCropImagePath)){
					ToastUtil.showToast(getActivity(),R.string.pic_crop_error);
					return ;
				}
				mChangeAvatarTid = EgmService.getInstance().doModifyPortrait(mAvatarPath, coor[0], coor[1],coor[2], coor[3]);
				showWatting(null, "上传中...", false);
				break;
			case EgmConstants.REQUEST_CAPTURE_PHOTO:
				/**
				 * 拍照
				 */
				mAvatarPath = EgmUtil.getFilePathByType(EgmUtil.TYPE_CAMERA, mId) ;
				if(upLoadProfile){
					Uri uri = Uri.fromFile(new File(EgmUtil.getFilePathByType(EgmUtil.TYPE_CAMERA, mId)));
					// 判断图片是否符合尺寸要求，过大的图片进行缩小
	                int min;
	                if(gender == EgmConstants.SexType.Female){
	                    min = EgmConstants.SIZE_MIN_AVATAR_FEMALE;
	                }
	                else{
	                    min = EgmConstants.SIZE_MIN_AVATAR_MALE;
	                }
	                
					// 判断图片是否符合尺寸要求，过大的图片进行缩小
	                Bitmap result = ImageUtil.legitimateImageSize(getActivity(), uri, EgmConstants.SIZE_MAX_AVATAR, min);
	                if(result == null){
	                    //Toast.makeText(getActivity(), R.string.reg_tip_avatar_get_error, Toast.LENGTH_SHORT).show();
	                	result = ImageUtil.legitimateImageSize(getActivity(), uri,480, min);
	                }else{
	                	mAvatarPath = ImageUtil.getBitmapFilePath(result, EgmConstants.TEMP_PROFILE_NAME);// 要先存到本地，否则图片过大会失败
	                	Intent intent = ImageCropActivity.actionImageCrop(getActivity(), mAvatarPath, mAvatarPath, EgmConstants.SIZE_MIN_AVATAR_CROPED);
	                	
	                	startActivityForResult(intent, EgmConstants.REQUEST_CROP_IMAGE);
	                // ActivityCropImage.actionGetCropImage(this,EgmConstants.REQUEST_CROP_IMAGE, mAvatarPath, false, min);// 图片裁剪
	                }
				} else{
					String filePath = EgmUtil.getFilePathByType(EgmUtil.TYPE_CAMERA, mId) ;
					if(!TextUtils.isEmpty(filePath)){
						upLoadPicTransactionId = EgmService.getInstance().upLoadPicture(filePath,EgmConstants.Photo_Type.TYPE_PUBLIC, IsCameraPhotoFlag.CameraPhoto);
						showWatting(null, "上传中...", false);
					}
				}
				
				break;
				/**
				 * 刷新列表，传照片单独处理
				 */
			case ActivityLevelTable.FRAGMENT_PUBLIC_PHOTO:
			case ActivityLevelTable.FRAGMENT_PRIVATE_PHOTO:
				EgmService.getInstance().doGetPrivateData();
				break;
				/**
				 * 处理魅力秘籍处的跳转
				 */
//			case ActivityPageInfo.CHARM_STRATEGY:
//				new Handler().postDelayed(new Runnable(){
//					@Override
//					public void run() {
//						CustomViewPager pager = ((ActivityHome)(FragmentHomeMyself.this.getActivity())).getViewPager();
//						if(pager != null){
//							pager.setCurrentItem(0);
//						}
//					}
//				},200);
//				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private String getAlbumImagePath(Uri uri){
		String srcFile = null ;
		if(uri.toString().startsWith("file://")) {
            File file = new File(URI.create(uri.toString()));
            srcFile = file.getPath();
		}else{
			Cursor cursor = getActivity().getContentResolver().query(uri, null,null, null, null);
			cursor.moveToFirst();
			//只有名称为"path"的一列
			srcFile = cursor.getString(0);
			cursor.close();
		}
		return srcFile ;
	}
	
	private EgmCallBack mCallback = new EgmCallBack(){
		@Override
		public void onGetPrivateDataSucess(int transactionId,UserPrivateData obj) {
			mLoadingListView.onLoadingComplete();
			mLoadingListView.onRefreshComplete();
			mUserPrivateData = obj;
			isUserInfoGet = true ;
			if(isUserConfiged && isUserInfoGet){
				refreshViews();
			}
		}
		
		@Override
		public void onGetPrivateDataError(int transactionId, int errCode,String err) {
			ToastUtil.showToast(getActivity(),err);
			mLoadingListView.onLoadingComplete();
			mLoadingListView.onRefreshComplete();
			if(errCode == EgmServiceCode.NETWORK_ERR
					|| errCode == EgmServiceCode.NETWORK_ERR_COMMON){
				mLoadingListView.onNoNetwork();
			}
		}
		
		@Override
		public void onGetUserInfoConfigSucess(int transactionId,UserInfoConfig obj) {
			mUserInfoConfig = obj ;
			isUserConfiged = true ;
			if(isUserConfiged && isUserInfoGet){
				refreshViews();
			}
		}
		
		@Override
		public void onGetUserInfoConfigError(int transactionId, int errCode,String err) {
			mLoadingListView.onLoadingComplete();
		}
		
		@Override
		public void onModifyProfileSucess(int transactionId, PortraitInfo portrait) {
//			Bitmap temp = ImageUtil.getBitmapFromFileLimitSize(mCropImagePath,256);
//			Bitmap profile = ImageUtil.getCircleBitmap(temp);
//			mProfileView.userFace.setImageBitmap(profile);
			stopWaiting();
			if(mChangeAvatarTid == transactionId){
				 ToastUtil.showToast(getActivity(), R.string.upload_pic_suc);
			}
			if(portrait != null && mProfileView != null){
				mUserInfo.portraitUrl192 = portrait.portraitUrl192;
				mUserInfo.portraitUrl = portrait.portraitUrl;
				mProfileView.setImageUrl(mUserInfo.isVip,HeadView.PROFILE_SIZE_LARGE,mUserInfo.portraitUrl192,
						mUserInfo.sex,getResources().getColor(R.color.pri_my_head_border));
			}
//			EgmService.getInstance().doGetPrivateData();
		}
		
		@Override
		public void onModifyProfileError(int transactionId, int errCode,String err) {
		   if(mChangeAvatarTid != transactionId)
	                return;
			stopWaiting();
			ToastUtil.showToast(getActivity(), err);
		}
		
		@Override
		public void onUploadPicSucess(int transactionId, PictureInfo obj) {
			stopWaiting();
			if(obj != null && upLoadPicTransactionId == transactionId){
			    EgmPrefHelper.putUpdatePicTime(getActivity(), java.lang.System.currentTimeMillis());
				ToastUtil.showToast(getActivity(), R.string.upload_pic_suc);
				EgmService.getInstance().doGetPrivateData();
			}
		}
		
		@Override
		public void onUploadPicError(int transactionId, int errCode, String err) {
            // 防止女性多次显示,此处仅提供男性使用 by lishang
            if (ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Male) {
                stopWaiting();
                ToastUtil.showToast(getActivity(), err);
            }
		}
		
		@Override
		public void onDelAudioSucess(int transactionId, Object obj) {
			EgmService.getInstance().doGetPrivateData();
			ToastUtil.showToast(getActivity(),R.string.delete_audio_suc);
		}
		
		@Override
		public void onDelAudioError(int transactionId, int errCode, String err) {
			ToastUtil.showToast(getActivity(),err);
		}
		
		//为了应对其它地方进行了资料编辑，此处要进行刷新
		@Override
		public void onModifyIntrSucess(int transactionId, int code) {
			EgmService.getInstance().doGetPrivateData();
		}

		@Override
		public void onModifyDetailInfoSucess(int transactionId, UserInfo obj) {
			mUserPrivateData.userInfo = obj ;
			refreshViews();
		}

		@Override
		public void onDeletePicSucess(int transactionId, int code) {
			EgmService.getInstance().doGetPrivateData();
		}

		@Override
		public void onUpdateAudioSucess(int transactionId, AudioIntroduce obj) {
			EgmService.getInstance().doGetPrivateData();
		}

		@Override
		public void onApplyWithdraw(int transactionId) {
			EgmService.getInstance().doGetPrivateData();
		}
		
		@Override
		public void onUpdateVideoSucess(int transactionId, VideoIntroduce obj) {
			EgmService.getInstance().doGetPrivateData();
		}
		
		@Override
		public void onSwitchAudioVideo(int transactionId) {
			EgmService.getInstance().doGetPrivateData();
		}
		
		@Override
		public void onPushMsgArrived(int transactionId, List<ChatItemInfo> obj) {
			if(obj == null || obj.size() <= 0){
				return ;
			}
			int addIntimacy = 0 ;
			for(ChatItemInfo info : obj){
				if(info.message.usercp != 0){
					addIntimacy = addIntimacy + info.message.usercp ;
				}
			}
			if(addIntimacy != 0){
				mUserInfo.usercp = mUserInfo.usercp + addIntimacy ;
				mTxtCharm.setText(getCharmText());
			}
		}

		@Override
		public void onLoopBack(int transactionId, LoopBack obj) {
			if(obj != null){
				switch(obj.mType){
					case EgmConstants.LOOPBACK_TYPE.usercp_change:
						mUserInfo.usercp = mUserInfo.usercp + (Integer)obj.mData ;
						mTxtCharm.setText(getCharmText());
						break;
					case EgmConstants.LOOPBACK_TYPE.update_talk_skill:
						EgmService.getInstance().doGetPrivateData();
						break;
				}
			}
		}
		
		/* 男性赠送礼物成功，那么金币值就会发生变化，需要刷新界面 */
	    @Override
        public void onSendGiftSucess(int transactionId, SendGiftResult obj){
	        if(mLoadingListView != null 
	        		&& ManagerAccount.getInstance().getCurrentAccount() != null 
	        		&& ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Male){
	            mLoadingListView.reLoad();
	        }
	    }
	    
		@Override
		public void onGetSysPortraitListSucess(int transactionId, SysPortraitListResult obj) {
			if (obj != null) {
				mSysPortraitInfoList = obj.sysPortraitList;
			}
		}
	};
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		EgmService.getInstance().removeListener(mCallback);
//		MediaPlayerWrapper.getInstance().doUnbindService(getActivity());
	}

	@Override
	public void onPageSelected() {
		initTitle();
	}

	@Override
	public void onPageReSelected() {
	}

	@Override
	public void onTabDoubleTap() {
	}

}
