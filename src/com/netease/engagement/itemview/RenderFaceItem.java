package com.netease.engagement.itemview;

import android.app.AlertDialog;
import android.view.View;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.widget.FaceImageView;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.LoopBack;
import com.netease.service.protocol.meta.MessageInfo;

public class RenderFaceItem extends RenderBase {
	
	private View mRoot ;
	private MessageInfo mMsgInfo ;
	private String mNick ;
	
	private FaceImageView mFaceIV;
	
	public RenderFaceItem(View root){
		mRoot = root ;
		
		mFaceIV = (FaceImageView) root.findViewById(R.id.faceIV);
	}

	@Override
	public void renderView(MessageInfo msgInfo, String nick) {
		mNick = nick;
		
		mMsgInfo = msgInfo ;
		
		mFaceIV.setFaceImage(msgInfo.getFaceId(), msgInfo.getMediaUrl());
		
		mRoot.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if(mMsgInfo.status != EgmConstants.Sending_State.SENDING){
					showDelChatItemDialog();
				}
				return true;
			}
		});
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
}
