package com.netease.engagement.fragment;

import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.cursorloader.LoaderFactory;
import com.netease.service.protocol.meta.SortChatListResult;


/**
 * 按照时间进行排序
 */
public class FragmentChatOrderTime extends FragmentChatOrderBase {
	
	public static FragmentChatOrderTime newInstance(){
		FragmentChatOrderTime fragment = new FragmentChatOrderTime();
		fragment.mLoaderId = FragmentChatOrderTime.class.getSimpleName().hashCode();
		fragment.chatListSortType = LoaderFactory.CHAT_LIST_SORT_TIME;
		fragment.chatSortType = EgmConstants.Chat_Sort_Type.TYPE_TIME;
		return fragment ;
	}

	@Override
	protected void onLoading() {
		getChatList();
	}
	
	// 按时间排序，不处理SortChatList方法，做屏蔽用
	@Override
	protected void onSortChatListSucess(int transactionId, SortChatListResult obj) {
	}
	@Override
	protected void onSortChatListError(int transactionId, int errCode, String err) {
	}
	
	public void scrollToTop() {
		if(mLoadingListView.getRefreshableView().getFirstVisiblePosition() != 0) {
			mLoadingListView.getRefreshableView().smoothScrollToPosition(0);
		}
	}

}
