package com.netease.service.transactions;


import com.google.gson.JsonElement;
import com.netease.common.http.THttpRequest;
import com.netease.engagement.app.EngagementApp;
import com.netease.engagement.pushMsg.MessagePushUtil;
import com.netease.service.app.BaseApplication;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.DebugData;
import com.netease.service.protocol.meta.LoginUserInfo;
import com.netease.service.protocol.meta.UserInfo;
import com.netease.service.stat.EgmStatService;


/**
 * 登录到URS后，需要上传地理位置并从同城约会服务器获取用户数据，因为登录URS无法返回用户数据。
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class LoginGetUserInfoTransaction extends EgmBaseTransaction {
    private static final boolean DEBUG = EgmProtocolConstants.RANDOM_DEBUG_DATA;
    
    private String mUserName, mPassword, mToken;
    private int mAccountType;
    
    private String latitude, longtitude;
    private String provinceCode, cityCode, districtCode;

    public LoginGetUserInfoTransaction(String userName, String password, String token, int accountType,
            String latitude, String longtitude, String provinceCode, String cityCode, String districtCode) {
        
        super(TRANSACTION_TYPE_LOGIN_GET_USER_INFO);
        
        this.mUserName = userName;
        this.mPassword = password;
        this.mToken = token;
        this.mAccountType = accountType;
        
        this.latitude = latitude;
        this.longtitude = longtitude;
        this.provinceCode = provinceCode;
        this.cityCode = cityCode;
        this.districtCode = districtCode;
    }

    @Override
    public void onTransact() {
        if (DEBUG) {
            UserInfo.generateTestUserInfo();
            setTestFileName(DebugData.FILENAME_LOGIN_USERINFO_JSON);
        }
        
        THttpRequest request = EgmProtocol.getInstance().createLoginGetUserInfoRequest(latitude, longtitude, 
                provinceCode, cityCode, districtCode);
        sendRequest(request);
    }

    @Override
    public void onEgmTransactionSuccess(int code, Object obj){
        super.onEgmTransactionSuccess(code, obj);
        
        LoginUserInfo loginUserInfo = null;
        
        if(obj != null && obj instanceof JsonElement){
            loginUserInfo = LoginUserInfo.fromJson((JsonElement)obj);
        }
        
        if(loginUserInfo != null){
            updateUserInfo(loginUserInfo);
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, loginUserInfo);
        }
        else{
            notifyDataParseError();
        }
        MessagePushUtil.bindAccount(BaseApplication.getAppInstance());  // 绑定消息推送帐号
        
    }
    @Override
    protected void onEgmTransactionError(int errCode, Object obj) {
        super.onEgmTransactionError(errCode, obj);
        MessagePushUtil.bindAccount(BaseApplication.getAppInstance());  // 绑定消息推送帐号,未登录过会return
    }
    
    /** 更新数据库里的用户信息 */
    private void updateUserInfo(LoginUserInfo info){
        if(info == null)
            return;
        
        EgmPrefHelper.putNonce(EngagementApp.getAppInstance(), info.nonce);
        EgmPrefHelper.putExpire(EngagementApp.getAppInstance(), info.expire);
        EgmPrefHelper.putSignature(EngagementApp.getAppInstance(), info.signature);
        
        ManagerAccount manager = ManagerAccount.getInstance();
        manager.setLoginAccount(info.userInfo, mUserName, mPassword, mToken, mAccountType);
        
        EgmStatService.init(info.userInfo.uid);
    }
}
