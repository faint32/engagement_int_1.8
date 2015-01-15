package com.netease.service.db.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.netease.common.db.BaseDBManager;
import com.netease.common.service.BaseService;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.ChatListMeta;
import com.netease.engagement.dataMgr.MemoryDataCenter;
import com.netease.service.db.MsgDBProvider;
import com.netease.service.db.MsgDBTables;
import com.netease.service.db.MsgDBTables.LastMsgTable;
import com.netease.service.db.MsgDBTables.MsgTable;
import com.netease.service.db.manager.ManagerAccount.Account;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.meta.ChatItemInfo;
import com.netease.service.protocol.meta.ChatItemUserInfo;
import com.netease.service.protocol.meta.ChatSortKeyValue;
import com.netease.service.protocol.meta.MessageInfo;
import com.netease.util.PDEEngine;


public class LastMsgDBManager extends BaseDBManager {
    
    public static class NotificationMsg {
        public int chatCount;//有新消息人数
        public int unreadNum;//未读消息总数
        public int msgType;//最新一条消息类型
        public ChatItemUserInfo user;//聊天人，当仅有一个聊天人时，需要直接跳转到相应聊天窗口
        public String extra;//系统消息的提示内容
        public boolean fire;
    }
    
    private static MsgDBProvider mDBProvider;
    
    private static MsgDBProvider getDBProvider() {
         MsgDBProvider provider = mDBProvider;
        if (provider == null) {
            provider = new MsgDBProvider();
            mDBProvider = provider;
        }
        return provider;
    }
    
    private static String[] Projection_Uid = new String[]{
    	MsgDBTables.LastMsgTable.C_ANOTHERID
    };
    
    /**
     * 生成消息插入ContentValues
     * @param itemInfo
     * @return
     */
    private static ContentValues makeInsertValue(ChatItemInfo itemInfo){
    	ContentValues values = new ContentValues();
    	
        long currentUid = -1;
        if(null != MemoryDataCenter.getInstance().get(MemoryDataCenter.CURRENT_CHAT_UID)){
            currentUid = (Long)MemoryDataCenter.getInstance().get(MemoryDataCenter.CURRENT_CHAT_UID);
        }
        if(currentUid == itemInfo.anotherUserInfo.uid){//当前聊天人，直接已读
            values.put(LastMsgTable.C_UNREADNUM, 0);
        } else {
            values.put(LastMsgTable.C_UNREADNUM, itemInfo.notReadCount);
        }
        //处理 MessageInfo
        long me = Long.valueOf(ManagerAccount.getInstance().getCurrentAccountId());
        if(me == itemInfo.message.receiver){//another 为聊天对方
            values.put(LastMsgTable.C_ANOTHERID, itemInfo.message.sender);
        } else {
            values.put(LastMsgTable.C_ANOTHERID, itemInfo.message.receiver);
        }
        values.put(LastMsgTable.C_FROMID, itemInfo.message.sender);
        values.put(LastMsgTable.C_TIME, itemInfo.message.time);
        
        itemInfo.message.setEncrypted();
        values.put(LastMsgTable.C_CONTENT, PDEEngine.PXEncrypt(itemInfo.message.msgContent));
        
        if (null != itemInfo.message.mediaUrl) {
            values.put(LastMsgTable.C_MEDIA_URL, itemInfo.message.mediaUrl);
        }
        values.put(LastMsgTable.C_TYPE, itemInfo.message.type);
        values.put(LastMsgTable.C_MSGID, itemInfo.message.msgId);
        values.put(LastMsgTable.C_DURATION, itemInfo.message.duration);
        values.put(LastMsgTable.C_EXTRA_ID, itemInfo.message.extraId);
        if (null == itemInfo.message.extraString && itemInfo.message.extra != null){
            itemInfo.message.extraString = itemInfo.message.extra.toString();
        }
       
        if (null != itemInfo.message.extraString) {
            values.put(LastMsgTable.C_EXTRA, itemInfo.message.extraString);
        }
        values.put(LastMsgTable.C_USERCP, itemInfo.message.usercp);
        
        //对方发送来的消息，直接将状态设置为发送成功
        if(itemInfo.message.sender != ManagerAccount.getInstance().getCurrentId()){
        	values.put(LastMsgTable.C_STATUS, EgmConstants.Sending_State.SEND_SUCCESS);
        }else{
            if(itemInfo.message.status >= 0){
                values.put(LastMsgTable.C_STATUS,itemInfo.message.status);
            }else {
                values.put(LastMsgTable.C_STATUS, EgmConstants.Sending_State.SEND_SUCCESS);
            }
        }
        
        if (null != itemInfo.message.attach) {
            values.put(LastMsgTable.C_ATTACH, itemInfo.message.attach);
        }
        
        //处理 ChatItemUserInfo
        values.put(LastMsgTable.C_ANOTHERID, itemInfo.anotherUserInfo.uid);
        values.put(LastMsgTable.C_ISVIP, itemInfo.anotherUserInfo.isVip?1:0);
        values.put(LastMsgTable.C_ISNEW, itemInfo.anotherUserInfo.isNew?1:0);
        values.put(LastMsgTable.C_CROWNID, itemInfo.anotherUserInfo.crownId);
        if (null != itemInfo.anotherUserInfo.nick) {
            values.put(LastMsgTable.C_NICK, itemInfo.anotherUserInfo.nick);
        }
        if (null != itemInfo.anotherUserInfo.portraitUrl192) {
            values.put(LastMsgTable.C_AVATAR, itemInfo.anotherUserInfo.portraitUrl192);
        }
        
        values.put(LastMsgTable.C_RESERVED3, itemInfo.message.getReserved3());
        
        values.put(LastMsgTable.C_RESERVED2, itemInfo.message.getFaceId());
        
        
        return values ;
    }
    
