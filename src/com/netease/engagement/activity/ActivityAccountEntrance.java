package com.netease.engagement.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.widget.LinearLayout;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.fragment.FragmentAccountEntrance;
import com.netease.service.Utils.LoginHelper;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmService;

/**
 * 登录和注册的主入口
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class ActivityAccountEntrance extends ActivityEngagementBase {
    public static final int CONTAINER_ID = R.id.activity_account_entrance_container_id;
    private int mSexType = -1;
    
    private FragmentManager mFragmentManager;
    
    public static void startActivity(Context context){
        Intent intent = new Intent(context, ActivityAccountEntrance.class);
        
        //非activity的context启动activity会出错
        if(!(context instanceof Activity))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        context.startActivity(intent);
    }
    
    public static void startActivity(Context context, int sexType){
        Intent intent = new Intent(context, ActivityAccountEntrance.class);
        intent.putExtra(EgmConstants.EXTRA_SEX_TYPE, sexType);
        
        //非activity的context启动activity会出错
        if(!(context instanceof Activity))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        context.startActivity(intent);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setCustomActionBar();
        
        mFragmentManager = getSupportFragmentManager();
        String ursId = EgmPrefHelper.getURSId(this);
		String ursKey = EgmPrefHelper.getURSKey(this);
		if (TextUtils.isEmpty(ursId) || TextUtils.isEmpty(ursKey)){
			EgmService.getInstance().doInitURS();
    		}
        
        Bundle extra = this.getIntent().getExtras();
        if(extra != null && extra.containsKey(EgmConstants.EXTRA_SEX_TYPE)){
            mSexType = extra.getInt(EgmConstants.EXTRA_SEX_TYPE);
        }
        
        this.getWindow().setBackgroundDrawableResource(R.color.app_background);
        
        LinearLayout linear = new LinearLayout(this);
        linear.setId(CONTAINER_ID);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        linear.setLayoutParams(lp);
        setContentView(linear);
        
        if (findViewById(CONTAINER_ID) != null && savedInstanceState == null) {
            FragmentAccountEntrance fragment = FragmentAccountEntrance.newInstance(mSexType);
            mFragmentManager.beginTransaction()
                .add(CONTAINER_ID, fragment)
                .commit();
        }
        
    }
    
    @Override
    public void onDestroy(){
        super.onDestroy();
    }
}
