package com.netease.engagement.widget;

import com.netease.date.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ChatAnimView extends RelativeLayout {

	public ChatAnimView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ChatAnimView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ChatAnimView(Context context) {
		super(context);
		init();
	}
	
	private void init(){
	}
	
	private AnimationSet getAnimSet(boolean isHide ,int index){
		TranslateAnimation translateAnim = null ;
		AlphaAnimation alphaAnim = null ;
		if(!isHide){
			if(index == 0){
				translateAnim = new TranslateAnimation(1.0f*this.getWidth(),0.0f,this.getHeight(),0);
			}else if(index == 1){
				translateAnim = new TranslateAnimation(0.5f*this.getWidth(),0.0f,this.getHeight(),0);
			}
			translateAnim.setInterpolator(new OvershootInterpolator(3.0f));
			alphaAnim = new AlphaAnimation(0.0f, 1.0f);
		}else{
			if(index == 1){
				translateAnim = new TranslateAnimation(0.0f,1.0f*this.getWidth(),0,this.getHeight());
			}else if(index == 0){
				translateAnim = new TranslateAnimation(0.0f,0.5f*this.getWidth(),0,this.getHeight());
			}
			translateAnim.setInterpolator(new AnticipateInterpolator(3.0f));
			alphaAnim = new AlphaAnimation(1.0f, 0.5f);
		}
		
		AnimationSet animSet = new AnimationSet(false);
		animSet.addAnimation(translateAnim);
		animSet.addAnimation(alphaAnim);
		return animSet ;
	}
	
	public void startAnim(boolean isFirstTime){
		int duration = 300 ;
		if(isFirstTime) {;
			TextView tx = (TextView) findViewById(R.id.man_gift_tip_txt);
			if (tx != null) {
				AnimationSet animSet = getAnimSet(false, 0);
				animSet.setDuration(duration);
				tx.setVisibility(View.VISIBLE);
				tx.startAnimation(animSet);
			}
		}
		RelativeLayout view = (RelativeLayout) this.getChildAt(1);
		for(int i = 0 ;i < view.getChildCount();i++){
			View child = view.getChildAt(i);
			AnimationSet animSet = getAnimSet(false ,i);
			animSet.setDuration(duration);
			duration = duration + 100 ;
			child.setVisibility(View.VISIBLE);
			child.startAnimation(animSet);
		}
	}
	
	public void hideAllViews(){
		int duration = 300 ;
		for(int i = this.getChildCount()-1 ;i >= 0;i--){
			View child = this.getChildAt(i);
			AnimationSet animSet = getAnimSet(true ,i);
			animSet.setFillAfter(true);
			animSet.setDuration(duration);
			duration = duration + 100 ;
			child.startAnimation(animSet);
		}
	}
}