    /**
     * 往最后一条消息列表中插入一项
     */
    public static void insertChatItemInfo(ChatItemInfo itemInfo) {
        if (null == itemInfo || null == itemInfo.message || null == itemInfo.anotherUserInfo) {
            return ;
        }
        ContentValues values = makeInsertValue(itemInfo);
        if(values != null){
        	getDBProvider().insert(LastMsgTable.CONTENT_URI, values);
        }
    }
    
    /**
     * 往最后一条消息列表中插入一项，并同时设置豪气值、亲密度
     */
    public static void insertChatItemInfo(ChatItemInfo itemInfo, long rich, long intimacy) {
        if (null == itemInfo || null == itemInfo.message || null == itemInfo.anotherUserInfo) {
            return ;
        }
        ContentValues values = makeInsertValue(itemInfo);
        values.put(LastMsgTable.C_RICH, rich);
        values.put(LastMsgTable.C_INTIMACY, intimacy);
        if(values != null){
        	getDBProvider().insert(LastMsgTable.CONTENT_URI, values);
        }
    }
    
    /**
     * 处理push过来的新消息
     * 单条消息处理
     */
    public static void handelNewMsg(ChatItemInfo itemInfo) {
        if (null == itemInfo || null == itemInfo.anotherUserInfo) {
            return ;
        }
        String selection = LastMsgTable.C_ANOTHERID + "=?";
        String[] selectionArgs = new String[] {
            String.valueOf(itemInfo.anotherUserInfo.uid)
        };
        Cursor c = getDBProvider().query(LastMsgTable.CONTENT_URI, new String[]{LastMsgTable._ID}, selection, selectionArgs,null);
        try{
            if (c != null && c.getCount() > 0) {
                updateLastMsg(itemInfo);
            } else {
                insertChatItemInfo(itemInfo);
            } 
        } catch (Exception e){
            e.printStackTrace();
        } finally{
            if(c != null) {
            	c.close();
            	c = null;
            }
        }
    }
    
    private static String[] ChatItemUserInfo_Projection = new String[]{
    		LastMsgTable.C_AVATAR ,
    		LastMsgTable.C_CROWNID,
    		LastMsgTable.C_ISNEW,
    		LastMsgTable.C_ISVIP,
    		LastMsgTable.C_NICK,
    		LastMsgTable.C_ANOTHERID
    };
    
