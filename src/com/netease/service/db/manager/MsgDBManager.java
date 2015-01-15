package com.netease.service.db.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.netease.common.cache.CacheManager;
import com.netease.common.cache.file.StoreFile;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.MsgAttach;
import com.netease.engagement.util.LevelChangeStatusBean;
import com.netease.engagement.util.LevelChangeStatusBean.LevelChangeType;
import com.netease.service.app.BaseApplication;
import com.netease.service.db.MsgDBProvider;
import com.netease.service.db.MsgDBTables.LastMsgTable;
import com.netease.service.db.MsgDBTables.MsgTable;
import com.netease.service.db.manager.ManagerAccount.Account;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.meta.ChatItemInfo;
import com.netease.service.protocol.meta.ChatItemUserInfo;
import com.netease.service.protocol.meta.MessageInfo;
import com.netease.util.PDEEngine;


/**
 * 消息表
 */
public class MsgDBManager {
    private static MsgDBProvider mDBProvider;
    
    private static MsgDBProvider getDBProvider() {
         MsgDBProvider provider = mDBProvider;
        if (provider == null) {
            provider = new MsgDBProvider();
            mDBProvider = provider;
        }
        
        return provider;
    }

    /**
     * 往消息列表中插入一条消息
     */
    public static void insertMsg(MessageInfo msg) {
        if (null == msg) {
            return ;
        }
        ContentValues values = new ContentValues();
        

        
        // 原来的设置方式，C_FROMID必然是sender，这不对。
//      long me = Long.valueOf(ManagerAccount.getInstance().getCurrentAccountId());
//      if(me == msg.receiver){//another 为聊天对方
//          values.put(MsgTable.C_ANOTHERID, msg.sender);
//      } else {
//          values.put(MsgTable.C_ANOTHERID, msg.receiver);
//      }
//      values.put(MsgTable.C_FROMID, msg.sender);
        // 改成与insertMsgInfo方法一样的设置方式
        values.put(MsgTable.C_FROMID,String.valueOf(msg.sender));
        values.put(MsgTable.C_ANOTHERID, String.valueOf(msg.receiver));
        
        
        values.put(MsgTable.C_FROMID, msg.sender);
        values.put(MsgTable.C_TIME, msg.time);
        
        msg.setEncrypted();
        values.put(MsgTable.C_CONTENT, PDEEngine.PXEncrypt(msg.msgContent));
        
        if (null != msg.mediaUrl) {
            values.put(MsgTable.C_MEDIA_URL, msg.mediaUrl);
        }
        values.put(MsgTable.C_TYPE, msg.type);
        values.put(MsgTable.C_MSGID, msg.msgId);
        values.put(MsgTable.C_DURATION, msg.duration);
        values.put(MsgTable.C_EXTRA_ID, msg.extraId);
        if (null == msg.extraString && msg.extra != null){
            msg.extraString = msg.extra.toString();
        }
        if (null != msg.extraString) {
            values.put(MsgTable.C_EXTRA, msg.extraString);
        }
        values.put(MsgTable.C_USERCP, msg.usercp);
        
        //设置消息状态
        values.put(MsgTable.C_STATUS, EgmConstants.Sending_State.SEND_SUCCESS);
        
        //设置未播放标识    这个标只对音频类型的消息有效
        values.put(MsgTable.C_RESERVED1, "0");
        
        values.put(MsgTable.C_RESERVED3, msg.getReserved3());
        
        if (msg.type == EgmProtocolConstants.MSG_TYPE.MSG_TYPE_FACE) {
        	values.put(MsgTable.C_RESERVED2, msg.getFaceId());
        } else if (msg.type == EgmProtocolConstants.MSG_TYPE.MSG_TYPE_GIFT) {
        	values.put(MsgTable.C_RESERVED2, msg.getAnimat());
        }
        
        if(!TextUtils.isEmpty(msg.tips)){//tips共用MsgAttach的tips
        		MsgAttach attach;
		    	if(!TextUtils.isEmpty(msg.attach)){
		    		attach = MsgAttach.toMsgAttach(msg.attach);
			 }else{
				attach = new MsgAttach();
			 }
		    	attach.tips = msg.tips;
		    	msg.attach = MsgAttach.toJsonString(attach);
        }
        if (null != msg.attach) {
            values.put(MsgTable.C_ATTACH, msg.attach);
        }
        getDBProvider().insert(MsgTable.CONTENT_URI, values);
        
        if (ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Female) {
	        if (msg.type == EgmProtocolConstants.MSG_TYPE.MSG_TYPE_GIFT) {
	        	long uid = ManagerAccount.getInstance().getCurrentId();
	        	int oldLevel = EgmPrefHelper.getUserLevel(BaseApplication.getAppInstance().getApplicationContext(), uid);
	        	int newLevel = msg.userLevel;
	        	if(oldLevel!=0 && oldLevel<newLevel) { // 判断女性是否升级
	        		LevelChangeStatusBean status = LevelChangeStatusBean.getInstance();
	        		if(status.getType()!=LevelChangeType.Female_Level_Up) {
	        			status.set(uid, LevelChangeType.Female_Level_Up, oldLevel, newLevel);
	        			EgmPrefHelper.putUserLevel(BaseApplication.getAppInstance().getApplicationContext(), uid, newLevel);
	        		} else {
	        			if (newLevel > status.getNewLevel()) {
	        				status.set(uid, LevelChangeType.Female_Level_Up, status.getOldLevel(), newLevel);
	        				EgmPrefHelper.putUserLevel(BaseApplication.getAppInstance().getApplicationContext(), uid, newLevel);
	        			}
	        		}
	        	}
	        }
        }
    }
    
