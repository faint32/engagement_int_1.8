package com.netease.service.protocol.meta;

import java.util.ArrayList;


public class RecommendListInfo {
    /** 每页数量 */
    public boolean hasPortrait;
    public int portraitStatus; //用户头像状态：0 未上传 1 待审核 2审核成功 3审核失败
    public String portraitTips;//头像状态信息描述
    public ArrayList<RecommendUserInfo> list;
}
