package com.netease.engagement.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentComplain;

public class ActivityComplain extends ActivityEngagementBase{
	
	public static void startActivity(Context context ,long postId) {
        Intent intent = new Intent(context, ActivityComplain.class);
        if(!(context instanceof Activity)){
        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(EgmConstants.BUNDLE_KEY.USER_ID, postId);
        ((Activity)context).startActivity(intent);
    }
	
	public static void startActivity(Fragment fragment ,long postId){
		Intent intent = new Intent(fragment.getActivity(), ActivityComplain.class);
		intent.putExtra(EgmConstants.BUNDLE_KEY.USER_ID, postId);
	    fragment.startActivity(intent);
	}
	
	private long mPostId ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setCustomActionBar();
		
		mManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		mPostId = this.getIntent().getLongExtra(EgmConstants.BUNDLE_KEY.USER_ID,0);
		FrameLayout container = new FrameLayout(this);
		container.setId(R.id.activity_complain_container_id);
		setContentView(container);
		
		if(findViewById(R.id.activity_complain_container_id) != null && savedInstanceState == null){
			FragmentComplain fragment = FragmentComplain.newInstance(mPostId);
			FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.activity_complain_container_id, fragment).commit();
		}
	}
	
	/* 点击软键盘外的区域使软键盘隐藏 */
    private InputMethodManager mManager;
    @Override  
    public boolean onTouchEvent(MotionEvent event) {  
        if(event.getAction() == MotionEvent.ACTION_DOWN){  
            if(mManager != null && getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null){  
                mManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }  
        }  
        return super.onTouchEvent(event);  
    }  
}
