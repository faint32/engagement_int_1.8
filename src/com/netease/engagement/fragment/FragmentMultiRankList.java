package com.netease.engagement.fragment;

import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.LoadingAdapterViewBaseWrap.OnLoadingListener;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityUserPage;
import com.netease.engagement.adapter.RankUserListAdapter;
import com.netease.engagement.adapter.RankUserListAdapter.FemaleViewHolder;
import com.netease.engagement.adapter.RankUserListAdapter.MaleViewHolder;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.view.PullListView;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.RankListInfo;
import com.netease.service.stat.EgmStat;

public class FragmentMultiRankList extends FragmentBase {
	
	public static final String EXTRA_RANK_ID = "extra_rank_id";
    public static final String EXTRA_SEX_TYPE = "extra_sex_type";
    public static final String EXTRA_RANK_TYPE = "extra_rank_type";
    
    private int mNextPage = 1;
    private int mTid;
    
    private int mRankId;
    private int mSexType;
    private int mRankType;
    
    private PullListView mListView;
    private RankUserListAdapter mAdapter;
    private TextView headerViewTips;
    
    public static FragmentMultiRankList newInstance(int rankId, int sexType, int rankType) {
    	FragmentMultiRankList fragment = new FragmentMultiRankList();
    	
    	Bundle extra = new Bundle();
    	extra.putInt(EXTRA_RANK_ID, rankId);
    	extra.putInt(EXTRA_SEX_TYPE, sexType);
    	extra.putInt(EXTRA_RANK_TYPE, rankType);
    	
    	fragment.setArguments(extra);
    	
    	return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	Bundle extra = this.getArguments();
    	mRankId = extra.getInt(EXTRA_RANK_ID);
    	mSexType = extra.getInt(EXTRA_SEX_TYPE);
    	mRankType = extra.getInt(EXTRA_RANK_TYPE);
    	
    	mAdapter = new RankUserListAdapter(getActivity(), mSexType, mRankId, mRankType,new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				long uid = 0;
                Object obj = v.getTag();
                int position = 0;
                String rankListType=null ;
                String logName=null;
                if(obj instanceof MaleViewHolder){
                    MaleViewHolder holder = (MaleViewHolder)obj;
                    uid = holder.mUid;
                    position=holder.positon;
                    logName=holder.logName;
                }
                else if(obj instanceof FemaleViewHolder){
                    FemaleViewHolder holder = (FemaleViewHolder)obj;
                    uid = holder.mUid;
                    position=holder.positon;
                    logName=holder.logName;
                }
				if (mRankType == EgmConstants.RankListType.RANK_LIST_DAY) {
					rankListType = EgmStat.RANK_LIST_DAY;
				} else if (mRankType == EgmConstants.RankListType.RANK_LIST_MONTH) {
					rankListType = EgmStat.RANK_LIST_MONTH;
				}
				if (uid > 0) {
                    ActivityUserPage.startActivity(FragmentMultiRankList.this, String.valueOf(uid), String.valueOf(mSexType));
					EgmStat.log(EgmStat.LOG_CLICK_RANK, EgmStat.SCENE_TOP_LIST, uid, position,logName,rankListType);
                }
			}
		});
    	
    	EgmService.getInstance().addListener(mEgmCallback);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.fragment_list_layout, container, false);
    	mListView = (PullListView)view.findViewById(R.id.listview);
        mListView.setShowIndicator(false);
        mListView.getRefreshableView().setDivider(null);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Object obj = view.getTag();
                long uid;
                int pos;
                int mRankId;
                String rankListType=null ;
                String logName=null;
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
				if (mRankType == EgmConstants.RankListType.RANK_LIST_DAY) {
					rankListType = EgmStat.RANK_LIST_DAY;
				} else if (mRankType == EgmConstants.RankListType.RANK_LIST_MONTH) {
					rankListType = EgmStat.RANK_LIST_MONTH;
				}
				EgmStat.log(EgmStat.LOG_CLICK_RANK, EgmStat.SCENE_TOP_LIST, uid, pos ,logName,rankListType);              
                ActivityUserPage.startActivity(FragmentMultiRankList.this, String.valueOf(uid), String.valueOf(mSexType));
			}
		});
        
        mListView.setOnLoadingListener(new OnLoadingListener() {

			@Override
			public void onRefreshing() {
				mNextPage = 1;
				doGetRankList();
				
			}

			@Override
			public void onLoading() {
				mNextPage = 1;
				doGetRankList();
			}

			@Override
			public void onLoadingMore() {
				doGetRankList();
			}
		});
        
        // 私照榜单添加额外提示
        if (mRankId == EgmConstants.RankID.PRIVATE_PIC_FEMALE) {
            
            headerViewTips = new TextView(getActivity());
            headerViewTips.setGravity(Gravity.CENTER);
            headerViewTips.setTextColor(0xFF787878);
            headerViewTips.setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources()
                    .getDimensionPixelSize(R.dimen.text_size_14));
            headerViewTips.setBackgroundColor(0xFFF1F1F1);
            headerViewTips.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, getActivity()
                    .getResources().getDimensionPixelSize(R.dimen.info_margin_32dp)));
            LinearLayout mLayout = new LinearLayout(getActivity());
            mLayout.addView(headerViewTips);
            mListView.getRefreshableView().addHeaderView(mLayout);
        }
        
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
    
    private void doGetRankList() {
    	if(mNextPage < 1) {
    		return;
    	}
    	
    	mTid = EgmService.getInstance().doGetRank(mRankId, mRankType, mNextPage);
    }
    
    private EgmCallBack mEgmCallback = new EgmCallBack(){
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
            

            // 显示榜单提示语
            if (mRankId == EgmConstants.RankID.PRIVATE_PIC_FEMALE) {
                if (headerViewTips != null) {
                    headerViewTips.setText(info.title);
                }
            }
            
        }
        @Override
        public void onGetRankListError(int transactionId, int errCode, String err){
            if(mTid != transactionId)
                return;
            
            stopWaiting();
            ToastUtil.showToast(getActivity(), err);
            
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
