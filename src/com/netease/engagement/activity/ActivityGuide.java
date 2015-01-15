package com.netease.engagement.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;

/** 引导界面 */
public class ActivityGuide extends ActivityEngagementBase {
    public static final String EXTRA_SEX_TYPE = "extra_sex_type";
    private int mSexType = EgmConstants.SexType.Female;
    private int mToLast = 0;
    
    public static void startActivity(Context context, int sexType){
        Intent intent = new Intent(context, ActivityGuide.class);
        intent.putExtra(EXTRA_SEX_TYPE, sexType);
        
        //非activity的context启动activity会出错
        if(!(context instanceof Activity))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        context.startActivity(intent);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extra = getIntent().getExtras();
        if(extra != null){
            mSexType = extra.getInt(EXTRA_SEX_TYPE);
        }
        
        this.mActionBar.hide();
        setContentView(R.layout.activity_guide_layout);
        
        ViewPager vp = (ViewPager)findViewById(R.id.viewpager);
        vp.setOffscreenPageLimit(1);
        vp.setAdapter(new GuidesAdapter());
        vp.setOnPageChangeListener(new OnPageChangeListener(){
            @Override
            public void onPageScrollStateChanged(int arg0) {}

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // 滑到最后一页，再滑动进入账号入口
                if(arg0 == 2 && arg2 == 0){
                    mToLast++;
                }
                else{
                    mToLast = 0;
                }
                
                if(mToLast > 2){
                    gotoEntrance();
                }
            }

            @Override
            public void onPageSelected(int arg0) { }
        });
    }
    
    private void gotoEntrance(){
        if(mSexType == EgmConstants.SexType.Female){
            ActivityAccountEntrance.startActivity(ActivityGuide.this, EgmConstants.SexType.Female);
        }
        else{
            ActivityAccountEntrance.startActivity(ActivityGuide.this, EgmConstants.SexType.Male);
        }
        
        finish();
    }
    
    private View.OnClickListener mClickInto = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            gotoEntrance();
        }
    };
    
    private class GuidesAdapter extends PagerAdapter {
        
        @Override
        public int getCount() {
            return 3;
        }
        
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
        }
        
        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = getView(position);
            container.addView(view);
            return view;
        }

        private View getView(int position) {
            ImageView view = new ImageView(ActivityGuide.this);
            switch(position){
                case 0:
                    if(mSexType == EgmConstants.SexType.Female){
                        view.setBackgroundResource(R.drawable.welcome_g1);
                    }
                    else{
                        view.setBackgroundResource(R.drawable.welcome_m1);
                    }
                    break;
                case 1:
                    if(mSexType == EgmConstants.SexType.Female){
                        view.setBackgroundResource(R.drawable.welcome_g2);
                    }
                    else{
                        view.setBackgroundResource(R.drawable.welcome_m2);
                    }
                    break;
                case 2:
                    if(mSexType == EgmConstants.SexType.Female){
                        view.setBackgroundResource(R.drawable.welcome_g3);
                    }
                    else{
                        view.setBackgroundResource(R.drawable.welcome_m3);
                    }
                    view.setOnClickListener(mClickInto);
                    break;
            }
            
            return view;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
    }
}
