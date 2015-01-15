package com.netease.engagement.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.netease.date.R;
import com.netease.engagement.dataMgr.ChatListMeta;
import com.netease.engagement.itemview.RenderChatListItem;
import com.netease.service.db.MsgDBTables;


/**
 * 聊天列表adapter
 */
public class ChatListCursorAdapter extends CursorAdapter {
	
	public ChatListCursorAdapter(Context context ,Cursor c){
		super(context,c,true);
	}

	public ChatListCursorAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, false);
	}

	public ChatListCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
	}

	@Override
	public void bindView(View view, Context arg1, Cursor cursor) {
		RenderChatListItem item = null ;
		if(view != null && view.getTag() != null
				&& view.getTag() instanceof RenderChatListItem){
			item = (RenderChatListItem) view.getTag() ;
		}
		ChatListMeta meta = ChatListMeta.fillData(cursor);
		item.renderView(meta);
	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		View view = View.inflate(mContext, R.layout.item_view_chat_list, null);
		RenderChatListItem item = new RenderChatListItem(view, mContext);
		view.setTag(item);
		return view;
	}

	@Override
	public Long getItem(int position) {
		Cursor cursor = this.getCursor();
		if(cursor != null && cursor.getCount() > 0 && cursor.moveToPosition(position)){
			return cursor.getLong(cursor.getColumnIndex(MsgDBTables.LastMsgTable.C_ANOTHERID));
		}
		return null ;
	}
}
