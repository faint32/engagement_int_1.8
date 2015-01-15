package com.netease.engagement.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.FrameLayout;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentBase;
import com.netease.engagement.fragment.FragmentVideoList;
import com.netease.engagement.fragment.FragmentEditPhoto.PicUpLoadMode;

/**
 * 从相册中选择视频
 */
public class ActivityVideoList extends ActivityEngagementBase{

	public static void startActivityForResult(FragmentBase fragment,int requestCode){
		Intent intent = new Intent();
		intent.setClass(fragment.getActivity(),ActivityVideoList.class);
		fragment.startActivityForResult(intent, requestCode);
	}
	
	public static void startActivityForResult(Activity context,int requestCode){
		Intent intent = new Intent();
		intent.setClass(context,ActivityVideoList.class);
		context.startActivityForResult(intent, requestCode);
	}
	public static void startActivityForResult(Activity context,int requestCode,int fromType){
		Intent intent = new Intent();
		intent.setClass(context,ActivityVideoList.class);
		intent.putExtra(EgmConstants.BUNDLE_KEY.SELECT_VIDEO_TYPE, fromType);
		context.startActivityForResult(intent, requestCode);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getSupportActionBar().hide();
		
		FrameLayout container = new FrameLayout(this);
		container.setId(R.id.activity_chat_video_list_container_id);
		setContentView(container);
		
		if(this.findViewById(R.id.activity_chat_video_list_container_id) != null && savedInstanceState == null){
			FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
			FragmentVideoList fragment = FragmentVideoList.newInstance();
			ft.replace(R.id.activity_chat_video_list_container_id, fragment).commit();
		}
	}
}
