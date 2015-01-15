package com.netease.android.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Window;
import android.view.WindowManager;

import com.netease.android.video.ui.CamcorderFragment;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;

public class CamcorderActivity extends ActivityEngagementBase {
	
	public static void startActivity(Context context){
		Intent intent = new Intent();
		intent.setClass(context,CamcorderActivity.class);
		context.startActivity(intent);
	}
	
	private CamcorderFragment camcorder;
	
	@Override
	protected void onCreate(Bundle bundle) {
		setOveride(false);
		super.onCreate(bundle);
		overridePendingTransition(R.anim.video_slide_from_bottom, R.anim.video_fade_0);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		getWindow().addFlags(4096);
		setContentView(R.layout.video_activity_capture);
		
		FragmentManager fm = getSupportFragmentManager();
		if (fm.findFragmentById(R.id.layout_container_main) == null) {
			camcorder = new CamcorderFragment();
//			camcorder.setQueueId(getIntent().getStringExtra("queueId"));
			fm.beginTransaction()
					.replace(R.id.layout_container_main, camcorder).commit();
		}
	}
	@Override
	protected void requestFeature() {
		 // 设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
              WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.video_hold, R.anim.video_slide_to_bottom);
	}
	@Override
	public void onBackPressed() {
		if(camcorder != null && camcorder.onBackPressed()){
			return;
		}
		super.onBackPressed();
	}
}
