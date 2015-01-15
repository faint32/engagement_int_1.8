package com.netease.engagement.activity;

import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.handmark.pulltorefresh.library.LoadingAdapterViewBaseWrap.OnLoadingListener;
import com.netease.date.R;
import com.netease.engagement.adapter.RankUserListAdapter;
import com.netease.engagement.adapter.RankUserListAdapter.FemaleViewHolder;
import com.netease.engagement.adapter.RankUserListAdapter.MaleViewHolder;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.view.PullListView;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.RankListInfo;
import com.netease.service.stat.EgmStat;


public class ActivitySingleRankList extends ActivityEngagementBase {
    public static final String EXTRA_RANK_ID = "extra_rank_id";
    public static final String EXTRA_RANK_NAME = "extra_rank_name";
    public static final String EXTRA_SEX_TYPE = "extra_sex_type";
    
//    private View mEmptyView;
    private PullListView mListView;
    private RankUserListAdapter mAdapter;
    private int mRankId;
    private int mNextPage = 1;
    private int mTid;
    
    public static void startActivity(Context context, int rankId, String rankName, int sexType){
        Intent intent = new Intent(context, ActivitySingleRankList.class);
        intent.putExtra(EXTRA_RANK_ID, rankId);
        intent.putExtra(EXTRA_RANK_NAME, rankName);
        intent.putExtra(EXTRA_SEX_TYPE, sexType);
        
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        
        Bundle extra = this.getIntent().getExtras();
        String rankName = extra.getString(EXTRA_RANK_NAME, "");
        mRankId = extra.getInt(EXTRA_RANK_ID, -1);
        final int sexType = extra.getInt(EXTRA_SEX_TYPE, EgmConstants.SexType.Female);
        
        if(mRankId < 0)
            return;
        
        super.setCustomActionBar();
        CustomActionBar actionBar = getCustomActionBar();
        actionBar.setMiddleTitle(rankName);
        actionBar.setLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        actionBar.setRightVisibility(View.INVISIBLE);
        this.getWindow().setBackgroundDrawableResource(R.color.white);
        
        setContentView(R.layout.fragment_list_layout);
        
//        mEmptyView = findViewById(R.id.empty_tip);
//        mEmptyView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(mTipType == TYPE_ERROR && mAdapter.getCount() <= 0){
//                    mListView.reLoad();
//                }
//            }
//        });
        
        mAdapter = new RankUserListAdapter(this, sexType, mRankId, EgmConstants.RankListType.RANK_LIST_DAY,new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long uid = 0;
                int mRankId = 0;
                Object obj = v.getTag();
                int position=0;
                String logName = null;
                if(obj instanceof MaleViewHolder){
                    MaleViewHolder holder = (MaleViewHolder)obj;
                    uid = holder.mUid;
                    position=holder.positon;
                    mRankId=holder.mRankId;
                    logName=holder.logName;
                }
                else if(obj instanceof FemaleViewHolder){
                    FemaleViewHolder holder = (FemaleViewHolder)obj;
                    uid = holder.mUid;
                    position=holder.positon;
                    mRankId=holder.mRankId;
                    logName=holder.logName;
                }
                
				if (uid > 0) {
					EgmStat.log(EgmStat.LOG_CLICK_RANK, EgmStat.SCENE_TOP_LIST, uid, position, logName,EgmStat.RANK_LIST_DAY);
					ActivityUserPage.startActivity(ActivitySingleRankList.this, String.valueOf(uid), String.valueOf(sexType));
                }
            }
        });
        
        mListView = (PullListView) findViewById(R.id.listview);
        mListView.setShowIndicator(false);
        mListView.getRefreshableView().setDivider(null);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object obj = view.getTag();
                long uid;
                int pos;
                int mRankId;
                String logName;
                if(obj instanceof MaleViewHolder){
                    uid = ((MaleViewHolder)obj).mUid;
                    pos=((MaleViewHolder)obj).positon;
                    mRankId=((MaleViewHolder)obj).mRankId;
                    logName=((MaleViewHolder)obj).logName;
                }
                else if(obj instanceof FemaleViewHolder){
                    uid = ((FemaleViewHolder)obj).mUid;
                    pos=((FemaleViewHolder)obj).positon;
                    mRankId=((FemaleViewHolder)obj).mRankId;
                    logName=((FemaleViewHolder)obj).logName;
                }
                else{
                    return;
                }
                
				EgmStat.log(EgmStat.LOG_CLICK_RANK, EgmStat.SCENE_TOP_LIST, uid, pos,logName,EgmStat.RANK_LIST_DAY);              
                ActivityUserPage.startActivity(ActivitySingleRankList.this, String.valueOf(uid), String.valueOf(sexType));
            }
        });
        mListView.setOnLoadingListener(new OnLoadingListener(){
            @Override
            public void onRefreshing() {
                mNextPage = 1;
                getRankListData();
            }

            @Override
            public void onLoading() {
                mNextPage = 1;
                getRankListData();
            }

            @Override
            public void onLoadingMore() {
                if(mNextPage > 0){  // 还有更多数据
                    getRankListData();
                }
            }
        });
        
        EgmService.getInstance().addListener(mEgmCallback);
        mListView.load();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        EgmService.getInstance().removeListener(mEgmCallback);
    }
    
    private void getRankListData(){
        if(mNextPage < 1)
            return;
        
//        showTip(TYPE_CONTENT);
//        showWatting(getString(R.string.common_tip_is_waitting));
        mTid = EgmService.getInstance().doGetRank(mRankId, 0, mNextPage); //日榜
    }

    
    private final EgmCallBack mEgmCallback = new EgmCallBack(){
        /** 获取排行榜 */
        @Override
        public void onGetRankList(int transactionId, RankListInfo info){
            if(mTid != transactionId)
                return;
            
            stopWaiting();
            
            if(mNextPage == 1){ // 完全刷新
                mAdapter.clearDateList();
            }
            
            mAdapter.addDataList(info);
            
            if(mAdapter.getCount() <= 0){
                mListView.onNoContent();//showTip(TYPE_EMPTY);
            }
            else if(info.userList.size() < info.count){  // 没有更多数据了
                mNextPage = 0;
                mListView.onRefreshComplete();  // 这里没有判断是下拉刷新还是加载刷新，直接停止头部可能出现的下拉刷新
                mListView.onLoadingComplete(false);
            }
            else{   // 还可能有更多数据
                mNextPage++;
                mListView.onRefreshComplete();  // 这里没有判断是下拉刷新还是加载刷新，直接停止头部可能出现的下拉刷新
                mListView.onLoadingComplete(true);  // 底部加载更多
            }
        }
        @Override
        public void onGetRankListError(int transactionId, int errCode, String err){
            if(mTid != transactionId)
                return;
            
            stopWaiting();
            ToastUtil.showToast(ActivitySingleRankList.this, err);
            
            // 这里没有判断是下拉刷新还是加载刷新，直接停止头部可能出现的下拉刷新
            mListView.onRefreshComplete();
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
