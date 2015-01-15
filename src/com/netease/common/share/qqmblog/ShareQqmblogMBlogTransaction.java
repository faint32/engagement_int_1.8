package com.netease.common.share.qqmblog;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.text.TextUtils;

import com.netease.common.http.THttpMethod;
import com.netease.common.http.THttpRequest;
import com.netease.common.http.Entities.FilePart;
import com.netease.common.http.Entities.MultipartEntity;
import com.netease.common.http.Entities.Part;
import com.netease.common.http.Entities.ResFilePartSource;
import com.netease.common.http.Entities.StringPart;
import com.netease.common.service.BaseService;
import com.netease.common.share.ShareBind;
import com.netease.common.share.ShareResult;
import com.netease.common.share.ShareService;
import com.netease.common.share.base.ShareBaseTransaction;
import com.netease.common.share.db.ManagerShareBind;

public class ShareQqmblogMBlogTransaction extends ShareBaseTransaction {

	ShareChannelQqmblog mChannel;
	ShareBind mShareBind;
	String mContent;
	String mImgPath;
	
	ShareQqmblogMBlogTransaction(ShareChannelQqmblog channel, 
			ShareBind shareBind, String content, String imgPath) {
		super(TRANS_TYPE_MBLOG, channel);
		
		mChannel = channel;
		mShareBind = shareBind;
		mContent = content;
		mImgPath = imgPath;
	}
	
	@Override
	protected void onTransactionSuccess(int code, Object obj) {
		if (! isCancel()) {
			ShareResult result = null;
			
			if (obj != null && obj instanceof JSONObject) {
				JSONObject json = (JSONObject) obj;
				
				int ret = json.optInt("ret");
				if(ret == 0){
					result = new ShareResult(mChannel.getShareType(), true);
					notifyMessage(0, result);
					
					return ;
				}
			}
			
			onTransactionError(code, obj);
		}
	}

	@Override
	public void onTransact() {
		String key = ShareService.getShareService().getPreferKey();
		if (mShareBind == null) {
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
			FilePart imgPart = null;
			
			try {
				if (! TextUtils.isEmpty(mImgPath)) {
					if (mImgPath.startsWith(ResFilePartSource.NAME_PREFIX)) {
						imgPart = new FilePart("pic", new ResFilePartSource(
								BaseService.getServiceContext(), mImgPath));
					}
					else {
						File file = new File(mImgPath);
						String fileName = file.getName();
						
						if (fileName.lastIndexOf('.') < 0) {
							fileName += ".jpg";
						}
						if (file.exists()){
							imgPart = new FilePart("pic", fileName, new File(mImgPath));
						}
					}
				}
			} catch (Exception e) {
			}
			
			List<NameValuePair> list = new LinkedList<NameValuePair>();
			list.add(new BasicNameValuePair("format", "json"));
			list.add(new BasicNameValuePair("content", mContent));
			
			list.add(new BasicNameValuePair("oauth_version", "2.a"));
			list.add(new BasicNameValuePair("access_token", mShareBind.getAccessToken()));
			list.add(new BasicNameValuePair("oauth_consumer_key", mChannel.getClientID()));
			list.add(new BasicNameValuePair("openid", mShareBind.getUserID()));
			
			if (imgPart != null) {
				Part[] parts = new Part[list.size() + 1];
				for (int i = 0; i < list.size(); i++) {
					NameValuePair pair = list.get(i);
					parts[i] = new StringPart(pair.getName(), 
							pair.getValue(), "utf-8");
				}
				
				parts[list.size()] = imgPart;
				
				MultipartEntity entity = new MultipartEntity(parts);
				
				request = new THttpRequest(mChannel.getSendPicMBlogUrl(), THttpMethod.POST);
				request.setHttpEntity(entity);
			} else {
				request = new THttpRequest(mChannel.getSendMBlogUrl(), THttpMethod.POST);
				
				try {
					UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, "utf-8");
					request.setHttpEntity(entity);
				} catch (Exception e) {
				}
			}
			
			sendRequest(request);
		}
	}

}
