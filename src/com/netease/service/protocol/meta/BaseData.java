package com.netease.service.protocol.meta;

import com.google.gson.JsonElement;

/**
 * 服务器返回的基础结构
 *@author echo_chen
 *@since  2014-04-03
 */
public class BaseData {
    public JsonElement data;
    public int code;
    public String message;
}
