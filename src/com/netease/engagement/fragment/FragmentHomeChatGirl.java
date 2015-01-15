package com.netease.engagement.fragment;

import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;

import com.netease.common.service.BaseService;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.activity.ActivityHome;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.view.HomeTabView;
import com.netease.engagement.view.IFragment;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.service.db.manager.LastMsgDBManager;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.ChatItemInfo;


/**
 * 女性聊天列表页
 */
public class FragmentHomeChatGirl extends FragmentBase implements IFragment{
    
    private int mCurrentTab = 0;
    
	
	public static FragmentHomeChatGirl newInstance(){
		FragmentHomeChatGirl fragment = new FragmentHomeChatGirl();
		return fragment ;
	}

	private CustomActionBar mCustomActionBar ;
	private RadioGroup mRadioGroup ;
	
	private boolean mLoaded = false ;
	public static boolean mOnlyNew = false ;
	
	private ViewPager mViewPager ;
	
	private ChatFragmentPagerAdapter mPagerAdapter ;
	
	private FragmentChatOrderTime mFragmentTime ;
	private FragmentChatOrderRich mFragmentRich ;
	private FragmentChatOrderIn mFragmentIn ;
	
	private LinearLayout mMsgTipLayout ;
	private long mLastRefreshTime = 0;//记录上次上传统计时间
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            mCurrentTab = savedInstanceState.getInt(EgmConstants.KEY_TAB_INDEX);
            mOnlyNew = savedInstanceState.getBoolean(EgmConstants.KEY_IS_ONLYNEW);
        }
        EgmService.getInstance().addListener(mCallBack);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.fragment_home_chat_girl, container, false);
        init(root);
        return root;
    }
    
    @Override
	public void onResume() {
		super.onResume();
		setUnReadNum();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setUnReadNum();
	}
    
    /**
	 * 设置底部消息未读数目
	 */
	private void setUnReadNum(){
		HomeTabView tabView = ((ActivityHome)getActivity()).getChatTab() ;
		if(tabView != null){
		    int count = LastMsgDBManager.getUnReadNum() ;
	        count = (count > 99 ? 99 : count) ;
			if(count != 0){
				tabView.setTipCount(count);
			}else if(count == 0){
				tabView.setTipLayoutVisibility(View.GONE);
			}
		}
	}

	@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EgmConstants.KEY_TAB_INDEX, mCurrentTab);
        outState.putBoolean(EgmConstants.KEY_IS_ONLYNEW, mOnlyNew);
    }
    
    /**
     * 初始化标题栏
     */
    private void initTitle(){
        ActivityEngagementBase activity = ((ActivityEngagementBase)getActivity());
        if (activity != null) {
            mCustomActionBar = activity.getCustomActionBar();
            mCustomActionBar.getCustomView().setBackgroundColor(
                    getResources().getColor(R.color.white));

            mCustomActionBar.setLeftAction(R.drawable.button_back_circle_selector, "chat");
            mCustomActionBar.setLeftTitleColor(getResources().getColor(R.color.white));
            mCustomActionBar.hideLeftTitle();

            mCustomActionBar.setMiddleTitle(R.string.str_chat);
            mCustomActionBar.setMiddleTitleColor(getResources().getColor(R.color.black));
            mCustomActionBar.setMiddleTitleSize(20);

            mCustomActionBar.setRightAction(R.drawable.icon_select_cancle, R.string.only_new);
            mCustomActionBar.setRightBackgroundResource(0);
            mCustomActionBar.setRightTitleColor(getResources().getColor(R.color.black));
            mCustomActionBar.setRightTitleColor(getResources().getColor(
                    R.color.info_level_txt_color));
            mCustomActionBar.setRightTitleSize(17);
            mCustomActionBar.setRightClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mOnlyNew = !mOnlyNew;
                    if (mOnlyNew) {
                        mCustomActionBar.setRightAction(R.drawable.icon_purple_select_ok,
                                R.string.only_new);
                    } else {
                        mCustomActionBar.setRightAction(R.drawable.icon_purple_select_cancle,
                                R.string.only_new);
                    }
                    // 只重新load当前viewpager的数据
                    switch (mCheckedId) {
                        case R.id.radio_time:
                            if (mFragmentTime != null) {
                                mFragmentTime.reLoad(mOnlyNew);
                            }
                            break;
                        case R.id.radio_rich:
                            if (mFragmentRich != null) {
                                mFragmentRich.reLoad(mOnlyNew);
                            }
                            break;
                        case R.id.radio_init:
                            if (mFragmentIn != null) {
                                mFragmentIn.reLoad(mOnlyNew);
                            }
                            break;
                    }
                }
            });
            if (mOnlyNew) {
                mCustomActionBar
                        .setRightAction(R.drawable.icon_purple_select_ok, R.string.only_new);
            } else {
                mCustomActionBar.setRightAction(R.drawable.icon_purple_select_cancle,
                        R.string.only_new);
            }
            mCustomActionBar.getmRightTv().setTextColor(this.getResources().getColor(R.color.chat_chat_list_new_text_color));
        }
    }
    
    private void init(View root){
    	mRadioGroup = (RadioGroup)root.findViewById(R.id.radio_group);
    	mRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);
    	
    	mMsgTipLayout = (LinearLayout)root.findViewById(R.id.msg_tip_layout);
    	mMsgTipLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			    setCurrentTab(0);
				mMsgTipLayout.setVisibility(View.GONE);
			}
		});
    	
    	mViewPager = (ViewPager)root.findViewById(R.id.pager);
        mViewPager.setOnPageChangeListener(mOnPageChangeListener);
    	mPagerAdapter = new ChatFragmentPagerAdapter(this.getChildFragmentManager());
    	mViewPager.setAdapter(mPagerAdapter);
    	mViewPager.setCurrentItem(mCurrentTab);
    	mViewPager.setOffscreenPageLimit(3);
    	
    	((RadioButton)mRadioGroup.getChildAt(mCurrentTab)).setChecked(true);
    	
    }
    
    private int mCheckedId ;
    private OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener(){
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			mCheckedId = checkedId ;
			switch(checkedId){
				case R.id.radio_time:
					/**
					 * 按照时间排序 
					 */
					setCurrentTab(0);
					if(mFragmentTime != null){
						mFragmentTime.load(mOnlyNew);
					}
					break;
				case R.id.radio_rich:
					/**
					 * 按照豪气值排序 
					 */
					setCurrentTab(1);
					if(mFragmentRich != null){
					    mFragmentRich.load(mOnlyNew);
					}
					break;
				case R.id.radio_init:
					/**
					 * 按照亲密度排序 
					 */
					setCurrentTab(2);
					if(mFragmentIn != null){
					    mFragmentIn.load(mOnlyNew);
					}
					break;
			}
		}
    };
    
    private void setCurrentTab(int index){
        if(mViewPager != null && (index >= 0 && index <= 2)){
            mViewPager.setCurrentItem(index);
            mCurrentTab = index;
        }
    }
    
    private int viewpageSelectedIndex = -1;
    private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener(){
		@Override
		public void onPageScrollStateChanged(int status) {
			if (status == 0) {
				if (viewpageSelectedIndex != -1) {
					if(mMsgTipLayout.getVisibility() == View.VISIBLE){
						mMsgTipLayout.setVisibility(View.GONE);
					}
					((RadioButton)mRadioGroup.getChildAt(viewpageSelectedIndex)).setChecked(true);
					viewpageSelectedIndex = -1;
				}
			}
		}
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
		@Override
		public void onPageSelected(int index) {
			viewpageSelectedIndex = index;
		}
    };
    
    private EgmCallBack mCallBack = new EgmCallBack(){
		@Override
		public void onPushMsgArrived(int transactionId, List<ChatItemInfo> obj) {
			if(mViewPager.getCurrentItem() != 0){
				mMsgTipLayout.setVisibility(View.VISIBLE);
			}
			setUnReadNum();
		}
    };
    
    @Override
	public void onPageSelected() {
		initTitle();
		long delta = System.currentTimeMillis() - mLastRefreshTime;
		if (delta > EgmConstants.TABSWITCH_REFRESH_INTERVAL) {
			mLastRefreshTime = System.currentTimeMillis();
			if (!mLoaded && mFragmentTime != null) {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						mFragmentTime.load(mOnlyNew);
						mLoaded = false;
					}
				}, 500);
			}
		}
		String path = BaseService.getServiceContext().getApplicationContext()
				.getFilesDir().getParent();
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
	}

	@Override
	public void onPageReSelected() {
	}
	
	@Override
	public void onTabDoubleTap() {
		if(mFragmentTime != null) {
			mFragmentTime.scrollToTop();
		}
	}
	
	class ChatFragmentPagerAdapter extends FragmentPagerAdapter {
		public ChatFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
		}
		@Override
		public Fragment getItem(int arg0) {
			switch (arg0) {
				case 0:
					return FragmentChatOrderTime.newInstance();
				case 1:
					return FragmentChatOrderRich.newInstance();
				case 2:
					return FragmentChatOrderIn.newInstance();
			}
			return null;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			switch(position){
			case 0:
				mFragmentTime = (FragmentChatOrderTime)super.instantiateItem(container, position);
				return mFragmentTime ;
			case 1:
				mFragmentRich = (FragmentChatOrderRich)super.instantiateItem(container, position);
				return mFragmentRich ;
			case 2:
				mFragmentIn = (FragmentChatOrderIn)super.instantiateItem(container, position);
				return mFragmentIn ;
			}
			return super.instantiateItem(container, position);
		}
		
		@Override
		public int getCount() {
			return 3;
		}
	} 
    
	@Override
	public void onDestroy() {
		super.onDestroy();
	}


}
