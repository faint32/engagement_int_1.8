package com.netease.engagement.itemview;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.internal.ViewCompat;
import com.netease.date.R;
import com.netease.engagement.activity.ActivityUserPage;
import com.netease.engagement.adapter.UploadPictureHelper;
import com.netease.engagement.adapter.YixinHelper;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.MemoryDataCenter;
import com.netease.engagement.dataMgr.MsgAttach;
import com.netease.engagement.view.HeadView;
import com.netease.service.Utils.TimeFormatUtil;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmProtocolConstants.KeyWords_MatchType;
import com.netease.service.protocol.meta.MessageInfo;

public class RenderMsgListItem {
	private LinearLayout mUserLayout ;
	private TextView mUserTime ;
	private HeadView mUserProfile ;
	private LinearLayout mUserAudioLayout ;
	private LinearLayout mUserGiftLayout ;
	private LinearLayout mUserContent ;
	private RelativeLayout mUserImageLayout ;
	private RelativeLayout mUserVideoLayout ;
	
	private LinearLayout mMyLayout ;
	private TextView mMyTime ;
	private HeadView mMyProfile ;
	private LinearLayout mMyAudioLayout ;
	private LinearLayout mMyContent ;
	private RelativeLayout mMyImageLayout ;
	private RelativeLayout mMyVideoLayout ;
	private LinearLayout mMyGiftLayout ;
	private ProgressBar mMyProgress ;
	private ImageView mMyFail ;
	private LinearLayout mKeyWordsLay;
	
	private LinearLayout mSysLayout ;
	private TextView mSysTime ;
	private ImageView mSysProfile ;
	private LinearLayout mSysMsg ;
	
	private String ID = "" ;
	
	private String otherProfile ;
	private String mNick;
	private YixinHelper mYixinHelper;
	
	private boolean isFromUserInfo;
	
	public RenderMsgListItem(View root){
		//对方部分
		mUserLayout = (LinearLayout)root.findViewById(R.id.user_layout);
		mUserTime = (TextView)root.findViewById(R.id.user_msg_time);
		mUserAudioLayout = (LinearLayout)root.findViewById(R.id.user_audio_layout);
		mUserProfile = (HeadView)root.findViewById(R.id.user_profile);
		mUserContent = (LinearLayout)root.findViewById(R.id.user_content);
		mUserImageLayout = (RelativeLayout)root.findViewById(R.id.user_image_layout);
		mUserVideoLayout = (RelativeLayout)root.findViewById(R.id.user_video_layout);
		mUserGiftLayout = (LinearLayout)root.findViewById(R.id.user_gift_layout);
		
		//自身部分
		mMyLayout = (LinearLayout)root.findViewById(R.id.my_layout);
		mMyTime = (TextView)root.findViewById(R.id.my_msg_time);
		mMyProfile = (HeadView)root.findViewById(R.id.my_profile);
		mMyContent = (LinearLayout)root.findViewById(R.id.my_content);
		mMyAudioLayout = (LinearLayout)root.findViewById(R.id.my_audio_layout);
		mMyImageLayout = (RelativeLayout)root.findViewById(R.id.my_image_layout);
		mMyVideoLayout = (RelativeLayout)root.findViewById(R.id.my_video_layout);
		mMyGiftLayout = (LinearLayout)root.findViewById(R.id.my_gift_layout);
		mMyProgress = (ProgressBar)root.findViewById(R.id.sending);
		mMyFail = (ImageView)root.findViewById(R.id.failed);
		mKeyWordsLay = (LinearLayout)root.findViewById(R.id.keywords_match_tip);
		
		mSysLayout = (LinearLayout)root.findViewById(R.id.system_msg_layout);
		mSysTime = (TextView)root.findViewById(R.id.system_msg_time);
		mSysProfile = (ImageView)root.findViewById(R.id.system_profile);
		mSysMsg = (LinearLayout)root.findViewById(R.id.sys_msg);
		
		if(MemoryDataCenter.getInstance().get(MemoryDataCenter.CURRENT_CHAT_OTHER_PROFILE) != null){
			otherProfile = ((String)MemoryDataCenter.getInstance().get(MemoryDataCenter.CURRENT_CHAT_OTHER_PROFILE));
		}
		if(ManagerAccount.getInstance().getCurrentAccount() != null){
            ID = ManagerAccount.getInstance().getCurrentIdString();
        }
		
	}
	
