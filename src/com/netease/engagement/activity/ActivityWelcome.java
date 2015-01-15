package com.netease.engagement.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.app.EngagementApp;
import com.netease.engagement.fragment.FragmentBase;

/**
 * 欢迎界面(也是性别选择界面）
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class ActivityWelcome extends ActivityEngagementBase {
    public static final String EXTRA_SEX_RESULT = "extra_sex_result";
    public static final String EXTRA_INTO_TYPE = "extra_into_type";
    
    /** 第一次使用，需要进入介绍页 */
    public static final int TYPE_FIRST = 1;
    /** 注册，进来选性别 */
    public static final int TYPE_REGISTER = 2;
    /** 登录，进来选性别 */
    public static final int TYPE_LOGIN = 3;
    
    public static void startActivity(Context context, int type){
        Intent intent = new Intent(context, ActivityWelcome.class);
        intent.putExtra(EXTRA_INTO_TYPE, type);
        
        //非activity的context启动activity会出错
        if(!(context instanceof Activity))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        context.startActivity(intent);
    }
    
    public static void startActivityForResult(Activity activity, int requestCode, int type){
        Intent intent = new Intent(activity, ActivityWelcome.class);
        intent.putExtra(EXTRA_INTO_TYPE, type);
        
        activity.startActivityForResult(intent, requestCode);
    }
    public static void startActivityForResult(FragmentBase fragment, int requestCode, int type){
        Intent intent = new Intent(fragment.getActivity(), ActivityWelcome.class);
        intent.putExtra(EXTRA_INTO_TYPE, type);
        if(!(fragment.getActivity() instanceof Activity)){
        		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        fragment.startActivityForResult(intent, requestCode);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extra = getIntent().getExtras();
        final int type = extra.getInt(EXTRA_INTO_TYPE, TYPE_FIRST);
        
        this.mActionBar.hide();
        setContentView(R.layout.activity_welcome_layout);
        
        findViewById(R.id.entrance_male).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click(type, EgmConstants.SexType.Male);
            }
        });
        
        findViewById(R.id.entrance_female).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click(type, EgmConstants.SexType.Female);
            }
        });
    }

    private void click(int type, int sex){
        switch(type){
            case TYPE_FIRST:
                ActivityGuide.startActivity(ActivityWelcome.this, sex);   // 进入分性别的介绍页
                break;
            case TYPE_REGISTER:
                ActivityRegisterEntrance.startActivity(ActivityWelcome.this, sex);// 进入注册页面
                break;
            case TYPE_LOGIN:
                Intent intent = new Intent();
                intent.putExtra(EXTRA_SEX_RESULT, sex);
                setResult(Activity.RESULT_OK, intent);
                break;
               
        }
        EngagementApp.getAppInstance().setGender(sex);
        finish();
    }
}
