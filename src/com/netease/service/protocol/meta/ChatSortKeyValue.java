package com.netease.service.protocol.meta;

public class ChatSortKeyValue implements Comparable<ChatSortKeyValue>{
	public long uid ;//用户id
	public long sorVvalue;//排序值
	@Override
	public int compareTo(ChatSortKeyValue another) {
		int i = 0 ;
		if(sorVvalue > another.sorVvalue){
			//按照降序进行排序
			i = -1 ;
		}else if(sorVvalue < another.sorVvalue){
			i = 1 ;
		}
		return i ;
	}
}
