
package com.netease.engagement.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.AbsListView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.LoadingAdapterViewBaseWrap.OnLoadingListener;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityBindMobile;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.activity.ActivityHome;
import com.netease.engagement.activity.ActivityMultiRankList;
import com.netease.engagement.activity.ActivityPicShowOffForFemale;
import com.netease.engagement.activity.ActivitySearchList;
import com.netease.engagement.activity.ActivityUserPage;
import com.netease.engagement.activity.ActivityWeb;
import com.netease.engagement.adapter.RecommendListAdapter;
import com.netease.engagement.adapter.RecommendListAdapter.FemaleSmallViewHolder;
import com.netease.engagement.adapter.RecommendListAdapter.MaleViewHolder;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.ConfigDataManager;
import com.netease.engagement.view.AutoScrollViewPager;
import com.netease.engagement.view.IFragment;
import com.netease.engagement.view.PagerIndicator;
import com.netease.engagement.view.PullListView;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.engagement.widget.LoadingImageView;
import com.netease.engagement.widget.LoadingImageView.IUiGetImage;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.app.BaseApplication;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.db.manager.ManagerAccount.Account;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.RankListInfoInHome;
import com.netease.service.protocol.meta.RecommendActivityInfo;
import com.netease.service.protocol.meta.RecommendActivityListInfo;
import com.netease.service.protocol.meta.RecommendListInfo;
import com.netease.service.protocol.meta.RecommendUserInfo;
import com.netease.service.protocol.meta.UserInfoConfig;
import com.netease.service.protocol.meta.UserPrivateData;
import com.netease.service.stat.EgmStat;
import com.netease.util.EnctryUtil;
import com.netease.util.PlatformUtil;


