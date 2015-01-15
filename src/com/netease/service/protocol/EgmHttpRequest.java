
package com.netease.service.protocol;


import com.netease.common.http.THttpMethod;
import com.netease.common.http.THttpRequest;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.app.BaseApplication;
import com.netease.util.PlatformUtil;

public class EgmHttpRequest extends THttpRequest {
    public static String APP_OS_NAME  = "androidPhone";
    public static final String USERAGENT =  "date/"
            + EgmUtil.getNumberVersion(BaseApplication.getAppInstance()) + " ("
            + APP_OS_NAME + "; " + android.os.Build.VERSION.RELEASE + "; " + android.os.Build.MODEL+ "; "
            + PlatformUtil.getResolution(BaseApplication.getAppInstance())+ "; "
            + EgmUtil.getAppChannelID(BaseApplication.getAppInstance()) + ") ";

    public EgmHttpRequest(String url) {
        super(url);
        init();

    }

    public EgmHttpRequest(String url, THttpMethod type) {
        super(url, type);
        init();
    }

    public void init() {
        addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        addHeader("Accept-Encoding", "gzip, deflate");
        addHeader("PLATFORM", String.valueOf(EgmProtocolConstants.PLATFORM_ANROID));
        addHeader("APPVERSION",EgmUtil.getNumberVersion(BaseApplication.getAppInstance()));
        addHeader("TOKEN",EgmProtocol.getInstance().getUrsToken());
        addHeader("DEVICEID",EgmProtocol.getInstance().getUrsId());
        addHeader("user-agent",USERAGENT);
        addHeader("CHANNEL",EgmUtil.getAppChannelID(BaseApplication.getAppInstance()));
        addHeader("PHONETYPE",android.os.Build.MODEL);
        addHeader("MEID",PlatformUtil.getDeviceID(BaseApplication.getAppInstance()));
    }

    public void addParametersInt(String key ,int[] params){
        for(int value : params) {
            addParameter(key, String.valueOf(value));
        }
    }
    
    public void addParametersLong(String key ,long[] params){
        for(long value : params) {
            addParameter(key, String.valueOf(value));
        }
    }
    public void addParametersString(String key ,String[] params){
        for(String value : params) {
            addParameter(key, value);
        }
    }
    
   
}
