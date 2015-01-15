package com.netease.engagement.adapter;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.itemview.RenderMsgListItemAnother;
import com.netease.engagement.itemview.RenderMsgListItemBase;
import com.netease.engagement.itemview.RenderMsgListItemMy;
import com.netease.engagement.itemview.RenderMsgListItemSystem;
import com.netease.service.db.MsgDBTables.MsgTable;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.ChatItemUserInfo;
import com.netease.service.protocol.meta.MessageInfo;


public class MsgListCursorAdapter extends CursorAdapter{
	
	private static final int TYPE_COUNT = 3;
	
	private static final int TYPE_MY = 0;
	private static final int TYPE_ANOTHER = 1;
	private static final int TYPE_SYSTEM = 2;
	
	private static final long MAX_FIRE_MSG_LIFE = 8 * 24 * 3600 * 1000;
	
	public MsgListCursorAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, false);
		
		init();
	}

	public MsgListCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		
		init();
	}

	public MsgListCursorAdapter(Context context, Cursor c) {
		super(context,c,true);
		
		init();
	}

	private boolean timeShow = false ;
	private UploadPictureHelper mUploadPictureHelper;
	private ChatItemUserInfo mChatUser;
	private boolean isFromUserInfo;
	private long myId;
	private boolean isOpenFire = false;
	
	private boolean isResumed = true;
	
	private long mCreateTime;
	
	public static final int DEFAULT_FIRE_DURATION = 10 * 1000;
	
	private static final int FIRE_UPDATE_TIME = 200;
	
	private RenderMsgListItemBase itemJumpToFireAudioOrView;
	
	// 开始时间
	public static Map<MessageInfo, Long> FireingMessages = new HashMap<MessageInfo, Long>();
	
	private static HashSet<MessageInfo> FireMessageIds = new HashSet<MessageInfo>();
	
	private static Map<MessageInfo, RenderMsgListItemBase> FireMessageMap
		= new HashMap<MessageInfo, RenderMsgListItemBase>();
	
	private static ChatItemUserInfo FireUserInfo = null;
	
	private static Handler FireHandler = new Handler(Looper.getMainLooper());
	
	private void init() {
		mCreateTime = System.currentTimeMillis();
	}
	
	public void onResume() {
		isResumed = true;
	}
	
	public void onPause() {
		isResumed = false;
	}
	
	public void onDestroy() {
		FireMessageMap.clear();
	}
	
	public void setUploadPictureHelper(UploadPictureHelper helper){
	    mUploadPictureHelper = helper;
	}
	
	public void setChatUser(ChatItemUserInfo info) {
		this.mChatUser = info;
		
		FireUserInfo = info;
	}
	
	public void setFromUserInfo(boolean isFromUserInfo) {
		this.isFromUserInfo = isFromUserInfo;
	}
	
	public void setMyId(long myId) {
		this.myId = myId;
	}
	
	public void setOpenFire(boolean isOpenFire) {
		this.isOpenFire = isOpenFire;
	}
	
	public Long getFireStart(MessageInfo info) {
		return FireingMessages.get(info);
	}
	
	public long setFireStart(RenderMsgListItemBase base) {
		return setFireStart(base, System.currentTimeMillis());
	}
	
	public long setFireStart(RenderMsgListItemBase base, long time) {
		MessageInfo simple = base.getMessageInfo().getSimpleInfo();
		
		FireingMessages.put(simple, time);
		
		updateFireProgressBar(base, mChatUser, base.getMessageInfo());
		
		return time;
	}
	
	public void setFireStart(MessageInfo[] list) {
		if (list != null && list.length > 0) {
			int size = list.length;
			long time = 0;//System.currentTimeMillis();
			
			for (int i = 0; i < size; i++) {
				MessageInfo info = list[i];
				
				if (! FireingMessages.containsKey(info)) {
					FireingMessages.put(info, time);
				}
			}
		}
	}
	
	public static long setFireStart(MessageInfo info) {
		long time = System.currentTimeMillis();
		
		return setFireStart(info, time);
	}
	
	/**
	 * 开始焚毁倒计时
	 * 
	 * @param info
	 * @param time 开始时间
	 * @return
	 */
	public static long setFireStart(MessageInfo info, long time) {
		if (info != null) {
			ChatItemUserInfo userInfo = FireUserInfo;
			
			if (! FireingMessages.containsKey(info)) {
				FireingMessages.put(info, time);
				
				if (! FireMessageIds.contains(info) && userInfo != null) {
					FireMessageIds.add(info);
					
					FireHandler.postDelayed(
							new FireProgressUpdate(info, userInfo), 
							FIRE_UPDATE_TIME);
				}
			}
		}
		
		return time;
	}
	
	public RenderMsgListItemBase getItemJumpToFireAudioOrView() {
		return itemJumpToFireAudioOrView;
	}

	public void setItemJumpToFireAudioOrView(
			RenderMsgListItemBase itemJumpToFireAudioOrView) {
		this.itemJumpToFireAudioOrView = itemJumpToFireAudioOrView;
	}

	@Override
	public void bindView(View view, Context arg1, Cursor cursor) {
		RenderMsgListItemBase item = null ;
		if(view != null && view.getTag() != null 
				&& view.getTag() instanceof RenderMsgListItemBase){
			item = (RenderMsgListItemBase)view.getTag();
		}
		
		view.setVisibility(View.VISIBLE);
		
		if(cursor != null){
			long time = Long.parseLong(cursor.getString(cursor.getColumnIndex(MsgTable.C_TIME)));
			MessageInfo info = MessageInfo.getMessageInfo(cursor);
			
			if(cursor.moveToPrevious()){
				long lastTime = Long.parseLong(cursor.getString(cursor.getColumnIndex(MsgTable.C_TIME)));
				timeShow = (time - lastTime) > EgmConstants.MSG_INTERVAL ? true : false ;
				cursor.moveToNext();
			}else{
				timeShow = true ;
			}
			
			if(info != null){
				item.renderView(info,timeShow, isOpenFire, mChatUser.nick);
				
				if (info.isFireMsg() && item.getFireProgressBar() != null) {
					// 超期消息处理
					if (isResumed) {
						if (mCreateTime - info.getTime() >= MAX_FIRE_MSG_LIFE) {
							if (! FireingMessages.containsKey(info)) {
								FireingMessages.put(info, 0l);
							}
						}
						else if (info.isFireMsgOpened() 
								&& ! FireingMessages.containsKey(info)) {
							FireingMessages.put(info, 0l);
						}
					}

					FireMessageMap.put(info, item);
					
					updateFireProgressBar(item, mChatUser, info);
				}
				else if (item.getFireProgressBar() != null) {
					item.getFireProgressBar().setVisibility(View.GONE);
				}
				
				if (info.getType() == EgmProtocolConstants.MSG_TYPE.MSG_TYPE_AUDIO
						&& ! info.isFireMsg()) {
					EgmService.getInstance().doDowloadMsgRes(info);
				}
			}
		}
	}

	@Override
	public View newView(Context context, Cursor arg1, ViewGroup arg2) {
		View view = null;
		int position = arg1.getPosition();
		int type = getItemViewType(position);
		RenderMsgListItemBase item;
		if (type == TYPE_MY) {
			view = View.inflate(context, R.layout.item_view_chat_my, null);
			item = new RenderMsgListItemMy(view, this);
		} else if (type == TYPE_ANOTHER) {
			view = View.inflate(context, R.layout.item_view_chat_another, null);
			item = new RenderMsgListItemAnother(view, this);
		} else {
			view = View.inflate(context, R.layout.item_view_chat_system, null);
			item = new RenderMsgListItemSystem(view, this);
		}
		item.setFromUserInfo(isFromUserInfo);
		item.setUploadPictureHelper(mUploadPictureHelper);
		view.setTag(item);
		return view;
	}
	
	@Override
	public int getViewTypeCount() {
		return TYPE_COUNT;
	}
	
	@Override
	public int getItemViewType(int position) {
		Cursor cursor = getCursor();
		cursor.moveToPosition(position);
		MessageInfo info = MessageInfo.getMessageInfo(cursor);
		if (info.sender == myId) {
			return TYPE_MY;
		} else if (info.sender == EgmConstants.System_Sender_Id.TYPE_XIAOAI
				|| info.sender == EgmConstants.System_Sender_Id.TYPE_YIXIN) {
			return TYPE_SYSTEM;
		} else {
			return TYPE_ANOTHER;
		}
	}
	
	private static boolean updateFireProgressBar(RenderMsgListItemBase item,
			ChatItemUserInfo userInfo, MessageInfo msgInfo) {
		ProgressBar fireProgress = item.getFireProgressBar();
		
		msgInfo = msgInfo.getSimpleInfo();
		
		Long start = FireingMessages.get(msgInfo);
		if (start != null) {
			long time = System.currentTimeMillis() - start.longValue();
			int progress = 100 - (int) ((100 * time) / DEFAULT_FIRE_DURATION);

			if (progress < 0 || progress > 100) {
				item.fireItemAnimation(userInfo);

				FireMessageMap.remove(msgInfo);
				FireMessageIds.remove(msgInfo);
				FireingMessages.remove(msgInfo);
			} else {
				fireProgress.setVisibility(View.VISIBLE);
				item.visibleKeywordsView();
				fireProgress.setProgress(progress);
				
				if (! FireMessageIds.contains(msgInfo)) {
					FireMessageIds.add(msgInfo);

					FireHandler.postDelayed(
							new FireProgressUpdate(msgInfo, userInfo),
							FIRE_UPDATE_TIME);
					
					return true;
				}
				
				return false;
			}
		}

		fireProgress.setVisibility(View.GONE);
		
		return false;
	}
	
	private static class FireProgressUpdate implements Runnable {
		
		private ChatItemUserInfo mUserInfo;
		private MessageInfo mMsg;
		
		public FireProgressUpdate(MessageInfo msg, ChatItemUserInfo userInfo) {
			mMsg = msg;
			mUserInfo = userInfo;
		}

		@Override
		public void run() {
			RenderMsgListItemBase item = FireMessageMap.get(mMsg);
			Long time = FireingMessages.get(mMsg);
			
			if (time == null) {
				return ;
			}
			
			long now = System.currentTimeMillis();
			
			if (item != null) {
				MessageInfo info = item.getMessageInfo();
				
				if (info != null && info.equals(mMsg)) {
					now = -1;
					updateFireProgressBar(item, mUserInfo, mMsg);
				}
			}
			
			if (now <= DEFAULT_FIRE_DURATION + time.longValue()) {
				FireHandler.postDelayed(this, FIRE_UPDATE_TIME);
			}
			else {
//				LoopBack lp = new LoopBack();
//				lp.mType = EgmConstants.LOOPBACK_TYPE.msg_delete ;
//				lp.mData = mMsg ;
//				EgmService.getInstance().doLoopBack(lp);
				
				EgmService.getInstance().doDelMsg(mMsg, mUserInfo, false);
			}
		}
	}
	
	public void returnFromFireAudioOrVideo() {
		if (itemJumpToFireAudioOrView != null) {
			setFireStart(itemJumpToFireAudioOrView, 0);
			itemJumpToFireAudioOrView = null;
		}
	}
}