    /**
     * 发送消息更新状态使用
     * @param msgInfo
     */
    public static void handleNewMsg(MessageInfo msgInfo){
//    	ChatItemInfo chatItemInfo = new ChatItemInfo();
//    	chatItemInfo.message = msgInfo ;
//    	chatItemInfo.notReadCount = 0 ;
//    	
//    	StringBuilder selection = new StringBuilder();
//    	selection.append(LastMsgTable.C_FROMID).append("=?").append(" AND ").append(LastMsgTable.C_ANOTHERID).append("=?");
//    	String[] args = new String[]{
//    		String.valueOf(msgInfo.sender),
//    		String.valueOf(msgInfo.receiver)
//    	};
//    	Cursor cursor = getDBProvider().query(
//    			LastMsgTable.CONTENT_URI,
//    			ChatItemUserInfo_Projection, 
//    			selection.toString(), 
//    			args, 
//    			null);
//    	if(cursor != null && cursor.getCount() > 0){
//    		cursor.moveToFirst();
//    		ChatItemUserInfo chatItemUserInfo = new ChatItemUserInfo();
//    		chatItemUserInfo.crownId = Integer.parseInt(cursor.getString(cursor.getColumnIndex(LastMsgTable.C_CROWNID)));
//    		chatItemUserInfo.isNew = cursor.getInt(cursor.getColumnIndex(LastMsgTable.C_ISNEW))== 1 ? true : false ;
//    		chatItemUserInfo.isVip = cursor.getInt(cursor.getColumnIndex(LastMsgTable.C_ISVIP))== 1 ? true : false ;
//    		chatItemUserInfo.nick = cursor.getString(cursor.getColumnIndex(LastMsgTable.C_NICK));
//    		chatItemUserInfo.portraitUrl192 = cursor.getString(cursor.getColumnIndex(LastMsgTable.C_AVATAR));
//    		chatItemUserInfo.uid = Long.parseLong(cursor.getString(cursor.getColumnIndex(LastMsgTable.C_ANOTHERID)));
//    		chatItemInfo.anotherUserInfo = chatItemUserInfo ;
//    	}
//    	handelNewMsg(chatItemInfo);
    	
    	// bug fix #141096  上面的实现，再同时收到和发出消息时，同步上会有问题（cursor查询为空），导致chatItemInfo.anotherUserInfo.uid为0，消息更新保存到小爱（uid为0）里面。
    	if(ManagerAccount.getInstance().getCurrentAccount() == null)
    	    return;
        long currentUid = ManagerAccount.getInstance().getCurrentId();
    	long anotherUid;
    	if(currentUid == msgInfo.sender) {
    		anotherUid = msgInfo.receiver;
    	} else {
    		anotherUid = msgInfo.sender;
    	}
    	ChatItemInfo chatItemInfo = getChatItemByUid(anotherUid);
    	if(chatItemInfo != null) {
	    	chatItemInfo.notReadCount = 0;
	    	chatItemInfo.message = msgInfo;
	    	
	    	handelNewMsg(chatItemInfo);
    	}
    }
    
    /**
     * 按uid查询ChatItemInfo
     * uid
     */
    public static ChatItemInfo getChatItemByUid(long uid) {
        ChatItemInfo itemInfo = null;
        String selection = LastMsgTable.C_ANOTHERID + "=?";
        String[] selectionArgs = new String[] {
            String.valueOf(uid)
        };
        Cursor c = getDBProvider().query(LastMsgTable.CONTENT_URI, null, 
        		selection, selectionArgs, null);
        if (c == null) {
        	return itemInfo;
        }
        
        if (c.moveToFirst()) {
        	itemInfo = new ChatItemInfo();
        	itemInfo.notReadCount = c.getInt(c.getColumnIndex(LastMsgTable.C_UNREADNUM));
        	itemInfo.anotherUserInfo.uid = c.getLong(c.getColumnIndex(LastMsgTable.C_ANOTHERID));
        	itemInfo.anotherUserInfo.crownId = c.getInt(c.getColumnIndex(LastMsgTable.C_CROWNID));
        	itemInfo.anotherUserInfo.isNew = c.getInt(c.getColumnIndex(LastMsgTable.C_ISNEW)) == 1 ? true : false;
        	itemInfo.anotherUserInfo.isVip = c.getInt(c.getColumnIndex(LastMsgTable.C_ISVIP)) == 1 ? true : false;
        	itemInfo.anotherUserInfo.nick = c.getString(c.getColumnIndex(LastMsgTable.C_NICK));
        	itemInfo.anotherUserInfo.portraitUrl192 = c.getString(c.getColumnIndex(LastMsgTable.C_AVATAR));
                    
        	itemInfo.message.sender = c.getLong(c.getColumnIndex(LastMsgTable.C_FROMID));
        	long another = c.getLong(c.getColumnIndex(LastMsgTable.C_ANOTHERID));
        	if(itemInfo.message.sender == another){
        		itemInfo.message.receiver = Long.valueOf(ManagerAccount.getInstance().getCurrentAccountId());
        	} else {
        		itemInfo.message.receiver = another;
        	}
        	itemInfo.message.time = c.getLong(c.getColumnIndex(LastMsgTable.C_TIME));
        	itemInfo.message.setReserved3(c.getInt(c.getColumnIndex(LastMsgTable.C_RESERVED3)));
        	
        	String content = c.getString(c.getColumnIndex(LastMsgTable.C_CONTENT));
        	if (itemInfo.message.isEncrypted()) {
        		itemInfo.message.msgContent = PDEEngine.PXDecrypt(content);
        	}
        	else {
        		itemInfo.message.msgContent = content;
        	}
        	
        	itemInfo.message.mediaUrl = c.getString(c.getColumnIndex(LastMsgTable.C_MEDIA_URL));
        	itemInfo.message.type = c.getInt(c.getColumnIndex(LastMsgTable.C_TYPE));
        	itemInfo.message.msgId = c.getLong(c.getColumnIndex(LastMsgTable.C_MSGID));
        	itemInfo.message.duration = c.getInt(c.getColumnIndex(LastMsgTable.C_DURATION));
        	itemInfo.message.extraId = c.getLong(c.getColumnIndex(LastMsgTable.C_EXTRA_ID));
        	itemInfo.message.extraString = c.getString(c.getColumnIndex(LastMsgTable.C_EXTRA));
        	itemInfo.message.usercp = c.getInt(c.getColumnIndex(LastMsgTable.C_USERCP));
        	itemInfo.message.attach = c.getString(c.getColumnIndex(LastMsgTable.C_ATTACH));
        	itemInfo.message.status = c.getInt(c.getColumnIndex(LastMsgTable.C_STATUS));
        }
    	c.close();
    	
        return itemInfo;
    }
    
    
    /**
     * 更新最后一条消息列表
     */
    public static boolean updateLastMsg(ChatItemInfo itemInfo) {
        if (null == itemInfo || null == itemInfo.anotherUserInfo || null == itemInfo.message) {
            return false;
        }
        ContentValues values = makeInsertValue(itemInfo);
        String selection = LastMsgTable.C_ANOTHERID + "=?";
        
        String[] selectionArgs = new String[] {
            String.valueOf(itemInfo.anotherUserInfo.uid)
        };
        int ret = getDBProvider().update(LastMsgTable.CONTENT_URI, values, selection, selectionArgs);
        return ret > 0;
    }
    
