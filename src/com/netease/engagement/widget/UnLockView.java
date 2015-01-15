package com.netease.engagement.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.internal.ViewCompat;
import com.netease.common.image.ImageViewAsyncCallback;
import com.netease.date.R;
import com.netease.engagement.dataMgr.GiftDownLoadManager;
import com.netease.engagement.dataMgr.GiftInfoManager;
import com.netease.service.Utils.EgmUtil;

public class UnLockView extends LinearLayout{

	public UnLockView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public UnLockView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public UnLockView(Context context) {
		super(context);
		init(context);
	}
	
	private Context mContext ;
	
	private View view_left ;
	private View view_right ;
	
	private RelativeLayout middleLayout ;
	private ImageView middleRound ;
	
	private ImageView giftIcon ;
	private LinearLayout coinIconLayout ;
	private TextView coinIcon;
	private LinearLayout sendGiftLayout;
	private TextView send_gift_txt ;
	private TextView praisePhotoTip;
	
	private int gift_icon_translate_distance = 32 ;
	
	private AnimationSet send_gift_anim_set ;
	private TranslateAnimation send_gift_translate_anim ;
	private ScaleAnimation send_gift_scale_anim ;
	private AlphaAnimation send_gift_alpha_anim ;
	
	private AlphaAnimation gift_icon_alpha_anim ;
	
	private ScaleAnimation middle_rount_scale_anim ;
	private RotateAnimation middle_layout_rotate_anim ;
	
	private TranslateAnimation top_left_translate_anim ;
	private TranslateAnimation top_right_translate_anim ;
	private float top_translate_distance ;
	
	private void init(Context context){
		mContext = context ;
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View root = inflater.inflate(R.layout.view_unlock_layout,this,true);
		root.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		view_left = root.findViewById(R.id.left);
		view_right = root.findViewById(R.id.right);
		
		middleLayout = (RelativeLayout)root.findViewById(R.id.middle_layout);
		middleRound = (ImageView)root.findViewById(R.id.middle_round);
		
		giftIcon = (ImageView)root.findViewById(R.id.gift_icon);
		
		coinIconLayout = (LinearLayout) root.findViewById(R.id.txt_coin_layout);
		coinIcon = (TextView)root.findViewById(R.id.txt_coin);
		
		sendGiftLayout = (LinearLayout)root.findViewById(R.id.txt_send_gift_layout);
		send_gift_txt = (TextView)root.findViewById(R.id.txt_send_gift);
		
		praisePhotoTip = (TextView)root.findViewById(R.id.txt_praise_tip);
		
		gift_icon_translate_distance = EgmUtil.dip2px(mContext,gift_icon_translate_distance);
		top_translate_distance = mContext.getResources().getDisplayMetrics().widthPixels/2 ;
		initAnims();
	}
	
	/**
	 * 初始化送礼物金币
	 * @param needCoins
	 */
	public void initCoins(int needCoins){
		coinIcon.setText(String.format(mContext.getResources().getString(R.string.coin_unit), 
				needCoins));
	}
	
	/**
	 * 初始化赞照片和踩照片的提示
	 * 如果该私照没有人赞过和没有人踩过，隐藏这个view
	 */
	public void initPraisePhoto(long praiseCount, long unlikeCount) {
		if(praiseCount > 0 || unlikeCount > 0) {
			praisePhotoTip.setVisibility(View.VISIBLE);
			praisePhotoTip.setText(String.format(
					mContext.getResources().getString(R.string.praise_photo_tip), 
					praiseCount, unlikeCount));
		} else {
			praisePhotoTip.setVisibility(View.GONE);
		}
	}
	
	public static final int STATE_UNLOCK = 1 ;
	public static final int STATE_VIP = 2 ;
	public static final int STATE_COMMON = 3;
	public void initLogos(int state,int vipTimes, int giftIconId){
		switch(state){
			case STATE_UNLOCK:
				//do nothing ;
				break;
			case STATE_VIP:
				ViewCompat.setBackground(giftIcon,mContext.getResources().getDrawable(R.drawable.icon_vip_unlock));
				send_gift_txt.setText(String.format(mContext.getResources().getString(R.string.vip_times),vipTimes));
				coinIconLayout.setVisibility(View.GONE);
				break;
			case STATE_COMMON:
				giftIcon.setImageResource(R.drawable.icon_key_for_gift);
				
				GiftInfoManager.setGiftInfo(giftIconId, giftIcon);
				break;
		}
	}
	
