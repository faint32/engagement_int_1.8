package com.netease.service.Utils;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.netease.date.R;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.util.PlatformUtil;

/**
 * 这里封装了登录相关的业务逻辑。因为涉及到URS的业务逻辑是不透明的，所以在这里做好封装。
 * <p>先初始化URS，获取该手机上的该版本应用在URS上的id，即ursId。这个只要获取一次，存在客户端，供后续使用；
 * <br>然后登录URS，登录成功后能获取到token；
 * <br>调用同城服务器上所有的接口，必须带上这个token才有效
 * 
 * <br>注意界面结束后要调用removeCallback方法移除callback
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class LoginHelper {
    private Context mContext;
    private String mUserName;
    private String mPassword;
    private String mToken;
    
    private int mInitTid;
    private int mLoginTid;
    
    /** 是否需要在初始化完成后直接进行登录 */
    private boolean mIsGoOnLogin = false;
    
    private ILoginSuccess mLoginSuccessListener;
    private ILoginError mLoginErrorListener;
    
    private int mType;

    /**
     * @param context
     * @param s 登录成功的回调
     * @param e 登录失败的回调
     * @param isGoOnLogin 是否初始化成功后直接进行登录
     */
    public LoginHelper(Context context, ILoginSuccess s, ILoginError e, boolean isGoOnLogin){
        mContext = context;
        EgmService.getInstance().addListener(mEgmCallback);
        
        mLoginSuccessListener = s;
        mLoginErrorListener = e;
        
        mIsGoOnLogin = isGoOnLogin;
    }
    
    /** 不再使用该Helper后必须主动调用该方法，释放EgmCallback */
    public void removeCallback(){
        EgmService.getInstance().removeListener(mEgmCallback);
        mEgmCallback = null;
    }
    
    public void login(String name, String password){
        mUserName = name;
        mPassword = password;
        mLoginTid = EgmService.getInstance().doLoginURS(name, password);
    }
    
    public String getToken(){
        return mToken;
    }
    
    private EgmCallBack mEgmCallback = new EgmCallBack(){   
        /** 登录URS成功 */
        @Override
        public void onLoginURS(int transactionId, String token){
            if(transactionId != mLoginTid)
                return;
            
            mToken = token;
            
            if(!TextUtils.isEmpty(token)){
                if(mLoginSuccessListener != null){
                    mLoginSuccessListener.onLoginSuccess();
                }
            }
            else{
                if(mLoginErrorListener != null){
                    mLoginErrorListener.onLoginError(EgmServiceCode.TRANSACTION_COMMON_PASSWORD_ERROR, 
                            mContext.getString(R.string.reg_tip_account_error));
                }
            }
        }
        @Override
        public void onLoginURSError(int transactionId, int errCode, String err){
            if(transactionId != mLoginTid)
                return;
            
            if(mLoginErrorListener != null){
                mLoginErrorListener.onLoginError(errCode, err);
            }
        }
    };
    
    public interface ILoginSuccess{
        public void onLoginSuccess();
    };
    public interface ILoginError{
        public void onLoginError(int errCode, String errStr);
    };
}
