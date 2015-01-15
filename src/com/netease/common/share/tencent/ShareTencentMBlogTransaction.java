package com.netease.common.share.tencent;

import java.io.File;
import java.io.FileNotFoundException;
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
import com.netease.common.http.Entities.StringPart;
import com.netease.common.share.ShareBind;
import com.netease.common.share.ShareResult;
import com.netease.common.share.ShareService;
import com.netease.common.share.base.ShareBaseTransaction;
import com.netease.common.share.db.ManagerShareBind;

public class ShareTencentMBlogTransaction extends ShareBaseTransaction {

	ShareChannelTencent mChannel;
	ShareBind mShareBind;
	String mTitle;
	String mContent;
	String mImgPath;
	String mUrl;
	
	public ShareTencentMBlogTransaction(ShareChannelTencent channel, 
			ShareBind shareBind, String title, String content,
			String imgPath, String url) {
		super(TRANS_TYPE_MBLOG, channel);
		
		mShareBind = shareBind;
		mTitle = title;
		mChannel = channel;
		mContent = content;
		mImgPath = imgPath;
		mUrl = url;
	}
	
	public ShareTencentMBlogTransaction(ShareChannelTencent channel, 
            ShareBind shareBind, String title, String content,
            String imgPath, boolean uploadLocalImage) {
        super(TRANS_TYPE_MBLOG, channel);
        
        mShareBind = shareBind;
        mTitle = title;
        mChannel = channel;
        mContent = content;
        mImgPath = imgPath;
    }
	
	@Override
	protected void onTransactionSuccess(int code, Object obj) {
		if (! isCancel()) {
			if (obj != null && obj instanceof JSONObject) {
				JSONObject json = (JSONObject) obj;
				
				if (json.optInt("ret") == 0) {
					ShareResult result = new ShareResult(mChannel.getShareType(), true);
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
			return ;
		}
		else {
			
			List<NameValuePair> list = new LinkedList<NameValuePair>();
			//共用参数
			list.add(new BasicNameValuePair("access_token", mShareBind.getAccessToken()));
			list.add(new BasicNameValuePair("oauth_consumer_key", mChannel.getClientID()));
			list.add(new BasicNameValuePair("openid", mShareBind.getUserID()));
			list.add(new BasicNameValuePair("format", "json"));
			list.add(new BasicNameValuePair("title", mTitle == null ? "" : mTitle));
			
			//私有参数
			if(!TextUtils.isEmpty(mUrl)){
			    list.add(new BasicNameValuePair("site", ShareChannelTencent.CLIENT_NAME));
			    list.add(new BasicNameValuePair("fromurl", ShareChannelTencent.CLIENT_URL));
			    
			    list.add(new BasicNameValuePair("url", mUrl == null ? "" : mUrl));
			    
			    if (mContent != null) {
			        list.add(new BasicNameValuePair("summary", mContent));
			    }
			    THttpRequest request = new THttpRequest(mChannel.getSendMBlogUrl(),
	                    THttpMethod.POST);
			    try {
			        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, "utf-8");
			        request.setHttpEntity(entity);
			    } catch (Exception e) {
			        ShareResult result = new ShareResult(mChannel.getShareType(), false);
		            result.setMessage("UnsupportedEncodingException");
		            notifyError(0, result);
		            doEnd();
		            return ;
			    }
			    sendRequest(request);
			}else{
			    if(TextUtils.isEmpty(mImgPath)){
			        ShareResult result = new ShareResult(mChannel.getShareType(), false);
                    result.setMessage("分享的图片路径为空");
                    notifyError(0, result);
                    doEnd();
                    return ;
			    }
			    File file = new File(mImgPath);
	            String fileName = file.getName();
	            FilePart imgPart = null ;
	            try {
	                imgPart = new FilePart("picture" , fileName ,file);
	            } catch (FileNotFoundException e) {
	                ShareResult result = new ShareResult(mChannel.getShareType(), false);
	                result.setMessage("未找到要上传的图片");
	                notifyError(0, result);
	                doEnd();
	                return ;
	            }
	            
	            //desc of pic
	            list.add(new BasicNameValuePair("photodesc",TextUtils.isEmpty(mContent)? "" : mContent));
	            list.add(new BasicNameValuePair("mobile","1"));
	            list.add(new BasicNameValuePair("needfeed","1"));
	            
	            if (imgPart != null) {
	                Part[] parts = new Part[list.size() + 1];
	                for (int i = 0; i < list.size(); i++) {
	                    NameValuePair pair = list.get(i);
	                    parts[i] = new StringPart(pair.getName(), 
	                            pair.getValue(), "utf-8");
	                }
	                
	                parts[list.size()] = imgPart;
	                
	                MultipartEntity entity = new MultipartEntity(parts);
	                THttpRequest requestUplode = new THttpRequest(mChannel.getUpLoadLocalUrl(),
	                        THttpMethod.POST);
	                requestUplode.setHttpEntity(entity);
	                sendRequest(requestUplode);
	            } 
			}
		}
	}

}
