package com.netease.common.share.bind;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.common.share.ShareService;
import com.netease.common.share.ShareType;
import com.netease.common.share.base.ShareBaseChannel;
import com.netease.common.task.TransactionListener;

public class ShareWebView extends LinearLayout {
	
	public static final int PAGELOAD_WAIT_TIME = 20000; // 20s的延迟
	
	private ShareBaseChannel mShareChannel;
	private String mRedirectPrefix;
	
	private Handler mHandler;
	
	private WebView mWebView;
	
	private LinearLayout mWaitLay;


	public ShareWebView(Context context) {
		super(context);
		init(context);
	}
	public ShareWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context) {
		mWebView = new WebView(context);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
		mWebView.setLayoutParams(lp);
		addView(mWebView);
		mWaitLay = new LinearLayout(context);
		mWaitLay.setLayoutParams(lp);
		mWaitLay.setOrientation(LinearLayout.HORIZONTAL);
		mWaitLay.setGravity(Gravity.CENTER);
		ProgressBar pb = new ProgressBar(context,null,android.R.attr.progressBarStyleSmall);
		lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		pb.setLayoutParams(lp);
		mWaitLay.addView(pb);
		TextView tv = new TextView(context);
		final float scale = context.getResources().getDisplayMetrics().density;
		lp.leftMargin = (int)(5*scale + 0.5f);
		tv.setLayoutParams(lp);
		tv.setTextSize(10*scale + 0.5f);
//		tv.setTextColor(ColorStateList.valueOf(0x666666));
		tv.setText("正在载入...");
		mWaitLay.addView(tv);
		addView(mWaitLay);
		initShareWebView();
	}
	
	@SuppressLint("SetJavaScriptEnabled") 
	private void initShareWebView() {
		mHandler = new Handler();
		WebSettings settings = mWebView.getSettings();
		settings.setJavaScriptEnabled(true);
	}
	
	public void initShareBind(ShareType shareType, TransactionListener listener) {
		ShareBaseChannel channel = ShareService.createShareChannel(shareType);
		
		channel.setShareListener(listener);
		mShareChannel = channel;
		mRedirectPrefix = channel.getRedirectPrefix();
		mWebView.setWebViewClient(mWebViewClient);
		
		String authorizeUrl = channel.getAuthorizeUrl(this);
		
		if (authorizeUrl != null) {
			mWebView.loadUrl(authorizeUrl);
		}
		viewControl(true, false);
	}
	
	WebViewClient mWebViewClient = new WebViewClient() {
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			if (mRedirectPrefix != null && url.startsWith(mRedirectPrefix)) {
				String errMsg = mShareChannel.onRedirectUrl(url);
				if (errMsg != null) {
					Toast.makeText(getContext(), errMsg, Toast.LENGTH_SHORT).show();
				}
				
				mWebView.stopLoading();
				
				viewControl(true, false);
			} else {
				super.onPageStarted(view, url, favicon);
			}
		}
		
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
//			if (getVisibility() == View.INVISIBLE) {
//				setVisibility(View.VISIBLE);
//			}
			viewControl(false, true);
		}
	};

	public void destroy() {
		mWebView.destroy();
		
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
			mHandler = null;
		}
		mWebViewClient = null;
		mShareChannel = null;
	}
	private void viewControl(boolean bWaitShow, boolean bWebViewShow){
		if(bWaitShow){
			mWaitLay.setVisibility(View.VISIBLE);
		} else {
			mWaitLay.setVisibility(View.GONE);
		}
		
		if(bWebViewShow){
			mWebView.setVisibility(View.VISIBLE);
			mWebView.requestFocus();
		} else {
			mWebView.setVisibility(View.GONE);
		}
	}
}