/**
 * 主页-推荐Fragment
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class FragmentHomeRecommend extends FragmentBase implements IFragment {
    private ActivityEngagementBase mContext;
    private FragmentHome fragmentHome;
    
    
    private PullListView mListView;

    private Dialog mTipDialog;
    private View mFootView;
    
    private LinearLayout recommendSearchLL;
    private FragmentSearch fragmentSearch;
    
    private RecommendListAdapter mAdapter;
    private UserInfoConfig mUserInfoConfig;
    private int mTid;
    
    public boolean isSearchStatus = false;
    private boolean isAnimationing = false;
    private long mLastRefreshTime = 0;//记录上次上传统计时间
	protected int getPrivateDataTid;

    public FragmentHome getFragmentHome() {
        if(fragmentHome == null){
            ActivityHome home = (ActivityHome)getActivity();
            if(home != null){
                fragmentHome = home.getHomeFragment();
            }
        }
		return fragmentHome;
	}

	public void setFragmentHome(FragmentHome fragmentHome) {
		this.fragmentHome = fragmentHome;
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mContext = (ActivityEngagementBase)getActivity();
        mTipDialog = new Dialog(mContext, R.style.CustomDialog);
        mTipDialog.setCanceledOnTouchOutside(false);
        
        EgmService.getInstance().addListener(mEgmCallback);
        
        ConfigDataManager manager = ConfigDataManager.getInstance();
        mUserInfoConfig = manager.getUConfigFromData();
        
        getCompetitionList();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommend_list_layout, container, false);
        
        
        recommendSearchLL = (LinearLayout)view.findViewById(R.id.recommendSearchLL);
        recommendSearchLL.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CustomActionBar actionBar = mContext.getCustomActionBar();
				actionBar.getmRightTv().performClick();
			}
		});
        
        mListView = (PullListView)view.findViewById(R.id.listview);
        mListView.setShowIndicator(false);
        
        if(ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Male){  // 男性用户看的列表有VIP入口或者榜单入口
            mFootView = getFootView();
            mListView.getRefreshableView().addFooterView(mFootView);
        }
        mListView.getRefreshableView().setDivider(null);
        
        final int sexType;
        if(ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Female){
            sexType = EgmConstants.SexType.Male;    // 女性用户看到的是男性推荐列表
        }
        else{
            sexType = EgmConstants.SexType.Female;    // 男性用户看到的是女性推荐列表
        }
        mAdapter = new RecommendListAdapter(mContext, sexType, new View.OnClickListener() {
            @Override
            public void onClick(View v) {   // 进入到用户详情页
                long uid;
                Object obj = v.getTag();
                int position;
                int size;
                String param1Size=null;
                String alg=null;
                if(obj instanceof MaleViewHolder){
                    uid = ((MaleViewHolder) obj).mUid;
                    position=((MaleViewHolder) obj).position;
                    size=((MaleViewHolder) obj).imgSize;
                    alg=((MaleViewHolder) obj).mAlg;
                }
                else if(obj instanceof FemaleSmallViewHolder){
                    uid = ((FemaleSmallViewHolder) obj).mUid;
                    position=((FemaleSmallViewHolder) obj).position;
                    size=((FemaleSmallViewHolder) obj).imgSize;
                    alg=((FemaleSmallViewHolder) obj).mAlg;
                }
                else{
                    return;
                }
				if (size == EgmConstants.Img_Size.IMG_SIZE_BIG) {
					param1Size = EgmStat.SIZE_BIG;
				} else if (size == EgmConstants.Img_Size.IMG_SIZE_SMALL) {
					param1Size = EgmStat.SIZE_SMALL;
				} else {
				}
				EgmStat.log(EgmStat.LOG_CLICK_MAINPAGE, EgmStat.SCENE_MAINPAGE, uid, position, param1Size,alg);  
                ActivityUserPage.startActivity(FragmentHomeRecommend.this, String.valueOf(uid), String.valueOf(sexType));
            }
        });
        
        mListView.setAdapter(mAdapter);
        mListView.disableLoadingMore();
        mListView.enablePullFromStart();
        mListView.setOnLoadingListener(new OnLoadingListener(){
            @Override
            public void onRefreshing() {    // 下拉刷新
                getRecommendData();
                
				getPrivateDataTid = EgmService.getInstance().doGetUserInfoDetail(ManagerAccount.getInstance().getCurrentId());
            }

            @Override
            public void onLoading() {   // 初始加载
                getRecommendData();
				getPrivateDataTid = EgmService.getInstance().doGetUserInfoDetail(ManagerAccount.getInstance().getCurrentId());
            }

            @Override
            public void onLoadingMore() {}
        });
//        mListView.getRefreshableView().addFooterView(new LinearLayout(getActivity()));
        mListView.load();
        
        
        if(mContext != null){
            CustomActionBar actionBar = mContext.getCustomActionBar();
            setActionBar(actionBar);
        }
        return view;
    }
    
    @Override
    public void onDestroy(){
        super.onDestroy();
        EgmService.getInstance().removeListener(mEgmCallback);
    }

    @Override
    public void onPageSelected() {
        if(mContext != null){
            CustomActionBar actionBar = mContext.getCustomActionBar();
            setActionBar(actionBar);
            long delta = System.currentTimeMillis() - mLastRefreshTime;
            if (delta > EgmConstants.TABSWITCH_REFRESH_INTERVAL) {
              	mLastRefreshTime = System.currentTimeMillis();
	            int mVisibleCount = mListView.getRefreshableView().getFirstVisiblePosition();
	            if(mVisibleCount != 0) {
	                mListView.postDelayed(new Runnable() {
						@Override
						public void run() {
							mListView.getRefreshableView().smoothScrollToPosition(0);
						}
					}, 300);
	                mListView.postDelayed(new Runnable() {
						@Override
						public void run() {
							getRecommendData();
						}
					}, 800);
	            } else {
	            	mListView.postDelayed(new Runnable() {
						@Override
						public void run() {
							getRecommendData();
						}
					}, 300);
	            	
	            }
            }
            
        }
    }

    @Override
    public void onPageReSelected() {
        
    }
    
    @Override
    public void onTabDoubleTap() {
    	if(mListView.getRefreshableView().getFirstVisiblePosition() != 0) {
    		mListView.getRefreshableView().smoothScrollToPosition(0);
    	}
    }
    
    private View getFootView(){
        View footView;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        
        if(ManagerAccount.getInstance().isVip()){    // vip用户，看到的是女神榜入口
            footView = inflater.inflate(R.layout.view_rank_item, null, false);
            LoadingImageView bg = (LoadingImageView)footView.findViewById(R.id.rank_bg);
            bg.setLoadingImage(EgmPrefHelper.getRankPicture3(mContext));
            
            // 公用引起的不居中问题
            View rankTitle=footView.findViewById(R.id.rank_item_title_bg);
            RelativeLayout.LayoutParams mLayoutParams = (android.widget.RelativeLayout.LayoutParams)rankTitle
                    .getLayoutParams();
            mLayoutParams.setMargins(0, 0, 0, 0);
            mLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            
            
            TextView title = (TextView)footView.findViewById(R.id.rank_name);
            TextView subTitle = (TextView)footView.findViewById(R.id.rank_vip_tip);
            title.setText(R.string.rank_name_top_female);
            subTitle.setText(R.string.rank_vip_only_tip);
            footView.setOnClickListener(new View.OnClickListener() {    // 进入女神榜
                @Override
                public void onClick(View v) {
                	
                	ActivityMultiRankList.startActivity(mContext, 
                			EgmConstants.RankID.TOP_FEMALE, 
                			mContext.getString(R.string.rank_name_top_female), 
                			EgmConstants.SexType.Female);
                }
            });
        }
        else{   // 非vip用户，看到但是升级vip入口
            footView = inflater.inflate(R.layout.view_recommend_list_footview, null, false);
            LoadingImageView bg = (LoadingImageView)footView.findViewById(R.id.recommend_vip_bg);
            bg.setDefaultResId(R.drawable.icon_photo_loaded_fail_with_bg);
            bg.setNeedLoadImageErrorCallBack(true);
            bg.setLoadingImage(getVipBgUrl());
            
            View openupVip = footView.findViewById(R.id.rec_vip_btn);
            openupVip.setOnClickListener(new View.OnClickListener() {   // 开通vip
                @Override
                public void onClick(View v) {
                    ActivityWeb.startUpgradeVip(mContext);
                }
            });
        }
        
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, 
                EgmUtil.dip2px(mContext, 240));
        footView.setLayoutParams(lp);
        
        return footView;
    }
    
    private String getVipBgUrl(){
        if(mUserInfoConfig == null || mUserInfoConfig.vipPicUrl == null || mUserInfoConfig.vipPicUrl.length == 0){
            return null;
        }
        
        Random random = new Random();
        int index = random.nextInt(mUserInfoConfig.vipPicUrl.length);
        return mUserInfoConfig.vipPicUrl[index];
    }
    
    
    @SuppressLint("ResourceAsColor") 
    private void setActionBar(CustomActionBar actionBar){
        if(actionBar == null)
            return;
        
        if (isSearchStatus) {
        	actionBar.setMiddleTitle(R.string.search);
        	actionBar.setRightAction(-1, R.string.close);
        } else {
        	if(ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Male){
                actionBar.setMiddleTitle(R.string.rec_recommend_title_male);
            }
            else{
                actionBar.setMiddleTitle(R.string.rec_recommend_title_female);
            }
        	actionBar.setRightAction(-1, R.string.search);
        }
        
        actionBar.setCustomBarBackground(R.drawable.bg_pgrecommendlist_top);
        actionBar.setLeftAction(R.drawable.icon_titlebar_back_selector, "");    // 为了切换tab后让标题居中
        actionBar.setLeftVisibility(View.INVISIBLE);
        actionBar.setRightVisibility(View.VISIBLE);
        actionBar.setRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	if (getFragmentHome().mShortcutPanel.getVisibility() == View.VISIBLE) {
            	    getFragmentHome().hideShortcutPanel();
            		return;
            	}
            	if (!isAnimationing) {
            		isAnimationing = true;
            		openOrCloseSearch();
            	}
            }
        });
        actionBar.setRightBackgroundResource(R.drawable.titlebar_a_selector);
        actionBar.setRightTitleColor(getResources().getColor(R.color.purple_dark));
        actionBar.setBackgroundColor(R.color.transparent);
    }
    
    private void getRecommendData(){

        mTid = EgmService.getInstance().doGetRecommend(true);
        // 超过半天去取活动
        if (isAfterHalfDay(EgmPrefHelper.getPicShowOffDataTime(mContext,
        		ManagerAccount.getInstance().getCurrentId()))) {
            getCompetitionList();
        }
    }
    
    private void getCompetitionList() {
        EgmService.getInstance().doGetCompetitionList();
        EgmPrefHelper.putPicShowOffDataTime(mContext, System.currentTimeMillis(),
        		ManagerAccount.getInstance().getCurrentId());
    }
    /** 绑定手机号 */
    private void showBindMobileTip(final boolean needInviteCode){
        View layout = LayoutInflater.from(mContext).inflate(R.layout.view_dialog_open_yuanfen_tip, null, false);
        TextView title = (TextView)layout.findViewById(R.id.open_yuanfen_title);
        title.setText(R.string.reg_bind_mobile_tip);
        
        TextView btn = (TextView)layout.findViewById(R.id.open_yuanfen_ok);
        btn.setText(R.string.reg_verify);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTipDialog.dismiss();
                ActivityBindMobile.startActivity(mContext, 
                		ManagerAccount.getInstance().getCurrentAccount().mUserName,
                		ManagerAccount.getInstance().getCurrentAccount().mPassword, 
                		ManagerAccount.getInstance().getCurrentAccount().mToken, needInviteCode);
            }
        });
        
        mTipDialog.setCancelable(false);
        mTipDialog.setContentView(layout);
        mTipDialog.show();
    }
    
    private boolean needGoToSearchList = false;
    private int sexType;
    private int ageBegin;
    private int ageEnd;
    private int constellation;
    private int provinceCode;
    private boolean hasPrivatePic;
    private int income;
    public void gotoSearchList(int sexType, int ageBegin, int ageEnd, int constellation, 
            int provinceCode, boolean hasPrivatePic, int income) {
    	needGoToSearchList = true;
    	
    	this.sexType = sexType;
    	this.ageBegin = ageBegin;
    	this.ageEnd = ageEnd;
    	this.constellation = constellation;
    	this.provinceCode = provinceCode;
    	this.hasPrivatePic = hasPrivatePic;
    	this.income = income;
    	
    	openOrCloseSearch();
    }
    
    public void openOrCloseSearch() {
    	
    	final CustomActionBar actionBar = ((ActivityEngagementBase)getActivity()).getCustomActionBar();
    	final TextView rightTextView = actionBar.getmRightTv();
    	
	  	if (isSearchStatus) { // 关闭搜索
	  		
	  		Animation slideOutToTop = AnimationUtils.loadAnimation(mContext, R.anim.slide_out_to_top);
	  		slideOutToTop.setDuration(300);
	  		slideOutToTop.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}
				@Override
				public void onAnimationRepeat(Animation animation) {
				}
				@Override
				public void onAnimationEnd(Animation animation) {
					FragmentTransaction ft = FragmentHomeRecommend.this.getActivity().getSupportFragmentManager().beginTransaction();
			  		ft.remove(fragmentSearch);
			  		ft.commit();
					
					
					Animation alpha1to0 = AnimationUtils.loadAnimation(mContext, R.anim.alpha_1_to_0);
					alpha1to0.setDuration(300);
					alpha1to0.setAnimationListener(new AnimationListener() {
						@Override
						public void onAnimationStart(Animation animation) {
						}
						@Override
						public void onAnimationRepeat(Animation animation) {
						}
						@Override
						public void onAnimationEnd(Animation animation) {
							recommendSearchLL.setVisibility(View.GONE);
							getFragmentHome().mTabBarCover.setVisibility(View.GONE);
							getFragmentHome().mTabBarCoverClickTarget = null;
					  		
					  		isAnimationing = false;
					  		
					  		if (needGoToSearchList) {
					  			needGoToSearchList = false;
					  			ActivitySearchList.startActivity(mContext, sexType, ageBegin, ageEnd, 
					  	    			constellation, provinceCode, hasPrivatePic, income);
					  		}
						}
					});
					recommendSearchLL.startAnimation(alpha1to0);
					getFragmentHome().mTabBarCover.startAnimation(alpha1to0);
				}
			});
	  		fragmentSearch.searchBgLL.startAnimation(slideOutToTop);
	  		
	  		
	  		Animation tvAlpha1to0 = AnimationUtils.loadAnimation(mContext, R.anim.alpha_1_to_0);
	        tvAlpha1to0.setDuration(300);
	        tvAlpha1to0.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}
				@Override
				public void onAnimationRepeat(Animation animation) {
				}
				@Override
				public void onAnimationEnd(Animation animation) {
					Animation tvAlpha0tp1 = AnimationUtils.loadAnimation(mContext, R.anim.alpha_0_to_1);
					tvAlpha0tp1.setAnimationListener(new AnimationListener() {
						@Override
						public void onAnimationStart(Animation animation) {
						}
						@Override
						public void onAnimationRepeat(Animation animation) {
						}
						@Override
						public void onAnimationEnd(Animation animation) {
							isSearchStatus = !isSearchStatus;
							setActionBar(actionBar);
						}
					});
			        tvAlpha0tp1.setDuration(300);
					rightTextView.setText(R.string.search);
					rightTextView.startAnimation(tvAlpha0tp1);
				}
			});
	    	rightTextView.startAnimation(tvAlpha1to0);
	  		
	  		
	  	} else { // 打开搜索
	  		recommendSearchLL.setVisibility(View.VISIBLE);
	  		getFragmentHome().mTabBarCover.setVisibility(View.VISIBLE);
	  		getFragmentHome().mTabBarCoverClickTarget = actionBar.getmRightTv();
	  		
	        FragmentTransaction ft = FragmentHomeRecommend.this.getActivity().getSupportFragmentManager().beginTransaction();
	        fragmentSearch = FragmentSearch.newInstance(ManagerAccount.getInstance().getCurrentGender());
	        fragmentSearch.setFragmentHomeRecommend(this);
	        ft.add(recommendSearchLL.getId(), fragmentSearch);
	        ft.commit();
	        
	        
	        // 动画效果
	        Animation tvAlpha1to0 = AnimationUtils.loadAnimation(mContext, R.anim.alpha_1_to_0);
	        tvAlpha1to0.setDuration(300);
	        tvAlpha1to0.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}
				@Override
				public void onAnimationRepeat(Animation animation) {
				}
				@Override
				public void onAnimationEnd(Animation animation) {
					Animation tvAlpha0tp1 = AnimationUtils.loadAnimation(mContext, R.anim.alpha_0_to_1);
					tvAlpha0tp1.setAnimationListener(new AnimationListener() {
						@Override
						public void onAnimationStart(Animation animation) {
						}
						@Override
						public void onAnimationRepeat(Animation animation) {
						}
						@Override
						public void onAnimationEnd(Animation animation) {
							isSearchStatus = !isSearchStatus;
							setActionBar(actionBar);
						}
					});
			        tvAlpha0tp1.setDuration(300);
					rightTextView.setText(R.string.close);
					rightTextView.startAnimation(tvAlpha0tp1);
				}
			});
	    	rightTextView.startAnimation(tvAlpha1to0);
	    	
	    	final Animation alpha0tp1 = AnimationUtils.loadAnimation(mContext, R.anim.alpha_0_to_1);
	    	alpha0tp1.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}
				@Override
				public void onAnimationRepeat(Animation animation) {
				}
				@Override
				public void onAnimationEnd(Animation animation) {
					fragmentSearch.searchBgLL.setVisibility(View.VISIBLE);
					Animation slideInFromTop = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_from_top);
					slideInFromTop.setInterpolator(new OvershootInterpolator());
					slideInFromTop.setAnimationListener(new AnimationListener() {
						@Override
						public void onAnimationStart(Animation animation) {
						}
						@Override
						public void onAnimationRepeat(Animation animation) {
						}
						@Override
						public void onAnimationEnd(Animation animation) {
					  		isAnimationing = false;
						}
					});
			    	slideInFromTop.setDuration(500);
			    	fragmentSearch.searchBgLL.startAnimation(slideInFromTop);
				}
			});
	    	alpha0tp1.setDuration(300);
	    	recommendSearchLL.startAnimation(alpha0tp1);
	    	getFragmentHome().mTabBarCover.startAnimation(alpha0tp1);
	  	}
    }
    
    private EgmCallBack mEgmCallback = new EgmCallBack(){
        @Override
        public void onGetRecommend(int transactionId, RecommendListInfo obj){
            if(mTid != transactionId)
                return;
        	
            stopWaiting();
			ArrayList<RecommendUserInfo> list = obj.list;
			//利用推荐来更新弹层
			ManagerAccount.getInstance().getCurrentAccount().mHasPortrait = obj.hasPortrait;
			getFragmentHome().updateByRecommend(obj);
			
			mAdapter.setDataList(list);
            if(mAdapter.getCount() <= 0){
                mListView.onNoContent();//showTip(TYPE_EMPTY);
            }
            else{
                mListView.onLoadingComplete();
                mListView.onRefreshComplete();
            }
//            else{
//                showTip(TYPE_CONTENT);
//            }
            
            // 因为有http缓存，所以没有网络的时候也会有缓存数据上来，这里也需要判断是否有网络
            if(!PlatformUtil.hasConnected(mContext)){
                showToast(R.string.reg_tip_netword_error);
            }
        }
        
        @Override
        public void onGetRecommendError(int transactionId, int errCode, String err){
            if(mTid != transactionId)
                return;
            
            stopWaiting();
            mListView.onRefreshComplete();
            
            if(mAdapter.getCount() <= 0){
                if(errCode == EgmServiceCode.NETWORK_ERR_COMMON){
                    mListView.onLoadingError();//showTip(TYPE_ERROR);
                }
                else{
                    mListView.onNoContent();//showTip(TYPE_EMPTY);
                }
            }
//            else{
//                showTip(TYPE_CONTENT);
//            }
            
            if(errCode == EgmServiceCode.TRANSACTION_ACCOUNT_NOT_BIND_MOBILE || errCode == EgmServiceCode.TRANSACTION_ACCOUNT_NOT_BIND_MOBILE2){
                if(errCode == EgmServiceCode.TRANSACTION_ACCOUNT_NOT_BIND_MOBILE){
                    showBindMobileTip(false);
                }
                else{
                    showBindMobileTip(true);
                }
                
//                EngagementApp.getAppInstance().clearAccountConfig();
//                ActivityAccountEntrance.startActivity(mContext);
//                mContext.finish();
            }
            else{
                showToast(err);
            }
        }
        
        @Override
        public void onGetPrivateDataSucess(int transactionId, UserPrivateData info) {
 
        	// 男性个人中心升级vip后，推荐页底部的升级vip/榜单入口状态需要随之改变
            if(info != null && info.userInfo != null 
            		&& ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Male){    // vip状态变化
                
            	ManagerAccount.getInstance().setVip(info.userInfo.isVip);
                
                mListView.getRefreshableView().removeFooterView(mFootView); // 去掉老的入口
                mFootView = getFootView();
                mListView.getRefreshableView().addFooterView(mFootView);    // 换成新的入口
            }
        }
        
        @Override
        public void onGetRankListInHomeSucess(int transactionId, RankListInfoInHome info) {

            if(ManagerAccount.getInstance().getCurrentGender()==EgmConstants.SexType.Female)
                return;
            
            mListView.getRefreshableView().removeFooterView(mFootView); // 去掉老的入口
            mFootView = getFootView();
            if (ManagerAccount.getInstance().isVip()) {
                String url = info.rankInfoList.get(3).picUrl;
                LoadingImageView bg = (LoadingImageView)mFootView.findViewById(R.id.rank_bg);
                bg.setLoadingImage(url);
                EgmPrefHelper.putRankPicture3(mContext, url);
                bg.setLoadingImage(EgmPrefHelper.getRankPicture3(mContext));
            }
            mListView.getRefreshableView().addFooterView(mFootView); // 换成新的入口
        }
		
		//  推荐活动 
        @Override
		public void onGetCompetitionListSucess(int transactionId, RecommendActivityListInfo info) {
            
            showOffPicForFemale(info);
        };
        @Override
		public void onGetCompetitionListError(int transactionId, int errCode, String err) {
           
            stopWaiting();
            showToast(err);
        };
    };
    
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }
    
    

    private String getUrl(String mUrl,String mTicket){
        StringBuilder builder = new StringBuilder();
        builder.append(EgmConstants.URL_URS_TICKET_LOGIN).append("?")
            .append("ticket=").append(mTicket).append("&")
            .append("url=").append(mUrl).append("&")
            .append("product=").append(EgmProtocolConstants.PRODUCT).append("&")
            .append("domains=").append("163.com");
        
        return builder.toString();
    }
    
    
    private boolean hasAddHeader;
	private List<RecommendActivityInfo> list;
	private TextView mTextView;
	private View rootHeaderShowOffPic;
    private AutoScrollViewPager viewPager;
    private Bundle mBundle;
	
    private final static String DES_URL="des_url";
	private final static int Card_Count_Per_Day=24*60*60/5;
	private int itemCount;
	private PagerIndicator mIndicator;
    private LinearLayout mLayout;
	
    //移除Headview    
    private void clear() {

        if (hasAddHeader) {
            list = null;
            mTextView = null;
            if (viewPager != null) {
                viewPager.stopAutoScroll();
                viewPager = null;
            }
            if (mLayout != null) {
                mListView.getRefreshableView().removeHeaderView(mLayout);
                hasAddHeader = false;
                mLayout=null;
                rootHeaderShowOffPic = null;
            }
        }
    }
    
    /**
     * 填充ViewPager内容
     */
    private int count=1;
    public void showOffPicForFemale(RecommendActivityListInfo info){

        if (info == null || info.list == null || info.list.size() == 0) {
            if (hasAddHeader) {
                clear();
            }
            return;
        }
        
        if (!hasAddHeader) {
            mLayout = new LinearLayout(mContext);
            rootHeaderShowOffPic = LayoutInflater.from(mContext).inflate(
                    R.layout.view_recommend_show_pic, mLayout, false);
            
            // 比例 720*200
            float height=mContext.getResources().getDisplayMetrics().widthPixels*200/640;
            
            //单独为男性添加白边
            if (ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Male) {
                LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, (int)height);
                mParams.setMargins(0, 0, 0,
                        mContext.getResources().getDimensionPixelSize(R.dimen.info_margin_4dp));
                mLayout.setBackgroundColor(0xFFFFFFFF);
                mLayout.addView(rootHeaderShowOffPic, mParams);

            } else {
                rootHeaderShowOffPic.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                        (int)height));
                mLayout.addView(rootHeaderShowOffPic);
            }
            viewPager = (AutoScrollViewPager)rootHeaderShowOffPic.findViewById(R.id.show_off_pic);
            mIndicator=(PagerIndicator)rootHeaderShowOffPic.findViewById(R.id.tips_indicator);
            mListView.getRefreshableView().addHeaderView(mLayout);
            hasAddHeader = true;
        }

		fillDataForShowOffPic(info);
    }
    

    
    /*** 填充ViewPager */
	private void fillDataForShowOffPic(RecommendActivityListInfo info) {
		
		if (info == null || info.list == null || info.list.size() == 0)
			return;
		
		list = info.list;
		itemCount=list.size();
		
		mIndicator.setCount(itemCount);
		mIndicator.setCurrentItem(0);
        mIndicator.setGap(6);
        mIndicator.setIndicator(mContext.getResources().getDrawable(R.drawable.shape_indicator_dot_unselected));
        mIndicator.setHighlight(mContext.getResources().getDrawable(R.drawable.shape_indicator_dot_selected));
		viewPager.setAdapter(new PicShowOffPagerAdapter(mContext, list,new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			    
                RecommendActivityInfo mInfo = (RecommendActivityInfo)v.getTag();
				mBundle=new Bundle();
				String checkKey="date-photo-show!#$^nMP";
				String deviceKey=PlatformUtil.getDeviceID(mContext);
				String checksum = EnctryUtil.getMd5Hash(ManagerAccount.getInstance().getCurrentAccount().mAccount + deviceKey + checkKey);
				
                String verifyUrl = mInfo.targetUrl 
                        + "&deviceKey=" + PlatformUtil.getDeviceID(mContext)
                        + "&platform=" + EgmProtocolConstants.PLATFORM_ANROID 
                        + "&channel=" + EgmUtil.getAppChannelID(BaseApplication.getAppInstance())
                        + "&checksum="+checksum
                        + "&version="+EgmUtil.getNumberVersion(getActivity());
				mBundle.putString(DES_URL, verifyUrl);
				
                ActivityPicShowOffForFemale.startActivity(getActivity(), mBundle);
                
			}
		}));
		
		mTextView=(TextView) rootHeaderShowOffPic.findViewById(R.id.tips_indicator_view);
		
		//如果只有一条，就不用显示条目，也不需要轮播
		if (itemCount == 1) {
			mTextView.setVisibility(View.INVISIBLE);
			mIndicator.setVisibility(View.INVISIBLE);
		} else {
			mTextView.setText(mContext.getString(R.string.rec_comption_count, 1,itemCount));
			viewPager.setCurrentItem(getTripleValue(Card_Count_Per_Day,itemCount));
			viewPager.setOnPageChangeListener(new SimpleOnPageChangeListener() {
				@Override
				public void onPageSelected(int pos) {
                    if (pos == 0 || pos == Card_Count_Per_Day) {
                        viewPager.setCurrentItem(Card_Count_Per_Day, false);
                    }
//                    mTextView.setText(mContext.getString(R.string.rec_comption_count, pos
//                            % itemCount + 1, itemCount));
                    mIndicator.setCurrentItem(pos % itemCount);
                }
			});
            viewPager.stopAutoScroll();
            viewPager.setInterval(5000);
            viewPager.startAutoScroll();
		}
	}
	
	/**自定义PagerAdapter，支持轮播*/
	
	class PicShowOffPagerAdapter extends PagerAdapter{

		private Context mContext;
		private List<RecommendActivityInfo>mList;
		private View.OnClickListener mClickListener;
		private int itemSize;
		
		public PicShowOffPagerAdapter(Context context, List<RecommendActivityInfo> list, View.OnClickListener mClickListener){
		
			this.mContext=context;
			this.mList=list;
			this.mClickListener=mClickListener;
			this.itemSize=list.size();
		}
		
		@Override
		public int getCount() {
			if (itemSize == 1)
				return 1;
			return Integer.MAX_VALUE;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
		    container.removeView((View)object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {

		    View view=LayoutInflater.from(mContext).inflate(R.layout.item_recommend_show_pic, null,false);
			LayoutParams params=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
			container.addView(view,params);
			view.setOnClickListener(mClickListener);
			view.setTag(mList.get(position%itemSize));
			
			LoadingImageView mImageView=(LoadingImageView)view.findViewById(R.id.content_competition);
			final ProgressBar mProgressBar=(ProgressBar)view.findViewById(R.id.progressBar_in_pic_showoff);
			mProgressBar.setVisibility(View.INVISIBLE);
			mImageView.setScaleType(ScaleType.FIT_XY);
            mImageView.setUiGetImageListener(new IUiGetImage() {
                
                @Override
                public void onLoadImageStar(String url) {
                    mProgressBar.setVisibility(View.VISIBLE);
                }
                
                @Override
                public void onLoadImageFinish() {
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
                
                @Override
                public void onLoadImageError() {
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            });
			mImageView.setLoadingImage(mList.get(position%itemSize).imgUrl);

			return view;
		}
	}
	
	public int getTripleValue(int value, int div) {
		if (div <= 0)
			return -1;
		return value - value % div;
	}
	
    
	private final long Half_DAY = 60 * 60 * 1000;

    boolean isAfterHalfDay(long lastTime) {
        return System.currentTimeMillis() - lastTime > Half_DAY;
    }
   

    @Override
    public void onPause() {
        super.onPause();
        if (viewPager != null) {
            viewPager.stopAutoScroll();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewPager != null) {
            viewPager.startAutoScroll();
        }
    }
}
