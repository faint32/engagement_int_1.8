package com.netease.engagement.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.LoadingAdapterViewBaseWrap.OnLoadingListener;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.adapter.MoneyRecordListAdapter;
import com.netease.engagement.view.PullListView;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.MoneyRecordListInfo;

/** 收支记录列表 */
public class FragmentMoneyHistoryList extends FragmentBase {
    public static final String EXTRA_TAB_TYPE = "extra_tab_type";
    public static final String EXTRA_HEAD_STR_ID = "extra_head_str_id";
    
    private ActivityEngagementBase mActivity;
//    private View mEmptyView;
    private PullListView mListView;
    private TextView mHeadView;
    private MoneyRecordListAdapter mAdapter;
    
    private int mRecordType;
    /** 列表header的文字资源id */
    private int mHeadStrId;
    private int mPage = 1;
    private int mCurrentCount = 0;
    private int mTid;

    public static FragmentMoneyHistoryList newInstance(int type, int headStrId){
        FragmentMoneyHistoryList fragment = new FragmentMoneyHistoryList();
        
        Bundle extra = new Bundle();
        extra.putInt(EXTRA_TAB_TYPE, type);
        extra.putInt(EXTRA_HEAD_STR_ID, headStrId);
        fragment.setArguments(extra);
        
        return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extra = this.getArguments();
        mRecordType = extra.getInt(EXTRA_TAB_TYPE);
        mHeadStrId = extra.getInt(EXTRA_HEAD_STR_ID);
        
        mActivity = (ActivityEngagementBase)this.getActivity();
        mAdapter = new MoneyRecordListAdapter(mActivity);
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
        
        mHeadView = (TextView)inflater.inflate(R.layout.view_money_record_list_header, null, false);
        
        mListView = (PullListView)view.findViewById(R.id.listview);
        mListView.setShowIndicator(false);
        mListView.getRefreshableView().setDivider(null);
        mListView.getRefreshableView().addHeaderView(mHeadView);
        mListView.getRefreshableView().setSelector(R.color.transparent);
        mListView.setAdapter(mAdapter);
        mListView.disablePullToRefresh();
        mListView.setOnLoadingListener(new OnLoadingListener(){
            @Override
            public void onRefreshing() {
                mPage = 1;
                doGetRecord();
            }

            @Override
            public void onLoading() {
                mPage = 1;
                doGetRecord();
            }

            @Override
            public void onLoadingMore() {
                doGetRecord();
            }
        });
        
        return view;
    }
    
    @Override
    public void onResume(){
        super.onResume();
        mListView.load();
    }
    
    @Override
    public void onDestroy(){
        super.onDestroy();
        
        EgmService.getInstance().removeListener(mEgmCallback);
    }
    
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
//                ((TextView)mEmptyView.findViewById(R.id.empty_text)).setText(R.string.common_content_empty_tip);
//                break;
//            case TYPE_ERROR:
//                mListView.setVisibility(View.INVISIBLE);
//                mEmptyView.setVisibility(View.VISIBLE);
//                mEmptyView.findViewById(R.id.empty_image).setVisibility(View.VISIBLE);
//                ((TextView)mEmptyView.findViewById(R.id.empty_text)).setText(R.string.common_reload_tip);
//                break;    
//        }
//    }
    
    private void doGetRecord(){
        if(mPage < 1)
            return;
        
        if(mPage == 1){
            mCurrentCount = 0;  // 从头取，总数要初始化为0
        }
        
//        showTip(TYPE_CONTENT);
//        showWatting(mActivity.getString(R.string.common_tip_is_waitting));
        mTid = EgmService.getInstance().doGetMoneyHistory(mRecordType, mPage);
    }
    
    private EgmCallBack mEgmCallback = new EgmCallBack(){
        /** 女性现金收支记录 */
        @Override
        public void onGetMoneyRecord(int transactionId, MoneyRecordListInfo info){
            if(mTid != transactionId)
                return;
            
            stopWaiting();
            
            if(mPage == 1){ // 第一次请求
                mAdapter.clearDataList();
                mHeadView.setText(mActivity.getString(mHeadStrId, info.totalCount));
            }
            mAdapter.addData(info.records);
            
            mCurrentCount += info.records.size();
            
            if(mAdapter.getCount() <= 0){
                mListView.onNoContent();//showTip(TYPE_EMPTY);
            }
            else if(mCurrentCount < info.totalCount){    // 服务器上还有数据
                mPage++;
                mListView.onLoadingComplete(true);
            }
            else{
                mPage = 0;  // 无效页
                mListView.onLoadingComplete(false);
            }
        }
        @Override
        public void onGetMoneyRecordError(int transactionId, int errCode, String err){
            if(mTid != transactionId)
                return;
            
            stopWaiting();
            showToast(err);
            mListView.onLoadingComplete();
            
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
