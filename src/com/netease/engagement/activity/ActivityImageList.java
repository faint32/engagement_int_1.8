package com.netease.engagement.activity;

import java.util.ArrayList;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentBase;
import com.netease.engagement.fragment.FragmentImageListGirl;
import com.netease.engagement.fragment.FragmentImageListMan;
import com.netease.service.protocol.meta.PictureInfo;

public class ActivityImageList extends ActivityEngagementBase{
	
	private Fragment fragment = null;
	
	public static void startActivity(Context context ,
			long userId ,
			ArrayList<PictureInfo> pictureInfoList, int position) {
        Intent intent = new Intent(context, ActivityImageList.class);
        if(!(context instanceof Activity)){
        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(EgmConstants.BUNDLE_KEY.USER_ID,userId);
        intent.putParcelableArrayListExtra(EgmConstants.BUNDLE_KEY.IMAGE_URL, pictureInfoList);
        intent.putExtra(EgmConstants.BUNDLE_KEY.POSITION , position);
        context.startActivity(intent);
    }
	
	public static void startActivity(FragmentBase fragment ,
			long userId ,
			ArrayList<PictureInfo> pictureInfoList) {
        Intent intent = new Intent(fragment.getActivity(), ActivityImageList.class);
        intent.putExtra(EgmConstants.BUNDLE_KEY.USER_ID, userId);
        intent.putParcelableArrayListExtra(EgmConstants.BUNDLE_KEY.IMAGE_URL, pictureInfoList);
        fragment.startActivity(intent);
    }
	
	private long userId ;
	
	private ArrayList<PictureInfo> mPictureList ;
	
	private int position;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setCustomActionBar();
        
        FrameLayout linear = new FrameLayout(this);
        linear.setId(R.id.activity_image_list_container_id);
        setContentView(linear);
        
        mPictureList = getIntent().getParcelableArrayListExtra(EgmConstants.BUNDLE_KEY.IMAGE_URL);
        userId = getIntent().getLongExtra(EgmConstants.BUNDLE_KEY.USER_ID,-1);
        position = getIntent().getIntExtra(EgmConstants.BUNDLE_KEY.POSITION, 0);
        
        if (findViewById(R.id.activity_image_list_container_id) != null && savedInstanceState == null) {
            
            if(mPictureList == null){
            	//进入女性私照列表
            	fragment = FragmentImageListGirl.newInstance(userId, position);
            }else{
            	//进入男性公开照列表
            	fragment = FragmentImageListMan.newInstance(userId, mPictureList, position);
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.activity_image_list_container_id, fragment).commit();
        }
    }

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(fragment != null && fragment instanceof FragmentImageListGirl) {
			if(((FragmentImageListGirl)fragment).dispatchTouchEvent(ev)) {
				return true;
			}
		}
		return super.dispatchTouchEvent(ev);
	}
    
	
}
