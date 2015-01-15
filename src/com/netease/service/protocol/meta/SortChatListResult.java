package com.netease.service.protocol.meta;

/**
 * 聊天列表后台排序返回结果
 */
public class SortChatListResult {
	public ChatSortKeyValue[]  sortList ;//用户id与对应排序值key-value的数组
	public int sortType ;//排序类型（1：魅力值 2：亲密度）
}
