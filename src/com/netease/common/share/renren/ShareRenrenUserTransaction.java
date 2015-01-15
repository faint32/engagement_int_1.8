package com.netease.common.share.renren;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.netease.common.http.THttpRequest;
import com.netease.common.share.ShareBind;
import com.netease.common.share.ShareBindFriend;
import com.netease.common.share.ShareResult;
import com.netease.common.share.ShareService;
import com.netease.common.share.base.ShareBaseChannel;
import com.netease.common.share.base.ShareBaseTransaction;
import com.netease.common.share.db.ManagerShareBind;
import com.netease.common.task.NotifyTransaction;
import com.netease.common.task.example.StringNotifyTransaction;

public class ShareRenrenUserTransaction extends ShareBaseTransaction {

	private static final int MAX_FOLLOING_COUNT = 400;
	private static final int SINGLE_LIST_COUNT = 100;
	
	int mCursor;
	int mMaxCount = MAX_FOLLOING_COUNT;
	
	ShareBind mShareBind;
	
	List<ShareBindFriend> mFriends;
	
	protected ShareRenrenUserTransaction(int type, ShareBaseChannel channel) {
		super(type, channel);
	}

	protected static ShareRenrenUserTransaction createGetFollowingList(
			ShareChannelRenren channel, ShareBind shareBind) {
		ShareRenrenUserTransaction t = new ShareRenrenUserTransaction(
				TRANS_TYPE_GET_FOLLOWLING_LIST, channel);
		t.mShareBind = shareBind;
		return t;
	}
	
	@Override
	protected void onTransactionError(int errCode, Object obj) {
		super.onTransactionError(errCode, obj);
		
		if (obj != null) {
			System.out.println(obj);
		}
	}
	
	@Override
	protected void onTransactionSuccess(int code, Object obj) {
		if (! isCancel()) {
			if (obj != null && obj instanceof String) {
				try {
					ShareResult result = new ShareResult(mChannel.getShareType(), true);
					
					JSONObject json = new JSONObject((String) obj);
					
					JSONArray array = json.optJSONArray("response");
					if (array != null) {
						if (mFriends == null) {
							mFriends = new LinkedList<ShareBindFriend>();
						}
						int length = array.length();
						
						for (int i = 0; i < length; i++) {
							JSONObject item = array.optJSONObject(i);
							ShareBindFriend friend = new ShareBindFriend();
							friend.setUserId(item.optString("id"));
							friend.setName(item.optString("name"));
							
							JSONArray avatar = item.optJSONArray("avatar");
							if (avatar != null) {
								int aSize = avatar.length();
								for (int j = 0; j < aSize; j++) {
									JSONObject img = avatar.optJSONObject(j);
									if ("TINY".equals(img.optString("size"))) {
										friend.setProfile(img.optString("url"));
										break;
									}
								}
							}
							
							mFriends.add(friend);
						}
						
						mCursor += SINGLE_LIST_COUNT;
						if (length < SINGLE_LIST_COUNT || mCursor >= MAX_FOLLOING_COUNT) {
							result.setFriends(mFriends);
							notifyMessage(0, result);
						}
						else if (! isCancel()){
							getTransactionEngine().beginTransaction(this);
						}
						else {
							onTransactionError(code, obj);
						}
						return ;
					}
				} catch (JSONException e) {
				}
			}
			
			onTransactionError(code, obj);
		}
	}
	
	@Override
	public NotifyTransaction createNotifyTransaction(Object data,
			int notifyType, int code) {
		return new StringNotifyTransaction(this, data, notifyType, code);
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
			THttpRequest request = createGetFollowingListRequest();
			
			sendRequest(request);
		}
	}
	
	private THttpRequest createGetFollowingListRequest() {
		THttpRequest request = new THttpRequest(mChannel.getFollowingListUrl());
		request.addParameter("access_token", mShareBind.getAccessToken());
		request.addParameter("userId", mShareBind.getUserID());
		request.addParameter("pageSize", String.valueOf(Math.min(mMaxCount - mCursor, SINGLE_LIST_COUNT)));
		request.addParameter("pageNumber", String.valueOf(1 + (mCursor / SINGLE_LIST_COUNT)));
		
		return request;
	}

}