    /**
     * 更新最后一条消息列表为空消息
     * 当与一个用户的消息表为空时，消息列表保留与该用户的一条消息，使得消息列表能现实该用户。但该消息的内容为空。
     */
    public static boolean updateLastMsgToEmpty(long uid) {
    	ContentValues values = new ContentValues();
    	values.put(LastMsgTable.C_TYPE, EgmProtocolConstants.MSG_TYPE.MSG_TYPE_TEXT);
    	values.put(LastMsgTable.C_CONTENT, "");
    	values.put(LastMsgTable.C_STATUS, EgmConstants.Sending_State.SEND_SUCCESS);
    	values.put(LastMsgTable.C_RESERVED3, 0);
    	
        String selection = LastMsgTable.C_ANOTHERID + "=?";
        
        String[] selectionArgs = new String[] {
            String.valueOf(uid)
        };
        int ret = getDBProvider().update(LastMsgTable.CONTENT_URI, values, selection, selectionArgs);
    	return ret > 0;
    }
    
    /**
     * 查询Notificationbar提醒所需数据
     * excludeUid 排除掉当前聊天人
     */
    public  static NotificationMsg getNotificationMsg(long excludeUid){
        String[] selectionArgs = null;
        String selection = null;
        if(excludeUid > 0 ){
            selection = LastMsgTable.C_UNREADNUM + " >?" + " AND " + LastMsgTable.C_ANOTHERID + " <>?";
            selectionArgs = new String[] {
                    String.valueOf(0),
                    String.valueOf(excludeUid)
                };
        } else {
            selection = LastMsgTable.C_UNREADNUM + " >?";
            selectionArgs = new String[] {
                    String.valueOf(0),
                };
        }
        
        Cursor c = getDBProvider().query(LastMsgTable.CONTENT_URI, null, selection, selectionArgs,
                LastMsgTable.C_TIME + " DESC");
        NotificationMsg notify = null;
        try {
	        if (c != null && c.getCount() > 0) {
	            notify = new NotificationMsg();
	            
	            if (c.moveToFirst()) {//取最后一条消息的User
	            	notify.chatCount = c.getCount();
	            	notify.msgType = c.getInt(c.getColumnIndex(LastMsgTable.C_TYPE));
	            	
	            	String content = c.getString(c.getColumnIndex(LastMsgTable.C_CONTENT));
	            	int reserve3 = c.getInt(c.getColumnIndex(LastMsgTable.C_RESERVED3));
	            	
	            	if (((reserve3 >> 10) & 0x1) != 0) {
	            		content = PDEEngine.PXDecrypt(content);
	            	}
	            	
	            	notify.extra = content;
	            	notify.unreadNum += c.getInt(c.getColumnIndex(LastMsgTable.C_UNREADNUM));
	            	notify.fire = (c.getInt(c.getColumnIndex(LastMsgTable.C_RESERVED3)) & 0x01) != 0;
                        
					ChatItemUserInfo itemInfo = new ChatItemUserInfo();
					itemInfo.uid = c.getLong(c.getColumnIndex(LastMsgTable.C_ANOTHERID));
					itemInfo.crownId = c.getInt(c.getColumnIndex(LastMsgTable.C_CROWNID));
					itemInfo.isNew = c.getInt(c.getColumnIndex(LastMsgTable.C_ISNEW)) == 1 ? true : false;
					itemInfo.isVip = c.getInt(c.getColumnIndex(LastMsgTable.C_ISVIP)) == 1 ? true : false;
					itemInfo.nick = c.getString(c.getColumnIndex(LastMsgTable.C_NICK));
					itemInfo.portraitUrl192 = c.getString(c.getColumnIndex(LastMsgTable.C_AVATAR));
					notify.user = itemInfo;
				}
	            
				for (c.moveToNext(); !c.isAfterLast(); c.moveToNext()) {// 所有未读消息数
					notify.unreadNum += c.getInt(c.getColumnIndex(LastMsgTable.C_UNREADNUM));
				}
			}
		}
        finally {
        	if (c != null) {
        		c.close();
        	}
            c = null;
        }
       return  notify;
    }
    
