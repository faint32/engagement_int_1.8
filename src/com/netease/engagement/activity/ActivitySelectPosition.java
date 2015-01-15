package com.netease.engagement.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.netease.date.R;
import com.netease.engagement.fragment.FragmentBase;
import com.netease.engagement.fragment.FragmentSelectPosition;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.service.Utils.AreaTable;

/**
 * 选择地理位置界面
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class ActivitySelectPosition extends ActivityEngagementBase{
    public static final String EXTRA_RESULT_PROVINCE_CODE = "extra_result_province_code";
    public static final String EXTRA_RESULT_CITY_CODE = "extra_result_city_code";
    public static final String EXTRA_RESULT_PROVINCE = "extra_result_province";
    public static final String EXTRA_RESULT_CITY = "extra_result_city";
    
    private final int CONTAINER_ID = R.id.activity_select_position_container_id;
    
    private Context mContext;
    private FragmentManager mFragmentManager;
    private String mProvince;
    private int mProvinceCode;
    
    public static void startActivityForResult(Activity activity, int requestCode){
        Intent intent = new Intent(activity, ActivitySelectPosition.class);
        
        activity.startActivityForResult(intent, requestCode);
    }
    
    public static void startActivityForResult(FragmentBase fragment, int requestCode){
        Intent intent = new Intent(fragment.getActivity(), ActivitySelectPosition.class);
        
        fragment.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mContext = this;
        mFragmentManager = getSupportFragmentManager();
        
        super.setCustomActionBar();
        CustomActionBar actionBar = this.getCustomActionBar();
        actionBar.setLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mFragmentManager != null && mFragmentManager.getBackStackEntryCount() > 0){
                    mFragmentManager.popBackStack();
                }
                else{
                    finish();
                }
            }
        });
        actionBar.setMiddleTitle(R.string.rec_select_position_title);
        actionBar.setRightVisibility(View.INVISIBLE);
        this.getWindow().setBackgroundDrawableResource(R.color.app_background);
        
        LinearLayout linear = new LinearLayout(this);
        linear.setId(CONTAINER_ID);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        linear.setLayoutParams(lp);
        setContentView(linear);
        
        if (findViewById(CONTAINER_ID) != null && savedInstanceState == null) {
            FragmentSelectPosition fragment = new FragmentSelectPosition();
            ArrayList<String> dataList = AreaTable.getAllProvinces(mContext);
            fragment.setDataList(dataList);
            fragment.setOnListItemClickListener(mClickItemProvince);
            
            mFragmentManager.beginTransaction().add(CONTAINER_ID, fragment).commit();
        }
    }
    
    private AdapterView.OnItemClickListener mClickItemProvince = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> list, View view, int position, long id) {
            mProvince = (String)list.getAdapter().getItem(position);
            mProvinceCode = AreaTable.getProvinceIdByName(mContext, mProvince);
            
            FragmentSelectPosition fragment = new FragmentSelectPosition();
            ArrayList<String> cityList = AreaTable.getAllCitys(mContext, mProvinceCode);
            
            if(cityList != null && cityList.size() > 0){    
                fragment.setDataList(cityList);
                fragment.setOnListItemClickListener(mClickItemCity);
                mFragmentManager.beginTransaction().replace(CONTAINER_ID, fragment).commit();
            }
            else{   // 只有省，没有市
                Intent intent = new Intent();
                intent.putExtra(EXTRA_RESULT_PROVINCE, mProvince);
                intent.putExtra(EXTRA_RESULT_PROVINCE_CODE, mProvinceCode);
                
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    };
    
    private AdapterView.OnItemClickListener mClickItemCity = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> list, View view, int position, long id) {
            String city = (String)list.getAdapter().getItem(position);
            int cityCode = AreaTable.getCityIdByName(mContext, mProvinceCode, city);
            
            Intent intent = new Intent();
            intent.putExtra(EXTRA_RESULT_PROVINCE, mProvince);
            intent.putExtra(EXTRA_RESULT_PROVINCE_CODE, mProvinceCode);
            intent.putExtra(EXTRA_RESULT_CITY_CODE, cityCode);
            intent.putExtra(EXTRA_RESULT_CITY, city);
            
            setResult(RESULT_OK, intent);
            finish();
        }
    };
    
}
