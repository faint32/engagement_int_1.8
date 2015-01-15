package com.netease.service.protocol.meta;

import java.io.Serializable;

import android.database.Cursor;
import android.text.TextUtils;

import com.google.gson.JsonElement;
import com.netease.engagement.dataMgr.MsgAttach;
import com.netease.service.db.MsgDBTables.LastMsgTable;
import com.netease.service.db.MsgDBTables.MsgTable;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.meta.IMetaContants.IMessageInfo;
import com.netease.util.PDEEngine;


/**
 * 消息结构
 */
public class MessageInfo implements IMessageInfo, Serializable {
	
	public long sender ;//聊天发送者uid
	public long receiver ;//聊天接收者uid
	public long time ;//消息的时间
	public String msgContent ;//消息文本内容
	public String mediaUrl ;//图片、音频、视频、礼物、私照地址
	public int type ;//消息类型   0：文本； 1：私照 ；2：本地照片；3：音频；4：视频；5：礼物；6：系统消息
	public long msgId ;//消息的Id
	public int duration ;//时长
	public long extraId ;//消息扩展id，如礼物id
	public String extraString ;//jsonString,聊天扩展内容,MsgExtra类型，使用时解析
	public int usercp;//收礼物或送礼物后增加的魅力值或是豪气值
	public int userLevel;//当前接收者的等级(收到礼物的消息才有值)
	
	public int ver; // 消息体版本，版本号呈整数增长：0为最低版本，1增加消息文本加密
	
	public int sendType;//消息发送类型   0:普通消息   1:阅后即焚消息
	public int isCameraPhoto;//是否是手机拍照图片,0非手机拍照图片，1手机拍照图片
	public int isFireMsgOpened; // 是否阅后即焚消息已读，0未读，1已读
	public int isEncrypted; // 是否加密存储（本地结构），0未加密，1加密
	
	public JsonElement extra;//解析时的临时对象，存入数据库时使用extraString
	public String tips;//命中关键字时的提示语
	public String faceId;//表情id
	public String animat;//动画效果图片url
	
	/**
	 * 以下为客户端本地使用
	 */
	public String attach;//客户端本地附加内容（可组合为jsonstring存入，音视频图片下载保存地址、文件名等）
	public int status = -1;//客户端本地，消息状态
	public String profileUrl;//聊天对象头像
	
	public boolean playStatus;  //用于记录 收到的 音频 信息是否已经播放过。   true:已播放    false：未播放
	
