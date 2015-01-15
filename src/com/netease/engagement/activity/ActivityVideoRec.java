package com.netease.engagement.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.netease.date.R;
import com.netease.engagement.fragment.FragmentBase;
import com.netease.engagement.fragment.FragmentVideoRec;

public class ActivityVideoRec extends ActivityEngagementBase{
	
	private static final String EXTRA_TIME_LIMIT = "extra_time_limit";
	
	private FragmentVideoRec fragment;
	private int mTimeLimit;
	
	public static void startActivityForResult(FragmentBase fragment,
			int requestCode) {
		Intent intent = new Intent(fragment.getActivity(), ActivityVideoRec.class);
	    fragment.startActivityForResult(intent,requestCode);
	}
	
	public static void startActivityForResult(FragmentBase fragment, 
			int timeLimit, int requestCode) {
		Intent intent = new Intent(fragment.getActivity(), ActivityVideoRec.class);
		intent.putExtra(EXTRA_TIME_LIMIT, timeLimit);
	    fragment.startActivityForResult(intent,requestCode);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getSupportActionBar().hide();
		
		FrameLayout container = new FrameLayout(this);
		container.setId(R.id.activity_chat_video_rec_container_id);
		setContentView(container);
		
		Intent intent = getIntent();
		
		if(findViewById(R.id.activity_chat_video_rec_container_id) != null 
				&& savedInstanceState == null){
			fragment = FragmentVideoRec.newInstance();
			fragment.setTimeLimit(intent != null ? intent.getIntExtra(EXTRA_TIME_LIMIT, 0) : 0);
			FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.activity_chat_video_rec_container_id, fragment).commit();
		}
	}
	
	// 增加对back键的监测，并指派给fragment处理  bug fix #140783  by gzlichangjie
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && fragment != null) {
			fragment.onBackKeyDown();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}

