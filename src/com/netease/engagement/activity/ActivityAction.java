package com.netease.engagement.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.widget.LinearLayout;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentAction;

public class ActivityAction extends ActivityEngagementBase{

	public static void startActivity(Context context,String url,String title){
		Intent intent = new Intent(context, ActivityAction.class);
        if(!(context instanceof Activity)){
        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(EgmConstants.BUNDLE_KEY.SYSTEM_ACTION_URL,url);
        intent.putExtra(EgmConstants.BUNDLE_KEY.SYSTEM_ACTION_TITLE,title);
        context.startActivity(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setCustomActionBar();
		
		String url = getIntent().getStringExtra(EgmConstants.BUNDLE_KEY.SYSTEM_ACTION_URL);
		if(TextUtils.isEmpty(url)){
			finish();
			return ;
		}
		String title = getIntent().getStringExtra(EgmConstants.BUNDLE_KEY.SYSTEM_ACTION_TITLE);
		
		 LinearLayout linear = new LinearLayout(this);
	        linear.setId(R.id.activity_action_container_id);
	        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
	                LinearLayout.LayoutParams.MATCH_PARENT,
	                LinearLayout.LayoutParams.MATCH_PARENT);
	        linear.setLayoutParams(lp);
	        setContentView(linear);

	        if (findViewById(R.id.activity_action_container_id) != null
	                && savedInstanceState == null) {
	            FragmentManager fm = getSupportFragmentManager();
	            FragmentTransaction ft = fm.beginTransaction();
	            Fragment newFragment = FragmentAction.newInstance(url,title) ;
	            ft.add(R.id.activity_action_container_id, newFragment);
	            ft.commit();
	        }
	}
}
