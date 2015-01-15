package com.netease.service.protocol.meta;


/**
 * 单个礼物对象
 */
public class GiftInfo {
	public int id ;//礼物id
	public String name ;//礼物名称
	public int type ;//礼物类型，（0：普通礼物，1：皇冠）
	public int price ;//礼物所需金币值
	public int times = 0;//有次数限制的礼物，剩余可发送的次数
	public int specialGift;//是否特殊礼物（0：不是，1：是）
	public int usercp;//魅力值/豪气值
	public int vipPrice;//VIP会员需要的金币数
	public int share;//是否要弹层分享: 0不分享，1分享
	public int status; // <2就可以使用啊
	public int backupId; // 礼物下线后需要替换发送的礼物id
	
	public boolean choosed ;

	public boolean isVisible() {
		return status < 2 && (specialGift == 0 || times > 0);
	}

	public boolean isCrown() {
		return type == 1;
	}
}