    /**
     * 生成ContentValues列表
     * @param list
     * @return
     */
    private static List<ContentValues> makeContentValues (List<ChatItemInfo> list){
    	if(list == null || list.size() == 0){
    		return null ;
    	}
    	List<ContentValues> valuesList = new ArrayList<ContentValues>();
    	for(ChatItemInfo info : list){
    		valuesList.add(makeInsertValue(info));
    	}
    	return valuesList ;
    }
    
    /**
     * 获取最近消息列表
     * @return
     */
    public static Cursor getLastMsgList(boolean onlyNew){
    	Cursor cursor = null ;
    	
    	StringBuilder selection = new StringBuilder();
    	if(onlyNew){
    		selection.append(LastMsgTable.C_ISNEW).append(" = 1");
    	}
    	StringBuilder sortOrder = new StringBuilder();
	    sortOrder.append(MsgDBTables.LastMsgTable.C_TIME).append(" DESC");
		cursor = getDBProvider().query(LastMsgTable.CONTENT_URI, ChatListMeta.Projection, selection.toString(), null, sortOrder.toString());
    	return cursor ;
    }
    
    /**
     * @return
     */
    public static Cursor getLastMsgListAsUids(long[] uids ,boolean onlyNew){
    	Cursor cursor = null ;
    	
    	StringBuilder uidStr = new StringBuilder();
    	uidStr.append("(");
    	for(long uid : uids){
    		uidStr.append(String.valueOf(uid)).append(",");
    	}
    	uidStr.deleteCharAt(uidStr.length() - 1);
    	uidStr.append(")");
    	
    	StringBuilder where = new StringBuilder();
    	where.append(MsgDBTables.LastMsgTable.C_ANOTHERID).append(" IN ").append(uidStr.toString());
    	
    	if(onlyNew){
    		where.append(" AND ").append(LastMsgTable.C_ISNEW).append(" = 1");
    	}

	cursor = getDBProvider().query(LastMsgTable.CONTENT_URI, ChatListMeta.Projection, where.toString(), null,null);
    	return cursor ;
    }
    
    /**
     * 根据uid获取豪气值和亲密度
     * @param uid
     * @return long[]    long[0]:豪气值   long[1]:亲密度
     */
    public static long[] getRichAndIntimacyByUid(long uid) {
    	long[] values = new long[2];
    	String[] projection = new String[]{ LastMsgTable.C_RICH, LastMsgTable.C_INTIMACY };
    	String selection = LastMsgTable.C_ANOTHERID + "=?";
    	String[] selectionArgs = new String[] { String.valueOf(uid) };
    	Cursor c = getDBProvider().query(LastMsgTable.CONTENT_URI, projection, selection, selectionArgs,
                null);
    	if(c != null) {
    		try {
    			if (c.moveToFirst()) {
	    			values[0] = c.getLong(c.getColumnIndex(LastMsgTable.C_RICH));
	    			values[1] = c.getLong(c.getColumnIndex(LastMsgTable.C_INTIMACY));
    			}
    		} finally {
    			c.close();
    			c = null;
    		}
    	}
    	return values;
    }
    
    public static boolean isExistMsgByMsgId(long fromId,long msgId) {
    	boolean isExist = false;
    	String[] projection = new String[]{  };
    StringBuilder selection = new StringBuilder();
    	selection.append(MsgTable.C_FROMID).append("=?").append(" AND ").append(MsgTable.C_MSGID).append("=?");
    	String[] selectionArgs = new String[]{String.valueOf(fromId),String.valueOf(msgId)};
    	Cursor c = getDBProvider().query(LastMsgTable.CONTENT_URI, projection, selection.toString(), selectionArgs, null);
    	if(c != null) {
    		try {
    			if (c.moveToFirst()) {
    				isExist = true;
    			}
    		} finally {
    			c.close();
    			c = null;
    		}
    	}
    	return isExist;
    }
    
