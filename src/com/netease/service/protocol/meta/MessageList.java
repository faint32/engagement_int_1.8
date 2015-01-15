package com.netease.service.protocol.meta;

import java.util.List;

/**
 * 聊天列表
 */
public class MessageList {
	public List<MessageInfo> msgList ;//消息列表
	public int count ;//每次取的个数
	public int intimacy ;//亲密度
	public String portraitUrl192 ;//用户192x192头像裁剪图url
	public String nick ;//昵称
	public SpecialGift[] specialGifts ;//特殊礼物列表
	public boolean isYixinFriend;//是否易信好友
	public MessageInfo[] fireMsgList;//被焚消息列表
	public ChatSkillInfo[] chatSkills;//对方（女性）的聊天技
}
