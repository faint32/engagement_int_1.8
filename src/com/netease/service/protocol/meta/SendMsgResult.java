package com.netease.service.protocol.meta;

/**
 * 发送消息返回
 *
 */
public class SendMsgResult {
	public MessageInfo messageInfo ;//消息信息
	public String thanks ;//答谢语句（发送礼物才有）
	public int usercp ;//增加的豪气值
	public int intimacy ;//增加的亲密度
	public int userLevel;//发送者的等级(发送礼物的消息才有值)
	public int matchType;//命中关键字类型 
	public String tips;//命中关键字提示语 
	
}
