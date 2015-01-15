package com.netease.engagement.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.widget.CustomActionBar;

/** 女性现金收入和提现历史记录 */
public class FragmentMoneyHistory extends FragmentBase {
    private ActivityEngagementBase mActivity;
    private ViewPager mViewPager;
    private RadioGroup mTab;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (ActivityEngagementBase)this.getActivity();
        
        CustomActionBar actionBar = mActivity.getCustomActionBar();
        actionBar.setLeftVisibility(View.VISIBLE);
        actionBar.setLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickBack();
            }
        });
        actionBar.setMiddleTitle(R.string.account_money_cash_history);
        actionBar.setRightVisibility(View.INVISIBLE);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_female_money_history, container, false);
        
        mViewPager = (ViewPager)view.findViewById(R.id.history_viewpager);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(new MoneyRecordFragmentAdapter(this.getChildFragmentManager()));
        mViewPager.setCurrentItem(0, true);
        mViewPager.setOnPageChangeListener(new OnPageChangeListener(){
            @Override
            public void onPageScrollStateChanged(int arg0) {}

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {}

            @Override
            public void onPageSelected(int arg0) {
                switch(arg0){
                    case 0:
                        mTab.check(R.id.history_tab_all);
                        break;
                    case 1:
                        mTab.check(R.id.history_tab_income);
                        break;
                    case 2:
                        mTab.check(R.id.history_tab_cash);
                        break;
                }
            }
        });
        
        mTab = (RadioGroup)view.findViewById(R.id.history_tab);
        mTab.check(R.id.history_tab_all);
        mTab.setOnCheckedChangeListener(new OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.history_tab_all:
                        mViewPager.setCurrentItem(0, true);
                        break;
                    case R.id.history_tab_income:
                        mViewPager.setCurrentItem(1, true);
                        break;
                    case R.id.history_tab_cash:
                        mViewPager.setCurrentItem(2, true);
                        break;
                }
            }
        });
        
        
        return view;
    }
    
    public class MoneyRecordFragmentAdapter extends FragmentPagerAdapter {
        public MoneyRecordFragmentAdapter(FragmentManager fm) {
            super(fm);
        }
        
        @Override
        public Fragment getItem(int arg0) {
            switch (arg0) {
                case 0:
                    return FragmentMoneyHistoryList.newInstance(EgmConstants.MoneyRecordType.ALL, R.string.account_money_history_list_head_all);
                case 1:
                    return FragmentMoneyHistoryList.newInstance(EgmConstants.MoneyRecordType.INCOME, R.string.account_money_history_list_head_income);
                case 2:
                    return FragmentMoneyHistoryList.newInstance(EgmConstants.MoneyRecordType.CASH, R.string.account_money_history_list_head_cash);
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    } 
}
