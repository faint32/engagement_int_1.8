package com.netease.engagement.fragment;

import android.R.integer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.handmark.pulltorefresh.library.LoadingAdapterViewBaseWrap.OnLoadingListener;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.activity.ActivityUserPage;
import com.netease.engagement.adapter.SearchUserListAdapter;
import com.netease.engagement.adapter.SearchUserListAdapter.FemaleViewHolder;
import com.netease.engagement.adapter.SearchUserListAdapter.MaleViewHolder;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.view.PullListView;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.SearchListInfo;
import com.netease.service.stat.EgmStat;

/**
 * 搜索结果页面
 * @version 1.0
 */
public class FragmentSearchList extends FragmentBase {
    
    private ActivityEngagementBase mActivity;
    private PullListView mListView;
//    private View mEmptyView;
    private SearchUserListAdapter mAdapter;
    private int mSexType;
    private int mTid;
    private int mPage = 1;
    
    private int mAgeBegin, mAgeEnd, mConstellation, mIncome, mProvinceCode;
    private boolean mHasPrivatePic = false;

    public static FragmentSearchList newInstance(int sexType, int ageBegin, int ageEnd, int constellation, 
            int provinceCode, boolean hasPrivatePic, int income){
        
        FragmentSearchList f = new FragmentSearchList();
        
        Bundle extra = new Bundle();
        extra.putInt(EgmConstants.EXTRA_SEARCH_SEX_TYPE, sexType);
        extra.putInt(EgmConstants.EXTRA_SEARCH_AGE_BEGIN, ageBegin);
        extra.putInt(EgmConstants.EXTRA_SEARCH_AGE_END, ageEnd);
        extra.putInt(EgmConstants.EXTRA_SEARCH_CONSTE, constellation);
        extra.putInt(EgmConstants.EXTRA_SEARCH_PROVINCE, provinceCode);
        extra.putBoolean(EgmConstants.EXTRA_SEARCH_PRIVATE, hasPrivatePic);
        extra.putInt(EgmConstants.EXTRA_SEARCH_INCOME, income);
        f.setArguments(extra);
        
        return f;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        
        Bundle extra = this.getArguments();
        mSexType = extra.getInt(EgmConstants.EXTRA_SEARCH_SEX_TYPE);
        mAgeBegin = extra.getInt(EgmConstants.EXTRA_SEARCH_AGE_BEGIN);
        mAgeEnd = extra.getInt(EgmConstants.EXTRA_SEARCH_AGE_END);
        mConstellation = extra.getInt(EgmConstants.EXTRA_SEARCH_CONSTE);
        mIncome = extra.getInt(EgmConstants.EXTRA_SEARCH_INCOME);
        mProvinceCode = extra.getInt(EgmConstants.EXTRA_SEARCH_PROVINCE);
        mHasPrivatePic = extra.getBoolean(EgmConstants.EXTRA_SEARCH_PRIVATE);
        
        mActivity = (ActivityEngagementBase)this.getActivity();
        EgmService.getInstance().addListener(mEgmCallback);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_layout, container, false);
        
//        mEmptyView = view.findViewById(R.id.empty_tip);
//        mEmptyView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(mTipType == TYPE_ERROR && mAdapter.getCount() <= 0){
//                    mListView.reLoad();
//                }
//            }
//        });
        
        int sexType;
        if(mSexType == EgmConstants.SexType.Female){
            sexType = EgmConstants.SexType.Male;
        }
        else{
            sexType = EgmConstants.SexType.Female;
        }
        
        mAdapter = new SearchUserListAdapter(mActivity, sexType);
        
        mListView = (PullListView)view.findViewById(R.id.listview);
        mListView.setShowIndicator(false);
        mListView.getRefreshableView().setDivider(null);
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(mScrollListener);
        mListView.disablePullToRefresh();   // 禁止下拉刷新
        mListView.setOnLoadingListener(new OnLoadingListener(){
            @Override
            public void onRefreshing() {}

            @Override
            public void onLoading() {
                mPage = 1;  // 从头加载
                doSearch();
            }

            @Override
            public void onLoadingMore() {
                if(mPage > 0){  // 还有更多数据
                    doSearch();
                }
            }
        });
        
        mListView.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object obj = view.getTag();
                long uid;
                String sexType;
                int pos;
                if(obj instanceof MaleViewHolder){
                    uid = ((MaleViewHolder)obj).mUid;
                    sexType = String.valueOf(EgmConstants.SexType.Male);
                    pos=((MaleViewHolder)obj).position;
                }
                else if(obj instanceof FemaleViewHolder){
                    uid = ((FemaleViewHolder)obj).mUid;
                    sexType = String.valueOf(EgmConstants.SexType.Female);
                    pos=((FemaleViewHolder)obj).position;
                }
                else{
                    return;
                }
                
