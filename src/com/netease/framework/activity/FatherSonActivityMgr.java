package com.netease.framework.activity;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;

import com.netease.common.log.NTLog;

/**
 * Activity管理
 * @author Panjf
 * @date   2011-10-9
 */
public class FatherSonActivityMgr extends ActivityGroup {
	private LocalActivityManager mChildActivityMgr;

	private String mTag = null;
	private View mSelfContent;
	private boolean mbSetSonInContent;

	
	private List<String> mTagList = new LinkedList<String>();
	
	private static int sTagCounter = 0;
	
	private String generaTag(){
		String tag = "child:"+String.valueOf(++sTagCounter);
		return tag;
	}
	
	public String getTag(){
		return mTag;
	}
	
	/**
	 * 保存自己的显示View
	 * @param vSelf
	 */
	protected void setSelfConternt(View vSelf){
		mSelfContent = vSelf;
	}
	
	/**
	 * 调用startChildActivity时, 确定是否把当前activity放入父activity的内容区域
	 * @param b
	 */
	protected void setSonInContent(boolean b){
		mbSetSonInContent = true;
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mChildActivityMgr = getLocalActivityManager();
		
		if(mTagList == null){
			mTagList = new LinkedList<String>();
		}
		
	}

    public void startChildActivity(Intent intent) {
		startChildActivity(null, intent);
    }
    
    public void startChildActivity(String tag, Intent intent){
    	if(tag == null)
    		tag = generaTag();
    	
    	Window w = mChildActivityMgr.startActivity(tag, intent);
		View childContent = w != null ? w.getDecorView() : null;
		
		FatherSonActivityMgr act = (FatherSonActivityMgr) mChildActivityMgr.getCurrentActivity();
		
		if(childContent != null){
			NTLog.i("FatherSonActivityMgr", "Activity Tag:" + tag);
			mTagList.add(tag);
			
//			if(mbSetSonInContent && act instanceof ActivityBase){
//				((ActivityBase)this).setContentView(childContent);
//			}
//			else{
//				super.setContentView(childContent);
//			}
		}
		
		
		if(act != null){
    		act.mTag = tag;
		}
    }
    
    public void destroyChildActivity(){
    	destroyChildActivity(null);
    }
    public void destroyChildActivity(String tag){
    	if(tag == null){
    		FatherSonActivityMgr act = (FatherSonActivityMgr) mChildActivityMgr.getCurrentActivity();
    		if(act != null)
    			tag = act.mTag;
    		else{
    			int len = mTagList.size();
    			if(len > 0)
    				tag = mTagList.get(len-1);
    		}
    	}

    	if(tag == null){
    		finish();
    		return;
    	}
    	
    	mChildActivityMgr.destroyActivity(tag, true);
    	mTagList.remove(tag);
    	
    	
    	//因为谷歌的bug, 需要清理LocalActivityManager 的 mActivities
    	Field mActivitiesField = null;
		try {
			mActivitiesField = mChildActivityMgr.getClass().getDeclaredField("mActivities");
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		if(mActivitiesField != null){
        	mActivitiesField.setAccessible( true );
        	try {
    			((Map) mActivitiesField.get(mChildActivityMgr)).remove(tag);
    		} catch (IllegalArgumentException e) {
    			e.printStackTrace();
    		} catch (IllegalAccessException e) {
    			e.printStackTrace();
    		} 
		}
		
		super.setContentView(mSelfContent);
    }
    
    @Override
    public void finishFromChild(Activity child) {
    	if(child instanceof FatherSonActivityMgr){
    		FatherSonActivityMgr fsActivity = (FatherSonActivityMgr)child;
    		destroyChildActivity(fsActivity.mTag);
    	}
    	else{
    		finish();
    	}
    }
    
    private boolean mbBackKeyDown = false;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//    	PalLog.i("FatherSonActivityMgr", "onKeyDown:" + this.getClass().getSimpleName());
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	mbBackKeyDown = true;
            return true;
        }
        mbBackKeyDown = false;
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
//    	PalLog.i("FatherSonActivityMgr", "onKeyUp:" + this.getClass().getSimpleName());
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	if(mbBackKeyDown)
        		onBackPressed();
        	
        	mbBackKeyDown = false;
            return true;
        }
        mbBackKeyDown = false;
        return super.onKeyUp(keyCode, event);
    }
    
    public void  onBackPressed() {
        finish();
    }
    
    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
    	Activity parent = getParent();
    	if(parent != null)
    		parent.startActivityForResult(intent, requestCode);
    	else
    		super.startActivityForResult(intent, requestCode);
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Activity a = getCurrentActivity();
		if(a != null && a instanceof FatherSonActivityMgr)
			((FatherSonActivityMgr)a).onActivityResult(requestCode, resultCode, data);
	};
	
}
