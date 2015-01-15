package com.netease.service.transactions;


import android.text.TextUtils;

import com.netease.common.http.THttpRequest;
import com.netease.engagement.app.EgmConstants;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.db.manager.ManagerAccount.Account;
import com.netease.service.db.manager.MsgDBManager;
import com.netease.service.protocol.EgmProtocol;
import com.netease.service.protocol.EgmServiceCode;
import com.netease.service.protocol.meta.ChatItemUserInfo;
import com.netease.service.protocol.meta.MessageInfo;

public class DelMsgTransaction extends EgmBaseTransaction{

	private MessageInfo mInfo;
	private long mMyId = -1;
	private ChatItemUserInfo mChatItemUserInfo;
	private boolean mNotifyRemote;
	
	public DelMsgTransaction(MessageInfo info,ChatItemUserInfo anotherUserInfo, 
			boolean notifyRemote) {
		super(TRANSACTION_DEL_MSG);
		mInfo = info;
		mChatItemUserInfo = anotherUserInfo;
		mNotifyRemote = notifyRemote;
		
		Account account = ManagerAccount.getInstance().getCurrentAccount();
		
		mMyId = ManagerAccount.getInstance().getCurrentId();
	}

	@Override
	public void onTransact() {
		delMsg(mInfo);
		notifyMessage(EgmServiceCode.TRANSACTION_SUCCESS,null);
		
		if (mInfo.msgId > 0 && mNotifyRemote) {
			int delType = (mInfo.sender == mMyId)?
					EgmConstants.Del_Msg_Type.TYPE_SEND : EgmConstants.Del_Msg_Type.TYPE_RECEIVE ;
			THttpRequest request = EgmProtocol.getInstance().createDelMsg(mInfo.msgId,delType);
			sendRequest(request);
		} 
		else {
			doEnd();
		}
	}

	@Override
	protected void onEgmTransactionSuccess(int code, Object obj) {
		
	}
	@Override
	protected void onEgmTransactionError(int errCode, Object obj) {
		
	}
	
	private  void delMsg(MessageInfo info){
		MsgDBManager.delMsgWithResource(info);
//		
//		MsgAttach msgAttach = MsgDBManager.getMsgAttach(info.sender,info.msgId);
//		if(msgAttach != null){
//			deleteFile(msgAttach.smallImagePath);
//			deleteFile(msgAttach.audioPath);
//			deleteFile(msgAttach.videoPath);
//			
//			if (! TextUtils.isEmpty(msgAttach.mediaResUrl)) {
//				StoreFile file = CacheManager.getStoreFile(msgAttach.mediaResUrl);
//				
//				if (file != null) {
//					file.delete();
//				}
//			}
//		}
//		
//		MsgDBManager.delMsg(info.sender,info.msgId);
////		FragmentPrivateSession.this.getLoaderManager().restartLoader(mLoaderId,null,FragmentPrivateSession.this);
//		
//		// bug fix #141118、#141119      这种修改方法，不会修改豪气值和亲密度，所以也不会出现bug#141005的情况
//		boolean isExist = LastMsgDBManager.isExistMsgByMsgId(info.sender,info.msgId); 
//		Log.e("isExist", "isExist = " + isExist);
//		if(isExist) { 
//			MessageInfo info1 = MsgDBManager.getLastMsg(mMyId,mChatItemUserInfo.uid);
//			if(info1 != null) {
//				LastMsgDBManager.updateLastMsg(getChatItemInfo(info1));
//			} else {
//				// 本地已经没有与该用户的聊天记录了，那么消息列表中，该用户的记录设置未空记录
//				LastMsgDBManager.updateLastMsgToEmpty(mChatItemUserInfo.uid);
//			}
//		}
	}
	
//	private static void deleteFile(String path) {
//		if(!TextUtils.isEmpty(path)){
//			File file = new File(path);
//			if(file.exists()){
//				file.delete();
//			}
//		}
//	}
//
//	/**
//	 * 获取ChatItemInfo
//	 * @param info
//	 * @return
//	 */
//	private ChatItemInfo getChatItemInfo(MessageInfo info){
//		ChatItemInfo chatItemInfo = new ChatItemInfo();
//		chatItemInfo.message = info ;
//		chatItemInfo.notReadCount = 0 ;
//		chatItemInfo.anotherUserInfo = mChatItemUserInfo ;
//		return chatItemInfo ;
//	}

}
