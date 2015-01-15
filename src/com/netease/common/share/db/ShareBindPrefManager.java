package com.netease.common.share.db;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.text.TextUtils;

import com.netease.common.service.BaseService;
import com.netease.common.share.ShareBind;
import com.netease.common.share.ShareType;

public class ShareBindPrefManager implements ShareBindManager {

	private static final String PREF_NAME = ShareBindTable.TABLE_NAME;
	private static final String KEY_TYPE_SEPERATER = "_";
	
	private static SharedPreferences getPreferences() {
		Context context = BaseService.getServiceContext();
		return context.getSharedPreferences(PREF_NAME, 0);
	}
	
	private List<ShareBind> getAllShareBindList(String key) {
		List<ShareBind> list = new LinkedList<ShareBind>();
		
		key += KEY_TYPE_SEPERATER;
		
		Map<String, ?> map = getPreferences().getAll();
		if (map != null && map.size() > 0) {
			
			for (Entry<String, ?> entry : map.entrySet()) {
				if (entry.getKey().startsWith(key)) {
					Object obj = entry.getValue();
					if (obj != null && obj instanceof String) {
						try {
							JSONObject json = new JSONObject((String) obj);
							ShareBind shareBind = new ShareBind(json, 
									ShareBind.JSON_TYPE_DEFAULT);
							list.add(shareBind);
						} catch (JSONException e) {
						}
					}
				}
			}
		}
		
		return list;
	}
	
	@Override
	public Cursor getAllShareBind(String key) {
		MatrixCursor cursor = new MatrixCursor(ShareBind.Projection);

		List<ShareBind> list = getAllShareBindList(key);

		if (list.size() > 0) {
			Collections.sort(list);

			for (ShareBind shareBind : list) {
				cursor.addRow(shareBind.getCursorRow());
			}
		}

		return cursor;
	}
	
	@Override
	public Cursor getAllValidShareBind(String key) {
		MatrixCursor cursor = new MatrixCursor(ShareBind.Projection);

		List<ShareBind> list = getAllShareBindList(key);

		if (list.size() > 0) {
			Collections.sort(list);

			for (ShareBind shareBind : list) {
				if (shareBind.isValid()) {
					cursor.addRow(shareBind.getCursorRow());
				}
			}
		}

		return cursor;
	}

	@Override
	public void addShareBind(String key, ShareBind shareBind) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(key).append(KEY_TYPE_SEPERATER)
			.append(shareBind.getShareType().value());
		
		getPreferences().edit().putString(buffer.toString(), 
				shareBind.getFullJson()).commit();
	}

	@Override
	public void updateShareBind(String key, ShareBind shareBind) {
		addShareBind(key, shareBind);
	}

	@Override
	public void removeShareBind(String key, ShareType shareType) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(key).append(KEY_TYPE_SEPERATER)
			.append(shareType.value());
		
		getPreferences().edit().remove(buffer.toString()).commit();
	}

	@Override
	public void removeShareBind(String key) {
		List<ShareBind> list = getAllShareBindList(key);
		if (list.size() > 0) {
			for (ShareBind shareBind : list) {
				removeShareBind(key, shareBind.getShareType());
			}
		}
	}
	
	@Override
	public ShareBind getShareBind(String key, ShareType shareType) {
		ShareBind shareBind = null;
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(key).append(KEY_TYPE_SEPERATER)
			.append(shareType.value());
		
		String value = getPreferences().getString(buffer.toString(), null);
		if (! TextUtils.isEmpty(value)) {
			try {
				JSONObject json = new JSONObject(value);
				shareBind = new ShareBind(json, ShareBind.JSON_TYPE_DEFAULT);
			} catch (JSONException e) {
			}
		}
		
		return shareBind;
	}
	
	@Override
	public void copyShareBind(String keySource, String keyTarget) {
		List<ShareBind> list = getAllShareBindList(keySource);
		
		if (list.size() > 0) {
			for (ShareBind shareBind : list) {
				if (getShareBind(keyTarget, shareBind.getShareType()) == null) {
					addShareBind(keyTarget, shareBind);
				}
			}
		}
	}

	
}
