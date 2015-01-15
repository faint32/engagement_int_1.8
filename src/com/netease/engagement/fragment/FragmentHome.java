package com.netease.engagement.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityChatSkill;
import com.netease.engagement.activity.ActivityHome;
import com.netease.engagement.activity.ActivityMoneyAccount;
import com.netease.engagement.activity.ActivityYuanfen;
import com.netease.engagement.adapter.HomeComeinTipHelper;
import com.netease.engagement.adapter.HomePageFragmentAdapter;
import com.netease.engagement.adapter.SelectAvatarHelper;
import com.netease.engagement.adapter.UploadPictureHelper;
import com.netease.engagement.adapter.UploadPictureHelper.IUploadPicture;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.app.EngagementApp;
import com.netease.engagement.pushMsg.NotificationBarMgr;
import com.netease.engagement.util.LevelChangeStatusBean;
import com.netease.engagement.util.LevelChangeStatusBean.LevelChangeType;
import com.netease.engagement.view.AbsTabNavigationBar.ITabDoubleTapListener;
import com.netease.engagement.view.AbsTabNavigationBar.ITabReselectedListener;
import com.netease.engagement.view.AbsTabNavigationBar.ITabSelectedListener;
import com.netease.engagement.view.CustomViewPager;
import com.netease.engagement.view.HomeTabNavigationBar;
import com.netease.engagement.view.HomeTabView;
import com.netease.engagement.view.IFragment;
import com.netease.engagement.view.OneByOneMenuBar;
import com.netease.engagement.view.ShareDialog;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.db.manager.ManagerAccount.Account;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.LoginUserInfo;
import com.netease.service.protocol.meta.RecommendListInfo;


