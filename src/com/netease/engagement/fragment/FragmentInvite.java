package com.netease.engagement.fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.netease.common.log.NTLog;
import com.netease.common.share.ShareBind;
import com.netease.common.share.ShareCallback;
import com.netease.common.share.ShareResult;
import com.netease.common.share.ShareService;
import com.netease.common.share.ShareType;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.app.EngagementApp;
import com.netease.engagement.dataMgr.TopicDataManager;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.Utils.WeixinShare;
import com.netease.service.Utils.YixinShare;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.db.manager.ManagerAccount.Account;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;



public class FragmentInvite extends FragmentBase implements OnClickListener{
    private final String KEY_SHARE_TYPE = "key_share_type";
    private final String KEY_TAB_LEFT = "key_tab_left";
    
    private TextView mTabFemale;
    private TextView mTabMale; 
    private TextView mWechat;
    private TextView mYixin;
    private TextView mWeibo;
    private TextView mText;
    
    private View mInviteTipLay;
    private int mGender;
    private String mTextLeft;
    private String mTextRight;
    private int mShareType = EgmConstants.ShareType.WeChat;
    private boolean mIsLeft = true;
    
    private SsoHandler mSsoHandler;
    private Oauth2AccessToken mAccessToken;
    private WeiboAuth mWeiboAuth;
    private int mTid;
    
  private AlertDialog mAlertDialog;  
    // sina
    public static final String SINA_CLIENT_ID = "876878023";
    public static final String SINA_REDIRECT_URI = "https://api.weibo.com/oauth2/default.html";
    public static final String SCOPE = "";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     // 恢复状态
        if(savedInstanceState != null){
            mShareType = savedInstanceState.getInt(KEY_SHARE_TYPE);
            mIsLeft = savedInstanceState.getBoolean(KEY_TAB_LEFT);
        }
        mGender = ManagerAccount.getInstance().getCurrentGender() ; 
        String uid = EgmPrefHelper.getUid(getActivity());
        if(!TextUtils.isEmpty(uid)){
            mAccessToken = new Oauth2AccessToken();
            mAccessToken.setUid(uid);
            mAccessToken.setToken(EgmPrefHelper.getAccessToken(getActivity()));
            mAccessToken.setExpiresTime(EgmPrefHelper.getExpireIn(getActivity()));
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putInt(KEY_SHARE_TYPE, mShareType);
        outState.putBoolean(KEY_TAB_LEFT, mIsLeft);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        CustomActionBar customActionBar = ((ActivityEngagementBase)getActivity()).getCustomActionBar();
        customActionBar.getCustomView().setBackgroundColor(getResources().getColor(R.color.pri_info_choice_title_color));
//        customActionBar.setLeftAction(R.drawable.button_back_circle_selector,null);
        customActionBar.setMiddleTitleColor(getResources().getColor(R.color.black));
        customActionBar.setMiddleTitle(R.string.invite_title);
        customActionBar.setMiddleTitleSize(20);
        customActionBar.hideRightTitle();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invite, container, false);
        initViews(view);
        
