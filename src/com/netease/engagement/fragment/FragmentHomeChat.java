package com.netease.engagement.fragment;

import java.util.List;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.handmark.pulltorefresh.compat.LoadingListView;
import com.handmark.pulltorefresh.compat.LoadingListView.OnNoContentListener;
import com.handmark.pulltorefresh.library.LoadingAdapterViewBaseWrap.OnLoadingListener;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityEngagementBase;
import com.netease.engagement.activity.ActivityHome;
import com.netease.engagement.activity.ActivityPrivateSession;
import com.netease.engagement.adapter.ChatListCursorAdapter;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.cursorloader.LoaderFactory;
import com.netease.engagement.view.CustomViewPager;
import com.netease.engagement.view.HomeTabView;
import com.netease.engagement.view.IFragment;
import com.netease.engagement.widget.CustomActionBar;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.db.manager.LastMsgDBManager;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.ChatItemInfo;
import com.netease.service.protocol.meta.ChatItemUserInfo;

/**
 * 男性聊天列表页
 */
public class FragmentHomeChat extends FragmentBase implements IFragment
		,LoaderCallbacks<Cursor>{
	
	public static FragmentHomeChat newInstance(){
		FragmentHomeChat fragment = new FragmentHomeChat();
		return fragment ;
	}	

	private CustomActionBar mCustomActionBar ;
	private LoadingListView mLoadingListView ;
	private ChatListCursorAdapter mAdapter ;
	
	private int mLoaderId = FragmentHomeChat.class.getSimpleName().hashCode();
	
	private String mDelNick ;
	private long mDelUid ;
	private long mLastRefreshTime = 0;//记录上次上传统计时间
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EgmService.getInstance().addListener(mCallBack);
    }
    
    private void initTitle(){
        ActivityEngagementBase activity = ((ActivityEngagementBase)getActivity());
        if (activity != null) {
            mCustomActionBar = activity.getCustomActionBar();
            mCustomActionBar.getCustomView().setBackgroundColor(
                    getResources().getColor(R.color.white));

            mCustomActionBar.hideLeftTitle();

            mCustomActionBar.setMiddleTitle(R.string.str_chat);
            mCustomActionBar.setMiddleTitleColor(getResources().getColor(R.color.black));
            mCustomActionBar.setMiddleTitleSize(20);
            mCustomActionBar.hideRightTitle();
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chat_list, container, false);
        initViews(root);
        return root;
    }
    
    @Override
	public void onResume() {
		super.onResume();
		setUnReadNum();
	}
    
    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setUnReadNum();
	}
    
    /**
	 * 设置底部消息未读数目
	 */
    private void setUnReadNum(){
		HomeTabView tabView = ((ActivityHome)getActivity()).getChatTab() ;
		if(tabView != null){
		    int count = LastMsgDBManager.getUnReadNum() ;
	        count = (count > 99 ? 99 : count) ;
			if(count != 0){
				tabView.setTipCount(count);
			}else if(count == 0){
				tabView.setTipLayoutVisibility(View.GONE);
			}
		}
	}

	private void initViews(View root){
    	mLoadingListView = (LoadingListView)root.findViewById(R.id.chat_list);
    	mLoadingListView.setNoContentString(getString(R.string.man_tip));
		mLoadingListView.setOnNoContentListener(new OnNoContentListener(){
			@Override
			public void onNoContent() {
				CustomViewPager pager = ((ActivityHome)(FragmentHomeChat.this.getActivity())).getViewPager();
				if(pager != null){
					pager.setCurrentItem(0);
				}
			}
		});
    	mLoadingListView.disableLoadingMore();
    	mLoadingListView.disablePullToRefresh();
    	mLoadingListView.setOnItemClickListener(mOnItemClickListener);
    	mLoadingListView.getRefreshableView().setOnItemLongClickListener(mOnItemLongClickListener);
    	
    	mLoadingListView.setOnLoadingListener(new OnLoadingListener(){
			@Override
			public void onRefreshing() {
			}
			@Override
			public void onLoading() {
				EgmService.getInstance().doGetChatList();
			}
			@Override
			public void onLoadingMore() {
			}
    	});
    	
    	mAdapter = new ChatListCursorAdapter(getActivity(),null);
    	mLoadingListView.setAdapter(mAdapter);
   
    }

	private void load(){
		mLoadingListView.reLoad();
	}
	
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// bug fix #140874  by gzlichangjie
			if (/*mLoadingListView.getLoadingState() == LoadingListView.STATE_LOADING
					||*/ mLoadingListView.getLoadingState() == LoadingListView.STATE_NO_CONTENT
					|| mLoadingListView.getLoadingState() == LoadingListView.STATE_NO_NETWORK || mAdapter.getCount() == 0 ) {
				return ;
			}
			int count = mLoadingListView.getRefreshableView().getHeaderViewsCount();
			ActivityPrivateSession.startActivity(
					FragmentHomeChat.this,
					getChatItemUserInfo(position - count));
		}
	};
	
	/**
	 * 获取ChatItemUserInfo
	 * @param position
	 * @return
	 */
	private ChatItemUserInfo getChatItemUserInfo(int position){
		ChatItemUserInfo info = null ;
		long uid = 0 ;
		if(mAdapter.getItem(position) != null){
			uid = mAdapter.getItem(position);
		}
		ChatItemInfo chatItemInfo = LastMsgDBManager.getChatItemByUid(uid);
		if(chatItemInfo != null && chatItemInfo.anotherUserInfo != null){
			info = chatItemInfo.anotherUserInfo ;
		}
		return info ;
	}
	
	private OnItemLongClickListener mOnItemLongClickListener = new OnItemLongClickListener(){
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			
			if (mLoadingListView.getLoadingState() == LoadingListView.STATE_LOADING
					|| mLoadingListView.getLoadingState() == LoadingListView.STATE_NO_CONTENT
					|| mLoadingListView.getLoadingState() == LoadingListView.STATE_NO_NETWORK) {
				return true;
			}
			
			int index = position - mLoadingListView.getRefreshableView().getHeaderViewsCount();
			final ChatItemUserInfo info = getChatItemUserInfo(index);
			if(info == null){
				return false;
			}
			
			if(info.uid == EgmConstants.System_Sender_Id.TYPE_XIAOAI
					|| info.uid == EgmConstants.System_Sender_Id.TYPE_YIXIN){
				return true;
			}
			
			mDelNick = info.nick ;
			mDelUid = info.uid ;
			showDelChatItemDialog();
			return true;
		}
	};
	
	private AlertDialog mDelChatItemDialog ;
	private void showDelChatItemDialog(){
//		if(mDelChatItemDialog == null){//注掉，否则返回键关闭后下次再打开信息无法更新 2014-10-08 by echo-chen
			mDelChatItemDialog = EgmUtil.createEgmMenuDialog(
					getActivity(), 
					mDelNick, 
					new CharSequence[]{getResources().getString(R.string.delete_chat_item)}, 
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							//删除聊天项
							EgmService.getInstance().doDelChatListItem(String.valueOf(mDelUid));
							//删除相应数据库记录
							if (mDelChatItemDialog != null) {
								mDelChatItemDialog.dismiss();
								mDelChatItemDialog = null;
							}
						}
					});
