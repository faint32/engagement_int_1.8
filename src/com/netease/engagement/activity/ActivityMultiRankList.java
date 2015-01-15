package com.netease.engagement.activity;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentMultiRankList;
import com.netease.engagement.widget.CustomActionBar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class ActivityMultiRankList extends ActivityEngagementBase {
	
	public static final String EXTRA_RANK_ID = "extra_rank_id";
    public static final String EXTRA_RANK_NAME = "extra_rank_name";
    public static final String EXTRA_SEX_TYPE = "extra_sex_type";
    
    private int mRankId;
    private int mSexType;
    
    private ViewPager mViewPager;
    private RadioGroup mTab;
    
    public static void startActivity(Context context, int rankId, String rankName, int sexType){
        Intent intent = new Intent(context, ActivityMultiRankList.class);
        intent.putExtra(EXTRA_RANK_ID, rankId);
        intent.putExtra(EXTRA_RANK_NAME, rankName);
        intent.putExtra(EXTRA_SEX_TYPE, sexType);
        
        context.startActivity(intent);
    }


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle extra = this.getIntent().getExtras();
        String rankName = extra.getString(EXTRA_RANK_NAME, "");
        mRankId = extra.getInt(EXTRA_RANK_ID, -1);
        mSexType = extra.getInt(EXTRA_SEX_TYPE, EgmConstants.SexType.Female);
        
        if(mRankId < 0)
            return;
        
        super.setCustomActionBar();
        CustomActionBar actionBar = getCustomActionBar();
        actionBar.setMiddleTitle(rankName);
        actionBar.setLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        actionBar.setRightVisibility(View.INVISIBLE);
        this.getWindow().setBackgroundDrawableResource(R.color.white);
        
        setContentView(R.layout.activity_multi_rank_list);
        init();
	}
    
	private void init() {
		mViewPager = (ViewPager) findViewById(R.id.rank_viewpager);
		mViewPager.setAdapter(new MultiRankListFragmentAdapter(getSupportFragmentManager()));
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setCurrentItem(0, true);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener(){

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int arg0) {
				switch (arg0) {
				case 0:
					mTab.check(R.id.rank_tab_day);
					break;
				case 1:
					mTab.check(R.id.rank_tab_month);
					break;
				}
			}
			
		});
		
		mTab = (RadioGroup) findViewById(R.id.rank_tab);
		mTab.check(R.id.rank_tab_day);
		mTab.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.rank_tab_day:
					mViewPager.setCurrentItem(0, true);
					break;
				case R.id.rank_tab_month:
					mViewPager.setCurrentItem(1, true);
					break;
				}
			}
		});
	}
	
	public class MultiRankListFragmentAdapter extends FragmentPagerAdapter {

		public MultiRankListFragmentAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int arg0) {
			switch (arg0) {
			case 0:
				return FragmentMultiRankList.newInstance(mRankId, mSexType, EgmConstants.RankListType.RANK_LIST_DAY); //日榜
			case 1:
				return FragmentMultiRankList.newInstance(mRankId, mSexType, EgmConstants.RankListType.RANK_LIST_MONTH); //月榜
			}
			return null;
		}

		@Override
		public int getCount() {
			return 2;
		}
		
	}
    
}
