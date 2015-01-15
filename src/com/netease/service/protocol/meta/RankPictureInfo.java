package com.netease.service.protocol.meta;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/** 排行榜背景图片 */
public class RankPictureInfo{
    public String version;
    public int code;
    
    public String rankPic0;
    public String rankPic1;
    public String rankPic2;
    public String rankPic3;
    public String rankPic4;
    public String rankPic5;
    public String rankPic6;   
    public String rankPic7;  
    public static RankPictureInfo fromGson(JsonElement json){
        Gson gson = new Gson();
        return gson.fromJson(json, new TypeToken<RankPictureInfo>(){}.getType());
    }
}