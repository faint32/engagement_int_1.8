
package com.netease.engagement.activity;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.gson.Gson;
import com.netease.common.share.ShareToThirdPartUtils;
import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentEditPhoto.PicUpLoadMode;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.engagement.widget.CustomWebView;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.DebugData.BaseDataTest;
import com.netease.service.protocol.meta.PicUpLoadInfo;
import com.netease.service.protocol.meta.PictureInfo;
import com.netease.service.protocol.meta.UserInfo;

/**
 * @author lishang 晒照片：实现功能 
 * 1、WebView中照片选择上传 
 * 2、分享到微信、微博、易信 
 * 3、跳转到女性资料页面
 */
public class ActivityPicShowOffForFemale extends ActivityEngagementBase {

    private final static String GO_TO_PERSON_INFO = "neteasedate://callback_user_page";
    private final static String GO_TO_PHOTO_SHARE = "neteasedate://callback_photoshow_share";
    private final static String PHOTO_UPLOAD = "neteasedate://callback_upload_photo_page";
    private final static String GO_TO_RANK_PAGE = "neteasedate://callback_rank_page";
    private final static String RELOAD_USERINFO_PAGE = "neteasedate://reload_userinfo_page";
    private final static String CLOSE_WEBVIEW_PAGE = "neteasedate://close_webview_page";
    private final static String DES_URL = "des_url";
    private final static String ACTIVITY_TYPE = "activity_type";
    

    /** 分享组件 */
    private ShareToThirdPartUtils mShareUtils;
    
    private WebView         mWebView;
    private CustomWebView   mCustomWebView;
    private UserInfo mUserInfo;

    /** 获取Uid Transaction的id */
    private int Tid;
    /** 活动类型 */
    private int activityType;
    /** 目标URL */
    private String desUrl;
    /** 置换token*/
    private int mExchangeTid;

    public interface ActivityType {

        int PIC_SHOWOFF_WITH_PUBLIC = 0;
        int PIC_SHOWOFF_WITH_PRIVATE = 1;
        int OTHERS = 2;

    }

    public static void startActivity(Context context, Bundle values) {
        Intent intent = new Intent(context, ActivityPicShowOffForFemale.class);
        intent.putExtra(DES_URL, values.getString(DES_URL));
        intent.putExtra(ACTIVITY_TYPE, ActivityType.PIC_SHOWOFF_WITH_PUBLIC);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent mIntent = getIntent();
        if (mIntent == null)
            return;

        mCustomWebView = new CustomWebView(this);
        super.setCustomActionBar();
        setUpActionBar();
        setContentView(mCustomWebView);

        mWebView = mCustomWebView.getWebView();
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setVerticalScrollBarEnabled(false);
        
        mWebView.setWebViewClient(new MyWebViewClient(this));
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);

