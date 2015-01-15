package com.netease.engagement.activity;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentBase;
import com.netease.engagement.fragment.FragmentDialogRecAudio;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.FrameLayout;

/**
 * 录制语音
 */
public class ActivityAudioRecorder extends ActivityEngagementBase{
	
	public static void startActivityForResult(FragmentBase fragment) {
        Intent intent = new Intent(fragment.getActivity(), ActivityAudioRecorder.class);
        fragment.startActivityForResult(intent,EgmConstants.REQUEST_RECORD_AUDIO);
    }
	
	public static void startActivity(Context context) {
        Intent intent = new Intent(context, ActivityAudioRecorder.class);
        if(!(context instanceof Activity)){
        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.getSupportActionBar().hide();
		
		FrameLayout container = new FrameLayout(this);
		container.setId(R.id.activity_audiorec_container_id);
		setContentView(container);
		
		FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
		Fragment newFragment = FragmentDialogRecAudio.newInstance() ;
		ft.replace(R.id.activity_audiorec_container_id, newFragment);
		ft.commit();
	}
}
