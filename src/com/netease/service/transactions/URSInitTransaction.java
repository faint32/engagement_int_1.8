package com.netease.service.transactions;

import java.util.StringTokenizer;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.netease.common.http.THttpRequest;
import com.netease.common.task.example.StringAsyncTransaction;
import com.netease.service.Utils.DeviceUtil;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.app.BaseApplication;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.util.PlatformUtil;

/**
 * 初始化URS
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class URSInitTransaction extends StringAsyncTransaction {

    public URSInitTransaction() {
        super(EgmBaseTransaction.TRANSACTION_TYPE_INIT_URS);
    }

    @Override
    public void onTransact() {
    		Context context = BaseApplication.getAppInstance();
        THttpRequest request = EgmProtocol.getInstance().createInitURSRequest(EgmProtocolConstants.PRODUCT, 
        		EgmUtil.getVersionStr(context), 
        		PlatformUtil.getDeviceID(context), 
        		Build.MODEL, 
        		EgmProtocolConstants.SYSTEM_NAME, 
        		Build.VERSION.RELEASE, 
        		DeviceUtil.getScreenWidth(context) + "*"
						+ DeviceUtil.getScreenHeight(context));
        sendRequest(request);
    }

    @Override
    protected void onTransactionSuccess(int code, Object obj) {
        String[] ursInitResults = null;
        
        if(obj != null && obj instanceof String){
            ursInitResults = dealUrsInitResult((String)obj);
        }
        
        if (ursInitResults != null && ursInitResults.length == 2) {
            String ursId = ursInitResults[0];
            String ursKey = ursInitResults[1];
            
            // 记录
            EgmPrefHelper.putURSId(BaseApplication.getAppInstance(), ursId);
            EgmPrefHelper.putURSKey(BaseApplication.getAppInstance(), ursKey);
            notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS, ursInitResults);
        } 
        else {
            // 清除 
            EgmPrefHelper.putURSId(BaseApplication.getAppInstance(), "");
            EgmPrefHelper.putURSKey(BaseApplication.getAppInstance(), "");
            
            notifyError(EgmServiceCode.TRANSACTION_COMMON_SERVER_ERROR, "");
        }
    }

    @Override
    protected void onTransactionError(int errCode, Object obj) {
        notifyError(errCode, obj);
    }

    /**
     * 解析URS初始化结果
     * @param str 服务器返回的结果字符串
     * @return [0]:urs_id; [1]:urs_key
     */
    private String[] dealUrsInitResult(String str) {
        String id = "";
        String key = "";
        
        StringTokenizer st = new StringTokenizer(str, "\n", false);
        while (st.hasMoreElements()) {
            String ss = st.nextToken();
            if(ss.contains("=")) {
                StringTokenizer st1 = new StringTokenizer(ss, "&", false);
                while (st1.hasMoreElements()) {
                    String sss = st1.nextToken();
                    if(sss.contains("=")) {
                        StringTokenizer st2 = new StringTokenizer(sss, "=", false);
                        if(st2.countTokens() == 2) {
                            String k = st2.nextToken();
                            String v = st2.nextToken();
                            if("id".equalsIgnoreCase(k)) {
                                id = v;
                            } 
                            else if("key".equalsIgnoreCase(k)) {
                                key = v;
                            }
                        }
                    }
                }
            }
        }
        
        String[] results = null;
        if(!TextUtils.isEmpty(id) && !TextUtils.isEmpty(key)) {
            results = new String[]{id, key};
        } 
        
        return results;
    }
}

