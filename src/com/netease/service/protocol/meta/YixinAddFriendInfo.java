package com.netease.service.protocol.meta;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;


public class YixinAddFriendInfo {
    /** AES加密key， 用于注册易信时进行加密 */
    public String AESKey;
    /** 手机号码，即易信号 */
    public String mobile;
    
    public static YixinAddFriendInfo fromGson(JsonElement json){
        Gson gson = new Gson();
        return gson.fromJson(json, new TypeToken<YixinAddFriendInfo>(){}.getType());
    }
}
