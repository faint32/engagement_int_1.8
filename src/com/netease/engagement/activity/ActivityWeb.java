package com.netease.engagement.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.widget.LinearLayout;

import com.netease.date.R;
import com.netease.engagement.dataMgr.ConfigDataManager;
import com.netease.engagement.fragment.FragmentWeb;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.UserInfoConfig;


public class ActivityWeb extends ActivityEngagementBase {
    private final int CONTAINER_ID = R.id.activity_web_container_id;
    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_NEED_LOGIN = "extra_need_login";
    public static final String EXTRA_NEED_TITLE = "extra_need_title";
    
    public static void startActivity(Context context, String url, boolean needTitle){
        Intent intent = new Intent(context, ActivityWeb.class);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_NEED_TITLE, needTitle);
        
        //非activity的context启动activity会出错
        if(!(context instanceof Activity))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        context.startActivity(intent);
    }
    
    /** 升级vip */
    public static void startUpgradeVip(Context context){
        UserInfoConfig userInfoConfig = ConfigDataManager.getInstance().getUConfigFromData();
        if(userInfoConfig == null || TextUtils.isEmpty(userInfoConfig.vipChargeUrl))
            return;
        
        startNeedLoginWeb(context, userInfoConfig.vipChargeUrl, false);
    }
    
    /** 男性充值 */
    public static void startCoinCharge(Context context){
        UserInfoConfig userInfoConfig = ConfigDataManager.getInstance().getUConfigFromData();
        if(userInfoConfig == null || TextUtils.isEmpty(userInfoConfig.coinChargeUrl))
            return;
        
        startNeedLoginWeb(context, userInfoConfig.coinChargeUrl, false);
    }
    
    /** 查看网易宝余额 */
    public static void startCheckBalance(Context context){
        UserInfoConfig userInfoConfig = ConfigDataManager.getInstance().getUConfigFromData();
        if(userInfoConfig == null || TextUtils.isEmpty(userInfoConfig.epayUrl))
            return;
        
        startNeedLoginWeb(context, userInfoConfig.epayUrl, true);
    }
    
    private static void startNeedLoginWeb(Context context, String url, boolean needTitle){
        Intent intent = new Intent(context, ActivityWeb.class);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_NEED_LOGIN, true);
        intent.putExtra(EXTRA_NEED_TITLE, needTitle);
        
        //非activity的context启动activity会出错
        if(!(context instanceof Activity))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        context.startActivity(intent);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.getWindow().setBackgroundDrawableResource(R.color.white);
        this.getActionBar().hide();
        
        Bundle extra = getIntent().getExtras();
        if(!extra.containsKey(EXTRA_URL)){
            return;
        }
        String url = extra.getString(EXTRA_URL);
        boolean needLogin = extra.getBoolean(EXTRA_NEED_LOGIN, false);
        boolean needTitle = extra.getBoolean(EXTRA_NEED_TITLE, false);
        
        LinearLayout linear = new LinearLayout(this);
        linear.setId(CONTAINER_ID);
        
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        linear.setLayoutParams(lp);
        setContentView(linear);
        
        if (findViewById(CONTAINER_ID) != null && savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            
            FragmentWeb fragment = new FragmentWeb();
            fragment.setUrl(url, needLogin, needTitle);
            
            ft.add(CONTAINER_ID, fragment);
            ft.commit();
        }
    }
    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        EgmService.getInstance().doGetPrivateData();
    }
}