    /**
     * 更新豪气值和亲密度
     * @param values
     * @param rich
     */
    public static void updateRichOrIntimacy(ChatSortKeyValue[] values, boolean rich){
    	if (values == null || values.length == 0) {
    		return ;
    	}
    	
		StringBuilder selection = new StringBuilder();
		selection.append(LastMsgTable.C_ANOTHERID).append("=?");

		StringBuffer buffer = new StringBuffer();
		buffer.append("UPDATE ").append(LastMsgTable.TABLE_NAME)
				.append(" SET ");

		if (rich) {
			buffer.append(LastMsgTable.C_RICH);
		} else {
			buffer.append(LastMsgTable.C_INTIMACY);
		}

		buffer.append("=?").append(" WHERE ").append(LastMsgTable.C_ANOTHERID)
				.append("=?");

		SQLiteOpenHelper openHelper = getDBProvider().getSQLiteOpenHelper();
		SQLiteDatabase database = openHelper.getWritableDatabase();

		SQLiteStatement update = database.compileStatement(buffer.toString());
		buffer.setLength(0);

		database.beginTransaction();

		try {
			for (ChatSortKeyValue value : values) {
				update.bindLong(1, value.sorVvalue);
				update.bindLong(2, value.uid);

				update.execute();
			}

			database.setTransactionSuccessful();
		} catch (Exception e) {
		}

		database.endTransaction();
    }
    
    /**
     * 根据豪气值排序
     * @param onlyNew
     * @return
     */
    public static Cursor getLastMsgListSortByRich(boolean onlyNew,boolean rich){
	    	Cursor cursor = null ;
	    	
	    	StringBuilder where = new StringBuilder();
	    	if(onlyNew){
	    		where.append(LastMsgTable.C_ISNEW).append(" = 1");
	    	}
	    	
	    	StringBuilder sort = new StringBuilder();
	    	if(rich){
	    		sort.append(LastMsgTable.C_RICH).append(" DESC");
	    	}else{
	    		sort.append(LastMsgTable.C_INTIMACY).append(" DESC");
	    	}
		cursor = getDBProvider().query(
				LastMsgTable.CONTENT_URI, 
				ChatListMeta.Projection, 
				where.toString(), 
				null,
				sort.toString());
	    	return cursor ;
    }
    
    
    
    /**
     * 获取聊天人列表中所有uid
     * @return
     */
    public static long[] getUids(){
    	Cursor c = getDBProvider().query(
    			LastMsgTable.CONTENT_URI, 
    			Projection_Uid,
    			null, 
    			null, 
    			null);
    	long[] uids = null;
    	if(c != null) {
	    	uids= new long[c.getCount()];
	    	int i = 0 ;
	    	if(c != null){
	    		while(c.moveToNext()){
	    			uids[i++] = c.getLong(c.getColumnIndex(MsgDBTables.LastMsgTable.C_ANOTHERID));
	    		}
	    		c.close();
	    		c = null;
	    	}
    	}
    	return uids ;
    }
    
    /**
     * 删除一条消息
     * @param msgId
     */
    public static int delMsg(long msgId){
    	StringBuilder selection = new StringBuilder();
    	selection.append(LastMsgTable.C_MSGID).append("=?");
    	
    	String[] args = new String[]{String.valueOf(msgId)};
    	int row = getDBProvider().delete(LastMsgTable.CONTENT_URI, selection.toString(), args);
    	return row;
    }
    
    /**
     * 删除一条消息
     * @param uid
     */
    public static void delMsgByUid(long uid){
    	StringBuilder selection = new StringBuilder();
    	selection.append(LastMsgTable.C_ANOTHERID).append("=?");
    	
    	String[] args = new String[]{String.valueOf(uid)};
    	getDBProvider().delete(LastMsgTable.CONTENT_URI, selection.toString(), args);
    }
    
    /**
     * 设置未读消息数目为0
     * @param uid
     */
    public static void setUnReadNumZero(long uid){
    	StringBuilder selection = new StringBuilder();
    	selection.append(LastMsgTable.C_ANOTHERID).append("=?");
    	
    	String[] args = new String[]{
    			String.valueOf(uid)
    	};
    	
    	ContentValues values = new ContentValues();
    	values.put(LastMsgTable.C_UNREADNUM,0);
    	getDBProvider().update(LastMsgTable.CONTENT_URI, values, selection.toString(), args);
    }
    
