package com.netease.common.share.kaixin;

import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.netease.common.http.THttpMethod;
import com.netease.common.http.THttpRequest;
import com.netease.common.share.ShareBind;
import com.netease.common.share.ShareResult;
import com.netease.common.share.ShareService;
import com.netease.common.share.base.ShareBaseTransaction;
import com.netease.common.share.db.ManagerShareBind;

public class ShareKaixinLoginTransaction extends ShareBaseTransaction {

	private static final byte PHASE_ACCESS_TOKEN = 0x01;
	private static final byte PHASE_USERS_SHOW = 0x02;
	
	private byte mPhase;
	
	private ShareChannelKaixin mChannel;
	private String mCode;
	
	protected ShareKaixinLoginTransaction(ShareChannelKaixin channel, String code) {
		super(TRANS_TYPE_LOGIN, channel);
		
		mChannel = channel;
		mCode = code;
		mPhase = PHASE_ACCESS_TOKEN;
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
					shareBind.setUserID(json.optString("uid"));
					shareBind.setDomainUrl("http://www.kaixin001.com/home/?uid="
							+ shareBind.getUserID());
					shareBind.setProfile(json.optString("logo50"));
					
					String key = ShareService.getShareService().getPreferKey();
					ManagerShareBind.addShareBind(key, shareBind);
					
					result = new ShareResult(mChannel.getShareType(), true);
					result.setShareBind(shareBind);
					notifyMessage(code, result);
				} else {
					mChannel.setToken(json.optString("access_token"), 
							json.optString("refresh_token"),
							json.optLong("expires_in"));
					
					mPhase = PHASE_USERS_SHOW;
					
					getTransactionEngine().beginTransaction(this);
				}
			}
		}
	}

	@Override
	public void onTransact() {
		THttpRequest request = null;
		
		if (mPhase == PHASE_ACCESS_TOKEN) {
			request = createAccessToken();
		} else if (mPhase == PHASE_USERS_SHOW) {
			request = createUserShow();
		}
		
		if (request != null) {
			sendRequest(request);
		} else {
			doEnd();
		}
	}
	
	private THttpRequest createAccessToken() {
		List<NameValuePair> list = new LinkedList<NameValuePair>();
		list.add(new BasicNameValuePair("grant_type", "authorization_code"));
		list.add(new BasicNameValuePair("code",  mCode));
		list.add(new BasicNameValuePair("client_id", mChannel.getClientID()));
		list.add(new BasicNameValuePair("client_secret", mChannel.getClientSecret()));
		list.add(new BasicNameValuePair("redirect_uri", mChannel.getRedirectPrefix()));
		
		THttpRequest request = new THttpRequest(mChannel.getAccessTokenUrl()
				+ "?" + URLEncodedUtils.format(list, "utf-8"), THttpMethod.GET);
		
		return request;
	}
	
	public THttpRequest createUserShow(){
		List<NameValuePair> list = new LinkedList<NameValuePair>();
		list.add(new BasicNameValuePair("access_token", mChannel.getAccessToken()));
		
		THttpRequest request = new THttpRequest(mChannel.getUserShowUrl()
				+ "?" + URLEncodedUtils.format(list, "utf-8"), THttpMethod.GET);
		
		return request;
		
	}

}
