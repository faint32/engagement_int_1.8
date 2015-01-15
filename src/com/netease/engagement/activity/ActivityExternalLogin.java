package com.netease.engagement.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.LinearLayout;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants.BUNDLE_KEY;
import com.netease.engagement.fragment.FragmentExternalLogin;

public class ActivityExternalLogin extends ActivityEngagementBase {
	
	 public static void startActivity(Context context, int type) {
	        Intent i = new Intent(context, ActivityExternalLogin.class);
	        i.putExtra(BUNDLE_KEY.EXTERNAL_LOGIN_ACC_TYPE, type);
	        if(!(context instanceof Activity))
	            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        context.startActivity(i);
	    }
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setCustomActionBar();
//		 this.getWindow().setBackgroundDrawableResource(R.color.white);
		 LinearLayout linear = new LinearLayout(this);
	     linear.setId(R.id.activity_external_login_container_id);
	     LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
	                LinearLayout.LayoutParams.MATCH_PARENT,
	                LinearLayout.LayoutParams.MATCH_PARENT);
	        linear.setLayoutParams(lp);
	        setContentView(linear);

	        if (findViewById(R.id.activity_external_login_container_id) != null
	                && savedInstanceState == null) {
	            FragmentManager fm = getSupportFragmentManager();
	            FragmentTransaction ft = fm.beginTransaction();
	            FragmentExternalLogin fragment = new FragmentExternalLogin();
	            ft.add(R.id.activity_external_login_container_id, fragment);
	            ft.commit();
	        }
	}
}
