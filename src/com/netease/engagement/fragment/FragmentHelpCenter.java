package com.netease.engagement.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.engagement.widget.CustomWebView;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.db.manager.ManagerAccount.Account;



public class FragmentHelpCenter extends FragmentBase {
    private static final String URL_FEMALE = "http://y.163.com/views/webviews/helper/datehelpfemale.html";
    private static final String URL_MALE = "http://y.163.com/views/webviews/helper/datehelpmale.html";
    private CustomWebView  mWebView ;
    
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        CustomActionBar customActionBar = ((ActivityEngagementBase)getActivity()).getCustomActionBar();
        customActionBar.getCustomView().setBackgroundColor(getResources().getColor(R.color.pri_info_choice_title_color));
        customActionBar.setLeftBackgroundResource(R.drawable.titlebar_a_selector);
        customActionBar.setLeftTitleColor(getResources().getColor(R.color.purple_dark));
        customActionBar.setLeftAction(R.drawable.bar_btn_back_a, R.string.back);
        customActionBar.setMiddleTitleColor(getResources().getColor(R.color.black));
        customActionBar.setMiddleTitle(R.string.setting_helpcenter);
        customActionBar.setMiddleTitleSize(20);
        customActionBar.hideRightTitle();
        customActionBar.setLeftClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mWebView = new CustomWebView(getActivity());
        mWebView.getWebView().requestFocus();
        mWebView.getWebView().clearCache(true);
        mWebView.getWebView().getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE); 
        mWebView.getWebView().setWebViewClient(mWebViewClient);
        int gender = ManagerAccount.getInstance().getCurrentGender() ;
        if(gender == EgmConstants.SexType.Female){
            mWebView.getWebView().loadUrl(URL_FEMALE);
        } else {
            mWebView.getWebView().loadUrl(URL_MALE);
        }
        return mWebView;
    }
    @Override
    public void onResume() {
        super.onResume();
    }
    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);
        };
        
        @Override
        public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
            if (mWebView != null) {
                mWebView.setLoadingState();
            }
        };
        
        @Override
        public void onPageFinished(WebView view, String url) {
            if(mWebView != null){
                mWebView.setLoadedState();
            }
        };
        
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        };
    };

}
