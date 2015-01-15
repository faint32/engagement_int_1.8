package com.netease.service.protocol.meta;

public class VersionInfo {
    public boolean hasNew;//是否有新版本
    public String version;//版本号（如果有新版本，返回）
    public String description;//升级描述（如果有新版本，返回）
    public String downUrl;//新版本下载地址（如果有新版本，返回）
    public boolean forceUpdate;//是否需要强制升级
}