//		}
		mDelChatItemDialog.show();
	}
	
	private EgmCallBack mCallBack = new EgmCallBack(){
		@Override
		public void onGetChatListSucess(int transactionId,List<ChatItemInfo> obj) {
			if(obj == null || obj.size() == 0){
				mLoadingListView.onNoContent();
				return ;
			}
			getLoaderManager().restartLoader(mLoaderId, null,FragmentHomeChat.this);
			setUnReadNum();
		}
		@Override
		public void onGetChatListError(int transactionId, int errCode, String err) {
			// 增加错误处理  bug fix #140832  by gzlichangjie
			mLoadingListView.onLoadingComplete();
//			if(errCode == EgmServiceCode.NETWORK_ERR
//					|| errCode == EgmServiceCode.NETWORK_ERR_COMMON){
//				mLoadingListView.onNoNetwork();
//			}
			getLoaderManager().restartLoader(mLoaderId, null,FragmentHomeChat.this);
			ToastUtil.showToast(getActivity(), err);
		}
		
		@Override
		public void onDelChatListSucess(int transactionId, int code) {
			ToastUtil.showToast(getActivity(),R.string.delete_chat_item_success);
//			LastMsgDBManager.delMsgByUid(mDelUid);
//			MsgDBManager.delMsgByUid(mDelUid);
			if (mAdapter.getCount() == 0) { // 表示消息从有变成无了，要重新加载，使得listview显示noContent的提示
				getLoaderManager().restartLoader(mLoaderId, null,FragmentHomeChat.this);
			}
			setUnReadNum();
		}
		
		@Override
		public void onDelChatListError(int transactionId, int errCode,String err) {
			ToastUtil.showToast(getActivity(),err);
		}
		@Override
		public void onPushMsgArrived(int transactionId, List<ChatItemInfo> obj) {
			if (obj!=null && obj.size()>0) {
				if (mLoadingListView.getLoadingState() == LoadingListView.STATE_NO_CONTENT
						|| mLoadingListView.getLoadingState() == LoadingListView.STATE_NO_NETWORK) {
					// 页面本来是显示noContent或者noNetwork提示的话，要重新加载，使得提示消失
					getLoaderManager().restartLoader(mLoaderId, null,FragmentHomeChat.this);
				}
			}
			setUnReadNum();
		}
	};
	
	@Override
	public void onPageSelected() {
		initTitle();
		long delta = System.currentTimeMillis() - mLastRefreshTime;
		if (delta > EgmConstants.TABSWITCH_REFRESH_INTERVAL) {
			mLastRefreshTime = System.currentTimeMillis();
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					load();
				}
			}, 500);
		}
	}

	@Override
	public void onPageReSelected() {
		
	}
	
	@Override
	public void onTabDoubleTap() {
		if(mLoadingListView.getRefreshableView().getFirstVisiblePosition() != 0) {
			mLoadingListView.getRefreshableView().smoothScrollToPosition(0);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		EgmService.getInstance().removeListener(mCallBack);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return LoaderFactory.getLoader(getActivity(),LoaderFactory.CHAT_LIST_SORT_TIME,false);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		mLoadingListView.onLoadingComplete();
		mAdapter.swapCursor(cursor);
		if(cursor.getCount() == 0){
			mLoadingListView.onNoContent();
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.swapCursor(null);
	}
}
