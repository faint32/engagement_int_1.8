package com.netease.service.protocol.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import com.google.gson.Gson;
import com.netease.service.protocol.meta.DebugData.BaseDataTest;

/**
 * 聊天列表返回数据项
 */
public class ChatItemInfo {
	public int notReadCount ;//未读消息个数
	public MessageInfo message ;//消息内容
	public ChatItemUserInfo anotherUserInfo ;//对方的userinfo 
	
	public ChatItemInfo(){
		message = new MessageInfo();
		anotherUserInfo = new ChatItemUserInfo();
	}
	public static String toJson(ChatItemInfo info){
		if(info == null){
			return null ;
		}
		Gson gson = new Gson ();
		String result = gson.toJson(info);
		return result ;
	}
	
	public static final String FILE_NAME = "chatlist.json";
	
	public static String generateTestChatItemInfo(){
        Gson gson = new Gson();
        Random random = new Random();
        int count = random.nextInt(10);
        List<ChatItemInfo> list = new ArrayList<ChatItemInfo>();
        for(int i = 0; i<count ;i++){
        	list.add(new ChatItemInfo());
        }
        BaseDataTest baseData = new BaseDataTest();
        baseData.code = 0;
        baseData.message = "success";
        baseData.data = list;
        String jsonString = gson.toJson(baseData);
        DebugData.saveTestData(FILE_NAME, jsonString);
        return jsonString;    
    }
}
