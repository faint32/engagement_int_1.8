
package com.netease.engagement.dataMgr;

import java.util.HashMap;
import java.util.Map;

import com.netease.service.protocol.meta.UserInfo;


/***
 * 内存数据存放位置，存放app的生命周期内都需要保存的数据或不想放在界面管理的数据
 * 
 * @author echo
 * @since 2014-04-21
 */

public class MemoryDataCenter {

    private static MemoryDataCenter mdc = null;
    private Map<String, Object> data = new HashMap<String, Object>();
    public static final String USERINFO_KEY = "MemoryDataCenter.UserInfo";
    public static final String CURRENT_CHAT_UID = "current_chat_userid";
    
    public static final String CURRENT_COMPARE_CROWNID = "current_compare_crownid" ;
    public static final String CURRENT_CHAT_OTHER_PROFILE = "current_chat_other_profile" ;
    public static final String CURRENT_CAHT_OTHER_NICK = "current_chat_other_nick" ;
    
//    public static final String SPECIALGIFTS = "specialgifts" ;
    
    public static synchronized MemoryDataCenter getInstance() {
        if (mdc == null) {
            mdc = new MemoryDataCenter();
        }
        return mdc;
    }

    public void put(String key, Object value) {
        data.put(key, value);
    }

    public void remove(String key) {
        data.remove(key);
    }

    public void clear() {
        data.clear();
    }

    public Object get(String key) {
        return data.get(key);
    }

    public void putCurUserInfo(UserInfo userInfo) {
        data.put(USERINFO_KEY, userInfo);
    }

    public UserInfo getCurUserInfo() {
        return (UserInfo)data.get(USERINFO_KEY);
    }
}
