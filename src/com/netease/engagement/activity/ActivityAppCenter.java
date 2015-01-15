
package com.netease.engagement.activity;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.engagement.widget.CustomWebView;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.app.BaseApplication;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmProtocolConstants;

/**
 * @author lishang 百宝箱
 * 1、1.4版本跳转到第三方浏览器下载
 * 2、预留本地下载入口，方便后期扩展 
 * 3、预留利用广播实现自动安装，方便后期扩展
 */

public class ActivityAppCenter extends ActivityEngagementBase {

    /**
     * 自定义WebView
     */
    private CustomWebView mWebView;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ActivityAppCenter.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setCustomActionBar();
        mWebView = new CustomWebView(this);
        mWebView.getWebView().requestFocus();
        mWebView.getWebView().clearCache(true);
        mWebView.getWebView().getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getWebView().getSettings().setBlockNetworkImage(false);
        mWebView.getWebView().getSettings().setLoadsImagesAutomatically(true);
        mWebView.getWebView().getSettings().setJavaScriptEnabled(true);
        mWebView.getWebView().setWebViewClient(mWebViewClient);
        mWebView.getWebView().loadUrl(getAppCenterUrl());
        mWebView.getWebView().setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                    String mimetype, long contentLength) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });
        setContentView(mWebView);

        CustomActionBar customActionBar = getCustomActionBar();
        customActionBar.getCustomView().setBackgroundColor(
                getResources().getColor(R.color.pri_info_choice_title_color));
        customActionBar.setLeftBackgroundResource(R.drawable.titlebar_a_selector);
        customActionBar.setLeftTitleColor(getResources().getColor(R.color.purple_dark));
        customActionBar.setLeftAction(R.drawable.bar_btn_back_a, R.string.back);
        customActionBar.setMiddleTitleColor(getResources().getColor(R.color.black));
        customActionBar.setMiddleTitle(R.string.setting_app_center);
        customActionBar.setMiddleTitleSize(20);
        customActionBar.hideRightTitle();
        customActionBar.setLeftClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mWebView.getWebView().canGoBack()) {
                    mWebView.getWebView().goBack();// 返回前一个页面
                } else {
                    finish();
                }
            }
        });
    }

    private WebViewClient mWebViewClient = new WebViewClient() {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            // if (url.endsWith(".apk")) // 预留下载入口与多级菜单入口
            {
                // downLoadApk(url);// 下载处理
            }
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
            if (mWebView != null) {
                mWebView.setLoadedState();
            }
        };

        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            return super.shouldOverrideKeyEvent(view, event);
        }
    };

    /**
     * 本地WebView下载逻辑
     * @param apkUrl
     */
    private void downLoadApk(final String apkUrl) {
        Runnable mRunnable = new Runnable() {

            private String url = apkUrl;

            @Override
            public void run() {
                try {
                    URL u = new URL(apkUrl);
                    URLConnection conn = null;
                    conn = u.openConnection();
                    ((HttpURLConnection)conn).setInstanceFollowRedirects(false);
                    int code = ((HttpURLConnection)conn).getResponseCode();
                    if (code == 301 || code == 302 || code == 303) {
                        url = conn.getHeaderField("Location");
                    }
                    File downLoadDir = new File("netease/download");
                    if ((!downLoadDir.exists() || !downLoadDir.isDirectory()))
                        downLoadDir.mkdirs();
                    DownloadManager downloadManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(downLoadDir.getPath(),
                            getApkName(url));
                    request.allowScanningByMediaScanner();// 表示允许MediaScanner扫描到这个文件，默认不允许。
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setVisibleInDownloadsUi(true);
                    long downloadId = downloadManager.enqueue(request);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(mRunnable).start();
    }

    /**
     * @param url 根据302重定位的url，解析Apk名称
     * @return Apk名称
     */
    String getApkName(String url) {
        int position = url.lastIndexOf('/');
        return url.substring(position);
    }

    /*
     * 点击物理返回键，如果加载多个html，返回上一个html
     */
    @Override
    public void onBackPressed() {
        if (mWebView.getWebView().canGoBack()) {
            mWebView.getWebView().goBack();// 返回前一个页面
        } else {
            super.onBackPressed();
        }
    }

    private String getAppCenterUrl() {
        return EgmProtocol.SERVER_DOMAIN + "recommend/apps?platform="
                + EgmProtocolConstants.PLATFORM_ANROID + "&channel="
                + EgmUtil.getAppChannelID(BaseApplication.getAppInstance());
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.getWebView().setVisibility(View.GONE);
        mWebView.getWebView().destroy();
    }
}
