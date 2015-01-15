
package com.netease.service.protocol.meta;

import java.util.List;

public class RankListItmeInfoInhome {

    // id   排行榜ID：
    // 0:   新秀榜（女性）
    // 1:   魅力榜（女性）
    // 2:   红人榜（女性）
    // 3:   女神榜（VIP栏目）
    // 4:   新贵榜（男性）
    // 5:   富豪榜（男性）
    // 6:   实力榜（男性）
    // 7:   私照榜（女性）
    // name                 排行榜名称
    // picUrl               底图URL
    // top3Urls String[]    前三名头像url
    public int          id;
    public String       name;
    public String       picUrl;
    public List<String> top3Urls;
    public int          sex;
    public int          rankCount;
    public boolean      needVip;
    public String       logName;
    
}
