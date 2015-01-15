package com.netease.engagement.activity;

import com.netease.common.image.ImageType;
import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.widget.CircleProgress;
import com.netease.engagement.widget.LoadingImageView;
import com.netease.engagement.widget.LoadingImageView.IUiGetImage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class ActivityProfileExplore extends ActivityEngagementBase {
	
	public static final String PORTRAIT_URL_192 = "portrait_url_192";
	public static final String SEX = "sex";
	
	private String portraitUrl192;
	
	private LoadingImageView mPortrait192;
	private CircleProgress mProgressBar;
	private LinearLayout mCoverLayout;
	private int sex;
	
	public static void startActivity(Context context, String portraitUrl192, int sex){
		Intent intent = new Intent(context, ActivityProfileExplore.class);
		
		intent.putExtra(PORTRAIT_URL_192, portraitUrl192);
		intent.putExtra(SEX, sex);
		
		context.startActivity(intent);
		if(sex == EgmConstants.SexType.Female) {
			((Activity)context).overridePendingTransition(R.anim.scale_in, R.anim.keep_still);
		} else {
			((Activity)context).overridePendingTransition(R.anim.scale_in_man, R.anim.keep_still);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getActionBar().hide();
		
		Bundle extra = this.getIntent().getExtras();
		
		if(extra != null) {
			portraitUrl192 = extra.getString(PORTRAIT_URL_192);
			sex = extra.getInt(SEX);
		}
		
		setContentView(R.layout.activity_profile_explore);
		init();
	}
	
	private void init() {
		

		mPortrait192  = (LoadingImageView) findViewById(R.id.portrait_192);
		mProgressBar=(CircleProgress)findViewById(R.id.progress_bar);
		mPortrait192.setCircleProgress(mProgressBar);
		
		mPortrait192.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				finish();
				return false;
			}
		});

		
		mCoverLayout = (LinearLayout) findViewById(R.id.cover_layout);
		mPortrait192.setUiGetImageListener(new IUiGetImage() {
			@Override
			public void onLoadImageStar(String url) {
			}
			
			@Override
			public void onLoadImageFinish() {
				mProgressBar.setVisibility(View.GONE);
				mCoverLayout.setVisibility(View.GONE);
				
				mPortrait192.startAnimation(getScaleOutAnimation());
				
				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				mPortrait192.setLayoutParams(lp);
				
				mPortrait192.startAnimation(getScaleInAnimation());
			}
			
			@Override
			public void onLoadImageError() {
				mProgressBar.setVisibility(View.GONE);
				mCoverLayout.setVisibility(View.GONE);
				
				mPortrait192.setImageDrawable(getResources().getDrawable(R.drawable.icon_photo_loaded_fail));
			}
		});
		mPortrait192.setImageDrawable(getResources().getDrawable(R.drawable.bg_portrait_ai_200));
		mPortrait192.setLoadingImage(portraitUrl192, ImageType.MemCache);
		
	}

	@Override
	public void finish() {
		super.finish();
		if(sex == EgmConstants.SexType.Female) {
			overridePendingTransition(0, R.anim.scale_out);
		} else {
			overridePendingTransition(0, R.anim.scale_out_man);
		}
	}
	
	private ScaleAnimation getScaleOutAnimation() {
		ScaleAnimation mScaleAnim = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f, 
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f );
		mScaleAnim.setDuration(300);
		return mScaleAnim;
	}
	
	private ScaleAnimation getScaleInAnimation() {
		ScaleAnimation mScaleAnim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, 
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f );
		mScaleAnim.setDuration(100);
		return mScaleAnim;
	}
	
//	private AlphaAnimation getDisappearAlphaAnimation() {
//		AlphaAnimation mAlphaAnim = new AlphaAnimation(1.0f, 0.0f);
//		mAlphaAnim.setDuration(300);
//		return mAlphaAnim;
//	}
//	
//	private AlphaAnimation getAppearAlphaAnimation() {
//		AlphaAnimation mAlphaAnim = new AlphaAnimation(0.0f, 1.0f);
//		mAlphaAnim.setDuration(300);
//		return mAlphaAnim;
//	}
	
}
