package com.netease.engagement.fragment;

import java.util.StringTokenizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.activity.ActivityHome;
import com.netease.engagement.activity.ActivityRegisterDetail;
import com.netease.engagement.activity.ActivityWelcome;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.app.EgmConstants.BUNDLE_KEY;
import com.netease.engagement.app.EngagementApp;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.engagement.widget.CustomWebView;
import com.netease.service.Utils.EgmLocationManager;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.LoginUserInfo;
import com.netease.util.EnctryUtil;

public class FragmentExternalLogin extends FragmentBase {
	private CustomWebView mCustomWebView;
	private WebView mWebView;
	private String mToken = "";
	private String mUserName = "";
	private int mAccountType ;
	/** 登录后获取用户信息的Transaction id */
    private int mGetUserTid;
	 /** 定位 */
    private EgmLocationManager mLocation;
    private final int REQUEST_CODE_SELECT_SEX = 1;//选择性别
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 Intent intent = getActivity().getIntent();
        if(intent != null) {
        		mAccountType = intent.getIntExtra(BUNDLE_KEY.EXTERNAL_LOGIN_ACC_TYPE, EgmProtocolConstants.AccountType.YiXin);
        		Log.e("FragmentExternalLogin", "intent acctype" + mAccountType);
        }
        EgmService.getInstance().addListener(mEgmCallback);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    		super.onActivityCreated(savedInstanceState);
    		mLocation = new EgmLocationManager(getActivity());
    		initTitle();
    }
	private void initTitle(){
        CustomActionBar customActionBar = ((ActivityEngagementBase)getActivity()).getCustomActionBar();
        customActionBar.getCustomView().setBackgroundColor(getResources().getColor(R.color.pri_info_choice_title_color));
        customActionBar.setLeftAction(R.string.back);
        customActionBar.setMiddleTitleColor(getResources().getColor(R.color.black));
        if(mAccountType == EgmProtocolConstants.AccountType.YiXin){
        		customActionBar.setMiddleTitle(R.string.login_yixin);
        }
        customActionBar.setMiddleTitleSize(20);
        customActionBar.hideRightTitle();
        customActionBar.setLeftClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                getActivity().finish();
                InputMethodManager im = (InputMethodManager)getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                if (im.isActive() && getActivity().getCurrentFocus() != null) {
                    im.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mCustomWebView = new CustomWebView(getActivity());
		initView();
		return mCustomWebView;
	}

	private void initView() {
		mWebView = mCustomWebView.getWebView();
//		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		mWebView.getSettings().supportZoom();
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		mWebView.getSettings().setUseWideViewPort(true);
		mWebView.getSettings().setLoadWithOverviewMode(true);

		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				 if (mCustomWebView != null) {
					 mCustomWebView.setLoadingState();
		            }
				if (url.contains("result")) {
					if (parseUrsResult(url)) {
//						showToast("授权成功，正在登录");
						doGetUserInfo();
					} else {
						showToast("授权失败，请重新授权");
					}
				}
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				if (mCustomWebView != null) {
					mCustomWebView.setLoadedState();
				}
			}
		});

		mWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onReceivedTitle(WebView view, String title) {
			}

			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					final JsResult result) {
				AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
				b.setTitle("警告");
				b.setMessage(message);
				b.setPositiveButton("确认", new AlertDialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						result.confirm();
					}
				});
				b.setCancelable(true);
				b.create();
				b.show();

				return true;
			};

			@Override
			public boolean onJsConfirm(WebView view, String url,
					String message, final JsResult result) {
				AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
				b.setTitle("确认");
				b.setMessage(message);
				b.setPositiveButton("确认", new AlertDialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						result.confirm();
					}
				});
				b.setNegativeButton("取消",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								result.cancel();
							}
						});
				b.setCancelable(true);
				b.create();
				b.show();

				return true;
			};

			@Override
			public void onProgressChanged(WebView view, int progress) {

			}
		});
		 doLoad();
	}
	private void doLoad(){
		String ursId = EgmPrefHelper.getURSId(getActivity());
		if (TextUtils.isEmpty(ursId)) { // 还未初始化URS
			EgmService.getInstance().addListener(mEgmCallback);
			EgmService.getInstance().doInitURS();
		} else {
			doLoad(ursId);
		}
	}
	private void doLoad(String ursId){
		int target = EgmProtocolConstants.EXTERNAL_LOGIN_TYPE.yixin_open;
		switch (mAccountType)
		{
		  case EgmProtocolConstants.AccountType.YiXin:
			  
			  break;
		  default:
			  showToast("登录类型错误");
			  getActivity().finish();
			  return;
			 
		}
		 String url = EgmConstants.URL_EXTERNAL_LOGIN + "?" + 
					"target=" + target + "&id=" + ursId + 
					"&product=" + EgmProtocolConstants.PRODUCT + "&display=mobile";
		 if(mWebView != null){
			 mWebView.loadUrl(url);
		 }
	}
	private void doGetUserInfo(){
		showWatting(getString(R.string.reg_tip_logining));
        mGetUserTid = EgmService.getInstance().doLoginGetUserInfo(mUserName, "", mToken, mAccountType, 
        mLocation.mLatitude > 0 ? String.valueOf(mLocation.mLatitude) : null, 
        mLocation.mLongtitude > 0 ? String.valueOf(mLocation.mLongtitude) : null, 
        mLocation.mProvinceCode > 0 ? String.valueOf(mLocation.mProvinceCode) : null, 
        mLocation.mCityCode > 0 ? String.valueOf(mLocation.mCityCode) : null, 
        mLocation.mDistrictCode > 0 ? String.valueOf(mLocation.mDistrictCode) : null);
    }
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK || data == null){
        	   getActivity().finish();
            return;
        }
        
        if(requestCode == REQUEST_CODE_SELECT_SEX){
            int sexType = EgmConstants.SexType.Female;
            Bundle extra = data.getExtras();
            
            // 从选性别界面获取性别后再跳去注册界面
            if(extra != null){
                sexType = extra.getInt(ActivityWelcome.EXTRA_SEX_RESULT);
                gotoRegisterDetail(sexType);
                getActivity().finish();
            }
        }
    }

	@Override
	public void onDestroy() {
		super.onDestroy();

		EgmService.getInstance().removeListener(mEgmCallback);
		mLocation.stop();
		   // 退出webview需要清除cookie，否则另外一个用户进来会显示上一个用户的信息
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        
        if(mWebView != null){
            mWebView.destroy();
        }
	}
	/** 进入推荐页面 */
    private void gotoRecommend(int portraitStatus){
        ActivityHome.startActivity(getActivity(), false, portraitStatus);
        getActivity().finish();
    }
    
    /** 进入选择性别界面（欢迎页） */
    private void gotoSelectSex(){
        ActivityWelcome.startActivityForResult(this, REQUEST_CODE_SELECT_SEX, ActivityWelcome.TYPE_LOGIN);
    }
    /** 前往补充资料界面 */
    private void gotoRegisterDetail(int sexType){
        ActivityRegisterDetail.startActivity(getActivity(), 
                sexType, mUserName, "", mAccountType, true);
    }
    
	 private EgmCallBack mEgmCallback = new EgmCallBack(){
        @Override
        public void onLoginGetUserInfo(int transactionId, LoginUserInfo loginUserInfo){
            if(transactionId != mGetUserTid)
                return;
            
            stopWaiting();
            showToast(R.string.reg_tip_login_success);
            
            gotoRecommend(loginUserInfo.userInfo.portraitStatus);
        }
        @Override
        public void onLoginGetUsrInfoError(int transactionId, int errCode, String err){
            if(transactionId != mGetUserTid)
                return;
            
            stopWaiting();
            switch(errCode){
                case EgmServiceCode.TRANSACTION_COMMON_EXTERNAL_REGISTER_NOT_FINISH:
                	    if(EngagementApp.getAppInstance().getGender() < 0){
                	    		gotoSelectSex();
                	    		 return;
                	    } else {
                	    	 	gotoRegisterDetail(EngagementApp.getAppInstance().getGender());
                	    }
                	    break;
                case EgmServiceCode.TRANSACTION_COMMON_NOT_REGISTER:
                    showToast(R.string.reg_tip_no_register_error);
                    break;
                case EgmServiceCode.TRANSACTION_COMMON_USER_BLOCK:
                		showToast(err);
                    break;
                default:
                    showToast(err);
                    break;
            }
            getActivity().finish();
        }
        
    };

	private boolean parseUrsResult(String url) {
		String encrypted = "";
		StringTokenizer st = new StringTokenizer(url, "?", false);

		while (st.hasMoreElements()) {
			String ss = st.nextToken();
			if (ss.contains("=")) {
				StringTokenizer st1 = new StringTokenizer(ss, "=", false);
				String k = st1.nextToken();
				String v = st1.nextToken();
				if ("result".equalsIgnoreCase(k)) {
					encrypted = v;
				}
			}
		}

		String decrypt = "";
		if (TextUtils.isEmpty(encrypted)) {
			return false;
		} else {
			String key = EgmPrefHelper.getURSKey(getActivity());
			try {
				byte[] tkey = EgmUtil.AEStoByte(key);
				byte[] src = EgmUtil.AEStoByte(encrypted);
				decrypt = EnctryUtil.decryptForAES(src, tkey);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (TextUtils.isEmpty(decrypt)) {
			return false;
		} else {
			StringTokenizer stringTokenizer = new StringTokenizer(decrypt, "&",
					false);
			while (stringTokenizer.hasMoreElements()) {
				String str = stringTokenizer.nextToken();
				if (str.contains("=")) {
					StringTokenizer stringTokenizer2 = new StringTokenizer(str,
							"=", false);
					String k = stringTokenizer2.nextToken();
					String v = stringTokenizer2.nextToken();
					if (k.equalsIgnoreCase("username")) {
						mUserName = v;
					} else if (k.equalsIgnoreCase("token")) {
						mToken = v;
					}
				}
			}

			if (!TextUtils.isEmpty(mToken)) {
				EgmProtocol.getInstance().setUrsToken(mToken);
				return true;
			} else {
				return false;
			}
		}
	}
}
