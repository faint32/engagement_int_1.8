package com.netease.common.share.qqmblog;

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

public class ShareQqmblogLoginTransaction extends ShareBaseTransaction {

	String mOpenID;
	ShareChannelQqmblog mChannel;
	
	public ShareQqmblogLoginTransaction(ShareChannelQqmblog channel, String openid) {
		super(TRANS_TYPE_LOGIN, channel);
		
		mChannel = channel;
		mOpenID = openid;
	}
	
	@Override
	protected void onTransactionSuccess(int code, Object obj) {
		if (!isCancel()) {
			if (obj != null && obj instanceof JSONObject) {
				ShareResult result = null;
				
				JSONObject json = (JSONObject) obj;
				
				ShareBind shareBind = mChannel.getShareBind();
				
				JSONObject data = json.optJSONObject("data");
				shareBind.setName(data.optString("nick"));
				shareBind.setUserID(data.optString("openid"));
				shareBind.setDomainUrl("http://t.qq.com/" + data.optString("name"));
				
				String profile_url = data.optString("head");
				
				if(!TextUtils.isEmpty(profile_url)) //腾讯头像需要填大小
					shareBind.setProfile(profile_url + "/50");
				
				String key = ShareService.getShareService().getPreferKey();
				ManagerShareBind.addShareBind(key, shareBind);
				
				result = new ShareResult(mChannel.getShareType(), true);
				result.setShareBind(shareBind);
				notifyMessage(code, result);
			}
		}
	}

	@Override
	public void onTransact() {
		THttpRequest request = createUserShow();
		sendRequest(request);
	}

	public THttpRequest createUserShow(){
		List<NameValuePair> list = new LinkedList<NameValuePair>();
		list.add(new BasicNameValuePair("access_token", mChannel.getAccessToken()));
		list.add(new BasicNameValuePair("oauth_consumer_key", mChannel.getClientID()));
		list.add(new BasicNameValuePair("oauth_version", "2.a"));
		list.add(new BasicNameValuePair("openid", mOpenID));
		
		THttpRequest request = new THttpRequest(mChannel.getUserShowUrl()
				+ "?" + URLEncodedUtils.format(list, "utf-8"), THttpMethod.GET);
		
		return request;
		
	}

}
