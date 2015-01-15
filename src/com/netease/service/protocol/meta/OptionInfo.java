package com.netease.service.protocol.meta;

/**
 * 用户资料多选项
 */
public class OptionInfo {
	public int key ;//选项编号
	public String value ;//选项内容
	
	public OptionInfo(int key,String value){
		this.key  = key ;
		this.value = value ;
	}
}
