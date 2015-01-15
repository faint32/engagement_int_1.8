package com.netease.common.share;

import com.netease.common.http.HttpDataChannel;
import com.netease.common.http.HttpEngine;
import com.netease.common.service.BaseService;
import com.netease.common.share.base.ShareBaseChannel;
import com.netease.common.share.douban.ShareChannelDouban;
import com.netease.common.share.kaixin.ShareChannelKaixin;
import com.netease.common.share.netease.ShareChannelNetease;
import com.netease.common.share.qqmblog.ShareChannelQqmblog;
import com.netease.common.share.renren.ShareChannelRenren;
import com.netease.common.share.sina.ShareChannelSina;
import com.netease.common.share.sohu.ShareChannelSohu;
import com.netease.common.share.tencent.ShareChannelTencent;
import com.netease.common.task.TransactionEngine;
import com.netease.common.task.TransactionListener;

/**
 * 
 * 
 * @author dingding
 *
 */
public class ShareService extends BaseService {

	private static ShareService mInstance;
	
	/**
	 * 前置key，查询时使用
	 */
	private String mPreferKey;
	
	private ShareService() {
		super(new HttpDataChannel(TransactionEngine.Instance(),
				HttpEngine.Instance()));
		mPreferKey = "";
	}
	
	public static ShareService getShareService() {
		if (mInstance == null) {
			mInstance = new ShareService();
		}
		
		return mInstance;
	}
	
	/**
	 * 设置Prefer Key
	 * @param preferKey
	 */
	public void setPreferKey(String preferKey) {
		if (preferKey == null) {
			preferKey = "";
		}
		mPreferKey = preferKey;
	}
	
	/**
	 * 返回Prefer Key
	 * @return
	 */
	public String getPreferKey() {
		return mPreferKey;
	}
	
	/**
	 * 发送微博
	 * 
	 * @param shareType
	 * @param title
	 * @param content
	 * @param imgPath
	 * @return
	 */
	public int sendMBlog(ShareType shareType, String title, String content, 
			String imgPath, String url, TransactionListener listener) {
		ShareBaseChannel channel = createShareChannel(shareType);
		channel.setShareListener(listener);
		return channel.sendMBlog(title, content, imgPath, url);
	}
	
	/**
	 * 发送微博
	 * 
	 * @param shareBind
	 * @param title
	 * @param content
	 * @param imgPath
	 * @return
	 */
	public int sendMBlog(ShareBind shareBind, String title, String content, 
			String imgPath, String url, TransactionListener listener) {
		ShareBaseChannel channel = createShareChannel(shareBind.getShareType());
		channel.setShareListener(listener);
		return channel.sendMBlog(shareBind, title, content, imgPath, url);
	}
	
	/**
	 * 获取关注列表
	 * 
	 * @param shareType
	 * @param shareBind 为空时表示使用默认绑定的账号信息
	 * @param listener
	 * @return
	 */
	public int getFollowingList(ShareType shareType, ShareBind shareBind, 
			TransactionListener listener) {
		ShareBaseChannel channel = createShareChannel(shareType);
		channel.setShareListener(listener);
		return channel.getFollowingList(shareBind);
	}
	
	public static ShareBaseChannel createShareChannel(ShareType shareType) {
		ShareBaseChannel channel = null;
		switch (shareType) {
		case Sina:
			channel = new ShareChannelSina();
			break;
		case Netease:
			channel = new ShareChannelNetease();
			break;
		case Qqmblog:
			channel = new ShareChannelQqmblog();
			break;
		case Tencent:
			channel = new ShareChannelTencent();
			break;
		case Renren:
			channel = new ShareChannelRenren();
			break;
		case Douban:
			channel = new ShareChannelDouban();
			break;
		case Kaixin:
			channel = new ShareChannelKaixin();
			break;
		case Sohu:
			channel = new ShareChannelSohu();
			break;
		}
		
		return channel;
	}
}
