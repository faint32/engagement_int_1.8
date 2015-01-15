package com.netease.util;

import android.view.View;
import android.view.animation.AlphaAnimation;

public class AnimationUtil {

	public static void alphaShow(View view, int duration) {
		AlphaAnimation ani = new AlphaAnimation(0, 1);
		ani.setDuration(duration);
		
		view.clearAnimation();
		view.startAnimation(ani);
		view.setVisibility(View.VISIBLE);
	}
	
	public static void alphaHide(View view, int duration) {
		AlphaAnimation ani = new AlphaAnimation(1, 0);
		ani.setDuration(duration);
		
		view.clearAnimation();
		view.startAnimation(ani);
		view.setVisibility(View.INVISIBLE);
	}
	
}
