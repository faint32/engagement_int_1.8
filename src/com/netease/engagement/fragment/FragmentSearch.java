package com.netease.engagement.fragment;

import net.simonvt.numberpicker.NumberPicker;
import net.simonvt.numberpicker.NumberPicker.Formatter;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.ConfigDataManager;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.framework.widget.DialogUtil;
import com.netease.service.Utils.AreaTable;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.meta.OptionInfo;
import com.netease.service.protocol.meta.UserInfoConfig;

/** 搜索入口页面 */
public class FragmentSearch extends FragmentBase {
	
	private FragmentHomeRecommend fragmentHomeRecommend;
    
    public LinearLayout searchBgLL;
    private TextView mInputAge;
    private TextView mInputAstro;
    private TextView mInputProvince;
    private CheckBox mInputPrivatePic;
    private TextView mInputIncome;
    private AlertDialog mAgeDialog;
    
    private ActivityEngagementBase mContext;
    private int mSexType = EgmConstants.SexType.Female;
    private UserInfoConfig mUserInfoConfig;
    
    // 搜索条件初始值
    private int mLowAge = EgmConstants.SEARCH_START_AGE_DEFAULT, mHeighAge = EgmConstants.SEARCH_END_AGE_DEFAULT;
    private OptionInfo mAstroInfo, mProvinceInfo, mIncomeInfo;
    
    // 搜索条件选择值列表
    private OptionInfo[] mAstroDatas;
    private OptionInfo[] mIncomeDatas;
    private OptionInfo[] mProvinceDatas;  // 存储的是  省名－index下标索引号
    
    public static FragmentSearch newInstance(int sexType){
        FragmentSearch f = new FragmentSearch();
        
        Bundle extra = new Bundle();
        extra.putInt(EgmConstants.EXTRA_SEX_TYPE, sexType);
        f.setArguments(extra);
        
        return f;
    }
    
    public void setFragmentHomeRecommend(FragmentHomeRecommend fragmentHomeRecommend) {
		this.fragmentHomeRecommend = fragmentHomeRecommend;
	}

	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mContext = (ActivityEngagementBase)this.getActivity();
        mUserInfoConfig = ConfigDataManager.getInstance().getUConfigFromData();
        
        Bundle extra = this.getArguments();
        mSexType = extra.getInt(EgmConstants.EXTRA_SEX_TYPE, EgmConstants.SexType.Female);
        
        initData();
        restoreData();
    }
    
    @Override
    public void onResume(){
        super.onResume();
        
        mInputAstro.setText(mAstroInfo.value);
        mInputProvince.setText(mProvinceInfo.value);
        mInputIncome.setText(mIncomeInfo.value);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        
        CustomActionBar actionBar = ((ActivityEngagementBase)getActivity()).getCustomActionBar();
        actionBar.setMiddleTitle(R.string.search);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_layout, container, false);
        
        searchBgLL = (LinearLayout)view.findViewById(R.id.search_bg);
        searchBgLL.setVisibility(View.INVISIBLE);
        searchBgLL.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
        
        // 年龄
        View ageItem = view.findViewById(R.id.search_item_age);
        mInputAge = (TextView)ageItem.findViewById(R.id.search_item_input_age);
        mInputAge.setText(mLowAge + "-" + mHeighAge);
        ageItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAgeDialog();
            }
        });
        
        // 星座
        View astroItem = view.findViewById(R.id.search_item_astro);
        mInputAstro = (TextView)astroItem.findViewById(R.id.search_item_input_astro);
        astroItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                gotoSelect(TYPE_ASTRO);
//            	Toast.makeText(mContext, "星座", Toast.LENGTH_SHORT).show();
            	showDialog(R.string.rec_search_item_astro);
            }
        });
        
        // 地区
        View areaItem = view.findViewById(R.id.search_item_area);
        mInputProvince = (TextView)areaItem.findViewById(R.id.search_item_input_area);
        areaItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                gotoSelect(TYPE_POSITION);
//            	Toast.makeText(mContext, "地区", Toast.LENGTH_SHORT).show();
            	showDialog(R.string.rec_search_item_area);
            }
        });
        
        // 私照
        View itemPrivatePic = view.findViewById(R.id.search_item_private_pic);
        mInputPrivatePic = (CheckBox)itemPrivatePic.findViewById(R.id.search_item_input_private_pic);
        mInputPrivatePic.setChecked(EgmPrefHelper.getSearchPrivate(mContext));
        
        // 收入
        View itemIncome = view.findViewById(R.id.search_item_income);
        mInputIncome = (TextView)itemIncome.findViewById(R.id.search_item_input_income);
        itemIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                gotoSelect(TYPE_INCOME);
