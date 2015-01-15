package com.netease.engagement.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentBase;
import com.netease.engagement.fragment.FragmentPrivateSession;
import com.netease.engagement.pushMsg.NotificationBarMgr;
import com.netease.engagement.view.ShareDialog;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.meta.ChatItemUserInfo;

public class ActivityPrivateSession extends ActivityProximitySensorBase{
	
	public static final int REQUEST_FIRE_AUDIO_OR_VIDEO = 555;
	
	private static String FROM_USERINFO = "FROM_USERINFO";
	private static String SKILL_ID = "SKILL_ID";
	
	private static final String TAG = ActivityPrivateSession.class.getSimpleName() ;
	private FragmentPrivateSession fragment ;
	
	public static void startActivity(Context context) {
        Intent intent = new Intent(context, ActivityPrivateSession.class);
        
        context.startActivity(intent);
    }
	
	public static void startActivity(Context context ,ChatItemUserInfo userInfo){
		Intent intent = new Intent(context,ActivityPrivateSession.class);
		intent.putExtra(EgmConstants.BUNDLE_KEY.CHAT_ITEM_USER_INFO, userInfo);
		context.startActivity(intent);
	}
	
	public static void startActivityFromUserinfo(Context context ,ChatItemUserInfo userInfo){
		Intent intent = new Intent(context,ActivityPrivateSession.class);
		intent.putExtra(EgmConstants.BUNDLE_KEY.CHAT_ITEM_USER_INFO, userInfo);
		intent.putExtra(FROM_USERINFO, true);
		context.startActivity(intent);
	}
	
	public static void startActivityFromUserinfo(Context context ,ChatItemUserInfo userInfo, String skillId){
		Intent intent = new Intent(context,ActivityPrivateSession.class);
		intent.putExtra(EgmConstants.BUNDLE_KEY.CHAT_ITEM_USER_INFO, userInfo);
		intent.putExtra(FROM_USERINFO, true);
		intent.putExtra(SKILL_ID, skillId);
		context.startActivity(intent);
	}
	
	public static void startActivity(FragmentBase fragment ,ChatItemUserInfo userInfo){
        Intent intent = new Intent(fragment.getActivity(),ActivityPrivateSession.class);
        intent.putExtra(EgmConstants.BUNDLE_KEY.CHAT_ITEM_USER_INFO, userInfo);
        fragment.startActivity(intent);
    }
    
	private ChatItemUserInfo mUserInfo ;
	private boolean isFromUserInfo;
	private boolean isFromScreenShotNotificatoin;
	private String skillId;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        super.setCustomActionBar();
        
        if(this.getIntent() == null || 
        		this.getIntent().getParcelableExtra(EgmConstants.BUNDLE_KEY.CHAT_ITEM_USER_INFO) == null){
        	this.finish() ;
        	return;
        }
        mUserInfo = this.getIntent().getParcelableExtra(EgmConstants.BUNDLE_KEY.CHAT_ITEM_USER_INFO);
        isFromUserInfo = this.getIntent().getBooleanExtra(FROM_USERINFO, false);
        skillId = this.getIntent().getStringExtra(SKILL_ID);
        
        isFromScreenShotNotificatoin  = this.getIntent().getBooleanExtra(EgmConstants.BUNDLE_KEY.CHAT_FROM_SCREEN_SHOT_NOTIFICATION, false);
        if (isFromScreenShotNotificatoin) {
        	int uid = (int) mUserInfo.uid;
        	EgmPrefHelper.putSessionHasScreenShoteFlag(this, uid, false);
        } else {
        	int uid = (int) mUserInfo.uid;
        	boolean hasScreenShot = EgmPrefHelper.getSessionHasScreenShoteFlag(this, uid);
        	if (hasScreenShot) {
        		isFromScreenShotNotificatoin = true;
        		EgmPrefHelper.putSessionHasScreenShoteFlag(this, uid, false);
        		NotificationBarMgr.getInstance(this).cancelScreenShot(uid);
        	}
        }
        
        FrameLayout linear = new FrameLayout(this);
        linear.setId(R.id.activity_private_session_id);
        setContentView(linear);
        
        if(findViewById(R.id.activity_private_session_id) != null && savedInstanceState == null){
            fragment = FragmentPrivateSession.newInstance(mUserInfo, isFromUserInfo, isFromScreenShotNotificatoin, skillId);
            fragment.setActivityPrivateSession(this);
            getSupportFragmentManager().beginTransaction().replace(R.id.activity_private_session_id, fragment, TAG).commit();
        }
    }
    
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(fragment != null){
			fragment.dispatchTouchEvent(ev);
		}
		return super.dispatchTouchEvent(ev);
	}
	
	// 增加对back键的监测，并指派给fragment处理  bug fix #140783  by gzlichangjie
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && fragment != null) {
			if(fragment.onBackKeyDown()) {
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (ShareDialog.mSsoHandler != null) {
        	ShareDialog.mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
        
        if (requestCode == REQUEST_FIRE_AUDIO_OR_VIDEO) {
        	fragment.returnFromFireAudioOrVideo();
        }
    }
	
	public void forbidScreenShot() {
//		Toast.makeText(this, "forbidScreenShot", Toast.LENGTH_SHORT).show();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
		View view = getWindow().peekDecorView();
		if (view != null) {
			getWindowManager().updateViewLayout(view, getWindow().getAttributes());
		}
	}
	
	public void admitScreenShot() {
//		Toast.makeText(this, "admitScreenShot", Toast.LENGTH_SHORT).show();
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
		View view = getWindow().peekDecorView();
		if (view != null) {
			getWindowManager().updateViewLayout(view, getWindow().getAttributes());
		}
	}
}
