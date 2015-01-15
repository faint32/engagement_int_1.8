package com.netease.service.protocol.meta;

import java.util.ArrayList;
import java.util.Random;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.meta.DebugData.BaseDataTest;

/**
 * 排行榜列表里的用户数据结构
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class RankUserInfo {
    private static final boolean DEBUG = EgmProtocolConstants.RANDOM_DEBUG_DATA;
    
    public long uid;//用户ID
    public int sex;//性别 0:女 1:男
    public int age;//年龄
    public int height;//身高
    public long crownId;//当前戴的皇冠的id
    public String crownUrl;//皇冠图片链接
    public int level;//当前等级
    public String levelName;//等级对应封号
    public long usercp;//当前魅力值（豪气值），不会减少
    public long visitTimes; // 访问人气(女)
    public String nick;//用户昵称
    public boolean isVip;//是否vip
    public String portraitUrl;//用户头像原图url
    public String portraitUrl192;//用户192x192头像裁剪图url
    public String portraitUrl640;//用户640x480头像裁剪图url
    public String portraitUrl316;//用户316x237头像裁剪图url
    public long privatePhotoCount;//私照数(女)
    public int status;//用户状态：0 正常 1冻结
    public boolean isNew;//是否新用户
    public long value;//用于私照榜人气值
    public RankUserInfo(){
        if (DEBUG) {
            Random random = new Random();
            
            uid = random.nextInt(100000000);
            sex = random.nextInt(2);
            age = random.nextInt(60);
            height = random.nextInt(250)%(250-120+1) + 120;
            crownId = random.nextInt(100000000);
            crownUrl = DebugData.getUserImage();
            
            level = random.nextInt(10)%(10-1+1) + 1;
            levelName = DebugData.getText();
            usercp = random.nextInt(1000000);
            nick = DebugData.getText();
            isVip = random.nextBoolean();
            portraitUrl = DebugData.getUserImage();
            portraitUrl192 = DebugData.getUserImage();
            portraitUrl640 = DebugData.getUserImage();
            portraitUrl316 = DebugData.getUserImage();
            privatePhotoCount = random.nextInt(100000000);
            status = random.nextInt(2);
            isNew = random.nextBoolean();
        }
    }
    
    /**
     * 从JsonElement中解出数据数组
     * @param json
     */
    public static RankUserInfo[] fromJson(JsonElement json){
        if(json == null)
            return null;
        
        Gson gson = new Gson();
        return gson.fromJson(json, new TypeToken<RankUserInfo[]>(){}.getType());
    }
    
    // For Debug
    public static String generateTestInfo(){
        ArrayList<RankUserInfo> infos = new ArrayList<RankUserInfo>(100);
        for(int i = 0; i < 100; i++){
            RankUserInfo info = new RankUserInfo();
            infos.add(info);
        }
        
        Gson gson = new Gson();
        BaseDataTest baseData = new BaseDataTest();
        baseData.code = 0;
        baseData.message = "success";
        baseData.data = infos;
        String jsonString = gson.toJson(baseData);
        DebugData.saveTestData(DebugData.FILENAME_RANK_JSON, jsonString);
        
        return jsonString;    
    }
}
