package com.netease.common.share.sina;

import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.text.TextUtils;

import com.netease.common.http.THttpMethod;
import com.netease.common.http.THttpRequest;
import com.netease.common.share.ShareBind;
import com.netease.common.share.ShareResult;
import com.netease.common.share.ShareService;
import com.netease.common.share.base.ShareBaseTransaction;
import com.netease.common.share.db.ManagerShareBind;

public class ShareSinaLoginTransaction extends ShareBaseTransaction {

	private static final byte PHASE_ACCESS_TOKEN = 0x01;
	private static final byte PHASE_USERS_SHOW = 0x02;
	private static final byte PHASE_CHECK_FRIENDSHIP = 0x03;
	
	private byte mPhase;
	
	private ShareChannelSina mChannel;
	private String mCode;
	private String mOfficialUid;
	private ShareResult mShareResult;
	
	/**
	 * 
	 * @param channel
	 * @param code
	 * @param officialUid 检查是否关注官网id
	 */
	protected ShareSinaLoginTransaction(ShareChannelSina channel, String code,
			String officialUid) {
		super(TRANS_TYPE_LOGIN, channel);
		
		mChannel = channel;
		mCode = code;
		mOfficialUid = officialUid;
		
		if (mChannel.getAccessToken() != null) {
			mPhase = PHASE_USERS_SHOW;
		} else {
			mPhase = PHASE_ACCESS_TOKEN;
		}
	}

	@Override
	protected void onTransactionSuccess(int code, Object obj) {
		if (! isCancel()) {
			if (obj != null && obj instanceof JSONObject) {
				JSONObject json = (JSONObject) obj;
				ShareResult result = null;
				
				if (mPhase == PHASE_USERS_SHOW) {
					ShareBind shareBind = mChannel.getShareBind();
					
					shareBind.setName(json.optString("name"));
					shareBind.setUserID(json.optString("id"));
					shareBind.setDomainUrl("http://weibo.com/" + json.optString("domain"));
					shareBind.setProfile(json.optString("profile_image_url"));
					
					String key = ShareService.getShareService().getPreferKey();
					ManagerShareBind.addShareBind(key, shareBind);
					
					result = new ShareResult(mChannel.getShareType(), true);
					result.setShareBind(shareBind);
					
					if (TextUtils.isEmpty(mOfficialUid)) {
						notifyMessage(code, result);
					} else {
						mShareResult = result;
						mPhase = PHASE_CHECK_FRIENDSHIP;
						
						getTransactionEngine().beginTransaction(this);
					}
				} else if (mPhase == PHASE_CHECK_FRIENDSHIP) {
					JSONObject source = json.optJSONObject("source");
					boolean following = source.optBoolean("following");
					
					mShareResult.setFollowingOfficial(following);
					notifyMessage(0, mShareResult);
				} else {
					mCode = json.optString("uid");
					mChannel.setToken(json.optString("access_token"), 
							json.optString("refresh_token"), 
							json.optLong("expires_in"));
					
					mPhase = PHASE_USERS_SHOW;
					
					getTransactionEngine().beginTransaction(this);
				}
			} else {
				onTransactionError(0, obj);
			}
		}
	}
	
	@Override
	public void onTransact() {
		THttpRequest request = null;
		
		switch (mPhase) {
		case PHASE_ACCESS_TOKEN:
			request = createAccessToken();
			break;
		case PHASE_USERS_SHOW:
			request = createUserShow();	
			break;
		case PHASE_CHECK_FRIENDSHIP:
			request = createCheckOfficialFriendShip();	
			break;
		}
		
		if (request != null) {
			sendRequest(request);
		} else {
			doEnd();
		}
	}
	
	private THttpRequest createAccessToken() {
		List<NameValuePair> list = new LinkedList<NameValuePair>();
		list.add(new BasicNameValuePair("client_id", mChannel.getClientID()));
		list.add(new BasicNameValuePair("client_secret", mChannel.getClientSecret()));
		list.add(new BasicNameValuePair("grant_type", "authorization_code"));
		list.add(new BasicNameValuePair("redirect_uri", mChannel.getRedirectPrefix()));
		list.add(new BasicNameValuePair("code",  mCode));
		
		THttpRequest request = new THttpRequest(mChannel.getAccessTokenUrl()
				+ "?" + URLEncodedUtils.format(list, "utf-8"), THttpMethod.POST);
		
		return request;
	}
	
	private THttpRequest createUserShow(){
		List<NameValuePair> list = new LinkedList<NameValuePair>();
		list.add(new BasicNameValuePair("access_token", mChannel.getAccessToken()));
		list.add(new BasicNameValuePair("uid", mCode));
		
		THttpRequest request = new THttpRequest(mChannel.getUserShowUrl()
				+ "?" + URLEncodedUtils.format(list, "utf-8"), THttpMethod.GET);
		
		return request;
	}
	

	private THttpRequest createCheckOfficialFriendShip() {
		List<NameValuePair> list = new LinkedList<NameValuePair>();
		list.add(new BasicNameValuePair("access_token", mChannel.getAccessToken()));
		list.add(new BasicNameValuePair("source_id", mCode));
		list.add(new BasicNameValuePair("target_id", mOfficialUid));
		
		THttpRequest request = new THttpRequest(mChannel.getShowFriendShipUrl()
				+ "?" + URLEncodedUtils.format(list, "utf-8"), THttpMethod.GET);
		
		return request;
	}
}
