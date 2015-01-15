package com.handmark.pulltorefresh.compat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.netease.date.R;


public class LoadingContainer extends FrameLayout {

    private Context mContext;
    private TextView mLoadingText;
    private TextView mLoadingErrorText;
    private TextView mNoContentText;
    private TextView mNoNetWorkText;

    // 存储各个状态的文案
    private String mLoadingString;
    private String mLoadingErrorString;
    private String mNoContentString;
    private String mNoNetWorkString;

    private OnLoadingListener mLoadingListener;
    // ---------------View-----------
    private View mLoadingView;// 正在加载视图
    private View mLoadingErrorView;// 加载出错
    private View mNoContentView;// 无内容
    private View mNoNetworkView;// 无网络
    
    private View mChild;
    private FrameLayout.LayoutParams mCenterParams;
    
    private final int TYPE_LOADING = 10;
    private final int TYPE_LOADING_ERROR = 11;
    private final int TYPE_NOCONTENT = 12;
    private final int TYPE_NONETWORK = 13;
    private final int TYPE_HIDE = 14;

    public LoadingContainer(Context context) {
        super(context);
        mContext = context;
    }
    public LoadingContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }
    public LoadingContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }
    
    public void setChild(View view){
        removeAllViews();
        mChild = view;
        
        ini();
    }
    
    public void ini(){
        mCenterParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER);
        
        addView(mChild, mCenterParams);

        mLoadingView = getLoadingView();
        mLoadingView.setLayoutParams(mCenterParams);
        mLoadingErrorView = getLoadingErrorView();
        mLoadingErrorView.setLayoutParams(mCenterParams);
        mNoContentView = getNoContentView();
        mNoContentView.setLayoutParams(mCenterParams);
        mNoNetworkView = getNoNetworkView();
        mNoNetworkView.setLayoutParams(mCenterParams);
        
        addView(mLoadingView);
        addView(mLoadingErrorView);
        addView(mNoContentView);
        addView(mNoNetworkView);
        
        setViews(TYPE_LOADING);
    }
    
    public void setLoadingView(){
        mLoadingView.setVisibility(View.VISIBLE);
    }
    
    public String getLoadingString() {
        return mLoadingString;
    }

    public String getLoadingErrorString() {
        return mLoadingErrorString;
    }

    public String getNoContentString() {
        return mNoContentString;
    }

    public String getNoNetWorkString() {
        return mNoNetWorkString;
    }

    public void setLoadingString(String mLoadingString) {
        this.mLoadingString = mLoadingString;
        mLoadingText.setText(mLoadingString);
    }

    public void setLoadingErrorString(String mLoadingErrorString) {
        this.mLoadingErrorString = mLoadingErrorString;
        mLoadingErrorText.setText(mLoadingErrorString);
    }

    public void setNoContentString(String mNoContentString) {
        this.mNoContentString = mNoContentString;
        mNoContentText.setText(mNoContentString);
    }

    public void setNoNetWorkString(String mNoNetWorkString) {
        this.mNoNetWorkString = mNoNetWorkString;
        mNoNetWorkText.setText(mNoNetWorkString);
    }

    protected View getLoadingView() {
        View v = View.inflate(mContext, R.layout.view_loading, null);
        mLoadingText = (TextView)v.findViewById(R.id.loading_text);
        mLoadingString = mContext.getString(R.string.loading_default);
        mLoadingText.setText(mLoadingString);
        return v;
    }

    protected View getLoadingErrorView() {
        View v = View.inflate(mContext, R.layout.view_load_complete, null);
        mLoadingErrorText = (TextView)v.findViewById(R.id.loading_text);
        mLoadingErrorString = mContext.getString(R.string.error_loading_default);
        mLoadingErrorText.setText(mLoadingErrorString);
        return v;
    }

    protected View getNoContentView() {
        View v = View.inflate(mContext, R.layout.view_load_complete, null);
        mNoContentText = (TextView)v.findViewById(R.id.loading_text);
        mNoContentString = mContext.getString(R.string.success_no_content_default);
        mNoContentText.setText(mNoContentString);
        return v;
    }

    protected View getNoNetworkView() {
        View v = View.inflate(mContext, R.layout.view_load_complete, null);
        mNoNetWorkText = (TextView)v.findViewById(R.id.loading_text);
        mNoNetWorkString = mContext.getString(R.string.error_no_network);
        mNoNetWorkText.setText(mNoNetWorkString);
        TextView retry = (TextView)v.findViewById(R.id.loading_text2);
        retry.setVisibility(View.VISIBLE);
        retry.setText(R.string.error_retry);
        retry.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                load();
            }
        });
        return v;
    }
    
    private void setViews(final int type) {
        switch(type){
            case TYPE_LOADING:
                mLoadingView.setVisibility(View.VISIBLE);
                mLoadingErrorView.setVisibility(View.INVISIBLE);
                mNoContentView.setVisibility(View.INVISIBLE);
                mNoNetworkView.setVisibility(View.INVISIBLE);
                mChild.setVisibility(View.INVISIBLE);
                break;
            case TYPE_LOADING_ERROR:
                mLoadingView.setVisibility(View.INVISIBLE);
                mLoadingErrorView.setVisibility(View.VISIBLE);
                mNoContentView.setVisibility(View.INVISIBLE);
                mNoNetworkView.setVisibility(View.INVISIBLE);
                mChild.setVisibility(View.INVISIBLE);
                break;
            case TYPE_NOCONTENT:
                mLoadingView.setVisibility(View.INVISIBLE);
                mLoadingErrorView.setVisibility(View.INVISIBLE);
                mNoContentView.setVisibility(View.VISIBLE);
                mNoNetworkView.setVisibility(View.INVISIBLE);
                mChild.setVisibility(View.INVISIBLE);
                break;
            case TYPE_NONETWORK:
                mLoadingView.setVisibility(View.INVISIBLE);
                mLoadingErrorView.setVisibility(View.INVISIBLE);
                mNoContentView.setVisibility(View.INVISIBLE);
                mNoNetworkView.setVisibility(View.VISIBLE);
                mChild.setVisibility(View.INVISIBLE);
                break;
            case TYPE_HIDE:
                mLoadingView.setVisibility(View.INVISIBLE);
                mLoadingErrorView.setVisibility(View.INVISIBLE);
                mNoContentView.setVisibility(View.INVISIBLE);
                mNoNetworkView.setVisibility(View.INVISIBLE);
                mChild.setVisibility(View.VISIBLE);
                break;
        }
    }
    
    public boolean isLoading(){
        return mLoadingView.getVisibility() == View.VISIBLE;
    }

    public View getChild(){
        return mChild;
    }
    public void load() {
        if(mLoadingListener == null)
            return;
        setViews(TYPE_LOADING);
        mLoadingListener.onLoading();
    }
    
    public void setOnLoadingListener(OnLoadingListener loadingListener) {
        mLoadingListener = loadingListener;
    }

    public interface OnLoadingListener {
        public void onLoading();
    }


    public void onLoadingComplete(){
        setViews(TYPE_HIDE);
    }

    public void onNoContent() {
        setViews(TYPE_NOCONTENT);
    }

    public void onLoadingError() {
        setViews(TYPE_LOADING_ERROR);
    }

    public void onNoNetwork() {
        setViews(TYPE_NONETWORK);
    }
    
}
