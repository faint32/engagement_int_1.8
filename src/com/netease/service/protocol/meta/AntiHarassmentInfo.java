package com.netease.service.protocol.meta;

public class AntiHarassmentInfo {
	public int noPortrait;//无头像用户骚扰配置： 0 允许 1不允许
	public int levelLimit;//等级不足（1级）用户配置：0 允许 1 不允许
	
	public boolean noPortaitOn(){
		return noPortrait == 1;
	}
	
	public boolean levelLimitOn(){
		return levelLimit == 1;
	}
    
	public void setNoPortait(boolean bOn){
		noPortrait = bOn ? 1:0;
	}
	public void setLevelLimit(boolean bOn){
		levelLimit = bOn ? 1:0;
	}
}
