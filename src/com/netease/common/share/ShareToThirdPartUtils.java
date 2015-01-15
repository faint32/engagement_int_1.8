package com.netease.common.share;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.netease.common.image.util.ImageUtil;
import com.netease.common.log.NTLog;
import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.app.EngagementApp;
import com.netease.engagement.view.ShareDialog;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.Utils.WeixinShare;
import com.netease.service.Utils.YixinShare;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;

/**
 * @author lishang
 * 分享工具类
 * 分享对象：易信、微信、微博
 * 使用方法，ShareToThirdPartUtils.getInstance(getActivity()).shareToFriends(EgmConstants.ShareType.YiXin, msg);
 *
 */
public class ShareToThirdPartUtils {

	private static ShareToThirdPartUtils mShareToThirdPartUtils;
	private Activity mContext;

	private ShareToThirdPartUtils(Activity context) {
		mContext = context;
		init(context);
	}

    // 单例模式会引发异常，后期探索
    public static ShareToThirdPartUtils getInstance(Activity context) {
        // if (mShareToThirdPartUtils == null) {
        // synchronized (ShareToThirdPartUtils.class) {
        // if (mShareToThirdPartUtils == null)
        // {
        // mShareToThirdPartUtils = new ShareToThirdPartUtils(context);
        // }
        // }
        // }
        mShareToThirdPartUtils = new ShareToThirdPartUtils(context);
        return mShareToThirdPartUtils;
    }

	private SsoHandler mSsoHandler;
	private Oauth2AccessToken mAccessToken;
	private WeiboAuth mWeiboAuth;
	private int mTid;

	private AlertDialog mAlertDialog;
	// sina
	public static final String SINA_CLIENT_ID = "876878023";
	public static final String SINA_REDIRECT_URI = "https://api.weibo.com/oauth2/default.html";
	public static final String SCOPE = "";

	private void init(Activity context) {
		String uid = EgmPrefHelper.getUid(mContext);
		if (!TextUtils.isEmpty(uid)) {
			mAccessToken = new Oauth2AccessToken();
			mAccessToken.setUid(uid);
			mAccessToken.setToken(EgmPrefHelper.getAccessToken(mContext));
			mAccessToken.setExpiresTime(EgmPrefHelper.getExpireIn(mContext));
		}
	}
	private String copyOfMsg;
	class AuthListener implements WeiboAuthListener {

		@Override
		public void onComplete(Bundle values) {
			// 从 Bundle 中解析 Token
			mAccessToken = Oauth2AccessToken.parseAccessToken(values);
			if (mAccessToken.isSessionValid()) {
				// showToast("weibo授权成功");
                if (!TextUtils.isEmpty(copyOfMsg)) {
                    SendBlog(null, copyOfMsg, null);
                    EgmPrefHelper.putUid(mContext, mAccessToken.getUid());
                    EgmPrefHelper.putAccessToken(mContext, mAccessToken.getToken());
                    EgmPrefHelper.putExpireIn(mContext, mAccessToken.getExpiresTime());
                }
			} else {
				// 以下几种情况，您会收到 Code：
				// 1. 当您未在平台上注册的应用程序的包名与签名时；
				// 2. 当您注册的应用程序包名与签名不正确时；
				// 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
				String code = values.getString("code");
				String message = mContext.getResources().getString(
						R.string.weibo_auth_error);
				if (!TextUtils.isEmpty(code)) {
					message = message + "\nObtained the code: " + code;
				}
				showToast(message);
			}
		}

		@Override
		public void onCancel() {
		}

		@Override
		public void onWeiboException(WeiboException e) {
			showToast(mContext.getResources().getString(
					R.string.weibo_auth_exeption)
					+ e.getMessage());
		}
	}

	public SsoHandler getSsoHandler() {
		return mSsoHandler;
	}

	private void SendBlog(String title , String msg, String url) {
		if (mAccessToken != null) {
			showWatting(mContext.getResources().getString(R.string.req_waiting));
			ShareBind shareBind = new ShareBind(ShareType.Sina);
			shareBind.setUserID(mAccessToken.getUid());
			shareBind.setAccessToken(mAccessToken.getToken());
			
			mTid = ShareService.getShareService().sendMBlog(shareBind, null,
			        msg, null, null, mShareCallback);

		}
		
		
 
        
//        Bitmap img = BitmapFactory.decodeResource(activity.getResources(), shareWBImageRes);
//        String imgPath = ImageUtil.getBitmapFilePath(img, "weiboshare");
//        
//        mTid = ShareService.getShareService().sendMBlog(shareBind, null, shareContentWB, imgPath, null, mShareCallback);
    
	}

	ShareCallback mShareCallback = new ShareCallback() {

		@Override
		public void onShareMBlogSuccess(int tid, ShareResult shareResult) {
			if (tid == mTid) {
				stopWaiting();
				showToast(mContext.getString(R.string.weibo_share_success));

			}
		};

		@Override
		public void onShareMBlogError(int tid, ShareResult shareResult) {
			if (tid == mTid) {
				stopWaiting();
				if (shareResult != null
						&& !TextUtils.isEmpty(shareResult.getMessage())) {
					showToast(shareResult.getMessage());
					NTLog.e("FragmentInvite",
							"code is " + shareResult.getCode()
									+ " MessageCode is "
									+ shareResult.getMessageCode());
					if (shareResult.getMessageCode().startsWith("213")) {// 授权相关错误
						EgmPrefHelper.deleteWeiboAcc(mContext);
						mWeiboAuth = new WeiboAuth(mContext, SINA_CLIENT_ID,
								SINA_REDIRECT_URI, SCOPE);
						mSsoHandler = new SsoHandler(mContext, mWeiboAuth);
						mSsoHandler.authorize(new AuthListener());
					}
				} else {
					showToast(mContext.getString(R.string.weibo_share_error));
				}
			}

		};

	};

