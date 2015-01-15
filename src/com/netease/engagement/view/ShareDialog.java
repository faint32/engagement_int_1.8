package com.netease.engagement.view;


import java.io.ByteArrayOutputStream;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.common.image.util.ImageUtil;
import com.netease.common.log.NTLog;
import com.netease.common.share.ShareBind;
import com.netease.common.share.ShareCallback;
import com.netease.common.share.ShareResult;
import com.netease.common.share.ShareService;
import com.netease.common.share.ShareType;
import com.netease.date.R;
import com.netease.engagement.app.EngagementApp;
import com.netease.engagement.dataMgr.GiftInfoManager;
import com.netease.engagement.dataMgr.TopicDataManager;
import com.netease.engagement.fragment.FragmentBase;
import com.netease.engagement.util.LevelChangeStatusBean.LevelChangeType;
import com.netease.service.Utils.WeixinShare;
import com.netease.service.Utils.YixinShare;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.meta.GiftInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;

public class ShareDialog {
	
	public static boolean isShowing = false;
	
	private FragmentBase fragment;
	private Activity activity;
	
	
	private AlertDialog alertDialog;
	
	private TextView closeTv;
	private TextView titleTv;
	private TextView shareViewTitleTv;
	
	private TextView weixinTv;
	private TextView yixinTv;
	private TextView weiboTv;
	
	private RelativeLayout shareBgRl;
	private LinearLayout shareRl;
	private Button noShareBtn;
	
	// money
	private RelativeLayout moneyRl;
	private TextView money2Tv;
	private TextView money3Tv;
	
	// gift
	private RelativeLayout giftRl;
	private ImageView giftIconIv;
	private TextView giftMsg1Iv;
	private TextView giftMsg2Iv;
	
	// level
	private RelativeLayout levelRl;
	private ImageView levelNumIv;
	private ImageView levelNameIv;
	private TextView levelMsg1Iv;
	private TextView levelMsg2Iv;
	private TextView levelMsg3Iv;
	
	public static SsoHandler mSsoHandler;
	private Oauth2AccessToken mAccessToken;
    private WeiboAuth mWeiboAuth;
    
    private int mTid;
    
    // sina
    public static final String SINA_CLIENT_ID = "876878023";
    public static final String SINA_REDIRECT_URI = "https://api.weibo.com/oauth2/default.html";
    public static final String SCOPE = "";
    
    
    private String showTitle;
    private String showContent;
    private String shareTitle;
    private String shareContent;
    private String shareContentWB;
    private String shareUrl;
    
    private Timer timer;
    private int currentBgIndex;
    private int bg1Res;
    private int bg2Res;
    private int oldLevelRes;
    private int newLevelRes;
    
    private int shareWBImageRes;
    private int shareWXYXImageRes;
    
    private ShareDialogInterface delegate;
    
    public boolean showMoney(FragmentBase fragment, String lastMoney) {
    	
    	if (!init(fragment)) {
    		return false;
    	}
    	
    	moneyRl.setVisibility(View.VISIBLE);
    	giftRl.setVisibility(View.GONE);
    	levelRl.setVisibility(View.GONE);
    	shareBgRl.setBackgroundResource(R.drawable.layer_money_bg4);
    	
    	JSONObject jsonObject = TopicDataManager.getInstance().getShareText("cash");
    	
		
    	if (jsonObject == null) {
    		return false;
    	}
    	setShow(jsonObject, 0);
    	setShare(jsonObject, 0);
    	
    	setShareUrl(lastMoney);
    	
    	String s1 = "";
    	String s2 = "";
    	String s[] = showContent.split("REPLACE");
    	if (s.length == 2) {
    		s1 = s[0];
    		s2 = s[1];
    	}
    	
    	shareContentWB = shareContentWB.replace("REPLACE", lastMoney);
    	
        titleTv.setText(showTitle);
        money2Tv.setText(s1);
        money3Tv.setText("¥"+lastMoney);
        shareViewTitleTv.setText(s2);
        
        shareWBImageRes = getWBImageRes(0, 0);
        shareWXYXImageRes = getResId("layer_wxyx_money");
        
        return true;
    }
    
