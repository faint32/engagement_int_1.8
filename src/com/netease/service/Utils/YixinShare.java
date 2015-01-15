package com.netease.service.Utils;

import com.netease.framework.widget.ToastUtil;

import im.yixin.sdk.api.IYXAPI;
import im.yixin.sdk.api.SendMessageToYX;
import im.yixin.sdk.api.YXAPIFactory;
import im.yixin.sdk.api.YXMessage;
import im.yixin.sdk.api.YXTextMessageData;
import im.yixin.sdk.api.YXWebPageMessageData;
import android.content.Context;
import android.widget.Toast;

public class YixinShare {
    
    // YiXin key
    public static final String YIXIN_CLIENT_ID = "yx308d11b092f74c308167f40e0aea5ded";
    
    private Context mContext;
    // IYXAPI 是第三方app和易信通信的openapi接口
    private IYXAPI mYXApi;
    //注册是否成功
    private boolean bYiXin;
    
    private static YixinShare mInstance;
    public static YixinShare getInstance(){
        if(mInstance == null)
            mInstance = new YixinShare();
        return mInstance;
    }

    public void registerYixin(Context context){
        mContext = context;
        mYXApi = YXAPIFactory.createYXAPI(mContext, YIXIN_CLIENT_ID);
        bYiXin = mYXApi.registerApp();
    }
    
    public void unregisterYixin(){
        if(mYXApi != null){
            mYXApi.unRegisterApp();
            bYiXin = false;
        }
    }
    
    public boolean isRegYixin(){
        return bYiXin;
    }
    
    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }
   
    public void share2YixinWebpage(String title, String description, byte[] bmp, String url, boolean timeline){
        if(!bYiXin){
            ToastUtil.showToast(mContext, "未安装易信");
            return;
        } 
        
        YXWebPageMessageData webpageObj = new YXWebPageMessageData();
        webpageObj.webPageUrl = url;
        
        YXMessage msg = new YXMessage();
        msg.messageData = webpageObj;
        msg.title = title;
        msg.description = description;
        msg.thumbData = bmp;
        
        SendMessageToYX.Req req = new SendMessageToYX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        req.scene = timeline ? SendMessageToYX.Req.YXSceneTimeline
                : SendMessageToYX.Req.YXSceneSession;

        mYXApi.sendRequest(req);
    }
 
    public void share2YixinText( String description, boolean timeline){
        if(!bYiXin){
            Toast.makeText(mContext, "未安装易信",Toast.LENGTH_SHORT).show();
            return;
        }
        YXTextMessageData textObj = new YXTextMessageData();
        textObj.text = description;

        YXMessage msg = new YXMessage();
        msg.messageData = textObj;
        // 发送文本类型的消息时，title字段不起作用
        // msg.title = "title is ignored";
        msg.description = description;

        SendMessageToYX.Req req = new SendMessageToYX.Req();
        // transaction字段用于唯一标识一个请求
        req.transaction = buildTransaction("text"); 
        req.message = msg;
        req.scene = timeline ? SendMessageToYX.Req.YXSceneTimeline
                : SendMessageToYX.Req.YXSceneSession;

        mYXApi.sendRequest(req);
    }
    /**
     * 判断易信是否可用 
     */
    public boolean checkYX() {
        return bYiXin && mYXApi.isYXAppInstalled();
    }
}
   