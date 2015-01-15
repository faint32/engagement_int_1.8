package com.netease.common.share.qqmblog;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.netease.common.http.THttpMethod;
import com.netease.common.http.THttpRequest;
import com.netease.common.service.BaseService;
import com.netease.common.share.ShareBind;
import com.netease.common.share.ShareBindFriend;
import com.netease.common.share.ShareResult;
import com.netease.common.share.ShareService;
import com.netease.common.share.base.ShareBaseTransaction;
import com.netease.common.share.db.ManagerShareBind;
import com.netease.util.PinYin;

public class ShareQqmblogUserTransaction extends ShareBaseTransaction {

	private static final int MAX_FOLLOING_COUNT = 400;
	private static final int SINGLE_LIST_COUNT = 200;
	
	ShareChannelQqmblog mChannel;
	
	ShareBind mShareBind;
	String mUid;
	
	int mCursor;
	int mMaxCount = MAX_FOLLOING_COUNT;
	
	List<ShareBindFriend> mFriends;
	
	protected ShareQqmblogUserTransaction(int type, ShareChannelQqmblog channel) {
		super(type, channel);
		
		mChannel = channel;
	}
	
	protected static ShareQqmblogUserTransaction createGetFollowingList(
			ShareChannelQqmblog channel, ShareBind shareBind) {
		ShareQqmblogUserTransaction t = new ShareQqmblogUserTransaction(
				TRANS_TYPE_GET_FOLLOWLING_LIST, channel);
		t.mShareBind = shareBind;
		return t;
	}
	
	protected static ShareQqmblogUserTransaction createCreateFriendShip(
			ShareChannelQqmblog channel, String uid) {
		ShareQqmblogUserTransaction t = new ShareQqmblogUserTransaction(
				TRANS_TYPE_FRIENDSHIPS_CREATE, channel);
		t.mUid = uid;
		return t;
	}

	@Override
	protected void onTransactionSuccess(int code, Object obj) {
		if (! isCancel()) {
			switch (getType()) {
			case TRANS_TYPE_FRIENDSHIPS_CREATE:
				// { errcode : 0, msg : ok, ret : 0, seqid : xxx }
				notifyMessage(0, null);
				break;
			case TRANS_TYPE_GET_FOLLOWLING_LIST:
				if (obj != null && obj instanceof JSONObject) {
					JSONObject json = (JSONObject) obj;
					if (json.optInt("ret") == 0) {
						json = json.optJSONObject("data");
						if (json != null) {
							int nextCursor = 0;
							if (json.optInt("hasnext") == 0) {
								nextCursor += SINGLE_LIST_COUNT;
							}
							
							JSONArray array = json.optJSONArray("info");
							
							if (array != null) {
								if (mFriends == null) {
									mFriends = new LinkedList<ShareBindFriend>();
								}
								
								Context context = BaseService.getServiceContext();
								PinYin pinyin = PinYin.getInstance();
								int length = array.length();
								for (int i = 0; i < length; i++) {
									JSONObject item = array.optJSONObject(i);
									ShareBindFriend friend = new ShareBindFriend();
									friend.setUserId(item.optString("openid"));
									friend.setAtName(item.optString("name"));
									friend.setName(item.optString("nick"));
									friend.setPinyin(pinyin.getCustomPinyin(context, friend.getName()));
									String profile = item.optString("head");
									if (! TextUtils.isEmpty(profile)) {
										profile += "/50";
									}
									friend.setProfile(profile);
									friend.setVip(item.optInt("isvip")); // 0-不是，1-是
									
									// 用户性别，1-男，2-女，0-未填写,
									int gender = item.optInt("sex");
									switch (item.optInt("sex")) {
									case 1:
										gender = ShareBindFriend.GENDER_MALE;
										break;
									case 2:
										gender = ShareBindFriend.GENDER_FEMALE;
										break;
									default:
										gender = ShareBindFriend.GENDER_UNKNOWN;
									}
									friend.setGender(gender);
									
									mFriends.add(friend);
								}
							}
							
							if (nextCursor < mMaxCount && nextCursor > 0) {
								mCursor = nextCursor;
								onTransact();
								return ;
							}
							else {
								ShareResult result = new ShareResult(mChannel.getShareType(), true);
								result.setFriends(mFriends);
								notifyMessage(0, result);
								return ;
							}
						}
					}
				}
					
				onTransactionError(code, obj);
				break;
			}
		}
	}

	@Override
	public void onTransact() {
		if (mShareBind == null) {
			String key = ShareService.getShareService().getPreferKey();
			mShareBind = ManagerShareBind.getShareBind(key, 
					mChannel.getShareType());
		}
		
		if (mShareBind == null || mShareBind.isInvalid()) {
			
			ShareResult result = new ShareResult(mChannel.getShareType(), false);
			result.setMessage("未绑定帐号或者帐号失效");
			notifyError(0, result);
			doEnd();
		}
		else {
			THttpRequest request = null;
			switch (getType()) {
			case TRANS_TYPE_FRIENDSHIPS_CREATE:
				request = createFriendShipRequest();
				break;
			case TRANS_TYPE_GET_FOLLOWLING_LIST:
				request = createGetFollowingListRequest();
				break;
			}
			
			sendRequest(request);
		}
	}

	private THttpRequest createGetFollowingListRequest() {
		THttpRequest request = new THttpRequest(mChannel.getFollowingListUrl());
		request.addParameter("oauth_consumer_key", mChannel.getClientID());
		request.addParameter("access_token", mShareBind.getAccessToken());
		request.addParameter("openid", mShareBind.getUserID());
		request.addParameter("oauth_version", "2.a");
		request.addParameter("scope", "all");
		request.addParameter("format", "json");
		request.addParameter("reqnum", String.valueOf(Math.min(mMaxCount - mCursor, SINGLE_LIST_COUNT)));
		request.addParameter("startindex", String.valueOf(mCursor));
		request.addParameter("mode", "1");
		
		return request;
	}

	private THttpRequest createFriendShipRequest() {
		THttpRequest request = new THttpRequest(mChannel.getCreateFriendShipUrl(), THttpMethod.POST);
		
		List<NameValuePair> parameters = new LinkedList<NameValuePair>();
		parameters.add(new BasicNameValuePair("oauth_consumer_key", mChannel.getClientID()));
		parameters.add(new BasicNameValuePair("access_token", mShareBind.getAccessToken()));
		parameters.add(new BasicNameValuePair("openid", mShareBind.getUserID()));
		parameters.add(new BasicNameValuePair("oauth_version", "2.a"));
		parameters.add(new BasicNameValuePair("scope", "all"));
		parameters.add(new BasicNameValuePair("format", "json"));
		parameters.add(new BasicNameValuePair("fopenids", mUid));
		
		UrlEncodedFormEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(parameters, "utf-8");
			request.setHttpEntity(entity);
		} catch (UnsupportedEncodingException e) {
		}
		
		return request;
	}
	
}