	private UploadPictureHelper mUploadPictureHelper;
    public void setUploadPictureHelper(UploadPictureHelper helper){
        mUploadPictureHelper = helper;
    }
	
	private MessageInfo mMsgInfo ;
	
	public void renderView(MessageInfo msgInfo,boolean timeShow, String nick){
		if(msgInfo == null){
			return ;
		}
		this.mNick = nick;
		
		mMsgInfo = msgInfo ;
		
		String sendId = String.valueOf(msgInfo.sender);
		
		//消息属于自身
		if(sendId.equals(ID)){
			initMyLayout(timeShow);
			renderItemState();
			mKeyWordsLay.setVisibility(View.GONE);
			switch(msgInfo.type){
				case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_TEXT:
					mMyContent.setVisibility(View.VISIBLE);
					getChatTextRender(mMyContent).renderView(msgInfo, mNick);
//					matchKeyWords(msgInfo);
					break ;
				case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_AUDIO:
					mMyAudioLayout.setVisibility(View.VISIBLE);
					getAudioRender(mMyAudioLayout).renderView(msgInfo, mNick);
					break;
				case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_GIFT:
					mMyGiftLayout.setVisibility(View.VISIBLE);
					getGiftRender(mMyGiftLayout).renderView(msgInfo, mNick);
					break;
				case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_LOCAL_PIC:
					mMyImageLayout.setVisibility(View.VISIBLE);
					getImageRender(mMyImageLayout).renderView(msgInfo, mNick);
					break;
				case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_PRIVATE_PIC:
					mMyImageLayout.setVisibility(View.VISIBLE);
					getImageRender(mMyImageLayout).renderView(msgInfo, mNick);
					break;
				case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_VIDEO:
					mMyVideoLayout.setVisibility(View.VISIBLE);
					getVideoRender(mMyVideoLayout).renderView(msgInfo, mNick);
					break;
			}
		}else if(Long.parseLong(sendId) == EgmConstants.System_Sender_Id.TYPE_XIAOAI
				|| Long.parseLong(sendId) == EgmConstants.System_Sender_Id.TYPE_YIXIN){
			initSysLayout(timeShow);
			getSysMsgRender(mSysMsg).renderView(msgInfo, mNick);
		}else{
			initUserLayout(timeShow);
			switch(msgInfo.type){
				case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_TEXT:
					mUserContent.setVisibility(View.VISIBLE);
					getChatTextRender(mUserContent).renderView(msgInfo, mNick);
					break;
				case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_LOCAL_PIC:
					mUserImageLayout.setVisibility(View.VISIBLE);
					getImageRender(mUserImageLayout).renderView(msgInfo, mNick);
					break;
				case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_PRIVATE_PIC:
					mUserImageLayout.setVisibility(View.VISIBLE);
					getImageRender(mUserImageLayout).renderView(msgInfo, mNick);
					break;
				case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_GIFT:
					mUserGiftLayout.setVisibility(View.VISIBLE);
					getGiftRender(mUserGiftLayout).renderView(msgInfo, mNick);
					break;
				case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_AUDIO:
					mUserAudioLayout.setVisibility(View.VISIBLE);
					getOtherAudioRender(mUserAudioLayout).renderView(msgInfo, mNick);
					break;
				case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_VIDEO:
					mUserVideoLayout.setVisibility(View.VISIBLE);
					getVideoRender(mUserVideoLayout).renderView(msgInfo, mNick);
					break;
				case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_SYS:
					break;
			}
		}
	}
	
	/**
	 * 处理自身发送的消息有三种不同的状态
	 */
	private void renderItemState(){
		switch(mMsgInfo.status){
			case EgmConstants.Sending_State.SENDING:
				if(mMsgInfo.type == EgmProtocolConstants.MSG_TYPE.MSG_TYPE_TEXT 
						|| mMsgInfo.type == EgmProtocolConstants.MSG_TYPE.MSG_TYPE_VIDEO
						|| mMsgInfo.type == EgmProtocolConstants.MSG_TYPE.MSG_TYPE_GIFT){
					mMyProgress.setVisibility(View.VISIBLE);
				}else{
					mMyProgress.setVisibility(View.GONE);
				}
				mMyFail.setVisibility(View.GONE);
				break;
			case EgmConstants.Sending_State.SEND_SUCCESS:
				mMyProgress.setVisibility(View.GONE);
				mMyFail.setVisibility(View.GONE);
				break;
			case EgmConstants.Sending_State.SEND_FAIL:
				if(mMsgInfo.type != EgmProtocolConstants.MSG_TYPE.MSG_TYPE_AUDIO){
					mMyFail.setVisibility(View.VISIBLE);
				}else{
					mMyFail.setVisibility(View.GONE);
				}
				mMyProgress.setVisibility(View.GONE);
				break;
		}
	}
	
