package com.netease.engagement.fragment;

import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.common.image.ImageViewAsyncCallback;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.activity.ActivityMultiRankList;
import com.netease.engagement.activity.ActivitySingleRankList;
import com.netease.engagement.activity.ActivityWeb;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.view.CustomerLinearLayout;
import com.netease.engagement.view.CustomerLinearLayout.IDispatchTouchEventListener;
import com.netease.engagement.view.IFragment;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.engagement.widget.LoadingImageView;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.db.manager.ManagerAccount.Account;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.RankListInfoInHome;
import com.netease.service.protocol.meta.RankListItmeInfoInhome;
import com.netease.service.protocol.meta.UserPrivateData;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * 主页-排行Fragment
 * 产品临时决定去掉滚动伸缩的动画，为防又更改回去，所以实现动画的布局和实现方式没有改变。
 * 如果不考虑有动画，可以直接ListView简单实现，不必现在如此复杂。
 * @author 
 * @version 1.0
 */
public class FragmentHomeDiscover extends FragmentBase implements IFragment {
    private ActivityEngagementBase mActivity;
    
    private LoadingImageView mRankBg0, mRankBg1, mRankBg2, mRankBg3, mRankBg4, mRankBg5;
    private LoadingImageView mRankBgChargeMale;
    private LoadingImageView mPrivatePicBgChargeMale;
    private View mUpgradeBtn;
    private TextView mVipTipTv;
    private int mRankItemWidth, mRankItemHeight;
    private int mTid;

    
    private FrameLayout rank_bg;
    private CustomerLinearLayout itemContainer;
    private int maxHeight;
    private int minHeight;
    private int maxTextSize;
    private int minTextSize;
    
    private ArrayList<View> viewList = new ArrayList<View>();
    private boolean oldVipState;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (ActivityEngagementBase)getActivity();
        EgmService.getInstance().addListener(mEgmCallback);
        
        mRankItemWidth = -1;//DeviceUtil.getScreenWidth(mActivity);
        mRankItemHeight = -1;//mActivity.getResources().getDimensionPixelSize(R.dimen.rank_item_max_height);
        
        maxHeight = getResources().getDimensionPixelSize(R.dimen.rank_item_max_height);
        minHeight = getResources().getDimensionPixelSize(R.dimen.rank_item_min_height);
        
        maxTextSize = getResources().getDimensionPixelSize(R.dimen.rank_item_text_max_size);
        minTextSize = getResources().getDimensionPixelSize(R.dimen.rank_item_text_min_size);
        
