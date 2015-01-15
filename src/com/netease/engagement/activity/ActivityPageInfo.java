package com.netease.engagement.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentBase;
import com.netease.engagement.fragment.FragmentCharmMiji;
import com.netease.engagement.fragment.FragmentDetailInfo;
import com.netease.engagement.fragment.FragmentSelfIntroduce;
import com.netease.service.protocol.meta.UserInfo;

public class ActivityPageInfo extends ActivityEngagementBase{
	
	private String mTagId ;
	private UserInfo mUserInfo ; 
	private InputMethodManager  mManager;
	
	public static final int ACCOUNT = 1 ;
	public static final int INTRODUCE = 2 ;
	public static final int DETAIL_INFO = 3 ;
	public static final int CHARM_STRATEGY = 4 ;
	public static final int INVITE = 5 ;
	
	public static void startActivity(FragmentBase fragment ,String tagId, UserInfo userInfo) {
        Intent intent = new Intent(fragment.getActivity(), ActivityPageInfo.class);
        if(!(fragment.getActivity() instanceof Activity)){
        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_TAGID, tagId);
        intent.putExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_USERINFO, userInfo);
        fragment.startActivity(intent);
    }
	
	public static void startActivityForResult(FragmentBase fragment ,String tagId, UserInfo userInfo) {
        Intent intent = new Intent(fragment.getActivity(), ActivityPageInfo.class);
        if(!(fragment.getActivity() instanceof Activity)){
        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_TAGID, tagId);
        intent.putExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_USERINFO, userInfo);
        fragment.startActivityForResult(intent, Integer.parseInt(tagId));
    }
	
	public static void startActivity(Context context, UserInfo userInfo) {
        Intent intent = new Intent(context, ActivityPageInfo.class);
        if(!(context instanceof Activity)){
        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_USERINFO, userInfo);
        context.startActivity(intent);
    }
	
	public static void startActivity(Context context, String tagId, UserInfo userInfo) {
        Intent intent = new Intent(context, ActivityPageInfo.class);
        if(!(context instanceof Activity)){
        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_TAGID, tagId);
        intent.putExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_USERINFO, userInfo);
        context.startActivity(intent);
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setCustomActionBar();
		
		mTagId = this.getIntent().getStringExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_TAGID);
		mUserInfo = this.getIntent().getParcelableExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_USERINFO);
		
		if(TextUtils.isEmpty(mTagId)){
			return ;
		}
		
		FrameLayout container = new FrameLayout(this);
		container.setId(R.id.activity_pageinfo_container_id);
		setContentView(container);
		
		int index = Integer.parseInt(mTagId);
		FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
		Fragment newFragment = null ;
		
		switch(index){
			case ACCOUNT:
				/**
				 * 账户 
				 */
				break;
			case INTRODUCE:
				/**
				 * 自我介绍
				 */
				if(mUserInfo != null){
					newFragment = FragmentSelfIntroduce.newInstance(mUserInfo.introduce);
				}else{
					newFragment = FragmentSelfIntroduce.newInstance("");
				}
				break;
			case DETAIL_INFO:
				/**
				 * 详细资料
				 */
				newFragment = FragmentDetailInfo.newInstance(mUserInfo);
				break;
			case CHARM_STRATEGY:
				/**
				 * 魅力秘籍
				 */
				if(mUserInfo != null) {
					newFragment = FragmentCharmMiji.newInstance(mUserInfo.introduceType);
				} else {
					newFragment = FragmentCharmMiji.newInstance(0);
				}
				break;
			case INVITE:
				/**
				 * 邀请朋友
				 */
				break;
		}
		ft.replace(R.id.activity_pageinfo_container_id, newFragment);
		ft.commit();
		mManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	@Override  
    public boolean onTouchEvent(MotionEvent event) {  
     if(event.getAction() == MotionEvent.ACTION_DOWN){  
        if(mManager != null && getCurrentFocus()!=null && getCurrentFocus().getWindowToken()!=null){  
           mManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }  
     }  
     return super.onTouchEvent(event);  
    }  
}
