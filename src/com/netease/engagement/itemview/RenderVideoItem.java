package com.netease.engagement.itemview;

import java.io.File;

import android.app.AlertDialog;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityFire;
import com.netease.engagement.activity.ActivityVideoPlay;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.MemoryDataCenter;
import com.netease.engagement.dataMgr.MsgAttach;
import com.netease.engagement.fragment.FragmentPrivateSession;
import com.netease.engagement.widget.LoadingImageView;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.LoopBack;
import com.netease.service.protocol.meta.MessageInfo;


public class RenderVideoItem extends RenderBase implements OnClickListener ,OnLongClickListener{
	
	private View mRoot ;
	private LoadingImageView mLoadingImageView ;
	
	private MessageInfo mMsgInfo ;
	private long myId ;
	private int mImageWidth = 120 ;
	private String mNick ;
	private String videoPath ;
	
	public RenderVideoItem(View root){
		mRoot = root ;
		mLoadingImageView = (LoadingImageView)root.findViewById(R.id.image);
		mLoadingImageView.setOnClickListener(this);
		mLoadingImageView.setOnLongClickListener(this);
		
		mImageWidth = EgmUtil.dip2px(root.getContext(),mImageWidth);
		myId = ManagerAccount.getInstance().getCurrentId();
		
		if(MemoryDataCenter.getInstance().get(MemoryDataCenter.CURRENT_CAHT_OTHER_NICK) != null){
			mNick = (String)(MemoryDataCenter.getInstance().get(MemoryDataCenter.CURRENT_CAHT_OTHER_NICK));
		}
	}
	
	public void renderView(MessageInfo msgInfo, String nick){
		mNick = nick;
		
		mMsgInfo = msgInfo ;
		mLoadingImageView.setImageBitmap(null);
		String uri = null ;
		MsgAttach msgAttach = null ;
		
		if(msgInfo.sender == myId && !TextUtils.isEmpty(msgInfo.attach)){
			msgAttach = MsgAttach.toMsgAttach(msgInfo.attach);
			
			if (! TextUtils.isEmpty(msgAttach.smallImagePath)) {
				uri = Uri.fromFile(new File(msgAttach.smallImagePath)).toString();
			}
			videoPath = msgAttach.getVideoPath();
		}else{
			uri = msgInfo.msgContent ;
			videoPath = msgInfo.mediaUrl ;
		}
		mLoadingImageView.setLoadingImage(uri);
	}

	@Override
	public boolean onLongClick(View v) {
		if(mMsgInfo.status != EgmConstants.Sending_State.SENDING){
			showDelChatItemDialog();
		}
		return true;
	}
	
	private AlertDialog mDelChatItemDialog ;
	private void showDelChatItemDialog(){
		CharSequence[] operations = null ;
		switch(mMsgInfo.status){
			case EgmConstants.Sending_State.SEND_SUCCESS:
				operations = new CharSequence[]{mRoot.getContext().getResources().getString(R.string.delete_audio)};
				break;
			case EgmConstants.Sending_State.SEND_FAIL:
				operations = mRoot.getContext().getResources().getStringArray(R.array.chat_list_item_operation_resend_del);
				break;
		}
		mDelChatItemDialog = EgmUtil.createEgmMenuDialog(
				mRoot.getContext(), 
				mNick, 
				operations, 
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int which = (Integer)v.getTag();
						if(mMsgInfo.status == EgmConstants.Sending_State.SEND_SUCCESS){
							delMsg();
						}else if(mMsgInfo.status == EgmConstants.Sending_State.SEND_FAIL){
							switch(which){
								case 0:
									LoopBack lp = new LoopBack();
									lp.mType = EgmConstants.LOOPBACK_TYPE.msg_resend;
									lp.mData = mMsgInfo ;
									EgmService.getInstance().doLoopBack(lp);
									break;
								case 1:
									delMsg();
									break;
							}
						}
						mDelChatItemDialog.dismiss();
					}
				});
		mDelChatItemDialog.show();
	}
	
	private void delMsg(){
		LoopBack lp = new LoopBack();
		lp.mType = EgmConstants.LOOPBACK_TYPE.msg_delete ;
		lp.mData = mMsgInfo ;
		EgmService.getInstance().doLoopBack(lp);
	}

	@Override
	public void onClick(View v) {
		if (myId!=mMsgInfo.sender && mMsgInfo.sendType==EgmProtocolConstants.MSG_SENDTYPE.MSG_SENDTYPE_FIRE) {
			Long time = adapter.getFireStart(mMsgInfo);
			if (time == null) {
				FragmentPrivateSession.notNeedRefresh = true;
				adapter.setItemJumpToFireAudioOrView(outerContinaer);
				ActivityFire.startActivityForVideo(mRoot.getContext(), mMsgInfo);
			}
		} else {
			if(!TextUtils.isEmpty(videoPath)){
				FragmentPrivateSession.notNeedRefresh = true;
				ActivityVideoPlay.startActivity(mRoot.getContext(),videoPath);
			}
		}
	}
}
