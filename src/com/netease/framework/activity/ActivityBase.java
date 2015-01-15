package com.netease.framework.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;

import com.netease.date.R;
import com.netease.framework.skin.SkinInterface;
import com.netease.framework.widget.DialogUtil;

/**
 * 
 * @author Panjf
 * @date   2011-10-9
 */
public abstract class ActivityBase extends ActionBarActivity implements IActivity, SkinInterface{
	private boolean mbMenuShown;
	
	private ArrayList<Fragment> mFragmentList = new ArrayList<Fragment>();
	
	private ProgressDialog mWaitingProgress;
	private PopupWindow mPopupWindow;
	
	private LinearLayout mTitleLay;
	private LinearLayout mContentLay;

	protected LayoutInflater mInflater;
	
	
//	protected static final int  MENU_EXIT = 10001;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestFeature();
		super.onCreate(savedInstanceState);
		FrameworkActivityManager.getInstance().addActivity(this);

		
		super.setContentView(R.layout.frame_base);
		
		mTitleLay = (LinearLayout) findViewById(R.id.title_lay);	
		mContentLay = (LinearLayout) findViewById(R.id.content_lay);

		mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public void onAttachFragment(Fragment fragment) {
		if (null != mFragmentList) {
			if (mFragmentList.contains(fragment)) {
				return;
			}
			mFragmentList.add(fragment);
		}
	}
	
	@Override
	public Object onRetainCustomNonConfigurationInstance() {
		if (null != mFragmentList) {
			for (Fragment fragment : mFragmentList) {
				if (fragment instanceof IConfigChangeInstance) {
					((IConfigChangeInstance) fragment).onSave();
				}
			}
		}
		return super.onRetainCustomNonConfigurationInstance();
	}
	
	@Override
	protected void onDestroy() {
		FrameworkActivityManager.getInstance().removeActivity(this);
		super.onDestroy();
		if (null != mFragmentList) {
			mFragmentList.clear();
		}
		mFragmentList = null;
	}	
	
	/**
	 * 一些需要在setContentView前初始化的东西在这里做
	 */
	protected abstract void requestFeature();
	
	public void setMenuShow(boolean b){
		mbMenuShown = b;
	}
	
	@Override
	public void setContentView(int layoutResID) {
		View v = mInflater.inflate(layoutResID,  mContentLay, false);
		setContentView(v);
	}
	
	@Override
	public void setContentView(View view) {
		if(view != null){
			if(mContentLay != null){
				mContentLay.removeAllViews();
				
				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
				if(params == null)
					params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		
				mContentLay.addView(view, params);
			}
			else{
				super.setContentView(view);
			}
		}
	}

	/**
	 * 设置Title布局
	 * @param layId
	 */
	public void setTitleLayout(int layId){
		View v = mInflater.inflate(layId, mTitleLay, false);
		setTitleLayout(v);
	}
	
	/**
	 * 设置title布局
	 * @param v
	 */
	public void setTitleLayout(View v){
		if(v != null){
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

			mTitleLay.removeAllViews();
			mTitleLay.addView(v, params);
		}		
	}
	
	/**
	 * 是否显示title
	 * @param bVisibility
	 */
	public void setTitleVisibility(boolean bVisibility){
		if(mTitleLay != null)
			mTitleLay.setVisibility(bVisibility ? View.VISIBLE : View.GONE);
	}
	
	/**
	 * 显示悬浮层
	 * @param layId
	 */
	public void showFullScreenTip(int layId){
		View v = mInflater.inflate(layId, null);
		showFullScreenTip(v);
	}
	/**
	 * 显示悬浮层
	 * @param d
	 */
	public void showFullScreenTip(Drawable d){
		LinearLayout lay = new LinearLayout(this);
		lay.setBackgroundDrawable(d);
		showFullScreenTip(lay);
	}
	/**
	 * 显示悬浮层
	 * @param view
	 */
	public void showFullScreenTip(View view) {
		if(view == null)
			return;
		
		if(mPopupWindow != null && mPopupWindow.isShowing()){
			mPopupWindow.dismiss();
		}
		
        view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        view.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(mPopupWindow != null){
					mPopupWindow.dismiss();
					mPopupWindow = null;
				}
			}
        	
        });
        mPopupWindow = new PopupWindow(view,
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setTouchable(true);
        mPopupWindow.setFocusable(true);
        
        mPopupWindow.showAtLocation(getWindow().getDecorView(), Gravity.TOP, 0, 0);
        
        mPopupWindow.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss() {
				mPopupWindow = null;
			}
		});
	}
	
	
	protected void waitingSomething(String message){
		waitingSomething(null, message);
	}
	
	protected void waitingSomething(String title, String message){
		if(mWaitingProgress != null)
			stopWaiting();
		
		mWaitingProgress = DialogUtil.showProgressDialog(this, title, message, true, true);
	}
	protected void stopWaiting(){
		if(mWaitingProgress != null){
			mWaitingProgress.dismiss();
			mWaitingProgress = null;
		}
	}
	protected boolean isWaiting(){
		return (mWaitingProgress != null);
	}
	
	//按键的处理
	 private boolean mbBackKeyDown = false;
	    @Override
	    public boolean onKeyDown(int keyCode, KeyEvent event) {
//	    	PalLog.i("FatherSonActivityMgr", "onKeyDown:" + this.getClass().getSimpleName());
	        if (keyCode == KeyEvent.KEYCODE_BACK) {
	        	mbBackKeyDown = true;
	            return true;
	        }
	        mbBackKeyDown = false;
	        return super.onKeyDown(keyCode, event);
	    }

	    @Override
	    public boolean onKeyUp(int keyCode, KeyEvent event) {
//	    	PalLog.i("FatherSonActivityMgr", "onKeyUp:" + this.getClass().getSimpleName());
	        if (keyCode == KeyEvent.KEYCODE_BACK) {
	        	if(mbBackKeyDown){
	        		onBackPressed();
	        	}
	        	mbBackKeyDown = false;
	            return true;
	        }
	        mbBackKeyDown = false;
	        return super.onKeyUp(keyCode, event);
	    }
	/**
	 * Activity间数据传递
	 * @param tag
	 * @param obj
	 */
	public void notifyActivity(String tag, Object obj) {
		Activity a = FrameworkActivityManager.getInstance().getActivityByTag(tag);
		if(a != null && a instanceof IActivity){
			((IActivity)a).onActivityNotify(obj);
		}
	}
	
	@Override
	public void onActivityNotify(Object obj) {

	}

	@Override
	public void onSkinChanged() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean onCreatePanelMenu(int featureId, Menu menu) {
		try {
			return super.onCreatePanelMenu(featureId, menu);
		} catch (Exception e) {
		}
		 return false;
	}
	
}
