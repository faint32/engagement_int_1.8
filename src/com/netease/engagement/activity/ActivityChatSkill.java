package com.netease.engagement.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.LinearLayout;

import com.netease.date.R;
import com.netease.engagement.fragment.FragmentBase;
import com.netease.engagement.fragment.FragmentChatSkill;

public class ActivityChatSkill extends ActivityEngagementBase {
	private final int CONTAINER_ID = R.id.activity_chatskill_container_id;
    private FragmentChatSkill mFragment;
    
    public static void startActivity(Activity context){
        Intent intent = new Intent(context, ActivityChatSkill.class);
        
        //非activity的context启动activity会出错
        if(!(context instanceof Activity))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.push_up_in, R.anim.fake_fade_out);
    }
    
    public static void startActivity(FragmentBase fragment){
        Intent intent = new Intent(fragment.getActivity(), ActivityChatSkill.class);
        fragment.startActivity(intent);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setCustomActionBar();
        
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
            
            mFragment = FragmentChatSkill.newInstance();
            ft.add(CONTAINER_ID, mFragment);
            ft.commit();
        }
    }
}
