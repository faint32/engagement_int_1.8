package com.netease.service.transactions;

import java.util.ArrayList;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.netease.common.task.Transaction;
import com.netease.engagement.adapter.MsgListCursorAdapter;
import com.netease.engagement.dataMgr.MemoryDataCenter;
import com.netease.engagement.pushMsg.NotificationBarMgr;
import com.netease.service.app.BaseApplication;
import com.netease.service.db.manager.LastMsgDBManager;
import com.netease.service.db.manager.LastMsgDBManager.NotificationMsg;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.db.manager.MsgDBManager;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmProtocolConstants.MSG_TYPE;
import com.netease.service.protocol.EgmProtocolConstants.SYS_MSG_TYPE;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.ChatItemInfo;
import com.netease.service.protocol.meta.ChatItemUserInfo;
import com.netease.service.protocol.meta.MessageInfo;
import com.netease.service.protocol.meta.MsgExtra;
import com.netease.util.PDEEngine;



public class PushMsgTransaction extends Transaction {
    private String jsonMsg;

    public PushMsgTransaction(String msg) {
        super(EgmBaseTransaction.TRANSACTION_TYPE_HANDLE_PUSH_MSG);
        jsonMsg = msg;
    }

    @Override
    public void onTransact() {
        if(TextUtils.isEmpty(jsonMsg)){
        	return ;
        }
        
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonArray Jarray = parser.parse(jsonMsg).getAsJsonArray();
        
        if (Jarray == null) {
        	return ;
        }

        ArrayList<ChatItemInfo> list = new ArrayList<ChatItemInfo>();
            
		long currentUid = -1;

		if (null != MemoryDataCenter.getInstance().get(
				MemoryDataCenter.CURRENT_CHAT_UID)) {
			currentUid = (Long) MemoryDataCenter.getInstance().get(
					MemoryDataCenter.CURRENT_CHAT_UID);
		}
		
		long myId = 0;
		String myIdStr = ManagerAccount.getInstance().getCurrentAccountId();
		
		if (!TextUtils.isEmpty(myIdStr)) {
			myId = Long.valueOf(myIdStr);
		}
        
		try {
			boolean notifyMsg = false;
			
			for (JsonElement obj : Jarray) {
				JsonElement message = obj.getAsJsonObject().get("message");
				String content = message.getAsJsonObject().get("content").getAsString();
				
				ChatItemStringInfo stringInfo = gson.fromJson(content,
								ChatItemStringInfo.class);
				
				if (stringInfo == null) {
					continue;
				}

				ChatItemInfo info = new ChatItemInfo();
				info.notReadCount = stringInfo.notReadCount;
				info.anotherUserInfo = gson.fromJson(
						stringInfo.anotherUserInfo, ChatItemUserInfo.class);
				
				if (info.anotherUserInfo.uid == myId) {// 异常保护，cancelbind失败等情况，push收到自己发的消息
					continue;
				}
				
				info.message = gson.fromJson(stringInfo.message, MessageInfo.class);
				if (info.message == null) {
					continue;
				}
				
				if (info.message.ver > 0) {
					switch (info.message.type) {
					case MSG_TYPE.MSG_TYPE_TEXT:
					case MSG_TYPE.MSG_TYPE_SYS:
						info.message.msgContent = PDEEngine.PXDecrypt(info.message.msgContent);
						break;
					}
				}
				else if (info.message.isFireMsg()) {
					if (info.message.type == EgmProtocolConstants.MSG_TYPE.MSG_TYPE_TEXT) {
						info.message.msgContent = PDEEngine.PXDecrypt(info.message.msgContent);
					}
				}
				
				int sysMsgType = -1;
				if (info.message.type == EgmProtocolConstants.MSG_TYPE.MSG_TYPE_SYS
						&& info.message.extra != null) {
					MsgExtra extra = MsgExtra.toMsgExtra(info.message.extra.toString());
					if (extra != null) {
						sysMsgType =  extra.sysMsgType;
					}
				}
				
				switch (sysMsgType) {
				case SYS_MSG_TYPE.SYSMSG_TYPE_FIRE_SCREEN_SHOT_NOTICE:
					if (currentUid != info.anotherUserInfo.uid) {
						NotificationBarMgr.getInstance(
								BaseApplication.getAppInstance())
								.showPushScreenShot(info.anotherUserInfo);
					} else {
						list.add(info);
					}
					break;
					
				case SYS_MSG_TYPE.SYSMSG_TYPE_MSG_FIRED_NOTICE:
					long msgId = info.message.msgId;
					MessageInfo msg = MsgDBManager.getMsg(myId, msgId);
					
					if (msg != null) {
						if (currentUid == msg.receiver) {
							MsgListCursorAdapter.setFireStart(msg);
						}
						else {
							MsgDBManager.delMsgWithResource(msg);
						}
					}
					break;
					
				default:
					if (currentUid < 0 || currentUid != info.anotherUserInfo.uid){
						notifyMsg = true;
					}
					
					LastMsgDBManager.handelNewMsg(info);// 插入lastMsg数据库
					MsgDBManager.insertMsg(info.message);// 插入msg数据库

					list.add(info);
					if (currentUid == info.anotherUserInfo.uid) {// 统计在当前聊天的消息数
						
					} 
					else if (info.message.type == EgmProtocolConstants.MSG_TYPE.MSG_TYPE_SYS) {
						MsgExtra extra = MsgExtra.toMsgExtra(info.message.extraString);
						if (extra != null
								&& extra.sysMsgType == EgmProtocolConstants.SYS_MSG_TYPE.SYSMSG_TYPE_ACTIVITIES) {
							NotificationBarMgr.getInstance(
									BaseApplication.getAppInstance())
									.showPushActivities(info.message.msgContent,
											extra);
						}
					}
					break;
				}
			}

			if (EgmPrefHelper.getNeedPush(BaseApplication.getAppInstance()) && notifyMsg) {
				NotificationMsg notify = null;
				if (currentUid > 0) {
					notify = LastMsgDBManager.getNotificationMsg(currentUid);
				}
				else {
					notify = LastMsgDBManager.getNotificationMsg(0);
				}

				if (notify != null) {
					NotificationBarMgr.getInstance(
							BaseApplication.getAppInstance()).showPushMessage(
							notify.user, notify.msgType, notify.chatCount,
							notify.unreadNum, notify.extra, notify.fire);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, list);
	}
    
    public static class ChatItemStringInfo {
        public int notReadCount ;//未读消息个数
        public JsonElement message ;//消息内容
        public JsonElement anotherUserInfo ;//对方的userinfo 
    }
}
