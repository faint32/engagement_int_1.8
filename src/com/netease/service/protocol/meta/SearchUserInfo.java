package com.netease.service.protocol.meta;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * 搜索结果列表里的用户数据结构
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class SearchUserInfo {
    public long uid;//用户ID
    public int sex;//性别 0:女 1:男
    public int age;//年龄
    public int height;//身高
    public long crownId;//当前戴的皇冠的id
    public String crownUrl;//皇冠图片链接
    public int level;//当前等级
    public String levelName;//等级对应封号
    public long usercp;//当前魅力值（豪气值），不会减少
    public String nick;//用户昵称
    public boolean isVip;//是否vip
    public String portraitUrl;//用户头像原图url
    public String portraitUrl192;//用户192x192头像裁剪图url
    public String portraitUrl640;//用户640x480头像裁剪图url
    public long privatePhotoCount;//私照数(女)
    public int status;//用户状态：0 正常 1冻结
    public boolean isNew;//是否新用户
    public int visitTimes;
    public int bust;
    public int cup;
    public int waist;
    public int hip;

    /**
     * 从JsonElement中解出数据数组
     * @param json
     */
    public static SearchUserInfo[] fromJson(JsonElement json){
        if(json == null)
            return null;
        
        Gson gson = new Gson();
        return gson.fromJson(json, new TypeToken<SearchUserInfo[]>(){}.getType());
    }
}