	/**
	 * 初始化自身布局
	 * @param timeShow
	 */
	private void initMyLayout(boolean timeShow){
		mUserLayout.setVisibility(View.GONE);
		mSysLayout.setVisibility(View.GONE);
		
		mMyLayout.setVisibility(View.VISIBLE);
		mMyContent.setVisibility(View.GONE);
		mMyImageLayout.setVisibility(View.GONE);
		mMyVideoLayout.setVisibility(View.GONE);
		mMyAudioLayout.setVisibility(View.GONE);
		mMyGiftLayout.setVisibility(View.GONE);
		mMyProfile.setImageUrl(false,HeadView.PROFILE_SIZE_SMALL, ManagerAccount.getInstance().getCurrentAvatar(),
				ManagerAccount.getInstance().getCurrentGender());
		if(timeShow){
			mMyTime.setVisibility(View.VISIBLE);
			mMyTime.setText(TimeFormatUtil.covert2DisplayTime(mMsgInfo.time));
		}else{
			mMyTime.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 初始化对方布局
	 * @param timeShow
	 */
	private void initUserLayout(boolean timeShow){
		mUserLayout.setVisibility(View.VISIBLE);
		mSysLayout.setVisibility(View.GONE);
		mMyLayout.setVisibility(View.GONE);
		mUserContent.setVisibility(View.GONE);
		mUserAudioLayout.setVisibility(View.GONE);
		mUserImageLayout.setVisibility(View.GONE);
		mUserVideoLayout.setVisibility(View.GONE);
		mUserGiftLayout.setVisibility(View.GONE);
		if(ManagerAccount.getInstance().getCurrentGender() == EgmConstants.SexType.Female) {
			mUserProfile.setImageUrl(false,HeadView.PROFILE_SIZE_SMALL,otherProfile, 
					EgmConstants.SexType.Male);
		} else {
			mUserProfile.setImageUrl(false,HeadView.PROFILE_SIZE_SMALL,otherProfile, 
					EgmConstants.SexType.Female);
		}
		mUserProfile.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int gender = ManagerAccount.getInstance().getCurrentGender() ^ 1 ;
				if (isFromUserInfo) {
					Context context = mUserProfile.getContext();
			        if(context instanceof Activity){
			        	Activity activity = (Activity) context;
			        	activity.finish();
			        }
				} else {
					ActivityUserPage.startActivity(
							mUserProfile.getContext(),
							String.valueOf(mMsgInfo.sender), 
							String.valueOf(gender));		
				}
			}
		});
		if(timeShow){
			mUserTime.setVisibility(View.VISIBLE);
			mUserTime.setText(TimeFormatUtil.covert2DisplayTime(mMsgInfo.time));
		}else{
			mUserTime.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 初始化系统布局
	 * @param msgInfo
	 * @param timeShow
	 */
	private void initSysLayout(boolean timeShow){
		mUserLayout.setVisibility(View.GONE);
		mMyLayout.setVisibility(View.GONE);
		mSysLayout.setVisibility(View.VISIBLE);
		if(mMsgInfo.sender == EgmConstants.System_Sender_Id.TYPE_XIAOAI){
			ViewCompat.setBackground(mSysProfile,mSysLayout.getContext().getResources().getDrawable(R.drawable.icon_mesg_portrait_ai));
		}else if(mMsgInfo.sender == EgmConstants.System_Sender_Id.TYPE_YIXIN){
			ViewCompat.setBackground(mSysProfile,mSysLayout.getContext().getResources().getDrawable(R.drawable.icon_mesg_portrait_yixin));
		}
		
		if(timeShow){
			mSysTime.setVisibility(View.VISIBLE);
			mSysTime.setText(TimeFormatUtil.covert2DisplayTime(mMsgInfo.time));
		}else{
			mSysTime.setVisibility(View.GONE);
		}
	}
	
	private RenderChatText getChatTextRender(View view){
		RenderChatText render = null ;
		if(view.getTag() != null && view.getTag() instanceof RenderChatText){
			render = (RenderChatText) view.getTag() ;
		}else{
			render = new RenderChatText(view);
			view.setTag(render);
		}
		return render ;
	}
	
	private RenderAudioItem getAudioRender(View view){
		RenderAudioItem render = null ;
		if(view.getTag() != null && view.getTag() instanceof RenderAudioItem){
			render = (RenderAudioItem) view.getTag() ;
		}else{
			render = new RenderAudioItem(view);
			view.setTag(render);
		}
		return render ;
	}
	
	private RenderOtherAudioItem getOtherAudioRender(View view){
		RenderOtherAudioItem render = null ;
		if(view.getTag() != null && view.getTag() instanceof RenderOtherAudioItem){
			render = (RenderOtherAudioItem) view.getTag() ;
		}else{
			render = new RenderOtherAudioItem(view);
			view.setTag(render);
		}
		return render ;
	}
	
	private RenderImageItem getImageRender(View view){
		RenderImageItem render = null ;
		if(view.getTag() != null && view.getTag() instanceof RenderImageItem){
			render = (RenderImageItem) view.getTag() ;
		}else{
			render = new RenderImageItem(view);
			view.setTag(render);
		}
		return render ;
	}
	
	private RenderVideoItem getVideoRender(View view){
		RenderVideoItem render = null ;
		if(view.getTag() != null && view.getTag() instanceof RenderVideoItem){
			render = (RenderVideoItem) view.getTag() ;
		}else{
			render = new RenderVideoItem(view);
			view.setTag(render);
		}
		return render ;
	}
	
	private RenderGiftItem getGiftRender(View view){
		RenderGiftItem render = null ;
		if(view.getTag() != null && view.getTag() instanceof RenderGiftItem){
			render = (RenderGiftItem)view.getTag();
		}else{
			render = new RenderGiftItem(view);
			view.setTag(render);
		}
		return render ;
	}
	
	private RenderSysMsgItem getSysMsgRender(View view){
		RenderSysMsgItem render = null ;
		if(view.getTag() != null && view.getTag() instanceof RenderSysMsgItem){
			render = (RenderSysMsgItem)view.getTag();
		}else{
			render = new RenderSysMsgItem(view);
			render.setUploadPictureHelper(mUploadPictureHelper);
			view.setTag(render);
		}
		return render ;
	}
	
	private void matchKeyWords(final MessageInfo msgInfo){
		if(!TextUtils.isEmpty(msgInfo.attach)){
			MsgAttach attach = MsgAttach.toMsgAttach(msgInfo.attach);
			if(attach.matchType > 0 && !TextUtils.isEmpty(attach.tips)){
				TextView keywordTx = (TextView)mKeyWordsLay.findViewById(R.id.keywords_match_text);
				keywordTx.setText(attach.tips);
				TextView keywordOpTx = (TextView)mKeyWordsLay.findViewById(R.id.keywords_match_operation);
				if(attach.matchType == KeyWords_MatchType.MATCH_WEIXIN_IS_FRIENDS){
					keywordOpTx.setText(R.string.keywords_match_use_yixin);
				}else if(attach.matchType == KeyWords_MatchType.MATCH_WEIXIN_ISNOT_FRIENDS){
					keywordOpTx.setText(R.string.keywords_match_add_friends);
				}
				keywordOpTx.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if(mYixinHelper == null){
							mYixinHelper = new YixinHelper(mKeyWordsLay.getContext(), YixinHelper.TYPE_FROM_CHAT_ADD_FRIEND);
						}
						mYixinHelper.addFriend(msgInfo.receiver);
					}
				});
				mKeyWordsLay.setVisibility(View.VISIBLE);
			} else {
				mKeyWordsLay.setVisibility(View.GONE);
			}
		} else{
			mKeyWordsLay.setVisibility(View.GONE);
		}
	}

	public void setFromUserInfo(boolean isFromUserInfo) {
		this.isFromUserInfo = isFromUserInfo;
	}

}
