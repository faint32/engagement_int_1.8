package com.netease.engagement.dataMgr;

import com.netease.service.db.MsgDBTables;
import com.netease.service.db.MsgDBTables.MsgTable;
import com.netease.util.PDEEngine;

import android.database.Cursor;
import android.text.TextUtils;

/**
 * 聊天列表界面显示数据结构
 */
public class ChatListMeta {
	public boolean isVip;
	public boolean isNew;
	public String portraitUrl192;
	public String nick;
	public long time;
	public String msgContent;
	public int unReadNum ;
	public long anotherId ;
	public int msgType ;
	public long extraId ;
	public int state ;
	
	public int reserved3;
	public int sendType;
	
	public int isEncrypted; // 是否加密存储（本地结构），0未加密，1加密

	public static String[] Projection = new String[] {
			MsgDBTables.LastMsgTable._ID, 
			MsgDBTables.LastMsgTable.C_AVATAR,
			MsgDBTables.LastMsgTable.C_CROWNID,
			MsgDBTables.LastMsgTable.C_ISNEW, 
			MsgDBTables.LastMsgTable.C_ISVIP,
			MsgDBTables.LastMsgTable.C_NICK,
			MsgDBTables.LastMsgTable.C_UNREADNUM,
			MsgDBTables.LastMsgTable.C_CONTENT, 
			MsgDBTables.LastMsgTable.C_TIME,
			MsgDBTables.LastMsgTable.C_ANOTHERID,
			MsgDBTables.LastMsgTable.C_TYPE,
			MsgDBTables.LastMsgTable.C_EXTRA_ID,
			MsgDBTables.LastMsgTable.C_STATUS,
			MsgDBTables.LastMsgTable.C_RESERVED3,
	};
	
	public static ChatListMeta fillData(Cursor cursor){
		if(cursor == null){
			return null;
		}
		//取出当前位置的ChatItemInfo
		ChatListMeta meta = new ChatListMeta() ;
		meta.setReserved3(cursor.getInt(cursor.getColumnIndex(MsgTable.C_RESERVED3))); // 需要把这个放在 C_CONTENT 前面
		
		meta.setVip(cursor.getInt(cursor.getColumnIndex(MsgDBTables.LastMsgTable.C_ISVIP))== 1 ? true : false);
		meta.setNew(cursor.getInt(cursor.getColumnIndex(MsgDBTables.LastMsgTable.C_ISNEW)) == 1 ? true : false);
		meta.setNick(cursor.getString(cursor.getColumnIndex(MsgDBTables.LastMsgTable.C_NICK)));
		meta.setPortraitUrl192(cursor.getString(cursor.getColumnIndex(MsgDBTables.LastMsgTable.C_AVATAR)));
		
		String content = cursor.getString(cursor.getColumnIndex(MsgDBTables.LastMsgTable.C_CONTENT));
		if (meta.isEncrypted()) {
			content = PDEEngine.PXDecrypt(content);
		}
		meta.setMsgContent(content);
		meta.setTime(cursor.getLong(cursor.getColumnIndex(MsgDBTables.LastMsgTable.C_TIME)));
		meta.setAnotherId(cursor.getLong(cursor.getColumnIndex(MsgDBTables.LastMsgTable.C_ANOTHERID)));
		meta.setUnReadNum(cursor.getInt(cursor.getColumnIndex(MsgDBTables.LastMsgTable.C_UNREADNUM)));
		meta.setMsgType(cursor.getInt(cursor.getColumnIndex(MsgDBTables.LastMsgTable.C_TYPE)));
		meta.setExtraId(cursor.getLong(cursor.getColumnIndex(MsgDBTables.LastMsgTable.C_EXTRA_ID)));
		meta.setState(cursor.getInt(cursor.getColumnIndex(MsgDBTables.LastMsgTable.C_STATUS)));
		
		return meta ;
	}
	
	
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getUnReadNum() {
		return unReadNum;
	}

	public void setUnReadNum(int unReadNum) {
		this.unReadNum = unReadNum;
	}

	public long getAnotherId() {
		return anotherId;
	}

	public void setAnotherId(long anotherId) {
		this.anotherId = anotherId;
	}

	public boolean isVip() {
		return isVip;
	}

	public void setVip(boolean isVip) {
		this.isVip = isVip;
	}

	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	public String getPortraitUrl192() {
		return portraitUrl192;
	}

	public void setPortraitUrl192(String portraitUrl192) {
		this.portraitUrl192 = portraitUrl192;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	public long getExtraId() {
		return extraId;
	}

	public void setExtraId(long extraId) {
		this.extraId = extraId;
	}

	public int getReserved3() {
		return reserved3;
	}

	public void setReserved3(int reserved3) {
		this.reserved3 = reserved3;
		sendType = reserved3 & 0xFF;
		
		isEncrypted = (reserved3 >> 10) & 0x01;
	}
	
	public boolean isEncrypted() {
		return isEncrypted > 0;
	}

	public int getSendType() {
		return sendType;
	}

	public void setSendType(int sendType) {
		this.sendType = sendType;
	}
	
}
