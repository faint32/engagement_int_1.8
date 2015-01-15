package com.netease.engagement.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.netease.common.log.NTLog;
import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentHome;
import com.netease.engagement.fragment.FragmentInfoList;
import com.netease.service.protocol.meta.UserInfo;

public class ActivityChoiceList extends ActivityEngagementBase{

	public static final int FAVOR_DATE = 1 ;
	public static final int INTEREST = 2 ;
	public static final int SKILL = 3 ;
	
	private int mTag ;
	private UserInfo mUserInfo ;
	
	FragmentInfoList mFragment;
	
	public static void startActivity(Context context ,int tagId,UserInfo userInfo) {
        Intent intent = new Intent(context, ActivityChoiceList.class);
        if(!(context instanceof Activity)){
        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_CHOICE, tagId);
        intent.putExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_USERINFO, userInfo);
        ((Activity)context).startActivityForResult(intent,tagId);
        ((Activity)context).overridePendingTransition(R.anim.push_right_in,R.anim.keep_still);
    }
	
	public static void lunch(Fragment fragment ,int tagId ,UserInfo userInfo){
		Intent intent = new Intent(fragment.getActivity(), ActivityChoiceList.class);
		intent.putExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_CHOICE, tagId);
	    intent.putExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_USERINFO, userInfo);
	    fragment.startActivityForResult(intent,tagId);
	    fragment.getActivity().overridePendingTransition(R.anim.push_right_in,R.anim.keep_still);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setCustomActionBar();
		
		mTag = this.getIntent().getIntExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_CHOICE, 0);
		mUserInfo = this.getIntent().getParcelableExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_USERINFO);
		if(mTag == 0 || mUserInfo == null){
			this.finish();
		}
		
		FrameLayout container = new FrameLayout(this);
		container.setId(R.id.activity_choice_list_container_id);
		setContentView(container);
		
		if(findViewById(R.id.activity_choice_list_container_id) != null && savedInstanceState == null){
			mFragment = FragmentInfoList.newInstance(mTag, mUserInfo);
			FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.activity_choice_list_container_id, mFragment).commit();
		}
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(0, R.anim.push_right_out);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
			if(mFragment != null){
				mFragment.setResult();
			}
			return true;
		} else {
			return super.dispatchKeyEvent(event);
		}
	}
	 public void setFragment(FragmentInfoList fragment){
	        if(fragment != null){
	        		mFragment = fragment;
	        }
	}
}
