package com.netease.engagement.adapter;

import java.util.List;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.netease.date.R;
import com.netease.engagement.itemview.RenderChatListItem;
import com.netease.service.protocol.meta.ChatItemInfo;
/**
 * 聊天列表页面adapter
 */
public class ChatListAdapter extends BaseAdapter{
	
	private Context mContext ;
	
	private List<ChatItemInfo> mChatItemList ;
	
	public ChatListAdapter(Context context ,List<ChatItemInfo> mChatItemList){
		mContext = context ;
		this.mChatItemList = mChatItemList ;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = View.inflate(mContext, R.layout.item_view_chat_list, null);
			RenderChatListItem item = new RenderChatListItem(convertView ,mContext);
			convertView.setTag(item);
		}
		RenderChatListItem item = (RenderChatListItem) convertView.getTag();
		//item.renderView(mChatItemList.get(position));
		return convertView ;
	}

	@Override
	public int getCount() {
		return mChatItemList == null ? 0 : mChatItemList.size() ;
	}

	@Override
	public Object getItem(int position) {
		return mChatItemList == null ? null : mChatItemList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