        oldVipState=ManagerAccount.getInstance().isVip();
    }
    
    @Override
    public void onDestroy(){
        super.onDestroy();
        EgmService.getInstance().removeListener(mEgmCallback);
    }
    
    private void addView(ViewGroup parent, View itemView, boolean isBig){
        int layoutHeight;
        if(isBig){
            layoutHeight = maxHeight;
        }
        else{
            layoutHeight = minHeight;
        }
        
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, layoutHeight);
        parent.addView(itemView, lp);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        View view = inflater.inflate(R.layout.fragment_discover_layout, container, false);
 
        rank_bg = (FrameLayout) view.findViewById(R.id.rank_bg);
        
        itemContainer = (CustomerLinearLayout)view.findViewById(R.id.rank_content_container);
        itemContainer.setDispatchTouchEventListener(dispatchTouchEventListener);
        itemContainer.setLongClickable(true);
        return view;
    }

    private long lastUpDataPoint;
    
    @Override
    public void onPageSelected() {
        if(mActivity != null){
            CustomActionBar actionBar = mActivity.getCustomActionBar();
            setActionBar(actionBar);
        }
        // 隔五分钟刷新
        if ((System.currentTimeMillis()-lastUpDataPoint ) > (5*60*1000)) {
            mTid = EgmService.getInstance().doGetRankListInfoInHome(ManagerAccount.getInstance().getCurrentIdString());
            lastUpDataPoint=System.currentTimeMillis();
        }
        
     }
 
    @Override
    public void onPageReSelected() {
        
    }
    
    @Override
    public void onTabDoubleTap() {

    }
    
    private void upgradeVip(){
        ActivityWeb.startUpgradeVip(mActivity);
    }
    
    @SuppressLint("ResourceAsColor") 
    private void setActionBar(CustomActionBar actionBar){
        actionBar.setMiddleTitle(R.string.rank_title);
        actionBar.setLeftAction(R.drawable.icon_titlebar_back_selector, "");    // 为了切换tab后让标题居中
        actionBar.setLeftVisibility(View.INVISIBLE);
        actionBar.setRightVisibility(View.INVISIBLE);
        actionBar.setRightAction(R.drawable.icon_titlebar_ok_selector, ""); // 为了切换tab后让标题居中
        actionBar.setBackgroundColor(R.color.transparent);
    }
    
    private EgmCallBack mEgmCallback = new EgmCallBack(){
        /** 获取排行榜背景图片 */
        @Override
        public void onGetPrivateDataSucess(int transactionId, UserPrivateData info) {
            // 个人中心升级vip后，页面底部的升级vip入口状态需要随之改变
            if(info != null && info.userInfo != null){    // vip状态变化
                
            	ManagerAccount.getInstance().setVip(info.userInfo.isVip);
                
                if(ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Male 
                		&& !ManagerAccount.getInstance().isVip()){
                    
                    if (mUpgradeBtn != null && mVipTipTv != null) {
                        mUpgradeBtn.setVisibility(View.VISIBLE);
                        mVipTipTv.setVisibility(View.VISIBLE);
                    }
                }
                else if(ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Male 
                		&& ManagerAccount.getInstance().isVip()){
                    if (mUpgradeBtn != null && mVipTipTv != null) {
                        mUpgradeBtn.setVisibility(View.GONE);
                        mVipTipTv.setVisibility(View.GONE);
                    }}
            }
            
            if (oldVipState != ManagerAccount.getInstance().isVip()) {
                
                // 为了让给VIP升级让路,以后想要更新榜单，只需要调用获取个人信息,当然必须成功才会变化
                oldVipState= ManagerAccount.getInstance().isVip();
                EgmService.getInstance().doGetRankListInfoInHome(ManagerAccount.getInstance().getCurrentIdString());
            }
        }

        public void onGetRankListInHomeSucess(int transactionId, RankListInfoInHome info) {

            if (info == null || info.rankInfoList == null) {
                cover.setVisibility(View.VISIBLE);
                return;
            }
            cover.setVisibility(View.GONE);
            addRankPagesItems(info);
            refreshRankList();
        } 

        public void onGetRankListInhomeError(int transactionId, int errCode, String err) {
            
            cover.setVisibility(View.VISIBLE);
            ToastUtil.showToast(getActivity(), err);
        }
    };

    
    private void addRankPagesItems(RankListInfoInHome info) {
        int size = info.rankInfoList.size();
        LayoutInflater mInflater = LayoutInflater.from(getActivity());
        setLayoutMargin(itemContainer, 0);
        itemContainer.removeAllViews();
        viewList.clear();
        for (int i = 0; i < size; i++) {
            View view = null;
            LoadingImageView mRankBg = null;
            RankListItmeInfoInhome infoItem = info.rankInfoList.get(i);

            if (i == 0) {

                view = getItemViewByRankId(mInflater, infoItem,true);

                if (ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Male
                        && infoItem.id == EgmConstants.RankID.TOP_FEMALE) {
                    setTopFemaleItem(view, infoItem, true);
                }
                addView(itemContainer, view, true);
            } else {
  
                view = getItemViewByRankId(mInflater, infoItem, false);

                if (ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Male
                        && infoItem.id == EgmConstants.RankID.TOP_FEMALE) {
                    setTopFemaleItem(view, infoItem, false);
                }
                addView(itemContainer, view, false);
            }
            viewList.add(view);
            mRankBg = (LoadingImageView)view.findViewById(R.id.rank_bg);
            mRankBg.setLoadingImage(infoItem.picUrl);
            setTopThress(view, infoItem, infoItem.sex);
        }
        
        upDateVipEntrance();

    }

    
    /**
     * 升级VIP入口
     */
    private void upDateVipEntrance() {
        // 升级vip
        LayoutInflater mInflater = LayoutInflater.from(getActivity());
        final View rankVip = mInflater.inflate(R.layout.view_rank_upgrade_vip, null, false);

        mUpgradeBtn = rankVip.findViewById(R.id.rank_update_vip);
        mUpgradeBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                v.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        upgradeVip();
                    }
                }, 50);

            }
        });
        if (ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Male 
        		&& !ManagerAccount.getInstance().isVip()) {
            rankVip.setVisibility(View.VISIBLE);
        } else {
            rankVip.setVisibility(View.INVISIBLE);
        }
        addView(itemContainer, rankVip, false);

        rankVip.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (isScroll) {
                    return;
                }
                int height = rank_bg.getMeasuredHeight();
                setLayoutHeight(rankVip, height - maxHeight, false);
                setLayoutHeight(itemContainer, height + minHeight * (viewList.size() - 1), false);
            }
        }, 1000);
    }
    
    /**
     * 女神榜的特殊处理，添加Vip处理逻辑
     */
    private void setTopFemaleItem(final View view, RankListItmeInfoInhome infoItem,boolean isBig) {

        
        mVipTipTv = (TextView)view.findViewById(R.id.rank_vip_tip);
        mVipTipTv.setText(R.string.rank_vip_only_tip);
        mVipTipTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, minTextSize - 8);
        if (ManagerAccount.getInstance().isVip()) {
            mVipTipTv.setVisibility(View.GONE);
        } else {
            mVipTipTv.setVisibility(View.VISIBLE);
        }
        LoadingImageView mRankBg = (LoadingImageView)view.findViewById(R.id.rank_bg);
        
        
        mRankBg.setLoadingImage(infoItem.picUrl);

        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                v.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (isScroll) {
                            return;
                        }
                        if (ManagerAccount.getInstance().isVip()) {
                            // vip用户可以进入查看
                            ActivityMultiRankList.startActivity(mActivity,
                                    EgmConstants.RankID.TOP_FEMALE,
                                    mActivity.getString(R.string.rank_name_top_female),
                                    EgmConstants.SexType.Female);
                        } else {
                            // 非vip去开通vip
                            upgradeVip();
                        }
                    }
                }, 50);

            }
        });
    }
    
    /**
     * 函数作用              创建排行榜Item
     * @param inflater      渲染Inflater
     * @param sexType       排行榜性别
     * @param rankTitle     排行榜title
     * @param rankId        排行榜ID
     * @param rankCount     是否区分日月榜
     * @param isBig         是否是初始大Item
     * @return
     */

            
    private View getItemViewByRankId(LayoutInflater inflater, final RankListItmeInfoInhome info,
            boolean isBig) {

        View itemView = inflater.inflate(R.layout.view_rank_item, null, false);
        itemView.setTag(info);
        TextView title = (TextView)itemView.findViewById(R.id.rank_name);
        title.setText(info.name);

        if (isBig) {
            setTextSize(itemView, maxHeight);
        } else {
            setTextSize(itemView, minHeight);
        }

        itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                v.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (isScroll) {
                            return;
                        }

                        RankListItmeInfoInhome item = (RankListItmeInfoInhome)v.getTag();
                        if (item.rankCount == 1) {
                            // Vip升级逻辑
                            if (item.needVip) {
                                if (!ManagerAccount.getInstance().isVip()) {
                                    upgradeVip();
                                    return;
                                }
                            }
                            // 满足条件跳转
                            ActivitySingleRankList.startActivity(mActivity, item.id, item.name,
                                    item.sex);
                        } else {
                            // Vip升级逻辑
                            if (item.needVip) {
                                if (!ManagerAccount.getInstance().isVip()) {
                                    upgradeVip();
                                    return;
                                }
                            }
                            // 满足条件跳转
                            ActivityMultiRankList.startActivity(mActivity, item.id, item.name,
                                    item.sex);
                        }
                    }
                }, 50);

            }
        });

        return itemView;
    }
    

    /**
     * 设置并填充前三名信息
     * @param view
     * @param item
     * @param sex
     */
    private void setTopThress(View view, RankListItmeInfoInhome item, int sex) {

        CircleImageView imageView;

        imageView = (CircleImageView)view.findViewById(R.id.rank_female_profile_first);
        imageView.setBorderColor(Color.WHITE);
        imageView.setBorderWidth(getActivity().getResources().getDimensionPixelSize(
                R.dimen.info_margin_1dp));
        setCircleImageUrl(imageView, item.top3Urls.get(0), sex);

        imageView = (CircleImageView)view.findViewById(R.id.rank_female_profile_second);
        imageView.setBorderColor(Color.WHITE);
        imageView.setBorderWidth(getActivity().getResources().getDimensionPixelSize(
                R.dimen.info_margin_1dp));
        setCircleImageUrl(imageView, item.top3Urls.get(1), sex);

        imageView = (CircleImageView)view.findViewById(R.id.rank_female_profile_third);
        imageView.setBorderColor(Color.WHITE);
        imageView.setBorderWidth(getActivity().getResources().getDimensionPixelSize(
                R.dimen.info_margin_1dp));
        setCircleImageUrl(imageView, item.top3Urls.get(2), sex);
    }

    private void setCircleImageUrl(CircleImageView view, String url, int sex) {
        
        if (sex == EgmConstants.SexType.Male) {
            view.setImageResource(R.drawable.bg_portrait_man_default_200);
        } else {
            view.setImageResource(R.drawable.bg_portrait_women_default_200);
        }
        view.setTag(new ImageViewAsyncCallback(view, url));
    }

    
    
    
    
    /***************************动效部分 开始*********************************/
    
    private boolean isScroll;  // 是否有滑动
    
    private float downY;
    private long downTimeStamp;
    
    private float lastY;
    
    private IDispatchTouchEventListener dispatchTouchEventListener = 
    		new IDispatchTouchEventListener() {
		@Override
		public void dispatchTouchEvent(MotionEvent ev) {
			float currentY = ev.getY();
			switch(ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				isScroll = false;
				downY = currentY;
				downTimeStamp = System.currentTimeMillis();
				lastY = downY;
				topView = null;
				bottomView = null;
				
				isMoveEnd=false;
				break;
			case MotionEvent.ACTION_MOVE:
				if (!isScroll) {
					if (Math.abs(currentY - downY) > 3) {
						isScroll = true;
					}
				}
				if (isScroll) {
					float deltaY = currentY - lastY;
					if(Math.abs(deltaY) > 4) {
						reSetLayoutHeight(deltaY * 1.2f);
						lastY = currentY;
					}
				}
				distanceMove=(int)Math.abs(currentY - downY);
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				lastY = currentY;
                isMoveEnd=true;
                
				animateToAlign();
				break;
			default:
				break;
			}
		}
	};
    
	
	/**
	 * 这个方法，就是根据手指的移动距离，修改itemContainer的margin，以及firstView和secondView的高度，文字大小
	 */
    private void reSetLayoutHeight(float delatY) {
        int delta = ( (int) delatY) / 2;
        if(delta == 0) {
            return;
        } else if (delta > 0) { // 手指向下滑动
            /*
             手指移动了 2*delta 的距离，itemContainer下移delta位置，firstView高度减少delta，secondView增加delta
             
             itemContainer ：外部的LinearLayout容器
             firstView ：从后往前查找，第一个高度大于minHeight的view （如果是viewList的第一个，就不处理，firstView为null）
             secondView ：是firstView的前一个view
             minus ＝ Math.min(Math.abs(delta), firstViewHeight - minHeight)  PS：minus是正数
             itemContainer位置(topMargin)下移minus
             firstView的高度(height)减少minus
             secondView的高度(height)增加minus
            */
            
            View firstView = null;
            View secondView = null;
            int firstViewHeight = 0;
            
            for (int i=viewList.size()-1; i>0; i--) {
                View view = viewList.get(i);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
                if (lp.height > minHeight) {
                    firstView = view;
                    firstViewHeight = lp.height;
                    secondView = viewList.get(i-1);
                    break;
                }
            }
            
            if (firstView != null) { 
                int minus = Math.min(Math.abs(delta), firstViewHeight - minHeight);
                setlayoutMarginDelta(itemContainer,  minus);
                setLayoutHeigthDelta(firstView, -minus);
                setLayoutHeigthDelta(secondView, minus);
            
                if (distanceMove > distanceGate) {
                    refreshRankList();
                }
            }
            
        } else { // 手指向上滑动
            /*
             手指移动了 2*delta 的距离，itemContainer上移delta位置，firstView高度减少delta，secondView增加delta
             
             itemContainer ：外部的LinearLayout容器
             firstView ：从前往后查找，第一个高度大于minHeight的view （如果是viewList的最后一个，就不处理，firstView为null）
             secondView ：是firstView的下一个view
             minus ＝ Math.min(Math.abs(delta), firstViewHeight - minHeight)  PS：minus是正数
             itemContainer位置(topMargin)上移minus
             firstView的高度(height)减少minus
             secondView的高度(height)增加minus
            */
            
            View firstView = null;
            View secondView = null;
            int firstViewHeight = 0;
            
            for (int i=0; i<viewList.size()-1; i++) {  // viewList的最后一个view不处理
                
                View view = viewList.get(i);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
                if (lp.height > minHeight) {
                    firstView = view;
                    firstViewHeight = lp.height;
                    secondView = viewList.get(i+1);

                    break;
                }
            }
            if (firstView != null) {
                int minus = Math.min(Math.abs(delta), firstViewHeight - minHeight);
                setlayoutMarginDelta(itemContainer,  -minus);
                setLayoutHeigthDelta(firstView, -minus);
                setLayoutHeigthDelta(secondView, minus);
                
                if (distanceMove > distanceGate) {
                    refreshRankList();
                }
            }
        }
    }
    
    private int distanceMove;
    
    /**
     * 以下参数和方法，是用来手指离开后，对页面的对齐调整。定位到放到最大的view，其他view缩到最小
     */
    private int itemContainerMargin;
    private View topView;
    private int topViewHeight;
    private int topViewIndex;
    private View bottomView;
    private int bottomViewHeight;
    private boolean isTopViewMax;
	private void animateToAlign() {
		/*
		 步骤一：确定对齐方案
		 步骤二：方案确定后，使用animation进行动画执行
		 步骤三：动画结束后，再进行最后一次位置纠正
		 
		 
		 ——————以下是对齐方案逻辑——————
		  topVew : 从前往后，查找第一个高度不是minHeight，也不是maxHeight的view(如果所有view都正常现实，topVew为null，不处理)
		  bottomVew : topVew之后的下一个view
		  if (快速滑动) { // 快速滑动定义 : 手指按下到手指松开的时间小雨100毫秒
		  	如果手指向下滑动，topVew变大，bottomVew变小
		  	如果手指向上滑动，topVew变小，bottomVew变大
		  } else {
		  	if ((topVew.height-minHeight) > 0.5*minHeight) {
		  		topVew变大，bottomVew变小
		  	} else {
		  		topVew变小，bottomVew变大
		  	}
		  }
		*/
		
		// 步骤一：确定对齐方案
		for (int i=0; i<viewList.size(); i++) {
			View view = viewList.get(i);
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
			if (lp.height>minHeight && lp.height<maxHeight) {
				topView = view;
				topViewHeight = lp.height;
				topViewIndex = i;
				bottomView = viewList.get(i+1);
				LinearLayout.LayoutParams lp2 = (LinearLayout.LayoutParams) bottomView.getLayoutParams();
				bottomViewHeight = lp2.height;
				LinearLayout.LayoutParams lp3 = (LinearLayout.LayoutParams) itemContainer.getLayoutParams();
				itemContainerMargin = lp3.topMargin;
				break;
			}
		}
        if (topView == null) { // 防止正好等于时不响应
            for (int i = 0; i < viewList.size(); i++) {
                View view = viewList.get(i);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)view.getLayoutParams();
                if (lp.height == maxHeight) {
                    refreshRankList();
                    return;
                }
            }
        }
        
		if (topView == null) {
			return;
		}
		
		int deltaHeight = 0;

        long currentTimestamp = System.currentTimeMillis();
        if (currentTimestamp - downTimeStamp < 300) {
            if (lastY - downY > 0) { // 手指向下滑动，topVew变大，bottomVew变小
                deltaHeight = maxHeight - topViewHeight; // deltaHeight为正数
                isTopViewMax = true;
            } else { // 手指向上滑动，topVew变小，bottomVew变大
                deltaHeight = minHeight - topViewHeight; // deltaHeight为负数
                isTopViewMax = false;
            }
        } else {
            if (topViewHeight - minHeight > 0.5 * minHeight) { // topVew变大，bottomVew变小
                deltaHeight = maxHeight - topViewHeight; // deltaHeight为正数
                isTopViewMax = true;
            } else { // topVew变小，bottomVew变大
                deltaHeight = minHeight - topViewHeight; // deltaHeight为负数
                isTopViewMax = false;
            }
        }
        
        
        
        // 步骤二：方案确定后，使用animation进行动画执行
        ValueAnimator animation = ValueAnimator.ofInt(0, deltaHeight);
        int duration = 10 + 50 * (Math.abs(deltaHeight) / (minHeight / 4));
        animation.setDuration(duration);
        animation.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int delta = (Integer) animation.getAnimatedValue();
                setLayoutMargin(itemContainer, itemContainerMargin + delta);
                setLayoutHeight(topView, topViewHeight + delta, true);
                setLayoutHeight(bottomView, bottomViewHeight - delta, true);
                

                if (Math.abs(delta) > Math.abs(distanceGate - distanceMove)) {
                    refreshRankList();
                }
                
            }
        });
        animation.setInterpolator(new DecelerateInterpolator());
        
        // 步骤三：动画结束后，再进行最后一次位置纠正
        animation.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                // 动画结束后，做最终的位置矫正
                if (isTopViewMax) {
                    setLayoutMargin(itemContainer, (-1) * minHeight * topViewIndex);
                    setLayoutHeight(topView, maxHeight, true);
                    setLayoutHeight(bottomView, minHeight, true);
                } else {
                    setLayoutMargin(itemContainer, (-1) * minHeight * (topViewIndex + 1));
                    setLayoutHeight(topView, minHeight, true);
                    setLayoutHeight(bottomView, maxHeight, true);
                }
                // 显示
                refreshRankList();
            }
            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
        
        animation.start();
    }
    
	/***************************动效部分 结束*********************************/
    
    
    
    private void setLayoutHeight(View itemView, int height, boolean needSetTextSize) {
    	if (itemView != null) {
	    	LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) itemView.getLayoutParams();
	    	lp.height = height;
	    	itemView.setLayoutParams(lp);
	    	
	    	if (needSetTextSize) {
	    		setTextSize(itemView, lp.height);
	    	}
    	}
    }
    
    private void setLayoutHeigthDelta(View itemView, int delta) {
    	if (itemView != null) {
	    	LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) itemView.getLayoutParams();
	    	lp.height = lp.height + delta;
	    	itemView.setLayoutParams(lp);
	    	
	    	setTextSize(itemView, lp.height);
    	}
    }
    
    private void setLayoutMargin(View itemView, int margin) {
    	if (itemView != null) {
	    	LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) itemView.getLayoutParams();
	    	lp.topMargin = margin;
	    	itemView.setLayoutParams(lp);
    	}
    }
    
    private void setlayoutMarginDelta(View itemView, int delta) {
    	if (itemView != null) {
	    	LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) itemView.getLayoutParams();
	    	lp.topMargin = lp.topMargin + delta;
	    	itemView.setLayoutParams(lp);
    	}
    }
    
    private void setTextSize(View itemView, int height) {
    	int h = height - minHeight;
    	int size = minTextSize + (h / (minHeight / 13)) * ((maxTextSize - minTextSize) / 13);
    	TextView rank_name = (TextView) itemView.findViewById(R.id.rank_name);
    	if (rank_name!=null) {
    		rank_name.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
    	}
    }
    

    // 拖动距离的阈值
    private int distanceGate;
    private View cover;
    private boolean DEBUG=true;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cover = view.findViewById(R.id.cover);
        cover.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mTid=EgmService.getInstance().doGetRankListInfoInHome(ManagerAccount.getInstance().getCurrentIdString());
            }
        });
        TextView textView = (TextView)cover.findViewById(R.id.empty_text);
        textView.setText(getActivity().getResources().getText(R.string.common_reload_tip));

        distanceGate = (int)(getActivity().getResources().getDisplayMetrics().heightPixels * 0.08);
        refreshRankList();

        mTid = EgmService.getInstance().doGetRankListInfoInHome(ManagerAccount.getInstance().getCurrentIdString());
        lastUpDataPoint = System.currentTimeMillis();
    }
    private void refreshRankList() {
        int size = viewList.size();
        for (int i = 0; i < size; i++) {
            View root = viewList.get(i);
            if (root.getLayoutParams().height == maxHeight) {
                final View view = root.findViewById(R.id.rank_rankings_three);
                view.setVisibility(View.VISIBLE);
                ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0.0f, 1.0f);
                animator.setDuration(200);
                animator.addListener(new AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator animation) {
                        view.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }
                });
                
                if (isMoveEnd) {
                    animator.start();
                }
            } else {
                
                final View view = root.findViewById(R.id.rank_rankings_three);
                float alphaStart = view.getAlpha();
                
                distanceMove=0;
                ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", alphaStart, 0.0f);
                animator.setDuration(100);
                animator.addListener(new AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator animation) {
                        view.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }
                });
                animator.start();
            }
        }
    }

    private boolean isMoveEnd;
    
}