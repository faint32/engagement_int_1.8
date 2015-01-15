package com.netease.engagement.itemview;

import android.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.GiftInfoManager;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.db.manager.ManagerAccount.Account;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.GiftInfo;
import com.netease.service.protocol.meta.LoopBack;
import com.netease.service.protocol.meta.MessageInfo;


public class RenderGiftItem extends RenderBase {
	
	private View mRoot ;
	private ImageView mGiftImage ;
	private TextView mGiftName ;
	private TextView mGiftTip ;
	
	private MessageInfo mMsgInfo ;
	private String mNick ;
	
	private long uid ;
	
	public RenderGiftItem(View root){
		
		mRoot = root ;
		
		mGiftImage = (ImageView)root.findViewById(R.id.gift_image);
		mGiftName = (TextView)root.findViewById(R.id.gift_name);
		mGiftTip = (TextView)root.findViewById(R.id.gift_tip);
		
		uid = ManagerAccount.getInstance().getCurrentId();
		
	}
	
	public void renderView(MessageInfo msgInfo, String nick){
		mNick = nick;
		
		mMsgInfo = msgInfo ;
		
		int giftId = Integer.parseInt(String.valueOf(msgInfo.extraId));
		
		GiftInfo info = GiftInfoManager.getGiftInfoById(giftId);
		
		GiftInfoManager.setGiftInfo(giftId, info, mGiftImage);
		
		if (info == null) {
			mGiftName.setText("送你一份礼物");
			mGiftTip.setVisibility(View.GONE);
		}
		else if(msgInfo.sender == uid){
			mGiftName.setText("送出" + info.name);
			mGiftTip.setText("豪气值  +" + msgInfo.usercp);
			mGiftTip.setVisibility(View.VISIBLE);
		} else{
			mGiftName.setText("送你 " + info.name);
			mGiftTip.setText("魅力值 +" + msgInfo.usercp);
			mGiftTip.setVisibility(View.VISIBLE);
		}
		
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
