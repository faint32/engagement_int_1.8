package com.netease.engagement.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.LinearLayout;

import com.netease.date.R;
import com.netease.engagement.fragment.FragmentInvite;


public class ActivityInvite extends ActivityEngagementBase {
    private FragmentInvite mFragmentInvite;
    
    public static void launch(Context context){
        Intent intent = new Intent(context, ActivityInvite.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setCustomActionBar();

        LinearLayout linear = new LinearLayout(this);
        linear.setId(R.id.activity_invite_container_id);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        linear.setLayoutParams(lp);
        setContentView(linear);

        if (findViewById(R.id.activity_invite_container_id) != null
                && savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            mFragmentInvite = new FragmentInvite();
            ft.add(R.id.activity_invite_container_id, mFragmentInvite);
            ft.commit();
        }
    }
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (mFragmentInvite != null && mFragmentInvite.getSsoHandler() != null) {
            mFragmentInvite.getSsoHandler().authorizeCallBack(requestCode, resultCode, data);
        }
    }
    

}
