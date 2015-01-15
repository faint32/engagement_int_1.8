package com.netease.service.protocol.meta;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;


public class MoneyAccountInfo {
    /** 当前现金值 */
    public double money;
    /** 已申请提现的现金值 */
    public double applyMoney;
    /** 累计现金值 */
    public double totalMoney;
    /** 用户魅力值 */
    public long usercp;
    /** 是否可以进行申请提现 */
    public boolean canApply;
    /** 现金过期时间 */
    public long expireTime;  
    /** 如果已经申请提现，未审核则返回“申请提现中”否则空值 */
    public String isApplying;
    /** 最后一次提现年月,格式：yyyyMM */
    public String lastMonth;
    /** 最后一次提现金额 */
    public String lastMoney;
    
    public static MoneyAccountInfo fromGson(JsonElement json){
        Gson gson = new Gson();
        return gson.fromJson(json, new TypeToken<MoneyAccountInfo>(){}.getType());
    }
}
