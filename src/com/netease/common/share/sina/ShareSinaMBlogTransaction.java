package com.netease.common.share.sina;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

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

public class ShareSinaMBlogTransaction extends ShareBaseTransaction {

	ShareChannelSina mChannel;
	ShareBind mShareBind;
	String mContent;
	String mImgPath;
	
	protected ShareSinaMBlogTransaction(ShareChannelSina channel, 
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
			list.add(new BasicNameValuePair("status", mContent));
			list.add(new BasicNameValuePair("access_token", mShareBind.getAccessToken()));
			
			if (imgPart != null) {
				Part[] parts = new Part[list.size() + 1];
				for (int i = 0; i < list.size(); i++) {
					NameValuePair pair = list.get(i);
					parts[i] = new StringPart(pair.getName(), pair.getValue(), "utf-8");
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

// 	必选	类型及范围	说明
//access_token	false	string	采用OAuth授权方式为必填参数，其他授权方式不需要此参数，OAuth授权后获得。
//status	true	string	要发布的微博文本内容，必须做URLencode，内容不超过140个汉字。
//visible	false	int	微博的可见性，0：所有人能看，1：仅自己可见，2：密友可见，3：指定分组可见，默认为0。
//list_id	false	string	微博的保护投递指定分组ID，只有当visible参数为3时生效且必选。
//pic	true	binary	要上传的图片，仅支持JPEG、GIF、PNG格式，图片大小小于5M。
//lat	false	float	纬度，有效范围：-90.0到+90.0，+表示北纬，默认为0.0。
//long	false	float	经度，有效范围：-180.0到+180.0，+表示东经，默认为0.0。
//annotations	false	string	元数据，主要是为了方便第三方应用记录一些适合于自己使用的信息，每条微博可以包含一个或者多个元数据，必须以json字串的形式提交，字串长度不超过512个字符，具体内容可以自定。

}
