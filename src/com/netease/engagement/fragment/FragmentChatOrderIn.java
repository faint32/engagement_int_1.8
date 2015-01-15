package com.netease.engagement.fragment;

import java.util.List;

import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.cursorloader.LoaderFactory;
import com.netease.service.protocol.meta.ChatItemInfo;


/**
 * 按照亲密度进行排序
 */
public class FragmentChatOrderIn extends FragmentChatOrderBase {
	
	public static FragmentChatOrderIn newInstance(){
		FragmentChatOrderIn fragment = new FragmentChatOrderIn();
		fragment.mLoaderId = FragmentChatOrderIn.class.getSimpleName().hashCode();
		fragment.chatListSortType = LoaderFactory.CHAT_LIST_SORT_INIT;
		fragment.chatSortType = EgmConstants.Chat_Sort_Type.TYPE_IN;
		return fragment ;
	}

	@Override
	protected void onLoading() {
		getSortChatList();
	}

	// 按亲密度排序，不处理GetChatList方法，做屏蔽用
	@Override
	protected void onGetChatListSucess(int transactionId,List<ChatItemInfo> obj) {
	}
	@Override
	protected void onGetChatListError(int transactionId, int errCode,String err) {
	}
	
}