    /**
     * 根据聊天对方id查询msg
     */
    public static List<MessageInfo> getMsgListByUser(long chatUid){
        List<MessageInfo> list = new ArrayList<MessageInfo>();
        String selection = MsgTable.C_ANOTHERID + "=?";
        String[] selectionArgs = new String[] {
            String.valueOf(chatUid)
        };
        Cursor c = getDBProvider().query(MsgTable.CONTENT_URI, null, selection, selectionArgs,
                null);
        if(c != null){
            for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
                MessageInfo info = MessageInfo.getMessageInfo(c);
                list.add(info);
            }
            c.close();
        }
        return list;
    }
    
    
    
    public static List<Long> getGiftListBySenderAndReceiver(long senderUid, long receiverUid) {
    	List<Long> list = new ArrayList<Long>();
    	String selection = MsgTable.C_FROMID + "=? And " 
    					 + MsgTable.C_ANOTHERID + "=? And " 
    					 + MsgTable.C_TYPE + "=" + EgmProtocolConstants.MSG_TYPE.MSG_TYPE_GIFT;
    	String[] selectionArgs = new String[] {
                String.valueOf(senderUid),
                String.valueOf(receiverUid),
            };
    	String[] projection = new String[] {
    			"DISTINCT " + MsgTable.C_EXTRA_ID
    	};
    	Cursor c = getDBProvider().query(MsgTable.CONTENT_URI, projection, selection, selectionArgs, null);
    	if(c != null) {
    		int index = c.getColumnIndex(MsgTable.C_EXTRA_ID);
    		
    		for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
    			long giftId = c.getLong(index);
    			list.add(giftId);
    		}
    		c.close();
    	}
    	return list;
    }
    
    /**
     * 本地处理消息发送
     * 根据不同的消息类型插入不同的内容
     */
    public static void insertMsgInfo(MessageInfo info){
    	if(info == null){
    		return ;
    	}
    	ContentValues values = new ContentValues();
    	values.put(MsgTable.C_MSGID,String.valueOf(info.msgId));
    	values.put(MsgTable.C_FROMID,String.valueOf(info.sender));
    	values.put(MsgTable.C_ANOTHERID, String.valueOf(info.receiver));
    	values.put(MsgTable.C_TIME,info.time);
    	values.put(MsgTable.C_TYPE,info.type);
    	values.put(MsgTable.C_STATUS,info.status);
    	
    	
    	info.setEncrypted();
    	
    	//设置未播放标识    这个标只对音频类型的消息有效
        values.put(MsgTable.C_RESERVED1, "0");
        
        values.put(MsgTable.C_RESERVED3, info.getReserved3());
        
        if (info.type == EgmProtocolConstants.MSG_TYPE.MSG_TYPE_FACE) {
        	values.put(MsgTable.C_RESERVED2, info.getFaceId());
        } else if (info.type == EgmProtocolConstants.MSG_TYPE.MSG_TYPE_GIFT) {
        	values.put(MsgTable.C_RESERVED2, info.getAnimat());
        }
    	
    	switch(info.type){
	    	case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_TEXT:
	    		values.put(MsgTable.C_CONTENT, PDEEngine.PXEncrypt(info.msgContent));
	    		break;
	    	case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_LOCAL_PIC:
	    		values.put(MsgTable.C_ATTACH,info.attach);
	    		values.put(MsgTable.C_MEDIA_URL,info.mediaUrl);
	    		break;
	    	case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_AUDIO:
	    		values.put(MsgTable.C_DURATION,info.duration);
	    		values.put(MsgTable.C_ATTACH,info.attach);
	    		break;
	    	case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_PRIVATE_PIC:
	    		values.put(MsgTable.C_EXTRA_ID,info.extraId);
	    		values.put(MsgTable.C_MEDIA_URL,info.mediaUrl);
	    		values.put(MsgTable.C_ATTACH,info.attach);
	    		break;
	    	case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_GIFT:
	    		values.put(MsgTable.C_EXTRA_ID,info.extraId);
	    		values.put(MsgTable.C_USERCP,info.usercp);
	    		break;
	    	case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_VIDEO:
	    		values.put(MsgTable.C_DURATION,info.duration);
	    		values.put(MsgTable.C_ATTACH,info.attach);
	    		break;
    	}
    	getDBProvider().insert(MsgTable.CONTENT_URI, values);
    }
    
    /**
     * 获取消息列表
     * @param time
     * @param receiverId
     * @param senderId
     * @return
     */
    public static Cursor getMsgList(long time,int pageNo,long receiverId ,long senderId){
    	Cursor cursor = null ;
    	StringBuilder inStr = new StringBuilder();
    	inStr.append("(").append(receiverId).append(",").append(senderId).append(")");
    	
    	StringBuilder selection = new StringBuilder();
    	selection.append(MsgTable.C_ANOTHERID).append(" IN ").append(inStr.toString())
		 	.append(" AND ")
		 	.append(MsgTable.C_FROMID).append(" IN ").append(inStr.toString())
		 	.append(" AND ").append(MsgTable.C_TIME).append(">=?");
    	
    	String[] args = new String[] {
                String.valueOf(time),
            };
    	
    	StringBuilder orderBy = new StringBuilder();
    	orderBy.append(MsgTable.C_TIME).append(" ASC ").append("limit ").append((pageNo + 1)*EgmConstants.CHAT_LIST_PAGE_NUM)
    		.append(" offset ").append(getOffset(getMsgCount(receiverId,senderId),pageNo));
    	
    	cursor = getDBProvider().query(MsgTable.CONTENT_URI,null, selection.toString(), args, orderBy.toString());
    	return cursor ;
    }
    
    private static int getOffset(int count,int pageNo){
    	if(count < EgmConstants.CHAT_LIST_PAGE_NUM){
    		return 0 ;
    	}
    	return count - EgmConstants.CHAT_LIST_PAGE_NUM * (pageNo + 1);
    }
    
    public static int getMsgCount(long receiverId ,long senderId){
    	Cursor cursor = null ;
    	StringBuilder inStr = new StringBuilder();
    	inStr.append("(").append(receiverId).append(",").append(senderId).append(")");
    	
    	StringBuilder selection = new StringBuilder();
    	selection.append(MsgTable.C_ANOTHERID).append(" IN ").append(inStr.toString())
		 	.append(" AND ")
		 	.append(MsgTable.C_FROMID).append(" IN ").append(inStr.toString());
    	cursor = getDBProvider().query(MsgTable.CONTENT_URI,null, selection.toString(),null, null);
    	int count = cursor.getCount();
    	cursor.close();
    	return count ;
    }
    
    public static MessageInfo getNextMessageInfo(long time, long receiverId ,long senderId) {
    	MessageInfo messageInfo = null;
    	
    	Cursor cursor = null ;
    	StringBuilder inStr = new StringBuilder();
    	inStr.append("(").append(receiverId).append(",").append(senderId).append(")");
    	
    	StringBuilder selection = new StringBuilder();
    	selection.append(MsgTable.C_ANOTHERID).append(" IN ").append(inStr.toString())
		 	.append(" AND ")
		 	.append(MsgTable.C_FROMID).append(" IN ").append(inStr.toString())
		 	.append(" AND ").append(MsgTable.C_TIME).append(">?");
    	
    	String[] args = new String[] {
                String.valueOf(time),
            };
    	
    	StringBuilder orderBy = new StringBuilder();
    	orderBy.append(MsgTable.C_TIME).append(" ASC ").append("limit 1");
    	
    	cursor = getDBProvider().query(MsgTable.CONTENT_URI,null, selection.toString(), args, orderBy.toString());
    	if (cursor != null) {
    		cursor.moveToFirst();
    		if (!cursor.isAfterLast()) {
    			messageInfo = MessageInfo.getMessageInfo(cursor);
    		}
    	}
    	
    	return messageInfo;
    }
    
    /**
     * 更新自身发送消息状态
     */
    public static void updateMsgPlayState(MessageInfo info) {
    	if(info == null){
    		return ;
    	}
    	StringBuilder selection = new StringBuilder();
    	selection.append(MsgTable.C_ANOTHERID)
    		.append("=?")
    		.append(" AND ")
    		.append(MsgTable.C_FROMID)
    		.append("=?")
    		.append(" AND ")
    		.append(MsgTable.C_MSGID)
    		.append("=?");
    	
    	String[] args = new String[]{
    			String.valueOf(info.receiver),
    			String.valueOf(info.sender),
    			String.valueOf(info.msgId)
    	};
    	
    	ContentValues values = new ContentValues();
    	values.put(MsgTable.C_RESERVED1, "1");

    	getDBProvider().update(MsgTable.CONTENT_URI,values, selection.toString(),args);
    }
    
    public static void updateMsgReserve3(MessageInfo info) {
    	if(info == null){
    		return ;
    	}
    	StringBuilder selection = new StringBuilder();
    	selection.append(MsgTable.C_ANOTHERID)
    		.append("=?").append(" AND ")
    		.append(MsgTable.C_FROMID).append("=?")
    		.append(" AND ").append(MsgTable.C_TIME)
    		.append("=?");
    	
    	String[] args = new String[]{
    			String.valueOf(info.receiver),
    			String.valueOf(info.sender),
    			String.valueOf(info.time)
    	};
    	
    	ContentValues values = new ContentValues();
    	values.put(MsgTable.C_RESERVED3, info.getReserved3());
    	
    	getDBProvider().update(MsgTable.CONTENT_URI, values, 
    			selection.toString(), args);
    }
    
    /**
     * 更新自身发送消息状态
     */
    public static void updataMsgState(MessageInfo info){
    	if(info == null){
    		return ;
    	}
    	StringBuilder selection = new StringBuilder();
    	selection.append(MsgTable.C_ANOTHERID)
    		.append("=?")
    		.append(" AND ")
    		.append(MsgTable.C_FROMID)
    		.append("=?")
    		.append(" AND ")
    		.append(MsgTable.C_TIME)
    		.append("=?");
    	
    	String[] args = new String[]{
    			String.valueOf(info.receiver),
    			String.valueOf(info.sender),
    			String.valueOf(info.time)
    	};
    	
    	ContentValues values = new ContentValues();
    	values.put(MsgTable.C_MSGID, info.msgId);
    	values.put(MsgTable.C_STATUS,info.status);
    	if (null != info.attach) {
        values.put(MsgTable.C_ATTACH, info.attach);
    }
    	
    	if(info.type == EgmProtocolConstants.MSG_TYPE.MSG_TYPE_GIFT){
    		values.put(MsgTable.C_USERCP,info.usercp);
    	}
    	getDBProvider().update(MsgTable.CONTENT_URI,values, selection.toString(),args);
    }
    
    /**
     * 更新自身发送消息状态，同时更新消息发送时间
     */
    public static void updataMsgState(MessageInfo info ,long time){
    	if(info == null){
    		return ;
    	}
    	StringBuilder selection = new StringBuilder();
    	selection.append(MsgTable.C_ANOTHERID)
    		.append("=?")
    		.append(" AND ")
    		.append(MsgTable.C_FROMID)
    		.append("=?")
    		.append(" AND ")
    		.append(MsgTable.C_TIME)
    		.append("=?");
    	
    	String[] args = new String[]{
    			String.valueOf(info.receiver),
    			String.valueOf(info.sender),
    			String.valueOf(info.time)
    	};
    	
    	ContentValues values = new ContentValues();
    	values.put(MsgTable.C_MSGID, info.msgId);
    	values.put(MsgTable.C_STATUS,info.status);
    	values.put(MsgTable.C_TIME,time);
    	
    	if(info.type == EgmProtocolConstants.MSG_TYPE.MSG_TYPE_GIFT){
    		values.put(MsgTable.C_USERCP,info.usercp);
    	}
    	getDBProvider().update(MsgTable.CONTENT_URI,values, selection.toString(),args);
    }
    
    /**
     * 
     * @param info
     */
    public static void delMsgWithResource(MessageInfo info) {
    	if (info == null) {
    		return ;
    	}
    	
    	long mMyId = -1;
    	
    	Account account = ManagerAccount.getInstance().getCurrentAccount();
    	if (ManagerAccount.getInstance().getCurrentAccount() != null){
			mMyId = ManagerAccount.getInstance().getCurrentId();
        }
    	else {
    		return ;
    	}
    	
    	long anothor = info.receiver != mMyId ? info.receiver : info.sender;
    	
    	MsgAttach msgAttach = MsgDBManager.getMsgAttach(info.sender,info.msgId);
		if(msgAttach != null){
			deleteFile(msgAttach.smallImagePath);
			deleteFile(msgAttach.audioPath);
			deleteFile(msgAttach.videoPath);
			
			if (! TextUtils.isEmpty(msgAttach.mediaResUrl)) {
				StoreFile file = CacheManager.getStoreFile(msgAttach.mediaResUrl);
				
				if (file != null) {
					file.delete();
				}
			}
		}
		
		MsgDBManager.delMsg(info.sender,info.msgId);
		
		// bug fix #141118、#141119      这种修改方法，不会修改豪气值和亲密度，所以也不会出现bug#141005的情况
		boolean isExist = LastMsgDBManager.isExistMsgByMsgId(info.sender, info.msgId);
		
		if(isExist) { 
			MessageInfo info1 = MsgDBManager.getLastMsg(mMyId, anothor);
			ChatItemInfo chatInfo = LastMsgDBManager.getChatItemByUid(anothor);
			
			if (chatInfo != null) {
				if(info1 != null) {
					LastMsgDBManager.updateLastMsg(getChatItemInfo(
							chatInfo.anotherUserInfo, info1));
				} else {
					// 本地已经没有与该用户的聊天记录了，那么消息列表中，该用户的记录设置未空记录
					LastMsgDBManager.updateLastMsgToEmpty(anothor);
				}
			}
		}
	}
    
    /**
	 * 获取ChatItemInfo
	 * @param info
	 * @return
	 */
	private static ChatItemInfo getChatItemInfo(ChatItemUserInfo userInfo, MessageInfo info){
		ChatItemInfo chatItemInfo = new ChatItemInfo();
		chatItemInfo.message = info ;
		chatItemInfo.notReadCount = 0 ;
		chatItemInfo.anotherUserInfo = userInfo ;
		return chatItemInfo ;
	}
    
	private static void deleteFile(String path) {
		if(!TextUtils.isEmpty(path)){
			File file = new File(path);
			if(file.exists()){
				file.delete();
			}
		}
	}
    
    /**
     * 删除一条消息
     * @param msgId
     */
    public static void delMsg(long fromId,long msgId){
    	StringBuilder selection = new StringBuilder();
    	selection.append(MsgTable.C_FROMID).append("=?").append(" AND ").append(MsgTable.C_MSGID).append("=?");
    	
    	String[] args = new String[]{String.valueOf(fromId),String.valueOf(msgId)};
    	getDBProvider().delete(MsgTable.CONTENT_URI, selection.toString(), args);
    }
    
    /**
     * 获取消息附件
     * @param msgId
     * @return
     */
    public static MsgAttach getMsgAttach(long fromId,long msgId){
    	MsgAttach msgAttach = null ;
    	
    	StringBuilder selection = new StringBuilder();
    	selection.append(MsgTable.C_FROMID).append("=?").append(" AND ")
    		.append(MsgTable.C_MSGID).append("=?");
    	
    	String[] args = new String[]{String.valueOf(fromId),String.valueOf(msgId)};
    	
    	Cursor cursor = getDBProvider().query(
    			MsgTable.CONTENT_URI,
    			new String[]{MsgTable.C_ATTACH}, 
    			selection.toString(), 
    			args, 
    			null);
    	if(cursor != null){
    		if (cursor.moveToFirst()) {
	    		String attach = cursor.getString(cursor.getColumnIndex(MsgTable.C_ATTACH));
	    		if(!TextUtils.isEmpty(attach)){
	    			msgAttach = MsgAttach.toMsgAttach(attach);
	    		}
    		}
    		
    		cursor.close();
    	}
    	
    	return msgAttach ;
    }
    
    /**
     * 
     * @param senderId
     * @param msgId
     * @return
     */
    public static MessageInfo getMsg(long senderId, long msgId) {
    	MessageInfo info = null;
    	
    	Cursor cursor = getDBProvider().query(MsgTable.CONTENT_URI, null,
    			MsgTable.C_FROMID + "=? AND " + MsgTable.C_MSGID + "=?", 
    			new String[] { String.valueOf(senderId), String.valueOf(msgId) },
    			null);
    	
		if(cursor != null){
			if (cursor.moveToFirst()) {
				info = new MessageInfo(cursor, MessageInfo.T_C_DEFAULT);
			}
			
			cursor.close();
		}
    	
    	return info;
    }
    
	/**
	 * 获取最后一条消息
	 * @param senderId
	 * @param receiverId
	 * @return
	 */
	public static MessageInfo getLastMsg(long senderId, long receiverId) {
		MessageInfo info = null;
		
		StringBuilder selection = new StringBuilder();
		selection.append(MsgTable.C_FROMID).append("=?").append(" AND ").append(MsgTable.C_ANOTHERID).append("=?");
		selection.append(" OR ");
		selection.append(MsgTable.C_ANOTHERID).append("=?").append(" AND ").append(MsgTable.C_FROMID).append("=?");
		
		String[] args = new String[] { 
				String.valueOf(receiverId),
				String.valueOf(senderId),
				String.valueOf(receiverId),
				String.valueOf(senderId) 
		};
		
		Cursor cursor = getDBProvider().query(MsgTable.CONTENT_URI, null,
				selection.toString(), args,  MsgTable.C_TIME + " DESC");
		if(cursor != null){
			if (cursor.moveToFirst()) {
				info = new MessageInfo(cursor, MessageInfo.T_C_DEFAULT);
			}
			
			cursor.close();
		}
		
		return info;
	}
	
	/**
	 * 删除和一个人的消息
	 * @param uid
	 */
	public static void delMsgByUid(long uid){
		StringBuilder selection = new StringBuilder();
		selection.append(LastMsgTable.C_ANOTHERID).append("=?").append(" or ").append(LastMsgTable.C_FROMID).append("=?");
    	
    	String[] args = new String[]{String.valueOf(uid), String.valueOf(uid)};
    	getDBProvider().delete(MsgTable.CONTENT_URI, selection.toString(), args);
	}

	/**
	 * 更新消息的attach信息，注意：更新成功的没有数据库变更通知
	 * 
	 * @param mInfo
	 * @param attach
	 */
	public static void updateMsgAttach(MessageInfo info, MsgAttach attach) {
		if (info == null || attach == null) {
			return ;
		}
		
		ContentValues cv = new ContentValues();
		cv.put(MsgTable.C_ATTACH, MsgAttach.toJsonString(attach));
		
		getDBProvider().updateSilent(MsgTable.CONTENT_URI, cv, 
				MsgTable.C_FROMID + "=? AND " + MsgTable.C_MSGID + "=?",
				new String[] {String.valueOf(info.getSender()), 
						String.valueOf(info.getMsgId())});
	}
}
