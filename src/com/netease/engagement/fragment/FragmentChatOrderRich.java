package com.netease.engagement.fragment;

import java.util.List;

import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.cursorloader.LoaderFactory;
import com.netease.service.protocol.meta.ChatItemInfo;


/**
 * 按照豪气值进行排序
 */
public class FragmentChatOrderRich extends FragmentChatOrderBase {
	
	public static FragmentChatOrderRich newInstance(){
		FragmentChatOrderRich fragment = new FragmentChatOrderRich();
		fragment.mLoaderId = FragmentChatOrderRich.class.getSimpleName().hashCode();
		fragment.chatListSortType = LoaderFactory.CHAT_LIST_SORT_RICH;
		fragment.chatSortType = EgmConstants.Chat_Sort_Type.TYPE_RICH;
		return fragment ;
	}

	@Override
	protected void onLoading() {
		getSortChatList();
	}
	
	// 按豪气值排序，不处理GetChatList方法，做屏蔽用
	@Override
	protected void onGetChatListSucess(int transactionId,List<ChatItemInfo> obj) {
	}
	@Override
	protected void onGetChatListError(int transactionId, int errCode,String err) {
	}

}
