package com.netease.engagement.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import com.netease.date.R;
import com.netease.engagement.fragment.FragmentBase;
import com.netease.engagement.fragment.FragmentMoneyAccount;
import com.netease.engagement.view.ShareDialog;

/** 女性账户界面 */
public class ActivityMoneyAccount extends ActivityEngagementBase {
    public static final int CONTAINER_ID = R.id.activity_money_account_container_id;
    public static String EXTRA_IS_SHORTCUT = "extra_is_shortcut";
    private boolean mIsFromShortcut = false;
    
    public static void startActivity(Activity context){
        Intent intent = new Intent(context, ActivityMoneyAccount.class);
        context.startActivity(intent);
    }
    
    public static void startActivity(Context context){
        Intent intent = new Intent(context, ActivityMoneyAccount.class);
        context.startActivity(intent);
    }
    
    /** 从快捷方式进入，activity的进入和退出方式是从下到上和从上到下 */
    public static void startActivityFromShortcut(Activity context){
        Intent intent = new Intent(context, ActivityMoneyAccount.class);
        intent.putExtra(EXTRA_IS_SHORTCUT, true);
        
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.push_up_in, R.anim.fake_fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setCustomActionBar();
        
        this.getWindow().setBackgroundDrawableResource(R.color.white);
        mIsFromShortcut = getIntent().getBooleanExtra(EXTRA_IS_SHORTCUT, false);
        mManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        
        LinearLayout linear = new LinearLayout(this);
        linear.setId(CONTAINER_ID);
        linear.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        setContentView(linear);
        
        if (findViewById(CONTAINER_ID) != null && savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            
            FragmentMoneyAccount fragment = new FragmentMoneyAccount();
            Bundle argument = new Bundle();
            argument.putBoolean(EXTRA_IS_SHORTCUT, mIsFromShortcut);
            fragment.setArguments(argument);
            
            ft.add(CONTAINER_ID, fragment);
            ft.commit();
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
    		FragmentManager fm = getSupportFragmentManager();
    		Fragment f = fm.findFragmentById(CONTAINER_ID);
    		
    		if (f != null && f instanceof FragmentBase) {
    			if (((FragmentBase) f).onBackPressed()) {
    				return true;
    			}
    		}
    	}
    	
    	return super.onKeyDown(keyCode, event);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (ShareDialog.mSsoHandler != null) {
        	ShareDialog.mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }
}
