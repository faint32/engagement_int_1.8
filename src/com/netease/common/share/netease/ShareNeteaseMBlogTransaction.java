package com.netease.common.share.netease;

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

public class ShareNeteaseMBlogTransaction extends ShareBaseTransaction {

	private static final int PARSE_UPLOAD_IMAGE = 0;
	private static final int PARSE_SEND_MBLOG = 1;
	
	int mParse;
	
	ShareChannelNetease mChannel;
	ShareBind mShareBind;
	String mContent;
	String mImgPath;
	
	protected ShareNeteaseMBlogTransaction(ShareChannelNetease channel, 
			ShareBind shareBind, String content, String imgPath) {
		super(TRANS_TYPE_MBLOG, channel);
		
		mChannel = channel;
		mShareBind = shareBind;
		mContent = content;
		mImgPath = imgPath;
	}
	
	@Override
	protected void onTransactionError(int errCode, Object obj) {
		if (mParse == PARSE_UPLOAD_IMAGE) {
			mParse = PARSE_SEND_MBLOG;
			
			getTransactionEngine().beginTransaction(this);
		}
		else {
			super.onTransactionError(errCode, obj);
		}
	}

	@Override
	protected void onTransactionSuccess(int code, Object obj) {
		if (! isCancel()) {
			if (mParse == PARSE_UPLOAD_IMAGE) {
				if (obj != null && obj instanceof JSONObject) {
					JSONObject json = (JSONObject) obj;
					mContent += json.optString("upload_image_url");
				}
				
				mParse = PARSE_SEND_MBLOG;
				getTransactionEngine().beginTransaction(this);
				
			} else {
				ShareResult result = new ShareResult(mChannel.getShareType(), true);
				notifyMessage(0, result);
			}
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
			
			if (mParse == PARSE_UPLOAD_IMAGE) {
				request = createBlogUpload(mShareBind, mImgPath);
			}
			
			if (request == null) {
				mParse = PARSE_SEND_MBLOG;
				request = createBlogSend(mShareBind, mContent, null, null, null);
			}
			
			sendRequest(request);
		}
	}
	
	/**
	 * 上传图片
	 * @param file
	 * @return
	 */
	private THttpRequest createBlogUpload(ShareBind shareBind, String file) {
		if (TextUtils.isEmpty(file)) {
			return null;
		}
		
		FilePart filePart = null;
		try {
			if (file.startsWith(ResFilePartSource.NAME_PREFIX)) {
				filePart = new FilePart("pic", new ResFilePartSource(
						BaseService.getServiceContext(), file, "upload.jpg"));
			}
			else {
				filePart = new FilePart("pic", "upload.jpg", new File(file));
			}
		} catch (Exception e) {
			return null;
		}
		
		MultipartEntity entity = new MultipartEntity(new Part[]{filePart,
				new StringPart("access_token", shareBind.getAccessToken())
				});
		
		THttpRequest request = new THttpRequest(mChannel.getUploadImgUrl(), 
				THttpMethod.POST);
		request.setHttpEntity(entity);
		
		return request;
	}

	/**
	 * 发布一条微博。
	 * 
	 * @param status
	 *            必选参数，微博内容，不得超过163个字符；
	 * @param in_reply_to_status_id
	 *            可选参数，当评论指定微博时需带上此参数，值为被回复的微博ID；
	 * @param 
	 * @return
	 */
	private THttpRequest createBlogSend(ShareBind shareBind, String status,
			String latitude, String longitude, String vid) {
		List<NameValuePair> list = new LinkedList<NameValuePair>();
		list.add(new BasicNameValuePair("access_token", shareBind.getAccessToken()));
		list.add(new BasicNameValuePair("status", status));

		if (!TextUtils.isEmpty(latitude)) {
			list.add(new BasicNameValuePair("lat", latitude));
		}
		if (!TextUtils.isEmpty(longitude)) {
			list.add(new BasicNameValuePair("long", longitude));
		}
		if (!TextUtils.isEmpty(vid)) {
			list.add(new BasicNameValuePair("vid", vid));
		}

		THttpRequest request = new THttpRequest(mChannel.getSendMBlogUrl(),
				THttpMethod.POST);
		try {
			request.setHttpEntity(new UrlEncodedFormEntity(list, "utf-8"));
		} catch (Exception e) {
		}

		return request;
	}

}