        return view;
    }
    
    private void initViews(View view){
        mInviteTipLay = view.findViewById(R.id.invite_tip_content);
        mTabFemale = (TextView)view.findViewById(R.id.btn_invite_femal);
        mTabMale = (TextView)view.findViewById(R.id.btn_invite_male);
        mWechat = (TextView)view.findViewById(R.id.icon_wechat);
        mYixin = (TextView)view.findViewById(R.id.icon_yixin);
        mWeibo = (TextView)view.findViewById(R.id.icon_weibo);
        if(mShareType == EgmConstants.ShareType.WeChat){
            mWechat.setSelected(true);
        } else if(mShareType == EgmConstants.ShareType.YiXin){
            mYixin.setSelected(true);
        } else{
            mWeibo.setSelected(true);
        }
        
        mText=new TextView(getActivity());
        
        mTabFemale.setOnClickListener(this);
        mTabMale.setOnClickListener(this);
        mWechat.setOnClickListener(this);
        mYixin.setOnClickListener(this);
        mWeibo.setOnClickListener(this);

        
        String uid = ManagerAccount.getInstance().getCurrentAccountId();
        JSONObject jsonObject = TopicDataManager.getInstance().getShareText("invite");
        String s[] = new String[4];
        JSONArray array = jsonObject.optJSONArray("shareTxt");
        if (array!=null && array.length()==4) {
        	for(int i=0; i<array.length(); i++) {
        		JSONObject json = array.optJSONObject(i);
        		s[i] = json.optString("txt") + json.optString("url");
        		if (i == 0) {
        			s[0] = s[0].replace("REPLACE", uid);
        		}
        	}
        } else { // 配置文件没有的时候的默认值，理论上不会使用的
        	s[0] = "有没用过【女神来了】？我正在玩，发了几张照片和聊天马上就有收入了。你注册填我的邀请码：REPLACE就有10元分给你 。很小的不耗流量。http://y.163.com/download/package?from=invite";
        	s[1] = "我最近在玩【女神来了】，第一次拍这么大胆的照片放上去呢，过几天就删啦，想看赶紧下一个来一起玩啊，以后在上面陪你聊天！http://y.163.com/download/package?from=invite";
        	s[2] = "我最近在玩【女神来了】，里面好多没你漂亮的女生都被很多土豪追捧，收到很多礼物，你快点下一个把她们PK下去吧。http://y.163.com/download/package?from=invite";
        	s[3] = "我最近在玩【女神来了】，里面美女好多，很开放。而且私密照片都很大胆，你懂的！http://y.163.com/download/package?from=invite";
        	
        	s[0] = s[0].replace("REPLACE", uid);
        }
        
        if(mGender == EgmConstants.SexType.Female){
            mInviteTipLay.setVisibility(View.VISIBLE);
            mTextLeft = s[0];
            mTextRight = s[1];
        } else {
            mInviteTipLay.setVisibility(View.GONE);
            mTextLeft = s[2];
            mTextRight = s[3];
        }
        mText.setText(mTextLeft);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id){
            case R.id.btn_invite_femal:
                mIsLeft = true;
                mText.setText(mTextLeft);
                shareToFriends(mShareType);
                break;
            case R.id.btn_invite_male:
                mIsLeft = false;
                mText.setText(mTextRight);
                shareToFriends(mShareType);
                break;
            case R.id.icon_wechat:
                mShareType = EgmConstants.ShareType.WeChat;
                mWechat.setSelected(true);
                mYixin.setSelected(false);
                mWeibo.setSelected(false);
                break;
            case R.id.icon_yixin:
                mShareType = EgmConstants.ShareType.YiXin;
                mWechat.setSelected(false);
                mYixin.setSelected(true);
                mWeibo.setSelected(false);
                break;
            case R.id.icon_weibo:
                mShareType = EgmConstants.ShareType.WeiBo;
                mWechat.setSelected(false);
                mYixin.setSelected(false);
                mWeibo.setSelected(true);
                break;
        }
        
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        WeixinShare.getInstance().unregisterWeixin();
        YixinShare.getInstance().unregisterYixin();
    }
   class AuthListener implements WeiboAuthListener {
        
        @Override
        public void onComplete(Bundle values) {
            // 从 Bundle 中解析 Token
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            if (mAccessToken.isSessionValid()) {
//                showToast("weibo授权成功");
                SendBlog();
                EgmPrefHelper.putUid(getActivity(), mAccessToken.getUid());
                EgmPrefHelper.putAccessToken(getActivity(), mAccessToken.getToken());
                EgmPrefHelper.putExpireIn(getActivity(), mAccessToken.getExpiresTime());
            } else {
                // 以下几种情况，您会收到 Code：
                // 1. 当您未在平台上注册的应用程序的包名与签名时；
                // 2. 当您注册的应用程序包名与签名不正确时；
                // 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
                String code = values.getString("code");
                String message = getResources().getString(R.string.weibo_auth_error);
                if (!TextUtils.isEmpty(code)) {
                    message = message + "\nObtained the code: " + code;
                }
                showToast(message);
            }
        }

        @Override
        public void onCancel() {
//            showToast("取消授权");
        }

        @Override
        public void onWeiboException(WeiboException e) {
            showToast(getResources().getString(R.string.weibo_auth_exeption) + e.getMessage());
        }
    }
   public SsoHandler getSsoHandler() {
       return mSsoHandler;
   }
   
   private void SendBlog(){
       if(mAccessToken != null){
           showWatting(getResources().getString(R.string.req_waiting));
           ShareBind shareBind = new ShareBind(ShareType.Sina);
           shareBind.setUserID(mAccessToken.getUid());
           shareBind.setAccessToken(mAccessToken.getToken());
           mTid = ShareService.getShareService().sendMBlog(shareBind, null, mText.getText().toString().trim(), null, null, mShareCallback);
       }
   }
   
   ShareCallback mShareCallback = new ShareCallback() {
       
       @Override
       public void onShareMBlogSuccess(int tid, ShareResult shareResult) {
           if (tid == mTid) {
               stopWaiting();
               showToast(R.string.weibo_share_success);
               
           }
       };
       
       @Override
       public void onShareMBlogError(int tid, ShareResult shareResult) {
           if (tid == mTid) {
               stopWaiting();
               if (shareResult != null && !TextUtils.isEmpty(shareResult.getMessage())) {
                   showToast(shareResult.getMessage());
                   NTLog.e("FragmentInvite","code is "+ shareResult.getCode() + " MessageCode is " + shareResult.getMessageCode());
                   if(shareResult.getMessageCode().startsWith("213")){//授权相关错误
                       EgmPrefHelper.deleteWeiboAcc(getActivity());
                       mWeiboAuth = new WeiboAuth(getActivity(), SINA_CLIENT_ID, SINA_REDIRECT_URI, SCOPE);
                       mSsoHandler = new SsoHandler(getActivity(), mWeiboAuth);
                       mSsoHandler.authorize(new AuthListener());
                   }
               } else {
                   showToast(R.string.weibo_share_error);
               }
           }
           
       };
       
   };

	public void shareToFriends(int mShareType) {
		switch (mShareType) {
		case EgmConstants.ShareType.WeChat:

			String[] weiXin = { "邀请微信好友", "分享到微信朋友圈" };
			mAlertDialog = EgmUtil.createEgmMenuDialog(getActivity(), "分享到微信", weiXin, new OnClickListener() {
				@Override
				public void onClick(View v) {
					int tag = (Integer) v.getTag();
					switch (tag) {
					case 0:
						if (!WeixinShare.getInstance().isRegWeixin()) {
							WeixinShare.getInstance().registerWeixin(EngagementApp.getAppInstance());
						}
						WeixinShare.getInstance().share2SessionText(mText.getText().toString().trim(), false);
						mAlertDialog.dismiss();
						break;
					case 1:
						if (!WeixinShare.getInstance().isRegWeixin()) {
							WeixinShare.getInstance().registerWeixin(EngagementApp.getAppInstance());
						}
						WeixinShare.getInstance().share2SessionText(mText.getText().toString().trim(), true);
						mAlertDialog.dismiss();
						break;
					default:
						break;
					}
				}
			});
			mAlertDialog.setCancelable(true);
			mAlertDialog.setCanceledOnTouchOutside(true);
			mAlertDialog.show();
			break;
		case EgmConstants.ShareType.YiXin:
			String[] yinXin = { "邀请易信好友", "分享到易信朋友圈" };
			mAlertDialog = EgmUtil.createEgmMenuDialog(getActivity(), "分享到易信", yinXin, new OnClickListener() {
				@Override
				public void onClick(View v) {
					int tag = (Integer) v.getTag();
					switch (tag) {
					case 0:
						if (!YixinShare.getInstance().isRegYixin()) {
							YixinShare.getInstance().registerYixin(EngagementApp.getAppInstance());
						}
						YixinShare.getInstance().share2YixinText(mText.getText().toString().trim(), false);
						mAlertDialog.dismiss();
						break;
					case 1:
						if (!YixinShare.getInstance().isRegYixin()) {
							YixinShare.getInstance().registerYixin(EngagementApp.getAppInstance());
						}
						YixinShare.getInstance().share2YixinText(mText.getText().toString().trim(), true);
						mAlertDialog.dismiss();
						break;
					default:
						break;
					}
				}
			});
			mAlertDialog.setCancelable(true);
			mAlertDialog.setCanceledOnTouchOutside(true);
			mAlertDialog.show();
			break;
		case EgmConstants.ShareType.WeiBo:

			if (mAccessToken != null && mAccessToken.isSessionValid()) {
				SendBlog();
			} else {
				mWeiboAuth = new WeiboAuth(getActivity(), SINA_CLIENT_ID, SINA_REDIRECT_URI, SCOPE);
				mSsoHandler = new SsoHandler(getActivity(), mWeiboAuth);
				mSsoHandler.authorize(new AuthListener());
			}
		default:
			break;
		}
	}
}
