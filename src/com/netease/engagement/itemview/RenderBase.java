package com.netease.engagement.itemview;

import com.netease.engagement.adapter.MsgListCursorAdapter;
import com.netease.service.protocol.meta.MessageInfo;

/**
 * 除了RenderChatText以外，所有Render item类型的父类
 */
public abstract class RenderBase {
	
	protected RenderMsgListItemBase outerContinaer;
	
	protected MsgListCursorAdapter adapter;

	public MsgListCursorAdapter getAdapter() {
		return adapter;
	}

	public void setAdapter(MsgListCursorAdapter adapter) {
		this.adapter = adapter;
	}

	public RenderMsgListItemBase getOuterContinaer() {
		return outerContinaer;
	}

	public void setOuterContinaer(RenderMsgListItemBase outerContinaer) {
		this.outerContinaer = outerContinaer;
	}
	
	public abstract void renderView(MessageInfo msgInfo, String nick);
}
