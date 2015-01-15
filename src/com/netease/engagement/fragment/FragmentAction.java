package com.netease.engagement.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.engagement.widget.CustomWebView;

public class FragmentAction extends FragmentBase{
	
	public static FragmentAction newInstance(String url,String title){
		FragmentAction fragment = new FragmentAction();
		Bundle bundle = new Bundle();
		bundle.putString(EgmConstants.BUNDLE_KEY.SYSTEM_ACTION_URL, url);
		bundle.putString(EgmConstants.BUNDLE_KEY.SYSTEM_ACTION_TITLE, title);
		fragment.setArguments(bundle);
		return fragment ;
	}

	private CustomWebView  mWebView ;
	
	private String url ;
	private String title;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(this.getArguments() != null){
			url = this.getArguments().getString(EgmConstants.BUNDLE_KEY.SYSTEM_ACTION_URL);
			title = this.getArguments().getString(EgmConstants.BUNDLE_KEY.SYSTEM_ACTION_TITLE);
		}
	}
	
	private void initTitle(){
        CustomActionBar customActionBar = ((ActivityEngagementBase)getActivity()).getCustomActionBar();
        customActionBar.getCustomView().setBackgroundColor(getResources().getColor(R.color.pri_info_choice_title_color));
        customActionBar.setLeftAction(R.drawable.button_back_circle_selector,null);
        customActionBar.setMiddleTitleColor(getResources().getColor(R.color.black));
        if(!TextUtils.isEmpty(title)){
            customActionBar.setMiddleTitle(title);
        } else {
            customActionBar.hideMiddleTitle(); 
        }
        customActionBar.setMiddleTitleSize(20);
        customActionBar.hideRightTitle();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout root = (LinearLayout) inflater.inflate(R.layout.fragment_action_layout,container,false);
		mWebView = (CustomWebView) root.findViewById(R.id.webview);
		mWebView.getWebView().requestFocus();
		mWebView.getWebView().clearCache(true);
        mWebView.getWebView().setWebViewClient(mWebViewClient);
		return root ;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initTitle();
		if(mWebView != null){
			mWebView.getWebView().loadUrl(url);
		}
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
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mWebView != null){
			mWebView = null ;
		}
		if(mWebViewClient != null){
			mWebViewClient = null ;
		}
	}
}
