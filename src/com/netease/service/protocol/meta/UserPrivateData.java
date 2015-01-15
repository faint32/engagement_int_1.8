package com.netease.service.protocol.meta;

import java.util.Random;


import com.google.gson.Gson;
import com.netease.service.protocol.meta.DebugData.BaseDataTest;

public class UserPrivateData {
	public UserInfo userInfo ;
	public PictureInfo[] publicPicList ;//公开照片列表
	
	public static String toJsonString(UserPrivateData user) {
        Gson gson = new Gson();
        return gson.toJson(user);
    }
	
	public UserPrivateData(){
		userInfo = new UserInfo();
		Random random = new Random();
		int publicNum = random.nextInt(11);
		publicPicList = new PictureInfo[publicNum];
		for(int i= 0;i<publicNum;i++){
			publicPicList[i] = new PictureInfo();
		}
	}
	
	public static final String FILE_NAME = "userprivatedata.json";
	public static String generateTestData(){
        Gson gson = new Gson();
        UserPrivateData data = new UserPrivateData();
        BaseDataTest baseData = new BaseDataTest();
        baseData.code = 0;
        baseData.message = "success";
        baseData.data = data;
        String jsonString = gson.toJson(baseData);
        DebugData.saveTestData(FILE_NAME, jsonString);
        return jsonString;    
    }
}
