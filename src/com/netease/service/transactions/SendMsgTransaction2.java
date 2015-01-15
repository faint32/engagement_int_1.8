package com.netease.service.transactions;

import java.io.File;
import java.net.URI;

import android.text.TextUtils;
import android.webkit.URLUtil;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.common.image.util.ImageUtil;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.GiftInfoManager;
import com.netease.engagement.dataMgr.MsgAttach;
import com.netease.engagement.util.LevelChangeStatusBean;
import com.netease.engagement.util.LevelChangeStatusBean.LevelChangeType;
import com.netease.pkgRelated.ErrorToString;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.app.BaseApplication;
import com.netease.service.db.manager.LastMsgDBManager;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.db.manager.MsgDBManager;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.MessageInfo;
import com.netease.service.protocol.meta.SendMsgResult;
import com.netease.util.PDEEngine;

public class SendMsgTransaction2 extends EgmBaseTransaction {

	private MessageInfo mMsgInfo;
	private String mFilePath;
	private String mFilePathTemp;

	public SendMsgTransaction2(MessageInfo info, String filePath) {
		super(TRANSACTION_SEND_MSG);
		mMsgInfo = info;
		mFilePath = filePath;
	}

	@Override
	public void onTransact() {
		if (mMsgInfo.type == EgmProtocolConstants.MSG_TYPE.MSG_TYPE_LOCAL_PIC) {// 图片压缩
			if (!TextUtils.isEmpty(mFilePath)) {
				File file = null;
				String path = null;

				if (URLUtil.isFileUrl(mFilePath)) {
					file = new File(URI.create(mFilePath));
				} else {
					file = new File(mFilePath);
				}

				path = file.getPath();

				String tempFile = EgmUtil.getCacheDir().toString();
				tempFile += System.currentTimeMillis() + "_temp.jpg";
				
				if (ImageUtil.saveResizeTmpFile(path, tempFile,
						EgmProtocolConstants.SIZE_MAX_PICTURE,
						EgmProtocolConstants.PIC_QULITY)) {
					File fileTemp = new File(tempFile);
					if (fileTemp.exists()) {
						mFilePathTemp = tempFile;
					}
				}
			}
		}
		

		THttpRequest request = EgmProtocol.getInstance().createSendMsg(
				mMsgInfo, TextUtils.isEmpty(mFilePathTemp) ? mFilePath : mFilePathTemp);
		sendRequest(request);
	}

	@Override
	protected void onEgmTransactionSuccess(int code, Object obj) {
		SendMsgResult result = null;
		if (obj != null && obj instanceof JsonElement) {
			Gson gson = new Gson();
			JsonElement json = (JsonElement) obj;
			result = gson.fromJson(json, SendMsgResult.class);
		}

		if (result == null) {
			onEgmTransactionError(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION,
					ErrorToString.getString(EgmServiceCode.ERR_CODE_DATA_PARSE_EXCEPTION));
			return;
		}

		if (code == EgmServiceCode.TRANSACTION_CHAT_KEYWORDS_BLOCKED) {
			// 因关键词过滤导致消息发送失败，需要返回内容,同时返回的tips及matchType需要记录在消息数据库
			MsgAttach attach;
			if (!TextUtils.isEmpty(mMsgInfo.attach)) {
				attach = MsgAttach.toMsgAttach(mMsgInfo.attach);
			} else {
				attach = new MsgAttach();
			}
			attach.matchType = result.matchType;
			attach.tips = result.tips;
			mMsgInfo.attach = MsgAttach.toJsonString(attach);
			onEgmTransactionError(code, "");
			return;
		}
		// 发送成功
		MessageInfo resultMsg = result.messageInfo;
		mMsgInfo.status = EgmConstants.Sending_State.SEND_SUCCESS;

		// changed by echo_chen 2014-07-22 修复对方黑名单，消息一直不显示发送成功
		if (resultMsg != null) {
			resultMsg.msgContent = PDEEngine.PXDecrypt(resultMsg.msgContent);
			
			mMsgInfo.msgId = resultMsg.msgId;
			if (mMsgInfo.type == EgmProtocolConstants.MSG_TYPE.MSG_TYPE_GIFT) {
				mMsgInfo.setUsercp(resultMsg.usercp);

				GiftInfoManager.reduceSpecialGift((int) resultMsg.extraId);
			}
		}
		
		MsgDBManager.updataMsgState(mMsgInfo, resultMsg == null ? mMsgInfo.time
				: resultMsg.time);
		if (resultMsg != null) {
			mMsgInfo.time = resultMsg.time;
		}
		LastMsgDBManager.handleNewMsg(mMsgInfo);

		if (mMsgInfo.type == EgmProtocolConstants.MSG_TYPE.MSG_TYPE_GIFT) {
			
			long uid = ManagerAccount.getInstance().getCurrentId();
			int oldLevel = EgmPrefHelper.getUserLevel(
					BaseApplication.getAppInstance(), uid);
			int newLevel = result.userLevel;
			if (oldLevel != 0 && oldLevel < newLevel) { // 判断男性是否升级
				LevelChangeStatusBean status = LevelChangeStatusBean.getInstance();
				
				status.set(uid, LevelChangeType.Male_Level_Up, oldLevel, newLevel);
			}

			EgmPrefHelper.putUserLevel(BaseApplication.getAppInstance(), uid,
					newLevel);
		}
		
		OnSendFinished();
		
		notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, result);
	}

	@Override
	protected void onEgmTransactionError(int errCode, Object obj) {
		OnSendError();
		OnSendFinished();
		super.onEgmTransactionError(errCode, obj);
	}

	private void OnSendError() {
		mMsgInfo.status = EgmConstants.Sending_State.SEND_FAIL;
		mMsgInfo.msgId = -mMsgInfo.time;

		MsgDBManager.updataMsgState(mMsgInfo);
		LastMsgDBManager.handleNewMsg(mMsgInfo);
	}
	
	private void OnSendFinished() {
		if (!TextUtils.isEmpty(mFilePathTemp)) {
			File file = new File(mFilePathTemp);
			if (file.exists()) {
				file.delete();
			}
		}
	}

}
