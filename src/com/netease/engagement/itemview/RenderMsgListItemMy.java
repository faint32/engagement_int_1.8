package com.netease.engagement.itemview;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.date.R;
import com.netease.engagement.adapter.MsgListCursorAdapter;
import com.netease.engagement.adapter.YixinHelper;
import com.netease.engagement.app.EgmConstants;
import com.netease.engagement.dataMgr.MsgAttach;
import com.netease.engagement.view.HeadView;
import com.netease.service.Utils.TimeFormatUtil;
import com.netease.service.db.manager.ManagerAccount;
import com.netease.service.protocol.EgmProtocolConstants;
import com.netease.service.protocol.EgmProtocolConstants.KeyWords_MatchType;
import com.netease.service.protocol.meta.MessageInfo;

public class RenderMsgListItemMy extends RenderMsgListItemBase {
	
	private YixinHelper mYixinHelper;

	private LinearLayout mMyLayout ;
	private TextView mMyTime ;
	private HeadView mMyProfile ;
	private RelativeLayout mMyContainerLayout ;
	private ProgressBar mMyProgress ;
	private ImageView mMyFail ;
	private LinearLayout mKeyWordsLay;
	private ProgressBar mFireProgress;
	private ImageView mFireIcon;
	
	public RenderMsgListItemMy(View root, MsgListCursorAdapter adapter) {
		super(root, adapter);
		
		//自身部分
		mMyLayout = (LinearLayout)root.findViewById(R.id.my_layout);
		mMyTime = (TextView)root.findViewById(R.id.my_msg_time);
		mMyProfile = (HeadView)root.findViewById(R.id.my_profile);
		mMyContainerLayout = (RelativeLayout)root.findViewById(R.id.my_container_layout);
		mMyProgress = (ProgressBar)root.findViewById(R.id.sending);
		mMyFail = (ImageView)root.findViewById(R.id.failed);
		mKeyWordsLay = (LinearLayout)root.findViewById(R.id.keywords_match_tip);
		mFireProgress = (ProgressBar) root.findViewById(R.id.fire_progressbar);
		mFireIcon = (ImageView)root.findViewById(R.id.fire_icon);
		mFireIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(RenderMsgListItemMy.this.root.getContext(), R.string.this_is_fire_message, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	@Override
    public void renderView(MessageInfo msgInfo,boolean timeShow, boolean isOpenFire, String nick){
		super.renderView(msgInfo, timeShow, isOpenFire, nick);
		
		initMyLayout(timeShow);
		renderItemState();
		mKeyWordsLay.setVisibility(View.GONE);
		
		switch (msgInfo.type) {
		case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_TEXT:
			RenderChatText renderChatText = null;
			if (mMyContainerLayout.getTag()!=null && mMyContainerLayout.getTag() instanceof RenderChatText) {
				renderChatText = (RenderChatText) mMyContainerLayout.getTag();
			} else {
				if (mMyContainerLayout.getTag()!=null) {
					mMyContainerLayout.setTag(null);
					mMyContainerLayout.removeAllViews();
				}
				View view = View.inflate(root.getContext(), R.layout.view_chat_text, null);
				renderChatText = new RenderChatText(view);
				renderChatText.setAdapter(adapter);
				renderChatText.setOuterContinaer(this);
				mMyContainerLayout.setTag(renderChatText);
				mMyContainerLayout.addView(view);
			}
			renderChatText.renderView(msgInfo, mNick);
			matchKeyWords(msgInfo);
			break;
		case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_AUDIO:
			RenderAudioItem renderAudioItem = null;
			if (mMyContainerLayout.getTag()!=null && mMyContainerLayout.getTag() instanceof RenderAudioItem) {
				renderAudioItem = (RenderAudioItem) mMyContainerLayout.getTag();
			} else {
				if (mMyContainerLayout.getTag()!=null) {
					mMyContainerLayout.setTag(null);
					mMyContainerLayout.removeAllViews();
				}
				View view = View.inflate(root.getContext(), R.layout.item_view_audio, null);
				renderAudioItem = new RenderAudioItem(view);
				renderAudioItem.setAdapter(adapter);
				renderAudioItem.setOuterContinaer(this);
				mMyContainerLayout.setTag(renderAudioItem);
				mMyContainerLayout.addView(view);
			}
			renderAudioItem.renderView(msgInfo, mNick);
			break;
		case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_GIFT:
			RenderGiftItem renderGiftItem = null;
			if (mMyContainerLayout.getTag()!=null && mMyContainerLayout.getTag() instanceof RenderGiftItem) {
				renderGiftItem = (RenderGiftItem) mMyContainerLayout.getTag();
			} else {
				if (mMyContainerLayout.getTag()!=null) {
					mMyContainerLayout.setTag(null);
					mMyContainerLayout.removeAllViews();
				}
				View view = View.inflate(root.getContext(), R.layout.view_chat_gift_layout, null);
				renderGiftItem = new RenderGiftItem(view);
				renderGiftItem.setAdapter(adapter);
				renderGiftItem.setOuterContinaer(this);
				mMyContainerLayout.setTag(renderGiftItem);
				mMyContainerLayout.addView(view);
			}
			renderGiftItem.renderView(msgInfo, mNick);
			break;
		case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_LOCAL_PIC:
			RenderImageItem renderImageItem = null;
			if (mMyContainerLayout.getTag()!=null && mMyContainerLayout.getTag() instanceof RenderImageItem) {
				renderImageItem = (RenderImageItem) mMyContainerLayout.getTag();
			} else {
				if (mMyContainerLayout.getTag()!=null) {
					mMyContainerLayout.setTag(null);
					mMyContainerLayout.removeAllViews();
				}
				View view = View.inflate(root.getContext(), R.layout.item_view_image, null);
				renderImageItem = new RenderImageItem(view);
				renderImageItem.setAdapter(adapter);
				renderImageItem.setOuterContinaer(this);
				mMyContainerLayout.setTag(renderImageItem);
				mMyContainerLayout.addView(view);
			}
			renderImageItem.renderView(msgInfo, mNick);
			break;
		case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_PRIVATE_PIC:
			RenderImageItem renderImageItem2 = null;
			if (mMyContainerLayout.getTag()!=null && mMyContainerLayout.getTag() instanceof RenderImageItem) {
				renderImageItem2 = (RenderImageItem) mMyContainerLayout.getTag();
			} else {
				if (mMyContainerLayout.getTag()!=null) {
					mMyContainerLayout.setTag(null);
					mMyContainerLayout.removeAllViews();
				}
				View view = View.inflate(root.getContext(), R.layout.item_view_image, null);
				renderImageItem2 = new RenderImageItem(view);
				renderImageItem2.setAdapter(adapter);
				renderImageItem2.setOuterContinaer(this);
				mMyContainerLayout.setTag(renderImageItem2);
				mMyContainerLayout.addView(view);
			}
			renderImageItem2.renderView(msgInfo, mNick);
			break;
		case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_VIDEO:
			RenderVideoItem renderVideoItem = null;
			if (mMyContainerLayout.getTag()!=null && mMyContainerLayout.getTag() instanceof RenderVideoItem) {
				renderVideoItem = (RenderVideoItem) mMyContainerLayout.getTag();
			} else {
				if (mMyContainerLayout.getTag()!=null) {
					mMyContainerLayout.setTag(null);
					mMyContainerLayout.removeAllViews();
				}
				View view = View.inflate(root.getContext(), R.layout.item_view_video, null);
				renderVideoItem = new RenderVideoItem(view);
				renderVideoItem.setAdapter(adapter);
				renderVideoItem.setOuterContinaer(this);
				mMyContainerLayout.setTag(renderVideoItem);
				mMyContainerLayout.addView(view);
			}
			renderVideoItem.renderView(msgInfo, mNick);
			break;
		case EgmProtocolConstants.MSG_TYPE.MSG_TYPE_FACE:
			RenderFaceItem renderFaceItem = null;
			if (mMyContainerLayout.getTag()!=null && mMyContainerLayout.getTag() instanceof RenderFaceItem) {
				renderFaceItem = (RenderFaceItem) mMyContainerLayout.getTag();
			} else {
				if (mMyContainerLayout.getTag()!=null) {
					mMyContainerLayout.setTag(null);
					mMyContainerLayout.removeAllViews();
				}
				View view = View.inflate(root.getContext(), R.layout.item_view_face, null);
				renderFaceItem = new RenderFaceItem(view);
				renderFaceItem.setAdapter(adapter);
				renderFaceItem.setOuterContinaer(this);
				mMyContainerLayout.setTag(renderFaceItem);
				mMyContainerLayout.addView(view);
			}
			renderFaceItem.renderView(msgInfo, mNick);
			break;
		}
	}
	
	@Override
	public ProgressBar getFireProgressBar() {
		return mFireProgress;
	}
	
	/**
	 * 初始化自身布局
	 * @param timeShow
	 */
	private void initMyLayout(boolean timeShow){
		mMyLayout.setVisibility(View.VISIBLE);
		mMyProfile.setImageUrl(false,HeadView.PROFILE_SIZE_SMALL, ManagerAccount.getInstance().getCurrentAvatar(),
				ManagerAccount.getInstance().getCurrentGender());
		if(timeShow){
			mMyTime.setVisibility(View.VISIBLE);
			mMyTime.setText(TimeFormatUtil.covert2DisplayTime(mMsgInfo.time));
			if (isOpenFire) {
				mMyTime.setTextColor(Color.BLACK);
				Drawable left = mMyTime.getContext().getResources().getDrawable(R.drawable.icon_clock_black);
				mMyTime.setCompoundDrawablesWithIntrinsicBounds(left, null, null, null);
			} else {
				mMyTime.setTextColor(mMyTime.getContext().getResources().getColor(R.color.info_audio_txt_color));
				Drawable left = mMyTime.getContext().getResources().getDrawable(R.drawable.icon_clock_gray);
				mMyTime.setCompoundDrawablesWithIntrinsicBounds(left, null, null, null);
			}
		}else{
			mMyTime.setVisibility(View.GONE);
		}
		
		if (mMsgInfo.sendType == EgmProtocolConstants.MSG_SENDTYPE.MSG_SENDTYPE_FIRE) {
			mFireIcon.setVisibility(View.VISIBLE);
			if (isOpenFire) {
				mFireIcon.setBackgroundResource(R.drawable.icon_pgchat_fire_s_white);
			} else {
				mFireIcon.setBackgroundResource(R.drawable.icon_pgchat_fire_s_gray);
			}
		} else {
			mFireIcon.setVisibility(View.GONE);
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
						mYixinHelper.onIsFriend();;
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
}
