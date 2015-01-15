package com.netease.service.Utils;

import java.io.ByteArrayOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.text.TextUtils;

import com.netease.common.image.util.ImageUtil;
import com.netease.framework.widget.ToastUtil;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXTextObject;
import com.tencent.mm.sdk.openapi.WXWebpageObject;


public class WeixinShare {
    // Weixin  key
    public static final String WEIXIN_CLIENT_ID = "wx2a96b9f3627c7a27";
    
    private Context mContext;
    // IWXAPI 是第三方app和微信通信的openapi接口
    private IWXAPI mWXApi;
    //注册是否成功
    private boolean bWeiXin;
    
    private static WeixinShare mInstance;
    public static WeixinShare getInstance(){
        if(mInstance == null)
            mInstance = new WeixinShare();
        return mInstance;
    }

    public void registerWeixin(Context context){
        mContext = context;
        mWXApi = WXAPIFactory.createWXAPI(mContext, WEIXIN_CLIENT_ID, true);
        try {
            bWeiXin = mWXApi.registerApp(WEIXIN_CLIENT_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void unregisterWeixin(){
        if(mWXApi != null){
            mWXApi.unregisterApp();
            bWeiXin = false;
        }
    }
    public boolean isRegWeixin(){
        return bWeiXin;
    }
    
    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }
    
    public void share2SessionWebpage(String title, String description, String img, String url,boolean isCircle){
        byte[] data = null;
        Bitmap bmp = null;
        if (!TextUtils.isEmpty(img)) {
            bmp = ImageUtil.getBitmapFromFileLimitSize(img, 80);
        }
        if(bmp != null){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(CompressFormat.PNG, 100, baos);
            data = baos.toByteArray();
        }
        
        share2SessionWebpage(title, description, data, url,isCircle);
    }

    public void share2SessionWebpage(String title, String description, byte[] bmp, String url,boolean isCircle){
        if(!bWeiXin){
            ToastUtil.showToast(mContext, "未安装微信");
            return;
        } 
        
        WXWebpageObject webObj = new WXWebpageObject();
        webObj.webpageUrl = url;
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = webObj;
        msg.description = description;
        msg.title = title;
        //thumbData < 32k
        msg.thumbData = bmp;

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage"); // transaction字段用于唯一标识一个请求
        req.message = msg;
        req.scene = isCircle ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        mWXApi.sendReq(req);
    }
    public void share2SessionText(String description,boolean isCircle){
        if(!bWeiXin){
            ToastUtil.showToast(mContext, "未安装微信");
            return;
        } 
        WXTextObject textObj = new WXTextObject();
        textObj.text = description;
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        // 发送文本类型的消息时，title字段不起作用
        // msg.title = "Will be ignored";
        msg.description = description;

        // 构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("text"); // transaction字段用于唯一标识一个请求
        req.message = msg;
        
        req.scene = isCircle ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        mWXApi.sendReq(req);
    }
}
