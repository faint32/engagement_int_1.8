package com.netease.service.protocol.meta;

import java.util.ArrayList;


public class RankListInfo {
    /** 每页的数量 */
    public int count;
    /** 总数 */
    public int totalCount;
    /** 用户列表 */
    public ArrayList<RankUserInfo> userList;
    /** 榜单HeadView的提示语 */
    public String title;
    /** 榜单统计日志名 */
    public String logName;
}
