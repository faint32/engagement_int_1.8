
package com.handmark.pulltorefresh.library;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public abstract class LoadingAdapterViewBaseWrap<T extends AbsListView> extends
        PullToRefreshAdapterViewBase<T> {

    public static final int STATE_IDLE = 0x01;
    public static final int STATE_LOADING = 0x02;
    public static final int STATE_LOAD_ERROR = 0x03;
    public static final int STATE_NO_NETWORK = 0x04;
    public static final int STATE_NO_CONTENT = 0x05;
    public static final int STATE_PREPARED = 0x06;
    public static final int STATE_LOADINGMORE = 0x07;

    // ---------------Data-----------
    private Context mContext;

    private int mState = STATE_IDLE;
    private boolean mHasMore;
    private boolean mIsLoadingMoreEnable = true;
    private boolean mIsPullToRefreshEnable = true;

    private OnLoadingListener mLoadingListener;
    // ---------------View-----------
    private AutoFillListLinearLayout mLoadingContainer;
    private View mLoadingView;// 正在加载视图
    private View mLoadingFooterView;// 上拉加载更多视图
    private View mLoadingErrorView;// 加载出错
    private View mNoContentView;// 无内容
    private View mNoNetworkView;// 无网络

    public LoadingAdapterViewBaseWrap(Context context) {
        super(context);
        mContext = context;
    }

    public LoadingAdapterViewBaseWrap(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public LoadingAdapterViewBaseWrap(Context context, Mode mode) {
        super(context, mode);
        mContext = context;
    }

    public LoadingAdapterViewBaseWrap(Context context, Mode mode, AnimationStyle animStyle) {
        super(context, mode, animStyle);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        // TODO Auto-generated method stub
        super.onFinishInflate();
        init();
    }

    private void init() {

        if (mRefreshableView instanceof ListView) {
            mLoadingContainer = new AutoFillListLinearLayout(mContext);
            mLoadingContainer.setOrientation(VERTICAL);
            mLoadingContainer.setGravity(Gravity.CENTER);
            ((ListView)mRefreshableView).addFooterView(mLoadingContainer);

            mLoadingView = getLoadingView();
            if (mLoadingView != null) {
                mLoadingContainer.addView(mLoadingView);
            }
            mLoadingErrorView = getLoadingErrorView();
            if (mLoadingErrorView != null) {
                mLoadingContainer.addView(mLoadingErrorView);
            }
            mNoContentView = getNoContentView();
            if (mNoContentView != null) {
                mLoadingContainer.addView(mNoContentView);
            }
            mLoadingFooterView = getLoadingFooterView();
            if (mLoadingFooterView != null) {
                mLoadingContainer.addView(mLoadingFooterView);
            }
            mNoNetworkView = getNoNetworkView();
            if (mNoNetworkView != null) {
                mLoadingContainer.addView(mNoNetworkView);
            }
        }

        reset();

        setOnRefreshListener(new OnRefreshListener<T>() {

            @Override
            public void onRefresh(PullToRefreshBase<T> refreshView) {
                if (mLoadingListener != null)
                    mLoadingListener.onRefreshing();
            }
        });

        setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {
                if (mIsLoadingMoreEnable && mHasMore && mState == STATE_PREPARED) {
                    mState = STATE_LOADINGMORE;
                    resetLoadingView();
                    if (mLoadingListener != null)
                        mLoadingListener.onLoadingMore();
                }
            }
        });
    }

    public void disableLoadingMore() {
        mIsLoadingMoreEnable = false;
        mHasMore = false;
        if (mLoadingFooterView != null) {
            mLoadingFooterView.setVisibility(View.GONE);
        }
    }

    public void disablePullToRefresh() {
        mIsPullToRefreshEnable = false;
        setMode(Mode.DISABLED);
    }
    
    /**
     * 隐藏所有加载视图, 根据当前状态显示对应视图
     */
    private void resetLoadingView() {
        if (mLoadingFooterView != null) {
            if (mHasMore) {
                mLoadingFooterView.setVisibility(View.INVISIBLE);
            } else {
                mLoadingFooterView.setVisibility(View.GONE);
            }
        }
        if (mLoadingErrorView != null) {
            mLoadingErrorView.setVisibility(View.GONE);
        }
        if (mLoadingView != null) {
            mLoadingView.setVisibility(View.GONE);
        }
        if (mNoContentView != null) {
            mNoContentView.setVisibility(View.GONE);
        }
        if (mNoNetworkView != null) {
            mNoNetworkView.setVisibility(View.GONE);
        }
        switch (mState) {
            case STATE_LOAD_ERROR:
                mLoadingContainer.setAutoFillState();
                if (mLoadingErrorView != null) {
                    mLoadingErrorView.setVisibility(View.VISIBLE);
                }
                break;
            case STATE_LOADING:
                mLoadingContainer.setAutoFillState();
                if (mLoadingView != null) {
                    mLoadingView.setVisibility(View.VISIBLE);
                }
                break;
            case STATE_LOADINGMORE:
                mLoadingContainer.setWrapState();
                if (mLoadingFooterView != null) {
                    mLoadingFooterView.setVisibility(View.VISIBLE);
                }
                break;
            case STATE_NO_CONTENT:
                mLoadingContainer.setAutoFillState();
                if (mNoContentView != null) {
                    mNoContentView.setVisibility(View.VISIBLE);
                }
                break;
            case STATE_NO_NETWORK:
                mLoadingContainer.setAutoFillState();
                if (mNoNetworkView != null) {
                    mNoNetworkView.setVisibility(View.VISIBLE);
                }
                break;
            case STATE_IDLE:
            case STATE_PREPARED:
            default:
                if(mHasMore){
                    mLoadingContainer.setWrapState();
                }else{
                    mLoadingContainer.setEmptyState();
                }
                break;
        }
        ListAdapter adapter = mRefreshableView.getAdapter();
        if (adapter != null && adapter instanceof BaseAdapter) {
            ((BaseAdapter)adapter).notifyDataSetChanged();
        }
        mLoadingContainer.measure(0, 0);
    }

    public void setLoadingState(int state) {
        mState = state;
        resetLoadingView();
    }

    public void load() {
        if (mState == STATE_IDLE || mState == STATE_NO_NETWORK) {
            mState = STATE_LOADING;
            resetLoadingView();
            if (mLoadingListener != null) {
                mLoadingListener.onLoading();
            }
        }
    }

    /**
     * 重新加载ListView的数据
     */
    public void reLoad() {
        mState = STATE_IDLE;
        mHasMore = false;
        load();
    }

    public void setOnLoadingListener(OnLoadingListener loadingListener) {
        mLoadingListener = loadingListener;
    }

    public interface OnLoadingListener {

        /**
         * 下拉刷新时的回调，刷新完成调用onRefreshComplete()
         */
        public void onRefreshing();

        /**
         * 调用load()后的回调
         */
        public void onLoading();

        /**
         * 上拉加载更多时的回调
         */
        public void onLoadingMore();
    }

    public void onLoadingComplete(boolean hasMore) {
        if (mIsPullToRefreshEnable)
            setMode(Mode.PULL_FROM_START);
        
        mHasMore = hasMore;
        mState = STATE_PREPARED;
        resetLoadingView();
    }

    public void onLoadingComplete(){
        if (mIsPullToRefreshEnable)
            setMode(Mode.PULL_FROM_START);
        mState = STATE_PREPARED;
        resetLoadingView();
    }

    public void onNoContent() {
        mState = STATE_NO_CONTENT;
        resetLoadingView();
    }

    public void onLoadingError() {
        mState = STATE_LOAD_ERROR;
        resetLoadingView();
    }

    public void onNoNetwork() {
        mState = STATE_NO_NETWORK;
        resetLoadingView();
    }

    /**
     * 重置该ListView的所有状态
     */
    public void reset() {
        mState = STATE_IDLE;
        mHasMore = false;
        setMode(Mode.DISABLED);
        resetLoadingView();
    }

    public boolean hasMore() {
        return mHasMore;
    }

    public void setHasMore(boolean hasMore) {
        mHasMore = hasMore;
    }

    public int getLoadingState() {
        return mState;
    }

    public void resizeLoadingView(){
        mLoadingContainer.measure(0, 0);
    }

    public abstract View getLoadingView();

    public abstract View getLoadingFooterView();

    public abstract View getNoContentView();

    public abstract View getLoadingErrorView();

    public abstract View getNoNetworkView();
}