                if(uid > 0){
                    ActivityUserPage.startActivity(mActivity, String.valueOf(uid), sexType);
					EgmStat.log(EgmStat.LOG_CLICK_SEARCH, EgmStat.SCENE_SEARCH, uid, pos);
                }
            }
        });
        
        mListView.load();
        
        return view;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        
        CustomActionBar actionBar = ((ActivityEngagementBase)getActivity()).getCustomActionBar();
        actionBar.setMiddleTitle(R.string.rec_search_result);
        actionBar.setLeftClickListener(mClickBack);
    }
    
    @Override
    public void onDestroy(){
        super.onDestroy();
        
        EgmService.getInstance().removeListener(mEgmCallback);
    }
    
    private View.OnClickListener mClickBack = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mActivity.finish();
        }
    };
    
//    private final int TYPE_CONTENT = 0;
//    private final int TYPE_EMPTY = 1;
//    private final int TYPE_ERROR = 2;
//    private int mTipType;
//    
//    /** 显示空内容或者重新加载提示 */
//    private void showTip(int type){
//        mTipType = type;
//        
//        switch(type){
//            case TYPE_CONTENT:
//                mListView.setVisibility(View.VISIBLE);
//                mEmptyView.setVisibility(View.INVISIBLE);
//                break;
//            case TYPE_EMPTY:
//                mListView.setVisibility(View.INVISIBLE);
//                mEmptyView.setVisibility(View.VISIBLE);
//                mEmptyView.findViewById(R.id.empty_image).setVisibility(View.GONE);
//                ((TextView)mEmptyView.findViewById(R.id.empty_text)).setText(R.string.rec_search_result_empty);
//                break;
//            case TYPE_ERROR:
//                mListView.setVisibility(View.INVISIBLE);
//                mEmptyView.setVisibility(View.VISIBLE);
//                mEmptyView.findViewById(R.id.empty_image).setVisibility(View.VISIBLE);
//                ((TextView)mEmptyView.findViewById(R.id.empty_text)).setText(R.string.common_reload_tip);
//                break;    
//        }
//    }
    
    private void doSearch(){
        if(mPage < 1)
            return;
        
//        showTip(TYPE_CONTENT);
//        showWatting(getString(R.string.common_tip_is_waitting));
        
        int privateValue;
        if(mSexType == EgmConstants.SexType.Female){    // 女性搜男性，不用是否有私照选项
            privateValue = -1;
        }
        else if(mHasPrivatePic){
            privateValue = 1;   // 有私照
        }
        else{
            privateValue = 0;   // 没有
        }
        
        mTid = EgmService.getInstance().doSearch(mAgeBegin, mAgeEnd, mConstellation, mProvinceCode, privateValue, mIncome, mPage);
    }
    
    /** 记录列表的最后一个Item的位置，以便判断是否要加载更多 */
    private int mLastItemPos = 0;
    /** 是否正在加载更多动态 */
    private boolean mIsLoadMoreAction = false;
    /** 是否有更多数据 */
    private boolean mIsHasMore = true;
    
    private final AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener(){
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            mLastItemPos = firstVisibleItem + visibleItemCount;
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if(scrollState == OnScrollListener.SCROLL_STATE_IDLE){
                if(!mIsLoadMoreAction && 
                        mLastItemPos >= mAdapter.getCount() - 1 && // 滚动到底
                        mIsHasMore){  // 有更多数据
//已在listview的loadingMore中加载更多，此处不需要，by echo_chen 2014-10-11
//                    doSearch();
                }
            }
        }
    };
    
    private EgmCallBack mEgmCallback = new EgmCallBack(){
        /** 获取搜索列表 */
        @Override
        public void onGetSearchList(int transactionId, SearchListInfo info){
            if(mTid != transactionId)
                return;
            
            stopWaiting();
            mAdapter.addDataList(info.searchList);
            
            if(mAdapter.getCount() <= 0){
                mListView.onNoContent();//showTip(TYPE_EMPTY);
            }
            else if(info.searchList.size() < info.count){    // 没有更多了
                mListView.onLoadingComplete(false);
                mPage = 0;
            }
            else{   // 还有更多
                mPage++;
                mListView.onLoadingComplete(true);
            }
        }
        @Override
        public void onGetSearchListError(int transactionId, int errCode, String err){
            if(mTid != transactionId)
                return;
            
            stopWaiting();
            showToast(err);
            mListView.onLoadingComplete(false);
            
            if(mAdapter.getCount() <= 0){
                if(errCode == EgmServiceCode.NETWORK_ERR_COMMON){
                    mListView.onLoadingError();//showTip(TYPE_ERROR);
                }
                else{
                    mListView.onNoContent();//showTip(TYPE_EMPTY);
                }
            }
        }
    };
}
