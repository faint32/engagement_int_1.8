package com.netease.engagement.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

/**
 * 
 * @version 1.0
 */
public class OneByOneMenuBar extends LinearLayout{
    private final int DURATION_SHOW_BAR = 400;
    private final int DURATION_HIDE_BAR = 400;
    private final int DURATION_CHILD = 100;
    private final int DURATION_CHILD_INTERVAL = 100;
    
    public boolean isHiding ;
    public boolean isShowing;
    private AnimationSet mShowBarAnim;
    private AlphaAnimation mHideBarAnim;
    
    /** 标记是否正在进行动画 */
    public boolean mIsInAnimation = false;
    
    /** 标记隐藏动画是否正在进行 */
    private   boolean flagHide;
    
    public OneByOneMenuBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OneByOneMenuBar(Context context) {
        super(context);
        init();
    }
    
    private void init(){
        mShowBarAnim = new AnimationSet(false);
        
        ScaleAnimation showBarAnimT = new ScaleAnimation(1, 1, 0.5f, 1, Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 1);
        AlphaAnimation showBarAnimA = new AlphaAnimation(0.0f, 1.0f);
        
        mShowBarAnim.addAnimation(showBarAnimT);
        mShowBarAnim.addAnimation(showBarAnimA);
        mShowBarAnim.setDuration(DURATION_SHOW_BAR);
        mShowBarAnim.setAnimationListener(new AnimationListener(){
            @Override
            public void onAnimationStart(Animation animation) {
                mIsInAnimation = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIsInAnimation = false;
                isShowing=false;
                OneByOneMenuBar.this.setVisibility(View.VISIBLE);
                showChildren();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
            
        });
        
        mHideBarAnim = new AlphaAnimation(1.0f, 0.0f);
        mHideBarAnim.setDuration(DURATION_HIDE_BAR);
        mHideBarAnim.setAnimationListener(new AnimationListener(){
            @Override
            public void onAnimationStart(Animation animation) {
                mIsInAnimation = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIsInAnimation = false;
                OneByOneMenuBar.this.setVisibility(View.INVISIBLE);
                isHiding=false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
            
        });
    }
    
    private AnimationSet getAnimSet(boolean isHide ,int childHeight){
        TranslateAnimation translateAnim = null ;
        AlphaAnimation alphaAnim = null ;
        
        if(!isHide){
            translateAnim = new TranslateAnimation(0.0f, 0.0f, this.getHeight(), 0);
            translateAnim.setInterpolator(new OvershootInterpolator(3.0f));
            alphaAnim = new AlphaAnimation(0.0f, 1.0f);
        }
        else{
            translateAnim = new TranslateAnimation(0.0f, 0.0f, 0, this.getHeight());
            translateAnim.setInterpolator(new AnticipateInterpolator(3.0f));
            alphaAnim = new AlphaAnimation(1.0f, 0.5f);
        }
        
        AnimationSet animSet = new AnimationSet(false);
        animSet.addAnimation(translateAnim);
        animSet.addAnimation(alphaAnim);
        
        return animSet ;
    }

    public void show(){
        isShowing=true;
        this.startAnimation(mShowBarAnim);
    }
    
    public void hide(){
        int duration = DURATION_CHILD;
        int childCount = getChildCount();
        
        isHiding = true;
        isShowing = false;
        for(int i = childCount - 1; i >= 0; i--){
            View child = this.getChildAt(i);
            AnimationSet animSet = getAnimSet(true , child.getHeight());
            animSet.setFillAfter(true);
            animSet.setDuration(duration);
            duration = duration + DURATION_CHILD_INTERVAL ;
//            Log.v("lishang", "--- "+i);  
            if(i == 0){
                animSet.setAnimationListener(new AnimationListener(){
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        OneByOneMenuBar.this.startAnimation(mHideBarAnim);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                    @Override
                    public void onAnimationStart(Animation animation) {}
                });
            }
            
            child.startAnimation(animSet);
        }
     }
    
    private void showChildren(){
        if (isHiding)
            return;
        int duration = DURATION_CHILD;

        for(int i = 0; i < this.getChildCount(); i++){
            View child = this.getChildAt(i);
//          Log.v("lishang", "+++ "+i);    
            AnimationSet animSet = getAnimSet(false , child.getHeight());
            animSet.setDuration(duration);
            duration = duration + DURATION_CHILD_INTERVAL ;
            
            child.setVisibility(View.VISIBLE);
            child.startAnimation(animSet);
        }
    }
}
