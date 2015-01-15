package com.netease.common.share.tencent;

import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.netease.common.http.THttpMethod;
import com.netease.common.http.THttpRequest;
import com.netease.common.share.ShareBind;
import com.netease.common.share.ShareResult;
import com.netease.common.share.ShareService;
import com.netease.common.share.base.ShareBaseTransaction;
import com.netease.common.share.db.ManagerShareBind;
import com.netease.common.task.NotifyTransaction;
import com.netease.common.task.example.StringNotifyTransaction;

public class ShareTencentLoginTransaction extends ShareBaseTransaction {
	private static final byte PHASE_AUTH_ME = 0x01;
	private static final byte PHASE_USERS_SHOW = 0x02;
	
	private ShareChannelTencent mChannel;
	
	private byte mPhase;
	private String mOpenId;
	
	public ShareTencentLoginTransaction(ShareChannelTencent channel) {
		super(TRANS_TYPE_LOGIN, channel);
		
		mChannel = channel;
		mPhase = PHASE_AUTH_ME;
	}
	
	@Override
	public NotifyTransaction createNotifyTransaction(Object data,
			int notifyType, int code) {
		if (mPhase == PHASE_AUTH_ME) {
			return new StringNotifyTransaction(this, data, notifyType, code);
		}
		return super.createNotifyTransaction(data, notifyType, code);
	}
	
	@Override
	protected void onTransactionSuccess(int code, Object obj) {
		if (! isCancel()) {
			if (mPhase == PHASE_AUTH_ME) {
				if (obj != null && obj instanceof String) {
					String response = (String) obj;
					int startIndex = response.indexOf('(');
					int endIndex = response.lastIndexOf(')');
					if (startIndex > 0 && endIndex > 0) {
						try {
							JSONObject json = new JSONObject(response.substring(
									startIndex + 1, endIndex - 1).trim());
							String openid = json.optString("openid");
							if (! TextUtils.isEmpty(openid)) {
								mOpenId = openid;
								mPhase = PHASE_USERS_SHOW;
								getTransactionEngine().beginTransaction(this);
								return ;
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
			} else if (mPhase == PHASE_USERS_SHOW && obj != null && obj instanceof JSONObject) {
				ShareResult result = null;
				JSONObject json = (JSONObject) obj;
				
				if (json.optInt("ret") == 0) {
					ShareBind shareBind = mChannel.getShareBind();
					shareBind.setName(json.optString("nickname"));
					shareBind.setUserID(mOpenId);
					shareBind.setProfile(json.optString("figureurl_1"));

					String key = ShareService.getShareService().getPreferKey();
					ManagerShareBind.addShareBind(key, shareBind);

					result = new ShareResult(mChannel.getShareType(), true);
					result.setShareBind(shareBind);
					notifyMessage(code, result);

					return;
				}
			}
			
			onTransactionError(code, obj);
		}
	}

	@Override
	public void onTransact() {
		THttpRequest request = null;
		
		if (mPhase == PHASE_AUTH_ME) {
			request = createAuthMe();
		} else if (mPhase == PHASE_USERS_SHOW) {
			request = createUserShow();
		}
		
		if (request != null) {
			sendRequest(request);
		} else {
			doEnd();
		}
	}
	
	private THttpRequest createAuthMe() {
		List<NameValuePair> list = new LinkedList<NameValuePair>();
		list.add(new BasicNameValuePair("access_token", mChannel.getAccessToken()));
		
		THttpRequest request = new THttpRequest(mChannel.getAuthMe()
				+ "?" + URLEncodedUtils.format(list, "utf-8"), THttpMethod.GET);
		
		return request;
	}

	public THttpRequest createUserShow(){
		List<NameValuePair> list = new LinkedList<NameValuePair>();
		list.add(new BasicNameValuePair("access_token", mChannel.getAccessToken()));
		list.add(new BasicNameValuePair("oauth_consumer_key", mChannel.getClientID()));
		list.add(new BasicNameValuePair("openid", mOpenId));
		
		THttpRequest request = new THttpRequest(mChannel.getUserShowUrl()
				+ "?" + URLEncodedUtils.format(list, "utf-8"), THttpMethod.GET);
		
		return request;
		
	}

}
