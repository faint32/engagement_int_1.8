package com.netease.engagement.widget;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class CustomWebView extends RelativeLayout{

	public CustomWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public CustomWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CustomWebView(Context context) {
		super(context);
		init(context);
	}
	
	private ProgressBar mProgressBar ;
	private WebView mWebView ;
	
	private void init(final Context context){
	    mWebView = new WebView(context);
        RelativeLayout.LayoutParams lpa = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        addView(mWebView,lpa);
        
        // 下载监听
        mWebView.setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                    String mimetype, long contentLength) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    context.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        
        mProgressBar = new ProgressBar(context);
        mProgressBar.setVisibility(View.GONE);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(mProgressBar,lp);
	}
	
	public WebView getWebView(){
		return mWebView ;
	}

	public void setLoadingState(){
		mProgressBar.setVisibility(View.VISIBLE);
	}
	
	public void setLoadedState(){
		mProgressBar.setVisibility(View.GONE);
		mWebView.setVisibility(View.VISIBLE);
	}
}
