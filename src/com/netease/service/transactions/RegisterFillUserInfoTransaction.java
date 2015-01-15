package com.netease.service.transactions;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.DebugData;
import com.netease.service.protocol.meta.UserInfo;
import com.netease.service.stat.EgmStat;
import com.netease.service.stat.EgmStatService;


/**
 * 手机号注册——补全用户资料
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class RegisterFillUserInfoTransaction extends EgmBaseTransaction {
    private final boolean DEBUG = EgmProtocolConstants.RANDOM_DEBUG_DATA;
    
    private String mUserName, mPassword, mToken;
    private int mAccountType;
    
    private int sexType;
    private String nick;
    private String avatarPath;
    private String x, y, w, h;
    private long birthday;
    private String inviteCode;
    private String latitude, longtitude;
    private String province, city, district;

    public RegisterFillUserInfoTransaction(String userName, String password, String token, int accountType,
            int sexType, String nick, String avatarPath, String x, String y, String w, String h, 
            long birthday, String inviteCode, String latitude, String longtitude, String province, String city ,String district) {
        super(TRANSACTION_TYPE_FILL_USER_INFO);
        
        this.mUserName = userName;
        this.mPassword = password;
        this.mToken = token;
        this.mAccountType = accountType;
        
        this.sexType = sexType;
        this.nick = nick;
        this.avatarPath = avatarPath;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.birthday = birthday;
        this.inviteCode = inviteCode;
        this.latitude = latitude;
        this.longtitude = longtitude;
        this.province = province;
        this.city = city;
        this.district = district;
    }

    @Override
    public void onTransact() {
        if(DEBUG){
            UserInfo.generateTestUserInfo();
            setTestFileName(DebugData.FILENAME_USERINFO_JSON);
        }
        
        THttpRequest request = EgmProtocol.getInstance().createFillUserInfoRequest(sexType, nick, avatarPath, 
                x, y, w, h, birthday, inviteCode, latitude, longtitude, province, city, district);
        sendRequest(request);
    }

    @Override
    public void onEgmTransactionSuccess(int code, Object obj){
        UserInfo userInfo = null;
        
        if (obj != null && obj instanceof JsonElement) {
            Gson gson = new Gson();
            JsonElement userJson = ((JsonElement)obj).getAsJsonObject().get("userInfo");
            userInfo = gson.fromJson(userJson, UserInfo.class);
        }
        
        if (userInfo != null) {
            saveUserInfo(userInfo);
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, userInfo);
        } 
        else {
            notifyDataParseError();
        }
    }
    
    /** 把用户信息保存到数据库 */
    private void saveUserInfo(UserInfo info){
        if(info == null)
            return;
        
        ManagerAccount manager = ManagerAccount.getInstance();
        manager.setLoginAccount(info, mUserName, mPassword, mToken, mAccountType);
        
        EgmStatService.init(info.uid);
    }
}
