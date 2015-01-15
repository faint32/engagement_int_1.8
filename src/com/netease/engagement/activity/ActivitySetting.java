package com.netease.engagement.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import com.netease.date.R;
import com.netease.engagement.fragment.FragmentSettingMain;


public class ActivitySetting extends ActivityEngagementBase {
    public static void launch(Context context){
        Intent intent = new Intent(context, ActivitySetting.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }
    private InputMethodManager  mManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setCustomActionBar();

        LinearLayout linear = new LinearLayout(this);
        linear.setId(R.id.activity_setting_container_id);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        linear.setLayoutParams(lp);
        setContentView(linear);

        if (findViewById(R.id.activity_setting_container_id) != null
                && savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            FragmentSettingMain fragment = new FragmentSettingMain();
            ft.add(R.id.activity_setting_container_id, fragment);
            ft.commit();
        }
        mManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
    }
    
    @Override  
     public boolean onTouchEvent(MotionEvent event) {  
      if(event.getAction() == MotionEvent.ACTION_DOWN){  
         if(getCurrentFocus()!=null && getCurrentFocus().getWindowToken()!=null){  
             mManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);  
         }  
      }  
      return super.onTouchEvent(event);  
     }  
    
}
