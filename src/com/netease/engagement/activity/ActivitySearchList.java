package com.netease.engagement.activity;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentSearchList;
import com.netease.engagement.widget.CustomActionBar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.LinearLayout;

public class ActivitySearchList extends ActivityEngagementBase {
	
	public static final String EXTRA_SEX_TYPE = "extra_sex_type";
	public static final int CONTAINER_ID = R.id.activity_search_list_container_id;

	public static void startActivity(Context context, int sexType, int ageBegin, int ageEnd, int constellation, 
            int provinceCode, boolean hasPrivatePic, int income){
        Intent intent = new Intent(context, ActivitySearchList.class);
        Bundle extra = new Bundle();
        extra.putInt(EgmConstants.EXTRA_SEARCH_SEX_TYPE, sexType);
        extra.putInt(EgmConstants.EXTRA_SEARCH_SEX_TYPE, sexType);
        extra.putInt(EgmConstants.EXTRA_SEARCH_AGE_BEGIN, ageBegin);
        extra.putInt(EgmConstants.EXTRA_SEARCH_AGE_END, ageEnd);
        extra.putInt(EgmConstants.EXTRA_SEARCH_CONSTE, constellation);
        extra.putInt(EgmConstants.EXTRA_SEARCH_PROVINCE, provinceCode);
        extra.putBoolean(EgmConstants.EXTRA_SEARCH_PRIVATE, hasPrivatePic);
        extra.putInt(EgmConstants.EXTRA_SEARCH_INCOME, income);
        intent.putExtras(extra);
        context.startActivity(intent);
    }
	
	@Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        
        super.setCustomActionBar();
        CustomActionBar actionBar = this.getCustomActionBar();
        actionBar.setLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	finish();
            }
        });
        actionBar.setMiddleTitle(R.string.search);
        actionBar.setRightVisibility(View.INVISIBLE);
        
        this.getWindow().setBackgroundDrawableResource(R.color.white);
        
        Bundle extra = getIntent().getExtras();
        int mSexType = extra.getInt(EgmConstants.EXTRA_SEARCH_SEX_TYPE);
        int mAgeBegin = extra.getInt(EgmConstants.EXTRA_SEARCH_AGE_BEGIN);
        int mAgeEnd = extra.getInt(EgmConstants.EXTRA_SEARCH_AGE_END);
        int mConstellation = extra.getInt(EgmConstants.EXTRA_SEARCH_CONSTE);
        int mIncome = extra.getInt(EgmConstants.EXTRA_SEARCH_INCOME);
        int mProvinceCode = extra.getInt(EgmConstants.EXTRA_SEARCH_PROVINCE);
        boolean mHasPrivatePic = extra.getBoolean(EgmConstants.EXTRA_SEARCH_PRIVATE);
        
        
        
        
        LinearLayout linear = new LinearLayout(this);
        linear.setId(CONTAINER_ID);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        linear.setLayoutParams(lp);
        setContentView(linear);

        if (findViewById(CONTAINER_ID) != null && savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            FragmentSearchList fragment = FragmentSearchList.newInstance(mSexType, mAgeBegin, mAgeEnd, 
            		mConstellation, mProvinceCode, mHasPrivatePic, mIncome);
            ft.add(CONTAINER_ID, fragment);
            ft.commit();
        }
        
    }

}
