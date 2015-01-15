package com.netease.framework.activity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import android.app.Activity;

/**
 * Activity stack 管理
 * @author Panjf
 * @date   2011-10-9
 */
public class FrameworkActivityManager {
	private Stack<Activity> mActivityStack = new Stack<Activity>();
	private Map<Activity, String> mActivityTagMap = new HashMap<Activity, String>();
	
	private static FrameworkActivityManager instance;

	private FrameworkActivityManager() {
	}

	synchronized public static FrameworkActivityManager getInstance() {
		if (instance == null) {
			instance = new FrameworkActivityManager();
		}
		return instance;
	}

	public void removeActivity(Activity activity) {
		if (activity != null) {
			mActivityStack.remove(activity);
			mActivityTagMap.remove(activity);
			activity = null;
		}
	}


	public void addActivity(Activity activity) {
		if (activity == null)
			return;

		mActivityStack.push(activity);
	}

	public boolean setActivityTag(Activity activity, String activityTag){
		if(mActivityStack.contains(activity)){
			mActivityTagMap.put(activity, activityTag);
			return true;
		}
		
		return false;
	}
	
	public Activity getActivityByTag(String tag){
		Iterator<Entry<Activity, String>> mapIt = mActivityTagMap.entrySet().iterator();
		while(mapIt.hasNext()){
			Entry<Activity, String> it = mapIt.next();
			if(it.getValue().equals(tag)){
				return it.getKey();
			}
		}
		
		return null;
	}
	
	
	public void FinishAllActivity() {

		Activity finishActivity = null;
		while (mActivityStack.size() > 0) {
			finishActivity = mActivityStack.pop();
			if (finishActivity != null) {
				finishActivity.finish();
				finishActivity = null;
			}
		}
		mActivityTagMap.clear();
	}
	
	public Activity getTopActivity() {
		Activity top = null;
		if(mActivityStack.size() > 0) {
			top = mActivityStack.peek();
		}
	    return top;
	}
}
