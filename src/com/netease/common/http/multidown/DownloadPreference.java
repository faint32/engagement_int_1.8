package com.netease.common.http.multidown;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

public class DownloadPreference {
	
	public static final String DownloadPrefTag = "down_pref";
	
	public static DownloadState getDownloadState(Context context, String url) {
		SharedPreferences pref = getPreference(context);
		String jsonStr = pref.getString(url, null);
		
		DownloadState downloadState = null;
		if (jsonStr != null) {
			try {
				downloadState = new DownloadState(url, new JSONObject(jsonStr));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return downloadState;
	}
	
	public static void saveDownloadState(Context context,  DownloadState state) {
		SharedPreferences pref = getPreference(context);
		pref.edit().putString(state.mUrl, state.toJSONString()).commit();
	}
	
	private static SharedPreferences getPreference(Context context) {
		SharedPreferences preferences = null;
		preferences = context.getSharedPreferences(DownloadPrefTag,
				Context.MODE_PRIVATE);

		return preferences;
	}
}
