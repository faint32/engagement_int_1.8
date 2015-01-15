package com.netease.engagement.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.app.EngagementApp;
import com.netease.engagement.fragment.FragmentBase;
import com.netease.engagement.fragment.FragmentUserPageGirl;
import com.netease.engagement.fragment.FragmentUserPageMan;
import com.netease.engagement.view.ShareDialog;
import com.netease.service.media.MediaPlayerWrapper;

public class ActivityUserPage extends ActivityEngagementBase{
    private static String EXTRA_IS_SHORTCUT = "extra_is_shortcut";
    private static String FROM_SESSION = "FROM_SESSION";
	
	public static void startActivity(FragmentBase fragment,String userid ,String gender) {
        Intent intent = new Intent(fragment.getActivity(), ActivityUserPage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra(EgmConstants.BUNDLE_KEY.USER_ID, userid);
        intent.putExtra(EgmConstants.BUNDLE_KEY.USER_GENDER, gender);
        fragment.startActivity(intent);
    }
	
	public static void startActivity(Context context,String userid ,String gender) {
        Intent intent = new Intent(context, ActivityUserPage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        if(!(context instanceof Activity)){
        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(EgmConstants.BUNDLE_KEY.USER_ID, userid);
        intent.putExtra(EgmConstants.BUNDLE_KEY.USER_GENDER, gender);
        context.startActivity(intent);
    }
	
	public static void startActivityFromSession(Context context,String userid ,String gender) {
        Intent intent = new Intent(context, ActivityUserPage.class);
        if(!(context instanceof Activity)){
        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(EgmConstants.BUNDLE_KEY.USER_ID, userid);
        intent.putExtra(EgmConstants.BUNDLE_KEY.USER_GENDER, gender);
        intent.putExtra(FROM_SESSION, true);
        context.startActivity(intent);
    }
	
	/** 从快捷方式进入，activity的进入和退出方式是从下到上和从上到下 */
	public static void startActivityFromShortcut(Activity activity, String userid, String gender) {
        Intent intent = new Intent(activity, ActivityUserPage.class);
        intent.putExtra(EgmConstants.BUNDLE_KEY.USER_ID, userid);
        intent.putExtra(EgmConstants.BUNDLE_KEY.USER_GENDER, gender);
        intent.putExtra(EXTRA_IS_SHORTCUT, true);
        activity.startActivity(intent);
        
        activity.overridePendingTransition(R.anim.push_up_in, R.anim.fake_fade_out);
    }
	
	/** 标记是否是从快捷方式进入的，如果是的话activity的进入和退出方式是从下到上和从上到下，与其它的不一样 */
	private boolean mIsFromeShortcut = false; 
	private boolean mIsFromSession = false;
	private Fragment newFragment = null ;
	private String userId ;
	private String gender ;
	private FrameLayout container ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setCustomActionBar();
		
		container = new FrameLayout(this);
		container.setId(R.id.activity_user_page_container_id);
		setContentView(container);
		
		init(container);
		
		if(this.getIntent() == null 
				|| TextUtils.isEmpty(getIntent().getStringExtra(EgmConstants.BUNDLE_KEY.USER_ID))
				|| TextUtils.isEmpty(getIntent().getStringExtra(EgmConstants.BUNDLE_KEY.USER_GENDER))){
			finish();
		}
		
		userId = getIntent().getStringExtra(EgmConstants.BUNDLE_KEY.USER_ID) ;
		gender = getIntent().getStringExtra(EgmConstants.BUNDLE_KEY.USER_GENDER);
		mIsFromeShortcut = getIntent().getBooleanExtra(EXTRA_IS_SHORTCUT, false);
		mIsFromSession = getIntent().getBooleanExtra(FROM_SESSION, false);
		MediaPlayerWrapper.getInstance().doBindService(EngagementApp.getAppInstance());
		if(this.findViewById(R.id.activity_user_page_container_id) != null && savedInstanceState == null){
			FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
			switch(Integer.parseInt(gender)){
			case EgmConstants.SexType.Female:
				newFragment = FragmentUserPageGirl.newInstance(userId, mIsFromeShortcut, mIsFromSession);
				break;
			case EgmConstants.SexType.Male:
				newFragment = FragmentUserPageMan.newInstance(userId, mIsFromSession);
				break;
			}
			ft.replace(R.id.activity_user_page_container_id, newFragment);
			ft.commit();
		}
	}
	
	public FragmentUserPageGirl getFragmentUserPageGirl(){
		if(newFragment != null && newFragment instanceof FragmentUserPageGirl){
			return (FragmentUserPageGirl)newFragment ;
		}
		return null ;
	}
	
	public FragmentUserPageMan getFragmentUserPageMan() {
		if(newFragment != null && newFragment instanceof FragmentUserPageMan){
			return (FragmentUserPageMan)newFragment ;
		}
		return null ;
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if(newFragment != null && newFragment instanceof FragmentUserPageGirl) {
				FragmentUserPageGirl fragment = (FragmentUserPageGirl) newFragment;
				if (fragment.resetPublicPicture()) {
					return true;
				}
			}
		}
		
		boolean ret = false;
		try {
			ret = super.onKeyUp(keyCode, event);
		} catch (Exception e) {
		}
		
		return ret;
	}
	
	private void init(final View root){
//		ViewTreeObserver viewTreeObserver = root.getViewTreeObserver();
//		viewTreeObserver.addOnPreDrawListener(new OnPreDrawListener(){
//			@Override
//			public boolean onPreDraw() {
//				if(newFragment != null && newFragment instanceof FragmentUserPageGirl){
//					((FragmentUserPageGirl)newFragment).layoutInfoLayout(-1);
//				}
//				root.getViewTreeObserver().removeOnPreDrawListener(this);
//				return true ;
//			}
//		});
	}
	/*@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(newFragment != null && newFragment instanceof FragmentUserPageGirl && hasFocus){
			((FragmentUserPageGirl)newFragment).layoutInfoLayout();
		}
	}*/

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
//		if(newFragment != null && newFragment instanceof FragmentUserPageGirl){
//			if(((FragmentUserPageGirl)newFragment).dispatchTouchEvent(ev)){
//				return true ;
//			}
//		} else if(newFragment != null && newFragment instanceof FragmentUserPageMan) {
//			if(((FragmentUserPageMan)newFragment).dispatchTouchEvent(ev)) {
//				return true;
//			}
//		}
		return super.dispatchTouchEvent(ev);
	}
	
	
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (ShareDialog.mSsoHandler != null) {
        	ShareDialog.mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }
}