    public boolean showGift(FragmentBase fragment, GiftInfo giftInfo) {
    	
    	if (!init(fragment)) {
    		return false;
    	}
    	
    	moneyRl.setVisibility(View.GONE);
    	giftRl.setVisibility(View.VISIBLE);
    	levelRl.setVisibility(View.GONE);
    	 
    	JSONObject jsonObject = TopicDataManager.getInstance().getShareText("gift");
    	if (jsonObject == null) {
    		return false;
    	}
    	setShow(jsonObject, giftInfo.id);
    	setShare(jsonObject, giftInfo.id);
    	
    	setShareUrl(""+giftInfo.id);
    	
        titleTv.setText(showTitle);
        giftMsg1Iv.setText(giftInfo.name);
        giftMsg2Iv.setText("价值：¥"+giftInfo.price);
        shareViewTitleTv.setText(showContent);
		
		GiftInfoManager.setGiftInfo(giftInfo.id, giftInfo, giftIconIv);
        
		shareWBImageRes = getWBImageRes(1, giftInfo.id);
		shareWXYXImageRes = getResId("layer_wxyx_gift");
		
        return true;
    }
    
    public boolean showLevel(FragmentBase fragment, LevelChangeType type, int oldLevel, int newLevel) {
    	
    	if (!init(fragment)) {
    		return false;
    	}
    	
    	moneyRl.setVisibility(View.GONE);
    	giftRl.setVisibility(View.GONE);
    	levelRl.setVisibility(View.VISIBLE);
    	
    	if (type==LevelChangeType.Male_Level_Up || type==LevelChangeType.Male_Level_Up_1) {
    		JSONObject jsonObject = TopicDataManager.getInstance().getShareText("male.upLevel");
    		if (jsonObject == null) {
    			return false;
        	}
    		setShow(jsonObject, newLevel);
        	setShare(jsonObject, newLevel);
        	
        	setShareUrl(""+newLevel);
        	
        	bg1Res = R.drawable.layer_upgrade_man_bg1;
        	bg2Res = R.drawable.layer_upgrade_man_bg2;
        	
        	oldLevelRes = getLevelNumImageRes(true, oldLevel);
        	newLevelRes = getLevelNumImageRes(true, newLevel);
        	
        	levelRl.setBackgroundResource(bg1Res);
        	levelNumIv.setImageResource(oldLevelRes);
        	levelNameIv.setImageResource(getLevelNameImageRes(true, newLevel));
        	levelMsg1Iv.setText("你已领先" + (newLevel-1) + "9%的小伙伴");
        	levelMsg1Iv.setBackgroundResource(R.drawable.layer_upgrade_man_titlebg);
        	levelMsg1Iv.setTextColor(Color.WHITE);
        	levelMsg2Iv.setText(showContent);
        	if (newLevel == 5) {
        		levelMsg3Iv.setVisibility(View.VISIBLE);
        		levelMsg3Iv.setText("*专属防骚扰设置已激活*");
        	}
        	
        	shareViewTitleTv.setText("分享到");
        	shareViewTitleTv.setTextColor(activity.getResources().getColor(R.color.content_text));
        	
        	shareWBImageRes = getWBImageRes(2, newLevel);
        	shareWXYXImageRes = getResId("layer_wxyx_man_level");
        	
    	} else if (type == LevelChangeType.Male_Level_Down) {
    		JSONObject jsonObject = TopicDataManager.getInstance().getShareText("male.downLevel");
    		if (jsonObject == null) {
    			return false;
        	}
    		setShow(jsonObject, oldLevel);
    		
    		bg1Res = R.drawable.layer_degrade_man_bg1;
        	bg2Res = R.drawable.layer_degrade_man_bg2;
        	
        	oldLevelRes = getLevelNumImageRes(false, oldLevel);
        	newLevelRes = getLevelNumImageRes(false, newLevel);
    		
    		levelRl.setBackgroundResource(bg1Res);
    		levelNumIv.setImageResource(oldLevelRes);
        	levelNameIv.setImageResource(getLevelNameImageRes(true, newLevel));
        	levelMsg1Iv.setText(showContent);
        	levelMsg2Iv.setBackgroundResource(R.drawable.layer_degrade_man_people);
    		
			shareRl.setVisibility(View.GONE);
			noShareBtn.setVisibility(View.VISIBLE);
			noShareBtn.setText(jsonObject.optString("btnTxt"));
			
    	} else if (type == LevelChangeType.Female_Level_Up) {
    		JSONObject jsonObject = TopicDataManager.getInstance().getShareText("female.upLevel");
    		if (jsonObject == null) {
    			return false;
        	}
    		setShow(jsonObject, newLevel);
        	setShare(jsonObject, newLevel);
        	String uid = ManagerAccount.getInstance().getCurrentIdString();
        	shareContentWB = shareContentWB.replace("REPLACE", uid);

        	setShareUrl(""+newLevel);
        	
        	bg1Res = R.drawable.layer_upgrade_man_bg1;
        	bg2Res = R.drawable.layer_upgrade_man_bg2;
        	
        	oldLevelRes = getLevelNumImageRes(true, oldLevel);
        	newLevelRes = getLevelNumImageRes(true, newLevel);
        	
        	levelRl.setBackgroundResource(bg1Res);
        	levelNumIv.setImageResource(oldLevelRes);
        	levelNameIv.setImageResource(getLevelNameImageRes(false, newLevel));
        	levelMsg1Iv.setVisibility(View.INVISIBLE);
        	levelMsg2Iv.setText(showContent);
        	shareViewTitleTv.setText("分享到");
        	shareViewTitleTv.setTextColor(activity.getResources().getColor(R.color.content_text));
    	
        	shareWBImageRes = getWBImageRes(3, newLevel);
        	shareWXYXImageRes = getResId("layer_wxyx_lady_level");
    	}
    	
    	titleTv.setText(showTitle);
    	currentBgIndex = 1;
    	
    	timer = new Timer();
    	timer.schedule(new TimerTask() {
			@Override
			public void run() {
				levelRl.post(new Runnable() {
					@Override
					public void run() {
						if(currentBgIndex == 1) {
							currentBgIndex = 2;
							levelRl.setBackgroundResource(bg2Res);
						} else {
							currentBgIndex = 1;
							levelRl.setBackgroundResource(bg1Res);
						}
					}
				});
			}
		}, 200, 200);
        
    	Animation anim1 = AnimationUtils.loadAnimation(activity, R.anim.alpha_1_to_0);
    	anim1.setStartOffset(500);
    	anim1.setDuration(500);
    	anim1.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			@Override
			public void onAnimationEnd(Animation animation) {
				levelNumIv.setImageResource(newLevelRes);
				Animation anim2 = AnimationUtils.loadAnimation(activity, R.anim.alpha_0_to_1);
				anim2.setDuration(500);
				levelNumIv.startAnimation(anim2);
			}
		});
    	levelNumIv.startAnimation(anim1);
    	
        return true;
    }
    
    private View view;
    
	public boolean init(FragmentBase fragment) {
		
		if (fragment==null || fragment.getActivity()==null) {
			return false;
		}
		
		isShowing = true;
		
		this.fragment = fragment;
		this.activity = fragment.getActivity();
		
		LayoutInflater inflater = LayoutInflater.from(activity);
		view = inflater.inflate(R.layout.view_share_dialog, null);
		
		// 关闭按钮
		closeTv = (TextView) view.findViewById(R.id.closeTv);
		closeTv.setOnClickListener(clickListener);
		
		// 标题
		titleTv = (TextView) view.findViewById(R.id.titleTv);
		shareViewTitleTv = (TextView) view.findViewById(R.id.shareViewTitleTv);
		
		// 微信按钮
		weixinTv = (TextView) view.findViewById(R.id.weixinTv);
		weixinTv.setOnClickListener(clickListener);
		
		// 易信按钮
		yixinTv = (TextView) view.findViewById(R.id.yixinTv);
		yixinTv.setOnClickListener(clickListener);
		
		// 微博按钮
		weiboTv = (TextView) view.findViewById(R.id.weiboTv);
		weiboTv.setOnClickListener(clickListener);
		
		// 分享部分背景
		shareBgRl = (RelativeLayout) view.findViewById(R.id.shareBgRl);
		
		// 分享按钮组背景
		shareRl = (LinearLayout) view.findViewById(R.id.shareRl);
		
		// 不用分享的按钮
		noShareBtn = (Button) view.findViewById(R.id.noShareBtn);
		noShareBtn.setOnClickListener(clickListener);
		
		// money
		moneyRl = (RelativeLayout) view.findViewById(R.id.moneyRl);
		money2Tv = (TextView) view.findViewById(R.id.money2Tv);
    	money3Tv = (TextView) view.findViewById(R.id.money3Tv);
		
    	// gift
    	giftRl = (RelativeLayout) view.findViewById(R.id.giftRl);
    	giftIconIv = (ImageView) view.findViewById(R.id.giftIconIv);
    	giftMsg1Iv = (TextView) view.findViewById(R.id.giftMsg1Iv);
    	giftMsg2Iv = (TextView) view.findViewById(R.id.giftMsg2Iv);
    	
    	// level
    	levelRl = (RelativeLayout) view.findViewById(R.id.levelRl);
    	levelNumIv = (ImageView) view.findViewById(R.id.levelNumIv);
    	levelNameIv = (ImageView) view.findViewById(R.id.levelNameIv);
    	levelMsg1Iv = (TextView) view.findViewById(R.id.levelMsg1Iv);
    	levelMsg2Iv = (TextView) view.findViewById(R.id.levelMsg2Iv);
    	levelMsg3Iv = (TextView) view.findViewById(R.id.levelMsg3Iv);

    	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setCancelable(true);
		alertDialog = builder.show();
		alertDialog.getWindow().setLayout( 
    			android.view.WindowManager.LayoutParams.MATCH_PARENT, 
    			android.view.WindowManager.LayoutParams.MATCH_PARENT); 
		alertDialog.getWindow().setContentView(view);
		alertDialog.setOnDismissListener(dismissListener);

		String uid = EgmPrefHelper.getUid(activity);
        if(!TextUtils.isEmpty(uid)){
            mAccessToken = new Oauth2AccessToken();
            mAccessToken.setUid(uid);
            mAccessToken.setToken(EgmPrefHelper.getAccessToken(activity));
            mAccessToken.setExpiresTime(EgmPrefHelper.getExpireIn(activity));
        }
        
        return true;
	}
	
	private OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.closeTv:
			case R.id.noShareBtn:
				dismiss();
				break;
			case R.id.weixinTv:
				if(!WeixinShare.getInstance().isRegWeixin()){
                    WeixinShare.getInstance().registerWeixin(EngagementApp.getAppInstance());
                }
                WeixinShare.getInstance().share2SessionWebpage(shareTitle, shareContent, getBitmapBytes(shareWXYXImageRes), shareUrl, true);
				break;
			case R.id.yixinTv:
				if(!YixinShare.getInstance().isRegYixin()){
                    YixinShare.getInstance().registerYixin(EngagementApp.getAppInstance());
                }
				YixinShare.getInstance().share2YixinWebpage(shareTitle, shareContent, getBitmapBytes(shareWXYXImageRes), shareUrl, true);
				break;
			case R.id.weiboTv:
				if(mAccessToken != null && mAccessToken.isSessionValid()){
                    SendBlog();
                } else {
                    mWeiboAuth = new WeiboAuth(activity, SINA_CLIENT_ID, SINA_REDIRECT_URI, SCOPE);
                    mSsoHandler = new SsoHandler(activity, mWeiboAuth);
                    mSsoHandler.authorize(new AuthListener());
                }
				break;
			default:
				break;
			}
		}
	};
	
	
	class AuthListener implements WeiboAuthListener {
        
        @Override
        public void onComplete(Bundle values) {
            // 从 Bundle 中解析 Token
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            if (mAccessToken.isSessionValid()) {
//            	Toast.makeText(activity, "weibo授权成功", Toast.LENGTH_SHORT).show();
                SendBlog();
                EgmPrefHelper.putUid(activity, mAccessToken.getUid());
                EgmPrefHelper.putAccessToken(activity, mAccessToken.getToken());
                EgmPrefHelper.putExpireIn(activity, mAccessToken.getExpiresTime());
            } else {
                // 以下几种情况，您会收到 Code：
                // 1. 当您未在平台上注册的应用程序的包名与签名时；
                // 2. 当您注册的应用程序包名与签名不正确时；
                // 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
                String code = values.getString("code");
                String message = activity.getResources().getString(R.string.weibo_auth_error);
                if (!TextUtils.isEmpty(code)) {
                    message = message + "\nObtained the code: " + code;
                }
                
                fragment.showToast(message);
            }
        }

        @Override
        public void onCancel() {
        	fragment.showToast("取消授权");
        }

        @Override
        public void onWeiboException(WeiboException e) {
        	fragment.showToast(activity.getResources().getString(R.string.weibo_auth_exeption) + e.getMessage());
        }
    }

   public SsoHandler getSsoHandler() {
       return mSsoHandler;
   }
   
	private void SendBlog(){
	       if(mAccessToken != null){
	    	   fragment.showWatting(activity.getResources().getString(R.string.req_waiting));
	           ShareBind shareBind = new ShareBind(ShareType.Sina);
	           shareBind.setUserID(mAccessToken.getUid());
	           shareBind.setAccessToken(mAccessToken.getToken());
	           
	           Bitmap img = BitmapFactory.decodeResource(activity.getResources(), shareWBImageRes);
	           String imgPath = ImageUtil.getBitmapFilePath(img, "weiboshare");
	           
	           mTid = ShareService.getShareService().sendMBlog(shareBind, null, shareContentWB, imgPath, null, mShareCallback);
	       }
	   }
	
	ShareCallback mShareCallback = new ShareCallback() {
	       @Override
	       public void onShareMBlogSuccess(int tid, ShareResult shareResult) {
	           if (tid == mTid) {
	               fragment.stopWaiting();
	               fragment.showToast(R.string.weibo_share_success);
	           }
	       };
	       
	       @Override
	       public void onShareMBlogError(int tid, ShareResult shareResult) {
	           if (tid == mTid) {
	        	   fragment.stopWaiting();
	               if (shareResult != null && !TextUtils.isEmpty(shareResult.getMessage())) {
	            	   fragment.showToast(shareResult.getMessage());
	                   NTLog.e("FragmentInvite","code is "+ shareResult.getCode() + " MessageCode is " + shareResult.getMessageCode());
	                   if(shareResult.getMessageCode().startsWith("213")){//授权相关错误
	                       EgmPrefHelper.deleteWeiboAcc(activity);
	                       mWeiboAuth = new WeiboAuth(activity, SINA_CLIENT_ID, SINA_REDIRECT_URI, SCOPE);
	                       mSsoHandler = new SsoHandler(activity, mWeiboAuth);
	                       mSsoHandler.authorize(new AuthListener());
	                   }
	               } else {
	            	   fragment.showToast(R.string.weibo_share_error);
	               }
	           }
	       };
	       
	   };
	   
	   
	
	private void dismiss() {
		if(alertDialog != null) {
			alertDialog.dismiss();
		}
		isShowing = false;
		if (delegate != null) {
			delegate.shareDialogDismiss();
		}
	}
	
	private OnDismissListener dismissListener = new OnDismissListener() {
		@Override
		public void onDismiss(DialogInterface dialog) {
			if(WeixinShare.getInstance().isRegWeixin()){
				WeixinShare.getInstance().unregisterWeixin();
			}
			if(!YixinShare.getInstance().isRegYixin()){
				YixinShare.getInstance().unregisterYixin();
			}
			
			mSsoHandler = null;
			mAccessToken = null;
			mWeiboAuth = null;
			
			if(timer != null) {
				timer.cancel();
				timer = null;
			}
		}
	};
	
	private byte[] getBitmapBytes(int resId) {
		Bitmap bitmap = BitmapFactory.decodeResource(fragment.getResources(), resId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
	}
	
	public void setShow(JSONObject jsonObject, int targetId) {
		showTitle = jsonObject.optString("title", "");
		
		JSONArray tips = jsonObject.optJSONArray("tips");
		for(int i=0; i<tips.length(); i++) {
			try {
				JSONObject tip = (JSONObject) tips.get(i);
				int currentId  = tip.optInt("id");
				if (currentId == targetId) {
					showContent = tip.optString("txt");
					return;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setShare(JSONObject jsonObject, int targetId) {
		shareTitle = jsonObject.optString("shareTitle", "");
		
		JSONArray shares = jsonObject.optJSONArray("shareTxt");
		if (shares != null) {
			for(int i=0; i<shares.length(); i++) {
				try {
					JSONObject share = (JSONObject) shares.get(i);
					int currentId  = share.optInt("id");
					if (currentId == targetId) {
						shareContent = share.optString("txt");
						shareContentWB = share.optString("wbtxt");
						shareUrl = share.optString("url");
						return;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void setShareUrl(String value) {
		// http://y.163.com/promote/sns?type=3&uid=&value=&time=
		String uid = ManagerAccount.getInstance().getCurrentIdString();
		shareUrl = shareUrl.replace("uid=", "uid="+uid);
		shareUrl = shareUrl.replace("value=", "value="+value);
		shareUrl = shareUrl.replace("time=", "time="+System.currentTimeMillis());
	}
	
	private int getLevelNameImageRes(boolean isMan, int level) {
		if (isMan) {
			return getResId("layer_upgrade_degrade_man_lv"+level);
		} else {
			return getResId("layer_upgrade_women_lv"+level);
		}
 	}
	
	private int getLevelNumImageRes(boolean isUpgrade, int level) {
		if (isUpgrade) {
			return getResId("layer_upgrade_num_lv"+level);
		} else {
			return getResId("layer_degrade_num_lv"+level);
		}
	}
	
	private int getWBImageRes(int type, int value) {
		if (type == 0) { // money
			return getResId("layer_wb_money");
		} else if (type == 1) { // gift
			return getResId("layer_wb_gift_" + value);
		} else if (type == 2) { // level man
			return getResId("layer_wb_man_level" + value);
		} else {
			return getResId("layer_wb_lady_level" + value);
		}
	}
	
	private int getResId(String s) {
		int imgId = activity.getResources().getIdentifier(s, "drawable", "com.netease.date");
		return imgId;
	}

	public SsoHandler getmSsoHandler() {
		return mSsoHandler;
	}

	public ShareDialogInterface getDelegate() {
		return delegate;
	}

	public void setDelegate(ShareDialogInterface delegate) {
		this.delegate = delegate;
	}
	
}
