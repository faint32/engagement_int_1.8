package com.netease.service.protocol.meta;

public class SendGiftResult {
	public String thanks ;//答谢语句（解锁照片不需要，普通才有）
	public String privacyUrl ;//私照图片的地址（解锁私照才有）
	public int freeTimes ;
	
	//506余额不足
	//509 参数错误
	//1105解私照时 照片不存在
	//1500 购买的王冠礼物比当前级别低
	//1501 礼物不存在
	public int code ;

	public int usercp ;//增加的豪气值
	public int intimacy ;//增加的亲密度
	public ChatItemInfo chatItemInfo;
	
	public int userLevel; //用户等级
}
