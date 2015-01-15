package com.netease.engagement.itemview;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityUserPage;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.ChatListMeta;
import com.netease.engagement.view.HeadView;
import com.netease.service.Utils.TimeFormatUtil;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.protocol.EgmProtocolConstants;


/**
 * 聊天列表
 */
public class RenderChatListItem {
	
	private HeadView mProfile ;
	private FrameLayout mUnReadLayout ;
	private TextView mUnReadNum ;
	
	private TextView mTxtTime ;
	private TextView mTxtNick ;
	private TextView mTxtNew ;
	private TextView mTxtMsg ;
	
	private ImageView mImageSending ;
	private ImageView mImageSendFail ;
	
	private RelativeLayout headLayout;
	
	public RenderChatListItem(View root ,final Context context){
		mProfile = (HeadView)root.findViewById(R.id.profile);
		mUnReadLayout = (FrameLayout)root.findViewById(R.id.unread_layout);
		mUnReadNum = (TextView)root.findViewById(R.id.unread_num);
		
		mTxtNick = (TextView)root.findViewById(R.id.txt_nick);
		mTxtNew = (TextView)root.findViewById(R.id.txt_new);
		mTxtTime = (TextView)root.findViewById(R.id.txt_time);
		mTxtMsg = (TextView)root.findViewById(R.id.msg_content);
		
		mImageSending = (ImageView)root.findViewById(R.id.sending);
		mImageSendFail = (ImageView)root.findViewById(R.id.send_fail);
		
		headLayout = (RelativeLayout)root.findViewById(R.id.head_layout);
	}
	
	public void renderView(final ChatListMeta meta){
         int sex = EgmConstants.SexType.Female;
		if(ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Female) {
			sex = EgmConstants.SexType.Male;
		} 
		mProfile.setImageUrl(meta.isVip,HeadView.PROFILE_SIZE_SMALL,meta.portraitUrl192,sex);
		
		headLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// bug fix #141352 by gzlichangjie
				if(meta.anotherId == EgmConstants.System_Sender_Id.TYPE_XIAOAI 
						|| meta.anotherId == EgmConstants.System_Sender_Id.TYPE_YIXIN) {
					return;
				}
				int gender = ManagerAccount.getInstance().getCurrentGender() ^ 1 ;
				ActivityUserPage.startActivity(
						mProfile.getContext(),
						String.valueOf(meta.anotherId), 
						String.valueOf(gender));			
			}
		});
		
		if(!TextUtils.isEmpty(meta.nick)){
			mTxtNick.setText(meta.nick);
		}
		if(meta.isNew){
			mTxtNew.setVisibility(View.VISIBLE);
		}else{
			mTxtNew.setVisibility(View.INVISIBLE);
		}
		mTxtTime.setText(TimeFormatUtil.covert2DisplayTime(meta.time));
		
		if(meta.unReadNum > 0){
			mUnReadLayout.setVisibility(View.VISIBLE);
			int num = meta.unReadNum > 99 ? 99 : meta.unReadNum ;
			mUnReadNum.setText(String.valueOf(num));
		}else{
			mUnReadLayout.setVisibility(View.GONE);
		}
		
		mImageSending.setVisibility(View.GONE);
		mImageSendFail.setVisibility(View.GONE);
		switch(meta.getState()){
			case EgmConstants.Sending_State.SENDING:
				mImageSending.setVisibility(View.VISIBLE);
				break;
			case EgmConstants.Sending_State.SEND_FAIL:
				mImageSendFail.setVisibility(View.VISIBLE);
				break;
		}
		
		switch(meta.msgType){
			case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_TEXT:
				if(!TextUtils.isEmpty(meta.msgContent)){
			    	if (meta.sendType == EgmProtocolConstants.MSG_SENDTYPE.MSG_SENDTYPE_FIRE) {
			    		mTxtMsg.setText("[阅后即焚消息]");
			    	} else {
			    		mTxtMsg.setText(meta.msgContent);
			    	}
			    } else {
			        mTxtMsg.setText("");
			    }
				break;
			case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_AUDIO:
				if (meta.sendType == EgmProtocolConstants.MSG_SENDTYPE.MSG_SENDTYPE_FIRE) {
					mTxtMsg.setText("[阅后即焚消息]");
				} else {
					mTxtMsg.setText("[语音]");
				}
				break;
			case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_LOCAL_PIC:
				if (meta.sendType == EgmProtocolConstants.MSG_SENDTYPE.MSG_SENDTYPE_FIRE) {
					mTxtMsg.setText("[阅后即焚消息]");
				} else {
					mTxtMsg.setText("[照片]");
				}
				break;
			case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_PRIVATE_PIC:
				mTxtMsg.setText("[私照]");
				break;
			case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_GIFT:
			    mTxtMsg.setText("[礼物]");
				break;
			case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_VIDEO:
				if (meta.sendType == EgmProtocolConstants.MSG_SENDTYPE.MSG_SENDTYPE_FIRE) {
					mTxtMsg.setText("[阅后即焚消息]");
				} else {
					mTxtMsg.setText("[视频]");
				}
				break;
			case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_SYS:
				if(!TextUtils.isEmpty(meta.msgContent)){
			        mTxtMsg.setText(meta.msgContent);
			    } else {
			        mTxtMsg.setText("");
			    }
				break;
			case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_FACE:
				if(!TextUtils.isEmpty(meta.msgContent)){
					mTxtMsg.setText(meta.msgContent);
				} else {
					mTxtMsg.setText("[贴图]");
				}
				break;
		}
	}
}
