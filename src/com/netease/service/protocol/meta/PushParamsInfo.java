package com.netease.service.protocol.meta;

import com.google.gson.Gson;
import com.google.gson.JsonElement;


public class PushParamsInfo {
    /** 随机字符串 */
    public String nonce;
    /** 表示签名过期时间(时间戳) */
    public long expire;
    /** 签名 */
    public String signature;
    
    /**
     * 从JSON字符串中生成数据结构
     * @param jsonStr
     * @return
     */
    public static PushParamsInfo fromJson(JsonElement json){
        if(json == null)
            return null;
        
        Gson gson = new Gson();
        return gson.fromJson(json, PushParamsInfo.class);
    }
}