//            	Toast.makeText(mContext, "收入", Toast.LENGTH_SHORT).show();
            	showDialog(R.string.rec_search_item_income);
            }
        });
        
        if(mSexType == EgmConstants.SexType.Female){    // 女性用户搜索男性
            itemPrivatePic.setVisibility(View.GONE);
        }
        else{
            itemIncome.setVisibility(View.GONE);
        }
        
        view.findViewById(R.id.search_search_btn).setOnClickListener(mClickSearch);
        
        return view;
    }
    
    private void initData(){
        String nolimit = getString(R.string.rec_search_no_limit);
        
        mAstroInfo = new OptionInfo(0, nolimit); 
        mProvinceInfo = new OptionInfo(0, nolimit);
        
        mAstroDatas = new OptionInfo[mUserInfoConfig.constellation.length + 1];
        mAstroDatas[0] = mAstroInfo;    // 增加“不限”
        for(int i = 1; i <= mUserInfoConfig.constellation.length; i++){
            mAstroDatas[i] = mUserInfoConfig.constellation[i - 1];
        }
        
        OptionInfo[] provinces = AreaTable.getAllProvinceOption(mContext);
        mProvinceDatas = new OptionInfo[provinces.length + 1];
        mProvinceDatas[0] = mProvinceInfo;
        for(int i = 1; i <= provinces.length; i++){
        	mProvinceDatas[i] = new OptionInfo(i, provinces[i - 1].value);
        }
        
        mIncomeDatas = mUserInfoConfig.searchIncome;
        mIncomeInfo = mIncomeDatas[0];
    }
    
    /** 存储本次的搜索条件 */
    private void storeData(){
        EgmPrefHelper.putSearchAgeStart(mContext, mLowAge);
        EgmPrefHelper.putSearchAgeEnd(mContext, mHeighAge);
        
        EgmPrefHelper.putSearchAstro(mContext, mAstroInfo.key);
        EgmPrefHelper.putSearchIncome(mContext, mIncomeInfo.key);
        EgmPrefHelper.putSearchPrivate(mContext, mInputPrivatePic.isChecked());
        
        EgmPrefHelper.putSearchArea(mContext, AreaTable.getProvinceIdByName(mContext, mProvinceInfo.value));
    }
    
    /** 恢复上一次的搜索条件 */
    private void restoreData(){
        mLowAge = EgmPrefHelper.getSearchAgeStart(mContext);
        mHeighAge = EgmPrefHelper.getSearchAgeEnd(mContext);
        
        int index = EgmPrefHelper.getSearchAstro(mContext);
        if(index >=0 && index < mAstroDatas.length){
            mAstroInfo = mAstroDatas[index];
        }
        
        int provinceCode = EgmPrefHelper.getSearchArea(mContext);
        String province;
        if(provinceCode == 0){  // 不限
            province = getString(R.string.rec_search_no_limit);;
        }
        else{
            province = AreaTable.getProvinceNameById(mContext, provinceCode);
        }
        for (int i=0; i<mProvinceDatas.length; i++) {
        	if (province.equalsIgnoreCase(mProvinceDatas[i].value)) {
        		mProvinceInfo = mProvinceDatas[i];
        	}
        }
        
        index = EgmPrefHelper.getSearchIncome(mContext);
        if(index >=0 && index < mIncomeDatas.length){
            mIncomeInfo = mIncomeDatas[index];
        }
    }
    
    /** 进入搜索结果页面 */
    private void gotoSearchListFragment(){
    	int provinceId = AreaTable.getProvinceIdByName(mContext, mProvinceInfo.value);
    	
    	if (fragmentHomeRecommend != null) {
    		fragmentHomeRecommend.gotoSearchList(mSexType, mLowAge, mHeighAge, 
                mAstroInfo.key, provinceId, mInputPrivatePic.isChecked(), mIncomeInfo.key);
    	}
    }
    
    private void showAgeDialog(){
        if(mAgeDialog == null){
            mAgeDialog = new AlertDialog.Builder(mContext).create();
            mAgeDialog.setCanceledOnTouchOutside(false);
            View layout = LayoutInflater.from(mContext).inflate(R.layout.view_age_picker_dialog, null, false);
            
            final NumberPicker mLowPicker, mHeighPicker;
            mLowPicker = (NumberPicker)layout.findViewById(R.id.search_age_picker_low);
            mLowPicker.setMinValue(EgmConstants.SEARCH_AGE_MIN);
            mLowPicker.setMaxValue(EgmConstants.SEARCH_AGE_MAX);
            mLowPicker.setValue(mLowAge);
            
            mHeighPicker = (NumberPicker)layout.findViewById(R.id.search_age_picker_heigh);
            mHeighPicker.setMinValue(EgmConstants.SEARCH_AGE_MIN);
            mHeighPicker.setMaxValue(EgmConstants.SEARCH_AGE_MAX);
            mHeighPicker.setValue(mHeighAge);
            
            View okBtn = layout.findViewById(R.id.search_select_age_ok);
            okBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLowAge = mLowPicker.getValue();
                    mHeighAge = mHeighPicker.getValue();
                    if(mLowAge > mHeighAge){
                        int t = mHeighAge;
                        mHeighAge = mLowAge;
                        mLowAge = t;
                    }
                    
                    mInputAge.setText(mLowAge + "-" + mHeighAge);
                    mAgeDialog.dismiss();
                }
            });
            View cancleBtn = layout.findViewById(R.id.search_select_age_cancel);
            cancleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAgeDialog.dismiss();
                }
            });
            
            mAgeDialog.setView(layout);
        }
        
        mAgeDialog.show();
    }
    
    private void showDialog(final int id){
		View view = null ;
		if(id == R.string.rec_search_item_income || id==R.string.rec_search_item_area) {
			view = mContext.getLayoutInflater().inflate(R.layout.view_choose_detail_info_income,null);
		} else {
			view = mContext.getLayoutInflater().inflate(R.layout.view_choose_detail_info, null);
		}
		final NumberPicker pickerView = (NumberPicker) view.findViewById(R.id.picker);
		TextView title = (TextView)view.findViewById(R.id.title);
		title.setText(getString(id));
		TextView cancel = (TextView)view.findViewById(R.id.cancel);
		TextView confirm = (TextView)view.findViewById(R.id.ok);
		ImageView up = (ImageView)view.findViewById(R.id.up);
		ImageView down = (ImageView)view.findViewById(R.id.down);
		
		switch(id){
			case R.string.rec_search_item_income:
				/**
				 * 收入
				 */
				pickerView.setMinValue(1);
				pickerView.setMaxValue(mIncomeDatas.length);
				pickerView.setValue(mIncomeInfo.key+1);
				pickerView.setFormatter(new Formatter() {
					@Override
					public String format(int value) {
						return mIncomeDatas[value - 1].value;
					}
				});
				break;
			case R.string.rec_search_item_astro:
				/**
				 * 星座
				 */
				pickerView.setMinValue(1);
				pickerView.setMaxValue(mAstroDatas.length);
				pickerView.setValue(mAstroInfo.key+1);
				pickerView.setFormatter(new Formatter() {
					@Override
					public String format(int value) {
						return mAstroDatas[value - 1].value;
					}
				});
				break;
			case R.string.rec_search_item_area:
				/**
				 * 地区
				 */
				pickerView.setMinValue(1);
				pickerView.setMaxValue(mProvinceDatas.length);
				pickerView.setValue(mProvinceInfo.key+1);
				pickerView.setFormatter(new Formatter() {
					@Override
					public String format(int value) {
						return mProvinceDatas[value - 1].value;
					}
				});
				break;	
		}
		
		final AlertDialog dialog = DialogUtil.newAlertDiag(mContext)
		.setView(view).show();
		
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		confirm.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view) {
				int result = pickerView.getValue();
				switch(id){
					case R.string.rec_search_item_income:
						mIncomeInfo = mIncomeDatas[result-1];
						mInputIncome.setText(mIncomeInfo.value);
						break;
					case R.string.rec_search_item_astro:
						mAstroInfo = mAstroDatas[result-1];
						mInputAstro.setText(mAstroInfo.value);
						break;
					case R.string.rec_search_item_area:
						mProvinceInfo = mProvinceDatas[result-1];
						mInputProvince.setText(mProvinceInfo.value);
						break;
				}
				dialog.dismiss();
			}
		});
		up.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pickerView.setValue(pickerView.getValue() -1);
			}
		});
		down.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pickerView.setValue(pickerView.getValue() + 1);
			}
		});
	}
	
    private View.OnClickListener mClickSearch = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            storeData();
            gotoSearchListFragment();
        }
    };
    
}
