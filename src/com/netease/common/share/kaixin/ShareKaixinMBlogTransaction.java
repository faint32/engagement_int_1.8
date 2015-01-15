package com.netease.common.share.kaixin;

import java.io.File;
import java.util.ArrayList;

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

public class ShareKaixinMBlogTransaction extends ShareBaseTransaction {

	ShareChannelKaixin mChannel;
	ShareBind mShareBind;
	
	String mContent;
	String mImgPath;
	
	String mSaveToAlbum;
	String mLocation;
	String mLat;
	String mLon;
	String mSyncStatus;
	String mSpri;
	String mPicUrl;
	
	protected ShareKaixinMBlogTransaction(ShareChannelKaixin channel, 
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
			ShareResult result = new ShareResult(mChannel.getShareType(), true);
			notifyMessage(0, result);
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
			THttpRequest request = createBlogSend(mShareBind);
			sendRequest(request);
		}
	}
	
	private THttpRequest createBlogSend(ShareBind shareBind) {
		String url = mChannel.getSendMBlogUrl();
			ArrayList<Part> parts = new ArrayList<Part>();
			parts.add(new StringPart("access_token", shareBind.getAccessToken()));
			parts.add(new StringPart("content", mContent, "utf-8"));
			
			if (! TextUtils.isEmpty(mSaveToAlbum)) {
				parts.add(new StringPart("save_to_album", mSaveToAlbum, "utf-8"));
			}
			if (! TextUtils.isEmpty(mLocation)) {
				parts.add(new StringPart("location", mLocation, "utf-8"));
			}
			if (! TextUtils.isEmpty(mLat)) {
				parts.add(new StringPart("lat", mLat, "utf-8"));
			}
			if (! TextUtils.isEmpty(mLon)) {
				parts.add(new StringPart("lon", mLon, "utf-8"));
			}
			if (! TextUtils.isEmpty(mSyncStatus)) {
				parts.add(new StringPart("sync_status", mSyncStatus, "utf-8"));
			}
			if (! TextUtils.isEmpty(mSpri)) {
				parts.add(new StringPart("spri", mSpri, "utf-8"));
			}
			if (! TextUtils.isEmpty(mPicUrl)) {
				parts.add(new StringPart("picurl", mPicUrl, "utf-8"));
			}
			
			try {
				if (! TextUtils.isEmpty(mImgPath)) {
					if (mImgPath.startsWith(ResFilePartSource.NAME_PREFIX)) {
						parts.add(new FilePart("pic", new ResFilePartSource(
								BaseService.getServiceContext(), mImgPath)));
					}
					else {
						File file = new File(mImgPath);
						if (file.exists()){
							parts.add(new FilePart("pic", new File(mImgPath)));
						}
					}
				}
			} catch (Exception e) { }
			
			Part[] partArr = new Part[parts.size()];
			parts.toArray(partArr);
			MultipartEntity entity = new MultipartEntity(partArr);


			THttpRequest request = null;
			request = new THttpRequest(url, THttpMethod.POST);
			request.setHttpEntity(entity);

			return request;
	}
}
