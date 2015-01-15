package com.netease.engagement.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.view.HeadView;
import com.netease.engagement.view.ProfileView;
import com.netease.engagement.view.ProgressTextView;
import com.netease.engagement.widget.CustomWebView;
import com.netease.service.protocol.meta.UserInfo;

/**
 * 会员等级查看表
 */
public class FragmentLevelTable extends FragmentBase{
	
	public static FragmentLevelTable newInstance(String userInfoDetail){
		FragmentLevelTable fragment = new FragmentLevelTable();
		Bundle args = new Bundle();
		args.putString(EgmConstants.BUNDLE_KEY.SELF_PAGE_USERINFO, userInfoDetail);
		fragment.setArguments(args);
		return fragment ;
	}
	
	private UserInfo mUserInfo ;
	private static final String URL_MAN = "http://y.163.com/webviews/leveltable?sex=0" ;
	private static final String URL_GIRL = "http://y.163.com/webviews/leveltable?sex=1" ;
	
	private TextView mImageBack ;
	
	private HeadView mProfileView ;
	private TextView mNickTxt ;
	private TextView mLevelTxt ;
	private ProgressTextView mShowTxt ;
	private TextView mLevelNameTxt ;
	
	private CustomWebView  mWebView ;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
		getActivity().setTitle(getString(R.string.my_level));
		
		Bundle bundle = this.getArguments();
		if(bundle == null || bundle.getString(EgmConstants.BUNDLE_KEY.SELF_PAGE_USERINFO) == null){
			return ;
		}
		Gson gson = new Gson();
		mUserInfo = gson.fromJson(bundle.getString(EgmConstants.BUNDLE_KEY.SELF_PAGE_USERINFO), UserInfo.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		ScrollView root = (ScrollView) inflater.inflate(R.layout.fragment_level_table, container,false);
		init(root);
		return root;
	}
	
	private void init(View root){
		mImageBack = (TextView)root.findViewById(R.id.back);
		mImageBack.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				getActivity().finish();
			}
		});
		
		mProfileView = (HeadView)root.findViewById(R.id.profile);
		mProfileView.setImageUrl(mUserInfo.isVip,HeadView.PROFILE_SIZE_SMALL,mUserInfo.portraitUrl192,
				mUserInfo.sex);
		
		mNickTxt = (TextView)root.findViewById(R.id.nick);
		mNickTxt.setText(mUserInfo.nick);
		
		mLevelTxt = (TextView)root.findViewById(R.id.level);
		String levelStr = String.format(getResources().getString(R.string.level_num),mUserInfo.level);
		mLevelTxt.setText(levelStr + " " + mUserInfo.levelName);
		
		mShowTxt = (ProgressTextView)root.findViewById(R.id.show);
		if(mUserInfo.level >= 10){
			mShowTxt.setMaxLevel();
		}else if(mUserInfo.level < 10){
			mShowTxt.setProgress(mUserInfo.usercp,mUserInfo.nextUsercp);
		}
		
		mLevelNameTxt = (TextView)root.findViewById(R.id.level_name);
		mLevelNameTxt.setText(String.format(getResources().getString(R.string.level_num),mUserInfo.nextLevel));
		
		mWebView = (CustomWebView)root.findViewById(R.id.webview);
		mWebView.getWebView().requestFocus();
		mWebView.getWebView().clearCache(true);
        mWebView.getWebView().setWebViewClient(mWebViewClient);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(mWebView != null){
			switch(mUserInfo.sex){
				case EgmConstants.SexType.Male:
					mWebView.getWebView().loadUrl(URL_GIRL);
					break;
				case EgmConstants.SexType.Female:
					mWebView.getWebView().loadUrl(URL_MAN);
					break;
			}
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

