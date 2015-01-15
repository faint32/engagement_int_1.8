package com.netease.engagement.view;

import com.netease.common.image.ImageViewAsyncCallback;
import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.db.manager.ManagerAccount;

import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout.LayoutParams;

public class HeadView extends RelativeLayout {

	private CircleImageView mHeadView;
	private ImageView mVipView;
	
	private int mType ;
	private int mProfileSize ;
	//48*48
	public static final int PROFILE_SIZE_TINY = 0 ;
	//64*64
	public static final int PROFILE_SIZE_SMALL = 1;
	//96*96
	public static final int PROFILE_SIZE_BIG = 2 ;
	//102*102
	public static final int PROFILE_SIZE_LARGE = 3 ;
	
	public HeadView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public HeadView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public HeadView(Context context) {
		super(context);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		findViews();
	}

	private void findViews() {
		mHeadView = (CircleImageView) findViewById(R.id.head);
		mVipView = (ImageView) findViewById(R.id.vip);
		if(mHeadView == null){
			mHeadView = new CircleImageView(getContext());
			mHeadView.setId(R.id.head);
//			mHeadView.setBorderColor(getContext().getResources().getColor(R.color.head_border));
//			mHeadView.setBorderWidth(EgmUtil.dip2px(getContext(),2));
//			mHeadView.setImageResource(R.drawable.bg_portrait_ai_200);
			LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			lp.addRule(RelativeLayout.CENTER_IN_PARENT);
			this.addView(mHeadView,lp);
		}
		if(mVipView == null){
			mVipView = new ImageView(getContext());
			mHeadView.setId(R.id.vip);
			LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
	        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			this.addView(mVipView,lp);
		}
		
	}
	public void setImageUrl(boolean isVip,int profileType, String url,int sex){
		setImage(isVip,profileType,url,sex,0,0,0,null);
	}
	public void setImageUrl(boolean isVip,int profileType, String url,int sex, int borderColor){
		setImage(isVip,profileType,url,sex,0,0,borderColor,null);
	}
	public void setImageUrl(boolean isVip,int profileType, String url,int sex,int borderId,int borderPx){
		setImage(isVip,profileType,url,sex,borderId,borderPx,0,null);
	}
	public void setImageBitmap(boolean isVip, int profileType, int sex,int borderId,int borderPx, Bitmap bitmap) {
		setImage(isVip,profileType,null,sex,borderId,borderPx,0,bitmap);
	}
	public void setImage(boolean isVip,int profileType, String url,int sex,int borderId,int borderPx, int borderColor, Bitmap bitmap) {
		
		mType = profileType ;
		if(mType == PROFILE_SIZE_TINY){
			mProfileSize = 48 ;
		}else if(mType == PROFILE_SIZE_SMALL){
			mProfileSize = 64 ;
		}else if(mType == PROFILE_SIZE_BIG){
			mProfileSize = 96 ;
		}else if(mType == PROFILE_SIZE_LARGE){
			mProfileSize = 102 ;
		}
		ViewGroup.LayoutParams lp = this.getLayoutParams();
		mProfileSize = EgmUtil.dip2px(getContext(),mProfileSize);
		lp.width = mProfileSize;
		lp.height = mProfileSize;
		this.setLayoutParams(lp);
		//border
		if(borderId > 0){
			this.setBackgroundResource(borderId);
			mProfileSize -= borderPx; 
//			mHeadView.setBorderColor(Color.argb(0x66, 0xff, 0xff, 0xff));
			mHeadView.setBorderWidth(0);
		}else{
			if(borderColor > 0){
				mHeadView.setBorderColor(borderColor);
			} else {
				mHeadView.setBorderColor(getContext().getResources().getColor(R.color.head_border));
			}
			mHeadView.setBorderWidth(EgmUtil.dip2px(getContext(),2));
			this.setBackgroundResource(0);
		}
		lp = mHeadView.getLayoutParams();
		lp.width = mProfileSize;
		lp.height = mProfileSize;

		mHeadView.setLayoutParams(lp);
		
		if(sex >= 0){
			if(sex == EgmConstants.SexType.Male) {
				mHeadView.setImageResource(R.drawable.bg_portrait_man_default_200);
			} else {
				mHeadView.setImageResource(R.drawable.bg_portrait_women_default_200);
			}
		} else {
			mHeadView.setImageResource(R.drawable.bg_portrait_ai_200);
		}
		if(bitmap != null){
			mHeadView.setTag(null);
			mHeadView.setImageBitmap(bitmap);
		} else {
			if (TextUtils.isEmpty(url)) {
				mHeadView.setTag(null);
			}
			else {
				mHeadView.setTag(new ImageViewAsyncCallback(mHeadView, url));
			}
		}
		if(mType == PROFILE_SIZE_LARGE || mType == PROFILE_SIZE_BIG) {
			mVipView.setImageResource(R.drawable.icon_vip_big);
		} else{
			mVipView.setImageResource(R.drawable.icon_portrait_vip_30x30);
		}
		setVip(isVip);
		
	}
	
	public void setImage(boolean isVip, int profileSize, Bitmap bitmap) {
		mHeadView.setImageBitmap(bitmap);
		
		setVip(isVip);
	}

	private void setVip(boolean isVip) {
		if (isVip) {
			mVipView.setVisibility(View.VISIBLE);
		}
		else {
			mVipView.setVisibility(View.INVISIBLE);
		}
	}
}
