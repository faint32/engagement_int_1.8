package com.netease.engagement.activity;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentChatPriPicList;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.widget.FrameLayout;

public class ActivityChatPriPicList extends ActivityEngagementBase{
	public static void startActivity(Context context,String uid) {
        Intent intent = new Intent(context, ActivityChatPriPicList.class);
        intent.putExtra(EgmConstants.BUNDLE_KEY.USER_ID, uid);
        if(!(context instanceof Activity)){
        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }
	
	private String mUid ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getActionBar().hide();
		
		mUid = this.getIntent().getStringExtra(EgmConstants.BUNDLE_KEY.USER_ID);
		if(TextUtils.isEmpty(mUid)){
			finish();
		}
		
		FrameLayout container = new FrameLayout(this);
		container.setId(R.id.activity_chat_pri_piclist_container_id);
		setContentView(container);
		
		FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
		Fragment newFragment = FragmentChatPriPicList.newInstance(mUid) ;
		ft.replace(R.id.activity_chat_pri_piclist_container_id, newFragment);
		ft.commit();
	}
}
