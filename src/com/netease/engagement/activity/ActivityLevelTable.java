package com.netease.engagement.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.widget.FrameLayout;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentBase;
import com.netease.engagement.fragment.FragmentEditPhoto;
import com.netease.engagement.fragment.FragmentGiftList;
import com.netease.engagement.fragment.FragmentLevelTable;
import com.netease.engagement.fragment.FragmentEditPhoto.PicUpLoadMode;

public class ActivityLevelTable extends ActivityEngagementBase{
	//等级查看表
	public static final int FRAGMENT_LEVEL = 100 ;
	//礼物列表
	public static final int FRAGMENT_GIFT = 101 ;
	//公开照片
	public static final int FRAGMENT_PUBLIC_PHOTO = 102;
	//私密照片
	public static final int FRAGMENT_PRIVATE_PHOTO = 103 ;
	
	private String mUserInfo ;
	private int fragmentTag ;
	
	public static void startActivity(FragmentBase fragment ,int fragmentTag ,String userInfo) {
        Intent intent = new Intent(fragment.getActivity(), ActivityLevelTable.class);
        if(!(fragment.getActivity() instanceof Activity)){
        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_USERINFO, userInfo);
        intent.putExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_LEVEL_TAG, fragmentTag);
        intent.putExtra(EgmConstants.BUNDLE_KEY.PIC_UPLOAD_MODE, PicUpLoadMode.DEFAULT_MODE);
        fragment.startActivity(intent);
    }
	
	public static void startActivityForResult(FragmentBase fragment ,int fragmentTag ,String userInfo) {
        Intent intent = new Intent(fragment.getActivity(), ActivityLevelTable.class);
        if(!(fragment.getActivity() instanceof Activity)){
        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_USERINFO, userInfo);
        intent.putExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_LEVEL_TAG, fragmentTag);
        intent.putExtra(EgmConstants.BUNDLE_KEY.PIC_UPLOAD_MODE, PicUpLoadMode.DEFAULT_MODE);
        fragment.startActivityForResult(intent,fragmentTag);
    }
	
    public static void startActivityForPicShowOff(Context context, int fragmentTag,String userInfo, int mode) {
        Intent intent = new Intent(context, ActivityLevelTable.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_USERINFO, userInfo);
        intent.putExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_LEVEL_TAG, fragmentTag);
        intent.putExtra(EgmConstants.BUNDLE_KEY.PIC_UPLOAD_MODE, mode);
        context.startActivity(intent);
    }

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);  
		
		this.getSupportActionBar().hide();
		
		FrameLayout container = new FrameLayout(this);
		container.setId(R.id.activity_level_container_id);
		setContentView(container);
		
		mUserInfo = this.getIntent().getStringExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_USERINFO);
		
		fragmentTag = this.getIntent().getIntExtra(EgmConstants.BUNDLE_KEY.SELF_PAGE_LEVEL_TAG, 0);
		if(fragmentTag == 0 ||TextUtils.isEmpty(mUserInfo)){
			finish();
		}
		
        if (this.getIntent().hasExtra(EgmConstants.BUNDLE_KEY.PIC_UPLOAD_MODE)) {
            
            picUpLoadMode = this.getIntent().getIntExtra(EgmConstants.BUNDLE_KEY.PIC_UPLOAD_MODE, 0);
        }
		if(savedInstanceState == null && findViewById(R.id.activity_level_container_id) != null){
			FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
			Fragment newFragment = null ;
			switch(fragmentTag){
				case FRAGMENT_LEVEL:
					/**
					 * 等级查看表 
					 */
					newFragment = FragmentLevelTable.newInstance(mUserInfo);
					break;
				case FRAGMENT_GIFT:
					/**
					 * 礼物列表
					 */
					newFragment = FragmentGiftList.newInstance(mUserInfo);
					break;
				case FRAGMENT_PUBLIC_PHOTO:
					/**
					 * 公开照列表
					 */
					newFragment = FragmentEditPhoto.newInstance(mUserInfo,false);
                    
					// 上传公开照片活动接口
					((FragmentEditPhoto)newFragment).setPicUpLoadMode(picUpLoadMode);
					
					break;
				case FRAGMENT_PRIVATE_PHOTO:
					/**
					 * 私照列表 
					 */
					newFragment = FragmentEditPhoto.newInstance(mUserInfo,true);
					
	                   // 上传公开照片活动接口
                    ((FragmentEditPhoto)newFragment).setPicUpLoadMode(picUpLoadMode);
					break; 
			}
			ft.replace(R.id.activity_level_container_id, newFragment);
			ft.commit();
		}
	}

    private int picUpLoadMode;
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