	public void shareToFriends(int mShareType, final String msg) {
		switch (mShareType) {
		case EgmConstants.ShareType.WeChat:

			String[] weiXin = { mContext.getString(R.string.invite_wechat_friend), 
					mContext.getString(R.string.invite_wechat_circle) };
			mAlertDialog = EgmUtil.createEgmMenuDialog(mContext, mContext.getString(R.string.share_to_wechat),
					weiXin, new OnClickListener() {
						@Override
						public void onClick(View v) {
							int tag = (Integer) v.getTag();
							switch (tag) {
							case 0:
								if (!WeixinShare.getInstance().isRegWeixin()) {
									WeixinShare.getInstance().registerWeixin(
											EngagementApp.getAppInstance());
								}
								WeixinShare.getInstance().share2SessionText(
										msg, false);
								mAlertDialog.dismiss();
								break;
							case 1:
								if (!WeixinShare.getInstance().isRegWeixin()) {
									WeixinShare.getInstance().registerWeixin(
											EngagementApp.getAppInstance());
								}
								WeixinShare.getInstance().share2SessionText(
										msg, true);
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
			String[] yinXin = {mContext.getString(R.string.invite_yixin_friend),
					mContext.getString(R.string.invite_yixin_circle) };
			
			mAlertDialog = EgmUtil.createEgmMenuDialog(mContext, mContext.getString(R.string.share_to_yixin),
					yinXin, new OnClickListener() {
						@Override
						public void onClick(View v) {
							int tag = (Integer) v.getTag();
							switch (tag) {
							case 0:
								if (!YixinShare.getInstance().isRegYixin()) {
									YixinShare.getInstance().registerYixin(
											EngagementApp.getAppInstance());
								}
								YixinShare.getInstance().share2YixinText(msg,
										false);
								mAlertDialog.dismiss();
								break;
							case 1:
								if (!YixinShare.getInstance().isRegYixin()) {
									YixinShare.getInstance().registerYixin(
											EngagementApp.getAppInstance());
								}
								YixinShare.getInstance().share2YixinText(msg,
										true);
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
                SendBlog(null, msg, null);
			} else {
				mWeiboAuth = new WeiboAuth(mContext, SINA_CLIENT_ID,
						SINA_REDIRECT_URI, SCOPE);
				mSsoHandler = new SsoHandler(mContext, mWeiboAuth);
				mSsoHandler.authorize(new AuthListener());
			}
		default:
			break;
		}
	}

	/**
	 * Toast 提示
	 * 
	 * @param message
	 */
	public void showToast(String message) {
		if (mContext != null) {
			Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
		}
	}

	private ProgressDialog mWaitingProgress;

	public void showWatting(String message) {
		showWatting(null, message);
	}

	public void showWatting(String title, String message) {
		if (mWaitingProgress != null)
			stopWaiting();

		mWaitingProgress = ProgressDialog.show(mContext, title, message, true,
				true);
	}

	public void stopWaiting() {
		if (mWaitingProgress != null) {
			mWaitingProgress.dismiss();
			mWaitingProgress = null;
		}
	}
	public void shareToThirdPartWithUrl(int type ,String title, String description, int shareWXYXImageRes, String url, boolean timeline){
	    

        switch (type) {
            case EgmConstants.ShareType.WeChat:
                if (!WeixinShare.getInstance().isRegWeixin()) {
                    WeixinShare.getInstance().registerWeixin(EngagementApp.getAppInstance());
                }
                if (timeline) {
                    WeixinShare.getInstance().share2SessionWebpage(description, description,
                            getBitmapBytes(shareWXYXImageRes), url, timeline);
                } else {
                    WeixinShare.getInstance().share2SessionWebpage(title, description,
                            getBitmapBytes(shareWXYXImageRes), url, timeline);
                }
                break;
            case EgmConstants.ShareType.YiXin:
                if (!YixinShare.getInstance().isRegYixin()) {
                    YixinShare.getInstance().registerYixin(EngagementApp.getAppInstance());
                }
                YixinShare.getInstance().share2YixinWebpage(title, description,
                        getBitmapBytes(shareWXYXImageRes), url, timeline);
                break;
            case EgmConstants.ShareType.WeiBo:
                copyOfMsg = description + url;
                if (mAccessToken != null && mAccessToken.isSessionValid()) {
                    SendBlog(null, description + url, null);
                } else {
                    mWeiboAuth = new WeiboAuth(mContext, SINA_CLIENT_ID, SINA_REDIRECT_URI, SCOPE);
                    mSsoHandler = new SsoHandler(mContext, mWeiboAuth);
                    mSsoHandler.authorize(new AuthListener());
                }
                break;
            default:
                break;
        }
    
	}
	
	   private byte[] getBitmapBytes(int resId) {
	        BitmapDrawable bitmapDrawable=(BitmapDrawable)mContext.getResources().getDrawable(resId);
	        Bitmap bitmap=bitmapDrawable.getBitmap();
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
	        return baos.toByteArray();
	    }
}
