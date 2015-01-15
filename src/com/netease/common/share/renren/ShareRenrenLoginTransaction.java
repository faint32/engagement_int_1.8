package com.netease.common.share.renren;

import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.text.TextUtils;

import com.netease.common.http.THttpMethod;
import com.netease.common.http.THttpRequest;
import com.netease.common.share.ShareBind;
import com.netease.common.share.ShareResult;
import com.netease.common.share.ShareService;
import com.netease.common.share.base.ShareBaseTransaction;
import com.netease.common.share.db.ManagerShareBind;

public class ShareRenrenLoginTransaction extends ShareBaseTransaction {

	private ShareChannelRenren mChannel;
	private String mCode;
	
	protected ShareRenrenLoginTransaction(ShareChannelRenren channel, String code) {
		super(TRANS_TYPE_LOGIN, channel);
		
		mChannel = channel;
		mCode = code;
	}

	@Override
	protected void onTransactionSuccess(int code, Object obj) {
		if (!isCancel()) {
			if (obj != null && obj instanceof JSONObject) {
				ShareResult result = null;
				JSONObject json = (JSONObject) obj;

				String accessToken = json.optString("access_token");
				String refreshToken = json.optString("refresh_token");
				
				if (! TextUtils.isEmpty(accessToken)) {
					mChannel.setToken(accessToken, refreshToken, json.optLong("expires_in"));
					
					JSONObject user = json.optJSONObject("user");
					
					ShareBind shareBind = mChannel.getShareBind();
					
					shareBind.setName(user.optString("name"));
					shareBind.setUserID(user.optString("id"));
					shareBind.setDomainUrl("http://www.renren.com/profile.do?id="
							+ user.optString("id"));
					JSONArray array = user.optJSONArray("avatar");
					if (array != null) {
						for (int i = 0; i < array.length(); i++) {
							JSONObject avatar = array.optJSONObject(i);
							if ("tiny".equals(avatar.optString("type"))) {
								shareBind.setProfile(avatar.optString("url"));
								break;
							}
						}
					}
					
					String key = ShareService.getShareService().getPreferKey();
					ManagerShareBind.addShareBind(key, shareBind);
					
					result = new ShareResult(mChannel.getShareType(), true);
					result.setShareBind(shareBind);
					notifyMessage(code, result);
				}
			}
		}
	}

	@Override
	public void onTransact() {
		THttpRequest request = createAccessToken();
		
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
				+ "?" + URLEncodedUtils.format(list, "utf-8"), THttpMethod.GET);
		
		return request;
	}
	
}
