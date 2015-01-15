package com.netease.engagement.activity;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.View.OnClickListener;

import com.netease.date.R;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.framework.activity.ActivityBase;
import com.netease.mobidroid.DATracker;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.media.MediaPlayerWrapper;

public abstract class ActivityEngagementBase extends ActivityBase implements OnClickListener {
	
	private static final boolean DA = true;

	protected ActionBar mActionBar ;
	private boolean mIsOveride = true;
	
	protected void setOveride(boolean overide) {
		mIsOveride = overide;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (DA) DATracker.enableTracker(this, "MA-91DB-2CED6930CDB6",
				EgmUtil.getVersionStr(this), 
				EgmUtil.getAppChannelID(this));
		
		if (mIsOveride) {
			mActionBar = getSupportActionBar();
			if(mActionBar != null){
				mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
				if (!(this instanceof ActivityHome)){
	    			mActionBar.setDisplayShowHomeEnabled(true);
	    			mActionBar.setDisplayHomeAsUpEnabled(true);
	    			mActionBar.setDisplayShowTitleEnabled(true);
	    			mActionBar.setDisplayShowCustomEnabled(true);
	    			mActionBar.setDisplayUseLogoEnabled(true);
				} else {
	                mActionBar.setDisplayHomeAsUpEnabled(false);
	            }
				mActionBar.setIcon(R.drawable.ic_launcher);
				
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (DA) DATracker.getInstance().resume();
		
		setOrientation();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		if (DA) DATracker.getInstance().close();
		
		if(MediaPlayerWrapper.getInstance() != null){
    			MediaPlayerWrapper.getInstance().stop();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	protected void setOrientation() {
		/**
		 * 设置屏幕为竖屏
		 */
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	

	@Override
	protected void requestFeature() {
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	@Override
	public void refreshContent() {
		// TODO Auto-generated method stub
	}

	//自定义actionbar相关
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.title_left){
			finish();
		}
	}
	
	protected void setCustomActionBar(){
		mCustomActionBar = new CustomActionBar(this,this.getSupportActionBar(), R.layout.common_title_bar);
		mCustomActionBar.setLeftClickListener(this);
	}
	
	private CustomActionBar mCustomActionBar ;
	
	public CustomActionBar getCustomActionBar(){
		return mCustomActionBar ;
	}
	
	private ProgressDialog mWaitingProgress;
	
	protected void showWatting(String message) {
        showWatting(null, message);
    }

    protected void showWatting(String title, String message) {
        if (mWaitingProgress != null)
            stopWaiting();

        mWaitingProgress = ProgressDialog.show(this, title, message, true, true);
    }
    
    protected void showWatting(String title, String message, boolean cancle) {
        if (mWaitingProgress != null)
            stopWaiting();

        mWaitingProgress = ProgressDialog.show(this, title, message, true, true);
        mWaitingProgress.setCancelable(cancle);
    }

    @Override
    protected void stopWaiting() {
        if (mWaitingProgress != null) {
            mWaitingProgress.dismiss();
            mWaitingProgress = null;
        }
    }
}
