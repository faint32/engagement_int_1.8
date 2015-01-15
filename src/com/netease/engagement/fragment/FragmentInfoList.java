package com.netease.engagement.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityChoiceList;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.activity.ActivityHome;
import com.netease.engagement.adapter.ChoiceListAdapter;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.ConfigDataManager;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.engagement.widget.UserInfoUtil;
import com.netease.service.protocol.meta.UserInfo;
import com.netease.service.protocol.meta.UserInfoConfig;

public class FragmentInfoList extends FragmentBase{
	
	public static FragmentInfoList newInstance(int tagId,UserInfo userInfo){
		FragmentInfoList fragment = new FragmentInfoList();
		Bundle args = new Bundle();
		args.putInt(EgmConstants.BUNDLE_KEY.SELF_PAGE_CHOICE, tagId);
		args.putParcelable(EgmConstants.BUNDLE_KEY.SELF_PAGE_USERINFO,userInfo);
		fragment.setArguments(args);
		return fragment ;
	}
	
	private ListView mListView ;
	private int mTag ;
	private UserInfo mUserInfo ;
	private UserInfoConfig config ;
	private List<String> mTagNames ;
	private List<String> mChoosedTags ;
	private ChoiceListAdapter mAdapter ;
	
	private CustomActionBar mCustomActionBar ;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle args = getArguments();
		if(args == null 
				|| args.getInt(EgmConstants.BUNDLE_KEY.SELF_PAGE_CHOICE, 0) == 0
				|| args.getParcelable(EgmConstants.BUNDLE_KEY.SELF_PAGE_USERINFO) == null){
			return ;
		}
		mTag = args.getInt(EgmConstants.BUNDLE_KEY.SELF_PAGE_CHOICE);
		mUserInfo = args.getParcelable(EgmConstants.BUNDLE_KEY.SELF_PAGE_USERINFO);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
	    initTitle();
	    init();
	    ActivityChoiceList home = (ActivityChoiceList)getActivity();
        if(home != null){
            home.setFragment(this);
        }
	}
	
	private void initTitle(){
		mCustomActionBar = ((ActivityEngagementBase)getActivity()).getCustomActionBar();
		mCustomActionBar.getCustomView().setBackgroundColor(getResources().getColor(R.color.pri_info_choice_title_color));
		
		mCustomActionBar.setLeftClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				setResult();
			}
		});
		
		mCustomActionBar.setMiddleTitle(R.string.detail_info);
		mCustomActionBar.setMiddleTitleColor(getResources().getColor(R.color.black));
		mCustomActionBar.setMiddleTitleSize(20);
		mCustomActionBar.hideRightTitle();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		mListView = (ListView) inflater.inflate(R.layout.view_common_listview,null);
		return mListView ;
	}
	
	private void init(){
		config = ConfigDataManager.getInstance().getUConfigFromData();
		if(config == null){
			return ;
		}
		mChoosedTags = new ArrayList<String>();
		
		switch(mTag){
			case ActivityChoiceList.FAVOR_DATE:
				mCustomActionBar.setMiddleTitle(R.string.favor_date);
				mTagNames = UserInfoUtil.getDateList(config);
				for(String item : UserInfoUtil.getDateText(mUserInfo, config).split("，")){ // 注意是中文的逗号
					if(!item.equalsIgnoreCase(""))
						mChoosedTags.add(item);
				}
				break;
			case ActivityChoiceList.INTEREST:
				mCustomActionBar.setMiddleTitle(R.string.interest_hobby);
				if(mUserInfo.sex == 0){
					mTagNames = UserInfoUtil.getFemaleHobbyList(config);
					for(String item : UserInfoUtil.getFemaleHobbyText(mUserInfo, config).split("，")){
						if(!item.equalsIgnoreCase(""))
							mChoosedTags.add(item);
					}
				}else if(mUserInfo.sex == 1){
					mTagNames = UserInfoUtil.getMaleHobbyList(config);
					//使用这个方法生成的list，在add和remove的时候会 java.lang.UnsupportedOperationException
					//mChoosedTags = Arrays.asList(UserInfoUtil.getMaleHobbyText(mUserInfo, config).split(","));
					for(String item : UserInfoUtil.getMaleHobbyText(mUserInfo, config).split("，")){
						if(!item.equalsIgnoreCase(""))
							mChoosedTags.add(item);
					}
				}
				break;
			case ActivityChoiceList.SKILL:
				if(mUserInfo.sex == 0){
					mCustomActionBar.setMiddleTitle(R.string.wanna_skill);
				}else if(mUserInfo.sex ==1){
					mCustomActionBar.setMiddleTitle(R.string.adept_skill);
				}
				mTagNames = UserInfoUtil.getSkillList(config);
				for(String item : UserInfoUtil.getSkillText(mUserInfo, config).split("，")){
					if(!item.equalsIgnoreCase(""))
						mChoosedTags.add(item);
				}
				break;
		}
		mAdapter = new ChoiceListAdapter(mTagNames,mChoosedTags,getActivity());
		mListView.setAdapter(mAdapter);
	}
	
	public void setResult() {
		Intent intent = new Intent();
		int[] ids = null ;
		switch(mTag){
			case ActivityChoiceList.FAVOR_DATE:
				ids = UserInfoUtil.getFavorDateIds(mAdapter.getChoosedList(), config);
				break;
			case ActivityChoiceList.INTEREST:
				if(mUserInfo.sex == 0){
					ids = UserInfoUtil.getFemaleHobbyIds(mAdapter.getChoosedList(), config);
				}else if(mUserInfo.sex == 1){
					ids = UserInfoUtil.getMaleHobbyIds(mAdapter.getChoosedList(), config);
				}
				break;
			case ActivityChoiceList.SKILL:
				ids = UserInfoUtil.getSkillIds(mAdapter.getChoosedList(), config);
				break;
		}
		intent.putExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_DATA, ids);
		getActivity().setResult(Activity.RESULT_OK, intent);
		getActivity().finish();
	}
	
	/*@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == android.R.id.home){
			Intent intent = new Intent();
			int[] ids = null ;
			switch(mTag){
				case ActivityChoiceList.FAVOR_DATE:
					ids = UserInfoUtil.getFavorDateIds(mAdapter.getChoosedList(), config);
					break;
				case ActivityChoiceList.INTEREST:
					if(mUserInfo.sex == 0){
						ids = UserInfoUtil.getFemaleHobbyIds(mAdapter.getChoosedList(), config);
					}else if(mUserInfo.sex == 1){
						ids = UserInfoUtil.getMaleHobbyIds(mAdapter.getChoosedList(), config);
					}
					break;
				case ActivityChoiceList.SKILL:
					ids = UserInfoUtil.getSkillIds(mAdapter.getChoosedList(), config);
					break;
			}
			intent.putExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_DATA, ids);
			getActivity().setResult(Activity.RESULT_OK, intent);
			getActivity().finish();
		}
		return super.onOptionsItemSelected(item);
	}*/
}