                CustomActionBar customActionBar = getCustomActionBar();
                customActionBar.setMiddleTitle(title);
            }
        });
        
        
        desUrl=mIntent.getStringExtra(DES_URL);
        activityType = mIntent.getIntExtra(ACTIVITY_TYPE, 0);
        
        mShareUtils = ShareToThirdPartUtils.getInstance(this);
        EgmService.getInstance().addListener(mCallBack);

        Tid = EgmService.getInstance().doGetPrivateData();
        doExchangeTicket();
        
    }
    
    private Map<String, String> upLoadActionMap;
    
    private class MyWebViewClient extends WebViewClient {

        private Context mContext;

        public MyWebViewClient(Context context) {
            super();
            mContext = context;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mCustomWebView.setLoadingState();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mCustomWebView.setLoadedState();
        }

        /* *监听特定类型的Url：分享、跳转、上传*/
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // 跳转到个人资料页
            if (!TextUtils.isEmpty(url) && url.contains(GO_TO_PERSON_INFO)) {

                Map<String, String> map = parseUrlParams(url);
                if (map != null) {
                    String uid = map.get("uid");
                    String sex = map.get("sex");
                    if (!TextUtils.isEmpty(uid) && !TextUtils.isEmpty(sex)) {
                        ActivityUserPage.startActivity(ActivityPicShowOffForFemale.this, uid, sex);
                    } else {
                        Toast.makeText(ActivityPicShowOffForFemale.this, "" + url, 1).show();
                    }
                }
                return true;
            }

            // 分享逻辑
            if (!TextUtils.isEmpty(url) && url.contains(GO_TO_PHOTO_SHARE)) {

                Map<String, String> map = parseUrlParams(url);
                if (map != null) {
                    try {
                        JSONObject mJsonObject = new JSONObject(map.get("data"));
                        if (mJsonObject != null) {

                            int type = mJsonObject.optInt("type");
                            int subtype = mJsonObject.optInt("subtype");
                            String content = mJsonObject.optString("content");
                            String url_des = mJsonObject.optString("url");

                            boolean circle =( subtype == 1);
                            mShareUtils.shareToThirdPartWithUrl(type, "", content,
                                    R.drawable.icon_app, url_des, circle);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            }
            // 照片上传逻辑，参看actonId，区分公照、私照
            if (!TextUtils.isEmpty(url) && url.contains(PHOTO_UPLOAD)) {

                upLoadActionMap = parseUrlParams(url);
                if (upLoadActionMap != null) {
                    String type = upLoadActionMap.get("type");
                    String actionId = upLoadActionMap.get("actionId");
                    if (!TextUtils.isEmpty(type) && !TextUtils.isEmpty(actionId)) {

                        switch (Integer.parseInt(type)) {
                            case ActivityType.PIC_SHOWOFF_WITH_PUBLIC:
                                ActivityLevelTable.startActivityForPicShowOff(
                                        ActivityPicShowOffForFemale.this,
                                        ActivityLevelTable.FRAGMENT_PUBLIC_PHOTO,
                                        UserInfo.toJsonString(mUserInfo),
                                        PicUpLoadMode.PIC_SHOWOFF_MODE);
                                break;
                            case ActivityType.PIC_SHOWOFF_WITH_PRIVATE:
                                ActivityLevelTable.startActivityForPicShowOff(
                                        ActivityPicShowOffForFemale.this,
                                        ActivityLevelTable.FRAGMENT_PRIVATE_PHOTO,
                                        UserInfo.toJsonString(mUserInfo),
                                        PicUpLoadMode.PIC_PRIVATE_MIRROR_MODE);
                                break;   
                            default:
                                break;
                        }
                    } else {
                        Toast.makeText(ActivityPicShowOffForFemale.this, "" + url, 1).show();
                    }

                }
                return true;
            }
            
            // 榜单跳转逻辑
            if (!TextUtils.isEmpty(url) && url.contains(GO_TO_RANK_PAGE)) {

                Map<String, String> map = parseUrlParams(url);
                if (map != null) { 
                    
                    String rankId=map.get("rankId");
                    String type=map.get("type");
                    String rankName=map.get("rankName");
                    int id=Integer.parseInt(rankId);
                    goToRankPage(id);
                }
                return true;
            }   
            
            // 个人资料页刷新 （vip等）
            if (!TextUtils.isEmpty(url) && url.contains(RELOAD_USERINFO_PAGE)) {
                EgmService.getInstance().doGetPrivateData();
                return true;
            }
            
            // 关闭webview
            if (!TextUtils.isEmpty(url) && url.contains(CLOSE_WEBVIEW_PAGE)) {
                finish();
                return true;
            }
            
            return super.shouldOverrideUrlLoading(view, url);
        }

    }

    /**
     * @param rankId
     * 排行榜跳转
     */
    private void goToRankPage(int rankId) {
        
        switch (rankId) {
            case EgmConstants.RankID.HOT:
                ActivityMultiRankList.startActivity(ActivityPicShowOffForFemale.this,
                        EgmConstants.RankID.HOT, getResources().getString(R.string.rank_name_hot),
                        EgmConstants.SexType.Female);
                break;
            case EgmConstants.RankID.NEW_FEMALE:
                ActivitySingleRankList.startActivity(ActivityPicShowOffForFemale.this, 
                        EgmConstants.RankID.NEW_FEMALE,
                        getResources().getString(R.string.rank_name_new_female), 
                        EgmConstants.SexType.Female);
                break;
            case EgmConstants.RankID.NEW_MALE:
                ActivitySingleRankList.startActivity(ActivityPicShowOffForFemale.this, 
                        EgmConstants.RankID.NEW_MALE,
                        getResources().getString(R.string.rank_name_new_male), 
                        EgmConstants.SexType.Male);
                break;
            case EgmConstants.RankID.PRIVATE_PIC_FEMALE:
                ActivityMultiRankList.startActivity(ActivityPicShowOffForFemale.this,
                        EgmConstants.RankID.PRIVATE_PIC_FEMALE,
                        getResources().getString(R.string.rank_name_private_pic_female),
                        EgmConstants.SexType.Female);
                break;
            case EgmConstants.RankID.STAR:
                ActivityMultiRankList.startActivity(ActivityPicShowOffForFemale.this,
                        EgmConstants.RankID.STAR,
                        getResources().getString(R.string.rank_name_star), 
                        EgmConstants.SexType.Female);
                break;
            case EgmConstants.RankID.STRENGTH_MALE:
                ActivitySingleRankList.startActivity(ActivityPicShowOffForFemale.this, 
                        EgmConstants.RankID.STRENGTH_MALE,
                        getResources().getString(R.string.rank_name_strength_male), 
                        EgmConstants.SexType.Male);

                break;
            case EgmConstants.RankID.TOP_FEMALE:
                if (ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Male
                        && ManagerAccount.getInstance().isVip()) {
                    ActivityMultiRankList.startActivity(ActivityPicShowOffForFemale.this,
                            EgmConstants.RankID.TOP_FEMALE,
                            getResources().getString(R.string.rank_name_top_female), 
                            EgmConstants.SexType.Female);
                }
                break;
            case EgmConstants.RankID.TOP_MALE:
                ActivityMultiRankList.startActivity(ActivityPicShowOffForFemale.this,
                        EgmConstants.RankID.TOP_MALE,
                        getResources().getString(R.string.rank_name_top_male), 
                        EgmConstants.SexType.Male);
                break;
            default:
                break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (mShareUtils.getSsoHandler() != null) {
            mShareUtils.getSsoHandler().authorizeCallBack(requestCode, resultCode, intent);
        }
        super.onActivityResult(requestCode, resultCode, intent);

    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EgmService.getInstance().removeListener(mCallBack);
        mWebView.setVisibility(View.GONE);
        
        //每次关闭的时候刷新一次，防止中途未刷新就关闭web
        EgmService.getInstance().doGetPrivateData();
    }

    /*
     * 防止Error WebView.destroy() called while still attached
     */
    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        
        mWebView.removeAllViews();
        mWebView.destroy();
    }

    @Override
    public void refreshContent() {

    }

    @Override
    protected void requestFeature() {

    }

    public void setUpActionBar() {

        CustomActionBar customActionBar = getCustomActionBar();
        customActionBar.getCustomView().setBackgroundColor(
                getResources().getColor(R.color.pri_info_choice_title_color));
        customActionBar.setLeftBackgroundResource(R.drawable.titlebar_a_selector);
        customActionBar.setLeftTitleColor(getResources().getColor(R.color.purple_dark));
        customActionBar.setLeftAction(R.drawable.bar_btn_back_a, R.string.back);
        customActionBar.setMiddleTitleColor(getResources().getColor(R.color.black));
        customActionBar.setMiddleTitle(getResources().getString(R.string.pic_showoff_title));
        customActionBar.setMiddleTitleSize(20);
        customActionBar.hideRightTitle();
        customActionBar.setLeftClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mWebView.canGoBack()) {
                    mWebView.goBack();// 返回前一个页面
                } else {
                    finish();
                }
            }
        });
    }

    public static Map<String, String> parseUrlParams(String encodeData) {

        if (TextUtils.isEmpty(encodeData))
            return null;

        String urlFormat = encodeData.replace("?", "&");
        Map<String, String> map = new HashMap<String, String>();
        StringTokenizer tokenizer = new StringTokenizer(urlFormat, "&");

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            String value = null;

            int index = token.indexOf('=');
            if (index > 0) {
                value = token.substring(index + 1);
                token = token.substring(0, index);

                map.put(URLDecoder.decode(token), URLDecoder.decode(value));
            }
        }
        return map;
    }

    EgmCallBack mCallBack = new EgmCallBack() {

        public void onGetPrivateDataSucess(int transactionId,
                com.netease.service.protocol.meta.UserPrivateData obj) {

            if (Tid != transactionId)
                return;
            mUserInfo = obj.userInfo;
        }

        public void onGetPrivateDataError(int transactionId, int errCode, String err) {

            Toast.makeText(ActivityPicShowOffForFemale.this, err, 1000).show();
        };

        @Override
        public void onUploadPicSucess(int transactionId, PictureInfo obj) {
            Gson gson = new Gson();
            BaseDataTest baseData = new BaseDataTest();
            baseData.code = 0;
            baseData.message = "success";
            switch (activityType) {
                case ActivityType.PIC_SHOWOFF_WITH_PUBLIC:
                case ActivityType.PIC_SHOWOFF_WITH_PRIVATE:
                    PicUpLoadInfo mInfo = new PicUpLoadInfo();
                    mInfo.photoId = obj.id;
                    baseData.data = mInfo;
                    String callBackjson = gson.toJson(baseData);
                    mWebView.loadUrl("javascript:YH.appCallback('"
                            + upLoadActionMap.get("actionId") +"','" + callBackjson + "')");
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onUploadPicError(int transactionId, int errCode, String err) {
            Gson gson = new Gson();
            BaseDataTest baseData = new BaseDataTest();
            baseData.code = errCode;
            baseData.message = err;
            Map<String, Object> map = new HashMap<String, Object>();
            switch (activityType) {
                case ActivityType.PIC_SHOWOFF_WITH_PUBLIC:
                case ActivityType.PIC_SHOWOFF_WITH_PRIVATE:
                    baseData.data = "";
                    String callBackjson = gson.toJson(baseData);
                    mWebView.loadUrl("javascript:YH.appCallback('"
                            + upLoadActionMap.get("actionId") +"','" + callBackjson + "')");
                    break;
                default:
                    break;
            }
        }
        
        //登陆晒照片个人页面 验证
        /** 置换ticket */
        @Override
        public void onExchangeTicket(int transactionId, String ticket) {
            if (mExchangeTid != transactionId)
                return;

            stopWaiting();

            String mTicket = ticket;
            String url = getUrl(URLEncoder.encode(desUrl), mTicket);
            if (!TextUtils.isEmpty(url)) {
                mWebView.loadUrl(url);
            }

        }
        @Override
        public void onExchangeTicketError(int transactionId, int errCode, String err){
            if(mExchangeTid != transactionId)
                return;
            
            stopWaiting();
            showToast(err);
        }
        
    };
    /**
     * Toast 提示
     * 
     * @param message
     */
    public void showToast(String message) {
        Toast.makeText(ActivityPicShowOffForFemale.this, message, Toast.LENGTH_SHORT).show();
    }
    
    private String getUrl(String mUrl,String mTicket){
        StringBuilder builder = new StringBuilder();
        builder.append(EgmConstants.URL_URS_TICKET_LOGIN).append("?")
            .append("ticket=").append(mTicket).append("&")
            .append("url=").append(mUrl).append("&")
            .append("product=").append(EgmProtocolConstants.PRODUCT).append("&")
            .append("domains=").append("163.com");
        
        return builder.toString();
    }

    private void doExchangeTicket() {
        showWatting(getString(R.string.common_tip_is_waitting));

        mExchangeTid = EgmService.getInstance().doExchangeTicketFromUrs(
                EgmPrefHelper.getURSId(ActivityPicShowOffForFemale.this),
                EgmPrefHelper.getURSKey(ActivityPicShowOffForFemale.this),
                ManagerAccount.getInstance().getCurrentAccountToken()); 
    }
}
