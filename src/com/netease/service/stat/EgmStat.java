package com.netease.service.stat;

import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

public class EgmStat {
	
	private static final boolean DEBUG = false;
	private static final String TAG = "EgmStat";
	
	public static final int LOG_IMPRESS_MAINPAGE = 0x01; // 显示首页推荐（impress），{”pos”:5,”size”:big”,"alg":"A"}
	public static final int LOG_CLICK_MAINPAGE = 0x02; // 点击首页推荐（click），{”pos”:5,”size”:big”,"alg":"A"}
	public static final int LOG_CLICK_PHOTO_DETAIL = 0x03; // 用户detail页,点击照片(click), {”photo_id”:2993, ”type”:”vip_free”}
	public static final int LOG_GIVE_GIFT_DETAIL = 0x04; // 赠送礼物(give),{”gift_id”:2993}
	public static final int LOG_CLICK_INTRODUCTION_DETAIL = 0x05;// 查看详细资料(click_introduction),{}
	public static final int LOG_IMPRESS_SEARCH = 0x06; // 搜索曝光日志（impress），{”pos”:5}
	public static final int LOG_CLICK_SEARCH = 0x07; // 搜索点击日志(click), {”pos”:5}
	public static final int LOG_IMPRESS_RANK = 0x08; // 榜单曝光日志（impress）, {”pos”:1, “list_name”:”f_new”, "type":"day"}
	public static final int LOG_CLICK_RANK = 0x09; // 榜单点击日志(click), {”pos”:1, “list_name”:”f_new”, "type":"day"}
	public static final int LOG_GIVE_GIFT_CHAT = 0x0A; // 聊天赠送礼物(give), {”gift_id”:2993}

	public static final String SCENE_MAINPAGE = "mainpage"; // 首页推荐tab
	public static final String SCENE_USER_DETAIL = "user-detail"; // 用户detail页
	public static final String SCENE_TOP_LIST = "top-list"; // 排行榜tab
	public static final String SCENE_CHAT = "chat"; // 聊天
	public static final String SCENE_SEARCH = "search"; // 搜索
	
	public static final String TYPE_PUB_FREE = "pub_free"; // 公开照
	public static final String TYPE_HEAD = "head"; // 头像
	public static final String TYPE_VIP_FREE = "vip_free"; // VIP免费点击
	public static final String TYPE_PAY = "pay"; // 付费点击
	public static final String TYPE_PAID = "paid"; // 已付费
	
	public static final String LIST_F_NEW = "f_new"; // 女神的new新秀榜
	public static final String LIST_F_STAR = "f_star"; // 女神的star红人榜
	public static final String LIST_F_HOT = "f_hot"; // 女神的hot魅力榜
	public static final String LIST_F_TOP = "f_top"; // 女神的top女神榜
	
	public static final String LIST_M_NEW = "m_new"; // 男生的new新贵榜
	public static final String LIST_M_TOP = "m_top"; // 男生的top富豪榜
	public static final String LIST_M_STRENGTH = "m_strength"; // 男生的实力榜
	
	public static final String SIZE_SMALL = "small"; // size small
	public static final String SIZE_BIG = "big"; // size big
	
	public static final String RANK_LIST_DAY = "day"; // 日榜
	public static final String RANK_LIST_MONTH = "month"; // 月榜
	
	public static void log(int action, String sence, long userid) {
		String actionName = getActionName(action);
		
		JSONObject json = new JSONObject();
		
		logInternal(actionName, sence, userid, json);
	}
	
	public static void log(int action, String sence, long userid, int param0) {
		String actionName = getActionName(action);
		String key0 = getActionParam0Key(action);
		
		if (key0 == null) {
			return ;
		}
		
		JSONObject json = new JSONObject();
		try {
			json.put(key0, param0);
		} catch (Exception e) {
		}
		
		logInternal(actionName, sence, userid, json);
	}
	
	public static void log(int action, String sence, long userid, String param0) {
		String actionName = getActionName(action);
		String key0 = getActionParam0Key(action);
		
		if (key0 == null) {
			return ;
		}
		
		JSONObject json = new JSONObject();
		try {
			json.put(key0, param0);
		} catch (Exception e) {
		}
		
		logInternal(actionName, sence, userid, json);
	}
	
	public static void log(int action, String sence, long userid, long param0, String param1) {
		String actionName = getActionName(action);
		String key0 = getActionParam0Key(action);
		String key1 = getActionParam1Key(action);
		
		if (key0 == null || key1 == null) {
			return ;
		}
		
		JSONObject json = new JSONObject();
		try {
			json.put(key0, param0);
			json.put(key1, param1);
		} catch (Exception e) {
		}
		
		logInternal(actionName, sence, userid, json);
	}
	
	public static void log(int action, String sence, long userid, long param0, String param1, String param2) {
		String actionName = getActionName(action);
		String key0 = getActionParam0Key(action);
		String key1 = getActionParam1Key(action);
		String key2 = getActionParam2Key(action);
		
		if (key0 == null || key1 == null || key2 == null) {
			return ;
		}
        switch (action) {
            case LOG_IMPRESS_MAINPAGE:
            case LOG_CLICK_MAINPAGE:
                if (TextUtils.isEmpty(param2)) { // alg 为空处理
                    param2 = "null";
                }
                break;
        }

		JSONObject json = new JSONObject();
		try {
			json.put(key0, param0);
			json.put(key1, param1);
			json.put(key2, param2);
		} catch (Exception e) {
		}
		
		logInternal(actionName, sence, userid, json);
	}
	