	public boolean alreadyStartAnimation = false;
	
	public void startUnlockAnim(){
		alreadyStartAnimation = true;
		coinIconLayout.setVisibility(View.GONE);
		praisePhotoTip.setVisibility(View.GONE);
		send_gift_txt.setText("");
		sendGiftLayout.startAnimation(send_gift_anim_set);
		giftIcon.startAnimation(gift_icon_alpha_anim);
	}
	
	private void initAnims(){
		send_gift_anim_set = new AnimationSet(true);
		send_gift_anim_set.setInterpolator(new LinearInterpolator());
		
		send_gift_translate_anim = new TranslateAnimation(0,0,0,-2*gift_icon_translate_distance);
		send_gift_alpha_anim = new AlphaAnimation(0.3f,1.0f);
		
		send_gift_scale_anim = new ScaleAnimation(
				1.0f,0.5f,
				1.0f,0.5f,
				ScaleAnimation.RELATIVE_TO_SELF,0.5f,
				ScaleAnimation.RELATIVE_TO_SELF,0.5f) ;
		
		send_gift_anim_set.addAnimation(send_gift_translate_anim);
		send_gift_anim_set.addAnimation(send_gift_scale_anim);
		send_gift_anim_set.addAnimation(send_gift_alpha_anim);
		send_gift_anim_set.setDuration(300);
		
		gift_icon_alpha_anim = new AlphaAnimation(1.0f,0.0f);
		gift_icon_alpha_anim.setDuration(300);
		gift_icon_alpha_anim.setFillAfter(true);
		
		send_gift_anim_set.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation arg0) {
				sendGiftLayout.setVisibility(View.GONE);
				middleLayout.setVisibility(View.VISIBLE);
				middleRound.startAnimation(middle_rount_scale_anim);
			}
			@Override
			public void onAnimationRepeat(Animation arg0) {}
			@Override
			public void onAnimationStart(Animation arg0) {}
		});
		
		middle_layout_rotate_anim = new RotateAnimation(
				0f,90,
				RotateAnimation.RELATIVE_TO_SELF,0.5f,
				RotateAnimation.RELATIVE_TO_SELF,0.5f);
		middle_layout_rotate_anim.setDuration(300);
		middle_layout_rotate_anim.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation arg0) {
				middleLayout.setVisibility(View.GONE);
				view_left.startAnimation(top_left_translate_anim);
				view_right.startAnimation(top_right_translate_anim);
			}
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			@Override
			public void onAnimationStart(Animation arg0) {
			}
		});
		
		middle_rount_scale_anim = new ScaleAnimation(
				0.0f,1.0f,
				0.0f,1.0f,
				ScaleAnimation.RELATIVE_TO_SELF,0.5f,
				ScaleAnimation.RELATIVE_TO_SELF,0.5f) ;
		middle_rount_scale_anim.setDuration(300);
		middle_rount_scale_anim.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation arg0) {
				middleLayout.startAnimation(middle_layout_rotate_anim);
			}
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			@Override
			public void onAnimationStart(Animation arg0) {
				sendGiftLayout.setVisibility(View.GONE);
			}
		});
		
		top_left_translate_anim = new TranslateAnimation(
				0f,-top_translate_distance,
				0,0);
		top_left_translate_anim.setDuration(300);
		top_left_translate_anim.setFillAfter(true);
		top_right_translate_anim = new TranslateAnimation(
				0f,top_translate_distance,
				0,0);
		top_right_translate_anim.setDuration(300);
		top_right_translate_anim.setFillAfter(true);
		
		top_right_translate_anim.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation arg0) {
				giftIcon.setVisibility(View.GONE);
				coinIconLayout.setVisibility(View.GONE);
				sendGiftLayout.setVisibility(View.GONE);
				praisePhotoTip.setVisibility(View.GONE);
			}
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			@Override
			public void onAnimationStart(Animation arg0) {
			}
		});
	}


}
