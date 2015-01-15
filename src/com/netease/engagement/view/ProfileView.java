package com.netease.engagement.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

import com.netease.common.image.ImageType;
import com.netease.common.image.util.ImageUtil;
import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.app.EngagementApp;
import com.netease.engagement.widget.LoadingImageView;
import com.netease.service.Utils.EgmUtil;

//该类准备废弃，新功能请用HeadView by echo_chen 2014-10-20
public class ProfileView extends RelativeLayout {
	
	public LoadingImageView userFace;
	private ImageView vipImage;
	private Bitmap defaultProfile ;
	private int mProfileSize ;
	private Context mContext;
	
	//因为用户中心和用户资料页用同样大小的头像，但边框不一样，所以通过这个变量去决定用哪个边框。
	//后续版本视觉会统一，届时此变量可以去掉。
	private boolean isUserCenter = false;
	
	private int margin_5dp = (int) (3 * EngagementApp.getAppInstance().getResources().getDisplayMetrics().density);
	private int mDlta;

	public ProfileView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mDlta = context.getResources().getDimensionPixelSize(R.dimen.padding_15);
	}
	
	private int mSize ;
	//48*48
	public static final int PROFILE_SIZE_TINY = 0 ;
	//64*64
	public static final int PROFILE_SIZE_SMALL = 1;
	//96*96
	public static final int PROFILE_SIZE_BIG = 2 ;
	//102*102
	public static final int PROFILE_SIZE_LARGE = 3 ;
	/**
	 * @param userAttr 用户属性，是否是vip
	 * @param url 图像url
	 */
	public void setImageUrl(boolean isVip,int profileSize, String url, int sex) {
		
		mSize = profileSize ;
		if(profileSize == PROFILE_SIZE_TINY){
			mProfileSize = 48 ;
		}else if(profileSize == PROFILE_SIZE_SMALL){
			mProfileSize = 68 ;
		}else if(profileSize == PROFILE_SIZE_BIG){
			mProfileSize = 96 ;
		}else if(profileSize == PROFILE_SIZE_LARGE){
			mProfileSize = 102 ;
		}
		
		//重新设置头像区域大小
		ViewGroup.LayoutParams lp = this.getLayoutParams();
		mProfileSize = EgmUtil.dip2px(getContext(),mProfileSize);
		lp.width = mProfileSize;
		lp.height = mProfileSize;
		this.setLayoutParams(lp);
		
		defaultProfile = ImageUtil.getCircleBitmap(ImageUtil
				.getBitmap(EngagementApp.getAppInstance().getResources(),
						R.drawable.bg_portrait_ai_200, mProfileSize, mProfileSize));
		
		if(sex == EgmConstants.SexType.Female) {
			defaultProfile = ImageUtil.getCircleBitmap(ImageUtil
					.getBitmap(EngagementApp.getAppInstance().getResources(),
							R.drawable.bg_portrait_women_default_200, mProfileSize, mProfileSize));
		} else {
			defaultProfile = ImageUtil.getCircleBitmap(ImageUtil
					.getBitmap(EngagementApp.getAppInstance().getResources(),
							R.drawable.bg_portrait_man_default_200, mProfileSize, mProfileSize));
		}
		
		
		userFace = new LoadingImageView(getContext());
		userFace.setServerClipSize(mProfileSize - margin_5dp, mProfileSize - margin_5dp);
		//设置默认图标
		userFace.setImageBitmap(defaultProfile);
		userFace.setScaleType(ScaleType.FIT_XY);
		LayoutParams lpd = new LayoutParams(mProfileSize - mDlta, mProfileSize - mDlta);// mDlta把头像内宿一些，避免毛边
		lpd.addRule(RelativeLayout.CENTER_IN_PARENT);
		lpd.bottomMargin = margin_5dp;
		lpd.rightMargin = margin_5dp;
		this.addView(userFace, lpd);
		
		addBorder(profileSize);
		

		if(isVip){
		    if(vipImage != null){
		        vipImage.setVisibility(View.VISIBLE);
		        removeView(vipImage);
		        addVipTag();
		    } else {
		        addVipTag();
		    }
		}else{
			if(vipImage != null){
			    removeView(vipImage);
			    vipImage = null;
			} 
		}
		
		if (!TextUtils.isEmpty(url)) {
			userFace.setLoadingImage(url, ImageType.CircleMemCache);
		}
	}
	
	public void setImage(boolean isVip, int profileSize, Bitmap bitmap) {
	    mSize = profileSize ;
	    
        if(profileSize == PROFILE_SIZE_TINY){
            mProfileSize = 48 ;
        }else if(profileSize == PROFILE_SIZE_SMALL){
            mProfileSize = 64 ;
        }else if(profileSize == PROFILE_SIZE_BIG){
            mProfileSize = 96 ;
        }else if(profileSize == PROFILE_SIZE_LARGE){
            mProfileSize = 102 ;
        }
        
        //重新设置头像区域大小
        ViewGroup.LayoutParams lp = this.getLayoutParams();
        mProfileSize = EgmUtil.dip2px(getContext(),mProfileSize);
        lp.width = mProfileSize ;
        lp.height = mProfileSize ;
        this.setLayoutParams(lp);
        
        defaultProfile = ImageUtil.getCircleBitmap(ImageUtil
				.getBitmap(EngagementApp.getAppInstance().getResources(),
						R.drawable.bg_portrait_ai_200, mProfileSize, mProfileSize));
        
        userFace = new LoadingImageView(getContext());
        userFace.setServerClipSize(mProfileSize - margin_5dp, mProfileSize - margin_5dp);
        //设置默认图标
        userFace.setImageBitmap(defaultProfile);
        userFace.setScaleType(ScaleType.FIT_XY);
        LayoutParams lpd = new LayoutParams(mProfileSize - mDlta, mProfileSize - mDlta);// mDlta把头像内宿一些，避免毛边
        lpd.addRule(RelativeLayout.CENTER_IN_PARENT);
        lpd.bottomMargin = margin_5dp;
        lpd.rightMargin = margin_5dp;
        this.addView(userFace, lpd);
        
        addBorder(profileSize);
        
        if(isVip){
            if(vipImage != null){
                vipImage.setVisibility(View.VISIBLE);
                removeView(vipImage);
                addVipTag();
            } else {
                addVipTag();
            }
        }else{
            if(vipImage != null){
                removeView(vipImage);
                vipImage = null;
            } 
        }

        if (bitmap != null) {
            userFace.setImageBitmap(ImageUtil.getCircleBitmap(bitmap));
        }
    }
	
	private void addBorder(int profileSize){
		ImageView cover = new ImageView(getContext());
		//设置头像遮罩
		int circleResId;
		switch(mSize){
            case PROFILE_SIZE_TINY:
            case PROFILE_SIZE_SMALL:
                circleResId = R.drawable.bg_portrait_circle_136x136;
                break;
            default:
            	if(isUserCenter) {
            		circleResId = R.drawable.bg_portrait_circle_slim_200x200;
            	} else {
            		circleResId = R.drawable.bg_portrait_circle_200x200;
            	}
                break;
        }
		Drawable d = mContext.getResources().getDrawable(circleResId);
		cover.setImageDrawable(d);
		cover.setScaleType(ScaleType.FIT_XY);
		RelativeLayout.LayoutParams lpd = new LayoutParams(mProfileSize, mProfileSize);
		if(profileSize != PROFILE_SIZE_LARGE) {
			lpd.bottomMargin = margin_5dp;
			lpd.rightMargin = margin_5dp;
		}
		if(isUserCenter) {
			lpd.bottomMargin = margin_5dp * 2;
			lpd.rightMargin = margin_5dp * 2;
		}
		lpd.addRule(RelativeLayout.CENTER_IN_PARENT);
		this.addView(cover,lpd);
	}
	
	private void addVipTag(){
	    vipImage = new ImageView(getContext());
		//加V标志
		Drawable vip = null ;
		switch(mSize){
			case PROFILE_SIZE_TINY:
			case PROFILE_SIZE_SMALL:
				vip = mContext.getResources().getDrawable(R.drawable.icon_portrait_vip_30x30);
				break;
			default:
				vip = mContext.getResources().getDrawable(R.drawable.icon_vip_big);
				break;
		}
		vipImage.setImageDrawable(vip);
		RelativeLayout.LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		this.addView(vipImage, lp);
		
	}
	
	public void setIsUserCenter(boolean isUserCenter) {
		this.isUserCenter = isUserCenter;
	}
}