    /**
     * 获取未读消息总数
     * @return
     */
    public static int getUnReadNum(){
    	int count = 0 ;
    	Cursor c = getDBProvider().query(LastMsgTable.CONTENT_URI,new String[]{LastMsgTable.C_UNREADNUM},null,null,null);
    	if(c == null || c.getCount() == 0){
    		return count ;
    	}
    	//注意，要查询的列记得出现在projection中
    	  try {
        	while(c.moveToNext()){
        		int unReadNum = Integer.parseInt(c.getString(c.getColumnIndex(LastMsgTable.C_UNREADNUM)));
        		count = count + unReadNum ;
        	}
    	  }catch (Exception e){
              e.printStackTrace();
          } finally {
    	      if (c != null) {
                  c.close();
                  c = null;
              }
          }
    	return count ;
    }
    
    /**
     * 处理push过期的情况
     * 1.发送人员相同，msgId也相同 ：发送成功的消息，且不是在切换账号期间push过来的
     * 2.发送人员相同，但是msgId不同：
     * 			a.这条消息的时间大于数据库中的时间，切换账号期间push过来的，没有存储                      需要进行替换
     * 			b.这条消息的时间小于数据库中的时间，且数据库中msgId为0，则数据库中为发送失败的消息
     * 3.发送人员不同，切换账号期间push过来的新的人员发来的消息，直接插入
     * 4.发送人员不同，新的人员之间发送失败的消息
     */
    public static void updateLasgMsgList(List<ChatItemInfo> list){
    	Account account = ManagerAccount.getInstance().getCurrentAccount();
		if (account == null)
			return;

		if (list == null || list.size() == 0) {
			// 删除全部
			getDBProvider().delete(LastMsgTable.CONTENT_URI, 
					LastMsgTable.C_STATUS + " <> 2", null);
			return;
		}
    	
    	List<Long> msgIds = new LinkedList<Long>();
    	boolean lastMsgChanged = false;
    	
    	long myUid = ManagerAccount.getInstance().getCurrentId();
		
		Map<Long, MessageInfo> olds = new HashMap<Long, MessageInfo>();
		
		Cursor cursor = getDBProvider().query(LastMsgTable.CONTENT_URI, 
				MessageInfo.SimpleProject, null, null, null);
		
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					MessageInfo info = new MessageInfo(cursor, MessageInfo.T_C_SIMPLE);
					olds.put(info.getReceiver(), info);
				} while (cursor.moveToNext());
			}
			
			cursor.close();
		}
		
		String insertSql, updateSql;
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("INSERT OR IGNORE INTO ").append(LastMsgTable.TABLE_NAME).append('(');
		int columns = appendColumns(buffer, ",");
		buffer.append(')').append(" VALUES (");
		for (int i = 0; i < columns; i++) {
			buffer.append("?,");
		}
		buffer.setCharAt(buffer.length() - 1, ')');
		
		insertSql = buffer.toString();
		buffer.setLength(0);
		
		buffer.append("UPDATE ").append(LastMsgTable.TABLE_NAME).append(" SET ");
		appendColumns(buffer, "=?,");
		buffer.append("=?").append(" WHERE ").append(LastMsgTable.C_ANOTHERID).append("=?");
		
		updateSql = buffer.toString();
		buffer.setLength(0);
		
		SQLiteOpenHelper openHelper = getDBProvider().getSQLiteOpenHelper();
		SQLiteDatabase database = openHelper.getWritableDatabase();
		
		SQLiteStatement insert = database.compileStatement(insertSql);
		SQLiteStatement update = database.compileStatement(updateSql);
		
		database.beginTransaction();
		
		try {
			int size = list.size();
			for(int i = 0; i < size; i++){
				ChatItemInfo info = list.get(i);
				
				long userId = info.anotherUserInfo.uid;
				msgIds.add(userId);
				
				MessageInfo old = olds.get(userId);
				
				if (old == null) {
					if (info.notReadCount == 0) {
						info.message.msgId = 0;
						info.message.type = EgmProtocolConstants.MSG_TYPE.MSG_TYPE_TEXT;
						info.message.msgContent = "";
						info.message.setReserved3(0); // 剔除阅后即焚的状态
					}
					
					bindStatement(insert, myUid, info);
					
					// 插入消息
					if(insert.executeInsert() != -1) {
						// 插入成功
						lastMsgChanged = true;
					}
				}
				else if (info.message.time < old.getTime() && old.getMsgId() < 0){
					// 本地有失败消息，且失败消息更加新，不更新
				}
				else {
					if (info.notReadCount == 0) {
						info.message.msgId = old.getMsgId();
						info.message.type = old.getType();
						info.message.msgContent = old.getMsgContent();
						info.message.setReserved3(old.getReserved3());
					}
					
					// 更新最后消息
					bindStatement(update, myUid, info);
					update.bindLong(columns + 1, userId);
					
					if(update.executeUpdateDelete() != -1) {
						// 更新成功
						lastMsgChanged = true;
					}
				}
			}
			
			insert.close();
			update.close();
			
			//将最近消息列表中有而服务端没有的项删除（跨设备删除了），发送失败的保留
			StringBuilder in = new StringBuilder();
			in.append('(');
			for(long msgId : msgIds){
				in.append(msgId).append(',');
			}
			in.setCharAt(in.length() - 1, ')');
			
			StringBuilder where = new StringBuilder();
			where.append(LastMsgTable.C_ANOTHERID).append(" NOT IN ").append(in.toString())
				.append(" AND ").append(LastMsgTable.C_STATUS).append(" <> 2");
			
			int count = database.delete(LastMsgTable.TABLE_NAME, where.toString(), null);
			
			if (count > 0) {
				lastMsgChanged = true;
			}
			
			if (lastMsgChanged) {
				Context context = BaseService.getServiceContext();
				context.getContentResolver().notifyChange(
						LastMsgTable.CONTENT_URI, null);
			}
			
			database.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		database.endTransaction();
    }
    
    private static int appendColumns(StringBuffer buffer, String split) {
    	buffer.append(LastMsgTable.C_UNREADNUM).append(split) // 0
		.append(LastMsgTable.C_ANOTHERID).append(split)
		.append(LastMsgTable.C_FROMID).append(split)
		.append(LastMsgTable.C_TIME).append(split)
		.append(LastMsgTable.C_CONTENT).append(split)  // 5
		.append(LastMsgTable.C_MEDIA_URL).append(split) 
		.append(LastMsgTable.C_TYPE).append(split)
		.append(LastMsgTable.C_MSGID).append(split)
		.append(LastMsgTable.C_DURATION).append(split) 
		.append(LastMsgTable.C_EXTRA_ID).append(split) // 10
		.append(LastMsgTable.C_EXTRA).append(split)
		.append(LastMsgTable.C_USERCP).append(split)
		.append(LastMsgTable.C_STATUS).append(split) 
		.append(LastMsgTable.C_ATTACH).append(split)
		.append(LastMsgTable.C_ANOTHERID).append(split) //15
		.append(LastMsgTable.C_ISVIP).append(split)
		.append(LastMsgTable.C_ISNEW).append(split)
		.append(LastMsgTable.C_CROWNID).append(split)
		.append(LastMsgTable.C_NICK).append(split) 
		.append(LastMsgTable.C_AVATAR).append(split) // 20
		.append(LastMsgTable.C_RESERVED3); // 
    	
    	return 21;
    }

	private static void bindStatement(SQLiteStatement statement, long myUid,
			ChatItemInfo info) {
		//当前聊天人，直接已读
		statement.bindLong(1, myUid == info.anotherUserInfo.uid ? 0 : info.notReadCount);
		
        //处理 MessageInfo
        if(myUid == info.message.receiver){//another 为聊天对方
        	statement.bindLong(2, info.message.sender);
        } else {
        	statement.bindLong(2, info.message.receiver);
        }
        
        statement.bindLong(3, info.message.sender);
        statement.bindLong(4, info.message.time);
        
        info.message.setEncrypted();
        bindString(statement, 5, PDEEngine.PXEncrypt(info.message.msgContent));
//        bindString(statement, 5, info.message.msgContent);
        bindString(statement, 6, info.message.mediaUrl);
        statement.bindLong(7, info.message.type);
        statement.bindLong(8, info.message.msgId);
        statement.bindLong(9, info.message.duration);
        statement.bindLong(10, info.message.extraId);
        
        if (null == info.message.extraString && info.message.extra != null){
        	info.message.extraString = info.message.extra.toString();
        }
        
        bindString(statement, 11, info.message.extraString);
        statement.bindLong(12, info.message.usercp);
        
        //对方发送来的消息，直接将状态设置为发送成功
        int status ;
        if (info.message.sender != myUid){
        	status = EgmConstants.Sending_State.SEND_SUCCESS;
        }
        else{
            if(info.message.status >= 0){
            	status = info.message.status;
            }else {
            	status = EgmConstants.Sending_State.SEND_SUCCESS;
            }
        }
        
        statement.bindLong(13, status);
        bindString(statement, 14, info.message.attach);
        statement.bindLong(15, info.anotherUserInfo.uid);
        statement.bindLong(16, info.anotherUserInfo.isVip ? 1 : 0);
        statement.bindLong(17, info.anotherUserInfo.isNew ? 1 : 0);
        statement.bindLong(18, info.anotherUserInfo.crownId);
        bindString(statement, 19, info.anotherUserInfo.nick);
        bindString(statement, 20, info.anotherUserInfo.portraitUrl192);
        
        statement.bindLong(21, info.message.getReserved3());
	}
}
