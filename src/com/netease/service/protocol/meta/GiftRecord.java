package com.netease.service.protocol.meta;

/**
 * 礼物赠送记录
 */
public class GiftRecord {
	public long recordId ;//礼物赠送记录ID
	public GiftInfo giftInfo ;//礼物信息
	public long date ;//赠送日期
	public long fromUserId ;//送出者UserId
	public String fromUserName ;//送出者昵称
	public long toUserId ;//接收者UserId
	public String toUserName ;//接收者昵称
}
