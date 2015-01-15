package com.netease.common.share.sina;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

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

public class ShareSinaUserTransaction extends ShareBaseTransaction {

	private static final int MAX_FOLLOING_COUNT = 400;
	private static final int SINGLE_LIST_COUNT = 200;
	
	ShareChannelSina mChannel;
	ShareBind mShareBind;
	String mUid;
	
	int mCursor;
	int mMaxCount = MAX_FOLLOING_COUNT;
	
	List<ShareBindFriend> mFriends;
	
	protected ShareSinaUserTransaction(int type, ShareChannelSina channel) {
		super(type, channel);
		
		mChannel = channel;
	}
	
	protected static ShareSinaUserTransaction createCreateFriendShip(
			ShareChannelSina channel, String uid) {
		ShareSinaUserTransaction t = new ShareSinaUserTransaction(
				TRANS_TYPE_FRIENDSHIPS_CREATE, channel);
		t.mUid = uid;
		return t;
	}
	
	protected static ShareSinaUserTransaction createGetFollowingList(
			ShareChannelSina channel, ShareBind shareBind) {
		ShareSinaUserTransaction t = new ShareSinaUserTransaction(
				TRANS_TYPE_GET_FOLLOWLING_LIST, channel);
		t.mShareBind = shareBind;
		return t;
	}
	
	@Override
	protected void onTransactionSuccess(int code, Object obj) {
		if (! isCancel()) {
			switch (getType()) {
			case TRANS_TYPE_FRIENDSHIPS_CREATE:
				notifyMessage(0, null);
				break;
			case TRANS_TYPE_GET_FOLLOWLING_LIST:
				if (obj != null && obj instanceof JSONObject) {
					JSONObject json = (JSONObject) obj;
					int nextCursor = json.optInt("next_cursor");
					JSONArray array = json.optJSONArray("users");
					
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
							friend.setUserId(item.optString("id"));
							friend.setName(item.optString("screen_name"));
							friend.setPinyin(pinyin.getCustomPinyin(context, friend.getName()));
							friend.setProfile(item.optString("profile_image_url"));
							friend.setDesp(item.optString("description"));
							friend.setVip(item.optBoolean("verified") ? 1 : 0);
							
							// m：男、f：女、n：未知   -->   0：女、１：男、２：未知
							String genderStr = item.optString("gender");
							int gender = ShareBindFriend.GENDER_UNKNOWN;
							if (genderStr != null) {
								if (genderStr.equals("m")) {
									gender = ShareBindFriend.GENDER_MALE;
								}
								else if (genderStr.equals("f")) {
									gender = ShareBindFriend.GENDER_FEMALE;
								}
							}
							friend.setGender(gender);
							
							mFriends.add(friend);
						}
					}
					
					if (nextCursor < mMaxCount && nextCursor > 0) {
						mCursor = nextCursor;
						onTransact();
					}
					else {
						ShareResult result = new ShareResult(mChannel.getShareType(), true);
						result.setFriends(mFriends);
						notifyMessage(0, result);
					}
				}
				else {
					ShareResult result = new ShareResult(mChannel.getShareType(), false);
					notifyError(0, result);
				}
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
		request.addParameter("access_token", mShareBind.getAccessToken());
		request.addParameter("uid", mShareBind.getUserID());
		request.addParameter("count", String.valueOf(Math.min(mMaxCount - mCursor, SINGLE_LIST_COUNT)));
		request.addParameter("cursor", String.valueOf(mCursor));
		
		return request;
	}

	private THttpRequest createFriendShipRequest() {
		THttpRequest request = new THttpRequest(mChannel.getCreateFriendShipUrl(), THttpMethod.POST);
		
		List<NameValuePair> parameters = new LinkedList<NameValuePair>();
		parameters.add(new BasicNameValuePair("access_token", mShareBind.getAccessToken()));
		parameters.add(new BasicNameValuePair("uid", mUid));
		
		UrlEncodedFormEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(parameters, "utf-8");
			request.setHttpEntity(entity);
		} catch (UnsupportedEncodingException e) {
		}
		
		return request;
	}
}
