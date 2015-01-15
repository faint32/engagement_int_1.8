package com.netease.service.protocol.meta;

import java.util.Random;


import com.google.gson.Gson;
import com.netease.service.protocol.meta.DebugData.BaseDataTest;

public class PictureInfos {
	public PictureInfo[] pictureInfos ;//照片列表
	public int count ;//每页多少
	public int totalCount ;//总数
	public int freeTimes ;//可以免费查看的次数
	
	public PictureInfos(){
		Random random = new Random();
		int picnum = random.nextInt(40);
		pictureInfos = new PictureInfo[picnum];
		for(int i= 0;i<picnum;i++){
			pictureInfos[i] = new PictureInfo();
		}
		count = 10 ;
		totalCount = 20 ;
	}
	
	public static final String FILE_NAME = "pictures.json";
	public static String generateTestData(){
        Gson gson = new Gson();
        PictureInfos data = new PictureInfos();
        BaseDataTest baseData = new BaseDataTest();
        baseData.code = 0;
        baseData.message = "success";
        baseData.data = data;
        String jsonString = gson.toJson(baseData);
        DebugData.saveTestData(FILE_NAME, jsonString);
        return jsonString;    
    }
}
