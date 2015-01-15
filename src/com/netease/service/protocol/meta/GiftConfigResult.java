package com.netease.service.protocol.meta;

/**
 * 获取礼物配置数据返回结构
 */
public class GiftConfigResult {
	public GiftGroupInfo[] giftGroup ;//礼物包
	public String url ;//图片包地址
	public String version ;//图片包版本
	public String giftUrl; // 礼物图片url根地址，完整的地址是在这个url后面加上图片的ID和图片后缀(.jpg)
}
