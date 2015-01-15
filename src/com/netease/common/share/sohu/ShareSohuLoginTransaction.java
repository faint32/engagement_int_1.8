package com.netease.common.share.sohu;

import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONObject;

import com.netease.common.http.THttpMethod;
import com.netease.common.http.THttpRequest;
import com.netease.common.share.ShareBind;
import com.netease.common.share.ShareResult;
import com.netease.common.share.ShareService;
import com.netease.common.share.base.ShareBaseTransaction;
import com.netease.common.share.db.ManagerShareBind;

public class ShareSohuLoginTransaction extends ShareBaseTransaction {

	private ShareChannelSohu mChannel;
	
	protected ShareSohuLoginTransaction(ShareChannelSohu channel) {
		super(TRANS_TYPE_LOGIN, channel);
		mChannel = channel;
	}
	
	@Override
	protected void onTransactionSuccess(int code, Object obj) {
		if (! isCancel()) {
			if (obj != null && obj instanceof JSONObject) {
				JSONObject json = (JSONObject) obj;
				ShareResult result = null;

				ShareBind shareBind = mChannel.getShareBind();

				shareBind.setName(json.optString("screen_name"));
				shareBind.setUserID(json.optString("id"));
				shareBind.setDomainUrl("http://t.sohu.com/people?uid="
						+ json.optString("id"));
				shareBind.setProfile(json.optString("profile_image_url"));

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
		THttpRequest request = null;
		
		request = createUserShow();
		
		if (request != null) {
			sendRequest(request);
		} else {
			doEnd();
		}
	}
	
	public THttpRequest createUserShow(){
		List<NameValuePair> list = new LinkedList<NameValuePair>();
//		list.add(new BasicNameValuePair("access_token", mChannel.getAccessToken()));
		
		THttpRequest request = new THttpRequest(mChannel.getUserShowUrl()
				+ "?" + URLEncodedUtils.format(list, "utf-8"), THttpMethod.GET);
		request.addHeader("Authorization", "OAuth2 " + mChannel.getAccessToken());
		
		return request;
		
	}

}
