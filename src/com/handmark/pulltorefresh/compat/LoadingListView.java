
package com.handmark.pulltorefresh.compat;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.netease.date.R;

public class LoadingListView extends PullToRefreshListView {

    private Context mContext;
    private TextView mLoadingText;
    private TextView mLoadingMoreText;
    private TextView mLoadingErrorText;
    private TextView mNoContentText;
    private TextView mNoContentActionText;
    private TextView mNoNetWorkText;

    // 存储各个状态的文案
    private String mLoadingString;
    private String mLoadingMoreString;
    private String mLoadingErrorString;
    private String mNoContentString;
    private String mNoContentActionString;
    private String mNoNetWorkString;

    public String getLoadingString() {
        return mLoadingString;
    }

    public String getLoadingMoreString() {
        return mLoadingMoreString;
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

    public void setLoadingMoreString(String mLoadingMoreString) {
        this.mLoadingMoreString = mLoadingMoreString;
        mLoadingMoreText.setTag(mLoadingMoreString);
    }

    public void setLoadingErrorString(String mLoadingErrorString) {
        this.mLoadingErrorString = mLoadingErrorString;
        mLoadingErrorText.setText(mLoadingErrorString);
    }

    public void setNoContentString(String mNoContentString) {
        this.mNoContentString = mNoContentString;
        mNoContentText.setText(mNoContentString);
    }

	public void setNoContentActionString(String noContentActionString) {
		this.mNoContentActionString = noContentActionString;
		if (mNoContentActionText != null) {
			mNoContentActionText.setText(this.mNoContentActionString);
		}
	}

    public void setNoNetWorkString(String mNoNetWorkString) {
        this.mNoNetWorkString = mNoNetWorkString;
        mNoNetWorkText.setText(mNoNetWorkString);
    }

    public void setStateString(int state, String string) {
        switch (state) {
            case STATE_LOADING:
                setLoadingString(string);
                break;
            case STATE_LOADINGMORE:
                setLoadingMoreString(string);
                break;
            case STATE_LOAD_ERROR:
                setLoadingErrorString(string);
                break;
            case STATE_NO_CONTENT:
                setNoContentString(string);
                break;
            case STATE_NO_NETWORK:
                setNoNetWorkString(string);
                break;
        }
    }

    public String getStateString(int state) {
        switch (state) {
            case STATE_LOADING:
                return getLoadingString();

            case STATE_LOADINGMORE:
                return getLoadingMoreString();

            case STATE_LOAD_ERROR:
                return getLoadingErrorString();

            case STATE_NO_CONTENT:
                return getNoContentString();

            case STATE_NO_NETWORK:
                return getNoNetWorkString();

        }
        return "";
    }

    public LoadingListView(Context context) {
        this(context, null);
    }

    public LoadingListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    public View getLoadingView() {
        View v = View.inflate(mContext, R.layout.view_loading, null);
        mLoadingText = (TextView)v.findViewById(R.id.loading_text);
        mLoadingString = mContext.getString(R.string.loading_default);
        mLoadingText.setText(mLoadingString);
        return v;
    }

    @Override
    public View getLoadingErrorView() {
        View v = View.inflate(mContext, R.layout.view_load_complete, null);
        mLoadingErrorText = (TextView)v.findViewById(R.id.loading_text);
        mLoadingErrorString = mContext.getString(R.string.error_loading_default);
        mLoadingErrorText.setText(mLoadingErrorString);
        return v;
    }

    @Override
    public View getLoadingFooterView() {
        View v = View.inflate(mContext, R.layout.view_loading, null);
        mLoadingMoreText = (TextView)v.findViewById(R.id.loading_text);
        mLoadingMoreString = mContext.getString(R.string.loading_more_default);
        mLoadingMoreText.setText(mLoadingMoreString);
        ImageView loadingImage = (ImageView)v.findViewById(R.id.loading_image);
        loadingImage.setVisibility(View.GONE);
        return v;
    }

    @Override
    public View getNoContentView() {
        View v = View.inflate(mContext, R.layout.view_load_complete, null);
        mNoContentText = (TextView)v.findViewById(R.id.loading_text);
        mNoContentText.setText(mNoContentString);
        
        mNoContentActionText = (TextView)v.findViewById(R.id.loading_text2);
        mNoContentActionText.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(mNoContentString)) {
        	mNoContentText.setText(mNoContentString);
        }
        mNoContentActionText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(noContentListener != null){
					noContentListener.onNoContent();
				}
			}
		});
        return v;
    }

    @Override
    public View getNoNetworkView() {
        View v = View.inflate(mContext, R.layout.view_loading_net_error, null);
        mNoNetWorkText = (TextView)v.findViewById(R.id.loading_text);
        mNoNetWorkString = mContext.getString(R.string.error_no_network);
        mNoNetWorkText.setText(mNoNetWorkString);
        return v;
    }
    
    private OnNoContentListener noContentListener ;
    public interface OnNoContentListener{
    	public void onNoContent();
    }
    
    public void setOnNoContentListener(OnNoContentListener listener){
    	noContentListener = listener ;
    }
}
