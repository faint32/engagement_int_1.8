package com.netease.engagement.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;
import android.widget.LinearLayout;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants.BUNDLE_KEY;
import com.netease.engagement.fragment.FragmentDownload;


public class ActivityDownload extends FragmentActivity {
    private FragmentDownload mFragmentDownload;
    public static void lunch(Context context, String url,boolean cancelable) {
        Intent intent = new Intent(context, ActivityDownload.class);
        intent.putExtra(BUNDLE_KEY.UPDATE_URL, url);
        intent.putExtra(BUNDLE_KEY.DOWNLOAD_CANCELABLE, cancelable);
        if(!(context instanceof FragmentActivity))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout linear = new LinearLayout(this);
        linear.setId(R.id.activity_download_container_id);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        linear.setLayoutParams(lp);
        setContentView(linear);
        Intent intent = getIntent();
        String url = null;
        if(intent != null) {
            url = intent.getStringExtra(BUNDLE_KEY.UPDATE_URL);
        }
        if(url == null ){
            finish();
            return;
        }
        if (findViewById(R.id.activity_download_container_id) != null
                && savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            mFragmentDownload = new FragmentDownload();
            ft.add(R.id.activity_download_container_id, mFragmentDownload);
            ft.commit();
        }
        setFinishOnTouchOutside(false);
    }
    @Override
    public void onBackPressed() {
        if(mFragmentDownload != null){
            mFragmentDownload.onBack();
        }
        
    }

}
