package com.netease.service.protocol.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.meta.DebugData.BaseDataTest;

/**
 * 推荐列表里的用户数据结构
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class RecommendUserInfo {
    private static final boolean DEBUG = EgmProtocolConstants.RANDOM_DEBUG_DATA;
    
    public long uid;//用户ID
    public int sex;//性别 0:女 1:男
    public int age;//年龄
    public int height;//身高
    public int bust;//三围-胸围(女)
    public int waist;//三围-腰围(女)
    public int hip;//三围-臀围(女)
    public int cup;//三围-罩杯(女)
    /** 当前等级 */
    public int level;
    /** 等级对应封号 */
    public String levelName;
    /** 当前魅力值（豪气值），不会减少 */
    public long usercp;
    public String nick;//用户昵称
    public boolean isVip;//是否vip
    public String portraitUrl;//用户头像原图url
    public String portraitUrl192;//用户192x192头像裁剪图url
    public String portraitUrl640;//用户640x480头像裁剪图url
    public String portraitUrl316;//用户316x237头像裁剪图url
    public long visitTimes;//访问人气(女)
    public long privatePhotoCount;//私照数(女)
    public int status;//用户状态：0 正常 1冻结
    public boolean isNew;//是否新用户
	public String alg; // 推荐算法统计字段

    public RecommendUserInfo() {
        if (DEBUG) {
            Random random = new Random();
            
            uid = random.nextInt(100000000);
            sex = random.nextInt(2);
            age = random.nextInt(60);
            height = random.nextInt(250)%(250-120+1) + 120;
            bust = random.nextInt(100);
            waist = random.nextInt(100);
            hip = random.nextInt(100);
            cup = random.nextInt(20);
            
            
            level = random.nextInt(10)%(10-1+1) + 1;
            levelName = DebugData.getText();
            usercp = random.nextInt(1000000);
            nick = DebugData.getText();
            isVip = random.nextBoolean();
            portraitUrl = DebugData.getUserImage();
            portraitUrl192 = DebugData.getUserImage();
            portraitUrl640 = DebugData.getUserImage();
            portraitUrl316 = DebugData.getUserImage();
            visitTimes = random.nextInt(100000000);
            privatePhotoCount = random.nextInt(100000000);
            status = random.nextInt(2);
            isNew = random.nextBoolean();
        }
    }
    
    /** 从JsonElement中解出数据数组 */
    public static List<RecommendUserInfo> fromeJson(JsonElement json){
        if(json == null)
            return null;
        
        Gson gson = new Gson();
        return gson.fromJson(json, new TypeToken<List<RecommendUserInfo>>(){}.getType());
    }

    // For Debug
    public static String generateTestInfo(){
        ArrayList<RecommendUserInfo> infos = new ArrayList<RecommendUserInfo>(100);
        for(int i = 0; i < 100; i++){
            RecommendUserInfo info = new RecommendUserInfo();
            infos.add(info);
        }
        
        Gson gson = new Gson();
        BaseDataTest baseData = new BaseDataTest();
        baseData.code = 0;
        baseData.message = "success";
        baseData.data = infos;
        String jsonString = gson.toJson(baseData);
        DebugData.saveTestData(DebugData.FILENAME_RECOMMEND_JSON, jsonString);
        
        return jsonString;    
    }
}
