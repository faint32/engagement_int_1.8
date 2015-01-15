package com.netease.engagement.fragment;

import java.util.List;

import com.handmark.pulltorefresh.compat.LoadingListView;
import com.handmark.pulltorefresh.compat.LoadingListView.OnNoContentListener;
import com.handmark.pulltorefresh.library.LoadingAdapterViewBaseWrap.OnLoadingListener;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityHome;
import com.netease.engagement.activity.ActivityPrivateSession;
import com.netease.engagement.activity.ActivityYuanfen;
import com.netease.engagement.adapter.ChatListCursorAdapter;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.cursorloader.LoaderFactory;
import com.netease.engagement.view.CustomViewPager;
import com.netease.engagement.view.HomeTabView;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.db.manager.LastMsgDBManager;
import com.netease.service.protocol.EgmCallBack;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.ChatItemInfo;
import com.netease.service.protocol.meta.ChatItemUserInfo;
import com.netease.service.protocol.meta.SortChatListResult;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;


public class FragmentChatOrderBase  extends FragmentBase
	implements LoaderCallbacks<Cursor> {
	
	protected LoadingListView mLoadingListView ;
	protected ChatListCursorAdapter mAdapter ;
	
	protected int mLoaderId;;
	protected int chatListSortType;
	protected int chatSortType;
	
	protected String mDelNick ;
	protected long mDelUid ;
	
	protected int delTransactionId ;
	protected int sortTransactionId ;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EgmService.getInstance().addListener(mCallBack);
	}
	
	@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EgmConstants.KEY_IS_ONLYNEW, onlyNew);
    }
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		EgmService.getInstance().removeListener(mCallBack);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		LinearLayout root = (LinearLayout) inflater.inflate(R.layout.fragment_chat_list,null,false);
		init(root);
		if(savedInstanceState != null){
		    onlyNew = savedInstanceState.getBoolean(EgmConstants.KEY_IS_ONLYNEW);
            load(onlyNew);
        }
		return root ;
	}
	
	private void init(View root){
		mLoadingListView = (LoadingListView) root.findViewById(R.id.chat_list);
		mLoadingListView.disableLoadingMore();
		mLoadingListView.disablePullToRefresh();
		mLoadingListView.setOnNoContentListener(new OnNoContentListener(){
			@Override
			public void onNoContent() {
				if (FragmentYuanfen.mIsYuanfenOpen) {
					CustomViewPager pager = ((ActivityHome)(FragmentChatOrderBase.this.getActivity())).getViewPager();
					if(pager != null){
						pager.setCurrentItem(0);
					}
				} else {
					ActivityYuanfen.startActivity(getActivity(), false);
				}
			}
		});
		
		mLoadingListView.setOnItemClickListener(mOnItemClickListener);
		mLoadingListView.getRefreshableView().setOnItemLongClickListener(mOnItemLongClickListener);
		
		mLoadingListView.setOnLoadingListener(new OnLoadingListener(){
			@Override
			public void onRefreshing() {
			}

			@Override
			public void onLoading() {
				FragmentChatOrderBase.this.onLoading();
			}

			@Override
			public void onLoadingMore() {
			}
		});
		
		mAdapter = new ChatListCursorAdapter(getActivity(),null);
		mLoadingListView.setAdapter(mAdapter);
	}
	
	private void setNoContentText() {
		if (FragmentYuanfen.mIsYuanfenOpen) {
			mLoadingListView.setNoContentString(getString(R.string.girl_tip));
			mLoadingListView.setNoContentActionString(getString(R.string.go_to_chat));
		} else {
			mLoadingListView.setNoContentString(getString(R.string.girl_tip_no_msg_no_yuanfen));
			mLoadingListView.setNoContentActionString(getString(R.string.open_peng_yuanfen));
		}
	}
	
	protected void onLoading() {
	}
	
	protected void getChatList() {
		//获取聊天列表
		EgmService.getInstance().doGetChatList();
	}
	
	protected void getSortChatList() {
		long[] uids = LastMsgDBManager.getUids();
		if(uids != null && uids.length > 0){
			sortTransactionId = EgmService.getInstance().doGetSortList(chatSortType, uids);
		}else{
			mLoadingListView.onNoContent();
		}
	}
	
	protected OnItemClickListener mOnItemClickListener = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
			if (/*mLoadingListView.getLoadingState() == LoadingListView.STATE_LOADING
					||*/ mLoadingListView.getLoadingState() == LoadingListView.STATE_NO_CONTENT
					|| mLoadingListView.getLoadingState() == LoadingListView.STATE_NO_NETWORK|| mAdapter.getCount() == 0) {
				return ;
			}
			int count = mLoadingListView.getRefreshableView().getHeaderViewsCount();
			ActivityPrivateSession.startActivity(
					FragmentChatOrderBase.this,
					getChatItemUserInfo(position - count));
		}
	};
	
	/**
	 * 获取ChatItemUserInfo
	 * @param position
	 * @return
	 */
	protected ChatItemUserInfo getChatItemUserInfo(int position){
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
	
	/**
	 * 长按监听
	 */
	protected OnItemLongClickListener mOnItemLongClickListener = new OnItemLongClickListener(){
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,int position, long id) {
			
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
	
	protected AlertDialog mDelChatItemDialog ;
	protected void showDelChatItemDialog(){
//		if(mDelChatItemDialog == null){
			mDelChatItemDialog = EgmUtil.createEgmMenuDialog(
					getActivity(), 
					mDelNick, 
					new CharSequence[]{getResources().getString(R.string.delete_chat_item)}, 
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							delTransactionId = EgmService.getInstance().doDelChatListItem(String.valueOf(mDelUid));
							mDelChatItemDialog.dismiss();
							mDelChatItemDialog = null;
						}
					});
//		}
		mDelChatItemDialog.show();
	}
	
	protected boolean isShowNoNewTip = false;
	public void reLoad(boolean onlyNew){
		this.onlyNew = onlyNew ;
		if(onlyNew) {
			isShowNoNewTip = true;
		}
		this.getLoaderManager().restartLoader(mLoaderId,null,this);
	}
	
	/**
     * 设置底部消息未读数目
     */
	protected void setUnReadNum(){
        HomeTabView tabView = ((ActivityHome)getActivity()).getChatTab() ;
        int count = LastMsgDBManager.getUnReadNum() ;
        count = (count > 99 ? 99 : count) ;
        if(tabView != null){
            if(count != 0){
                tabView.setTipCount(count);
            }else if(count == 0){
                tabView.setTipLayoutVisibility(View.GONE);
            }
        }
    }
	
	protected boolean onlyNew = false ;
	public void load(boolean onlyNew){
//		if(this.onlyNew != onlyNew){
//			this.onlyNew = onlyNew ;
//			mLoadingListView.reLoad();
//			return ;
//		}
//		mLoadingListView.load();
		
		this.onlyNew = onlyNew ;
		setNoContentText();
		mLoadingListView.reLoad();
	}
	
	
	protected EgmCallBack mCallBack = new EgmCallBack(){
		
		@Override
		public void onGetChatListSucess(int transactionId,List<ChatItemInfo> obj) {
			FragmentChatOrderBase.this.onGetChatListSucess(transactionId, obj);
		}
		
		@Override
		public void onGetChatListError(int transactionId, int errCode,String err) {
			FragmentChatOrderBase.this.onGetChatListError(transactionId, errCode, err);
		}
		
		@Override
		public void onSortChatListSucess(int transactionId,
				SortChatListResult obj) {
			FragmentChatOrderBase.this.onSortChatListSucess(transactionId, obj);
		}

		@Override
		public void onSortChatListError(int transactionId, int errCode,
				String err) {
			FragmentChatOrderBase.this.onSortChatListError(transactionId, errCode, err);
		}
		
		
		@Override
		public void onDelChatListSucess(int transactionId, int code) {
			if(delTransactionId == transactionId){
				ToastUtil.showToast(getActivity(),R.string.delete_chat_item_success);
//				LastMsgDBManager.delMsgByUid(mDelUid);
//				MsgDBManager.delMsgByUid(mDelUid);

				if (mAdapter.getCount() == 0) { // 表示消息从有变成无了，要重新加载，使得listview显示noContent的提示
					getLoaderManager().restartLoader(mLoaderId,null,FragmentChatOrderBase.this);
				}
				setUnReadNum();
			}
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
					getLoaderManager().restartLoader(mLoaderId,null,FragmentChatOrderBase.this);
				}
			}
		}
	};
	
	protected void onGetChatListSucess(int transactionId,List<ChatItemInfo> obj) {
		getLoaderManager().restartLoader(mLoaderId,null,FragmentChatOrderBase.this);
		setUnReadNum();
	}
	
	protected void onGetChatListError(int transactionId, int errCode,String err) {
		mLoadingListView.onLoadingComplete();
		getLoaderManager().restartLoader(mLoaderId,null,FragmentChatOrderBase.this);
		ToastUtil.showToast(getActivity(), err);
	}
	
	protected void onSortChatListSucess(int transactionId, SortChatListResult obj) {
		if(sortTransactionId == transactionId){
			if(obj == null || obj.sortList == null || obj.sortList.length == 0){
				return ;
			}
			LastMsgDBManager.updateRichOrIntimacy(obj.sortList,true);
			getLoaderManager().restartLoader(mLoaderId,null,FragmentChatOrderBase.this);
		}
	}
	
	protected void onSortChatListError(int transactionId, int errCode, String err) {
		if(sortTransactionId == transactionId){
			mLoadingListView.onLoadingComplete();
			if(errCode == EgmServiceCode.NETWORK_ERR
					|| errCode == EgmServiceCode.NETWORK_ERR_COMMON){
				mLoadingListView.onNoNetwork();
			}
			ToastUtil.showToast(getActivity(), err);
		}
	}
	
	protected boolean isVisiable ;
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		isVisiable = isVisibleToUser ;
	}

	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return LoaderFactory.getLoader(getActivity(), chatListSortType, onlyNew);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		if(arg0.getId() == mLoaderId && isVisiable){
			
			mAdapter.swapCursor(cursor);
			if(cursor.getCount() == 0){
				setNoContentText();
				if(onlyNew){
					if (isShowNoNewTip) {
						isShowNoNewTip = false;
						ToastUtil.showToast(getActivity(),R.string.no_new_tip);
					}
				}
				mLoadingListView.onNoContent();
			} else {
				mLoadingListView.onLoadingComplete();
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.swapCursor(null);
	}

}
