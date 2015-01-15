package com.netease.engagement.itemview;

import java.io.File;

import android.app.AlertDialog;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.netease.date.R;
import com.netease.engagement.activity.ActivityChatImage;
import com.netease.engagement.activity.ActivityFire;
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


public class RenderImageItem extends RenderBase {
	
	private View mRoot ;
	private LoadingImageView mLoadingImageView ;
	private ProgressBar mProgressBar ;
	
	private MessageInfo mMsgInfo ;
	private int mImageWidth = 120 ;
	private long myId ;
	private String mNick ;
	
	private ImageView private_image_icon;
	
	private RelativeLayout foreImageRL;
	
	public RenderImageItem(View root){
		mRoot = root ;
		mImageWidth = EgmUtil.dip2px(root.getContext(), mImageWidth);
		mLoadingImageView = (LoadingImageView)root.findViewById(R.id.image);
		mProgressBar = (ProgressBar)root.findViewById(R.id.progress);
		private_image_icon = (ImageView) root.findViewById(R.id.private_image_icon);
		foreImageRL = (RelativeLayout) root.findViewById(R.id.foreImageRL);
		
		myId = ManagerAccount.getInstance().getCurrentId();
		
		if(MemoryDataCenter.getInstance().get(MemoryDataCenter.CURRENT_CAHT_OTHER_NICK) != null){
			mNick = (String)(MemoryDataCenter.getInstance().get(MemoryDataCenter.CURRENT_CAHT_OTHER_NICK));
		}
	}
	
	public void renderView(final MessageInfo msgInfo, String nick){
		mNick = nick;
		
		mMsgInfo = msgInfo ;
		mProgressBar.setVisibility(View.GONE);
		
		mLoadingImageView.setImageBitmap(null);
		String url = null ;
		MsgAttach msgAttach = null ;
		
	    if(!TextUtils.isEmpty(msgInfo.attach)){
	    	msgAttach = MsgAttach.toMsgAttach(msgInfo.attach);
	    }
	    
	    if(private_image_icon != null) {
			private_image_icon.setVisibility(View.GONE);
		}
		if(foreImageRL != null) {
			foreImageRL.setVisibility(View.GONE);
		}
	    
		switch(msgInfo.type){
			case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_LOCAL_PIC:
				/**
				 * 本地图片
				 */
				if(msgAttach != null && !TextUtils.isEmpty(msgAttach.smallImagePath)){
					url = Uri.fromFile(new File(msgAttach.smallImagePath)).toString();
					mLoadingImageView.setLoadingImage(url);
				}else{
					//接收消息，网络图片
					mLoadingImageView.setServerClipSize(mImageWidth, mImageWidth);
					mLoadingImageView.setLoadingImage(msgInfo.mediaUrl);
				}
				break;
			case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_PRIVATE_PIC:
				/**
				 * 私照
				 */
				if(msgInfo.sender == myId && msgAttach != null && !TextUtils.isEmpty(msgAttach.smallImagePath)){
					mLoadingImageView.setLoadingImage(msgAttach.smallImagePath);
				}else{
					mLoadingImageView.setServerClipSize(mImageWidth, mImageWidth);
					mLoadingImageView.setLoadingImage(msgInfo.mediaUrl);
				}
				if (msgInfo.sender == myId) {
					if(foreImageRL != null) {
						foreImageRL.setVisibility(View.VISIBLE);
					}
				} else {
					if(private_image_icon != null) {
						private_image_icon.setVisibility(View.VISIBLE);
					}
				}
				
				break;
		}
		
		mLoadingImageView.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				switch(msgInfo.type){
					case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_LOCAL_PIC:
						if (msgInfo.sendType == EgmProtocolConstants.MSG_SENDTYPE.MSG_SENDTYPE_FIRE
							&& msgInfo.sender != myId) {
							Long time = adapter.getFireStart(msgInfo);
							if (time == null) {
								time = 0l;
							}
							FragmentPrivateSession.notNeedRefresh = true;
							ActivityFire.startActivityForImage(v.getContext(), msgInfo, time);
						} else {
							FragmentPrivateSession.notNeedRefresh = true;
							ActivityChatImage.startActivity(mRoot.getContext(), msgInfo.mediaUrl,msgInfo.isCameraPhoto);//标示符
						}
						break;
					case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_PRIVATE_PIC:
						// 自己发送的私照，查看大图，是不用加锁的  bug fix #140783  by gzlichangjie
						FragmentPrivateSession.notNeedRefresh = true;
						boolean isPrivate = (msgInfo.sender == myId) ? false : true;
						ActivityChatImage.startActivity(
								mRoot.getContext(),
								msgInfo.mediaUrl,
								isPrivate,
								msgInfo.sender,
								msgInfo.extraId);
						break;
				}
			}
		});
		
		mLoadingImageView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if(msgInfo.status != EgmConstants.Sending_State.SENDING){
					showDelChatItemDialog();
				}				
				return true;
			}
		});
		
		if(msgInfo.sender == myId){
			switch(msgInfo.status){
				case EgmConstants.Sending_State.SENDING:
					mProgressBar.setVisibility(View.VISIBLE);
					break;
				case EgmConstants.Sending_State.SEND_SUCCESS:
					mProgressBar.setVisibility(View.GONE);
					break;
				case EgmConstants.Sending_State.SEND_FAIL:
					mProgressBar.setVisibility(View.GONE);
					break;
			}
		}
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
