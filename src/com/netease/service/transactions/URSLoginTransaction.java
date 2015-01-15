package com.netease.service.transactions;

import java.util.StringTokenizer;

import android.text.TextUtils;

import com.netease.common.http.THttpRequest;
import com.netease.common.log.NTLog;
import com.netease.common.task.example.StringAsyncTransaction;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.app.BaseApplication;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.util.EnctryUtil;

/**
 * 登录URS
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class URSLoginTransaction extends EncryptBaseTransaction{
//    private String ursId;
    private String mUrsKey;
    private String name;
    private String password;

    public URSLoginTransaction(String name, String password) {
        super(EgmBaseTransaction.TRANSACTION_TYPE_LOGIN_URS);
        this.name = name;
        this.password = password;
    }

    @Override
    public void realOntransact() {
    	    String ursId = EgmPrefHelper.getURSId(BaseApplication.getAppInstance());
    	    mUrsKey = EgmPrefHelper.getURSKey(BaseApplication.getAppInstance());
        String md5Password = EgmUtil.getMD5(password);
        StringBuffer buffer = new StringBuffer();
        buffer.append("username=").append(name).append("&password=").append(md5Password);
        
        String params = "";
        try {
//            byte[] tkey = EgmUtil.AEStoByte(ursKey);
//            byte[] src = buffer.toString().getBytes("utf-8");
            params = EnctryUtil.encryptForAES(buffer.toString(), mUrsKey);
        } 
        catch (Exception e) {
            
        }
        
        THttpRequest request = EgmProtocol.getInstance().createLoginURSRequest(ursId, params);
        sendRequest(request);
    }
    
    @Override
    protected void onTransactionSuccess(int code, Object obj) {
        if(obj != null && obj instanceof String){
            String result = (String)obj;
            String token = dealUrsLoginResult(result);
            NTLog.i("URSLoginTransaction", "onTransactionSuccess result is " + result);
            if(!TextUtils.isEmpty(token)){  // 成功
//                EgmPrefHelper.putURSToken(BaseApplication.getAppInstance(), token);
                EgmProtocol.getInstance().setUrsToken(token);
                
                notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, token);
            }
            else{
                int errCode = EgmServiceCode.TRANSACTION_COMMON_SERVER_ERROR;
                if(!TextUtils.isEmpty(result)) {
                    if(result.contains("420")) {    // 用户名不存在
                        errCode = EgmServiceCode.TRANSACTION_COMMON_NOT_REGISTER;
                    } 
                    else if(result.contains("460")) {   // 密码错误
                        errCode = EgmServiceCode.TRANSACTION_COMMON_PASSWORD_ERROR;
                    } 
                }
                
                notifyError(errCode, "");
            }
        }
    }

    @Override
    protected void onTransactionError(int errCode, Object obj) {
        notifyError(errCode, obj);
    }
    
    /**
     * 处理URS登陆结果
     * @param str
     * @return URS的token
     */
    private String dealUrsLoginResult(String str) {
        String token = "";
        String encrypted = "";  // 加密过的返回结果
        
        // 从结果字符串中截取加密过的结果
        StringTokenizer st = new StringTokenizer(str, "\n", false);
        while (st.hasMoreElements()) {
            String ss = st.nextToken();
            if(ss.contains("=")) {
                StringTokenizer st1 = new StringTokenizer(ss, "=", false);
                String k = st1.nextToken();
                String v = st1.nextToken();
                if("result".equalsIgnoreCase(k)) {
                    encrypted = v;
                }
            }
        }
        
        // 解密加密过的结果
        String decrypt = "";    // 解密后的结果
        if(!TextUtils.isEmpty(encrypted)) { 
            String key = mUrsKey;
            try {
                byte[]tkey = EgmUtil.AEStoByte(key);
                byte[]src = EgmUtil.AEStoByte(encrypted);
                decrypt = EnctryUtil.decryptForAES(src, tkey);
            } 
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // 从解密后的结果里截取token
        if(!TextUtils.isEmpty(decrypt)) {
            st = new StringTokenizer(decrypt, "&", false);
            while (st.hasMoreElements()) {
                String ss = st.nextToken();
                if(ss.contains("token")) {
                    StringTokenizer st1 = new StringTokenizer(ss, "=", false);
                    String k = st1.nextToken();
                    String v = st1.nextToken();
                    if("token".equalsIgnoreCase(k)) {
                        token = v;
                    }
                }
            }
        }
        
        return token;
    }
}
