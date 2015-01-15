package com.netease.engagement.fragment;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.netease.date.R;
import com.netease.engagement.activity.ActivitySingleRankList;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.widget.CustomWebView;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.preferMgr.EgmPrefHelper;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmService;



public class FragmentWeb extends FragmentBase {
//    private ActivityEngagementBase mActivity;
    
    private CustomWebView mCustomWebView;
    private WebView mWebView;
    private int mExchangeTid;
    
    private String mUrl;
    private String mTicket;
    private boolean mIsNeedLogin;
    private boolean mIsNeedTitleBar = false;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        EgmService.getInstance().addListener(mEgmCallback);
        
        if(mIsNeedLogin){
            doExchangeTicket();
        }
        else{
            mWebView.loadUrl(mUrl);
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web, container, false);
        View title = view.findViewById(R.id.web_title_bar);
        if(mIsNeedTitleBar){
            title.setVisibility(View.VISIBLE);
            title.findViewById(R.id.title_title).setVisibility(View.INVISIBLE);
            title.findViewById(R.id.title_right).setVisibility(View.INVISIBLE);
            View back = title.findViewById(R.id.title_left);
            back.findViewById(R.id.title_left).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickBack();
                }
            });
        }
        else{
            title.setVisibility(View.GONE);
        }

        mCustomWebView = (CustomWebView)view.findViewById(R.id.web_content);
        mWebView = mCustomWebView.getWebView();
        
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().supportZoom();
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.contains(EgmConstants.URL_REDIRECT_BACK)) {
					clickBack();
					EgmService.getInstance().doGetPrivateData();
					return true;
				} else if (url.contains(EgmConstants.URL_REDIRECT_GOTO_STRENGTH_RANK)) {

					String urlFormat = url.replace("?", "&");
					Map<String, String> rankInfo = parseUrlParams(urlFormat);
					
					// 此部分代码不完善，性别、日/月榜单都还没有兼容
					switch (Integer.parseInt(rankInfo.get("rankId"))) {
					case EgmConstants.RankID.STRENGTH_MALE:
						ActivitySingleRankList.startActivity(getActivity(), EgmConstants.RankID.STRENGTH_MALE,
								getResources().getString(R.string.rank_name_strength_male), EgmConstants.SexType.Male);
						clickBack();
						break;
					default:
						break;
					}

				}
                
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                mCustomWebView.setLoadedState();
            }
        });
        
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
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
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                b.setTitle("确认");
                b.setMessage(message);
                b.setPositiveButton("确认",
                        new AlertDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        });
                b.setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
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
        
        return view;
    }
    
    @Override
    public void onDestroy(){
        super.onDestroy();
        EgmService.getInstance().removeListener(mEgmCallback);
        
        // 退出webview需要清除cookie，否则另外一个用户进来会显示上一个用户的信息
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        
        if(mWebView != null){
            mWebView.destroy();
        }
    }
    
    public void setUrl(String url, boolean needLogin, boolean needTitle){
        mUrl = url;
        mIsNeedLogin = needLogin;
        mIsNeedTitleBar = needTitle;
    }
    
    private void doExchangeTicket(){
        showWatting(getActivity().getString(R.string.common_tip_is_waitting));
        
        mExchangeTid = EgmService.getInstance().doExchangeTicketFromUrs(EgmPrefHelper.getURSId(getActivity()), 
                EgmPrefHelper.getURSKey(getActivity()), 
                ManagerAccount.getInstance().getCurrentAccountToken());//EgmPrefHelper.getURSToken(getActivity()));
    }
    
    private String getUrl(){
        if(!mIsNeedLogin){
            return mUrl;
        }
        
        StringBuilder builder = new StringBuilder();
        builder.append(EgmConstants.URL_URS_TICKET_LOGIN).append("?")
            .append("ticket=").append(mTicket).append("&")
            .append("url=").append(mUrl).append("&")
            .append("product=").append(EgmProtocolConstants.PRODUCT).append("&")
            .append("domains=").append("163.com");
        
        return builder.toString();
    }
    
    private EgmCallBack mEgmCallback = new EgmCallBack(){
        /** 置换ticket */
        @Override
        public void onExchangeTicket(int transactionId, String ticket){
            if(mExchangeTid != transactionId)
                return;
            
            stopWaiting();
            mTicket = ticket;
            
            String url = getUrl();
            if(!TextUtils.isEmpty(url)){
                mCustomWebView.setLoadingState();
                mWebView.loadUrl(url);
            }
        }
        @Override
        public void onExchangeTicketError(int transactionId, int errCode, String err){
            if(mExchangeTid != transactionId)
                return;
            
            stopWaiting();
            showToast(err);
        }
    };

	public static Map<String, String> parseUrlParams(String encodeData) {
		Map<String, String> map = new HashMap<String, String>();

		StringTokenizer tokenizer = new StringTokenizer(encodeData, "&");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			String value = null;

			int index = token.indexOf('=');
			if (index > 0) {
				value = token.substring(index + 1);
				token = token.substring(0, index);

				map.put(URLDecoder.decode(token), URLDecoder.decode(value));
			}
		}
		return map;
	}
}
