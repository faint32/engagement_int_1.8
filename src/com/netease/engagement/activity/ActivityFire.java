package com.netease.engagement.activity;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentFireAudio;
import com.netease.engagement.fragment.FragmentFireImage;
import com.netease.engagement.fragment.FragmentFireVideo;
import com.netease.service.protocol.meta.MessageInfo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

public class ActivityFire extends ActivityProximitySensorBase {
	
	private static final int FIRE_TYPE_AUDIO = 1;
	private static final int FIRE_TYPE_VIDEO = 2;
	private static final int FIRE_TYPE_IMAGE = 3;
	
	public static void startActivityForAudio(Context context, MessageInfo msgInfo){
        Intent intent = new Intent(context, ActivityFire.class);
        if(!(context instanceof Activity)){
        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(EgmConstants.BUNDLE_KEY.MESSAGE_INFO, msgInfo);
        intent.putExtra(EgmConstants.BUNDLE_KEY.FIRE_TYPE, FIRE_TYPE_AUDIO);
        ((Activity)context).startActivityForResult(intent, ActivityPrivateSession.REQUEST_FIRE_AUDIO_OR_VIDEO);
	}
	
	public static void startActivityForVideo(Context context, MessageInfo msgInfo){
        Intent intent = new Intent(context, ActivityFire.class);
        if(!(context instanceof Activity)){
        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(EgmConstants.BUNDLE_KEY.MESSAGE_INFO, msgInfo);
        intent.putExtra(EgmConstants.BUNDLE_KEY.FIRE_TYPE, FIRE_TYPE_VIDEO);
        ((Activity)context).startActivityForResult(intent, ActivityPrivateSession.REQUEST_FIRE_AUDIO_OR_VIDEO);
	}
	
	public static void startActivityForImage(Context context, MessageInfo msgInfo, long startTime){
        Intent intent = new Intent(context, ActivityFire.class);
        if(!(context instanceof Activity)){
        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(EgmConstants.BUNDLE_KEY.MESSAGE_INFO, msgInfo);
        intent.putExtra(EgmConstants.BUNDLE_KEY.FIRE_TYPE, FIRE_TYPE_IMAGE);
        intent.putExtra(EgmConstants.BUNDLE_KEY.FIRE_START_TIME, startTime);
        ((Activity)context).startActivity(intent);
	}

	
	private MessageInfo msgInfo;
	private int fireType;
	private long startTime;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setOveride(false);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
//		super.setCustomActionBar();
		
		msgInfo = (MessageInfo) getIntent().getSerializableExtra(EgmConstants.BUNDLE_KEY.MESSAGE_INFO);
		fireType = getIntent().getIntExtra(EgmConstants.BUNDLE_KEY.FIRE_TYPE, 0);
		startTime = getIntent().getLongExtra(EgmConstants.BUNDLE_KEY.FIRE_START_TIME, 0);
		
		FrameLayout container = new FrameLayout(this);
		container.setId(R.id.activity_fire_container_id);
		setContentView(container);
		
		if(findViewById(R.id.activity_fire_container_id) != null && savedInstanceState == null){
			if (fireType == FIRE_TYPE_AUDIO) {
//				getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
				FragmentFireAudio fragment = FragmentFireAudio.newInstance(msgInfo);
				FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.activity_fire_container_id, fragment).commit();
			} else if (fireType == FIRE_TYPE_VIDEO) {
//				getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
				FragmentFireVideo fragment = FragmentFireVideo.newInstance(msgInfo);
				FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.activity_fire_container_id, fragment).commit();
			} else if (fireType == FIRE_TYPE_IMAGE) {
				
				FragmentFireImage fragment = FragmentFireImage.newInstance(msgInfo, startTime);
				FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.activity_fire_container_id, fragment).commit();
			}
		}
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (fireType == FIRE_TYPE_AUDIO || fireType == FIRE_TYPE_VIDEO) {
				Toast.makeText(this, R.string.fire_first, Toast.LENGTH_SHORT).show();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
}
