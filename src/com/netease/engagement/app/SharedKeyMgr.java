
package com.netease.engagement.app;

import com.netease.common.share.sina.ShareChannelSina;

public class SharedKeyMgr {
    
    // sina
    public static final String SINA_CLIENT_ID = "876878023";
    public static final String SINA_CLIENT_SECRET = "0a92efeb5cc86b32c04bcca6fad5b8ab";
    public static final String SINA_REDIRECT_URI = "https://api.weibo.com/oauth2/default.html";
   
    public static void initSharedKey() {
        initSina();
    }
    private static void initSina() {
        ShareChannelSina.CLIENT_ID = SINA_CLIENT_ID;
        ShareChannelSina.CLIENT_SECRET = SINA_CLIENT_SECRET;
        ShareChannelSina.REDIRECT_URI = SINA_REDIRECT_URI;
    }
   
}
