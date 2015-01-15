package com.netease.android.camera.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.CompoundButton;

import com.netease.date.R;

public class ActionBarHighlightButton extends CompoundButton {
//	private static final int FADE_DURATION = 666;
//	private static final String TAG = "ActionBarHighlightButton";
//	private Drawable mBackgroundDrawable;
	private Drawable mButtonDrawable;
//	private AlphaDrawableAnimation mHighlightAnimation;

	public ActionBarHighlightButton(Context context) {
		super(context);
		init();
	}

	public ActionBarHighlightButton(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		TypedArray a = context.obtainStyledAttributes(attributeSet,
				R.styleable.caremaButton);
		mButtonDrawable = a.getDrawable(0);
		setBackgroundDrawable(mButtonDrawable);
		init();
		a.recycle();
	}

//	private int calculateStartAlpha(float alpha) {
//		float f = Math.max(Math.min(alpha, 1.0F), 0.0F);
//		return (int) (255.0F * f);
//	}

	private void init() {
//		mBackgroundDrawable = getResources().getDrawable(
//				R.drawable.action_bar_pressed_overlay).mutate();
//		mBackgroundDrawable.setAlpha(0);
//		setBackgroundDrawable(mBackgroundDrawable);
		setClickable(true);
	}

//	private void resetAnimation(float alpha) {
//		int i = calculateStartAlpha(alpha);
//		int j = (int) (i * FADE_DURATION / 255.0F);
//		this.mBackgroundDrawable.setAlpha(i);
//		if (this.mHighlightAnimation != null)
//			this.mHighlightAnimation.cancel();
//		this.mHighlightAnimation = new AlphaDrawableAnimation(
//				mBackgroundDrawable, i, 0);
//		this.mHighlightAnimation.setInterpolator(new DecelerateInterpolator());
//		this.mHighlightAnimation.setDuration(j);
//		this.mHighlightAnimation.setFillAfter(true);
//	}

	public Drawable getButtonDrawable() {
		return this.mButtonDrawable;
	}

//	public boolean onTouchEvent(MotionEvent event) {
//		boolean flag = false;
//		switch (event.getAction()) {
//		case 0:
//			if (this.mHighlightAnimation != null) {
//				startAnimation(mHighlightAnimation);
//			}
//			flag = true;
//			break;
//		case 1:
//			resetAnimation(event.getPressure());
//			flag = true;
//			break;
//		}
//		return super.onTouchEvent(event) || flag;
//	}

//	private static class AlphaDrawableAnimation extends Animation {
//		private int mCurrentAlpha = -1;
//		private Drawable mDrawable;
//		private int mFromAlpha;
//		private int mToAlpha;
//
//		public AlphaDrawableAnimation(Drawable drawable, int fromAlpha,
//				int toAlpha) {
//			this.mDrawable = drawable;
//			this.mFromAlpha = fromAlpha;
//			this.mToAlpha = toAlpha;
//		}
//
//		protected void applyTransformation(float interpolatedTime,
//				Transformation transformation) {
//			float f2 = (mToAlpha - mFromAlpha) * interpolatedTime;
//			int k = (int) (mFromAlpha + f2);
//			if (k != mCurrentAlpha) {
//				mDrawable.setAlpha(k);
//				mDrawable.invalidateSelf();
//				mCurrentAlpha = k;
//			}
//		}

//		public boolean willChangeBounds() {
//			return false;
//		}
//
//		public boolean willChangeTransformationMatrix() {
//			return false;
//		}
//	}
}