/**
 * 主界面，包含推荐、发现、聊天和我四个Fragment。
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class FragmentHome extends FragmentBase{
    public static final int TAB_INDEX_RECOMMEND = 0;
    public static final int TAB_INDEX_RANK = 1;
    public static final int TAB_INDEX_CHAT = 2;
    public static final int TAB_INDEX_MYSELF = 3;
    
    private final String KEY_TAB_INDEX = "key_home_tab_index";
    private final long A_DAY = 60 * 60 * 24 * 1000;
    
    private FragmentActivity mContext;
    private CustomViewPager mViewPager;
    private HomePageFragmentAdapter fragmentAdapter ;
    /** Tab导航栏 */
    private HomeTabNavigationBar mTabBar;
    /** Tab导航栏盖板  */
    public View mTabBarCover;
    public View mTabBarCoverClickTarget;
    
    /** 快捷工具面板 */
    public OneByOneMenuBar mShortcutPanel;
    /** 快捷工具面板开关 */
    private ImageView mShortcutSwitcher;
    /** 快捷工具面板打开后的整个界面遮罩 */
    private View mLayoutShade;
    /** 聊天tab */
    public HomeTabView mChatTab;
    /** 个人中心tab */
    public HomeTabView mMySelfTab;
    
    private UploadPictureHelper mUploadPictureHelper;
    private SelectAvatarHelper mAvatarHelper;
    private HomeComeinTipHelper mHomeComeinTipHelper;
    
    private RotateAnimation mCloseAnim, mOpenAnim;
    private Animation mLayoutZoomInAnim, mLayoutZoomOutAnim;
    
    private int mGetUserTid;
    /** 碰缘分快捷按钮 */
    private TextView mShortcutYuanfen;
    private int mCurrentTab = EgmConstants.INDEX_RECOMMEND;
    
    /** 标记是否从注册界面进入该界面 */
    private boolean mIsFromRegister = false;
    private boolean mBIsInit = false;
    
    /** 标记用户头像状态*/
    private int mPortraitStatus = -1;
	private boolean haveShownConmeiForOnceFlag;
    private View coverView;
    
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mContext = this.getActivity();
        
        if(ManagerAccount.getInstance().getCurrentAccount() == null) {
            return;
        }
        
        // 恢复状态
        if(savedInstanceState != null){
            mCurrentTab = savedInstanceState.getInt(KEY_TAB_INDEX);
        }
        
        mUploadPictureHelper = new UploadPictureHelper(this, new IUploadPicture(){
            @Override
            public void onStartUpload() {
                showWatting(null, getString(R.string.common_tip_is_updating), false);
            }
            
            @Override
            public void onFinishUpload() {
                stopWaiting();
            }
        });
        
        int min;
        if(ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Female){
            min = EgmConstants.SIZE_MIN_AVATAR_FEMALE;
        }
        else{
            min = EgmConstants.SIZE_MIN_AVATAR_MALE;
        }
        mAvatarHelper = new SelectAvatarHelper(this, EgmConstants.SIZE_MAX_AVATAR, min, true);
        
        mHomeComeinTipHelper = new HomeComeinTipHelper(this, 
        		ManagerAccount.getInstance().getCurrentAccount(), 
        		mUploadPictureHelper, mAvatarHelper);
        mHomeComeinTipHelper.registerCallback();
        EgmService.getInstance().addListener(mEgmCallBack);
        initAnimation();

		if (mIsFromRegister) {
			 mHomeComeinTipHelper.showRegisterComeinTip();
			// 标记是否是注册或者第一次登陆 
			haveShownConmeiForOnceFlag = true;
			EgmPrefHelper.putUpdateUserInfoTime(mContext,System.currentTimeMillis());
			// 存储时间
		} else {
			// 标记重启应用
			haveShownConmeiForOnceFlag=false;
		}


    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	
    	if (mUploadPictureHelper != null) {
    		mUploadPictureHelper.onSaveInstanceState(outState);
    	}
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActivityHome home = (ActivityHome)getActivity();
        if(home != null){
            home.setHomeFragment(this);
        }
    }
    
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        
//        outState.putInt(KEY_TAB_INDEX, mCurrentTab);
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_home_layout, container, false);
        initView(view);
        
        // 如果不推迟执行，就会导致推荐页的onPageSelected执行的时候Context是空的，导致标题栏无法被初始化
        view.postDelayed(new Runnable(){
            @Override
            public void run() {
                mTabBar.setCurrentTab(mCurrentTab);
            }
        }, 400);
        
        if (savedInstanceState != null) {
	        if (mUploadPictureHelper != null) {
	    		mUploadPictureHelper.onRestoreId(savedInstanceState);
	    	}
        }
        
        coverView = view.findViewById(R.id.cover);
        
        return view;
    }
    
    private void initAnimation(){
        mCloseAnim = new RotateAnimation(405, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mCloseAnim.setDuration(600);
        mCloseAnim.setFillAfter(true);
        mOpenAnim = new RotateAnimation(0, 405, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mOpenAnim.setDuration(600);
        mOpenAnim.setFillAfter(true);
        
        mLayoutZoomInAnim = AnimationUtils.loadAnimation(mContext, R.anim.home_zoom_in);
        mLayoutZoomOutAnim = AnimationUtils.loadAnimation(mContext, R.anim.home_zoom_out);
    }
    
    @Override
    public void onResume(){
        super.onResume();
        
        if(!ManagerAccount.getInstance().isMale()){
            setYuanfenShortcutState(FragmentYuanfen.mIsYuanfenOpen);//FragmentYuanfen里会改变开关状态
        }
        
        long now = System.currentTimeMillis();
        // 每次进app都会自动登录（如果没有退出app而是退到后台再打开就不会自动登录，这里就是处理这种情况的）。
        // 在登录获取用户信息的transaction里保存了时间
        long last = EgmPrefHelper.getUpdateUserInfoTime(mContext);
        
		// 首次登陆
		if (!haveShownConmeiForOnceFlag) {
			haveShownConmeiForOnceFlag = true;
			doGetUserInfo();
		} else if (now - last > A_DAY) { // 已经有一天没有更新用户信息了，那就去更新
			
			doGetUserInfo();
		// 推荐刷新的开启与关闭 
		}
        if(mCurrentTab == TAB_INDEX_CHAT){
            NotificationBarMgr.getInstance(EngagementApp.getAppInstance()).cancelPushChat();
        }
    }
    
    private boolean isFirst = true;
    @Override
    public void onStart() {
    	super.onStart();
    	if(isFirst) {
    		isFirst = false;
    	} else {
    		LevelChangeStatusBean status = LevelChangeStatusBean.getInstance();
    		long uid = ManagerAccount.getInstance().getCurrentId();
    		
    		if(status.getUid() != uid) {
    			status.clear();
    		} else {
	    		if(status.getType() == LevelChangeType.Male_Level_Down
	    				|| status.getType() == LevelChangeType.Male_Level_Up_1
	    				|| status.getType() == LevelChangeType.Female_Level_Up) {
	    			new ShareDialog().showLevel(this, status.getType(), status.getOldLevel(), status.getNewLevel());
	    			status.clear();
	    		}
    		}
    	}
    }
    
    @Override
    public void onDestroy(){
        super.onDestroy();
        
        if(ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Female){
            EgmService.getInstance().removeListener(mEgmCallBack);
        }
        
        mHomeComeinTipHelper.removeCallback();
    }

    public void setIsFromRegister(boolean is){
        mIsFromRegister = is;
    }
    
    public void setPortraitStatus(int status) {
    	mPortraitStatus = status;
	}
    
    public void setCurrentTab(int index){
        mCurrentTab = index;
        
        if(mTabBar != null){
            mTabBar.setCurrentTab(index);
            if(mCurrentTab == TAB_INDEX_CHAT){
                NotificationBarMgr.getInstance(EngagementApp.getAppInstance()).cancelPushChat();
            }
        }
    }
    
    /** 获取用户信息 */
    private void doGetUserInfo(){
    	Account mAccount = ManagerAccount.getInstance().getCurrentAccount();
        mGetUserTid = EgmService.getInstance().doLoginGetUserInfo(mAccount.mUserName, 
                mAccount.mPassword, mAccount.mToken, mAccount.mUserType, 
                null, null, null, null, null);
    }
    
    public int mViewPagerScrollState = 0;
    
    private void initView(View view){
        fragmentAdapter = new HomePageFragmentAdapter(mContext.getSupportFragmentManager());
        fragmentAdapter.setFragmentHome(this);
        mViewPager = (CustomViewPager)view.findViewById(R.id.home_content_page);
        mViewPager.setAdapter(fragmentAdapter);
        mViewPager.setAllowedScrolling(false);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setOnPageChangeListener(new OnPageChangeListener(){
			@Override
			public void onPageScrollStateChanged(int arg0) {
				mViewPagerScrollState = arg0;
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			@Override
			public void onPageSelected(int arg0) {
			    if(mBIsInit){
			        setCurrentTab(arg0);
			    } else {//处理页面后台被杀恢复，延迟传递，
			        mBIsInit = true;
			        final int index = arg0;
			        new Handler().postDelayed(new Runnable() {
                        
                        @Override
                        public void run() {
                            setCurrentTab(index);
                        }
                    }, 100);
			    }
			}
        });
        
        initTabBar(view);
        
        // 快捷工具
        mShortcutPanel = (OneByOneMenuBar)view.findViewById(R.id.home_shortcur_panel);
        mShortcutPanel.setVisibility(View.INVISIBLE);
        
        TextView shortcutPhoto = (TextView)mShortcutPanel.findViewById(R.id.home_shortcur_photo);
        shortcutPhoto.setOnClickListener(mClickShortcut);
        
        mShortcutYuanfen = (TextView)mShortcutPanel.findViewById(R.id.home_shortcur_yuanfen);
        mShortcutYuanfen.setOnClickListener(mClickShortcut);
        
        TextView shortcutMe = (TextView)mShortcutPanel.findViewById(R.id.home_shortcur_talk);
        shortcutMe.setOnClickListener(mClickShortcut);
        
        TextView shortcutCash = (TextView)mShortcutPanel.findViewById(R.id.home_shortcur_cash);
        shortcutCash.setOnClickListener(mClickShortcut);
        
        mLayoutShade = view.findViewById(R.id.home_layout_shade);
        mLayoutShade.setOnClickListener(mClickSwitcher);    // 点击快捷工具面板以外的区域，把面板收起来
    }
    
    private void initTabBar(View view){
        mTabBar = (HomeTabNavigationBar)view.findViewById(R.id.home_tab_bar);
        mTabBarCover = view.findViewById(R.id.home_tab_bar_cover);
        mTabBarCover.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mTabBarCoverClickTarget != null) {
					mTabBarCoverClickTarget.performClick();
				}
			}
		});
        
        mTabBar.addTab(TAB_INDEX_RECOMMEND, R.string.view_home_tabbar_title_recommend, R.drawable.icon_home_tab_recommend_selector);
        mTabBar.addTab(TAB_INDEX_RANK, R.string.view_home_tabbar_title_discover, R.drawable.icon_home_tab_rank_selector);
        
        if(!ManagerAccount.getInstance().isMale()){
            View switcher = LayoutInflater.from(mContext).inflate(R.layout.item_home_tab_shortcut_switcher, null, false);
            mShortcutSwitcher = (ImageView)switcher.findViewById(R.id.home_tab_switcher);
            switcher.setOnClickListener(mClickSwitcher);
            mTabBar.addShortcutSwitcher(switcher);
        }
        
        mChatTab = mTabBar.addTab(TAB_INDEX_CHAT, R.string.view_home_tabbar_title_chat, R.drawable.icon_home_tab_talk_selector);
        mMySelfTab = mTabBar.addTab(TAB_INDEX_MYSELF, R.string.view_home_tabbar_title_myself, R.drawable.icon_home_tab_me_selector);
        
        mTabBar.setFocusable(true);
        mTabBar.setFocusableInTouchMode(true);
        mTabBar.setTabSelectedListener(mTabSelectedListener);
        mTabBar.setTabReselectedListener(mTabReselectedListener);
        mTabBar.setTabDoubleTapListener(mTabDoubleTapListener);
    }
    
    /** 点击Tab */
    private final ITabSelectedListener mTabSelectedListener = new ITabSelectedListener(){
        @Override
        public void onTabSelected(int index, View tab) {
            mViewPager.setCurrentItem(index);
            Fragment fragment = getFragmentByIndex(index) ;
            if(fragment != null && fragment instanceof IFragment){
                ((IFragment)fragment).onPageSelected();
            }
            if (mShortcutPanel.isHiding) {
                return;
            } else if (mShortcutPanel.isShowing) {
                hideShortcutPanel();
            } else if (ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Female
                    && mShortcutPanel.getVisibility() == View.VISIBLE) { // 隐藏快捷面板
                hideShortcutPanel();
            }
     
        }
    };
    
    private ITabReselectedListener mTabReselectedListener = new ITabReselectedListener(){
        @Override
        public void onTabReselected(int index, View tabView) {
            Fragment fragment = getFragmentByIndex(index) ;
            if(fragment != null && fragment instanceof IFragment){
                ((IFragment)fragment).onPageReSelected();
            }
            if (mShortcutPanel.isHiding) {
                return;
            } else if (mShortcutPanel.isShowing) {
                hideShortcutPanel();
            } else if (ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Female
                    && mShortcutPanel.getVisibility() == View.VISIBLE) { // 隐藏快捷面板
                hideShortcutPanel();
            } 
        }
    };
    
    private ITabDoubleTapListener mTabDoubleTapListener = new ITabDoubleTapListener() {
		@Override
		public void onTabDoubleTap(int index) {
			Fragment fragment = getFragmentByIndex(index) ;
			if(fragment != null && fragment instanceof IFragment){
                ((IFragment)fragment).onTabDoubleTap();
            }
            if (mShortcutPanel.isHiding) {
                return;
            } else if (mShortcutPanel.isShowing) {
                hideShortcutPanel();
            } else if (ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Female
                    && mShortcutPanel.getVisibility() == View.VISIBLE) { // 隐藏快捷面板
                hideShortcutPanel();
            }     
		}
	};
    
    private Fragment getFragmentByIndex(int index) {
        Fragment fragment = null;
        if (null != mViewPager
                && null != mContext.getSupportFragmentManager()) {
            fragment = (Fragment) fragmentAdapter.instantiateItem(mViewPager, index);
        }
        return fragment;
    }
    
    /** 点击快捷工具面板开关 */
    private final View.OnClickListener mClickSwitcher = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
 
            if (mShortcutPanel.isShowing || mShortcutPanel.isHiding) {
                return;
            } else {

                // 控制面板显示和切换开关状态
                if (mShortcutPanel.getVisibility() == View.VISIBLE) {
                    hideShortcutPanel();
                } else {
                    showShortcutPanel();
                }}
        }
    };
    
    public CustomViewPager getViewPager(){
    	return mViewPager ;
    }
    
    private void showShortcutPanel(){
    	mShortcutSwitcher.setBackgroundResource(R.drawable.icon_purplebar_add_prs);
        mShortcutSwitcher.startAnimation(mOpenAnim);
        mViewPager.startAnimation(mLayoutZoomInAnim);
        mShortcutPanel.show();
        mLayoutShade.setVisibility(View.VISIBLE);
    }
    
    public void hideShortcutPanel(){
        mShortcutSwitcher.startAnimation(mCloseAnim);
        mViewPager.startAnimation(mLayoutZoomOutAnim);
        mShortcutPanel.hide();
        mLayoutShade.setVisibility(View.GONE);
        mShortcutSwitcher.setBackgroundResource(R.drawable.icon_purplebar_add);
    }
    
    /** 碰缘分按钮状态 */
    @SuppressLint("NewApi") 
    public void setYuanfenShortcutState(boolean isOpen){
        if(isOpen){
            mShortcutYuanfen.setText(R.string.rec_shortcut_yuanfen_open);
            mShortcutYuanfen.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.btn_home_shortcut_yuanfen_open_bg_selector, 0, 0);
            mShortcutYuanfen.setTextColor(getResources().getColor(R.color.white));
        }
        else{
            mShortcutYuanfen.setText(R.string.rec_shortcut_yuanfen);
            mShortcutYuanfen.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.btn_home_shortcut_yuanfen_bg_selector, 0, 0);
            mShortcutYuanfen.setTextColor(getResources().getColorStateList(R.drawable.color_home_shortcut_text_selector));
        }
    }
    
    /** 点击快捷工具的监听 */
    private final View.OnClickListener mClickShortcut = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            hideShortcutPanel();
            
            switch(v.getId()){
                case R.id.home_shortcur_photo:  // 传照片
                    mUploadPictureHelper.showUploadUpPicEntrance();
                    break;
                    
                case R.id.home_shortcur_yuanfen:    // 碰缘分
                    ActivityYuanfen.startActivity(mContext, false);
                    break;
                    
//                case R.id.home_shortcur_talk:         // 看看我
//                    ActivityUserPage.startActivityFromShortcut(mContext, mAccount.mUserId, String.valueOf(mAccount.mSex));
//                    break;
                
                case R.id.home_shortcur_talk:		// 聊天技
                	ActivityChatSkill.startActivity(mContext);
                	break;
                    
                case R.id.home_shortcur_cash:       // 兑现
                    ActivityMoneyAccount.startActivityFromShortcut(mContext);
                    break;
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == EgmConstants.REQUEST_SELECT_PICTURE_AVATAR
				|| requestCode == EgmConstants.REQUEST_CAPTURE_PHOTO_AVATAR
				|| requestCode == EgmConstants.REQUEST_CROP_IMAGE
				||requestCode==EgmConstants.REQUEST_SELECT_POSITION) {
            mHomeComeinTipHelper.onActivityResult(requestCode, resultCode, data);
            
    	}
    	else{
            mUploadPictureHelper.onActivityResult(requestCode, resultCode, data);
    	}

    }
    
    private EgmCallBack mEgmCallBack = new EgmCallBack(){
        @Override
        public void onLoginGetUserInfo(int transactionId, LoginUserInfo loginUserInfo){
            if(transactionId != mGetUserTid)
                return;
			// 弹层判断  
            mHomeComeinTipHelper.showComeinTips(loginUserInfo); 
            // 打开推荐弹层权限         
            mHomeComeinTipHelper.openUpdateByRecommend();
        }
    };
    
    /** 所有弹层，该函数可能扩展有用 */
    public void showComeinTips(LoginUserInfo info) {
        mHomeComeinTipHelper.showComeinTips(info);
    }

    /** 升级与头像弹层 */
    public void updateByRecommend(RecommendListInfo info) {
        mHomeComeinTipHelper.updateByRecommend(info);
    }
    
    
    /*** 上传头像时，事件的穿透处理 ----拦截*/
    public void showCoverToIncept() {

        if (coverView != null) {
            coverView.setVisibility(View.VISIBLE);
            coverView.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        }
    }
    /*** 上传头像时，事件的穿透处理 ----打开 */
    public void hideCoverToIncept() {

        if (coverView != null) {
            coverView.setVisibility(View.GONE);
        }
    }

}
