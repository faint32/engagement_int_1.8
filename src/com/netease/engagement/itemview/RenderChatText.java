package com.netease.engagement.itemview;

import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.widget.richtext.view.RichTextView;
import com.netease.framework.widget.ToastUtil;
import com.netease.service.Utils.EgmUtil;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmService;
import com.netease.service.protocol.meta.LoopBack;
import com.netease.service.protocol.meta.MessageInfo;

/**
 * 聊天文本项
 */
public class RenderChatText extends RenderBase {
	
	private RichTextView mRichTextView ;
	private TextView mRichTextViewCover ;
	private View mRoot ;
	
	private MessageInfo mMsgInfo ;
	private String mNick ;
	
	private long myId = 0;
	
	public RenderChatText(View root){
		mRoot = root ;
		mRichTextView = (RichTextView)root.findViewById(R.id.text_content);
		mRichTextViewCover = (TextView)root.findViewById(R.id.text_content_cover);
	}
	
	public void renderView(final MessageInfo msgInfo, String nick){
		mNick = nick;
		
		OnLongClickListener longListener = new OnLongClickListener(){
			@Override
			public boolean onLongClick(View v) {
				if(mMsgInfo.status != EgmConstants.Sending_State.SENDING){
					showDelChatItemDialog();
				}
				return true;
			}
		};
		
		mMsgInfo = msgInfo ;
		mRichTextView.setOnLongClickListener(longListener);
		mRichTextView.setRichText(msgInfo.msgContent);
		
		mRichTextView.setVisibility(View.VISIBLE);
		if (mRichTextViewCover != null) { // 自己放出的消息，没有Cover层
			mRichTextViewCover.setVisibility(View.GONE);
		}
		
		if (msgInfo.sendType == EgmProtocolConstants.MSG_SENDTYPE.MSG_SENDTYPE_FIRE) {
			if (myId == 0) {
				myId = ManagerAccount.getInstance().getCurrentId() ;
			}
			if (msgInfo.sender != myId) {
				Long time = adapter.getFireStart(msgInfo);
				if (time == null) {
					mRichTextView.setVisibility(View.GONE);
					mRichTextViewCover.setVisibility(View.VISIBLE);
					mRichTextViewCover.setOnLongClickListener(longListener);
					
					mRichTextViewCover.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							mRichTextView.setVisibility(View.VISIBLE);
							mRichTextViewCover.setVisibility(View.GONE);
							
							adapter.setFireStart(getOuterContinaer());
							delFireMsg();
						}
					});
				}
			}
		}
	}
	
	private AlertDialog mDelChatItemDialog ;
	private void showDelChatItemDialog(){
		CharSequence[] operations = null ;
		switch(mMsgInfo.status){
			case EgmConstants.Sending_State.SEND_SUCCESS:
				if (mMsgInfo.sender!=myId && mMsgInfo.isFireMsg()) { // 收到的阅后即焚消息
					operations = mRoot.getContext().getResources().getStringArray(R.array.chat_list_item_operation_del);
				} else {
					operations = mRoot.getContext().getResources().getStringArray(R.array.chat_list_item_operation);
				}
				break;
			case EgmConstants.Sending_State.SEND_FAIL:
				operations = mRoot.getContext().getResources().getStringArray(R.array.chat_list_item_operation_more);
				break;
		}
		mDelChatItemDialog = EgmUtil.createEgmMenuDialog(
				mRoot.getContext(), 
				mNick, 
				operations,					
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int which = (Integer) v.getTag() ;
						if(mMsgInfo.status == EgmConstants.Sending_State.SEND_SUCCESS){
							if (mMsgInfo.sender!=myId && mMsgInfo.isFireMsg()) { // 收到的阅后即焚消息
								switch(which){
								case 0:
									//删除
									delMsg();
									break;
								}
							} else {
								switch(which){
								case 0:
									//复制
									copyText();
									break;
								case 1:
									//删除
									delMsg();
									break;
							}
							}
						}else if(mMsgInfo.status == EgmConstants.Sending_State.SEND_FAIL){
							switch(which){
							case 0:
								//重发
								LoopBack lp = new LoopBack();
								lp.mType = EgmConstants.LOOPBACK_TYPE.msg_resend;
								lp.mData = mMsgInfo ;
								EgmService.getInstance().doLoopBack(lp);
								break;
							case 1:
								//复制
								copyText();
								break;
							case 2:
								//删除
								delMsg();
								break;
							}
						}
						mDelChatItemDialog.dismiss();
					}
				});
		mDelChatItemDialog.show();
	}
	
	private void copyText(){
		ClipboardManager cmb = (ClipboardManager)mRoot.getContext().getSystemService(mRoot.getContext().CLIPBOARD_SERVICE);  
		cmb.setText(mMsgInfo.msgContent.trim());
		ToastUtil.showToast(mRoot.getContext(),"已复制");
	}
	
	private void delMsg(){
		LoopBack lp = new LoopBack();
		lp.mType = EgmConstants.LOOPBACK_TYPE.msg_delete ;
		lp.mData = mMsgInfo ;
		EgmService.getInstance().doLoopBack(lp);
	}
	
	private void delFireMsg(){
		LoopBack lp = new LoopBack();
		lp.mType = EgmConstants.LOOPBACK_TYPE.msg_fire_delete ;
		lp.mData = mMsgInfo ;
		EgmService.getInstance().doLoopBack(lp);
	}
}
