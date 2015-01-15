package com.netease.common.share.renren;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.json.JSONObject;

import android.text.TextUtils;

import com.netease.common.http.THttpMethod;
import com.netease.common.http.THttpRequest;
import com.netease.common.share.ShareBind;
import com.netease.common.share.ShareResult;
import com.netease.common.share.ShareService;
import com.netease.common.share.base.ShareBaseTransaction;
import com.netease.common.share.db.ManagerShareBind;
import com.netease.util.KeyValuePair;

public class ShareRenrenMBlogTransaction extends ShareBaseTransaction {

	ShareChannelRenren mChannel;
	ShareBind mShareBind;
	
	String mTitle;
	String mContent;
	String mImgPath;
	String mUrl;
	
	String mPlaceId;
	
	int mType; //0，更新状态，1，更新新鲜事
	
	public ShareRenrenMBlogTransaction(ShareChannelRenren channel,
			ShareBind shareBind, String content, String imgPath) {
		super(TRANS_TYPE_MBLOG, channel);
		
		mChannel = channel;
		mShareBind = shareBind;
		mContent = content;
		mImgPath = imgPath;
		
		mType = 0;
	}
	
	public ShareRenrenMBlogTransaction(ShareChannelRenren channel,
            ShareBind shareBind, String title, String content, String imgPath, String url) {
        super(TRANS_TYPE_MBLOG, channel);
        
        mChannel = channel;
        mShareBind = shareBind;
        mTitle = title;
        mContent = content;
        mImgPath = imgPath;
        mUrl = url;
        
        mType = 1;
    }
	
	@Override
	protected void onTransactionSuccess(int code, Object obj) {
		if (! isCancel()) {
			if (obj != null && obj instanceof JSONObject) {
				JSONObject json = (JSONObject) obj;
				if(mType == 0){
					JSONObject o1 = json.optJSONObject("response");
                	if (o1 != null) {
                    	long id = o1.optLong("id");
                    	if(id != 0){
                    		ShareResult result = new ShareResult(mChannel.getShareType(), true);
                    		notifyMessage(0, result);
                    		return ;
                    	}
                	}
				} else if(mType == 1){
				    if (json.optInt("response") != 0) {
                        ShareResult result = new ShareResult(mChannel.getShareType(), true);
                        notifyMessage(0, result);
                        return ;
                    }
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
		    if(mType == 0){
		        request = createMBlogSend(mShareBind);
		    }
		    else if(mType == 1){
                request = createFeedSend(mShareBind);
		    }
			sendRequest(request);
		}
	}
	
	private THttpRequest createMBlogSend(ShareBind shareBind) {
		String url = mChannel.getSendMBlogUrl();
				
		List<KeyValuePair> list = new ArrayList<KeyValuePair>();
		
		if (! TextUtils.isEmpty(mContent)) {
			list.add(new KeyValuePair("content", mContent));
		}
		
		THttpRequest request = new THttpRequest(url, THttpMethod.POST);
		request.addParameter("access_token", shareBind.getAccessToken());
		request.addParameter("content", mContent);
		
		return request;
	}
    
    private THttpRequest createFeedSend(ShareBind shareBind) {
        String url = "https://api.renren.com/v2/feed/put";
                
        List<KeyValuePair> list = new ArrayList<KeyValuePair>();
        
        list.add(new KeyValuePair("access_token", shareBind.getAccessToken()));
        list.add(new KeyValuePair("message", "推荐"));
        if (! TextUtils.isEmpty(mTitle)) {
            if(mTitle.length() > 30){
                mTitle = mTitle.substring(0, 30);
            }
            list.add(new KeyValuePair("title", mTitle));
        }
        if (! TextUtils.isEmpty(mImgPath)) {
            list.add(new KeyValuePair("imageUrl", mImgPath));
        }
        if (! TextUtils.isEmpty(mContent)) {
            if(mContent.length() > 200){
                mContent = mContent.substring(0, 200);
            }
            list.add(new KeyValuePair("description", mContent));
        }
        if (! TextUtils.isEmpty(mUrl)) {
            list.add(new KeyValuePair("targetUrl", mUrl));
        }

//        THttpRequest request = new THttpRequest(url + "?" + URLEncodedUtils.format(list, "utf-8"), THttpMethod.POST);

        THttpRequest request = new THttpRequest(url, THttpMethod.POST);
        
        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(list,"utf-8");
            request.setHttpEntity(entity);
        } catch (Exception e) {
        }
        
        return request;

    }

}