	public static MessageInfo getMessageInfo(Cursor cursor){
		MessageInfo info = new MessageInfo();
		
		info.setSender(Long.parseLong(cursor.getString(cursor.getColumnIndex(MsgTable.C_FROMID))));
		info.setReceiver(Long.parseLong(cursor.getString(cursor.getColumnIndex(MsgTable.C_ANOTHERID))));
		info.setMsgId(Long.parseLong(cursor.getString(cursor.getColumnIndex(MsgTable.C_MSGID))));
		info.setType(Integer.parseInt(cursor.getString(cursor.getColumnIndex(MsgTable.C_TYPE))));
		info.setStatus(Integer.parseInt(cursor.getString(cursor.getColumnIndex(MsgTable.C_STATUS))));
		info.setTime(Long.parseLong(cursor.getString(cursor.getColumnIndex(MsgTable.C_TIME))));
		
		info.setReserved3(cursor.getInt(cursor.getColumnIndex(MsgTable.C_RESERVED3)));
		
		String s = cursor.getString(cursor.getColumnIndex(MsgTable.C_RESERVED1));
		if ("0".equalsIgnoreCase(s)) { // 未播放
			info.playStatus = false;
		} else { // 已播放    s为空值 或 s为1
			info.playStatus = true;
		}
		
		String content = cursor.getString(cursor.getColumnIndex(MsgTable.C_CONTENT));
		if (info.isEncrypted()) {
			content = PDEEngine.PXDecrypt(content);
		}
		
		switch(info.getType()){
			case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_TEXT:
				info.setMsgContent(content);
				info.setAttach(cursor.getString(cursor.getColumnIndex(MsgTable.C_ATTACH)));
				if(!TextUtils.isEmpty(info.getAttach())){
					MsgAttach attach = MsgAttach.toMsgAttach(info.getAttach());
					if(!TextUtils.isEmpty(attach.tips) && attach.matchType <= 0){
						info.setTips(attach.tips);
					}
				}
				break;
			case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_LOCAL_PIC:
				info.setMediaUrl(cursor.getString(cursor.getColumnIndex(MsgTable.C_MEDIA_URL)));
				info.setAttach(cursor.getString(cursor.getColumnIndex(MsgTable.C_ATTACH)));
				break;
			case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_AUDIO:
				info.setMediaUrl(cursor.getString(cursor.getColumnIndex(MsgTable.C_MEDIA_URL)));
				info.setDuration(Integer.parseInt(cursor.getString(cursor.getColumnIndex(MsgTable.C_DURATION))));
				info.setAttach(cursor.getString(cursor.getColumnIndex(MsgTable.C_ATTACH)));
				break;
			case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_PRIVATE_PIC:
				info.setMediaUrl(cursor.getString(cursor.getColumnIndex(MsgTable.C_MEDIA_URL)));
				info.setAttach(cursor.getString(cursor.getColumnIndex(MsgTable.C_ATTACH)));
				info.setExtraId(Long.parseLong(cursor.getString(cursor.getColumnIndex(MsgTable.C_EXTRA_ID))));
				break;
			case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_GIFT:
				info.setExtraId(Long.parseLong(cursor.getString(cursor.getColumnIndex(MsgTable.C_EXTRA_ID))));
				info.setUsercp(Integer.parseInt(cursor.getString(cursor.getColumnIndex(MsgTable.C_USERCP))));
				info.setAnimat(cursor.getString(cursor.getColumnIndex(MsgTable.C_RESERVED2)));
				break;
			case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_VIDEO:
				info.setDuration(Integer.parseInt(cursor.getString(cursor.getColumnIndex(MsgTable.C_DURATION))));
				info.setMsgContent(content);
				info.setMediaUrl(cursor.getString(cursor.getColumnIndex(MsgTable.C_MEDIA_URL)));
				info.setAttach(cursor.getString(cursor.getColumnIndex(MsgTable.C_ATTACH)));
				break;
			case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_FACE:
				info.setFaceId(cursor.getString(cursor.getColumnIndex(MsgTable.C_RESERVED2)));
				info.setMsgContent(content);
				info.setMediaUrl(cursor.getString(cursor.getColumnIndex(MsgTable.C_MEDIA_URL)));
				break;
			case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_SYS:
				info.setMsgContent(content);
				info.setExtraString(cursor.getString(cursor.getColumnIndex(MsgTable.C_EXTRA)));
				break;
		}
		return info ;
	}
	
	public MessageInfo() {
		
	}
	
	public MessageInfo(Cursor cursor, int type) {
		switch (type) {
		case T_C_SIMPLE:
			msgId = cursor.getLong(S_C_MSGID);
			receiver = cursor.getLong(S_C_ANOTHERID);
			time = cursor.getLong(S_C_TIME);
			status = cursor.getInt(S_C_STATUS);
			this.type = cursor.getInt(S_C_TYPE);
			msgContent = cursor.getString(S_C_CONTENT);
			setReserved3(cursor.getInt(S_C_RESERVED3));
			
			if (isEncrypted()) {
				msgContent = PDEEngine.PXDecrypt(msgContent);
			}
			break;
		case T_C_DEFAULT:
			sender = cursor.getLong(cursor.getColumnIndex(MsgTable.C_FROMID));
			long another = cursor.getLong(cursor.getColumnIndex(MsgTable.C_ANOTHERID));
			if (sender == another) {
				receiver = Long.valueOf(ManagerAccount.getInstance()
						.getCurrentAccountId());
			} else {
				receiver = another;
			}
			time = cursor.getLong(cursor.getColumnIndex(MsgTable.C_TIME));
			msgContent = cursor.getString(cursor.getColumnIndex(MsgTable.C_CONTENT));
			mediaUrl = cursor.getString(cursor.getColumnIndex(MsgTable.C_MEDIA_URL));
			this.type = cursor.getInt(cursor.getColumnIndex(MsgTable.C_TYPE));
			msgId = cursor.getLong(cursor.getColumnIndex(MsgTable.C_MSGID));
			duration = cursor.getInt(cursor.getColumnIndex(MsgTable.C_DURATION));
			extraId = cursor.getLong(cursor.getColumnIndex(MsgTable.C_EXTRA_ID));
			extraString = cursor.getString(cursor.getColumnIndex(MsgTable.C_EXTRA));
			usercp = cursor.getInt(cursor.getColumnIndex(MsgTable.C_USERCP));
			attach = cursor.getString(cursor.getColumnIndex(MsgTable.C_ATTACH));
			status = cursor.getInt(cursor.getColumnIndex(MsgTable.C_STATUS));
			setReserved3(cursor.getInt(cursor.getColumnIndex(LastMsgTable.C_RESERVED3)));
			
			if (isEncrypted()) {
				msgContent = PDEEngine.PXDecrypt(msgContent);
			}
			break;
		}
	}
	