	private static String getActionName(int action) {
		String ret = null;
		
		switch (action) {
		case LOG_IMPRESS_MAINPAGE: // 显示首页推荐（impress），{”pos”:5,”size”:big”,"alg":"A"}
			ret = "impress";
			break;
		case LOG_CLICK_MAINPAGE: // 点击首页推荐（click），{”pos”:5,”size”:big,"alg":"A"”}
			ret = "click";
			break;
		case LOG_CLICK_PHOTO_DETAIL: // 用户detail页,点击照片(click), {”photo_id”:2993, ”type”:”vip_free”}
			ret = "click";
			break;
		case LOG_GIVE_GIFT_DETAIL: // 赠送礼物(give),{”gift_id”:2993}
			ret = "give";
			break;
		case LOG_CLICK_INTRODUCTION_DETAIL: // 查看详细资料(click_introduction),{}
			ret = "click_introduction";
			break;
		case LOG_IMPRESS_SEARCH: // 搜索曝光日志（impress），{”pos”:5}
			ret = "impress";
			break;
		case LOG_CLICK_SEARCH: // 搜索点击日志(click), {”pos”:5}
			ret = "click";
			break;
		case LOG_IMPRESS_RANK: // 榜单曝光日志（impress）, {”pos”:1, “list_name”:”f_new”, "type":"day"}
			ret = "impress";
			break;
		case LOG_CLICK_RANK: // 榜单点击日志(click), {”pos”:1, “list_name”:”f_new”, "type":"day"}
			ret = "click";
			break;
		case LOG_GIVE_GIFT_CHAT: // 聊天赠送礼物(give), {”gift_id”:2993}
			ret = "give";
			break;
		}
		
		return ret;
	}
	
	private static String getActionParam0Key(int action) {
		String ret = null;
		
		switch (action) {
		case LOG_IMPRESS_MAINPAGE: // 显示首页推荐（impress），{”pos”:5,”size”:big”,"alg":"A"}
			ret = "pos";
			break;
		case LOG_CLICK_MAINPAGE: // 点击首页推荐（click），{”pos”:5,”size”:big”,"alg":"A"}
			ret = "pos";
			break;
		case LOG_CLICK_PHOTO_DETAIL: // 用户detail页,点击照片(click), {”photo_id”:2993, ”type”:”vip_free”}
			ret = "photo_id";
			break;
		case LOG_GIVE_GIFT_DETAIL: // 赠送礼物(give),{”gift_id”:2993}
			ret = "gift_id";
			break;
		case LOG_IMPRESS_SEARCH: // 搜索曝光日志（impress），{”pos”:5}
			ret = "pos";
			break;
		case LOG_CLICK_SEARCH: // 搜索点击日志(click), {”pos”:5}
			ret = "pos";
			break;
		case LOG_IMPRESS_RANK: // 榜单曝光日志（impress）, {”pos”:1, “list_name”:”f_new”, "type":"day"}
			ret = "pos";
			break;
		case LOG_CLICK_RANK: // 榜单点击日志(click), {”pos”:1, “list_name”:”f_new”, "type":"day"}
			ret = "pos";
			break;
		case LOG_GIVE_GIFT_CHAT: // 聊天赠送礼物(give), {”gift_id”:2993}
			ret = "gift_id";
			break;
		}
		
		return ret;
	}
	
	private static String getActionParam1Key(int action) {
		String ret = null;
		
		switch (action) {
		case LOG_IMPRESS_MAINPAGE: // 显示首页推荐（impress），{”pos”:5,”size”:big”,"alg":"A"}
			ret = "size";
			break;
		case LOG_CLICK_MAINPAGE: // 点击首页推荐（click），{”pos”:5,”size”:big,"alg":"A"”}
			ret = "size";
			break;
		case LOG_CLICK_PHOTO_DETAIL: // 用户detail页,点击照片(click), {”photo_id”:2993, ”type”:”vip_free”}
			ret = "type";
			break;
		case LOG_IMPRESS_RANK: // 榜单曝光日志（impress）, {”pos”:1, “list_name”:”f_new”, "type":"day"}
			ret = "list_name";
			break;
		case LOG_CLICK_RANK: // 榜单点击日志(click), {”pos”:1, “list_name”:”f_new”, "type":"day"}
			ret = "list_name";
			break;
		}
		
		return ret;
	}
	
	private static String getActionParam2Key(int action) {
		String ret = null;
		
		switch (action) {
		case LOG_IMPRESS_MAINPAGE: // 显示首页推荐（impress），{”pos”:5,”size”:big”,"alg":"A"}
			ret = "alg";
			break;
		case LOG_CLICK_MAINPAGE: // 点击首页推荐（click），{”pos”:5,”size”:big,"alg":"A"”}
			ret = "alg";
			break;
		case LOG_IMPRESS_RANK: // 榜单曝光日志（impress）, {”pos”:1, “list_name”:”f_new”, "type":"day"}
			ret = "type";
			break;
		case LOG_CLICK_RANK: // 榜单点击日志(click), {”pos”:1, “list_name”:”f_new”, "type":"day"}
			ret = "type";
			break;
		}
		
		return ret;
	}
	
	private static void logInternal(String action, String sence, long userid, 
			JSONObject json) {
		if (action == null || sence == null || json == null) {
			return ;
		}
		
		try {
			json.put("action", action);
			json.put("scene", sence);
			json.put("other_userid", userid);
		} catch (Exception e) {
		}
		
		if (DEBUG)
			Log.e(TAG, "logInternal: " + json.toString());
		
		
		EgmStatService.log(json);
	}
}
