package com.netease.service.protocol.meta;


public class UserInfoConfig {
	public OptionInfo[] cup ;//罩杯
	public OptionInfo[] satisfiedPart;//最满意部位
	public OptionInfo[] favorDate;//喜欢的约会
	public OptionInfo[] hobbyFemale;//女性兴趣爱好
	public OptionInfo[] hobbyMale;//男性兴趣爱好
	public OptionInfo[] skill;//想学（男：擅长）的技能
	public OptionInfo[] income;//月收入（男）
	public OptionInfo[] levelNameFemale;//女性封号
	public OptionInfo[] levelNameMale;//男性封号	
	public OptionInfo[] constellation ;//星座
	public OptionInfo[] searchIncome ;//搜索的收入项
	
	public String[] vipPicUrl; // 推荐页底部vip入口背景假用户图列表
	
	/** 金币充值webUrl */
	public String coinChargeUrl;
	/** vip升级 webUrl */
	public String vipChargeUrl;
	/** 网易宝账户webUrl */
	public String epayUrl;     

	public String version;//版本号
}