	public long getSender() {
		return sender;
	}
	public void setSender(long sender) {
		this.sender = sender;
	}
	public long getReceiver() {
		return receiver;
	}
	public void setReceiver(long receiver) {
		this.receiver = receiver;
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
	public String getMediaUrl() {
		return mediaUrl;
	}
	public void setMediaUrl(String mediaUrl) {
		this.mediaUrl = mediaUrl;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public long getExtraId() {
		return extraId;
	}
	public void setExtraId(long extraId) {
		this.extraId = extraId;
	}
	public String getExtraString() {
		return extraString;
	}
	public void setExtraString(String extraString) {
		this.extraString = extraString;
	}
	public int getUsercp() {
		return usercp;
	}
	public void setUsercp(int usercp) {
		this.usercp = usercp;
	}
	public int getUserLevel() {
		return userLevel;
	}
	public void setUserLevel(int userLevel) {
		this.userLevel = userLevel;
	}
	public String getAttach() {
		return attach;
	}
	
	public MsgAttach getMsgAttach() {
		MsgAttach ret = null;
		
		if (! TextUtils.isEmpty(attach)) {
			ret = MsgAttach.toMsgAttach(attach);
		}
		
		return ret;
	}
	
	public void setAttach(String attach) {
		this.attach = attach;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getProfileUrl() {
		return profileUrl;
	}
	public void setProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
	}
	public boolean isPlayStatus() {
		return playStatus;
	}
	public void setPlayStatus(boolean playStatus) {
		this.playStatus = playStatus;
	}
	public int getSendType() {
		return sendType;
	}
	public void setSendType(int sendType) {
		this.sendType = sendType;
	}
	
	public String getFaceId() {
		return faceId;
	}

	public void setFaceId(String faceId) {
		this.faceId = faceId;
	}
	
	public String getAnimat() {
		return animat;
	}

	public void setAnimat(String animat) {
		this.animat = animat;
	}

	public int getIsCameraPhoto() {
		return isCameraPhoto;
	}
	public void setIsCameraPhoto(int isCameraPhoto) {
		this.isCameraPhoto = isCameraPhoto;
	}
	
	public boolean isFireMsgOpened() {
		return isFireMsgOpened != 0;
	}
	
	public void setFireMsgOpened(boolean value) {
		isFireMsgOpened = value ? 1 : 0;
	}
	
	public boolean isFireMsg() {
		return sendType == EgmProtocolConstants.MSG_SENDTYPE.MSG_SENDTYPE_FIRE;
	}

	public void setReserved3(int value) {
		isCameraPhoto = (value >> 8) & 0x01;
		isFireMsgOpened = (value >> 9) & 0x01;
		isEncrypted = (value >> 10) & 0x01;
		sendType = value & 0xFF;
	}
	
	public int getReserved3() {
		return (isCameraPhoto << 8) | (isFireMsgOpened << 9) 
				| (isEncrypted << 10) | (0xFF & sendType);
	}
	
	public void setEncrypted() {
		isEncrypted = 1;
	}
	
	public boolean isEncrypted() {
		return isEncrypted != 0;
	}
	
	public String getTips() {
		return tips;
	}

	public void setTips(String tips) {
		this.tips = tips;
	}

	
	public MessageInfo getSimpleInfo() {
		MessageInfo info = new MessageInfo();
		info.msgId = msgId;
		info.sender = sender;
		info.receiver = receiver;
		info.sendType = sendType;
		info.time = time;
		
		return info;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof MessageInfo) {
			MessageInfo info = (MessageInfo) o;
			
			return msgId == info.msgId && sender == info.sender && receiver == info.receiver;
		}
		
		return super.equals(o);
	}
	
	@Override
	public int hashCode() {
		return (int) (msgId * 13 + sender);
	}
}
