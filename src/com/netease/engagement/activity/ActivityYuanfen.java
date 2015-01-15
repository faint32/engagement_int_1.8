package com.netease.engagement.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import com.netease.date.R;
import com.netease.engagement.fragment.FragmentYuanfen;

/**
 * 碰缘分界面
 * @author Byron(hzchenlk&corp.netease.com)
 * @version 1.0
 */
public class ActivityYuanfen extends ActivityEngagementBase {
    private final int CONTAINER_ID = R.id.activity_yuanfen_container_id;
    private FragmentYuanfen mFragment;
    public final static  String EXTRA_IS_FROM_COME_IN_TIP = "extra_is_from_come_in_tip";
    
    public static void startActivity(Activity context, boolean isFromComeInTip){
        Intent intent = new Intent(context, ActivityYuanfen.class);
        intent.putExtra(EXTRA_IS_FROM_COME_IN_TIP, isFromComeInTip);
        
        //非activity的context启动activity会出错
        if(!(context instanceof Activity))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.push_up_in, R.anim.fake_fade_out);
    }
    
    public static void startActivity(Context context, boolean isFromComeInTip){
        Intent intent = new Intent(context, ActivityYuanfen.class);
        intent.putExtra(EXTRA_IS_FROM_COME_IN_TIP, isFromComeInTip);
        
        //非activity的context启动activity会出错
        if(!(context instanceof Activity))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        context.startActivity(intent);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        
        this.mActionBar.hide();
        
        Bundle extra = getIntent().getExtras();
        
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
            
            mFragment = new FragmentYuanfen();
            
            if(extra != null) {
            	mFragment.setArguments(extra);
            }
            
            ft.add(CONTAINER_ID, mFragment);
            ft.commit();
        }
    }
    
    /* 点击软键盘外的区域使软键盘隐藏 */
    private InputMethodManager mManager;
    @Override  
    public boolean onTouchEvent(MotionEvent event) {  
        if(event.getAction() == MotionEvent.ACTION_DOWN){  
            if(mManager != null && getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null){  
                mManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }  
        }  
        return super.onTouchEvent(event);  
    } 
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mFragment != null) {
            mFragment.complete();
        }
        
        return super.onKeyDown(keyCode, event);
    }
}
