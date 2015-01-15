package com.netease.engagement.view;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

public class PraisePhotoAnimation {
	
	private AnimationSet comeFromBottom;
	
	private AnimationSet outToTop;
	
	private View mPraiseView;
	
	private RelativeLayout parent;
	private int mViewHeight;
	
	public void setPraiseView(View v) {
		this.mPraiseView = v;
		parent = (RelativeLayout) mPraiseView.getParent();
		mViewHeight = parent.getMeasuredHeight();
	
	}
	
	public void startAnimation() {
		initAnimation();
		mPraiseView.startAnimation(comeFromBottom);
	}
	
	private void initAnimation() {
		
		final ScaleAnimation selfScaleIn = new ScaleAnimation(2.0f, 1.0f, 2.0f, 1.0f, 
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
		selfScaleIn.setDuration(200);
		selfScaleIn.setInterpolator(new LinearInterpolator());
		selfScaleIn.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation arg0) {
				
			}
			
			@Override
			public void onAnimationRepeat(Animation arg0) {
				
			}
			
			@Override
			public void onAnimationEnd(Animation arg0) {
				mPraiseView.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						mPraiseView.startAnimation(outToTop);
					}
				}, 100);
			}
		});
		
		
		final ScaleAnimation selfScaleOut = new ScaleAnimation(1.0f, 2.0f, 1.0f, 2.0f, 
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
		selfScaleOut.setDuration(250);
		selfScaleOut.setFillAfter(true);
		selfScaleOut.setInterpolator(new LinearInterpolator());
		selfScaleOut.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation arg0) {
				
			}
			
			@Override
			public void onAnimationRepeat(Animation arg0) {
				
			}
			
			@Override
			public void onAnimationEnd(Animation arg0) {
				mPraiseView.startAnimation(selfScaleIn);
			}
		});
		
		
		
		
		comeFromBottom = new AnimationSet(true);
		comeFromBottom.setInterpolator(new LinearInterpolator());
		
		TranslateAnimation translate_anim_in = new TranslateAnimation(0, 0, 
				mViewHeight/4, 0);
		AlphaAnimation alpha_anim_in = new AlphaAnimation(0.3f, 1f);
		
		comeFromBottom.addAnimation(translate_anim_in);
		comeFromBottom.addAnimation(alpha_anim_in);
		comeFromBottom.setDuration(300);
		comeFromBottom.setFillAfter(true);
		
		comeFromBottom.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				mPraiseView.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				mPraiseView.startAnimation(selfScaleOut);
			}
		});
		
		
		outToTop = new AnimationSet(true);
		outToTop.setInterpolator(new LinearInterpolator());
		
		TranslateAnimation translate_anim_out = new TranslateAnimation(0, 0, 
				0, -mViewHeight/6);
		AlphaAnimation alpha_anim_out = new AlphaAnimation(1f, 0.3f);
		
		outToTop.addAnimation(translate_anim_out);
		outToTop.addAnimation(alpha_anim_out);
		outToTop.setDuration(200);
		outToTop.setFillAfter(false);
		
		outToTop.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				mPraiseView.setVisibility(View.GONE);
			}
		});
	}
}
